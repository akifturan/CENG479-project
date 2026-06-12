package convolution;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Row-wise (horizontal stripe) parallel convolution. The output rows are split into
 * {@code threads} contiguous stripes (the remainder spread one row at a time across the
 * first stripes for balance), each handed to a {@link ConvolutionTask}. The caller owns
 * the pool's lifecycle so the benchmark can reuse one pool across repetitions.
 */
public final class ParallelConvolver {
    public static int[] convolveParallel(PixelImage src, Filter f, int threads, ExecutorService pool)
            throws Exception {
        int w = src.width, h = src.height;
        int[] out = new int[src.pixels.length];

        // EN: split the output rows into N stripes -> domain decomposition (Week 10)
        // TR: çıktı satırlarını N şeride böl -> alan ayrıştırma / domain decomposition (Hafta 10)
        List<Callable<Void>> tasks = new ArrayList<>(threads);
        int base = h / threads, rem = h % threads, y = 0;
        for (int t = 0; t < threads; t++) {
            // EN: equal-sized stripes -> balanced work per thread (load balancing, Week 6 & 10)
            // TR: eşit boyutlu şeritler -> thread başına dengeli yük (yük dengeleme, Hafta 6 & 10)
            int rows = base + (t < rem ? 1 : 0);
            int yStart = y, yEnd = y + rows;
            y = yEnd;
            // EN: each stripe = one independent task on the thread pool (Week 5 POSIX Threads analogue)
            // TR: her şerit = thread havuzunda koşan bağımsız bir görev (Hafta 5 POSIX Threads benzeri)
            tasks.add(new ConvolutionTask(src.pixels, out, w, h, f, yStart, yEnd));
        }

        // EN: stripes write to disjoint rows -> no mutex/lock needed (Week 5), no false sharing (Week 9)
        // TR: şeritler ayrık satırlara yazar -> mutex/kilit gerekmez (Hafta 5), false sharing yok (Hafta 9)
        for (Future<Void> fu : pool.invokeAll(tasks)) fu.get(); // EN: wait for all / TR: hepsini bekle
        return out;
    }
}
