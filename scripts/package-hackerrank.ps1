param(
    [string]$SubmissionDir = "submission"
)

$ErrorActionPreference = "Stop"

Write-Host "Building current branch before packaging..."
mvn -DskipTests package

if (!(Test-Path $SubmissionDir)) {
    New-Item -ItemType Directory -Path $SubmissionDir | Out-Null
}

$codeZip = Join-Path $SubmissionDir "code.zip"
$outputCsv = "output.csv"
$logTxt = "log.txt"

if (Test-Path $codeZip) { Remove-Item $codeZip -Force }

Write-Host "Creating minimal HackerRank code.zip..."
$temp = Join-Path $env:TEMP ("agentflow-hackerrank-code-" + [guid]::NewGuid())
New-Item -ItemType Directory -Path $temp | Out-Null

# Minimal Maven project for the challenge submission. This avoids shipping unrelated AgentFlow API code.
$minimalPom = @'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.mikeywestman</groupId>
    <artifactId>hackerrank-multimodal-claims-review</artifactId>
    <version>1.0.0</version>
    <name>HackerRank Multi-Modal Claims Review</name>
    <description>Damage-claim evidence review workflow for HackerRank Orchestrate.</description>

    <properties>
        <maven.compiler.release>21</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <jackson.version>2.17.2</jackson.version>
        <exec.maven.plugin.version>3.3.0</exec.maven.plugin.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>${jackson.version}</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
                <configuration>
                    <release>${maven.compiler.release}</release>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>${exec.maven.plugin.version}</version>
                <configuration>
                    <mainClass>com.mikeywestman.agentflow.hackerrankclaims.clean.CleanClaimsCli</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
'@
Set-Content -Path (Join-Path $temp "pom.xml") -Value $minimalPom -Encoding UTF8

# Challenge source only.
$sourceRoot = "src\main\java\com\mikeywestman\agentflow\hackerrankclaims\clean"
$destSourceRoot = Join-Path $temp $sourceRoot
New-Item -ItemType Directory -Path $destSourceRoot -Force | Out-Null
Copy-Item "$sourceRoot\*" $destSourceRoot -Recurse -Force

# Scripts useful for reproducibility.
$destScripts = Join-Path $temp "scripts"
New-Item -ItemType Directory -Path $destScripts -Force | Out-Null
Copy-Item "scripts\run-hackerrank-claims.ps1" $destScripts -Force

# README and evaluation material.
if (Test-Path "README_HACKERRANK_CLAIMS.md") {
    Copy-Item "README_HACKERRANK_CLAIMS.md" (Join-Path $temp "README_HACKERRANK_CLAIMS.md") -Force
}
if (Test-Path "evaluation") {
    Copy-Item "evaluation" (Join-Path $temp "evaluation") -Recurse -Force
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
