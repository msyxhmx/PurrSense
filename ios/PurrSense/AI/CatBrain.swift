import UIKit
import FirebaseAILogic

/// The two brains of PurrSense on iOS. ALL Firebase AI Logic calls live in this file.
///
/// iOS split (Apple's on-device Foundation Model is text-only today):
/// - See:   Apple Vision on-device cat check + crop  ->  Gemini (cloud) reads the face.
/// - Who / Care / Trend:  Firebase AI Logic **hybrid** session, Apple Foundation Models first,
///   Gemini in the cloud as fallback when Apple Intelligence is unavailable.
/// - Deep Check: larger cloud Gemini model, only when the rules escalate.
final class CatBrain {

    private let config: AppConfig
    private let ai = FirebaseAI.firebaseAI(backend: .googleAI())
    private let visionModel: GenerativeModel
    private let deepModel: GenerativeModel

    init(config: AppConfig) {
        self.config = config
        let json = GenerationConfig(temperature: 0.2, responseMIMEType: "application/json")
        visionModel = ai.generativeModel(modelName: config.cloudQuickModel, generationConfig: json)
        deepModel = ai.generativeModel(modelName: config.cloudDeepModel, generationConfig: json)
    }

    // MARK: - On-device status

    var onDeviceStatus: String {
        let model = FirebaseAI.SystemLanguageModel.default
        if model.isAvailable { return "AVAILABLE" }
        return String(describing: model.availability) // e.g. unavailable(.appleIntelligenceNotEnabled)
    }

    // MARK: - Hybrid text generation

    /// One fresh session per request: GenerativeModelSession keeps chat history, which we don't want.
    /// NOTE: the Firebase docs show two spellings for this (HybridModel(...) and .hybridModel(...)).
    /// If this line doesn't compile with your SDK version, use:
    ///   ai.generativeModelSession(model: .hybridModel(primary: systemModel, secondary: cloudModel))
    private func hybridText(_ prompt: String) async throws -> (String, Brain) {
        let systemModel = FirebaseAI.SystemLanguageModel.default
        let cloudModel = ai.geminiModel(name: config.cloudQuickModel)
        let session = ai.generativeModelSession(model: HybridModel(primary: systemModel, secondary: cloudModel))
        let response = try await session.respond(to: prompt)
        // On-device responses report "apple-foundation-models-system-language-model".
        let version = response.rawResponse.modelVersion ?? ""
        let brain: Brain = version.contains("apple") ? .onDevice : .cloud
        return (response.content, brain)
    }

    // MARK: - Pipeline steps

    /// Step 0 + 1 "See". Returns nil if Vision finds no cat (nothing is uploaded in that case).
    func quickRead(_ photo: UIImage, trace: inout [TraceStep]) async throws -> QuickRead? {
        var t0 = Date()
        let vision = try await CatVision.detectCat(in: photo)
        trace.append(TraceStep(step: "Cat check + crop (Apple Vision)", brain: .onDevice, millis: ms(since: t0)))
        guard vision.isCat else { return nil }

        t0 = Date()
        let small = vision.crop.scaled(maxSide: 768)
        let response = try await visionModel.generateContent(small, Prompts.quickRead)
        trace.append(TraceStep(step: "See (mood + grimace)", brain: .cloud, millis: ms(since: t0)))
        var parsed = Self.parse(QuickRead.self, response.text)
        if parsed == nil {
            // The model occasionally breaks the JSON format: retry once before giving up.
            t0 = Date()
            let retry = try await visionModel.generateContent(small, Prompts.quickRead)
            trace.append(TraceStep(step: "See (retry)", brain: .cloud, millis: ms(since: t0)))
            parsed = Self.parse(QuickRead.self, retry.text)
        }
        // Unreadable twice: say so, rather than reporting "no cat" for a photo that has one.
        guard var read = parsed else { throw UnreadableAnswerError() }

        t0 = Date()
        read.embedding = (try? CatVision.featurePrint(of: vision.crop)) ?? []
        if !read.embedding.isEmpty {
            trace.append(TraceStep(step: "Photo embedding (Apple Vision)", brain: .onDevice, millis: ms(since: t0)))
        }
        return read
    }

    /// Step 2 "Who". Photo matching first: compare the photo's on-device embedding with each enrolled
    /// cat's, and accept the best only above `cat_id_min_similarity`. The research found text
    /// descriptions identify cats about 54% of the time versus 85-89% for image embeddings.
    /// Cats registered before photo matching have no embedding and fall back to text matching.
    func identify(embedding: [Float], observed: String, cats: [Cat], trace: inout [TraceStep]) async throws -> Cat? {
        guard !cats.isEmpty else { return nil }
        let enrolled = cats.filter { !$0.embedding.isEmpty }
        if !embedding.isEmpty,
           let top = enrolled.map({ ($0, CatVision.cosine(embedding, $0.embedding)) }).max(by: { $0.1 < $1.1 }) {
            let (best, score) = top
            trace.append(TraceStep(step: "Who (photo match \(String(format: "%.2f", score)))", brain: .onDevice, millis: 0))
            if score >= config.catIdMinSimilarity { return best }
            // Every cat has an embedding and none matched well enough: this cat isn't registered.
            if enrolled.count == cats.count { return nil }
        }
        let legacy = cats.filter { $0.embedding.isEmpty }
        guard !legacy.isEmpty, !observed.isEmpty else { return nil }
        if cats.count == 1 {
            trace.append(TraceStep(step: "Who (single cat)", brain: .rules, millis: 0))
            return cats[0]
        }
        let t0 = Date()
        let (text, brain) = try await hybridText(Prompts.identify(observed: observed, cats: legacy))
        trace.append(TraceStep(step: "Who (identify)", brain: brain, millis: ms(since: t0)))
        guard let m = Self.parse(IdMatch.self, text), m.confidence >= 0.5 else { return nil }
        return legacy.first { $0.id == m.catId }
    }

    /// Step 3 "Care": owner-friendly explanation, on-device first.
    func careNote(name: String, read: QuickRead, trace: inout [TraceStep]) async throws -> String {
        let t0 = Date()
        let (text, brain) = try await hybridText(Prompts.careNote(name: name, read: read, threshold: config.grimaceThreshold, painOffset: config.painOffset))
        trace.append(TraceStep(step: "Care note", brain: brain, millis: ms(since: t0)))
        return text.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    /// Rule-based escalation: fully explainable, no model involved. Uses the corrected pain score
    /// (raw total + Remote Config offset), because Gemini under-scores FGS pain.
    func needsDeepCheck(_ read: QuickRead) -> Bool {
        Escalation.needsDeepCheck(read, threshold: config.grimaceThreshold, painOffset: config.painOffset)
    }

    /// Step 4 "Deep Check": larger cloud model on a higher-resolution crop.
    func deepCheck(_ photo: UIImage, trace: inout [TraceStep]) async throws -> DeepCheck? {
        let crop = (try? await CatVision.detectCat(in: photo).crop) ?? photo
        let t0 = Date()
        let response = try await deepModel.generateContent(crop.scaled(maxSide: 1280), Prompts.deepCheck)
        trace.append(TraceStep(step: "Deep Check", brain: .cloud, millis: ms(since: t0)))
        return Self.parse(DeepCheck.self, response.text)
    }

    /// Weekly trend from stored readings: text only, on-device first, works offline.
    func trend(name: String, lines: [String]) async throws -> (String, Brain) {
        let (text, brain) = try await hybridText(Prompts.trend(name: name, lines: lines, threshold: config.grimaceThreshold))
        return (text.trimmingCharacters(in: .whitespacesAndNewlines), brain)
    }

    // MARK: - Helpers

    /// Models sometimes wrap JSON in prose or code fences or get types slightly wrong: see ModelJSON.
    static func parse<T: Decodable>(_ type: T.Type, _ text: String?) -> T? {
        ModelJSON.decode(T.self, from: text)
    }

    private func ms(since d: Date) -> Int { Int(Date().timeIntervalSince(d) * 1000) }
}
