# Cat Face Detection, Facial Landmarks, and Individual Cat Re-Identification (Mobile-Oriented)

Researcher notes, compiled 2026-11-26. Confidence ratings: HIGH = peer-reviewed, primary source read; MEDIUM = peer-reviewed workshop or preprint with numbers read from the source; LOW = abstract or snippet only, preprint, or numbers not checked against the full text.

## Cat facial landmark detection (CatFLW / ELD, Sun & Murata, other 2022-2026 work)

### Takeaway
The benchmark to cite is CatFLW, with 48 anatomically grounded landmarks on about 2,000 images, and its Ensemble Landmark Detector (ELD), published in IJCV 2024. Its best NME is 2.83 with an EfficientNetV2S backbone, and a MobileNetV2 variant reaches 3.09, which suggests on-device use is realistic. Sun & Murata (WACVW 2020) is an earlier 3D morphable-model approach with a 10k-image, 15-landmark relabeled dataset. I found no peer-reviewed 2025-2026 successor that beats ELD on CatFLW.

### Cited Findings
- **Martvel, Shimshoni & Zamansky, "Automated Detection of Cat Facial Landmarks", International Journal of Computer Vision, 2024 (DOI 10.1007/s11263-024-02006-w).**
  - Introduces CatFLW: 2,091 in-the-wild cat images with 48 facial landmarks grounded in cat facial anatomy, annotated with an AI-assisted method.
  - ELD is a CNN-based detector that uses a "magnifying ensemble" approach.
  - Sources: [Springer](https://link.springer.com/article/10.1007/s11263-024-02006-w); [arXiv 2310.09793](https://arxiv.org/abs/2310.09793v2). Confidence: HIGH (peer-reviewed IJCV).
- **Reported NME on CatFLW** (normalized by inter-ocular distance; lower is better): EfficientNetV2S 2.83, EfficientNetV2B0 2.98, MobileNetV2 3.09. The same detector reaches NME 4.65 on the human WFLW benchmark (EfficientNetV2B1), and the authors say this is competitive with SOTA human landmark models.
  - Source: [Papers with Code listing](https://astro.paperswithcode.com/paper/automated-detection-of-cat-facial-landmarks). Confidence: MEDIUM. These are aggregator numbers that match the paper's backbones, but I did not check them against the paper's tables.
- **Earlier CatFLW dataset paper (arXiv 2305.04232, 2023)** reports 2,016 images and a semi-supervised, human-in-the-loop annotation pipeline. The image count differs from the IJCV version (2,091).
  - Source: [arXiv 2305.04232](https://arxiv.org/pdf/2305.04232); [PwC](https://astro.paperswithcode.com/paper/catflw-cat-facial-landmarks-in-the-wild). Confidence: MEDIUM (preprint).
- **Downstream uses:** follow-up work from the same group (Hussein, Zamansky, Martvel 2025) uses the detector for breed, cephalic-type and pain recognition in cats.
  - Source: as summarized in search results around [the IJCV paper](https://link.springer.com/10.1007/s11263-024-02006-w). Confidence: LOW (I did not read the 2025 paper directly).
- **Sun & Murata, "CAFM: A 3D Morphable Model for Animals", WACV Workshops 2020 (Waseda University).**
  - Builds a parametric 3D cat face model from 50 samples and fits it to 2D images.
  - Contributes a relabeled cat face dataset of 10,000 images with 15 landmarks.
  - It is not a modern deep landmark detector.
  - Source: [CVF Open Access](https://openaccess.thecvf.com/content_WACVW_2020/html/w2/Sun_CAFM_A_3D_Morphable_Model_for_Animals_WACVW_2020_paper.html). Confidence: HIGH that the paper exists and on its contents; no NME comparison with CatFLW is available.
- **Style-transfer augmentation for animal landmarks (arXiv 2505.05640, 2025)** builds on cat and animal landmark benchmarks.
  - Source: [arXiv 2505.05640](https://arxiv.org/html/2505.05640v1). Confidence: LOW (preprint; I did not read its results).
- **Apple Vision API (unverified):** offers `VNRecognizeAnimalsRequest`, which returns cat and dog bounding boxes and labels, and `VNDetectAnimalBodyPoseRequest` (iOS 17+), which reportedly includes nose, eye and ear joints.
  - Source: [Apple docs page](https://developer.apple.com/documentation/vision/vnrecognizeanimalsrequest). The page did not render, so the details come from the fetch model's general knowledge. Confidence: LOW; verify in Xcode docs.

### Inferences
- PurrSense could normalize and align cat face crops before re-ID using a CatFLW-trained MobileNetV2 ELD (NME around 3.1) or a YOLO-pose model fine-tuned on CatFLW. On iOS, a cheaper option is the eye, ear and nose joints from Apple's animal body pose request.
- Face alignment from landmarks generally helps face re-ID. I found no cat-specific ablation that quantifies this.

### Gaps
- I found no published on-device latency or model-size numbers for ELD.
- I found no 2025-2026 peer-reviewed paper reporting better results than ELD on CatFLW. A YOLO-pose or real-time follow-up from the Martvel group was not found.
- The Sun & Murata landmark dataset has no NME benchmark comparable to CatFLW.

## Individual cat identification / re-identification (PetFace, MegaDescriptor, metric learning, nose prints)

### Takeaway
Embedding-based re-ID trained with a metric-learning loss (ArcFace) is the clearly best-supported approach.

- **PetFace (ECCV 2024 Oral)**, ArcFace on ResNet-50:
  - Cats, closed-set over a huge gallery: 70.3% top-1 with joint training.
  - Cats, open-set verification: 98.0% AUC.
  - Off-the-shelf CLIP scores 85.1% AUC and MegaDescriptor 87.3%.
- **Cat Individual Images (518 cats; CVPR 2026 FGVC workshop paper):**
  - Generic foundation embeddings score 83-89% top-1 (DINOv2-Small 85.5%, SigLIP2-Giant 89.0%).
  - These are slightly ahead of MegaDescriptor (84.2%).
- **Cat nose-print biometrics:** no published accuracy figures were found. Petnow-style nose-print evidence exists only for dogs and cattle.

### Cited Findings
- **Shinoda & Shiohara, "PetFace: A Large-Scale Dataset and Benchmark for Animal Identification", ECCV 2024 (Oral).**
  - Scale: about 257,484 individuals, 13 animal families, 319 breeds, about 1M images.
  - Two benchmarks: re-identification of seen individuals (closed set; at least 3 photos per individual, 2 for train and 1 for test) and verification of unseen individuals (open set).
  - Licence: research use only.
  - Sources: [arXiv 2407.13555](https://arxiv.org/html/2407.13555v2); [project page](https://dahlian00.github.io/PetFacePage); [ECCV](https://eccv2024.ecva.net/virtual/2024/oral/1070). Confidence: HIGH.
- **PetFace cat re-ID, top-1 on seen individuals (ResNet-50):**

  | Method | Cat top-1 |
  |---|---|
  | Softmax | 30.46% |
  | Center loss | 0.00% |
  | ArcFace | 54.29% |
  | ArcFace, jointly trained on all PetFace families | 70.30% |

  The gallery holds a very large number of cat identities; the paper mentions 113,592 cat test classes. So these figures are not comparable to choosing among 2-10 cats.
  - Source: [arXiv 2407.13555 Table 2](https://arxiv.org/html/2407.13555v2). Confidence: HIGH.
- **PetFace cat verification AUC on unseen individuals:**

  | Method | Cat AUC |
  |---|---|
  | Softmax | 97.97 |
  | Center loss | 50.20 |
  | Triplet | 96.94 |
  | ArcFace | 97.71 |
  | ArcFace, jointly trained | 98.04 |
  | ImageNet ResNet-50 | 81.58 |
  | CLIP ResNet-50 | 85.10 |
  | MegaDescriptor (Swin-B) | 87.30 |

  Backbone comparison: ResNet-50 97.71, ViT-B/32 94.44, Swin-B 87.35. DINOv2 was not evaluated.
  - Source: [arXiv 2407.13555 Tables 3 and 5](https://arxiv.org/html/2407.13555v2). Confidence: HIGH.
- **Kudryavtsev, Borodin, Berezin, Bubenchikov, Mkrtchian, Ryzhkov, "From Visual to Multimodal: Systematic Ablation of Encoders and Fusion Strategies in Animal Identification", arXiv 2603.02270.**
  - Venue: accepted to the CVPR 2026 FGVC13 workshop. The arXiv page also carries journal-style metadata (vol. 12, art. 30, 2026), and the dates on it are inconsistent.
  - Training corpus: about 1.9M photos of 695,091 animals.
  - Results on the Cat Individual Images set (13,542 photos, 518 cats):

    | Model | Top-1 | ROC AUC | EER |
    |---|---|---|---|
    | CLIP-ViT-B | 83.59% | 0.982 | |
    | DINOv2-Small | 85.47% | 0.990 | 0.042 |
    | SigLIP2-Giant | 88.99% | 0.994 | 0.034 |
    | SigLIP2-Giant + text, best fusion | 89.6% | | |
    | MiewID-msv3 | 87.23% | | |
    | MegaDescriptor-L-384 | 84.21% | 0.936 | |
    | BioCLIP | 84.73% | | |

  - The paper's cat table lists MiewID twice with different values, which is likely a labelling error.
  - Sources: [arXiv 2603.02270](https://arxiv.org/html/2603.02270); [PMC version](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC12843040/). Confidence: MEDIUM (workshop paper, metadata inconsistencies).
- **Čermák, Picek, Adam, Papafitsoros, "WildlifeDatasets: An open-source toolkit for animal re-identification" (WACV 2024; arXiv 2311.09118).**
  - Introduces MegaDescriptor, a Swin-based re-ID foundation model in several sizes.
  - Reports that it outperforms CLIP and DINOv2 by a significant margin on wildlife re-ID datasets.
  - Sources: [arXiv 2311.09118](https://arxiv.org/html/2311.09118v2); [ar5iv](https://ar5iv.labs.arxiv.org/html/2311.09118). Confidence: HIGH for the general claim. The claim covers wildlife, mostly pattern-based species; cat-specific results came from other papers (see above).
- **MegaDescriptor-L-384 model card:** Swin-L, 228.8M parameters, 384x384 input, CC BY-NC 4.0 licence, so it cannot be used commercially without permission.
  - Source: [Hugging Face BVRA/MegaDescriptor-L-384](https://huggingface.co/BVRA/MegaDescriptor-L-384). Confidence: HIGH.
- **Adam, Čermák, Papafitsoros, Picek, "WildlifeReID-10k", CVPR 2025 Workshops (FGVC).**
  - Covers more than 10k identities of about 33 species across more than 140k images, cats included.
  - Argues that random splits leak near-duplicates and proposes similarity-aware splits.
  - Uses MegaDescriptor as the baseline.
  - Sources: [CVF Open Access](https://openaccess.thecvf.com/content/CVPR2025W/FGVC/html/Adam_WildlifeReID-10k_Wildlife_re-identification_dataset_with_10k_individual_animals_CVPRW_2025_paper.html); [arXiv 2406.09211](https://arxiv.org/abs/2406.09211). Confidence: HIGH. I did not extract cat-subset numbers.
- **Conflicting evidence on DINOv2 versus MegaDescriptor:**
  - WildlifeDatasets reports that MegaDescriptor beats DINOv2 on wildlife.
  - The cat results in Kudryavtsev et al. show DINOv2-Small (85.5%) beating MegaDescriptor-L (84.2%) on top-1 for cats.
  - A jaguar preprint reports frozen DINOv2-Giant beating fine-tuned MegaDescriptor-L.
  - Sources: [WildlifeDatasets](https://arxiv.org/html/2311.09118v2); [Kudryavtsev et al.](https://arxiv.org/html/2603.02270); [Research Square jaguar preprint](https://www.researchsquare.com/article/rs-10427175). Confidence: MEDIUM. This looks species-dependent: MegaDescriptor is tuned to patterned wildlife.
- **AnimalCLEF 2025/2026 entries** report that raw general-purpose foundation features give weak individual-level discrimination, and that triplet-loss fine-tuning on top of DINOv2 helps.
  - Sources: [CEUR-WS Vol-4038 paper 231](https://ceur-ws.org/Vol-4038/paper_231.pdf); [CEUR-WS Vol-4283 paper 339](https://ceur-ws.org/Vol-4283/paper339.pdf); [arXiv 2509.12353](https://arxiv.org/abs/2509.12353v1). Confidence: MEDIUM (challenge working notes).
- **Small cat-specific studies:**
  - Li & Zhou (arXiv 2410.02305, Oct 2024 preprint): transfer-learned CNNs (ConvNeXt, DenseNet) beat fine-tuning and Siamese networks for individual cat recognition. Numbers are not in the abstract.
  - Trein & Garcia (Hello Street Cat): Siamese networks on 2,796 images of 69 street cats.
  - Klein: EfficientNet with regularized triplet loss for cat faces.
  - Sources: [arXiv 2410.02305](https://arxiv.org/abs/2410.02305); [arXiv 2501.02112](https://arxiv.org/pdf/2501.02112.pdf). Confidence: LOW (preprints, small datasets).
- **UniReID, "Toward Re-Identifying Any Animal", NeurIPS 2023** prompts GPT-4 for text phrases that describe distinguishing visual cues. These texts guide a vision re-ID model; they do not replace it.
  - Source: [NeurIPS 2023 paper](https://proceedings.neurips.cc/paper_files/paper/2023/file/7df69dbf39705c7a39b40f2d70e806c1-Paper-Conference.pdf). Confidence: HIGH.
- **Nose prints:** I found no peer-reviewed cat nose-print identification accuracy.
  - Dogs: 98.97% Rank-1 with cross-validation ([IEEE Access 2021](https://doaj.org/article/94b2614fe0db4d1c892491dcc386beed)); contrastive dual-descriptor matching ([arXiv 2206.00580](https://arxiv.org/pdf/2206.00580)).
  - Cats: PawPrint (arXiv 2505.17445) covers footprints of only 7 cats.
  - Source: [arXiv 2505.17445](https://arxiv.org/abs/2505.17445v1). Confidence: MEDIUM for the dog numbers, N/A for cats.

### Inferences
- For PurrSense the task is closed-set identification among 2-10 enrolled cats, plus an open-set "unknown cat" rejection. That is far easier than the PetFace or 518-cat benchmarks, where 70-89% top-1 is achieved over hundreds to 100k+ identities.
  - With a small gallery, several enrolled photos per cat and a cosine-similarity threshold, embedding approaches should plausibly reach very high accuracy.
  - The hard cases are look-alike cats, such as two black cats or littermates, and poor-quality photos.
  - This is an extrapolation; no paper tests exactly the 2-10 cat household setting.
- For a commercial app, the practical candidates are:
  - An ArcFace-trained ResNet-50 or MobileNet on PetFace-style data. Note that the PetFace licence is research only.
  - DINOv2-Small fine-tuned with ArcFace or triplet loss.
  - MegaDescriptor is CC BY-NC and too large in its L variant (229M parameters).
- Combining face and body crops may help: Li & Zhou note that prior cat work used faces only. This is unquantified.

### Gaps
- I found no PetFace numbers for DINOv2 or SigLIP on cats.
- I found no published benchmark for few-shot closed-set re-ID among 2-10 cats.
- I did not extract cat-subset accuracy from WildlifeReID-10k.
- I found no public evidence on Petnow-style cat nose prints; Petnow's own claims were not located.

## LLM text-description matching vs embedding-based re-ID for a small set of household cats

### Takeaway
The most direct evidence is Kudryavtsev et al. (2026):

- **Text-only identification on 518 cats:** text descriptions generated by a VLM (Qwen3-VL) and encoded as text identify cats far less accurately than image embeddings, about 53-55% top-1 versus 85-89%.
- **Text plus image:** adding text to strong image embeddings gives at most about 0.6 percentage points.
- **General VLMs on direct pair comparison:**
  - Bio-VQA (WACV 2027) tested 18 open-weight VLMs on "same animal?" pairs. The best, Qwen3-VL-32B, reached only 63.6% accuracy against 50% chance.
  - In adversarial look-alike conditions, 17 of 18 VLMs fell below chance.

Embedding-based re-ID should therefore be substantially better and more robust than LLM text matching.

### Cited Findings
- **Kudryavtsev et al. 2026:**
  - The authors generated standardized descriptions with Qwen3-VL because manual descriptions were unavailable.
  - Text encoders (BERT, E5 variants) used alone performed far below image encoders. On cats, text-only top-1 was about 53-55%, against 85.5% for DINOv2-Small and 89.0% for SigLIP2-Giant.
  - The best multimodal fusion reached 89.6%, a marginal gain over image-only, and did not improve AUC or EER.
  - Source: [arXiv 2603.02270](https://arxiv.org/html/2603.02270). Confidence: MEDIUM.
- **Rayeed, Ma, Stewart, Ma, "Bio-VQA: How Finely Can Vision-Language Models See Biology?", arXiv 2611.05559 (Nov 2026; accepted to WACV 2027).**
  - Setup: 18 open-weight VLMs (3.75B-38.4B; Qwen2.5/3-VL, InternVL3/3.5, Gemma-3/4, Molmo 2, Pixtral, Phi-3.5-V, Llama-3.2-V, LLaVA-NeXT) on 15,000 same/different pairs from 5 wildlife catalogs.
  - Random pairs: best accuracy 0.636 (Qwen3-VL-32B); VLM mean 0.558; chance 0.50. MegaDescriptor reached 0.683 on the one catalog where it was tested (Grevy's zebra).
  - Easy pairs: MegaDescriptor d′ 2.055 versus a VLM mean of 1.346.
  - Hard pairs, where appearance and identity disagree: 17 of 18 VLMs scored below chance. MegaDescriptor also had negative d′ on that adversarial set.
  - VLMs became more inclined to answer "same" as pairs got harder.
  - Sources: [arXiv abstract](https://arxiv.org/abs/2611.05559); [HTML](https://arxiv.org/html/2611.05559). Confidence: MEDIUM (preprint, accepted to WACV; the paper has internal inconsistencies in its text versus tables).
  - Caveats: these are wildlife species, not cats. No closed frontier models (GPT, Gemini, Claude) were tested.
- **Bio-VQA authors' framing:** they were not aware of any prior benchmark that asks generalist VLMs to do re-ID, because the task is normally handled by purpose-built matchers.
  - Source: [arXiv 2611.05559](https://arxiv.org/html/2611.05559). Confidence: MEDIUM.
- **UniReID (NeurIPS 2023):** LLM text works as auxiliary guidance for a visual re-ID model, not as the matcher itself.
  - Source: [NeurIPS 2023](https://proceedings.neurips.cc/paper_files/paper/2023/file/7df69dbf39705c7a39b40f2d70e806c1-Paper-Conference.pdf). Confidence: HIGH.

### Inferences
- With only 2-10 cats that differ in coat colour or pattern, LLM text matching probably works acceptably, since coarse attributes suffice.
  - It is likely to fail on visually similar cats (same coat colour, littermates). Bio-VQA's hard-pair results show VLMs answer from appearance, not identity cues.
  - It is also non-deterministic, costly (cloud calls), slow, and gives no calibrated similarity score for rejecting unknown cats.
- Embedding re-ID gives a continuous similarity score usable for an "unknown cat" threshold. It can run on device in milliseconds and improves with more enrolment photos.
- A reasonable hybrid is an on-device embedding as the primary matcher, with the LLM description as a tiebreaker or explanation layer.

### Gaps
- I found no published head-to-head of a frontier closed VLM (GPT-4o/5, Gemini, Claude) against embedding re-ID on cats.
- I found no study in a 2-10 identity household setting.
- The 53-55% text-only figure comes from encoded synthetic descriptions over 518 cats, not from LLM-judged matching against a few stored descriptions.

## Mobile feasibility (model sizes, on-device inference, cat face detectors)

### Takeaway
Every stage has a feasible mobile option:

- **Detection:** Apple Vision animal detection on iOS, or a YOLO-nano or MobileNet-SSD cat face or cat detector fine-tuned on public Roboflow sets.
- **Landmarks:** ELD with a MobileNetV2 backbone (NME 3.09).
- **Re-ID embedding:** a ResNet-50, MobileNet or DINOv2-Small class network.

MegaDescriptor-L (229M parameters, non-commercial licence) is not suitable. I found no published on-device latency benchmarks for cat-specific models.

### Cited Findings
- **ELD MobileNetV2 variant:** NME 3.09 on CatFLW, versus 2.83 for EfficientNetV2S. The accuracy cost of a mobile backbone is small.
  - Source: [PwC](https://astro.paperswithcode.com/paper/automated-detection-of-cat-facial-landmarks). Confidence: MEDIUM.
- **Wang, YOLOv8 + TensorRT cat face detection (SPIE / ICCPA 2024):** inference reportedly drops from 50.1 ms to 0.9 ms on a GPU, not a phone.
  - Source: [SPIE](https://spiedigitallibrary.org/conference-proceedings-of-spie/13256/132560S/Enhanced-feline-facial-recognition--advancing-cat-face-detection-with/10.1117/12.3037875.full). Confidence: LOW (abstract only).
- **Public cat face detection datasets (Roboflow Universe):** "cat face" (699 images, YOLO format) and "stray-cat-ph" (about 3,200 images). Licensing varies.
  - Sources: [cat-face](https://universe.roboflow.com/cat-test/cat-face-xejom/dataset/2); [stray-cat-ph](https://universe.roboflow.com/stray-cats/stray-cat-ph/dataset/8). Confidence: MEDIUM (community data, not peer-reviewed).
- **YOLO26 identifying 3 specific household cats (JAISE 2026, Indonesian journal):** the closest analogue to the PurrSense setting, but it uses a classification-style detector rather than enrolment by embedding.
  - Source: [JAISE](https://e-jurnal.pnl.ac.id/JAISE/article/download/9076/6276). Confidence: LOW.
- **MegaDescriptor-L-384:** 228.8M parameters, CC BY-NC 4.0.
  - Source: [HF](https://huggingface.co/BVRA/MegaDescriptor-L-384). Confidence: HIGH.
- **PetFace backbones:** a ResNet-50 ArcFace model performed best for cat verification (AUC 97.71), better than ViT-B/32 and Swin-B. A CNN-class backbone suits Core ML and TFLite well.
  - Source: [arXiv 2407.13555](https://arxiv.org/html/2407.13555v2). Confidence: HIGH.
- **Kudryavtsev et al.:** DINOv2-Small, the smallest foundation encoder tested, reached 85.5% top-1 on 518 cats, only about 3.5 points below SigLIP2-Giant.
  - Source: [arXiv 2603.02270](https://arxiv.org/html/2603.02270). Confidence: MEDIUM.
- **Apple Vision:** `VNRecognizeAnimalsRequest` returns cat and dog bounding boxes; `VNDetectAnimalBodyPoseRequest` (iOS 17+) returns animal joints, including face points.
  - Source: [Apple docs](https://developer.apple.com/documentation/vision/vnrecognizeanimalsrequest). Confidence: LOW (page content not retrieved).

### Inferences
- The parameter counts below are commonly known values that I did not verify in this session; treat them as approximate.
  - DINOv2-Small (ViT-S/14) is about 21-22M parameters; ResNet-50 is about 25M; MobileNetV2 is about 3.5M.
  - All three are deployable through Core ML or TFLite/LiteRT, and quantized to INT8 or FP16 they become roughly 5-50 MB.
- Suggested pipeline:
  1. Detect the cat with Apple Vision on iOS, or a YOLO-n detector on Android.
  2. Optionally detect the face and landmarks to align the crop.
  3. Compute an embedding with a small backbone fine-tuned with ArcFace on cat face or body identity data.
  4. Match by cosine similarity against 3-10 enrolled embeddings per cat, with an unknown-cat threshold.
- On Android, Apple Vision is unavailable, so a cross-platform YOLO or MobileNet detector would unify the two platforms.

### Gaps
- I found no peer-reviewed on-device latency or energy benchmarks for cat face detection, landmark or re-ID models on phones.
- I did not verify Apple Vision's joint list or minimum OS versions from documentation.
- I did not verify the parameter counts of the smaller MegaDescriptor variants (T, S, B).
- Licensing for commercial use of PetFace-trained weights is restrictive (research only); a commercial-safe cat re-ID training set was not identified.
