# scripts/run-bench.ps1 - compile then run the full benchmark matrix
$ErrorActionPreference = "Stop"
powershell -File scripts/build.ps1
if (-not (Test-Path "images"))  { New-Item -ItemType Directory "images"  | Out-Null }
if (-not (Test-Path "results")) { New-Item -ItemType Directory "results" | Out-Null }
java -Xmx2g -cp out convolution.Benchmark
Write-Host "Benchmark done -> results/benchmark.csv, results/kernel_sweep.csv, results/io_timing.csv"
