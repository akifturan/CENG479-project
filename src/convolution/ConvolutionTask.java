package convolution;

import java.util.concurrent.Callable;

/**
 * A worker that convolves one disjoint stripe of output rows {@code [yStart, yEnd)} by
 * delegating to {@link Convolver#convolveRows}. Threads write to disjoint, row-aligned
 * regions of the shared {@code out} array and only read from {@code in}, so no locking
 * is needed.
 */
public record ConvolutionTask(int[] in, int[] out, int w, int h, Filter f, int yStart, int yEnd)
        implements Callable<Void> {
    @Override
    public Void call() {
        Convolver.convolveRows(in, out, w, h, f, yStart, yEnd);
        return null;
    }
}
