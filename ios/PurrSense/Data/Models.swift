import Foundation

/// Where a pipeline step actually ran. Shown as a badge in the "TwoBrain trace".
enum Brain: String, Codable {
    case onDevice = "ON_DEVICE", cloud = "CLOUD", rules = "RULES"
}

struct TraceStep: Identifiable {
    let id = UUID()
    let step: String
    let brain: Brain
    let millis: Int
}

/// `embedding` is an image embedding of the enrolment photo, used to recognise the cat. It is empty
/// for cats registered before photo matching existed, which fall back to matching `appearance` text.
struct Cat: Identifiable, Hashable {
    var id: String
    var name: String
    var appearance: String
    var embedding: [Float] = []
    var createdAt: Double = Date().timeIntervalSince1970 * 1000
}

/// Feline Grimace Scale (Evangelista et al., 2019): five action units scored 0-2, total /10.
/// Total >= 4 is the published threshold suggesting pain / re-assessment.
struct Grimace: Decodable {
    var ears = 0, eyes = 0, muzzle = 0, whiskers = 0, head = 0
    var total: Int { [ears, eyes, muzzle, whiskers, head].map { min(max($0, 0), 2) }.reduce(0, +) }
    var asMap: [String: Int] { ["ears": ears, "eyes": eyes, "muzzle": muzzle, "whiskers": whiskers, "head": head] }

    /// Pain score used for the alert: the raw total plus a correction for Gemini under-scoring.
    /// A Sci Rep 2025 study found chatbots score FGS pain too low (Gemini 1.5 Pro about 2.4/10 low),
    /// so a cat truly at 5 would often miss the >= 4 alert. The offset comes from Remote Config
    /// (`gemini_pain_offset`) and is an interim fix until a landmark-based scorer replaces Gemini
    /// (see docs/RESEARCH_HANDOFF.md).
    func painScore(offset: Int) -> Int { min(max(total + offset, 0), 10) }

    init() {}
    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        ears = (try? c.decode(Int.self, forKey: .ears)) ?? 0
        eyes = (try? c.decode(Int.self, forKey: .eyes)) ?? 0
        muzzle = (try? c.decode(Int.self, forKey: .muzzle)) ?? 0
        whiskers = (try? c.decode(Int.self, forKey: .whiskers)) ?? 0
        head = (try? c.decode(Int.self, forKey: .head)) ?? 0
    }
    enum CodingKeys: String, CodingKey { case ears, eyes, muzzle, whiskers, head }
}

/// Mood is positive / neutral / negative: the research found no scientific basis for finer cat
/// mood labels. Pain is read from the grimace score, not from mood. `normalize` also maps the
/// older 8 labels, so earlier readings and off-format model answers still display sensibly.
enum Mood {
    static func normalize(_ raw: String) -> String {
        switch raw.trimmingCharacters(in: .whitespaces).lowercased() {
        case "positive", "relaxed", "playful", "content", "happy": return "positive"
        case "neutral", "alert", "sleepy", "calm": return "neutral"
        case "negative", "anxious", "fearful", "irritated", "uncomfortable", "stressed": return "negative"
        default: return "unknown"
        }
    }
}

/// Output of the "See" step. Decoded leniently: missing fields fall back to defaults.
struct QuickRead: Decodable {
    var cat = false
    var faceVisible = false
    var appearance = ""
    var mood = "unknown"
    var moodConfidence = 0.0
    var fgs = Grimace()
    var signs: [String] = []
    /// On-device embedding of the cat crop; not part of the model's JSON.
    var embedding: [Float] = []

    init() {}
    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        cat = (try? c.decode(Bool.self, forKey: .cat)) ?? false
        faceVisible = (try? c.decode(Bool.self, forKey: .faceVisible)) ?? false
        appearance = (try? c.decode(String.self, forKey: .appearance)) ?? ""
        mood = Mood.normalize((try? c.decode(String.self, forKey: .mood)) ?? "")
        moodConfidence = (try? c.decode(Double.self, forKey: .moodConfidence)) ?? 0
        fgs = (try? c.decode(Grimace.self, forKey: .fgs)) ?? Grimace()
        signs = (try? c.decode([String].self, forKey: .signs)) ?? []
    }
    enum CodingKeys: String, CodingKey {
        case cat, appearance, mood, fgs, signs
        case faceVisible = "face_visible", moodConfidence = "mood_confidence"
    }
}

struct IdMatch: Decodable {
    var catId: String
    var confidence: Double
    enum CodingKeys: String, CodingKey { case catId = "cat_id", confidence }
}

struct DeepCheck: Decodable {
    var urgency = "monitor" // none | monitor | vet_soon | vet_urgent
    var fgs = Grimace()
    var findings: [String] = []
    var summary = ""

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        urgency = (try? c.decode(String.self, forKey: .urgency)) ?? "monitor"
        fgs = (try? c.decode(Grimace.self, forKey: .fgs)) ?? Grimace()
        findings = (try? c.decode([String].self, forKey: .findings)) ?? []
        summary = (try? c.decode(String.self, forKey: .summary)) ?? ""
    }
    enum CodingKeys: String, CodingKey { case urgency, fgs, findings, summary }
}

/// Stored in Firestore. The photo itself is never uploaded or stored.
struct Reading: Identifiable {
    var id: String
    var catId: String
    var mood: String
    var moodConfidence: Double
    var grimaceTotal: Int // raw model total
    var painScore: Int    // grimaceTotal + pain offset, used for the alert
    var signs: [String]
    var careNote: String
    var deepUrgency: String?
    var deepSummary: String?
    var createdAt: Double
}

struct Analysis {
    let quick: QuickRead
    let matchedCat: Cat?
    let careNote: String
    let deep: DeepCheck?
    let trace: [TraceStep]
    let alertThreshold: Int
    let painOffset: Int
    /// The Deep Check's scores win when it ran.
    var grimace: Grimace { deep?.fgs ?? quick.fgs }
    var painScore: Int { grimace.painScore(offset: painOffset) }
}

struct AppConfig {
    var cloudQuickModel = "gemini-3.5-flash-lite"
    var cloudDeepModel = "gemini-3.8-flash"
    var grimaceThreshold = 4
    /// Points added to Gemini's grimace total before the alert check (interim fix).
    var painOffset = 2
    /// Minimum cosine similarity for a photo match; below it the cat counts as unknown.
    /// Placeholder: calibrate on real photos of look-alike cats.
    var catIdMinSimilarity = 0.6
}
