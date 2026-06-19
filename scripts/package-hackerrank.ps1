param(
    [string]$SubmissionDir = "submission"
)

$ErrorActionPreference = "Stop"

Write-Host "Building project..."
mvn -DskipTests package

if (!(Test-Path $SubmissionDir)) {
    New-Item -ItemType Directory -Path $SubmissionDir | Out-Null
}

$codeZip = Join-Path $SubmissionDir "code.zip"
$outputCsv = "output.csv"
$logTxt = "log.txt"

if (Test-Path $codeZip) { Remove-Item $codeZip -Force }

Write-Host "Creating code.zip..."
$exclude = @(
    ".git/*",
    "target/*",
    "dataset/*",
    "data/*",
    "submission/*",
    "node_modules/*",
    ".idea/*",
    ".vscode/*",
    "*.db",
    "*.sqlite"
)

$temp = Join-Path $env:TEMP ("agentflow-hackerrank-code-" + [guid]::NewGuid())
New-Item -ItemType Directory -Path $temp | Out-Null

Get-ChildItem -Path . -Recurse -File | ForEach-Object {
    $relative = Resolve-Path -Relative $_.FullName
    $relative = $relative.TrimStart(".", "\\", "/")
    $skip = $false
    foreach ($pattern in $exclude) {
        if ($relative -like $pattern) { $skip = $true; break }
    }
    if (-not $skip) {
        $dest = Join-Path $temp $relative
        $destDir = Split-Path $dest -Parent
        if (!(Test-Path $destDir)) { New-Item -ItemType Directory -Path $destDir | Out-Null }
        Copy-Item $_.FullName $dest
    }
}

Compress-Archive -Path (Join-Path $temp "*") -DestinationPath $codeZip -Force
Remove-Item $temp -Recurse -Force

if (Test-Path $outputCsv) {
    Copy-Item $outputCsv (Join-Path $SubmissionDir "output.csv") -Force
    Write-Host "Copied output.csv"
} else {
    Write-Warning "output.csv not found yet. Run the CLI first."
}

if (Test-Path $logTxt) {
    Copy-Item $logTxt (Join-Path $SubmissionDir "log.txt") -Force
    Write-Host "Copied log.txt"
} else {
    Write-Warning "log.txt not found yet. Run the CLI first."
}

Write-Host "Done. Submission folder: $SubmissionDir"
Write-Host "Expected HackerRank uploads: code.zip, output.csv, log.txt"
