# Replace prompting with purpose-built cat vision models

For each of PurrSense's three steps, the best-supported algorithm is a small, purpose-built vision model. Prompting a general LLM is the weakest option on the evidence. Three peer-reviewed studies anchor this conclusion, one per step:

- **Identification:** **PetFace** (ECCV 2024 Oral) shows that a ResNet-50 trained with ArcFace metric learning verifies unseen cats at **98.0% AUC**. Off-the-shelf CLIP reaches 85.1% ([arXiv 2407.13555](https://arxiv.org/html/2407.13555v2)).
- **Pain:** **Steagall et al.** (Scientific Reports 2023) automate the Feline Grimace Scale with a mobile landmark CNN feeding XGBoost, at **95.5% accuracy and AUROC 0.97** for painful vs non-painful ([PMC10703818](https://pmc.ncbi.nlm.nih.gov/articles/PMC10703818/)).
- **Mood:** **Martvel et al.** (Scientific Reports 2025) find that zero-shot vision-language models, **including three Gemini versions, score at or below 0.6 accuracy** on animal emotion labels that come from a known context. A specialised vision transformer reaches 89% on the same data ([PMC12638295](https://pmc.ncbi.nlm.nih.gov/articles/PMC12638295/)).

A fourth Scientific Reports study tests the exact current approach. Gemini 1.5 Pro did worst of four chatbots at FGS scoring: it **underestimated pain by about 2.2–2.5 points on the 0–10 scale, and more so as pain rose** ([PMC12689790](https://pmc.ncbi.nlm.nih.gov/articles/PMC12689790)). For PurrSense, this means a cat truly at 5/10 would often pass under the ≥4 alert.

Confidence is not uniform across the three steps:

- **Identification:** strong evidence. Picking among a household's 2–10 cats is easier than any published benchmark.
- **Pain:** moderate evidence. The headline numbers have not been replicated independently, and none were measured on owner photos.
- **Mood:** weak evidence. No validated multi-class cat mood classifier exists, so the eight-label mood output cannot be backed by science in its current form.

## Three studies carry the evidence, at different strengths

The table summarises the three anchor studies. Of everything in the research notes, these are the most reputable and most directly useful for each app step. Each confidence rating reflects the study design, not the venue.

| | Identification | Pain (FGS) | Mood / emotion |
|---|---|---|---|
| **Study** | Shinoda & Shiohara, "PetFace: A Large-Scale Dataset and Benchmark for Animal Identification" | Steagall, Monteiro, Marangoni, Moussa, Sautié, "Fully automated deep learning models with smartphone applicability for prediction of pain using the Feline Grimace Scale" | Martvel, Zamansky, Shimshoni, Bremhorst, "Investigating the capabilities of large vision language models in dog emotion recognition" |
| **Year / venue** | 2024, ECCV (Oral) | 2023, *Scientific Reports* 13:21584 | 2025, *Scientific Reports* 15:41250 |
| **DOI / ID** | arXiv 2407.13555 (ECCV proceedings) | 10.1038/s41598-023-49031-2 | 10.1038/s41598-025-25199-7 |
| **Method** | ResNet-50 embeddings trained with ArcFace (also softmax, triplet, center loss), compared with CLIP, ImageNet and MegaDescriptor features | 37 FGS-mapped landmarks predicted by a mobile CNN (best: ShuffleNetV2_0.75, 6.17M params, 0.046 s/image), 35 geometric descriptors, XGBoost per action unit and for the total score | 11 zero-shot LVLMs (GPT-4o/4.1 family, o4-mini, Gemini 1.5 Pro, 1.5 Flash, 2.0 Flash, LLaVA-NeXT) compared with a DINO-ViT classifier |
| **Dataset** | About 1M images of about 257k individuals across 13 families. Cats tested closed-set (113,592 test classes) and open-set | 3,447 images for landmarks; 1,188 FGS-scored frames from 6 clinical trials of hospitalised cats | Cropped Labrador faces with emotion labels induced by context (anticipation vs frustration), plus a web image set with subjective labels |
| **Key metrics** | Cat verification AUC **98.04** (ArcFace, joint training) vs CLIP 85.10, MegaDescriptor 87.30. Cat closed-set top-1: 70.3% over more than 100k identities | Binary pain **95.51% accuracy, AUROC 0.97**. Total-score MSE about 0.01 on the 0–1 scale. Per-AU MSE: ears 0.08, eyes 0.11, head 0.17, whiskers 0.23, muzzle 0.31 | **No LVLM above 0.6 accuracy**; most predicted a single class. DINO-ViT reached 89%. Prompt engineering did not help significantly |
| **Confidence** | **HIGH** | **MODERATE-LOW** (headline figure); HIGH (relevance of the architecture) | **HIGH for dogs**, MODERATE as a guide to cats |

Sources for the table: [PetFace](https://arxiv.org/html/2407.13555v2), [Steagall 2023](https://pmc.ncbi.nlm.nih.gov/articles/PMC10703818/), [Martvel 2025](https://pmc.ncbi.nlm.nih.gov/articles/PMC12638295/).

**Why PetFace earns high confidence.** It is a peer-reviewed oral paper at a top computer-vision venue. Its scale is unmatched, and it tests the open-set case PurrSense needs ("is this one of my cats, or a stranger?"). A separate CVPR 2026 workshop study reaches the same verdict on a 518-cat set, where DINOv2-Small scores 85.5% top-1 and SigLIP2-Giant 89.0% ([arXiv 2603.02270](https://arxiv.org/html/2603.02270)). Two caveats apply. PetFace's weights and data are licensed for research only, and no paper tests the 2–10 cat household case directly.

**Why the Steagall result earns only moderate-low confidence.** It is the only automated system that outputs the actual five FGS action units on mobile-class networks, and the group behind it created the FGS. But the 95.5% figure comes from **just 100 randomly chosen test images, with no split by cat**, so the same animal could appear in both training and test sets. A single annotator placed the landmarks, the model assumes a frontal face, and no one outside the group has replicated it ([PMC10703818](https://pmc.ncbi.nlm.nih.gov/articles/PMC10703818/)). The Haifa group validates by subject, holding out whole cats. Its cat pain classifiers reach a more sober **66–78%** ([PMC10238514](https://pmc.ncbi.nlm.nih.gov/articles/PMC10238514/); [Frontiers 2024](https://frontiersin.org/journals/veterinary-science/articles/10.3389/fvets.2024.1442634/full)). Its fully automated video pipeline, filmed partly on a phone, scores **above 70% and 66%** on two datasets ([Sci Rep 2024](https://d-nb.info/1355225132/34)). The true accuracy on owner photos probably sits between these numbers. That is an inference: the two approaches have never been compared on a common dataset.

**Why the mood study is about dogs.** No cat-specific benchmark of vision-language models exists. The Martvel 2025 study is the strongest test of whether general VLMs read animal affect from faces, and it covers the exact model family PurrSense uses. Its labels come from context, not from a human judging the face. On a web dataset with subjective labels, the same models did moderately well, but they relied on background: changing the background shifted their answers toward the emotion it implied ([PMC12638295](https://pmc.ncbi.nlm.nih.gov/articles/PMC12638295/)). Cats are arguably harder. Humans average only **59% on binary cat valence** (positive vs negative), and only 13% of 6,329 participants scored significantly above chance ([Dawson et al. 2019](https://cambridge.org/core/services/aop-cambridge-core/content/view/AC3942FBE101A240694349890B12553F/S0962728600011295a.pdf/humans-can-identify-cats-affective-states-from-subtle-facial-expressions.pdf)).

## The FGS is solid ground, but Gemini misreads it toward danger

The pain step stands on the firmest scientific base of the three, because the scale itself is well validated. In its development study, trained raters reached **inter-rater ICC 0.89**. A total score above 0.39/1 flagged cats needing analgesia with **90.7% sensitivity, 86.6% specificity and AUC 0.94** ([Evangelista et al. 2019](https://pmc.ncbi.nlm.nih.gov/articles/PMC6911058)). PurrSense's ≥4/10 alert is effectively the published cut-off, so the threshold needs no change.

The scale has two documented soft spots:

- **Muzzle and whiskers are its weakest action units**, even for experts, with ICC 0.63 and 0.55.
- **Owners are far less reliable on single action units:** in a survey of about 1,500 caregivers, single-rater ICC was 0.37 for whiskers and 0.38 for head position ([PMC10812049](https://pmc.ncbi.nlm.nih.gov/articles/PMC10812049)).

Any model trained on these labels inherits that noise. The app should therefore lean on the total score and on the ears, eyes and head action units. The FGS was also validated on frames of undisturbed, caged hospital cats filmed at eye level. It covers acute pain only, and no study has validated it, manually or automatically, on photos owners take at home.

Against this baseline, the current LLM approach fails in the dangerous direction. Ngai, Bukhari, Alonso Sousa & Steagall (2025, *Scientific Reports* 15:43461, DOI 10.1038/s41598-025-27404-z) gave 50 clinical cat images to four chatbots under a standardised prompt. Each image was scored repeatedly and compared with an FGS co-developer. All four chatbots **underestimated pain**:

- **Gemini 1.5 Pro had the widest limits of agreement** (−0.29 to 0.79 on the 0–1 scale). Its proportional bias was significant (p<0.001), so it erred more at higher pain.
- **Gemini's agreement on individual action units was close to none:** weighted kappa was 0.02–0.06 for whiskers and 0.06–0.14 for muzzle.
- **Only Claude's second trial met the authors' bias bar of <0.1**, and even its limits of agreement crossed the treatment threshold.

Source: [PMC12689790](https://pmc.ncbi.nlm.nih.gov/articles/PMC12689790).

The study is small, used a single expert rater, and tested April 2024 model versions, so its confidence is moderate-low. But every model erred in the same direction. Gemini Nano is far smaller than Gemini 1.5 Pro, and nothing suggests it would do better. That last point is an inference, because Nano has never been tested.

## Mood labels outrun what any method can see

No study validates an automated classifier for cat "moods" such as PurrSense's eight labels. The credible automated cat-affect work all comes from one lab (Haifa) and is entirely binary:

- **Pain vs no pain:** about 66–73% accuracy.
- **Affiliative vs non-affiliative social signalling:** above 77% from manually coded CatFACS units and **about 68% from automatically detected landmarks** (Martvel et al. 2024, *Scientific Reports* 14:27533, DOI 10.1038/s41598-024-79216-2; [PMC11554805](https://pmc.ncbi.nlm.nih.gov/articles/PMC11554805)).

Ethology studies that coded cat faces with CatFACS, the cat facial action coding system, find only three emotion clusters. These are **relaxed engagement, fear and frustration**, and even they account for just half the data ([Bennett, Gourkow & Mills 2017](https://pubmed.ncbi.nlm.nih.gov/28341145/)).

Some multi-class "cat emotion" classifiers report 90%+ accuracy. They train on web images labelled by someone deciding a face "looks angry", which makes the labels circular. One 2026 *Multimedia Tools and Applications* paper reports 95.7% on CatFLW images with added emotion labels, but its label set and validation split could not be verified ([Springer](https://link.springer.com/article/10.1007/s11042-026-21301-x)). These figures should not be trusted until someone shows the splits were made by cat and the labels came from context.

Mapped against the literature, the eight labels fall into three groups:

- **Partly defensible:** "relaxed" (relaxed engagement), "irritated" (the scientific construct is frustration) and "fearful/anxious" (the literature cannot tell these apart from faces, so merge them).
- **Not an emotion:** "uncomfortable" is really the pain construct and belongs to the FGS step.
- **No facial evidence:** "alert", "sleepy" and "playful" describe arousal or behaviour, and no study validates them as facially distinguishable.

The defensible redesign is a **valence readout (positive / neutral / negative) plus an arousal cue, with a pain flag driven by the FGS**. Specific labels should appear only when cues are strong, and should be framed as suggestions.

## A recommended on-device pipeline for PurrSense

The evidence points to a pipeline that shares one face front-end and then branches into three light heads. All of it can run on the phone:

1. **Detect and quality-gate.** Find the cat face with a YOLO-nano-class detector (Apple's Vision animal detector is an iOS option, though unverified). Reject or ask for a retake when face confidence is low, the pose is not frontal, or landmark quality is poor. The Haifa video pipeline uses exactly this gating ([Sci Rep 2024](https://d-nb.info/1355225132/34)). It matters more for owner snapshots than for clinical footage.
2. **Find landmarks.** Train the landmark detector on **CatFLW** (2,091 images, 48 anatomically grounded landmarks). Its Ensemble Landmark Detector reaches NME 2.83, and a **MobileNetV2 variant 3.09** ([IJCV 2024](https://link.springer.com/article/10.1007/s11263-024-02006-w); [PwC](https://astro.paperswithcode.com/paper/automated-detection-of-cat-facial-landmarks)). NME is the mean landmark error relative to the distance between the eyes; lower is better. Steagall's 37-point mobile models (4–10M parameters, about 0.05 s per image) are the FGS-specific alternative ([PMC10703818](https://pmc.ncbi.nlm.nih.gov/articles/PMC10703818/)). This stage is the bottleneck: moving from manual to automatic landmarks costs about **7 points of pain accuracy** ([Frontiers 2024](https://frontiersin.org/journals/veterinary-science/articles/10.3389/fvets.2024.1442634/full)).
3. **Score pain.** Compute Steagall-style geometric descriptors for ears, eyes, muzzle, whiskers and head. Feed them to small XGBoost/MLP classifiers, one per action unit, and sum the results to the 0–10 total with the ≥4 alert. Landmark models beat raw-image CNNs on small, varied datasets (77.5% vs 65.1%) and are more explainable ([PMC10238514](https://pmc.ncbi.nlm.nih.gov/articles/PMC10238514/)). The app can say "ears rotated, eyes narrowed" rather than just a number.
4. **Identify the cat.** Embed the aligned crop with a compact backbone fine-tuned with ArcFace or triplet loss (ResNet-50, MobileNet or DINOv2-Small class). Match by cosine similarity against 3–10 enrolment photos per cat, with a threshold that rejects unknown cats. VLM-generated text descriptions identify cats at only **about 53–55% top-1 versus 85–89%** for image embeddings ([arXiv 2603.02270](https://arxiv.org/html/2603.02270)). On hard look-alike pairs, 17 of 18 open VLMs fell below chance at "same animal?" judgements ([Bio-VQA, arXiv 2611.05559](https://arxiv.org/abs/2611.05559)). Two black littermates are exactly where LLM matching breaks.
5. **Read mood.** Reuse the same landmarks for a binary or three-way valence classifier along the lines of the Haifa social-valence work. Treat its output as low-confidence.

The pipeline has three practical constraints. **Licensing:** PetFace data and weights are research-only, and MegaDescriptor-L is CC BY-NC with 229M parameters ([HF](https://huggingface.co/BVRA/MegaDescriptor-L-384)), so a commercial identity model needs data that is safe to use commercially or a licence. **No public FGS weights exist,** so partnering with the Steagall group (owners of 1,188 FGS-scored images) or the Haifa group (owners of CatFLW and the pain pipelines) is the fastest route to training data. **Validation:** PurrSense must run its own study on owner-taken photos, with veterinary FGS scores as ground truth and a train/test split by cat, before it claims any diagnostic accuracy.

## Conclusion

The current LLM layer should move from scorer to narrator. Published evidence supports three uses for it: phrasing the specialised models' outputs in friendly language, checking photo quality, and breaking ties when two registered cats score nearly the same. It should never decide the pain score or the identity. If Gemini must stay as the pain scorer during the transition, the evidence calls for adding about 0.2 to its total (on the 0–1 scale) and lowering the effective alert threshold. A bias that grows with pain means false reassurance is exactly the failure a triage app cannot afford.

The findings rank the work by evidence rather than by product appeal. Identification is close to a solved problem that only needs engineering and data licensing. Pain is scientifically grounded but needs PurrSense's own validation on home photos. Mood, the most marketable feature, has the thinnest science. Shrinking it to valence plus a pain flag is the honest choice, and it also protects users from being misled by confident labels.
