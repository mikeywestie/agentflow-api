# HackerRank Multi-Modal Evidence Review Branch

This branch adapts AgentFlow into a focused Java CLI workflow for the HackerRank damage-claim review challenge.

## Goal

Read claim CSV files and local images, run a Planner -> Vision -> Reviewer workflow, persist audit traces, evaluate against labelled samples when available, and export `output.csv` using the exact HackerRank schema.

## Intended flow

```text
CSV claim row
  -> PlannerAgent extracts the actual damage claim
  -> VisionAgent analyses local image evidence
  -> ReviewerAgent compares the claim against the image evidence
  -> AuditLogWriter stores intermediate and final decisions
  -> OutputCsvWriter produces output.csv
  -> EvaluationRunner can compare sample predictions with expected outputs
```

## Run locally

```powershell
mvn -DskipTests package
java -cp target/classes com.mikeywestman.agentflow.hackerrankclaims.HackerRankClaimsCli --input dataset/test.csv --output output.csv --image-root dataset
```

Optional Gemini vision:

```powershell
$env:GEMINI_API_KEY="your-key"
java -cp target/classes com.mikeywestman.agentflow.hackerrankclaims.HackerRankClaimsCli --input dataset/test.csv --output output.csv --image-root dataset --vision gemini
```

If no VLM key is supplied, the pipeline still runs with conservative fallback decisions and audit traces.
