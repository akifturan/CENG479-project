# scripts/build.ps1 - compile all Java sources to out/
$ErrorActionPreference = "Stop"
if (-not (Test-Path "out")) { New-Item -ItemType Directory "out" | Out-Null }
$sources = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d out $sources
Write-Host "Build OK -> out/"
