package convolution;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Correctness / determinism gate: parallel output must be byte-identical to
 * the sequential baseline for every filter and thread count. Exits non-zero on any
 * mismatch so it can gate a build.
 */
public final class Verify {
    public static void main(String[] args) throws Exception {
        PixelImage img = PixelImage.generate(512, 42);
        boolean allOk = true;
        for (Filter f : Filter.defaultSet()) {
            int[] seq = Convolver.convolveSequential(img, f);
            for (int n : new int[]{2, 4, 8}) {
                ExecutorService pool = Executors.newFixedThreadPool(n);
                int[] par;
                try {
                    par = ParallelConvolver.convolveParallel(img, f, n, pool);
                } finally {
                    pool.shutdown();
                }
                // EN: parallel output must be byte-identical to sequential -> proves there is
                //     no race condition / data hazard on the shared output (Week 5 - Threads).
                // TR: paralel çıktı sıralıyla byte byte aynı olmalı -> paylaşılan çıktıda race
                //     condition / veri tehlikesi olmadığını kanıtlar (Hafta 5 - Threads).
                boolean ok = Arrays.equals(seq, par);
                allOk &= ok;
                System.out.printf("%-12s %dT : %s%n", f.name, n, ok ? "PASS" : "FAIL");
            }
        }
        if (!allOk) {
            System.err.println("CORRECTNESS FAILED");
            System.exit(1);
        }
        System.out.println("All correctness checks PASSED");
    }
}
