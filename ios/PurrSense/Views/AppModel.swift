import SwiftUI

enum Route: Hashable {
    case result, addCat, cat(Cat)
}

@MainActor
final class AppModel: ObservableObject {
    @Published var ready = false
    @Published var busy: String?
    @Published var error: String?
    @Published var onDeviceStatus = "checking…"
    @Published var cats: [Cat] = []
    @Published var path: [Route] = []

    @Published var photo: UIImage?
    @Published var analysis: Analysis?
    @Published var suggestedAppearance = ""
    /// Embedding of the photo on the "add cat" screen, saved with the new cat.
    private var pendingEmbedding: [Float] = []
    @Published var history: [Reading] = []
    @Published var trend: (String, Brain)?

    private let repo = Repository()
    private var brain: CatBrain!
    private var config = AppConfig()
    private var uid = ""

    func start() async {
        do {
            config = await RemoteConfigService.load()
            brain = CatBrain(config: config)
            onDeviceStatus = brain.onDeviceStatus
            uid = try await repo.ensureSignedIn()
            cats = try await repo.listCats(uid: uid)
            ready = true
        } catch {
            self.error = "Startup failed: \(error.localizedDescription)"
        }
    }

    /// Full pipeline: Vision -> See -> Who -> Care -> (rules) -> Deep Check -> save.
    func analyze(_ image: UIImage) async {
        photo = image
        var trace: [TraceStep] = []
        do {
            busy = "Reading your cat's face…"
            guard let quick = try await brain.quickRead(image, trace: &trace), quick.cat, quick.faceVisible else {
                busy = nil
                error = "No cat face found. Try a clear, front-facing photo."
                return
            }
            busy = "Who's this?"
            let cat = try await brain.identify(embedding: quick.embedding, observed: quick.appearance, cats: cats, trace: &trace)

            busy = "Writing a note…"
            let note = try await brain.careNote(name: cat?.name ?? "your cat", read: quick, trace: &trace)

            var deep: DeepCheck?
            if brain.needsDeepCheck(quick) {
                trace.append(TraceStep(step: "Escalate? pain \(quick.fgs.painScore(offset: config.painOffset)) ≥ \(config.grimaceThreshold) or signs",
                                       brain: .rules, millis: 0))
                busy = "Score is elevated: asking the cloud for a Deep Check…"
                deep = try? await brain.deepCheck(image, trace: &trace) // offline: skip gracefully
            }

            let a = Analysis(quick: quick, matchedCat: cat, careNote: note, deep: deep,
                             trace: trace, alertThreshold: config.grimaceThreshold, painOffset: config.painOffset)
            repo.saveReading(uid: uid, catId: cat?.id ?? "unknown", analysis: a)
            analysis = a
            busy = nil
            path.append(.result)
        } catch {
            busy = nil
            self.error = "Analysis failed: \(error.localizedDescription)"
        }
    }

    func startAddCat(_ image: UIImage) async {
        photo = image
        var trace: [TraceStep] = []
        busy = "Learning what this cat looks like…"
        defer { busy = nil }
        do {
            guard let quick = try await brain.quickRead(image, trace: &trace), quick.cat else {
                error = "No cat found in that photo."
                return
            }
            suggestedAppearance = quick.appearance
            pendingEmbedding = quick.embedding
            path.append(.addCat)
        } catch {
            self.error = error.localizedDescription
        }
    }

    func saveCat(name: String, appearance: String) {
        let cat = repo.addCat(uid: uid,
                              name: name.trimmingCharacters(in: .whitespaces),
                              appearance: appearance.trimmingCharacters(in: .whitespaces),
                              embedding: pendingEmbedding)
        pendingEmbedding = []
        cats.append(cat)
        path.removeAll()
    }

    func loadHistory(for cat: Cat) async {
        history = []
        trend = nil
        do {
            history = try await repo.recentReadings(uid: uid, catId: cat.id)
            guard !history.isEmpty else { return }
            let fmt = DateFormatter()
            fmt.dateFormat = "EEE d MMM"
            let lines = history.prefix(10).map { r in
                "\(fmt.string(from: Date(timeIntervalSince1970: r.createdAt / 1000))): mood \(r.mood), pain \(r.painScore)/10"
                    + (r.signs.isEmpty ? "" : ", signs: \(r.signs.joined(separator: ", "))")
            }
            trend = try await brain.trend(name: cat.name, lines: lines)
        } catch {
            self.error = "Could not load history: \(error.localizedDescription)"
        }
    }
}
