package convolution;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Benchmark driver. Runs the full matrix (4 filters x 4 sizes x 4 thread counts), a
 * Gaussian kernel-size sweep at 4096^2, and a separate decode/encode I/O measurement.
 * Applies a warm-up / repeat / trimmed-mean timing protocol and writes CSVs.
 */
public final class Benchmark {
    static final int[] SIZES   = {512, 2048, 4096, 8192};
    static final int[] THREADS = {1, 2, 4, 8};
    static final int WARMUP = 2, REPS = 5;
    static final long SEED = 42;

    public static void main(String[] args) throws Exception {
        new File("results").mkdirs();
        new File("images").mkdirs();
        runMatrix();
        runKernelSweep();
        runIoTiming();
    }

    static void runMatrix() throws Exception {
        try (PrintWriter csv = new PrintWriter(new FileWriter("results/benchmark.csv"))) {
            csv.println("filter,size,threads,time_ms,speedup,efficiency");
            for (Filter f : Filter.defaultSet()) {
                for (int s : SIZES) {
                    PixelImage img = PixelImage.generate(s, SEED);
                    double t1 = Double.NaN;
                    for (int n : THREADS) {
                        double ms = measure(img, f, n);
                        if (n == 1) t1 = ms;
                        // EN: speedup S(n) = T(1)/T(n); efficiency E(n) = S(n)/n (Week 2 - Performance)
                        // TR: speedup S(n) = T(1)/T(n); verimlik E(n) = S(n)/n (Hafta 2 - Performans)
                        double sp = t1 / ms, eff = sp / n;
                        csv.printf("%s,%d,%d,%.3f,%.4f,%.4f%n", f.name, s, n, ms, sp, eff);
                        csv.flush();
                        System.out.printf("%-12s %5d %dT -> %8.2f ms  S=%.2f%n", f.name, s, n, ms, sp);
                        if (s >= 8192) Thread.sleep(3000); // cooldown for the U-series CPU
                    }
                }
            }
        }
    }

    static void runKernelSweep() throws Exception {
        try (PrintWriter csv = new PrintWriter(new FileWriter("results/kernel_sweep.csv"))) {
            csv.println("k,threads,time_ms,speedup,efficiency");
            int s = 4096;
            PixelImage img = PixelImage.generate(s, SEED);
            for (int k : new int[]{3, 5, 7, 15}) {
                Filter f = Filter.gaussian(k);
                double t1 = Double.NaN;
                for (int n : THREADS) {
                    double ms = measure(img, f, n);
                    if (n == 1) t1 = ms;
                    double sp = t1 / ms, eff = sp / n;
                    csv.printf("%d,%d,%.3f,%.4f,%.4f%n", k, n, ms, sp, eff);
                    csv.flush();
                    System.out.printf("Gaussian k=%-2d %dT -> %8.2f ms  S=%.2f%n", k, n, ms, sp);
                }
                Thread.sleep(3000); // cooldown between kernel sizes
            }
        }
    }

    /** Separate I/O timing (the Amdahl sequential-fraction component). */
    static void runIoTiming() throws Exception {
        // EN: image decode/encode is NOT parallelized -> it is the fixed sequential part
        //     that caps overall speedup (Amdahl's Law, Week 2). Measured separately.
        // TR: görüntü decode/encode paralelleştirilmez -> genel speedup'ı sınırlayan sabit
        //     sıralı kısımdır (Amdahl Yasası, Hafta 2). Ayrı ölçülür.
        PixelImage img = PixelImage.generate(4096, SEED);
        long t0 = System.nanoTime();
        img.save("images/io_sample_4096.png");
        long encMs = (System.nanoTime() - t0) / 1_000_000;
        t0 = System.nanoTime();
        PixelImage.load("images/io_sample_4096.png");
        long decMs = (System.nanoTime() - t0) / 1_000_000;
        try (PrintWriter csv = new PrintWriter(new FileWriter("results/io_timing.csv"))) {
            csv.println("operation,size,time_ms");
            csv.printf("encode,4096,%d%n", encMs);
            csv.printf("decode,4096,%d%n", decMs);
        }
        System.out.printf("I/O 4096^2: encode=%d ms decode=%d ms%n", encMs, decMs);
    }

    static double measure(PixelImage img, Filter f, int n) throws Exception {
        ExecutorService pool = (n == 1) ? null : Executors.newFixedThreadPool(n);
        try {
            // EN: warm-up runs let the JVM JIT-compile the hot loop before we time it.
            // TR: ısınma koşuları, ölçümden önce JVM'in sıcak döngüyü JIT-derlemesini sağlar.
            for (int w = 0; w < WARMUP; w++) run(img, f, n, pool);
            double[] t = new double[REPS];
            for (int r = 0; r < REPS; r++) {
                long s = System.nanoTime();
                run(img, f, n, pool);
                t[r] = (System.nanoTime() - s) / 1_000_000.0;
            }
            return trimmedMean(t);
        } finally {
            if (pool != null) pool.shutdown();
        }
    }

    static int[] run(PixelImage img, Filter f, int n, ExecutorService pool) throws Exception {
        return (n == 1) ? Convolver.convolveSequential(img, f)
                        : ParallelConvolver.convolveParallel(img, f, n, pool);
    }

    static double trimmedMean(double[] t) {
        double[] s = t.clone();
        Arrays.sort(s);
        double sum = 0;
        for (int i = 1; i < s.length - 1; i++) sum += s[i]; // drop min & max
        return sum / (s.length - 2);
    }
}
