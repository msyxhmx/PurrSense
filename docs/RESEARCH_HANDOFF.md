# Handoff: AI algorithm and model research for PurrSense

_Written 27 Nov 2026. Covers two research rounds done on 26 Nov 2026 on branch `claude/compassionate-keller-slgrrq`._

## What this was

We asked two questions about PurrSense's AI steps: identifying which cat is in the photo, scoring pain on the Feline Grimace Scale (FGS), and reading mood.

1. **Which algorithm does the research support for each step?** Report: `reports/Cat face analysis algorithms.md`
2. **Which open-source models can we actually use, and can we use them commercially?** Report: `reports/Open source models for PurrSense.md`

Both reports cite sources for every claim. The raw research notes are in `research_notes/<report title>/`.

## The short version

- **Stop using Gemini to score or identify; use it only to explain results.** Small vision models trained for cats beat prompting a general model at every step.
- **Pain:** a peer-reviewed study found chatbots score pain too low, and Gemini 1.5 Pro was furthest off at about 2.4 points low on the 0–10 scale. A cat truly at 5/10 would often fall below our ≥4 alert. The ≥4 threshold itself is correct: it matches the published cutoff, which catches 90.7% of cats in pain with 86.6% specificity.
- **Identifying the cat:** matching AI-written text descriptions gets about 54% right; image embeddings get 85–89%. Look-alike cats are where text matching fails.
- **Mood:** there is no scientific support for 8 mood labels. Switch to positive / neutral / negative, and take pain from the FGS score.
- **Licensing is the real blocker, not model quality.** Commercially usable open models exist for the general jobs. Every good cat-face-specific model or dataset is non-commercial or has no licence. That includes CatFLW and the phone-ready landmark model trained on it, PetFace, MegaDescriptor and MiewID.

## Recommended pipeline (all on the phone)

| Step | Recommended model | Licence | Status |
|---|---|---|---|
| Find the face | D-FINE-N/S, fine-tuned on CC BY cat-face datasets | Apache-2.0 | Ready to fine-tune. Converting it to LiteRT / Core ML is unproven. |
| 48 facial landmarks | RTMPose trained on landmark data we own or license | Apache-2.0 (not yet checked against its own LICENSE file) | **Blocked on data** (see decisions) |
| Pain score | Steagall 2023 method: landmarks, then geometric measurements, then an XGBoost classifier per grimace unit, summed to 0–10 | Build in-house | Needs vet-scored photos |
| Which cat | DINOv2-Small + an ArcFace head; match against 3–10 enrolment photos per cat; threshold to reject unregistered cats | Apache-2.0 | Needs consented user photos |
| Mood | Positive / neutral / negative classifier on the same landmarks | Build in-house | Needs labels taken from the situation the cat was in |
| Breed and "is it a cat?" | BioCLIP 2, scaled down into a small model | MIT | Ready |
| Explain results in friendly words | Gemma 4 E2B on LiteRT-LM (2.58 GB, 0.3 s to first word on 2026 flagships) | Apache-2.0 | Ready; runs alongside Firebase AI Logic, not inside it |

## Avoid

- **Ultralytics YOLO:** AGPL-3.0, which also covers models you train yourself. Shipping it in a closed app needs a paid licence.
- **YOLOv9:** GPL-3.0.
- **RF-DETR Atto, Femto and Pico:** a paid licence is needed for commercial self-hosting.
- **MobileCLIP weights:** research use only.
- **MiewID:** no licence at all.
- **DINOv3:** commercial use is fine, but it requires a visible "Built with DINOv3" notice.

## Decisions needed from the team

1. **Approve the on-device pipeline**, with Gemini only explaining results.
2. **Approve replacing the 8 mood labels** with positive / neutral / negative.
3. **Choose how to get landmark data** (this is the critical path): request a commercial licence for CatFLW from the Tech4Animals Lab at the University of Haifa, or annotate our own photos with CatFLW's 48 points. Recommended: decide after the prototype below.
4. **Interim fix while Gemini still scores pain:** add about 2 points to its total before comparing with the ≥4 threshold, to correct for its under-scoring.

## Suggested next steps

1. **Prototype the pipeline**, for internal testing only. Use the non-commercial `hugocornellier/cat-face-landmarks` TFLite model (Hugging Face, CC BY-NC) and the Steagall pain method.
2. **Collect a small validation set:** owner-taken photos scored on the FGS by vets, with each cat in either training or test data, never both. Measure how accurate pain scoring really is on home photos.
3. **Contact both labs:**
   - Steagall's group (FGS developers; they hold 1,188 FGS-scored images, available on request) about a data agreement.
   - The Haifa lab about commercial terms for CatFLW.
4. **Spike the phone conversions:** D-FINE and DINOv2-S to LiteRT and Core ML. Measure speed, including on mid-range phones.
5. **Legal review:**
   - Oxford-IIIT Pet's ShareAlike clause.
   - Licence notices that must ship inside the app.
   - Confirm the licences we haven't checked against the source: RTMPose, YOLOX, Qwen3-VL-2B and the Roboflow cat-face datasets.

## Known gaps and weak evidence

- **Pain accuracy:** the 95.5% headline (Steagall 2023) was tested on only 100 images, and the same cat could appear in training and test. Studies that test on unseen cats get 66–78%.
- **Mood:** the strongest test of general AI models on emotion used dogs. No cat version exists.
- **Phone speed:** no figures were found for D-FINE, RF-DETR, DINOv2 or SigLIP2 on phones, and none for mid-range phones on any model.
- **Lab terms:** we don't know whether the Haifa lab or Steagall's group offer commercial terms.
- **Catellect-VL-2B** (a cat-behaviour model fine-tuned from Qwen3-VL-2B) looks promising, but it was trained and tested on the same six cats and gives no pain score. Treat it as a future direction only.

## Where everything is

| What | Where |
|---|---|
| Report 1: algorithms | `reports/Cat face analysis algorithms.md` |
| Report 2: open-source models | `reports/Open source models for PurrSense.md` |
| Research notes | `research_notes/Cat face analysis algorithms/`, `research_notes/Open source models for PurrSense/` |
| Review page 1 (published) | https://claude.ai/artifact/F3DaLrpvAi3coBbaspws9z, source `docs/research/purrsense-algorithm-review.html` |
| Review page 2 (published) | https://claude.ai/artifact/8DoKci86sGjgeihLMq79AR, source `docs/research/purrsense-open-models.html` |
| One-page summaries (2 slides; download as PDF or PowerPoint) | https://claude.ai/artifact/Ya4RgHUkkCbw3bRiNSbEJr |
| Summary images | `docs/research/PurrSense-research-summary.png`, `docs/research/PurrSense-open-models-summary.png` |

The published links are private to their owner. Share them from each page's Share menu before sending them to anyone.

No app code was changed. This work is research and documentation only.
