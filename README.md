# Parallel 2D Image Convolution (Java Threads)

CENG-479 Parallel Programming — Spring 2026 term project.
A multi-threaded 2D image-convolution engine plus a benchmark harness that measures
sequential-vs-parallel speedup across filters, image sizes, and thread counts on a
multi-core CPU.

## Prerequisites

### JDK 21 (required)

The project uses plain `javac` / `java` — no Maven or Gradle.

```powershell
winget install Microsoft.OpenJDK.21
```

(Alternatively download a JDK 21 build from https://adoptium.net.)
Open a **new** terminal after installing, then confirm both report 21 or newer:

```powershell
java -version
javac -version
```

### Python (for plotting)

Python 3.12+ with the chart dependencies listed in `analysis/requirements.txt`
(see the Plots section below).

## Build

```powershell
powershell -File scripts/build.ps1
```

Compiles every `.java` file under `src/` into `out/`. Prints `Build OK -> out/` on success.

## Run

### Visual demo

```powershell
java -cp out convolution.Main
```

Generates a 512x512 shapes image and applies all four filters sequentially, writing
`results/demo_input.png` plus one `results/demo_<FILTER>.png` per filter. Open them to
confirm: Gaussian/box are blurred, sharpen accentuates edges, Sobel shows bright edges
on a dark background.

### Benchmark

```powershell
powershell -File scripts/run-bench.ps1
```

Runs the full matrix (4 filters x 4 sizes x 4 thread counts), a Gaussian kernel-size
sweep at 4096x4096, and a separate I/O timing. Streams per-cell timings and writes:

- `results/benchmark.csv` — `filter,size,threads,time_ms,speedup,efficiency` (64 data rows)
- `results/kernel_sweep.csv` — `k,threads,time_ms,speedup,efficiency` (16 data rows)
- `results/io_timing.csv` — encode/decode timing for 4096x4096

Takes a few minutes; inserts a short cooldown after the heaviest cells.

### Plots

```powershell
python -m pip install -r analysis/requirements.txt
python analysis/plot.py
```

Reads the benchmark CSVs and writes to `results/`: `speedup_<FILTER>.png` and
`efficiency_<FILTER>.png` for each filter, plus `speedup_vs_size.png` and
`kernel_sweep.png`.
