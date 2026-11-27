# Automated pain detection in cats from facial images (automating the Feline Grimace Scale)

Research notes, compiled 2026-11-26. Two research groups lead this field: (1) the Steagall / Monteiro group (Université de Montréal and City University of Hong Kong), who created the FGS and built FGS-specific automation; (2) the Zamansky / Feighelstein / Martvel group (University of Haifa Tech4Animals, with Finka, Luna, Mills and Volk), who built 48-landmark (CatFACS-based) binary pain classifiers. Neither group's models or datasets are publicly downloadable for pain, except the CatFLW landmark dataset.

## Q1. Which peer-reviewed papers automate FGS or cat pain detection from images, and how strong are they?

### Takeaway
The strongest FGS-specific result is Steagall et al. 2023 (Sci Rep): automatic 37-landmark detection plus XGBoost on geometric descriptors, with 95.5% accuracy and AUROC 0.97 for painful vs non-painful. But it was tested on only 100 random images, with no subject-level split reported and no independent replication. The Haifa group's results are more modest (66-78% accuracy) but come from stricter subject-level validation, including a fully automated pipeline. No study yet automates FGS from owner-taken photos and validates it prospectively.

### Cited Findings

**A. Steagall, Monteiro, Marangoni, Moussa, Sautié (2023). "Fully automated deep learning models with smartphone applicability for prediction of pain using the Feline Grimace Scale." Scientific Reports 13:21584. DOI 10.1038/s41598-023-49031-2 (PMID 38062194)**
- Phase I (landmarks): 3,447 cat face images, each annotated with 37 landmarks mapped to the 5 FGS action units. Sources: research studies (n=1,655), images submitted through the FGS smartphone app (n=1,092), and a Kaggle cat dataset (n=700). The number of individual cats and the breed counts are not reported. — [PMC10703818](https://pmc.ncbi.nlm.nih.gov/articles/PMC10703818/)
- Landmark architectures: 11 models compared, all pretrained mobile-class CNNs (NASNetMobile, EfficientNetB0, MobileNetV2/V3, ShuffleNetV2). The best aligned model, ShuffleNetV2_0.75 with a Laplacian filter, reached NRMSE 16.76% at 0.046 s/image with 6.17M parameters. MobileNetV3Large reached NRMSE 18.16% with 4.32M parameters. — [PMC10703818](https://pmc.ncbi.nlm.nih.gov/articles/PMC10703818/)
- Phase II (scoring): 1,188 images from 6 clinical trials of naturally occurring pain. Images were frames from video of hospitalised, caged cats, scored by veterinary raters with the FGS. Pain was defined as FGS ≥0.4/1, though the paper also states 0.39 elsewhere. XGBoost ran on 35 geometric descriptors (ear 10, orbital 5, muzzle 8, whiskers 5, head 7). — [PMC10703818](https://pmc.ncbi.nlm.nih.gov/articles/PMC10703818/)
- Binary pain classification: accuracy 95.51%, AUROC 0.97 (AND aggregation, all 35 descriptors). OR aggregation gave 92.86%, AUROC 0.95. Without the whiskers and head descriptors, accuracy was 91.0-94.1%. Total-score regression MSE was about 0.0096-0.0104 on the 0-1 scale. Per-AU ordinal MSE (mode aggregation): ears 0.08, orbital 0.11, muzzle 0.31, whiskers 0.23, head 0.17. Muzzle and whiskers were the hardest. — [PMC10703818](https://pmc.ncbi.nlm.nih.gov/articles/PMC10703818/)
- Validation weaknesses: tested on 120 random images (landmarks) and 100 random images (FGS), with 5-fold CV for hyperparameter tuning. A subject-independent split is not described, so the same cat may appear in train and test. A single annotator labelled the landmarks, and that annotator's reliability is "unpublished". The models assume frontal faces and need 10 pre-annotated landmarks for alignment. There is no cat-face detector. Data is available on request only. No app was built or tested in the paper. — [PMC10703818](https://pmc.ncbi.nlm.nih.gov/articles/PMC10703818/)
- **Confidence: MODERATE-LOW for the headline 95.5%.** The image set is large and the ground truth is the FGS itself, the target scale for PurrSense. But the test set is small (100 images), leakage at the cat level is possible, there is no external test set and no independent replication. This is still the most directly relevant architecture for PurrSense, because it outputs per-AU FGS scores and runs on mobile CNNs.

**B. Feighelstein, Shimshoni, Finka, Luna, Mills, Zamansky (2022). "Automated recognition of pain in cats." Scientific Reports 12:9575. DOI 10.1038/s41598-022-13348-1**
- Data: 26 of 29 young female domestic shorthair cats, filmed around ovariohysterectomy (originally the Finka et al. dataset). 464 balanced images (232 pain, 232 no pain). Ground truth was based on time point, not on scores: "pain" was 1 h post-surgery, "no pain" was pre-surgery or after rescue analgesia. — [PMC9187730](https://pmc.ncbi.nlm.nih.gov/articles/PMC9187730)
- Methods: (i) 48 manually annotated CatFACS-inspired landmarks fed to an MLP (96 inputs, hidden layers 100-100-500), with face alignment by the eye centres; (ii) ResNet50 (ImageNet-pretrained) with a custom head. Validation was leave-one-subject-out (19 train / 6 val / 1 test cats). — [PMC9187730](https://pmc.ncbi.nlm.nih.gov/articles/PMC9187730)
- Results: best DL accuracy 0.736 (precision 0.82, recall 0.70); best landmark accuracy 0.724 (aligned and augmented). Both approaches reached about 72-74%. — [PMC9187730](https://pmc.ncbi.nlm.nih.gov/articles/PMC9187730)
- **Confidence: MODERATE for the validity of the method, LOW for generalisability.** The leave-one-subject-out validation is rigorous. But the population is very homogeneous (one breed, one sex, young), the sample is small, and the landmarks were annotated manually.

**C. Feighelstein, Henze, Meller, Shimshoni, Hermoni, Berko, Twele, Schütter, Dorn, Kästner, Finka, Luna, Mills, Volk, Zamansky (2023). "Explainable automated pain recognition in cats." Scientific Reports 13:8973. DOI 10.1038/s41598-023-35846-6**
- Data: 84 client-owned cats of mixed breed and sex, with varied conditions (TiHo Hannover). One image per cat, 42 pain and 42 no pain. Ground truth: Glasgow CMPS-Feline ≥5 plus a clinical reason for pain = pain; <4 with no reason = no pain; scores of exactly 4 were excluded. — [PMC10238514](https://pmc.ncbi.nlm.nih.gov/articles/PMC10238514/)
- Methods: 48 manual landmarks fed to MLP / Random Forest, compared with ResNet50. Validation was 10-fold CV with no subject overlap. — [PMC10238514](https://pmc.ncbi.nlm.nih.gov/articles/PMC10238514/)
- Results: best landmark model was Random Forest, with accuracy 0.775, precision 0.76 and recall 0.835. Best ResNet50 accuracy was 0.651. The deep learning approach degrades on small, heterogeneous data. — [PMC10238514](https://pmc.ncbi.nlm.nih.gov/articles/PMC10238514/)
- Explainability (occlusion, RF feature importance, GradCAM): the mouth/muzzle region was most important and the ears least. Ears alone gave RF accuracy of 0.41. GradCAM heat was highest on the mouth (3.97) and eyes (3.48), lowest on the ears (1.00). The authors note this may reflect breed variation in ear shape. — [PMC10238514](https://pmc.ncbi.nlm.nih.gov/articles/PMC10238514/)
- **Confidence: MODERATE-LOW.** The population is realistic, but the sample is tiny (84 images, about 8 per test fold) and the ground truth is CMPS-F, not FGS.

**D. Martvel, Lazebnik, Feighelstein, Henze, Meller, ... Zamansky (2024). "Automated video-based pain recognition in cats using facial landmarks." Scientific Reports. DOI 10.1038/s41598-024-78406-2**
- Fully automated pipeline: a YOLOv8 cat-face detector (frames dropped if face confidence <25%), then the 48-landmark ensemble detector, then a small fully connected network that rates landmark quality, then a fully connected autoencoder (48-36-18) over a temporal window, then XGBoost, with aggregation of the frame predictions across the video (frame probabilities below 0.65 are filtered out). Sampling rates tested: 6, 3 and 1.5 FPS; 6 FPS was best. — [Sci Rep PDF (DNB copy)](https://d-nb.info/1355225132/34)
- Datasets: Finka et al. (27 female DSH cats, 54 videos of about 3 min each, leave-one-animal-out validation) and TiHo (72 videos of about 30 s each, 36 pain / 36 no pain, labelled by CMPS-feline, recorded on a **mobile phone** about 10 cm from the cat, k-fold validation). — [Sci Rep PDF (DNB copy)](https://d-nb.info/1355225132/34)
- Results: >70% accuracy on Finka and >66% on TiHo, beating the single-frame automated landmark approaches. The authors conclude that dynamics matter. — [Sci Rep PDF (DNB copy)](https://d-nb.info/1355225132/34)
- **Confidence: MODERATE.** This is the only fully end-to-end pipeline with subject-level validation, and the TiHo videos were filmed on a phone. Accuracy is modest and the samples are small.

**E. Martvel, Lazebnik, Feighelstein, Meller, Shimshoni, Finka, Luna, Mills, Volk, Zamansky (2024). "Automated landmark-based cat facial analysis and its applications." Frontiers in Veterinary Science 11:1442634. DOI 10.3389/fvets.2024.1442634**
- Landmark detector: a CNN with a "magnifying ensemble" that localises the face, then 5 regions of interest, then 48 landmarks. It was trained on CatFLW (2,091 images, 48 landmarks each). — [Frontiers](https://frontiersin.org/journals/veterinary-science/articles/10.3389/fvets.2024.1442634/full)
- Pain (Finka data, 464 images, leave-one-subject-out): manual landmarks gave 73% accuracy (F1 0.76); **automated landmarks dropped to 66% (F1 0.67)**. Landmark-detection error therefore costs about 7 points of accuracy. Cephalic type from automated landmarks reached 79% (the abstract says 75%, an internal inconsistency). — [Frontiers](https://frontiersin.org/journals/veterinary-science/articles/10.3389/fvets.2024.1442634/full)
- The paper does not evaluate mobile deployment. — [Frontiers](https://frontiersin.org/journals/veterinary-science/articles/10.3389/fvets.2024.1442634/full)

**F. Landmark resources (Martvel et al.)**
- CatFLW: "Cat Facial Landmarks in the Wild", 2,016 images (later 2,091) with 48 landmarks each, tied to CatFACS (arXiv 2305.04232 preprint; later expanded). — [arXiv](https://arxiv.org/pdf/2305.04232)
- Ensemble landmark detector: NME 2.91 on CatFLW, as cited in the 2024 Sci Rep video paper (Martvel et al., International Journal of Computer Vision, 2024). — [Sci Rep PDF (DNB copy)](https://d-nb.info/1355225132/34)

### Inferences
- Rough ranking for PurrSense: (1) the Steagall 2023 FGS pipeline (landmarks → geometric descriptors → XGBoost per AU) is the only one that outputs the exact 5-AU, 0-2 FGS structure; (2) the Haifa 48-landmark pipeline is the most rigorously validated but outputs only a binary pain label against CMPS/time-point ground truth.
- The gap between 95% (Steagall) and 66-78% (Haifa) probably reflects differences in validation (random image split vs subject split) and ground truth (FGS-scored frames vs CMPS/time point), more than real superiority. This is inference; there has been no head-to-head comparison on a common dataset.
- Across studies, landmark/geometry methods match or beat end-to-end CNNs when data is small (<1,500 images), and they are more explainable.

### Gaps
- No independent external replication of any of these models; all results come from the developing groups.
- No study since 2023 using a vision transformer or a fine-tuned VLM for FGS was found (searches covered Nature, PMC and general web; arXiv was not exhaustively searched).
- The 2026 paper "Challenges using the Feline Grimace Scale in brachycephalic cats with ocular pain" (likely JFMS) and the 2025 paper "Understanding the FGS: dimensional structure, importance of each action unit..." (PMID 40998152) are listed on felinegrimacescale.com but were not read. — [FGS publications list](https://www.felinegrimacescale.com/publications)

## Q2. How reliable is the FGS itself, and is it validated for owners and smartphone photos?

### Takeaway
The FGS has strong psychometrics with trained raters (ICC 0.89; sensitivity 90.7% and specificity 86.6% at >0.39/1; AUC 0.94). Muzzle and whiskers are its weakest action units. Cat owners score total FGS with good-to-moderate reliability, but owners' reliability on single action units (whiskers and head) is poor. The FGS is validated on still images taken from video of caged hospital cats, not on owner smartphone photos taken at home.

### Cited Findings
- Evangelista, Watanabe, Leung, Monteiro, O'Toole, Pang, Steagall (2019). "Facial expressions of pain in cats: the development and validation of a Feline Grimace Scale." Scientific Reports 9:19128. DOI 10.1038/s41598-019-55693-8. The validation set was 110 images from 55 cats (35 client-owned, 20 controls), scored by 4 blinded observers twice, 30 days apart. Images were frames from 6-minute videos of undisturbed caged cats, filmed at eye level and cropped to the face. — [PMC6911058](https://pmc.ncbi.nlm.nih.gov/articles/PMC6911058)
- Reliability in that study: inter-rater ICC (single) 0.89 (95% CI 0.85-0.92); intra-rater ICC 0.91-0.95; Cronbach's α 0.89. Per-AU inter-rater ICC: ears 0.87, eyes 0.86, **muzzle 0.63, whiskers 0.55**, head 0.90. — [PMC6911058](https://pmc.ncbi.nlm.nih.gov/articles/PMC6911058)
- Validity in that study: Spearman ρ = 0.86 with the rCMPS-F. Cut-off for analgesia was total >0.39/1.0, with **sensitivity 90.7%, specificity 86.6%, ROC AUC 0.94 (0.89-0.98)**. Note that FGS is formally a ratio (sum ÷ max possible), so PurrSense's ≥4/10 equals ≥0.4, essentially the published cut-off. — [PMC6911058](https://pmc.ncbi.nlm.nih.gov/articles/PMC6911058)
- Evangelista & Steagall (2021). "Agreement and reliability of the FGS among cat owners, veterinarians, veterinary students and nurses." Scientific Reports, DOI 10.1038/s41598-021-84696-7. All groups showed very good agreement with veterinarians (bias <0.1, narrow limits of agreement); muzzle and whiskers had lower reliability. — [PMC7933168](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC7933168/); [search summary](https://link.springer.com/10.1038/s41598-021-84696-7)
- Monteiro, Lee, Steagall (2022/2023). "Can cat caregivers reliably assess acute pain in cats using the FGS? A large bilingual global survey." Journal of Feline Medicine and Surgery. About 1,500 caregivers from 66 countries scored 10 images. Single-rater ICC: ears 0.65, eyes 0.69, muzzle 0.58, **whiskers 0.37, head 0.38**, total 0.65. — [PMC10812049](https://pmc.ncbi.nlm.nih.gov/articles/PMC10812049); [CityU record](https://scholars.cityu.edu.hk/en/publications/can-cat-caregivers-reliably-assess-acute-pain-in-cats-using-the-f/)
- Training effect (2024, JFMS, PMID 39466280): for 7 veterinarians compared with an expert, total-score ICC rose from 0.75 to 0.80 after training. — [PubMed 39466280](https://pubmed.ncbi.nlm.nih.gov/39466280/)
- Clinical applicability study (Watanabe et al. 2020): real-time FGS scores and image-based FGS scores did not differ significantly, which supports image-based scoring (as summarised in the 2024 Sci Rep video paper). — [Sci Rep PDF (DNB copy)](https://d-nb.info/1355225132/34)
- Scope: the FGS is for acute pain only, not chronic pain such as osteoarthritis. — [search summary of Monteiro et al.](https://catvets.com/publication/can-cat-caregivers-reliably-assess-acute-pain-in-cats-using-the-feline-grimace-scale-a-large-bilingual-global-survey/)
- The official FGS smartphone app exists and collected images: 1,092 app-submitted images were used to train landmark detection in Steagall 2023. No published accuracy validation of that app was found. — [PMC10703818](https://pmc.ncbi.nlm.nih.gov/articles/PMC10703818/)

### Inferences
- Even expert humans only reach ICC about 0.89 on total score and 0.55-0.63 on muzzle and whiskers. Any automated system's per-AU accuracy on whiskers and muzzle will be capped by this label noise. PurrSense should weight the total score, and the ears/eyes/head AUs, more than individual muzzle and whisker scores.
- Owner-taken home photos differ from the validation conditions (undisturbed caged cats, filmed at eye level, frontal). Handling, alertness, sleep, sedation and brachycephalic breeds can all shift scores. The 2026 brachycephalic paper title suggests known problems there.

### Gaps
- No peer-reviewed validation of the FGS (manual or automated) on owner-captured smartphone photos taken at home was found.
- Sensitivity and specificity of each AU individually were not found in a verified source.

## Q3. Have general LLMs/VLMs (Gemini, GPT-4V, etc.) been evaluated for FGS or animal pain scoring?

### Takeaway
Yes, in one peer-reviewed study. Ngai, Bukhari, Alonso Sousa, Steagall (2025, Sci Rep) found that ChatGPT-4o, Gemini 1.5 Pro, Claude 3.5 Sonnet and Perplexity all systematically **underestimated** FGS scores compared with the FGS co-developer. **Gemini performed worst** (bias about 0.22-0.25 on the 0-1 scale, widest limits of agreement, per-AU kappa 0.02-0.38). This directly challenges PurrSense's current Gemini-prompting approach.

### Cited Findings
- Ngai ST, Bukhari SSUH, Alonso Sousa S, Steagall PV (2025). "Agreement of Feline Grimace Scale scores between chatbots and an expert rater." Scientific Reports 15:43461. DOI 10.1038/s41598-025-27404-z. Design: 50 facial images of DSH/DLH cats at varied pain levels from Montreal clinical trials. Each image was uploaded in a fresh session with a standardised prompt asking for 5 AU scores plus the total ratio, scored 3 times at least 24 h apart; two trials 2 months apart; models as of April 2024. The gold standard was a board-certified anaesthesiologist and FGS co-developer. — [PMC12689790](https://pmc.ncbi.nlm.nih.gov/articles/PMC12689790)
- Total FGS, Bland-Altman bias (expert minus chatbot; positive = chatbot underestimates), with limits of agreement:
  - ChatGPT: 0.22 / 0.21 (LoA about -0.14 to 0.59)
  - Claude: 0.11 / 0.05 (LoA -0.39 to 0.49)
  - **Gemini: 0.22 / 0.25 (LoA -0.29 to 0.79), with significant proportional bias (p<0.001): it underestimates more as pain rises**
  - Perplexity: 0.21 / 0.18

  The acceptable bias was <0.1; only Claude's trial 2 met it, and its LoA still spanned the 0.39 treatment threshold. — [PMC12689790](https://pmc.ncbi.nlm.nih.gov/articles/PMC12689790)
- Per-AU weighted kappa (trial 1 / trial 2):
  - Gemini: ears 0.16/0.25, orbital 0.22/0.38, muzzle 0.14/0.06, whiskers 0.06/0.02, head 0.17/0.23
  - ChatGPT: orbital 0.65/0.70, but muzzle about 0.10 and whiskers 0.04-0.11
  - Claude: 0.28-0.49 across AUs

  — [PMC12689790](https://pmc.ncbi.nlm.nih.gov/articles/PMC12689790)
- Authors' conclusion: most chatbots showed poor agreement and could lead to under- or overtreatment; an accurate automated deep learning model is still needed. Limitations: 1 expert, 50 images, unstandardised image quality, a snapshot of fast-changing models. — [PMC12689790](https://pmc.ncbi.nlm.nih.gov/articles/PMC12689790)
- The paper's results text and its abstract conflict on whether Perplexity's trial-2 LoA spans 0.39. — [PMC12689790](https://pmc.ncbi.nlm.nih.gov/articles/PMC12689790)

### Inferences
- **Confidence in the chatbot study: MODERATE-LOW.** It is small (n=50, 1 rater) and the models are outdated (2024). But the direction is consistent across all four models: systematic underestimation, especially at high pain, which is the dangerous direction for a triage app. Gemini Nano on-device is a much smaller model than Gemini 1.5 Pro and is unlikely to do better; this is untested, an inference.
- With a positive bias of about 0.22, a cat truly at 0.5 (5/10) would often be scored below the 0.4 threshold. That means false reassurance.

### Gaps
- No peer-reviewed evaluation of newer models (Gemini 2.x/3, GPT-5, Claude 4.x) or Gemini Nano for FGS was found.
- No study of fine-tuned VLMs, or of VLMs given landmark or geometry inputs, for cat pain was found.

## Q4. Which approach is most feasible on a mobile device?

### Takeaway
A small on-device pipeline is the most feasible and best-evidenced option: a cat-face detector (YOLOv8-class), then a mobile landmark CNN (ShuffleNetV2/MobileNetV3, 4-6M parameters, about 0.05 s/image), then geometric descriptors, then tiny XGBoost/MLP heads per AU (the Steagall 2023 design). It is light and explainable, maps directly onto FGS AUs, and published evidence beats the LLM-prompting approach.

### Cited Findings
- Steagall 2023 landmark models: 4.32-10.10M parameters, about 0.04-0.08 s per image; the authors say the technology "can now be used" for smartphone app development. — [PMC10703818](https://pmc.ncbi.nlm.nih.gov/articles/PMC10703818/)
- The Haifa video pipeline uses YOLOv8 face detection and filters out frames with low face or landmark quality, a quality-gating idea useful for owner photos. Its TiHo data was filmed on a mobile phone. — [Sci Rep PDF (DNB copy)](https://d-nb.info/1355225132/34)
- Automated landmarks cost accuracy compared with manual ones (73% to 66% for pain), so landmark-detector quality is the bottleneck. — [Frontiers 2024](https://frontiersin.org/journals/veterinary-science/articles/10.3389/fvets.2024.1442634/full)
- Raw-image ResNet50 underperforms landmarks on small heterogeneous data (65% vs 77.5%). — [PMC10238514](https://pmc.ncbi.nlm.nih.gov/articles/PMC10238514/)

### Inferences
- Recommended PurrSense direction: (a) train a mobile landmark detector on CatFLW (public) plus FGS-labelled data; (b) compute FGS geometric descriptors and per-AU classifiers; (c) gate on face detection, frontal pose and landmark confidence, and ask for a retake when they fail; (d) if the LLM stays, use it only as a secondary explainer or quality checker, never as the scorer. Apply a calibration offset at minimum: published LLM bias is underestimation.
- Partnering with the Steagall group (owners of the FGS and of 1,188 FGS-scored images) or the Haifa group (owners of CatFLW and the pain pipelines) is likely the fastest route to validated training data.
- Any claim of diagnostic accuracy for PurrSense would need its own validation on owner-taken photos, with vet FGS scoring as ground truth and a subject-level split.

### Gaps
- No published benchmark of on-device latency or accuracy on real phones (TFLite/Core ML) for any cat pain model was found.
- No public pretrained FGS model weights were found.
