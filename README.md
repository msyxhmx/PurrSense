# PurrSense 🐱 — read your cat's face, on-device first

**Team TwoBrain · #BuildHybridAI (Firebase AI Logic Hybrid Inference) · Android + iOS**

Cats are experts at hiding pain. PurrSense lets any owner snap a photo and get, in seconds:

- **Who** it is — recognises which of your registered cats is in the photo, by comparing on-device image embeddings (Apple Vision feature print on iOS, MediaPipe MobileNet-V3 on Android). These are general-purpose models for now; cats registered before this fall back to text descriptions.
- **Mood** — positive, neutral or negative (the research found no scientific basis for finer cat mood labels; pain comes from the grimace score)
- **Pain screening** — a score on the **Feline Grimace Scale** (FGS), a published scale vets use to spot pain in cats: 5 facial "action units" (ears, eyes, muzzle, whiskers, head), each scored 0–2, giving a total out of 10. A score of 4 or more is the published threshold for reassessing pain.
- **Visible health signs** — eye discharge, squinting, nasal discharge, an unkempt coat…
- **A safety margin on the pain score** — Gemini tends to under-score cat pain (about 2.4/10 low in a 2025 study), so the app adds 2 points (Remote Config `gemini_pain_offset`) before the ≥4 check. This is an interim fix; see [`docs/RESEARCH_HANDOFF.md`](docs/RESEARCH_HANDOFF.md).
- **A cloud Deep Check** — runs automatically only when the score crosses the threshold
- **Weekly trend** — a plain-English summary of each cat's history

> PurrSense is a screening aid, not a veterinary diagnosis.

## Why hybrid? (the "TwoBrain" design)

Each step runs where it makes the most sense, and the app shows which brain ran each step (the **TwoBrain trace**).

| Step | Android (Gemini Nano 4) | iOS (Apple Foundation Models) | Why |
|---|---|---|---|
| 0. Cat check + crop | — | **On-device** (Apple Vision) | Photos without a cat never leave the phone; only the cat crop is uploaded |
| 1. See: mood + grimace (image) | **Hybrid**: on-device Nano, falls back to cloud | Cloud (Apple's on-device model is text-only) | Daily check-ins are private, instant, free and work offline on Nano phones |
| 2. Who: identify the cat (image embedding) | **On-device** (MediaPipe) | **On-device** (Apple Vision) | Image embeddings identify cats far better than text descriptions (85–89% vs ~54% in the research); text matching remains only for cats registered earlier |
| 3. Care note (text) | **Hybrid**, on-device first | **Hybrid**, on-device first | Private and free |
| Escalate? | Rule: `FGS + pain offset ≥ threshold or signs` | same | Explainable; the threshold is tuned in Remote Config |
| 4. Deep Check (image) | **Cloud** (larger Gemini) | **Cloud** | Only the rare worrying cases use the stronger, paid model |
| Weekly trend (text) | **Hybrid**, on-device first | **Hybrid**, on-device first | Summarising your own history works offline |

The photo itself is **never stored** in the cloud. Firestore keeps only the structured results.

## Firebase products used

- **Firebase AI Logic** — hybrid inference (`PREFER_ON_DEVICE` on Android, `HybridModel(primary: SystemLanguageModel, secondary: Gemini)` on iOS), plus cloud Gemini models
- **App Check** — required by AI Logic (debug provider for the hackathon)
- **Authentication** — anonymous sign-in, no account friction
- **Cloud Firestore** — cats and readings per user, offline cache, security rules
- **Remote Config** — cloud model names and the grimace alert threshold, changeable without a release

All Firebase AI Logic calls are in one file per platform:
- Android: [`android/app/src/main/java/dev/twobrain/purrsense/ai/CatBrain.kt`](android/app/src/main/java/dev/twobrain/purrsense/ai/CatBrain.kt)
- iOS: [`ios/PurrSense/AI/CatBrain.swift`](ios/PurrSense/AI/CatBrain.swift)

## Repo layout

```
android/            Kotlin + Jetpack Compose app
ios/                SwiftUI app (XcodeGen project.yml)
firebase/           Firestore rules, Remote Config template
firebase.json       Firebase CLI config (deploy rules + remote config)
docs/PROPOSAL.md    Project proposal
docs/DEVPOST_STORY.md  Devpost "About the project" draft
docs/file-map.html  Which files are frontend / backend and what each one does
```

**File map:** [view the page](https://claude.ai/artifact/MYvr2zXZ4uxw92WThxmr71) (source: [`docs/file-map.html`](docs/file-map.html)). There is no backend server: the backend is Firebase, configured by `firebase/` and `firebase.json`. Each app has a client logic layer (`ai/`, `data/`) and a UI layer (`ui/` on Android, `Views/` on iOS).

> **Frontend design is not finalised.** The current screens are a first working version. Aqil and Ilham will do more research on the frontend design before it is settled, so expect `ui/` and `Views/` to change.

## Setup

### 1. Firebase project
1. Create a Firebase project and open **AI Logic** → enable the **Gemini Developer API**.
2. Add an **Android app** (`dev.twobrain.purrsense`) and an **iOS app** (`dev.twobrain.purrsense`).
3. Enable **Authentication → Anonymous**, create a **Firestore** database, and enable **App Check** for both apps.
4. Deploy the rules and the Remote Config template:
   ```bash
   npm i -g firebase-tools && firebase login && firebase use --add
   firebase deploy --only firestore:rules,remoteconfig
   ```

### 2. Android
1. Put `google-services.json` in `android/app/`.
2. Open `android/` in Android Studio and run on a device. For on-device inference, use a Gemini Nano–capable phone (e.g. Pixel 9/10, Galaxy S25+; see the [ML Kit device list](https://developers.google.com/ml-kit/genai#prompt-device)). Any other device uses the cloud automatically.
3. Copy the App Check debug secret from logcat ("Enter this debug secret into the allow list…") into **Firebase console → App Check → Manage debug tokens**.
4. If the home screen says *DOWNLOADABLE*, tap **Download** to fetch Gemini Nano.

### 3. iOS
1. `brew install xcodegen && cd ios && xcodegen` (generates `PurrSense.xcodeproj`).
2. Add `GoogleService-Info.plist` to `ios/PurrSense/`, then re-run `xcodegen`.
3. Run on a device with **Apple Intelligence enabled** for on-device text. Other devices fall back to the cloud.
4. Copy "App Check debug token" from the Xcode console into the Firebase console as above.

## Status / known gaps (hackathon build)
- The hybrid SDKs are **experimental**. The code follows the Firebase docs as of Nov 2026. The iOS docs show two spellings of the hybrid session API; the alternative is noted in `CatBrain.swift`.
- On-device output is capped at ~256 tokens and the input at ~4k, so prompts are compact. If on-device JSON can't be parsed, Android retries once in the cloud.
- First launch needs a connection (anonymous sign-in + Remote Config). After that, Firestore's offline cache lets on-device scans and history work offline.

## License
[Apache 2.0](LICENSE)
