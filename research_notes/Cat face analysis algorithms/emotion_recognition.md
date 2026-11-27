# Automated Recognition of Cat Emotional State / Mood from Facial Images (and Body Posture)

Scope note: searched Nov 2026 with about 15 search/fetch calls. Several full texts were paywalled (Springer, Nature redirects), so some details come from abstracts, PubMed/PMC records or secondary summaries. These are flagged. Confidence ratings: HIGH = peer-reviewed, adequate N, objective ground truth, some replication. MEDIUM = peer-reviewed but small N, single lab or no replication. LOW = preprint, unclear labels, or unverified.

## Q1. Which peer-reviewed papers classify cat emotions/affective states from images or video?

### Takeaway
No peer-reviewed, validated automated classifier exists for multi-class cat "moods" such as PurrSense's eight labels. The scientifically strongest automated work on cat faces is binary and context-grounded, all from one lab (Haifa: Zamansky, Martvel, Feighelstein, Shimshoni, with Mills, Luna, Finka and Florkiewicz):
- pain vs no pain, about 66–73% accuracy
- affiliative vs non-affiliative social interaction, about 68–77% accuracy

The underlying ethology identifies only a few CatFACS-based clusters: relaxed engagement, fear and frustration (Bennett 2017), plus a validated pain grimace scale (FGS). Multi-class "cat emotion" CNNs that report high accuracy use web images with subjective labels and have low scientific credibility.

### Cited Findings
**Foundations (anatomical coding and ethology)**
- **Caeiro, Burrows & Waller (2017)**, "Development and application of CatFACS: Are human cat adopters influenced by cat facial expressions?", *Applied Animal Behaviour Science* 189:66–78, DOI 10.1016/j.applanim.2017.01.005.
  - Defines 15 Action Units, 6 Action Descriptors and 7 Ear Action Descriptors for the domestic cat.
  - Applied to 106 shelter cats: facial movements did not predict rehoming speed, but rubbing did.
  - The CatFACS manual is freely available.
  - Confidence: HIGH as a coding standard. It is not an emotion classifier.
  - Sources: [NTU repository](https://irep.ntu.ac.uk/id/eprint/41463); [AnimalFACS CatFACS](https://animalfacs.github.io/CatFACS)
- **Bennett, Gourkow & Mills (2017)**, "Facial correlates of emotional behaviour in the domestic cat (Felis catus)", *Behavioural Processes* 141(Pt 3):342–350, PMID 28341145.
  - Data: 29 shelter cats, 275 four-second clips, coded with CatFACS, with and without human interaction.
  - Hierarchical clustering grouped about half the data into three clusters, interpreted as **relaxed engagement, fear and frustration**.
  - Fear: blinking, half-blinking, and a left head/gaze bias.
  - Frustration: hissing, nose-licking, jaw drop, upper-lip raise, nose wrinkle and vocalisation.
  - Relaxed engagement: a right gaze/head-turn bias.
  - Reports convergent validity with Leyhausen (1979) offensive/defensive moods and with Gourkow et al. (2014).
  - Confidence: MEDIUM. N=29, single shelter, exploratory clustering, and about half the data unclustered.
  - Source: [PubMed](https://pubmed.ncbi.nlm.nih.gov/28341145/)
- **Scott & Florkiewicz (2023)**, "Feline faces: Unraveling the social function of domestic cat facial signals", *Behavioural Processes*, DOI 10.1016/j.beproc.2023.104959.
  - CatFACS coding of cat–cat interactions at a Los Angeles cat café identified **276 distinct facial signals**.
  - About 45% were friendly (affiliative), 37% aggressive (non-affiliative) and 18% ambiguous.
  - Breathing/yawning movements were excluded.
  - The authors did not decode what each expression means.
  - Confidence: MEDIUM. Single site, observational, valence only.
  - Sources: [gwern PDF](https://gwern.net/doc/cat/psychology/2023-scott.pdf); [Phys.org](https://phys.org/news/2023-10-housecats-hundreds-facial-interact-cats.amp)
  - A later paper describes the sample as 53 domestic shorthair cats, and says about 83% of signals were exclusive to either affiliative or non-affiliative interactions — [PMC11554805](https://pmc.ncbi.nlm.nih.gov/articles/PMC11554805)
  - Media reports conflict on recording volume: 194 minutes (Phys.org) vs "more than 100 hours" (Washington Post/Star Tribune) — [Star Tribune](https://www.startribune.com/cats-arent-as-aloof-as-you-might-think/600320516)
- **Evangelista et al. (2019)**, "Facial expressions of pain in cats: the development and validation of a Feline Grimace Scale", *Scientific Reports* 9:19128.
  - Data: 35 painful cats and 20 controls.
  - Five action units: ears, orbital tightening, muzzle tension, whiskers and head position.
  - Validity: correlates with a validated pain instrument (rho=0.86) and responds to analgesia. Cut-off is >0.39 out of 1.0.
  - Reliability: inter-rater ICC=0.89, intra-rater ICC>0.91.
  - Follow-up (Evangelista & Steagall 2021, Sci Rep): reliable across owners, vets and students, but muzzle and whiskers were less reliable (ICC 0.39–0.74).
  - Confidence: HIGH. This is the most validated cat facial affect instrument.
  - Sources: [PMC6911058](https://pmc.ncbi.nlm.nih.gov/articles/PMC6911058); [PMC7933168](https://pmc.ncbi.nlm.nih.gov/articles/PMC7933168)

**Automated classifiers (peer-reviewed)**
- **Feighelstein et al. (2022)**, "Automated recognition of pain in cats", *Scientific Reports* (June 2022).
  - Data: 29 female domestic shorthair cats photographed at time points around ovariohysterectomy.
  - Compared a ResNet50 CNN with machine learning on 48 CatFACS-informed geometric landmarks (Finka et al. 2019).
  - Both reached **>72% accuracy**.
  - The landmark approach is more interpretable.
  - Confidence: MEDIUM. Small N, single surgical context, female cats only.
  - Source: [PMC9187730](https://pmc.ncbi.nlm.nih.gov/articles/PMC9187730)
- **Martvel, Lazebnik, Feighelstein, … Mills, Volk, Zamansky (2024)**, "Automated landmark-based cat facial analysis and its applications", *Frontiers in Veterinary Science* 11, DOI 10.3389/fvets.2024.1442634.
  - Data: 464 images from 26 cats.
  - Pain (binary): **0.73 accuracy with manual landmarks vs 0.66 with automatic landmarks**.
  - Breed (18 classes): 0.70 manual vs 0.48 automatic. Cephalic type: 0.82 manual vs 0.79 automatic.
  - The paper contains internal inconsistencies (n=26 vs n=27; the abstract's 75% matches automatic F1, not accuracy).
  - Confidence: MEDIUM.
  - Source: [Frontiers](https://www.frontiersin.org/journals/veterinary-science/articles/10.3389/fvets.2024.1442634/full)
- **"Automated video-based pain recognition in cats using facial landmarks"** (*Scientific Reports*, Nov 2024, co-authored by Feighelstein).
  - End-to-end video pipeline with no manual frame selection.
  - Accuracy: **>70% and 66%** on two cat pain datasets.
  - Concludes that temporal dynamics matter.
  - Confidence: MEDIUM.
  - Source: [Nature](https://www.nature.com/articles/s41598-024-78406-2)
- **Martvel, Scott, Florkiewicz, Zamansky, Shimshoni & Lazebnik (2024)**, "Computational investigation of the social function of domestic cat facial signals", *Scientific Reports* 14:27533, DOI 10.1038/s41598-024-79216-2.
  - Affiliative vs non-affiliative classifier: **>77% accuracy from manual CatFACS codes**, **about 68% from automatically detected landmarks**.
  - Also found faster facial mimicry in affiliative contexts, especially in ear movements (EAD103, EAD104).
  - Confidence: MEDIUM. Single cat café, binary task.
  - Sources: [PMC11554805](https://pmc.ncbi.nlm.nih.gov/articles/PMC11554805); [DOAJ](https://doaj.org/article/f273d48016044e2f84ff198c29525f42)
- **Martvel et al.**, CatFLW dataset (arXiv 2305.04232; expanded as "Automated Detection of Cat Facial Landmarks", *International Journal of Computer Vision* 2024, DOI 10.1007/s11263-024-02006-w).
  - About 2,016–2,091 cat face images with 48 landmarks tied to CatFACS.
  - Includes a landmark detector.
  - This is infrastructure for downstream emotion models, not an emotion classifier itself.
  - Sources: [arXiv](https://arxiv.org/pdf/2305.04232); [IJCV](https://link.springer.com/article/10.1007/s11263-024-02006-w)

**Multi-class "cat emotion" classifiers (weaker evidence)**
- **"Advancing cat facial emotion interpretation: evaluating supervised and semi-supervised models using facial landmark and image data"** (Springer, 2026, DOI 10.1007/s11042-026-21301-x; the DOI prefix suggests *Multimedia Tools and Applications*).
  - Added emotion labels to CatFLW.
  - Best supervised model: an MLP on landmark vectors, with **95.73 ± 1.28% validation accuracy** (per search snippet).
  - The full text was paywalled, so I could not verify the label set, how labels were assigned, or whether validation was split by cat.
  - Confidence: LOW–MEDIUM until verified.
  - Sources: [Springer](https://link.springer.com/article/10.1007/s11042-026-21301-x); [search summary](https://www.researchgate.net/publication/387327364_Automated_landmark-based_cat_facial_analysis_and_its_applications)
- **Ali, Oyana, Salum**, "Domestic Cats Facial Expression Recognition Based on Convolutional Neural Networks" (SSRN preprint / ResearchGate).
  - Real-time CNN with four classes: Pleased, Angry, Alarmed, Calm.
  - Trained on an annotated cat image dataset whose labelling is not described.
  - Confidence: LOW (preprint, folk labels).
  - Sources: [SSRN](https://papers.ssrn.com/sol3/papers.cfm?abstract_id=4871374); [ResearchGate](https://www.researchgate.net/publication/381845330_Domestic_Cats_Facial_Expression_Recognition_Based_on_Convolutional_Neural_Networks)
- **Wu, Liu, Wang & Tao (2026)**, "Catellect-VL-2B: A Vision-Language Model for Edge-Based Feline Behavior Understanding" (arXiv 2608.22070, v3 Sept 2026). **Preprint.**
  - Post-trained Qwen3-VL-2B on a self-built 40K-image "SoulGardBench" dataset.
  - Accuracy: **80.62% behavior-field macro accuracy**.
  - Annotation protocol and class set are not given on the abstract page, and there is no GPT/Gemini comparison.
  - Confidence: LOW.
  - Source: [arXiv](https://arxiv.org/pdf/2608.22070)

**Closest precedent for frustration vs positive anticipation (dogs, same lab)**
- **Boneh-Shitrit et al. (2022)**, "Explainable Automated Recognition of Emotional States from Canine Facial Expressions: The Case of Positive Anticipation and Frustration", *Scientific Reports*.
  - Data: Labrador faces with context-induced labels (anticipating a food reward vs frustration).
  - Compared ResNet50 and DINO-ViT against DogFACS/landmark models.
  - A DINO-ViT model reached **89%** on this cropped-face dataset (figure as cited in Martvel et al. 2025).
  - I found **no equivalent cat study** on frustration vs anticipation.
  - Sources: [ResearchGate](https://www.researchgate.net/publication/362656504_Explainable_Automated_Recognition_of_Emotional_States_from_Canine_Facial_Expressions_The_Case_of_Positive_Anticipation_and_Frustration); [PMC12638295](https://pmc.ncbi.nlm.nih.gov/articles/PMC12638295/)

### Inferences
- The "best" scientifically trustworthy algorithm today is a CatFACS/48-landmark pipeline: CatFLW-style landmark detector, then geometric features, then a shallow classifier. A CNN/ViT is a comparable-accuracy alternative. Either way, the system is validated only for binary pain detection and binary social valence, with accuracy around 66–77% on automatically detected landmarks.
- Accuracy drops of 7–10 points from manual to automatic landmarks show that landmark detection on in-the-wild phone photos is a bottleneck.
- All strong automated cat work comes from one research group, so there is no independent replication.

### Gaps
- No peer-reviewed automated classifier for cat fear, frustration or relaxation with context-induced ground truth was found.
- Could not verify the label taxonomy or validation design of the 2026 Springer CatFLW emotion paper.
- Did not find full methods for Jain et al. (2023) "cat breeds emotion CNN", which was mentioned only as a citation.

## Q2. How was emotional ground truth established, and which categories are scientifically supported?

### Takeaway
The credible studies define ground truth by **context or stimulus** rather than by human judgement of the face:
- surgical/analgesic time points for pain
- affiliative vs agonistic social context
- shelter or human-interaction contexts plus ethogram/CatFACS clustering

Folk-labelled web datasets, where an annotator decides a face looks "angry", are circular and anthropomorphic. Scientifically supported constructs for cats are:
- **valence** (positive/negative)
- **pain** (FGS)
- **fear**
- **frustration**
- **relaxed engagement**
- **affiliative vs non-affiliative** signalling

### Cited Findings
- Pain labels came from time points: 1 hour after surgery = Pain; before surgery and after rescue analgesia = No Pain — [Frontiers 2024](https://www.frontiersin.org/journals/veterinary-science/articles/10.3389/fvets.2024.1442634/full)
- Bennett 2017's clusters (relaxed engagement, fear, frustration) emerged from CatFACS coding in shelter contexts and were checked against classic ethological moods (Leyhausen) — [PubMed](https://pubmed.ncbi.nlm.nih.gov/28341145/)
- Social-interaction labels (affiliative/non-affiliative) came from behavioural context in cat–cat interactions — [PMC11554805](https://pmc.ncbi.nlm.nih.gov/articles/PMC11554805)
- Dawson 2019 operationalised positive states as situations the cat sought out (e.g. petting, treats) and negative states as health problems or situations the cat retreated or fled from. This covered fear and frustration as well as positive states — [Animal Welfare PDF](https://cambridge.org/core/services/aop-cambridge-core/content/view/AC3942FBE101A240694349890B12553F/S0962728600011295a.pdf/humans-can-identify-cats-affective-states-from-subtle-facial-expressions.pdf); [Neuroscience News](https://neurosciencenews.com/cat-communication-15275/)
- Martvel et al. 2025 contrast a web dataset labelled "by a single annotator based on subjective judgment" (possibly anthropomorphic) with context-derived lab labels, which they call more objective — [PMC12638295](https://pmc.ncbi.nlm.nih.gov/articles/PMC12638295/)
- Scott & Florkiewicz did not decode what each of the 276 expressions means. Only valence was assigned — [Phys.org](https://phys.org/news/2023-10-housecats-hundreds-facial-interact-cats.amp)
- Even the Frontiers 2024 review describes the literature linking facial shape to emotion only as "fear, frustration, relaxed engagement, and pain" (citing Holden 2014, Evangelista 2019, Bennett 2017) — [Frontiers](https://www.frontiersin.org/journals/veterinary-science/articles/10.3389/fvets.2024.1442634/full)

### Inferences
How PurrSense's eight labels map onto the literature:

| PurrSense label | Literature mapping | Defensibility |
|---|---|---|
| relaxed | Bennett's "relaxed engagement" / positive valence | Defensible |
| anxious / fearful | Bennett's "fear" cluster | Partly defensible. The literature does not separate the two from faces. Consider merging into "fearful/anxious". |
| irritated | Bennett's "frustration", which is the scientific term | Partly defensible under that name |
| uncomfortable | Pain, the FGS construct | Partly defensible. Should be framed as possible pain/discomfort, with an FGS-like vet-referral caveat. |
| alert | Arousal/attention state, not an emotion | Weak. No validated facial classifier. |
| playful | Inferred mostly from body and context, not face | Weak. No validated facial classifier. |
| sleepy | Arousal/attention state, not an emotion | Weak. No validated facial classifier. |

- A defensible scheme could use two axes: valence (positive / neutral / negative) and arousal. Specific labels would be shown only when cues are strong, with a pain flag based on FGS action units.
- Body posture (ears, tail, crouch) is central to Leyhausen-style offensive/defensive moods. Face-only inference is a known limitation.

### Gaps
- Found no peer-reviewed automated study of cat **body posture** emotion recognition. Pose/behaviour work exists (e.g. Catellect-VL preprint), but none with validated affective ground truth.
- No study validates "playful", "sleepy" or "alert" as facially distinguishable cat emotional states.

## Q3. Architectures, dataset sizes, validation and accuracy

### Takeaway
The approaches cluster into two families with similar performance on small datasets (tens of cats, a few hundred to a few thousand images):
1. **48-landmark geometric features** (CatFACS-informed, CatFLW-trained detectors) plus a shallow classifier
2. **CNN/ViT on cropped faces** (ResNet50; DINO-ViT in dogs)

Video and temporal models help for pain.

### Cited Findings
| Study | Task | Data | Method | Result |
|---|---|---|---|---|
| Feighelstein 2022, Sci Rep | Pain vs no pain | 29 cats | ResNet50 vs landmark ML | Both >72% — [PMC9187730](https://pmc.ncbi.nlm.nih.gov/articles/PMC9187730) |
| Martvel 2024, Front Vet Sci | Pain | 464 images, 26 cats | Landmark ML | 0.73 manual / 0.66 automatic — [Frontiers](https://www.frontiersin.org/journals/veterinary-science/articles/10.3389/fvets.2024.1442634/full) |
| Video pain, Sci Rep 2024 | Pain | 2 datasets | Landmark time series | >70% and 66% — [Nature](https://www.nature.com/articles/s41598-024-78406-2) |
| Martvel 2024, Sci Rep | Affiliative vs non-affiliative | Cat café videos | CatFACS codes / automatic landmarks | >77% / ~68% — [PMC11554805](https://pmc.ncbi.nlm.nih.gov/articles/PMC11554805) |
| CatFLW / IJCV 2024 | Landmark detection | ~2,000+ images, 48 landmarks | Landmark detector | Infrastructure — [IJCV](https://link.springer.com/article/10.1007/s11263-024-02006-w) |
| Springer 2026 (MTAP?) | Multi-class emotion | CatFLW + added labels | MLP on landmarks | 95.73% validation (unverified) — [Springer](https://link.springer.com/article/10.1007/s11042-026-21301-x) |
| Catellect-VL-2B (preprint) | Behaviour fields | 40K images | Qwen3-VL-2B fine-tune | 80.62% macro — [arXiv](https://arxiv.org/pdf/2608.22070) |
| Boneh-Shitrit 2022 (dogs) | Anticipation vs frustration | 29 Labradors | DINO-ViT | 89% — [PMC12638295](https://pmc.ncbi.nlm.nih.gov/articles/PMC12638295/) |

### Inferences
- Very high accuracies (>90%) on multi-class web-image cat datasets probably reflect label circularity and dataset leakage, such as the same cat appearing in both train and validation, rather than true affect recognition. Context-grounded binary tasks consistently land at 66–77%.
- For a mobile app, a CatFLW-style landmark detector on-device plus interpretable geometric features maps directly onto CatFACS AUs and FGS AUs. That makes it explainable to users ("ears flattened, eyes narrowed").

### Gaps
- Most papers do not clearly report subject-wise (leave-one-cat-out) validation in the material I could access. I could not verify this for the 2026 Springer paper.

## Q4. Evidence on general VLMs/LLMs (Gemini, GPT-4V/4o, CLIP) recognising animal emotions

### Takeaway
The strongest evidence (Martvel, Zamansky, Shimshoni & Bremhorst 2025, Sci Rep) shows the following for dog faces with context-induced (objective) labels:
- 11 zero-shot LVLMs, including **Gemini 1.5 Pro, Gemini 2.0 Flash, Gemini 1.5 Flash, GPT-4o and GPT-4.1**, were **near chance (≤0.6 accuracy)**.
- A specialised DINO-ViT reached 89% on the same data.
- On web images with subjective labels, the LVLMs did moderately well, but appear to rely on background and breed cues.

No published benchmark tests VLMs on cat emotion specifically. This directly challenges PurrSense's current Gemini-prompting approach.

### Cited Findings
- **Martvel, Zamansky, Shimshoni, Bremhorst (2025)**, "Investigating the capabilities of large vision language models in dog emotion recognition", *Scientific Reports* 15:41250, DOI 10.1038/s41598-025-25199-7. Confidence: HIGH for dogs.
  - Tested GPT-4.1 (incl. mini and nano), o4-mini, GPT-4o (incl. mini), GPT-4 turbo, LLaVA-NeXT, Gemini 1.5 Pro, Gemini 2.0 Flash and Gemini 1.5 Flash.
  - On the cropped Labrador faces with context-induced labels, **no model exceeded 0.6 accuracy**. Most predicted almost a single class for every image.
  - GPT-4o mini scored 0.48 to 0.51 depending on prompt. Prompt engineering ("Panel of Experts") did not significantly help.
  - On the web dataset, GPT-4o variants beat the Gemini models. Changing the background shifted predictions toward the emotion the background implied.
  - Authors' conclusion: "current LVLMs are not yet suitable for reliable, biologically grounded recognition of canine emotions."
  - Source: [PMC12638295](https://pmc.ncbi.nlm.nih.gov/articles/PMC12638295/)
- **Cetintav, Guven, Gulek & Akbas (2025)**, "Generative AI Meets Animal Welfare: Evaluating GPT-4 for Pet Emotion Detection", *Animals* 15(4):492, DOI 10.3390/ani15040492. Confidence: LOW–MEDIUM (weak labels, internal inconsistencies).
  - The methods actually used GPT-4o.
  - Mixed-species Kaggle set (750 images; happy/sad/angry; labelling not described): **50.2% accuracy**.
  - Dog set (1,517 images, four classes, vet-reviewed): **76.6%**. The discussion contradicts this with 87.7%.
  - No per-species cat accuracy was reported.
  - Source: [PMC11851729](https://pmc.ncbi.nlm.nih.gov/articles/PMC11851729/)
- Catellect-VL-2B (preprint) says general VLMs tend to jump to labels such as "relaxed, afraid, or in pain" without grounding, but it reports no head-to-head comparison — [arXiv](https://arxiv.org/pdf/2608.22070)

### Inferences
- Gemini's mood labels for cats are likely driven by context (background, posture, breed morphology, stereotypes) more than by facial action units.
- A Gemini output is therefore not scientifically validated. Expect near-chance performance on subtle, context-grounded distinctions, especially fear vs frustration vs relaxed.
- This is inferred from the dog evidence. Cat faces are arguably harder: humans score only around 59% on them (Q5).

### Gaps
- No peer-reviewed cat-specific VLM benchmark found.
- No CLIP-based animal emotion benchmark found in this search.
- Newer models (e.g. Gemini 2.5/3.x, GPT-5) are untested in the literature found.

## Q5. Human accuracy at reading cat faces (baseline)

### Takeaway
Humans are only modestly better than chance at judging cat affective valence from faces. In Dawson et al. 2019, the average was 11.85/20 (about 59%) on a binary valence task, and only 13% of participants were individually significant. Vets and other experienced people do better; ordinary owners do not.

### Cited Findings
- **Dawson, Cheal, Niel & Mason (2019)**, "Humans can identify cats' affective states from subtle facial expressions", *Animal Welfare* 28(4):519–531, DOI 10.7120/09627286.28.4.519. Confidence: HIGH for the human baseline (very large N). Clips came from YouTube, so ground truth is moderate.
  - 6,329 online participants each watched 20 face-focused clips (10 positive, 10 negative).
  - Mean score: **11.85/20**, above chance.
  - **13%** of participants were individually significant.
  - Positive states were easiest to identify.
  - Women, younger people and those with veterinary experience scored better. Cat ownership helped little, and strong attachment to cats did not help.
  - Sources: [Cambridge PDF](https://cambridge.org/core/services/aop-cambridge-core/content/view/AC3942FBE101A240694349890B12553F/S0962728600011295a.pdf/humans-can-identify-cats-affective-states-from-subtle-facial-expressions.pdf); [Neuroscience News](https://neurosciencenews.com/cat-communication-15275/)
- Trained raters using the Feline Grimace Scale reach high reliability for pain (inter-rater ICC=0.89). The scale remains reliable across owners and vets, though muzzle and whiskers are weaker — [PMC6911058](https://pmc.ncbi.nlm.nih.gov/articles/PMC6911058); [PMC7933168](https://pmc.ncbi.nlm.nih.gov/articles/PMC7933168)
- Caeiro 2017 found that adopters were not influenced by subtle cat facial movements, which suggests humans largely do not attend to them — [NTU repository](https://irep.ntu.ac.uk/id/eprint/41463)

### Inferences
- Benchmarks for PurrSense:
  - Untrained humans: about 59% on binary valence.
  - Best automated binary models: about 66–77% for pain and social valence.
  - Trained FGS raters: reliable for pain.
- An eight-class mood output claimed from a single photo far exceeds what any validated human or machine method supports.
- The app should present outputs as probabilistic, low-confidence suggestions. Valence plus a pain flag is the most defensible core.

### Gaps
- No human-accuracy study for eight-way cat mood classification was found.
- No direct human vs machine comparison on the same cat dataset was found.
