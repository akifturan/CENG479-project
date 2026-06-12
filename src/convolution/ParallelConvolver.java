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
        List<Callable<Void>> tasks = new ArrayList<>(threads);
        int base = h / threads, rem = h % threads, y = 0;
        for (int t = 0; t < threads; t++) {
            int rows = base + (t < rem ? 1 : 0);
            int yStart = y, yEnd = y + rows;
            y = yEnd;
            tasks.add(new ConvolutionTask(src.pixels, out, w, h, f, yStart, yEnd));
        }
        for (Future<Void> fu : pool.invokeAll(tasks)) fu.get(); // propagate worker exceptions
        return out;
    }
}
