# HackerRank Multi-Modal Evidence Review - Evaluation Report

## Overview

This solution implements a deterministic Java workflow for reviewing damage claims using claim conversations, local image files, user history, and constrained output labels. The workflow is structured as a pipeline with separate stages for claim planning, multimodal image review, risk handling, output normalization, audit logging, and sample-set evaluation.

The images are treated as the primary source of truth. The conversation is used to determine what object, part, and issue must be checked. User history is used only as risk context and does not override clear visual evidence.

## Evaluation Workflow

The CLI supports an optional `--expected` argument. When this is provided, the workflow compares generated predictions with `dataset/sample_claims.csv` and prints field-level metrics for:

- `evidence_standard_met`
- `issue_type`
- `object_part`
- `claim_status`
- `valid_image`
- `severity`

Example command:

```powershell
.\scripts\run-hackerrank-claims.ps1 `
  -InputCsv "dataset\sample_claims.csv" `
  -OutputCsv "sample_output.csv" `
  -ImageRoot "dataset" `
  -Vision "github" `
  -ExpectedCsv "dataset\sample_claims.csv"
```

Recent sample evaluation run:

| Field | Score |
|---|---:|
| evidence_standard_met | 14/20 = 70.00% |
| issue_type | 11/20 = 55.00% |
| object_part | 17/20 = 85.00% |
| claim_status | 12/20 = 60.00% |
| valid_image | 16/20 = 80.00% |
| severity | 10/20 = 50.00% |

The evaluation is intentionally field-level rather than a single aggregate score because the output schema contains multiple constrained labels with different failure modes.

## Model Calls

The GitHub Models provider makes approximately one multimodal model call per claim row when at least one provider-supported image is available.

Approximate calls:

| Dataset | Rows | Approximate model calls |
|---|---:|---:|
| sample_claims.csv | 20 | Up to 20 |
| claims.csv | 44 | Up to 44 |

The actual number can be lower if all images for a row are unsupported or missing, in which case the workflow emits a manual-review decision without calling the model.

## Images Processed

The test set contains multiple image paths per claim. The workflow reads local image files, derives image IDs from filenames, and sends supported image formats to the multimodal provider.

Supported provider formats:

- JPEG
- PNG
- WEBP

Unsupported or misleading formats, such as AVIF files disguised with `.jpg` extensions, are detected by byte signature rather than file extension and skipped for the provider call. The row still receives an output decision and does not crash the full run.

Approximate image handling:

| Dataset | Rows | Image behavior |
|---|---:|---|
| sample_claims.csv | 20 | One or more local images per row |
| claims.csv | 44 | One or more local images per row, with mixed image formats |

## Token Usage Estimate

Each model request includes:

- claim object
- planner-extracted issue and object part
- full claim conversation
- user history summary/flags
- image IDs and one or more images
- strict JSON output instructions

Approximate text token usage per call:

| Item | Estimate |
|---|---:|
| Prompt/instructions | 500-800 tokens |
| Conversation/user history | 100-500 tokens |
| JSON response | 100-250 tokens |

Image tokens depend on the provider's internal multimodal accounting and image size. For operational planning, this implementation assumes image-heavy calls are the dominant cost and latency factor.

Approximate full test text usage for 44 rows:

- Input text tokens: roughly 25k-60k
- Output text tokens: roughly 5k-12k
- Image processing: up to one multimodal request per row, with one or more images attached

## Cost Estimate

The current development configuration uses GitHub Models with `openai/gpt-4o-mini` through the GitHub Models inference endpoint when `-Vision "github"` is selected and `GITHUB_TOKEN` is present.

Pricing assumptions vary by provider and free-tier availability. For this challenge-sized dataset, cost is expected to be low because the maximum number of model calls is small:

- sample run: up to 20 model calls
- final test run: up to 44 model calls
- total development/evaluation runs: depends on repeated local testing

In free-tier development, the practical constraint observed was rate limiting rather than monetary cost.

## Latency and Runtime

Observed sample runtime with GitHub Models was approximately 6-7 minutes for 20 rows. Runtime is mostly affected by:

- network latency to the model endpoint
- image upload payload size
- model inference time
- retry/backoff for HTTP 429 rate limits

Expected full test runtime for 44 rows is roughly proportional and may take 10-20 minutes depending on rate limits and provider response time.

Fallback mode runs much faster because it does not call a remote model.

## Rate Limits and Reliability

The workflow includes several reliability safeguards:

- Per-claim failure isolation: one failed model call does not stop the whole CSV generation.
- HTTP 429 retry handling: model calls retry with progressive waiting.
- Unsupported image handling: images are inspected by magic bytes and unsupported formats are skipped.
- Missing image handling: missing files produce `not_enough_information` rather than a crash.
- Strict output validation: model outputs are mapped back to the allowed HackerRank labels.
- Fallback provider: the CLI can run with `-Vision "fallback"` when no model key is configured or when a conservative baseline output is needed.

## Batching and Caching Considerations

This implementation processes one claim row at a time. Batching was not used because each claim can contain different evidence requirements, images, user history, and final justifications. Row-level processing also makes audit logs easier to inspect.

Caching was considered but not implemented as a persistent cache because the dataset is small and local. In a production version, cache keys could be based on:

- normalized claim conversation hash
- image file hash
- claim object
- model name/version

This would prevent repeated model calls during iterative development.

## Final Prediction Workflow

Final predictions are generated with:

```powershell
.\scripts\run-hackerrank-claims.ps1 `
  -InputCsv "dataset\claims.csv" `
  -OutputCsv "output.csv" `
  -ImageRoot "dataset" `
  -Vision "github"
```

If the provider is unavailable or heavily rate-limited, the fallback workflow can still produce a complete, schema-valid `output.csv`:

```powershell
.\scripts\run-hackerrank-claims.ps1 `
  -InputCsv "dataset\claims.csv" `
  -OutputCsv "output.csv" `
  -ImageRoot "dataset" `
  -Vision "fallback"
```

## Limitations

- Multimodal output quality depends on provider availability and model consistency.
- Some image formats, especially AVIF, are not accepted by the selected provider and are skipped instead of converted.
- Severity labels remain subjective and are harder to match exactly than object-part labels.
- The solution favors reliability and schema correctness over aggressive rule hardcoding.

## Interview Notes

The architecture was designed to be explainable:

1. `PlannerService` extracts the claim intent from the conversation.
2. `VisionProvider` reviews local images and returns structured evidence decisions.
3. `HistoryService` adds risk context from user history.
4. `OutputMapper` guarantees the exact HackerRank CSV schema.
5. `Evaluator` measures sample-set field accuracy before final prediction generation.
6. `AuditLog` records decisions and failures for traceability.

This separation makes the system easier to debug and safer under hidden tests, because failures are isolated and all rows still produce output.
