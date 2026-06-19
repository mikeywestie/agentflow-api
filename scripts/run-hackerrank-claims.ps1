param(
    [string]$InputCsv = "dataset/test.csv",
    [string]$OutputCsv = "output.csv",
    [string]$ImageRoot = "dataset",
    [string]$AuditLog = "log.txt",
    [string]$Vision = "fallback",
    [string]$ExpectedCsv = ""
)

$ErrorActionPreference = "Stop"

$argsList = @(
    "--input", $InputCsv,
    "--output", $OutputCsv,
    "--image-root", $ImageRoot,
    "--audit", $AuditLog,
    "--vision", $Vision
)

if ($ExpectedCsv -ne "") {
    $argsList += @("--expected", $ExpectedCsv)
}

Write-Host "Running HackerRank claims workflow..."
Write-Host "Input: $InputCsv"
Write-Host "Output: $OutputCsv"
Write-Host "Vision: $Vision"

mvn -DskipTests compile exec:java "-Dexec.args=$($argsList -join ' ')"
