import Foundation
import FirebaseAuth
import FirebaseFirestore
import FirebaseRemoteConfig

enum RemoteConfigService {
    static func load() async -> AppConfig {
        let rc = RemoteConfig.remoteConfig()
        let settings = RemoteConfigSettings()
        settings.minimumFetchInterval = 600
        rc.configSettings = settings
        rc.setDefaults([
            "cloud_quick_model": "gemini-3.5-flash-lite" as NSString,
            "cloud_deep_model": "gemini-3.8-flash" as NSString,
            "grimace_alert_threshold": 4 as NSNumber, // published FGS analgesia threshold: 4/10
            "gemini_pain_offset": 2 as NSNumber,
            "cat_id_min_similarity": 0.6 as NSNumber, // placeholder: calibrate on look-alike cats      // Gemini under-scores FGS pain by ~2.4/10 (Sci Rep 2025)
        ])
        _ = try? await rc.fetchAndActivate() // offline: defaults / last activated values
        var c = AppConfig()
        if let s = rc.configValue(forKey: "cloud_quick_model").stringValue, !s.isEmpty { c.cloudQuickModel = s }
        if let s = rc.configValue(forKey: "cloud_deep_model").stringValue, !s.isEmpty { c.cloudDeepModel = s }
        let t = rc.configValue(forKey: "grimace_alert_threshold").numberValue.intValue
        if (1...10).contains(t) { c.grimaceThreshold = t }
        let o = rc.configValue(forKey: "gemini_pain_offset").numberValue.intValue
        if (0...5).contains(o) { c.painOffset = o }
        let s = rc.configValue(forKey: "cat_id_min_similarity").numberValue.doubleValue
        if (0...1).contains(s) { c.catIdMinSimilarity = s }
        return c
    }
}

/// Firestore layout (see /firebase/firestore.rules):
///   users/{uid}/cats/{catId}
///   users/{uid}/cats/{catId}/readings/{readingId}   ("unknown" catId for unmatched cats)
/// Photos are never uploaded; only structured results are stored.
final class Repository {
    private let db = Firestore.firestore()

    func ensureSignedIn() async throws -> String {
        if let uid = Auth.auth().currentUser?.uid { return uid }
        return try await Auth.auth().signInAnonymously().user.uid
    }

    private func cats(_ uid: String) -> CollectionReference {
        db.collection("users").document(uid).collection("cats")
    }

    func listCats(uid: String) async throws -> [Cat] {
        try await cats(uid).order(by: "createdAt").getDocuments().documents.map { d in
            Cat(id: d.documentID,
                name: d["name"] as? String ?? "",
                appearance: d["appearance"] as? String ?? "",
                embedding: (d["embedding"] as? [Any] ?? []).compactMap { ($0 as? NSNumber)?.floatValue },
                createdAt: d["createdAt"] as? Double ?? 0)
        }
    }

    /// Writes are not awaited: Firestore updates its local cache immediately and syncs later,
    /// so adding cats and saving readings also work offline.
    func addCat(uid: String, name: String, appearance: String, embedding: [Float]) -> Cat {
        let ref = cats(uid).document()
        let cat = Cat(id: ref.documentID, name: name, appearance: appearance, embedding: embedding)
        // The embedding is a vector of numbers, not the photo: the photo is never uploaded.
        ref.setData(["name": name, "appearance": appearance,
                     "embedding": embedding.map { Double($0) }, "createdAt": cat.createdAt])
        return cat
    }

    func saveReading(uid: String, catId: String, analysis a: Analysis) {
        var data: [String: Any] = [
            "mood": a.quick.mood,
            "moodConfidence": a.quick.moodConfidence,
            "grimaceTotal": a.grimace.total,
            "painScore": a.painScore,
            "grimace": a.grimace.asMap,
            "signs": a.quick.signs,
            "careNote": a.careNote,
            "brains": Dictionary(a.trace.map { ($0.step, $0.brain.rawValue) }, uniquingKeysWith: { $1 }),
            "createdAt": Date().timeIntervalSince1970 * 1000,
        ]
        if let d = a.deep { data["deepUrgency"] = d.urgency; data["deepSummary"] = d.summary }
        cats(uid).document(catId).collection("readings").addDocument(data: data)
    }

    func recentReadings(uid: String, catId: String, limit: Int = 14) async throws -> [Reading] {
        try await cats(uid).document(catId).collection("readings")
            .order(by: "createdAt", descending: true).limit(to: limit)
            .getDocuments().documents.map { d in
                Reading(id: d.documentID, catId: catId,
                        mood: Mood.normalize(d["mood"] as? String ?? ""),
                        moodConfidence: d["moodConfidence"] as? Double ?? 0,
                        grimaceTotal: d["grimaceTotal"] as? Int ?? 0,
                        // Readings saved before the offset existed have no painScore: use the raw total.
                        painScore: d["painScore"] as? Int ?? d["grimaceTotal"] as? Int ?? 0,
                        signs: d["signs"] as? [String] ?? [],
                        careNote: d["careNote"] as? String ?? "",
                        deepUrgency: d["deepUrgency"] as? String,
                        deepSummary: d["deepSummary"] as? String,
                        createdAt: d["createdAt"] as? Double ?? 0)
            }
    }
}
