# Open-source vision backbones for PurrSense (detection, landmarks, re-ID), on-device and commercially licensable

Research date: 2026-11-26. Licenses were checked against the primary source (repo LICENSE, README or HF model card) where a citation is given. Items marked **[unverified this session]** come from background knowledge and were not re-checked against a primary source. Confirm those before relying on them.

## Detection: which cat/cat-face detectors are Apache/MIT and mobile-ready?

### Takeaway
The two strongest permissively licensed modern detectors are **RF-DETR Nano/Small/Medium (Apache-2.0)** and **D-FINE (Apache-2.0)**. D-FINE-N/S are the most phone-friendly at 4M and 10M params. Ultralytics YOLO (v8/11/26) is AGPL-3.0, and Ultralytics says AGPL also covers custom-trained weights, so a closed-source app needs a paid Enterprise license. YOLOv9 (WongKinYiu) is GPL-3.0, which is also unsuitable for a closed app.

### Cited Findings
- **RF-DETR (Roboflow), ~10.2k GitHub stars.** The `rfdetr` package and the Nano, Small, Medium and Large detection sizes are Apache-2.0. Atto, Femto, Pico, XL, 2XL and the NAS models are under the Roboflow Platform Model License (PML 1.0), and commercial self-hosting of those requires an Enterprise add-on. — [roboflow/rf-detr](https://github.com/roboflow/rf-detr)
- RF-DETR COCO AP50:95 and T4 TensorRT FP16 latency: Nano 30.5M params, 48.4 AP, 2.3 ms; Small 32.1M, 53.0 AP, 3.5 ms; Medium 33.7M, 54.7 AP, 4.4 ms; Large 33.9M, 56.5 AP. The small PML models (Atto 7.4M, Femto 7.9M, Pico 8.4M) are the phone-sized ones, but they are not Apache. — [roboflow/rf-detr](https://github.com/roboflow/rf-detr)
- RF-DETR also ships an Apache-2.0 **Keypoint (Preview)** model: 40.7M params, 71.8 AP, 9.7 ms on T4. — [roboflow/rf-detr](https://github.com/roboflow/rf-detr)
- The RF-DETR README does not mention TFLite or Core ML export. — [roboflow/rf-detr](https://github.com/roboflow/rf-detr)
- **D-FINE**: Apache-2.0, ~3.4k stars. COCO val AP and T4 TensorRT FP16 latency: N 4M params, 42.8 AP, 2.12 ms, 7 GFLOPs; S 10M, 48.5 AP, 3.49 ms; M 19M, 52.3 AP; L 31M, 54.0 AP; X 62M, 55.8 AP. ONNX export is supported via `tools/deployment/export_onnx.py`. The README warns that Objects365-pretrained checkpoints "may be subject to the Objects365 dataset terms." — [Peterande/D-FINE](https://github.com/Peterande/D-FINE)
- **Ultralytics YOLO**: AGPL-3.0. A closed-source app or any commercial product needs an Enterprise License, which also applies to "custom-trained or fine-tuned YOLO models in a proprietary or commercial setting." Ultralytics states that trained models fall under AGPL-3.0 by default, even when trained from scratch. The Enterprise license covers "YOLO26 and earlier YOLO versions" listed in the order. — [Ultralytics License](https://www.ultralytics.com/license)
- **YOLOv9 (WongKinYiu)**: GPL-3.0, ~9.6k stars. TFLite export appears only via a community issue, and Core ML is not documented. — [WongKinYiu/yolov9](https://github.com/WongKinYiu/yolov9)
- An existing Flutter package, `cat_detection` (pub.dev), ships a 17 MB, 224x224 cat face localizer TFLite model. Its weights are CC BY-NC 4.0 (non-commercial) and not usable in PurrSense commercially. The package's Dart code is MIT. — [pub.dev cat_detection](https://pub.dev/packages/cat_detection)
- [unverified this session] YOLOX (Megvii) is Apache-2.0 and has Nano/Tiny variants with TFLite/ONNX/ncnn deployment paths. RT-DETR (lyuwenyu/RT-DETR) is Apache-2.0. MediaPipe Model Maker object detector (MobileNet-based, exports TFLite natively) is Apache-2.0. Zero-shot OWLv2 (google/owlv2-*) and Grounding DINO (IDEA-Research) are Apache-2.0, but they are too large for real-time phone use and are better used as offline auto-labelers for building a cat-face training set.

### Inferences
- **Recommended ranking for the cat-face detector:**
  1. D-FINE-N or D-FINE-S (Apache-2.0, 4–10M params) fine-tuned on cat faces.
  2. RF-DETR-Nano (Apache-2.0, 30.5M, heavier but stronger).
  3. YOLOX-Nano/Tiny or a MediaPipe/SSD-MobileNet detector, both Apache [verify], which have the most mature TFLite tooling.
  4. Ultralytics YOLO11n/YOLO26n, only with a purchased Enterprise license.
- Training D-FINE from COCO-only checkpoints, not Objects365, avoids the extra dataset-terms ambiguity.
- DETR-style models (D-FINE, RF-DETR) convert to ONNX. Getting from ONNX to TFLite/LiteRT (for example via onnx2tf or ai-edge-torch) and to Core ML (coremltools) is plausible, but transformer decoder ops can complicate conversion. Budget engineering time for this. A CNN detector (YOLOX, SSD-MobileNet) is the low-risk fallback.
- Because the app takes a single owner photo rather than a video stream, latency on the order of 50–100 ms is acceptable. This widens the set of usable detectors.

### Gaps
- No published phone latencies (Pixel or iPhone) were found for D-FINE or RF-DETR. All published figures are T4 TensorRT.
- YOLOX, RT-DETR, MediaPipe, OWLv2 and Grounding DINO licenses were not re-verified from primary sources this session.
- No benchmark for cat-face detection specifically was found for any of these models.

## Landmarks/pose: RTMPose/MMPose, ViTPose, SuperAnimal, open CatFLW weights

### Takeaway
No commercially licensable CatFLW-trained (48-landmark) weights exist. CatFLW itself is CC BY-NC 4.0, so PurrSense must either license CatFLW from its authors (Tech4Animals Lab, University of Haifa) or annotate its own dataset. The architectures are permissive: RTMPose/MMPose and ViTPose are Apache-2.0. The generic animal pose data (AP-10K, CC BY 4.0) has only 3 face keypoints, so it is useful for pretraining but not for FGS. DeepLabCut SuperAnimal weights are non-commercial.

### Cited Findings
- The `cat_detection` package's 48-point landmark model (11 MB, 384x384, TFLite) was trained on CatFLW by Martvel et al. The model files are CC BY-NC 4.0 because CatFLW is CC BY-NC 4.0, and commercial permission must come from the dataset authors. A higher-accuracy variant on HF is described as too slow for phones. — [pub.dev cat_detection](https://pub.dev/packages/cat_detection)
- A search found no official GitHub release of CatFLW or Ensemble Landmark Detector (ELD) weights from the original authors. The papers are [CatFLW arXiv 2305.04232](https://arxiv.org/pdf/2305.04232) and [Automated Detection of Cat Facial Landmarks, arXiv 2310.09793](https://arxiv.org/pdf/2310.09793).
- **RTMPose (MMPose)**: RTMPose-m at 256x256 reaches 0.722 AP on AP-10K val. — [mmpose RTMPose animal configs](https://github.com/open-mmlab/mmpose/tree/main/configs/animal_2d_keypoint/rtmpose). [unverified this session] MMPose and RTMPose are Apache-2.0, and RTMPose ships ONNX/ncnn deployment via MMDeploy, with reported mobile CPU latencies in the paper.
- **ViTPose / ViTPose++**: Apache-2.0, 2.2k stars. AP-10K test AP: S 71.4, B 74.5, L 80.4, H 82.4. APT-36K val AP: S 74.2, B 75.9. — [ViTAE-Transformer/ViTPose](https://github.com/ViTAE-Transformer/ViTPose)
- **AP-10K**: CC-BY-4.0, 10,015 images, 54 species, 17 keypoints, of which only 3 are on the face (left eye, right eye, nose). — [AlexTheBad/AP-10K](https://github.com/AlexTheBad/AP-10K)
- **DeepLabCut SuperAnimal-Quadruped**: "Modified MIT", limited to "academic, non-commercial purposes only", with commercial licensing via Prof. Mackenzie Mathis or the EPFL TTO. The architecture is HRNet-w32 with a Faster R-CNN detector. — [HF mwmathis/DeepLabCutModelZoo-SuperAnimal-Quadruped](https://huggingface.co/mwmathis/DeepLabCutModelZoo-SuperAnimal-Quadruped)
- RF-DETR Keypoint (Preview) is an Apache-2.0 keypoint head (40.7M params). It could be fine-tuned on custom cat-face landmarks. — [roboflow/rf-detr](https://github.com/roboflow/rf-detr)

### Inferences
- **Recommended landmark path:** an RTMPose-t/s top-down model (Apache-2.0, SimCC head, designed for mobile), optionally initialised from AP-10K-pretrained weights (CC BY 4.0, so attribution only), then fine-tuned on a 48-point cat-face set. The fine-tuning set is either CatFLW under a negotiated commercial license or PurrSense-owned annotations following the CatFLW scheme.
- ViTPose++-S is the accuracy alternative, though a ViT is heavier on phones. Avoid SuperAnimal and the `cat_detection` weights for production.
- Legal caveat: the license of a model trained on CC BY-NC data is legally unsettled. Treat CatFLW-trained weights as non-commercial unless the authors grant permission.

### Gaps
- Exact RTMPose param counts and phone latencies were not re-fetched. They are reported in the RTMPose paper (arXiv 2303.07399).
- I did not confirm whether the CatFLW authors offer commercial licensing.
- No open, commercially licensed cat-face landmark dataset other than CatFLW was found.

## Re-ID embeddings: DINOv2 vs DINOv3, SigLIP/SigLIP2, CLIP, MiewID, MegaDescriptor, MobileCLIP, CNN backbones

### Takeaway
The best commercially clean re-ID backbones are **DINOv2-S (Apache-2.0, 22M)** and **SigLIP/SigLIP2-B (Apache-2.0)**. In Kudryavtsev et al. (arXiv 2603.02270), both score about 85–87% Top-1 on a held-out cat ID set, close to SigLIP2-Giant (89%). DINOv3 is under a custom Meta license that permits commercial use but requires a "Built with DINOv3" notice and has military/ITAR restrictions. **MobileCLIP weights are research-only (Apple ML Research Model TOU)**, not commercial. MiewID's license is unstated, and MegaDescriptor is CC BY-NC.

### Cited Findings
- **Kudryavtsev et al., "From Visual to Multimodal: Systematic Ablation of Encoders and Fusion Strategies in Animal Identification" (arXiv 2603.02270, paper CC BY 4.0).** Training corpus: 1.9M photos of 695,091 animals from Pet911, Telegram, Dogs-World, LCW and PetFace. Best results: SigLIP2-Giant vision with E5-Small-v2 text, gated fusion, 84.28% Top-1, EER 0.0422. — [arXiv abs](https://arxiv.org/abs/2603.02270)
- Vision encoder ablation from the same paper (overall Top-1 / EER; Cat Individual Images, 518 cats, Top-1 / EER):

  | Backbone | Overall Top-1 / EER | Cat Individual Images Top-1 / EER |
  |---|---|---|
  | CLIP-ViT-B | 0.651 / 0.073 | 0.836 / 0.060 |
  | DINOv2-Small | 0.718 / 0.055 | 0.855 / 0.042 |
  | SigLIP-B | 0.736 / 0.057 | 0.865 / 0.039 |
  | SigLIP2-B | 0.740 / 0.063 | 0.866 / 0.039 |
  | Zer0int CLIP-L | 0.763 / 0.057 | 0.877 / 0.051 |
  | SigLIP2-Giant | 0.824 / 0.038 | 0.890 / 0.034 |

  DINOv2-Small is the smallest (~22M) and the fastest (224 img/s). DINOv3 was not in the ablation. — [arXiv HTML](https://arxiv.org/html/2603.02270)
- **DINOv2-small**: apache-2.0, 22.1M params, ~2.37M downloads/month. — [HF facebook/dinov2-small](https://huggingface.co/facebook/dinov2-small)
- **DINOv3**: "DINOv3 License" (Aug 14, 2025), gated on HF. ViT-S/16 has about 21.6M params and ~165k downloads/month. — [HF facebook/dinov3-vits16-pretrain-lvd1689m](https://huggingface.co/facebook/dinov3-vits16-pretrain-lvd1689m)
  - The license grants a non-exclusive, worldwide, royalty-free license to use, reproduce, distribute and modify, with no explicit commercial exclusion.
  - Distributed or user-facing products must prominently display "Built with DINOv3."
  - ITAR and military end uses are prohibited.
  - Redistribution must include the agreement.
  - The license terminates if you sue Meta over IP, and the licensee must indemnify Meta. — [Meta DINOv3 License](https://ai.meta.com/resources/models-and-libraries/dinov3-license/)
- **SigLIP2-base-patch16-224**: Apache-2.0, ~0.4B params total (image and text towers), ~1.23M downloads/month. — [HF google/siglip2-base-patch16-224](https://huggingface.co/google/siglip2-base-patch16-224)
- **MobileCLIP / MobileCLIP2 (Apple)**: code is MIT, model weights are under the Apple ML Research Model TOU, data is CC-BY-NC-ND. ~1.7k stars. — [apple/ml-mobileclip](https://github.com/apple/ml-mobileclip)
  - The TOU limits use "exclusively for Research Purposes", which excludes "any commercial exploitation, product development" and use "in any commercial product or service." — [LICENSE_MODELS](https://raw.githubusercontent.com/apple/ml-mobileclip/main/LICENSE_MODELS)
  - For reference only: MobileCLIP2-S0 image encoder is 11.4M params, 1.5 ms on iPhone, 71.5% IN-1k zero-shot. — [apple/ml-mobileclip](https://github.com/apple/ml-mobileclip)
- **MiewID-msv3 (Conservation X Labs)**: EfficientNetV2 backbone (`efficientnetv2_rw_m`), ~51M params, 440x440 input, contrastive training on 64 wildlife species, ~451k downloads/month. The HF card states no license. — [HF conservationxlabs/miewid-msv3](https://huggingface.co/conservationxlabs/miewid-msv3)
  - The GitHub repo has no LICENSE file, only "Copyright Conservation X Labs 2025." — [WildMeOrg/wbia-plugin-miew-id](https://github.com/WildMeOrg/wbia-plugin-miew-id)
  - Benchmark on wild felids: Amur tiger 88.9 mAP, 97.4 rank-1.
- MegaDescriptor (CC BY-NC) and PetFace weights (research-only) are already flagged as blockers in the brief and were not re-checked here.
- [unverified this session] OpenCLIP code is MIT, but weight licensing depends on the training data (LAION). timm MobileNetV3/MobileNetV4, EfficientNet and EfficientNetV2 ImageNet weights are generally Apache-2.0 and convert cleanly to TFLite and Core ML.

### Inferences
- **Recommended ranking for the cat-ID embedding backbone:**
  1. **DINOv2-S/14 (Apache-2.0, 22M)** fine-tuned with ArcFace on cat-face crops. It has the best size-to-accuracy ratio, a clean license and broad adoption. A ViT-S at 224 px is about 4.6 GFLOPs and runs on phone GPU/NPU in tens of milliseconds.
  2. **SigLIP2-B/16 vision tower (Apache-2.0, ~86M vision params)**. It was slightly better than DINOv2-S on cats (0.866 vs 0.855 Top-1) but is about 4x larger.
  3. **DINOv3-S/16** if the team accepts the "Built with DINOv3" attribution and the custom terms. It was not benchmarked on cat re-ID in the sources found.
  4. **MobileNetV4 / EfficientNet-Lite with ArcFace** (Apache, if verified) as the smallest and most conversion-friendly option.
- Avoid MobileCLIP (research-only weights), MiewID (no license, so default copyright applies; contact Conservation X Labs), MegaDescriptor and PetFace.
- Distilling a large Apache teacher (SigLIP2-Giant/So400m) into a MobileNetV4 or ViT-S student on PurrSense's own cat data is a commercially clean route to near-Giant accuracy on device.
- Registered-cat ID is closed-set with few cats per household, which is easier than the 518-cat open benchmark. Expected per-household accuracy is likely higher than these numbers, but this needs validation.

### Gaps
- No direct cat re-ID benchmark of DINOv3 or MobileNetV4-ArcFace was found.
- No published phone latencies for DINOv2-S or SigLIP2-B in LiteRT or Core ML were found in this session.
- I did not verify whether ready-made LiteRT or Core ML conversions of DINOv2 or SigLIP2 exist on HF.
- The WildlifeReID-10k and PetFace benchmark comparisons were not fetched in this session.

## Mobile feasibility summary (inferred, partly unverified)
- **Easy (CNNs):** YOLOX-Nano, SSD/MediaPipe, MobileNetV4, EfficientNet, RTMPose-t/s (CSPNeXt backbone).
- **Moderate (ViT-S scale):** DINOv2-S, DINOv3-S, ViTPose-S. These need fixed input size, and INT8/FP16 quantization is advisable.
- **Harder (DETR decoders):** D-FINE and RF-DETR. ONNX export is documented, but TFLite and Core ML paths are not.
