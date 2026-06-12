package convolution;

/**
 * A convolution filter. Most filters apply a single linear {@link Kernel}; Sobel edge
 * detection instead applies two kernels ({@code gx}, {@code gy}) and reduces them by
 * gradient magnitude. This is a class (not an enum) so the benchmark's kernel sweep can
 * build {@code gaussian(k)} for an arbitrary {@code k}.
 */
public final class Filter {
    public final String name;
    public final boolean sobel;
    public final Kernel kernel; // linear filters; null for Sobel
    public final Kernel gx, gy; // Sobel only; null otherwise

    private Filter(String name, boolean sobel, Kernel kernel, Kernel gx, Kernel gy) {
        this.name = name;
        this.sobel = sobel;
        this.kernel = kernel;
        this.gx = gx;
        this.gy = gy;
    }

    public static Filter gaussian(int k) {
        return new Filter("GAUSSIAN_" + k, false, Kernel.gaussian(k), null, null);
    }

    public static Filter box(int k) {
        return new Filter("BOX_" + k, false, Kernel.box(k), null, null);
    }

    public static Filter sharpen() {
        return new Filter("SHARPEN_3", false, Kernel.sharpen(), null, null);
    }

    public static Filter sobel() {
        return new Filter("SOBEL_3", true, null, Kernel.sobelGx(), Kernel.sobelGy());
    }

    /** The 4 filters of the benchmark matrix. */
    public static Filter[] defaultSet() {
        return new Filter[]{ gaussian(5), sobel(), sharpen(), box(5) };
    }

    @Override
    public String toString() {
        return name;
    }
}
