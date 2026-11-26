# Open-Weight On-Device VLMs for PurrSense (as of Nov 2026)

Scope: which open-weight vision-language models (VLMs) could run on Android and iOS phones as a narrator, photo-quality checker or tie-breaker next to (or instead of) Gemini Nano / Apple Foundation Models, and whether any open VLM fine-tuned on animals does better on cat tasks. Research covered about 15 tool calls. Primary sources were Google LiteRT-LM docs, Hugging Face model cards, Qwen/OpenBMB cards, the Apple FastVLM repo, and two arXiv preprints (Catellect-VL-2B, Bio-VQA), read as full text.

## Q1. Ranked shortlist: model, developer, size, license, benchmarks, runtimes, phone latency/memory

### Takeaway
**Gemma 4 E2B/E4B is the strongest pick for PurrSense.** It is Apache-2.0, ships through Google's own LiteRT-LM runtime on both Android and iOS, Google publishes phone latency and memory figures for it, and its family matches the Gemini/Firebase stack already in use. **The Qwen family is the best open alternative on benchmarks.** Qwen3.5-2B and Qwen3-VL-2B/4B are Apache-2.0 and score highest per parameter on MMMU and MMBench; they run through llama.cpp, Qualcomm AI Hub and MLX. Qwen3-VL-2B is also the base of the only published cat-behavior fine-tune. MiniCPM-V 4.6 (1.3B, Apache-2.0) and SmolVLM2 (256M/500M/2.2B, Apache-2.0) are the ultra-light options.

### Ranked shortlist (for the narrator / photo-quality / tie-breaker role)

| Rank | Model | Developer | Params | License (as stated on primary source) | Vision benchmarks found | Phone runtimes | Measured phone perf |
|---|---|---|---|---|---|---|---|
| 1 | Gemma 4 E2B / E4B | Google DeepMind | E2B 2.3B effective (5.1B with embeddings); E4B 4.5B effective (8B with embeddings) | Apache-2.0 | MMMU-Pro 44.2% (E2B) / 52.6% (E4B); MATH-Vision 52.4 / 59.5; OmniDocBench edit dist 0.290 / 0.181 | LiteRT-LM (Android, iOS, desktop, Pi) | E2B on S26 Ultra GPU: 52 tok/s decode, 0.3 s TTFT, 676 MB peak CPU memory; on iPhone 17 Pro GPU: 56 tok/s, 0.3 s TTFT. E4B: 22–25 tok/s on GPU, 0.8–0.9 s TTFT |
| 2 | Qwen3.5-2B (and 0.8B) | Alibaba Qwen | 2B (0.8B) | Apache-2.0 | MMMU 64.2; MMBench 81.3–83.3; OCRBench 84.5–85.4; MathVista 73.9–76.7 (0.8B: MMMU 47.4–49, MMBench 68–69.9) | llama.cpp / GGUF and others (community) | No vendor phone numbers found; one community review reports generation loops on the 2B |
| 3 | Qwen3-VL-2B / 4B | Alibaba Qwen | 2B / 4B | Apache-2.0 per the Qwen3.5 card's comparison context (the Qwen3-VL card itself was not opened, see Gaps) | MMMU 61.4 / 70.8; MMBench 81.9 / 86.7; OCRBench 79.2 / 80.8 | Qualcomm AI Hub (q4_0), llama.cpp on Android (Arm guide), MLX community conversions | Qualcomm q4_0 on Snapdragon 8 Elite: about 41–43 tok/s (2B, short context) |
| 4 | MiniCPM-V 4.6 | OpenBMB | 1.3B (SigLIP2-400M + Qwen3.5-0.8B) | Apache-2.0 for weights and code | No numeric scores in card text; claims "Qwen3.5 2B-level" on OpenCompass, OCRBench and HallusionBench; AA Intelligence Index 13 | iOS, Android and HarmonyOS demo code; llama.cpp GGUF (about 2 GB CPU); Ollama | TTFT and throughput published only as chart images; claims about 1.5x the throughput of Qwen3.5-0.8B |
| 5 | SmolVLM2 256M / 500M / 2.2B | Hugging Face | 0.256B / 0.5B / 2.2B | Apache-2.0 | (not retrieved this pass) | MLX (Python + Swift) from day 0; HF iPhone demo app; LiteRT community build of 2.2B; community ExecuTorch build of 500M | Not found; HF notes 8-bit 500M "works great on iPhone" with an unquantized vision tower |
| 6 | FastVLM 0.5B / 1.5B / 7B | Apple | 0.5B / 1.5B / 7B | Code and weights licensed separately; weights under the `apple-amlr` / "apple-ascl" model license (text not verified) | Paper: beats LLaVA-OneVision-0.5B on SeedBench, MMMU and DocVQA with 85x faster TTFT | MLX iOS/macOS demo app (iOS 18.2+), checkpoints in FP16/INT8/INT4 | No published per-iPhone TTFT |
| 7 | Moondream 3 (preview) / 3.1 | M87 Labs | not verified this pass | Preview: BSL 1.1 with Additional Use Grant (commercial OK except competing hosted API; converts to Apache-2.0 after 2 years). 3.1: "Moondream Model License" (permits commercial products and SaaS) | not retrieved | not verified | not found |
| — | Gemma 3n E2B/E4B, Gemma 3 4B, PaliGemma 2 | Google | — | Gemma Terms of Use (not re-verified this pass) | — | MediaPipe LLM Inference / LiteRT-LM | Gemma 4 E-series supersedes them |
| — | Llama 3.2 Vision 11B, Phi-4-multimodal, InternVL3 small, LLaVA-OneVision | Meta, Microsoft, Shanghai AI Lab, LLaVA team | — | not re-verified | Bio-VQA ranks Llama-3.2-Vision and Phi-3.5-Vision near the bottom (see Q3) | — | 11B is too large for phones in practice |

### Cited Findings
- Google lists Gemma 4 E2B and E4B as the "mobile and edge" sizes and 26B A4B and 31B as GPU/workstation sizes. Gemma 4 is Apache-2.0. LiteRT-LM currently supports only E2B and E4B. — [LiteRT-LM Gemma 4 page](https://developers.google.com/edge/litert-lm/models/gemma-4)
- Gemma 4 file sizes are 2.58 GB (E2B) and 3.65 GB (E4B). LiteRT-LM supports Android, iOS, Linux, macOS, Windows and Raspberry Pi 5. — [LiteRT-LM Gemma 4 page](https://developers.google.com/edge/litert-lm/models/gemma-4)
- Gemma 4 E2B benchmarks:
  - S26 Ultra GPU: prefill 3808 tok/s, decode 52 tok/s, TTFT 0.3 s, peak CPU memory 676 MB.
  - S26 Ultra CPU: decode 47 tok/s, TTFT 1.8 s, 1733 MB.
  - iPhone 17 Pro GPU: prefill 2878, decode 56 tok/s, TTFT 0.3 s, 1450 MB.
  - iPhone 17 Pro CPU: decode 25 tok/s, TTFT 1.9 s, 607 MB.
  - [LiteRT-LM Gemma 4 page](https://developers.google.com/edge/litert-lm/models/gemma-4)
- Gemma 4 E4B benchmarks:
  - S26 Ultra GPU: decode 22 tok/s, TTFT 0.8 s, 710 MB.
  - iPhone 17 Pro GPU: decode 25 tok/s, TTFT 0.9 s, 3380 MB.
  - CPU paths are much slower: 10–18 tok/s with 5–6.5 s TTFT.
  - Multi-Token Prediction gives up to 2.2x decode speedup on mobile GPU and 1.5x on mobile CPU.
  - [LiteRT-LM Gemma 4 page](https://developers.google.com/edge/litert-lm/models/gemma-4)
- LiteRT-LM launched May 19, 2026, with on-device Gemma 4 vision and audio. A Gemma 4 12B dense model (June 3, 2026) targets desktops, not phones. — [Google Developers Blog: LiteRT-LM](https://developers.googleblog.com/blazing-fast-on-device-genai-with-litert-lm/); [Gemma 4 12B guide](https://developers.googleblog.com/en/gemma-4-12b-the-developer-guide/)
- Gemma 4 E2B model card details:
  - License apache-2.0.
  - E2B: 2.3B effective / 5.1B with embeddings. E4B: 4.5B effective / 8B with embeddings.
  - Text, image and audio input; 128K context.
  - MMMU-Pro 44.2% (E2B), 52.6% (E4B) vs 49.7% for Gemma 3 27B.
  - Tech report arXiv 2607.02770 (July 2026).
  - [HF google/gemma-4-E2B-it](https://huggingface.co/google/gemma-4-E2B-it)
- Qwen3.5-2B model card:
  - License Apache-2.0, released Feb 2026, early-fusion multimodal.
  - Benchmark table (Thinking/Non-thinking for Qwen3.5):

    | | MMMU | MMBench | OCRBench | MathVista |
    |---|---|---|---|---|
    | Qwen3.5-2B | 64.2 | 83.3 / 81.3 | 84.5 / 85.4 | 76.7 / 73.9 |
    | Qwen3.5-0.8B | 49 / 47.4 | 69.9 / 68.0 | 74.5 / 79.1 | 62.2 / 58.6 |
    | Qwen3-VL-2B | 61.4 | 81.9 | 79.2 | 73.6 |
    | Qwen3-VL-4B | 70.8 | 86.7 | 80.8 | 79.5 |

  - [HF Qwen/Qwen3.5-2B](https://huggingface.co/Qwen/Qwen3.5-2B)
- Qualcomm publishes Qwen3-VL-2B/4B for Snapdragon. The q4_0 2B runs at about 41–43 tok/s on Snapdragon 8 Elite at short context. — [HF qualcomm/Qwen3-VL-2B-Instruct](https://huggingface.co/qualcomm/Qwen3-VL-2B-Instruct); [Qualcomm AI Hub Qwen3-VL-4B](https://aihub.qualcomm.com/models/qwen3_vl_4b_instruct)
- Arm publishes an Android tutorial that runs Qwen3-VL-2B via llama.cpp, with a Q4_K_M language model and a Q8_0 vision encoder. — [Arm Learning Path](https://learn.arm.com/learning-paths/mobile-graphics-and-gaming/ai-portal-mobile-vision-language/3-run-model/)
- A community review reports that Qwen3.5-2B handles vision and OCR well but sometimes gets stuck in generation loops. This is anecdotal. — [Better Stack guide](https://betterstack.com/community/guides/ai/qwen-35/)
- MiniCPM-V 4.6 model card:
  - Weights and code "open-sourced under the Apache-2.0 license"; no separate registration is mentioned.
  - 1.3B params; iOS, Android and HarmonyOS edge code; llama.cpp GGUF.
  - AA Intelligence Index 13 vs 10 for Qwen3.5-0.8B.
  - Benchmark numbers appear only as images.
  - [HF openbmb/MiniCPM-V-4.6](https://huggingface.co/openbmb/MiniCPM-V-4.6)
- MiniCPM-V 4.6 was released May 11, 2026 (SigLIP2-400M + Qwen3.5-0.8B; GGUF about 2 GB on CPU; demos on iPhone 17 Pro Max, Redmi K70 and HUAWEI nova 14). This is a secondary, partly AI-drafted article. — [NYU Shanghai RITS article](https://rits.shanghai.nyu.edu/ai/minicpm-v-4-6-a-1-3b-multimodal-model-built-for-phones/)
- SmolVLM2 comes in 2.2B, 500M and 256M sizes and was MLX-ready (Python and Swift) at launch. HF built a fully local iPhone app on the 500M model. — [HF blog SmolVLM2](https://huggingface.co/blog/smolvlm2)
- SmolVLM-500M is Apache 2.0. — [HF SmolVLM-500M-Instruct](https://huggingface.co/HuggingFaceTB/SmolVLM-500M-Instruct)
- A LiteRT community build of SmolVLM2-2.2B exists. — [HF litert-community/SmolVLM2-2.2B](https://huggingface.co/litert-community/SmolVLM2-2.2B)
- A community ExecuTorch build of SmolVLM2-500M exists. — [HF mlboydaisuke/SmolVLM2-500M-ExecuTorch](https://huggingface.co/mlboydaisuke/SmolVLM2-500M-ExecuTorch)
- FastVLM keeps the model license (LICENSE_MODEL) separate from the code license. It ships an MLX iOS/macOS demo (iOS 18.2+) with 0.5B FP16, 1.5B INT8 and 7B INT4 checkpoints. — [GitHub apple/ml-fastvlm](https://github.com/apple/ml-fastvlm)
- Per Apple, FastVLM matches or beats LLaVA-OneVision on SeedBench, MMMU and DocVQA with the same 0.5B LLM, with 85x faster TTFT and a 3.4x smaller vision encoder. — [Apple ML Research](https://machinelearning.apple.com/research/fast-vision-language-models); [arXiv 2412.13303](https://arxiv.org/html/2412.13303v2)
- Moondream 3 Preview weights use BSL 1.1 with an Additional Use Grant. Self-hosting and most commercial use are allowed; commercial hosting or rehosting needs an agreement. — [HF moondream3-preview](https://huggingface.co/moondream/moondream3-preview); [Moondream license](https://moondream.ai/licenses/model/1.0)
- Moondream 3.1 moved to a "Moondream Model License" that permits commercial products and SaaS. — [Moondream 3.1 blog](https://moondream.ai/blog/moondream-3-1-beyond-benchmarks)

### Inferences
- **Benchmarks are not directly comparable across families.** Gemma 4 reports MMMU-Pro, while Qwen reports MMMU (standard), so Gemma 4 E2B's 44.2 MMMU-Pro cannot be set against Qwen3.5-2B's 64.2 MMMU. Use the OpenCompass leaderboard, or an in-house cat-photo evaluation, for a fair comparison.
- **Gemma 4 E2B on GPU fits the narrator role.** About 0.3 s TTFT and 50+ tok/s on 2026 flagships means a 60-token explanation renders in roughly 1.5 s. E4B on CPU (5–6 s TTFT) is too slow for mid-range phones.
- **No phone-latency data was found for older or mid-tier devices.** All Gemma 4 numbers are from S26 Ultra and iPhone 17 Pro. PurrSense's user base will include much older hardware.
- **The cleanest licenses for a commercial app** are Apache-2.0 (Gemma 4, Qwen3.5/Qwen3-VL small sizes, MiniCPM-V 4.6, SmolVLM2). Apple's FastVLM model license and Moondream's BSL need legal review.
- **Gemma 4 being Apache-2.0 is a notable change** from the Gemma Terms of Use that governed Gemma 3, 3n and PaliGemma 2. It removes use-policy pass-through obligations.

### Gaps
- Qwen3-VL-2B/4B license not confirmed from its own model card; the Apache-2.0 attribution is inferred (Qwen3.5 card and Qualcomm listing were read).
- Gemma 3n, PaliGemma 2, Phi-4-multimodal, InternVL3/3.5 small, LLaVA-OneVision and Llama 3.2 Vision licenses were not re-verified this session.
  - From prior knowledge only (verify before citing): Gemma 3n, Gemma 3 and PaliGemma 2 use the Gemma Terms of Use; Llama 3.2 uses the Llama 3.2 Community License, which historically restricted EU use of multimodal models; Phi-4-multimodal is MIT.
- No Apple FastVLM license text was read; commercial terms unknown.
- Moondream 3 parameter count and phone runtime support not verified.
- No OpenCompass Open VLM Leaderboard snapshot was retrieved, so there is no common-benchmark ranking.
- Whether Firebase AI Logic can host a non-Gemini-Nano local model was not investigated. It likely cannot, so a LiteRT-LM or llama.cpp path would sit beside Firebase rather than inside it.

## Q2. Which run on Android and iOS today, at what size and quantization, with what latency

### Takeaway
Three models have documented cross-platform phone paths today:
- **Gemma 4 E2B/E4B via LiteRT-LM**: official, with benchmarks.
- **Qwen3-VL-2B via llama.cpp / Qualcomm AI Hub on Android**, and MLX on iOS.
- **MiniCPM-V 4.6**: official iOS and Android demos.

SmolVLM2 and FastVLM are strongest on iOS (MLX); SmolVLM2 also has a community LiteRT build (2.2B) and an ExecuTorch build (500M). Gemma 4 E2B is the only one with first-party, per-device latency and memory figures for both an Android and an iOS flagship.

### Cited Findings
- **Gemma 4 E2B** is a 2.58 GB .litertlm file. It reaches 52–56 tok/s decode and 0.3 s TTFT on S26 Ultra and iPhone 17 Pro GPU. Custom fine-tuned safetensors can be converted to .litertlm with `litert-torch-nightly`, which gives a route to ship a cat-tuned model. — [LiteRT-LM Gemma 4](https://developers.google.com/edge/litert-lm/models/gemma-4)
- **Qwen3-VL-2B** runs as q4_0 on Snapdragon 8 Elite at about 41–43 tok/s. — [HF qualcomm/Qwen3-VL-2B-Instruct](https://huggingface.co/qualcomm/Qwen3-VL-2B-Instruct)
- On Android it also runs via llama.cpp (Q4_K_M LLM + Q8_0 vision encoder). — [Arm](https://learn.arm.com/learning-paths/mobile-graphics-and-gaming/ai-portal-mobile-vision-language/3-run-model/)
- MLX community conversions of Qwen3-VL exist. — [HF mlx-community Qwen3-VL](https://huggingface.co/collections/mlx-community/qwen3-vl)
- **MiniCPM-V 4.6** has open-sourced edge code for iOS, Android and HarmonyOS, and a llama.cpp GGUF. — [HF openbmb/MiniCPM-V-4.6](https://huggingface.co/openbmb/MiniCPM-V-4.6)
- **SmolVLM2-500M** runs at 8-bit on iPhone with the vision tower unquantized. — [HF SmolVLM2 collection](https://huggingface.co/collections/HuggingFaceTB/smolvlm2-smallest-video-lm-ever); [HF blog](https://huggingface.co/blog/smolvlm2)
- **FastVLM** ships an iOS app via MLX. — [GitHub apple/ml-fastvlm](https://github.com/apple/ml-fastvlm)

### Inferences
- **For a shared Android + iOS codebase, LiteRT-LM + Gemma 4 E2B is the lowest-friction option.** It is one runtime and one model file on both platforms, and the vendor (Google) is the same as for the existing Gemini stack.
- **llama.cpp is the most portable fallback** for Qwen or MiniCPM models.

### Gaps
- No measured iPhone latency for Qwen3-VL, SmolVLM2, FastVLM or MiniCPM-V 4.6 was found in text form.
- Vision-encoder (image prefill) time per photo is not separated out in Google's tables. TTFT figures appear to be text-prompt benchmarks, so image-input latency is unknown.
- MLC LLM and ExecuTorch first-party support for these specific VLMs was not checked.

## Q3. Evidence on animal/pet tasks (species ID, cat behavior/emotion, veterinary)

### Takeaway
**Catellect-VL-2B shows a cat-domain fine-tune of a phone-sized open VLM can beat much larger general models.** It is a fine-tune of Qwen3-VL-2B (arXiv 2608.22070, preprint) that scores 83.63% macro accuracy on observable cat-behavior fields (ears, tail, face, posture), vs 53.72% for the untuned base, 75.08% for Qwen3.5-VL-9B, and about 80–81% for Gemini 3 Pro, GPT-5.4 and Claude Opus. The evidence has caveats:
- Small, homogeneous data: 6 household cats.
- LLM-distilled labels.
- The authors' own benchmark.
- 32 s per image on a Rockchip board, not a phone.
- It does not score pain.

**Bio-VQA (arXiv 2611.05559, preprint) confirms general open VLMs (3.75B–38B) transfer poorly to fine-grained biological tasks.**

### Cited Findings
- **Catellect-VL-2B basics.** Post-trained from Qwen3-VL-2B on "CatellectBench," a 40K-sample image–behavior dataset (about 38K training instances, 2K held-out test). It outputs structured JSON behavior fields. Authors: YuHang Wu, Jiexian Liu, JunYi Wang, Jia Tao (Catellect Research, USA). arXiv 2608.22070v3, "Manuscript submitted to ACM." — [arXiv 2608.22070](https://arxiv.org/pdf/2608.22070)
- **Catellect held-out results (Table 7), average accuracy:**

  | Model | Avg | Notes |
  |---|---|---|
  | Catellect-VL-2B Phase 2 | 83.63 | Ears 92.03, Face 89.64, Tail 69.72, Body 66.53, Action 77.65 |
  | Phase 3 compact variant | 80.62 | |
  | GPT-5.4 | 81.16 | |
  | Gemini3-Pro | 80.84 | |
  | Claude-Opus-4.8 | 80.40 | |
  | Qwen3.5-VL-9B | 75.08 | |
  | MiniCPM-V 4.5 (8B) | 63.46 | |
  | Qwen3-VL-2B (base) | 53.72 | |
  | Llama-3-LLaVA-NeXT-8B | 36.84 | |

  Source: [arXiv 2608.22070](https://arxiv.org/pdf/2608.22070)
- **Catellect data limitations.** The raw pool is more than 100K self-collected images and frames of **six household cats**. A licensed veterinarian designed the schema and annotated a 416-image gold seed set. Training labels were then generated by a teacher LLM (Doubao-Seed-2.0-Pro, 89.18% agreement with the vet seed set; GPT-5.4 82.78%; Gemini3-Pro-Preview 75.19%). — [arXiv 2608.22070](https://arxiv.org/pdf/2608.22070)
- **Catellect emotion handling.** Emotion and intent are not predicted directly. A separate rule-based knowledge base maps structured fields to emotion/intent concepts with a risk level. The paper cites the Feline Grimace Scale as motivation for facial cues, but the model does not output FGS scores. It defers severe or persistent symptoms to vets. — [arXiv 2608.22070](https://arxiv.org/pdf/2608.22070)
- **Catellect deployment.** The model runs on a Rockchip RK3576 with 8 GB memory. Compact serialization cut per-image latency from 81.2 s to 32.3 s on that board; on an A100 it takes 1.78 s per image. The paper is internally inconsistent on the speedup: the abstract says 2.51x, the intro says 2.98x. — [arXiv 2608.22070](https://arxiv.org/pdf/2608.22070)
- **Scalpel-VL-1.6B (freeai-org, Hugging Face)** is a Qwen3-VL-architecture cat posture/emotion model that cites the same arXiv paper (2608.22070).
  - Reports a posture score of 80.04 vs 69.62 for Qwen3.5-9B.
  - License metadata conflicts: header says MIT, body says Apache 2.0.
  - Dataset not released; 27 downloads/month.
  - A sibling Scalpel-VL-1.8B also exists.
  - [HF freeai-org/Scalpel-VL-1.6B](https://huggingface.co/freeai-org/Scalpel-VL-1.6B); [HF Scalpel-VL-1.8B](https://huggingface.co/freeai-org/Scalpel-VL-1.8B)
- A "JoyFox-PawScope-VL" pet VLM appears on Hugging Face, but the page returned 401 and could not be read. — [HF joyfox/JoyFox-PawScope-VL](https://huggingface.co/joyfox/JoyFox-PawScope-VL)
- **Bio-VQA basics.** S M Rayeed, Qian Ma, Charles V. Stewart, Yao Ma (RPI), arXiv 2611.05559v1, Nov 3, 2026, marked © 2027 IEEE. It has 1.86M questions over 217K images from 11 public datasets and evaluates 18 open-weight VLMs from 3.75B to 38.4B parameters. — [arXiv 2611.05559](https://arxiv.org/pdf/2611.05559)
- **Bio-VQA findings:**
  - The best model counts animals exactly on only 41.7% of images with 2–10 individuals.
  - Taxonomic accuracy falls from 0.72 at phylum to 0.12 at scientific name.
  - 15 of 18 models do no better than an image-blind baseline on trait-size measurement.
  - 17 models fall below chance on adversarial re-identification.
  - The specialist BioCLIP-2 beats the VLMs: 0.874 vs 0.731 at class, 0.544 vs 0.269 at scientific name.
  - [arXiv 2611.05559](https://arxiv.org/pdf/2611.05559)
- **Bio-VQA overall ranking:**
  1. Qwen3-VL-32B
  2. Qwen3-VL-30B-A3B
  3. InternVL3-38B
  4. Gemma-4-12B
  5. Qwen3-VL-8B
  - Lower down: Qwen2.5-VL-3B (the smallest model, 3.75B) ranks 13th; Phi-3.5-Vision 16th; Llama-3.2-Vision 17th; LLaVA-NeXT-Mistral last.
  - Phone-class models (Gemma 4 E2B/E4B, Qwen3.5-2B, SmolVLM, MiniCPM-V 4.6) were **not** evaluated.
  - [arXiv 2611.05559](https://arxiv.org/pdf/2611.05559)

### Inferences
- **Off-the-shelf small VLMs are weak at fine-grained cat cues.** Untuned Qwen3-VL-2B scores 25.68% on face and 29.57% on tail. This supports keeping the LLM in a narrator / photo-quality role rather than scoring pain, consistent with Ngai et al. 2025 and Martvel et al. 2025.
- **Catellect suggests a supervised fine-tune of a 2B open VLM on structured, vet-designed observable fields could make the VLM useful for feature extraction.** Examples are ear position, eye squint and whisker change. Those FGS action units could then feed a dedicated scorer. This is unproven for pain and on phones.
- **Catellect's test set comes from the same 6 cats as training,** so its generalization to arbitrary cats and breeds is unknown. Treat the 83.6% figure as an upper bound.
- **The base model choice matters for a PurrSense fine-tune.** Qwen3-VL-2B and Qwen3.5-2B are Apache-2.0 with published phone paths. Gemma 4 E2B is Apache-2.0 and convertible to LiteRT-LM, which makes it the most natural fine-tune target for a Firebase/Google-aligned app.
- **For species ID or breed, specialist embedding models outperform VLMs** (Bio-VQA vs BioCLIP-2). A small CLIP-style classifier is preferable to a VLM for "is this a cat / is the face visible" gating.

### Gaps
- Neither Catellect weights nor CatellectBench data are confirmed public. Scalpel-VL may be the released model but this is unconfirmed, and its license is contradictory.
- No study found that evaluates any open VLM on the Feline Grimace Scale or veterinary pain images.
- BioTrove and BioCLIP model cards (licenses, sizes) were not fetched this session.
- No evidence on Gemma 4 E-series or Qwen3.5 small models for animal emotion.

## Q4. Licensing for commercial use in an app

### Takeaway
**Apache-2.0 models carry the lowest risk for commercial app redistribution:** Gemma 4 (including E2B/E4B), Qwen3.5-2B/0.8B, MiniCPM-V 4.6 and SmolVLM2. The others need care:
- Moondream 3 Preview (BSL 1.1) and Moondream 3.1 (custom license) restrict hosted-API use.
- Apple FastVLM weights are under a separate Apple model license whose terms were not verified.
- Older Gemma models (3, 3n, PaliGemma 2) use the Gemma Terms of Use.
- Cat-specific community fine-tunes have unclear or contradictory licenses.

### Cited Findings
- Gemma 4: Apache-2.0. — [LiteRT-LM Gemma 4](https://developers.google.com/edge/litert-lm/models/gemma-4); [HF gemma-4-E2B-it](https://huggingface.co/google/gemma-4-E2B-it)
- Qwen3.5-2B: Apache-2.0. — [HF Qwen/Qwen3.5-2B](https://huggingface.co/Qwen/Qwen3.5-2B)
- MiniCPM-V 4.6: "MiniCPM-o/V model weights and code are open-sourced under the Apache-2.0 license." — [HF openbmb/MiniCPM-V-4.6](https://huggingface.co/openbmb/MiniCPM-V-4.6)
- SmolVLM-500M: Apache 2.0. — [HF SmolVLM-500M-Instruct](https://huggingface.co/HuggingFaceTB/SmolVLM-500M-Instruct)
- FastVLM: separate LICENSE_MODEL for weights; HF mirrors tag it `apple-amlr`. — [GitHub apple/ml-fastvlm](https://github.com/apple/ml-fastvlm); [HF mirror README](https://huggingface.co/solnoman/FastVLM05B/blob/main/README.md)
- Moondream 3 Preview: BSL 1.1 + Additional Use Grant; commercial hosting requires an agreement. — [HF moondream3-preview](https://huggingface.co/moondream/moondream3-preview)
- Moondream 3.1: Moondream Model License, which permits commercial products. — [Moondream blog](https://moondream.ai/blog/moondream-3-1-beyond-benchmarks)
- Scalpel-VL-1.6B: license metadata "mit" vs text "Apache 2.0," which conflict. — [HF freeai-org/Scalpel-VL-1.6B](https://huggingface.co/freeai-org/Scalpel-VL-1.6B)

### Inferences
- **If PurrSense fine-tunes its own cat model, Apache-2.0 bases avoid downstream obligations** beyond attribution and NOTICE files: Gemma 4 E2B, Qwen3.5-2B or Qwen3-VL-2B.
- **An on-device model shipped inside the app binary is redistribution,** so license text (e.g., NOTICE) must be bundled.

### Gaps
- Exact text of Apple's model license, the Gemma Terms (for 3n and PaliGemma 2), the Llama 3.2 license and Phi-4-multimodal was not read this session; verify before any decision involving those models.
- The Qwen3-VL-2B/4B license was not read from its own card.
