# PurrSense: Project Proposal (draft v1)

**Team:** TwoBrain
**Hackathon:** #BuildHybridAI (Firebase AI Logic Hybrid Inference)
**Platforms:** Android (Gemini Nano 4) + iOS (Apple Foundation Models). Multi-platform, for Grand Prize consideration.
**One-liner:** Snap your cat, and PurrSense tells you who it is, how they feel, and whether their face shows signs of pain. It runs on-device first and calls the cloud only when it matters.

---

## 1. Problem

Cats are well known for masking pain and illness. Owners often notice only when a problem is advanced, because the early signs are subtle changes in facial expression and grooming. Veterinary researchers built the **Feline Grimace Scale (FGS)** (Evangelista et al., *Scientific Reports*, 2019) for exactly this. It scores five facial action units (ear position, orbital tightening, muzzle tension, whisker position, head position) from 0 to 2. A total of **4 or more out of 10** is the published threshold for reassessing pain. The FGS is used in clinics, but almost no owner has heard of it, let alone applies it at home every day.

**Audience:** cat owners, especially multi-cat households, owners of senior cats or cats with chronic conditions (arthritis, dental disease, kidney disease), cats recovering after surgery, and shelters or fosters monitoring many cats.

## 2. Solution

A mobile app that turns one photo into a structured, explainable wellbeing check:

1. **Recognise:** confirms a cat face is in the photo and identifies *which* registered cat it is.
2. **Mood:** relaxed / alert / playful / sleepy / anxious / fearful / irritated / uncomfortable, with confidence.
3. **Pain screening:** scores the five FGS action units, shows each one visually, and computes the total in code (not by the model).
4. **Visible health signs:** eye discharge, squinting, nasal discharge, unkempt coat and similar.
5. **Escalation:** if the score reaches the threshold or signs appear, a cloud **Deep Check** runs automatically and the app recommends contacting a vet.
6. **History and trends:** every reading is saved per cat, and an on-device weekly summary highlights changes such as "Mochi's grimace score has risen from 1 to 4 over 5 days."

**Responsible framing:** PurrSense is a screening aid. It never diagnoses or suggests medication, and every result carries a clear "see your vet if worried" message.

## 3. Why hybrid is essential (not a fallback)

| | On-device brain | Cloud brain |
|---|---|---|
| **Used for** | Daily quick reads (Android), identification, care notes, weekly trends | Image reading on iOS, Deep Check escalation, any device without a local model |
| **Why** | Check-ins happen several times a day, so they need to be free, instant and private (photos of your home), and they should work offline at the vet, in the garden or at a cattery | Rare, high-stakes cases deserve the strongest model and a higher-resolution image |

- **Escalation is rule-based and explainable:** `FGS total ≥ threshold OR visible signs → Deep Check`. The threshold lives in **Remote Config**, so it can be tuned without a release.
- **Platform-aware design:** Apple's on-device model is text-only today. On iOS, **Apple Vision** checks on-device that the photo contains a cat and crops to it, so non-cat photos are never uploaded and only the cat crop reaches the cloud. The text steps then run on Apple Foundation Models through the hybrid SDK.
- **Transparency:** every result shows a **TwoBrain trace**, a badge for each step (On-device / Cloud / Rule) with its latency. Judges and users can see the hybrid logic working.

## 4. Firebase usage

| Product | Use |
|---|---|
| Firebase AI Logic (hybrid) | Android `OnDeviceConfig(mode = PREFER_ON_DEVICE)` with one image + text; iOS `HybridModel(primary: SystemLanguageModel, secondary: Gemini)` |
| Firebase AI Logic (cloud) | `gemini-3.5-flash-lite` for quick reads and fallback, `gemini-3.8-flash` for the Deep Check |
| App Check | Required by AI Logic (debug provider for the submission) |
| Authentication | Anonymous sign-in, no friction |
| Cloud Firestore | `users/{uid}/cats/{catId}/readings`, offline cache, per-user security rules. Photos are never stored |
| Remote Config | Model names and grimace threshold |

## 5. Architecture

```
 photo ──► [iOS: Apple Vision cat check + crop (on-device)]
        ──► SEE   mood + FGS + appearance   Android: Nano on-device ⇄ cloud | iOS: cloud
        ──► WHO   match appearance ↔ registered cats         hybrid (on-device first)
        ──► CARE  owner-friendly note                         hybrid (on-device first)
        ──► RULE  FGS ≥ threshold or signs?  ──yes──► DEEP CHECK (cloud, larger model)
        ──► SAVE  structured reading to Firestore (no photo)
 history ──► TREND weekly summary                             hybrid (on-device first)
```

## 6. Judging criteria fit

- **Technological implementation:** hybrid inference across *two* platforms with different on-device capabilities, a rule-based escalation tier, robust JSON parsing with cloud retry, a visible trace, and on-device model download management.
- **Design:** a complete flow (onboarding a cat, scanning, results, history and trends) with clear status of on-device availability.
- **Potential impact:** grounded in a validated veterinary pain scale, aimed at a real and common problem (hidden pain in cats), with a clear "when to call the vet" outcome.
- **Quality of idea:** non-obvious use of hybrid AI. Frequent, private, free check-ins on-device; rare, high-stakes cases in the cloud; identity through on-device text matching.

## 7. Plan (under 3 days)

| When | Android | iOS | Shared |
|---|---|---|---|
| **Day 1** | Firebase setup, run on device, fix compile issues, test on-device vs cloud | Generate project, fix compile issues, run | Firebase project, App Check tokens, deploy rules + Remote Config, collect 15–20 test cat photos (calm and in-pain reference images) |
| **Day 2** | Tune prompts on real photos, polish UI | Same | Test identification with 2–3 cats, test offline mode, record screen captures |
| **Day 3 (AM)** | Bug fixes | Bug fixes | Video (≤ 3 min, English), Devpost text, public repo, About-section license check |

## 8. Demo video outline (≤ 3:00)

1. **0:00–0:20 Hook:** "Cats hide pain. Vets use the Feline Grimace Scale to spot it. We put it in your pocket."
2. **0:20–1:10 Android:** add two cats, scan a relaxed cat (all-green **On-device** badges), switch to **airplane mode** and scan again (still works).
3. **1:10–1:50 Escalation:** scan a cat showing a pain grimace. The score crosses 4, the **Deep Check** runs in the cloud, and the app recommends a vet.
4. **1:50–2:30 iOS:** the same flow. Apple Vision runs on-device, the image read goes to the cloud, and identification and care notes run on Apple Intelligence. Then the weekly trend.
5. **2:30–3:00 Architecture slide** (the hybrid table) and the impact close.

## 9. Risks and mitigations

| Risk | Mitigation |
|---|---|
| Experimental SDK APIs change or don't compile | All AI calls are in one file per platform; the alternative iOS syntax is noted |
| No Gemini Nano / Apple Intelligence device for the demo | Cloud fallback works everywhere; borrow or record on one supported device for the on-device segment |
| On-device model returns malformed JSON | Lenient parser, then automatic cloud retry (Android) |
| Inaccurate health readings | Conservative prompts, threshold-based escalation, clear disclaimers, no diagnosis |
| Identification confuses look-alike cats | User-editable appearance signature, plus a confidence ≥ 0.5 rule, otherwise "Unknown cat" |

## 10. Future work
Daily reminders, vet-shareable PDF reports, live camera scanning, multi-photo identification with embeddings, and a shelter mode for many cats.
