package dev.twobrain.purrsense.ai

import dev.twobrain.purrsense.data.Cat
import dev.twobrain.purrsense.data.QuickRead

/**
 * Prompts are kept short on purpose: on-device requests are limited to ~4000 tokens in and
 * outputs over 256 tokens are discouraged. Keep in sync with /shared/prompts.md and the iOS app.
 */
object Prompts {

    const val QUICK_READ = """You are a feline behaviour assistant. Look at the cat photo.
Reply with ONLY one JSON object, no markdown:
{"cat":bool,"face_visible":bool,"appearance":"coat colour, pattern, eye colour, marks (max 15 words)",
"mood":"relaxed|alert|playful|sleepy|anxious|fearful|irritated|uncomfortable",
"mood_confidence":0.0-1.0,
"fgs":{"ears":0-2,"eyes":0-2,"muzzle":0-2,"whiskers":0-2,"head":0-2},
"signs":["visible issues e.g. eye discharge, squinting, nasal discharge, unkempt coat"]}
Feline Grimace Scale: 0=absent, 1=moderate, 2=marked.
ears: 0 facing forward, 2 flattened and rotated outwards.
eyes: 0 open, 2 squinted/closed tight.
muzzle: 0 relaxed round, 2 tense elliptical.
whiskers: 0 loose curved, 2 straight and pulled forward.
head: 0 above shoulder line, 2 below shoulders or tilted down.
If no cat face is visible set cat/face_visible false and scores 0. Use [] if no signs."""

    fun identify(observed: String, cats: List<Cat>): String = buildString {
        appendLine("Match the observed cat to one registered cat by appearance.")
        appendLine("Registered cats:")
        cats.forEach { appendLine("- id=${it.id} name=${it.name}: ${it.appearance}") }
        appendLine("Observed: $observed")
        append("""Reply with ONLY JSON: {"cat_id":"<id or unknown>","confidence":0.0-1.0}. """)
        append("Use unknown if no cat clearly matches.")
    }

    fun careNote(name: String, read: QuickRead, threshold: Int): String = buildString {
        appendLine("Write a warm, plain-English note (max 3 sentences) to the owner of $name.")
        appendLine("Mood: ${read.mood} (confidence ${"%.0f".format(read.moodConfidence * 100)}%).")
        val g = read.fgs
        appendLine("Grimace score ${g.total}/10 (ears ${g.ears}, eyes ${g.eyes}, muzzle ${g.muzzle}, whiskers ${g.whiskers}, head ${g.head}).")
        if (read.signs.isNotEmpty()) appendLine("Visible signs: ${read.signs.joinToString()}.")
        appendLine("Explain what the expression suggests and one practical tip.")
        if (g.total >= threshold || read.signs.isNotEmpty()) {
            appendLine("The score is at or above the pain threshold of $threshold: recommend contacting a vet.")
        }
        append("Never give a diagnosis or medication advice.")
    }

    const val DEEP_CHECK = """You are assisting a cat owner (not a vet) with a careful visual wellbeing screen.
Score the Feline Grimace Scale action units (0-2 each: ears, eyes, muzzle, whiskers, head) and look for
visible signs: eye discharge or redness, third eyelid showing, nasal discharge, drooling, unkempt or greasy
coat, hair loss, swelling, weight change, lethargic posture.
Reply with ONLY JSON:
{"urgency":"none|monitor|vet_soon|vet_urgent","fgs":{"ears":0,"eyes":0,"muzzle":0,"whiskers":0,"head":0},
"findings":["short observations"],"summary":"2-3 sentences for the owner, no diagnosis"}
Be conservative: photo-based screening cannot rule anything out."""

    fun trend(name: String, lines: List<String>): String = buildString {
        appendLine("Summarise $name's recent wellbeing for the owner in max 3 sentences.")
        appendLine("Readings (newest first): ")
        lines.forEach { appendLine("- $it") }
        append("Mention any trend in mood or grimace score. Suggest a vet visit only if scores are rising or >= 4. No diagnosis.")
    }
}
