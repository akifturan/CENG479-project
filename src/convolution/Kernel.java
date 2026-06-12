package convolution;

import java.util.Arrays;

/**
 * An immutable square convolution kernel stored in row-major order.
 * {@code size} is the side length {@code k} (odd); {@code radius} is {@code (k-1)/2}.
 */
public final class Kernel {
    public final double[] values; // length size*size, row-major
    public final int size;        // k (odd)
    public final int radius;      // (k-1)/2

    public Kernel(double[] values, int size) {
        this.values = values;
        this.size = size;
        this.radius = (size - 1) / 2;
    }

    /** Normalized Gaussian blur kernel (values sum to 1). */
    public static Kernel gaussian(int k) {
        double sigma = Math.max(k / 6.0, 0.5);
        int r = (k - 1) / 2;
        double[] v = new double[k * k];
        double sum = 0;
        int i = 0;
        for (int y = -r; y <= r; y++) {
            for (int x = -r; x <= r; x++) {
                double g = Math.exp(-(x * x + y * y) / (2 * sigma * sigma));
                v[i++] = g;
                sum += g;
            }
        }
        for (int j = 0; j < v.length; j++) v[j] /= sum; // normalize to 1
        return new Kernel(v, k);
    }

    /** Uniform box-blur kernel (each weight 1/k^2). */
    public static Kernel box(int k) {
        double[] v = new double[k * k];
        Arrays.fill(v, 1.0 / (k * k));
        return new Kernel(v, k);
    }

    /** 3x3 Laplacian-based sharpening kernel. */
    public static Kernel sharpen() {
        return new Kernel(new double[]{ 0, -1, 0,  -1, 5, -1,  0, -1, 0 }, 3);
    }

    /** 3x3 Sobel horizontal-gradient kernel. */
    public static Kernel sobelGx() {
        return new Kernel(new double[]{ -1, 0, 1,  -2, 0, 2,  -1, 0, 1 }, 3);
    }

    /** 3x3 Sobel vertical-gradient kernel. */
    public static Kernel sobelGy() {
        return new Kernel(new double[]{ -1, -2, -1,  0, 0, 0,  1, 2, 1 }, 3);
    }
}
