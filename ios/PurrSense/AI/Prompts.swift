import Foundation

/// Keep in sync with /shared/prompts.md and android/.../ai/Prompts.kt.
/// Short on purpose: on-device requests are limited to ~4096 tokens.
enum Prompts {

    static let quickRead = """
    You are a feline behaviour assistant. Look at the cat photo.
    Reply with ONLY one JSON object, no markdown:
    {"cat":bool,"face_visible":bool,"appearance":"coat colour, pattern, eye colour, marks (max 15 words)",
    "mood":"positive|neutral|negative",
    "mood_confidence":0.0-1.0,
    "fgs":{"ears":0-2,"eyes":0-2,"muzzle":0-2,"whiskers":0-2,"head":0-2},
    "signs":["visible issues e.g. eye discharge, squinting, nasal discharge, unkempt coat"]}
    Feline Grimace Scale: 0=absent, 1=moderate, 2=marked.
    ears: 0 facing forward, 2 flattened and rotated outwards.
    eyes: 0 open, 2 squinted/closed tight.
    muzzle: 0 relaxed round, 2 tense elliptical.
    whiskers: 0 loose curved, 2 straight and pulled forward.
    head: 0 above shoulder line, 2 below shoulders or tilted down.
    Mood: positive = relaxed, content or playful; neutral = calm, alert or sleepy, no clear sign;
    negative = fearful, anxious, irritated or uncomfortable.
    If no cat face is visible set cat/face_visible false and scores 0. Use [] if no signs.
    """

    static func identify(observed: String, cats: [Cat]) -> String {
        var s = "Match the observed cat to one registered cat by appearance.\nRegistered cats:\n"
        for c in cats { s += "- id=\(c.id) name=\(c.name): \(c.appearance)\n" }
        s += "Observed: \(observed)\n"
        s += #"Reply with ONLY JSON: {"cat_id":"<id or unknown>","confidence":0.0-1.0}. "#
        s += "Use unknown if no cat clearly matches."
        return s
    }

    static func careNote(name: String, read: QuickRead, threshold: Int, painOffset: Int) -> String {
        let g = read.fgs
        let score = g.painScore(offset: painOffset)
        var s = "Write a warm, plain-English note (max 3 sentences) to the owner of \(name).\n"
        s += "Mood: \(read.mood) (confidence \(Int(read.moodConfidence * 100))%).\n"
        s += "Pain score \(score)/10 (ears \(g.ears), eyes \(g.eyes), muzzle \(g.muzzle), whiskers \(g.whiskers), head \(g.head), +\(painOffset) safety margin).\n"
        if !read.signs.isEmpty { s += "Visible signs: \(read.signs.joined(separator: ", ")).\n" }
        s += "Explain what the expression suggests and one practical tip.\n"
        if score >= threshold || !read.signs.isEmpty {
            s += "The score is at or above the pain threshold of \(threshold): recommend contacting a vet.\n"
        }
        s += "Never give a diagnosis or medication advice."
        return s
    }

    static let deepCheck = """
    You are assisting a cat owner (not a vet) with a careful visual wellbeing screen.
    Score the Feline Grimace Scale action units (0-2 each: ears, eyes, muzzle, whiskers, head) and look for
    visible signs: eye discharge or redness, third eyelid showing, nasal discharge, drooling, unkempt or greasy
    coat, hair loss, swelling, weight change, lethargic posture.
    Reply with ONLY JSON:
    {"urgency":"none|monitor|vet_soon|vet_urgent","fgs":{"ears":0,"eyes":0,"muzzle":0,"whiskers":0,"head":0},
    "findings":["short observations"],"summary":"2-3 sentences for the owner, no diagnosis"}
    Be conservative: photo-based screening cannot rule anything out.
    """

    static func trend(name: String, lines: [String], threshold: Int) -> String {
        var s = "Summarise \(name)'s recent wellbeing for the owner in max 3 sentences.\nReadings (newest first):\n"
        for l in lines { s += "- \(l)\n" }
        s += "Mention any trend in mood or pain score. Suggest a vet visit only if scores are rising or >= \(threshold). No diagnosis."
        return s
    }
}
