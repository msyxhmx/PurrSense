## Inspiration

Cats are masters at hiding pain. By the time most owners notice something is wrong, the problem is often advanced. Vets have a tool for this, the **Feline Grimace Scale**, which reads pain from five facial cues: ears, eyes, muzzle, whiskers and head position. But almost no owner knows it exists.

We wanted every cat owner to have that vet's eye in their pocket, several times a day, without uploading photos of their home every time. That's where hybrid AI clicked for us, and why our team is called **TwoBrain**: an on-device brain for frequent, private check-ins, and a cloud brain for the rare moments that really matter.

## What it does

Snap or pick a photo of your cat and **PurrSense** tells you:

- **Who** it is: it recognises which of your registered cats is in the photo
- **Mood**: relaxed, alert, playful, sleepy, anxious, fearful, irritated or uncomfortable
- **Pain screening**: each Feline Grimace Scale action unit scored 0–2, with the total computed in code
- **Visible health signs**: eye discharge, squinting, nasal discharge, unkempt coat
- **A Deep Check**: if the score reaches the threshold, a stronger cloud model takes a closer look and the app suggests contacting a vet
- **Weekly trend**: a plain-English summary of each cat's recent wellbeing

Every result shows a **TwoBrain trace**: a badge for each step (On-device, Cloud or Rule) with its latency, so you can see exactly where your data went.

## How we built it

**Firebase AI Logic hybrid inference** is the core, on both platforms.

- **Android (Kotlin, Jetpack Compose):** one hybrid model with `OnDeviceConfig(mode = InferenceMode.PREFER_ON_DEVICE)`. Gemini Nano reads the photo (one bitmap + prompt) directly on the phone. On devices without Nano, the same call falls back to Gemini in the cloud. `response.inferenceSource` powers the trace.
- **iOS (SwiftUI):** Apple's on-device Foundation Model is text-only, so we split the work. **Apple Vision** detects and crops the cat on-device (photos without a cat never leave the phone), Gemini reads the face, and identification, care notes and trends run through a `HybridModel(primary: SystemLanguageModel, secondary: Gemini)` session on Apple Intelligence.
- **Escalation is a rule, not a vibe:** the cloud Deep Check runs when $\text{FGS} = \sum_{i=1}^{5} a_i \ge \tau$ (each $a_i \in \{0,1,2\}$) or when visible signs are reported. $\tau$ defaults to 4, the published threshold, and lives in **Remote Config**.
- **App Check** protects AI Logic, **anonymous Authentication** removes sign-up friction, and **Cloud Firestore** stores per-cat readings with offline caching and per-user security rules. **The photo itself is never stored.**
- Built with Claude Code, Android Studio, Xcode and Firebase Agent Skills.

## Challenges we ran into

- **Different on-device capabilities per platform.** Gemini Nano on Android can see images; Apple's model can't (yet). We redesigned the iOS pipeline around Apple Vision plus on-device text steps instead of forcing the same architecture onto both.
- **Small context and output budgets.** On-device requests are capped around 4k tokens with short outputs, so we compressed the Grimace Scale rubric into a compact prompt and a tight JSON schema.
- **Reliability of small models.** On-device output sometimes breaks the JSON format. We added a lenient parser and an automatic cloud retry, which also shows up in the trace.
- **Responsible health claims.** We framed PurrSense as screening, not diagnosis: conservative prompts, the score computed in code, and a clear "contact your vet" path.

## Accomplishments that we're proud of

- One product, two platforms, two very different on-device models, both behind one Firebase SDK
- Daily check-ins on Gemini Nano phones are private, free and work in airplane mode
- Grounded in a validated veterinary pain scale rather than an invented "sick score"
- The TwoBrain trace makes hybrid AI visible and understandable to non-technical users

## What we learned

- Hybrid design is product design: deciding *what* runs locally (frequent, private, cheap) versus in the cloud (rare, high-stakes) shaped the whole UX.
- Small on-device models do well when the task is narrow, structured and well specified.
- [Add personal learnings from the build here.]

## What's next for PurrSense

- Live camera scanning and daily reminders
- Vet-shareable reports of grimace trends
- Embedding-based identification for look-alike cats
- A shelter mode for monitoring many cats at once
- A real-world pilot with a local shelter or vet clinic

---

### Built with (tags)
`firebase` · `firebase-ai-logic` · `hybrid-inference` · `gemini` · `gemini-nano` · `apple-foundation-models` · `apple-intelligence` · `on-device-ai` · `firebase-app-check` · `firebase-authentication` · `cloud-firestore` · `firebase-remote-config` · `kotlin` · `jetpack-compose` · `android` · `swift` · `swiftui` · `ios` · `apple-vision` · `ml-kit` · `claude-code` · `android-studio` · `xcode` · `firebase-agent-skills`
