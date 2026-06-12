package convolution;

/**
 * The per-pixel convolution math — the single source of truth shared by the sequential
 * baseline and the parallel workers. {@link #convolveRows} processes a half-open row
 * range {@code [yStart, yEnd)} into {@code out}; the parallel engine calls it per stripe.
 *
 * <p>Edge handling is boundary clamping (replicate the nearest valid pixel); output values
 * are clamped to {@code [0, 255]}.
 */
public final class Convolver {

    // EN: each output pixel depends only on the read-only input -> fully independent work
    //     (embarrassingly parallel, Week 10). The same routine is used by the sequential
    //     baseline and by every parallel worker, so results are guaranteed identical.
    // TR: her çıktı pikseli yalnızca salt-okunur girdiye bağlı -> tümüyle bağımsız iş
    //     (embarrassingly parallel, Hafta 10). Aynı fonksiyonu hem sıralı baseline hem de
    //     her paralel işçi kullanır, bu yüzden sonuçların aynı olması garantidir.
    public static void convolveRows(int[] in, int[] out, int w, int h, Filter f, int yStart, int yEnd) {
        if (f.sobel) sobelRows(in, out, w, h, f.gx, f.gy, yStart, yEnd);
        else         linearRows(in, out, w, h, f.kernel, yStart, yEnd);
    }

    private static void linearRows(int[] in, int[] out, int w, int h, Kernel k, int yStart, int yEnd) {
        int r = k.radius;
        double[] kv = k.values;
        for (int y = yStart; y < yEnd; y++) {
            for (int x = 0; x < w; x++) {
                double sr = 0, sg = 0, sb = 0;
                int ki = 0;
                for (int ky = -r; ky <= r; ky++) {
                    int iy = clampIdx(y + ky, h);
                    int rowBase = iy * w;
                    for (int kx = -r; kx <= r; kx++) {
                        int ix = clampIdx(x + kx, w);
                        int p = in[rowBase + ix];
                        double kw = kv[ki++];
                        sr += ((p >> 16) & 0xFF) * kw;
                        sg += ((p >> 8) & 0xFF) * kw;
                        sb += (p & 0xFF) * kw;
                    }
                }
                out[y * w + x] = (clampPix((int) Math.round(sr)) << 16)
                               | (clampPix((int) Math.round(sg)) << 8)
                               |  clampPix((int) Math.round(sb));
            }
        }
    }

    private static void sobelRows(int[] in, int[] out, int w, int h, Kernel gx, Kernel gy, int yStart, int yEnd) {
        int r = gx.radius;
        double[] xv = gx.values, yv = gy.values;
        for (int y = yStart; y < yEnd; y++) {
            for (int x = 0; x < w; x++) {
                double gxr = 0, gxg = 0, gxb = 0, gyr = 0, gyg = 0, gyb = 0;
                int ki = 0;
                for (int ky = -r; ky <= r; ky++) {
                    int iy = clampIdx(y + ky, h);
                    int rowBase = iy * w;
                    for (int kx = -r; kx <= r; kx++) {
                        int ix = clampIdx(x + kx, w);
                        int p = in[rowBase + ix];
                        int pr = (p >> 16) & 0xFF, pg = (p >> 8) & 0xFF, pb = p & 0xFF;
                        double wx = xv[ki], wy = yv[ki];
                        ki++;
                        gxr += pr * wx; gyr += pr * wy;
                        gxg += pg * wx; gyg += pg * wy;
                        gxb += pb * wx; gyb += pb * wy;
                    }
                }
                int mr = clampPix((int) Math.round(Math.hypot(gxr, gyr)));
                int mg = clampPix((int) Math.round(Math.hypot(gxg, gyg)));
                int mb = clampPix((int) Math.round(Math.hypot(gxb, gyb)));
                out[y * w + x] = (mr << 16) | (mg << 8) | mb;
            }
        }
    }

    // EN: sequential baseline = one thread does all rows. This is T(1), the reference
    //     for speedup S(n) = T(1)/T(n) (Week 2 - Performance).
    // TR: sıralı baseline = tek thread tüm satırları yapar. Bu T(1)'dir; speedup
    //     S(n) = T(1)/T(n) hesabının referansıdır (Hafta 2 - Performans).
    public static int[] convolveSequential(PixelImage src, Filter f) {
        int[] out = new int[src.pixels.length];
        convolveRows(src.pixels, out, src.width, src.height, f, 0, src.height);
        return out;
    }

    static int clampIdx(int i, int max) { return i < 0 ? 0 : (i >= max ? max - 1 : i); }
    static int clampPix(int v)          { return v < 0 ? 0 : (v > 255 ? 255 : v); }
}
