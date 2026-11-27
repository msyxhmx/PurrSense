# Open-source cat- and pet-specific models and datasets PurrSense could reuse (checked 2026-11-26)

Method: I queried the Hugging Face Hub API directly for license tags, download counts and last-modified dates, and read model cards (README) and LICENSE text. I fetched GitHub, pub.dev and paper pages with a web fetcher, and ran web searches. The GitHub REST API was blocked in this session, so GitHub star counts and last-push dates could **not** be verified. "Dl" means downloads in the last month as reported by the HF API on 2026-11-26.

## 1. Cat face detection and cat facial landmarks (CatFLW / ELD weights)

### Takeaway
CatFLW-trained 48-landmark weights do now exist publicly (hugocornellier/cat-face-landmarks, shipped as TFLite, Sept 2026), but they are **CC BY-NC 4.0**, inherited from the CatFLW dataset. That makes them fine for prototyping and not usable in a commercial app without a licence from the Haifa Tech4Animals lab. The original ELD ensemble weights from Martvel et al. are not publicly released. For a commercially safe cat **face box**, the options are a CC BY 4.0 Roboflow cat-face dataset plus your own detector, or the OpenCV Haar cat-face cascade.

### Cited Findings
- **hugocornellier/cat-face-landmarks (HF)**
  - Two-stage TFLite pipeline:
    - `cat_face_localizer.tflite`: EfficientNetB2, 224 px, 16 MB, outputs one face box.
    - `cat_face_landmarks_full.tflite`: MobileNetV3-Large, 384 px, 11 MB, outputs 48 CatFLW landmarks.
  - Keras source models are also provided.
  - Accuracy on the CatFLW val split (311 images): NME_IOD 3.48, or 3.31 with TTA. The CatFLW paper's ELD ensemble reaches 2.91. Localizer bbox IoU is 0.81. Trained on 1,768 images.
  - Speed: the landmark stage runs at 3.82 ms on GPU vs 27.10 ms on CPU (M4 Max). Both files are float16 static-shape exports built for the LiteRT GPU delegate.
  - The author states these are "the first publicly released weights trained on CatFLW". A larger EfficientNetV2-S model (NME 3.27, 55 MB) was withdrawn on 24 Sep 2026 but remains in the repo history.
  - License tag: `cc-by-nc-4.0`. Dl ≈ 990, last modified 2026-09-24.
  - [HF model card](https://huggingface.co/hugocornellier/cat-face-landmarks); training code at [GitHub cat-face-landmarks-training](https://github.com/hugocornellier/cat-face-landmarks-training)
- **cat_detection Flutter package (pub.dev, v4.2.0, published ~mid-Nov 2026, verified publisher hugo.ml)**
  - Runs on-device on Android (minSdk 24), iOS 15+ and desktop.
  - Outputs cat body bbox, breed with confidence, body pose keypoints, face box and 48 face landmarks.
  - Licensing: Dart code is MIT, but the bundled models are CC BY-NC 4.0. The page explicitly says the package "can't be used in a commercial product". Commercial permission has to come from the Tech4Animals Lab, University of Haifa. — [pub.dev cat_detection](https://pub.dev/packages/cat_detection)
- **CatFLW dataset**
  - 2,079 cat face images (the arXiv version says 2,016), each with 48 landmarks plus a bbox.
  - Licensed **CC BY-NC 4.0**. Downloadable from Kaggle (georgemartvel/catflw).
  - The GitHub page mentions no released model weights. — [GitHub martvelge/CatFLW](https://github.com/martvelge/CatFLW); [arXiv 2305.04232](https://arxiv.org/pdf/2305.04232.pdf)
- **Haifa group follow-up work**
  - "Automated landmark-based cat facial analysis and its applications" (Frontiers Vet Sci, Dec 2024) builds breed, cephalic-type and pain recognition on the 48-landmark scheme. The search turned up no code or weights release. — [Frontiers 10.3389/fvets.2024.1442634](https://frontiersin.org/journals/veterinary-science/articles/10.3389/fvets.2024.1442634/full)
- **Other HF cat face models are toy-level**
  - `d-v-18/cat-face-detector` (MIT, 0 downloads) wraps OpenCV's `haarcascade_frontalcatface.xml`.
  - `harisansarkhan/CatFaceLandmarks` has no license and 0 downloads.
  - Source: HF API query 2026-11-26, [d-v-18/cat-face-detector](https://huggingface.co/d-v-18/cat-face-detector)
- **Roboflow Universe**
  - Single-class cat-face datasets "cat face" (cat-test/cat-face-xejom) and "Cats Faces" (project1373/cats-faces-pqdnj) are listed as **CC BY 4.0** in search snippets.
  - CC BY 4.0 permits commercial use with attribution. — [cat-face-xejom](https://universe.roboflow.com/cat-test/cat-face-xejom); [cats-faces-pqdnj](https://universe.roboflow.com/project1373/cats-faces-pqdnj)
- **"Cat Dataset" (Kaggle crawford/cat-dataset)**
  - About 9k images with 9 head points.
  - No formal license was found. The only statement found, on the fast.ai forum, says the data is "for research use" and that the uploader claims no copyright over the images. Treat it as **unclear / research-only**. — [fast.ai forum](https://forums.fast.ai/t/adding-datasets-to-untar-data/58422); [gts.ai description](https://gts.ai/dataset-download/cat-dataset/)
- **HF dataset `cvdl/catfaces`** is tagged MIT (106 dl). Its provenance was not verified. — [HF](https://huggingface.co/datasets/cvdl/catfaces)

### Inferences
- The CatFLW 48-point path is technically de-risked. A mobile-ready, GPU-delegate-friendly TFLite model at about 3.5 NME exists today, matching the earlier pipeline recommendation. **The blocker is legal, not technical.**
- Three routes are open:
  - (a) Negotiate a commercial licence with Haifa/Tech4Animals, covering both the dataset and the derivative weights.
  - (b) Annotate your own commercially owned landmark set. The open training code and journal can be reused, since the code licence was not verified but the Dart code is MIT.
  - (c) Use only a commercially safe face box (Roboflow CC BY data) and skip landmarks.
- A model trained on CC BY-NC data stays NC under the dataset terms, as the package author also concludes. Retraining on CatFLW yourself does not escape this.

### Gaps
- The license of the training-code repo (cat-face-landmarks-training) could not be verified because GitHub API access was blocked.
- I did not verify whether Haifa offers commercial licences or on what terms.
- The license on each Roboflow project page was verified only from search snippets, not by opening each page.

## 2. Cat pain / FGS models and FGS datasets

### Takeaway
There are still **no public FGS model weights or public FGS image sets**. The Steagall (Montreal) 2023 data is "available on reasonable request" and is being withheld for their own app. The Haifa pain papers released no weights either. The HF Hub has zero cat-pain models.

### Cited Findings
- **Steagall et al., Sci Rep 2023**
  - Data: 3,447 cat face images annotated with 37 landmarks (built around the 5 FGS action units); 1,188 of those images carry FGS scores.
  - Landmark models: ShuffleNetV2, EfficientNetB0 and MobileNetV3. Best NRMSE was 16.76%.
  - FGS scoring: XGBoost on 35 geometric descriptors. Binary pain accuracy 95.51%, AUROC 0.97. Regression MSE 0.0096.
  - Data availability, quoted: data are "not publicly available due to the undergoing development of the mobile phone application" but "available from the corresponding author on reasonable request". There is no code-availability statement. — [PMC10703818](https://pmc.ncbi.nlm.nih.gov/articles/PMC10703818/)
- **Martvel/Zamansky et al., Sci Rep Nov 2024**
  - End-to-end video pain recognition with no manual landmarks: >70% accuracy on one dataset and 66% on another.
  - The search turned up no released model. — [Springer 10.1038/s41598-024-78406-2](https://link.springer.com/10.1038/s41598-024-78406-2); [DOAJ](https://doaj.org/article/28a19f6ee2714e309df9cbb1078118b7)
- **Earlier comparison:** ResNet50 and a geometric-landmark approach both reached >72% pain accuracy. — [PMC9187730](https://pmc.ncbi.nlm.nih.gov/articles/PMC9187730)
- **FGS validation paper (2019):** [PMC6911058](https://pmc.ncbi.nlm.nih.gov/articles/PMC6911058)
- **HF Hub:** a search for "cat pain" on 2026-11-26 returned no relevant models and no datasets. — HF API

### Inferences
- The FGS step will have to be built in-house. The realistic recipe is CatFLW-style landmarks → geometric descriptors → gradient-boosted classifier, as in Steagall 2023. It needs either a data-sharing or research agreement with the Steagall lab, or PurrSense-collected images scored by vets.
- Steagall's 95.5% figure is on curated clinical images; the Haifa numbers on more naturalistic data are 66–77%. Expect lower accuracy on owner phone photos.

### Gaps
- I found no 2025–2026 FGS model release. A general web search may miss very recent papers, so a PubMed or Scholar check is advised.
- Whether Steagall's lab would license data for commercial use is unknown.

## 3. Cat emotion / mood classifiers and cat breed classifiers

### Takeaway
The public cat "emotion" image classifiers are hobby-grade: small custom datasets, about 64% val accuracy, no scientific labelling basis. Breed classifiers are plentiful and Apache-licensed, but trained on scraped data of unclear provenance. Neither type is a credible basis for a "mood valence" feature.

### Cited Findings
- **semihdervis/cat-emotion-classifier**
  - ViT-base fine-tuned on a "custom dataset" (imagefolder). Val accuracy 0.635.
  - Apache-2.0, Dl 21, last modified 2024-09-14. — [HF card](https://huggingface.co/semihdervis/cat-emotion-classifier)
- **Belall87/Cat-Emotion-Classification-with-CNN** is MIT with 28 dl. **KAITANG2003/cat-emotion-dataset** has no license tag (263 dl). — HF API 2026-11-26
- **Cat sound emotion datasets (CatSoundClassificationDataset-V2 mirrors)** exist on HF. They are audio only and carry no license tag. — [HF DynamicSuperb](https://huggingface.co/datasets/DynamicSuperb/CatEmotionClassification_CatSoundClassificationDataset-V2)
- **Breed classifiers**
  - `dima806/cat_breed_image_detection` (Apache-2.0, 218 dl, 7 likes, 2024-10-19).
  - `dima806/67_cat_breeds_image_detection` (Apache-2.0).
  - `ferdifdi/cat-breed-60-classes` (MIT).
  - Source: [HF dima806](https://huggingface.co/dima806/cat_breed_image_detection)
- **Oxford-IIIT Pet**
  - 37 pet breeds (12 cat breeds), with breed labels, head bboxes and segmentation.
  - Licensed "for commercial/research purposes under a Creative Commons Attribution-ShareAlike 4.0 International License. The copyright remains with the original owners of the images." — [VGG Oxford Pets](https://www.robots.ox.ac.uk/~vgg/data/pets/); also the HF mirror timm/oxford-iiit-pet, tagged cc-by-sa-4.0

### Inferences
- Model weights carrying an Apache tag don't guarantee clean training data, because these breed models were trained on scraped images.
- Oxford-IIIT Pet is the cleanest commercially usable cat image set. Its ShareAlike clause matters if you redistribute derived datasets. Whether ShareAlike reaches trained weights is legally debated, so a legal review is needed.
- For mood, an owner-facing feature would be better grounded in the Haifa CatFACS/landmark work or in your own labelled data than in any HF classifier.

### Gaps
- I found no scientifically labelled public cat emotion image dataset with a clear commercial license.
- CatFACS resources were not checked; the tool-call budget ran out.

## 4. Pet re-identification (which registered cat): PetFace, MegaDescriptor, MiewID, others

### Takeaway
Every strong re-ID backbone is either non-commercial or unlicensed:
- PetFace weights, including ArcFace `cat.pt`, are research-only.
- MegaDescriptor is CC BY-NC 4.0.
- MiewID has **no license** declared.

There is no commercially clean cat re-ID model. A commercial app needs to train its own embedding model on a permissive backbone, using its own users' cat photos (with consent) plus permissively licensed data.

### Cited Findings
- **PetFace (ECCV 2024 Oral)**
  - 257,484 individuals across 13 animal families and 319 breeds, cats included.
  - The repo ships ArcFace pretrained weights per family, including `cat.pt`.
  - The README says the dataset, code and pretrained models are for **non-commercial research only**. No formal LICENSE file was seen in the mirror. — [Project page](https://dahlian00.github.io/PetFacePage); [arXiv 2407.13555](https://arxiv.org/abs/2407.13555v2); [Gitee mirror](https://gitee.com/noteplus/PetFace)
  - Conflict: the canonical repo is unclear. github.com/ktshio/PetFace returned 404, and mapooon/PetFace is indexed instead. — [relatedrepos](https://relatedrepos.com/gh/mapooon/PetFace)
- **MegaDescriptor-L-384 (BVRA, WildlifeDatasets, WACV 2024)**
  - Swin-L, 228.8 M parameters, 384 px input, trained on all public animal re-ID datasets.
  - **cc-by-nc-4.0**, Dl ≈ 465k, last modified 2024-10-14. — [HF card](https://huggingface.co/BVRA/MegaDescriptor-L-384)
- **MiewID-msv3 (Conservation X Labs / Wild Me)**
  - EfficientNetV2-rw-m backbone, 51.1 M parameters, 440 px input. Contrastive training on 64 species.
  - Its benchmark table includes "dog" (DogFaceNet): mAP 70.9, rank-1 78.7. No cat row.
  - The HF card has **no license tag**, and the GitHub repo shows only "Copyright Conservation X Labs 2025" with no LICENSE file. That makes it **all-rights-reserved by default / unclear**.
  - Dl ≈ 451k, last modified 2025-02-13. — [HF card](https://huggingface.co/conservationxlabs/miewid-msv3); [GitHub](https://github.com/WildMeOrg/wbia-plugin-miew-id)
- **HF `Cloudy2026/pet_face`**: a single `best_w8a32.tflite` file, Apache-2.0, an empty card and 18 dl. Unknown quality. — [HF](https://huggingface.co/Cloudy2026/pet_face)
- **Petnow and similar commercial pet-biometric vendors**: I found no open weights.

### Inferences
- The best choice for an internal benchmark or prototype is the PetFace cat ArcFace model or MegaDescriptor, under research use only.
- The best choice for a shipped product is your own ArcFace/sub-center head fine-tuned on a permissive backbone, with a registry of 1–N cats per household. Candidate backbones are BioCLIP 2 (MIT), DINOv2 (Apache-2.0, per general knowledge) or a MobileNet/EfficientNet trained on ImageNet.
- Matching among a household's few registered cats is a much easier problem than open-set re-ID over 250k identities. Even generic embeddings may suffice, and this needs validation.

### Gaps
- No cat-specific re-ID benchmark numbers were collected for MiewID or MegaDescriptor.
- I did not confirm whether Conservation X Labs would grant a commercial licence for MiewID.

## 5. Animal foundation models: BioCLIP / BioCLIP 2, SpeciesNet, MegaDetector, SuperAnimal

### Takeaway
- **BioCLIP 2 (MIT)** is the most useful commercially safe foundation model: a ViT-L/14 embedding backbone for cats.
- **SpeciesNet (Apache-2.0)** and **MegaDetector (MIT code)** are camera-trap tools. They are useful at most as "is there an animal/cat in the photo" gates, and MDv1000 weights carry GPL/AGPL baggage.
- **SuperAnimal-Quadruped** weights are academic/non-commercial only.

### Cited Findings
- **BioCLIP 2 (imageomics/bioclip-2)**
  - Model card license: **MIT**.
  - OpenCLIP ViT-L/14, trained on TreeOfLife-200M starting from a LAION-2B CLIP.
  - Dl ≈ 261k, last modified 2026-05-20, so actively maintained.
  - The card claims state-of-the-art species recognition and "emergent properties beyond species", meaning intra-species traits. — [HF card](https://huggingface.co/imageomics/bioclip-2)
- **BioCLIP v1** is also MIT (Dl ≈ 89k, modified 2026-02-26). — [HF card](https://huggingface.co/imageomics/bioclip)
- **SpeciesNet (google/cameratrapai)**
  - Apache-2.0 repository. Weight versions are 4.0.3a (crop classifier) and 4.0.3b (whole image), downloadable from Kaggle as PyTorch.
  - More than 2,000 labels, including higher taxa such as Felidae and non-animal classes.
  - No TFLite/mobile export is mentioned. Fine-tuning docs reference Oct 2026, so the project is active. — [GitHub](https://github.com/google/cameratrapai); [TechCrunch](https://techcrunch.com/2025/03/03/google-releases-speciesnet-an-ai-model-designed-to-identify-wildlife/)
  - Not verified: whether the Kaggle weights carry Apache-2.0, or whether a specific *Felis catus* label exists. Mirrors such as Addax-Data-Science are tagged apache-2.0. — [HF Addax mirror](https://huggingface.co/Addax-Data-Science/SPECIESNET-v4-0-1-A-v1/tree/main)
- **MegaDetector**
  - Repo is MIT; code on or before 2023-04-28 is © Microsoft. It detects animal / person / vehicle only, with no species. — [GitHub agentmorris/MegaDetector](https://github.com/agentmorris/MegaDetector)
  - MDv1000 (July 2025) variants, with the licence of their inference library:

    | Variant | Architecture | Inference-library licence |
    |---|---|---|
    | redwood | YOLOv5x6 | GPL |
    | cedar | YOLOv9c | GPL |
    | larch | YOLOv11L | AGPL |
    | sorrel | YOLOv11s | AGPL |
    | spruce | YOLOv5s | GPL |

    — [MDv1000 release notes](https://github.com/agentmorris/MegaDetector/blob/main/docs/release-notes/mdv1000-release.md)
- **SuperAnimal-Quadruped (DeepLabCut)**
  - The HF card says "Modified MIT", but the text grants "a fully-paid, non-exclusive, and non-transferable license for **academic, non-commercial purposes only**" to use the model weights.
  - Last modified 2025-06-30. — [HF mwmathis/DeepLabCutModelZoo-SuperAnimal-Quadruped](https://huggingface.co/mwmathis/DeepLabCutModelZoo-SuperAnimal-Quadruped)
- **AP-10K** (10,015 images, 54 species, 17 keypoints)
  - Licence conflict: the official GitHub says CC-BY-4.0, while the MMPose docs say "non-commercial use only". — [AP-10K GitHub](https://github.com/superz678/ap-10k); [MMPose docs](https://mmpose.readthedocs.io/en/0.x/tasks/2d_animal_keypoint.html)
- **Animal Kingdom, iNaturalist, BioTrove**: not verified within the tool budget.

### Inferences
- For PurrSense, BioCLIP 2 is the strongest permissive backbone to fine-tune for both cat re-ID embeddings and breed. At ViT-L it is heavy on mobile, so it would need distillation or server-side inference.
- MegaDetector and SpeciesNet target camera-trap imagery and are overkill for close-up owner photos. COCO-trained detectors already detect "cat", though Ultralytics YOLO models also carry AGPL.
- Body-pose keypoints (SuperAnimal) are irrelevant to FGS, which is facial, and NC anyway.

### Gaps
- Licences for Animal Kingdom, iNaturalist (per-image CC licences vary) and BioTrove were not checked.
- The licences of BioCLIP 2's training data (TreeOfLife-200M is mixed-licence) were not examined for downstream implications.
- I did not verify the SpeciesNet Kaggle weights licence or its domestic-cat label.

## 6. Summary: best option per job, and commercial safety

### Takeaway

| Job | Best for prototype | Commercially safe option |
|---|---|---|
| Cat face box | hugocornellier `cat_face_localizer.tflite` (NC) | Train a small detector on CC BY 4.0 Roboflow cat-face data plus Oxford-IIIT Pet head boxes (CC BY-SA 4.0); the OpenCV Haar cat-face cascade as a crude fallback |
| 48 facial landmarks | hugocornellier `cat_face_landmarks_full.tflite` (CC BY-NC 4.0, NME 3.48, 11 MB, mobile GPU) | None. Needs a Haifa licence or your own annotations |
| FGS pain | Nothing public. Request Steagall data | Build in-house: landmarks → geometric features → XGBoost, on vet-scored data |
| Mood / valence | No credible model (best HF classifier 63.5% on unknown labels) | Build in-house |
| Breed | dima806 ViT (Apache-2.0, provenance unclear) | Fine-tune BioCLIP 2 (MIT) on Oxford-IIIT Pet (CC BY-SA) |
| Re-ID | PetFace cat ArcFace (research only) or MegaDescriptor-L-384 (CC BY-NC) | Own ArcFace head on a BioCLIP 2 (MIT) or other permissive backbone |
| Animal / cat gate | MegaDetector / SpeciesNet | SpeciesNet (Apache-2.0 code), BioCLIP 2 zero-shot (MIT) |

### Cited Findings
- Licences per item are cited in sections 1–5 above.
- Activity indicators from the HF API on 2026-11-26:

  | Model | Last modified |
  |---|---|
  | cat-face-landmarks | 2026-09 |
  | cat_detection package | Nov 2026 |
  | BioCLIP 2 | 2026-05 |
  | SpeciesNet docs | Oct 2026 |
  | MiewID | 2025-02 |
  | MegaDescriptor | 2024-10 |
  | cat emotion and breed HF models | 2024 |

### Inferences
- The actively maintained, commercially safe set is short: BioCLIP 2 (MIT), SpeciesNet (Apache-2.0 code), Oxford-IIIT Pet (CC BY-SA 4.0) and Roboflow CC BY 4.0 cat-face sets.
- Everything cat-face-specific and high-quality is NC: CatFLW, its derivative weights, PetFace, MegaDescriptor and SuperAnimal. MiewID is unlicensed.

### Gaps
- GitHub star counts and last-push dates are unverified because the API was blocked.
- Training-data licence chains behind the Apache/MIT-tagged hobby models were not audited.
