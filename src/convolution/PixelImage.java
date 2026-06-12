package convolution;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Image representation for the convolution engine.
 * Pixels are stored as packed RGB ({@code 0xRRGGBB}) in a row-major {@code int[]},
 * so {@code pixels[y * width + x]} addresses pixel (x, y). This layout keeps each
 * worker thread's memory access contiguous (cache-friendly).
 */
public final class PixelImage {
    // EN: pixels are packed (one int per pixel) and stored row by row (row-major). Threads
    //     then read/write contiguous memory -> good cache locality (Week 9 - Cache).
    // TR: pikseller paketli (piksel başına tek int) ve satır satır (row-major) tutulur.
    //     Böylece threadler ardışık belleğe erişir -> iyi cache lokalitesi (Hafta 9 - Cache).
    public final int width;
    public final int height;
    public final int[] pixels; // packed 0xRRGGBB, length = width*height

    public PixelImage(int width, int height, int[] pixels) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }

    /** Deterministic gradient + noise image for benchmarking (cheap, reproducible). */
    public static PixelImage generate(int size, long seed) {
        int[] px = new int[size * size];
        Random rnd = new Random(seed);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int r = (x * 255) / size;
                int g = (y * 255) / size;
                int b = ((r + g) / 2) ^ rnd.nextInt(32);
                px[y * size + x] = rgb(r, g, b & 0xFF);
            }
        }
        return new PixelImage(size, size, px);
    }

    /** Shapes image for the visual demo so edges/blur are clearly visible. */
    public static PixelImage generateShapes(int size) {
        int[] px = new int[size * size];
        for (int i = 0; i < px.length; i++) px[i] = rgb(30, 30, 40); // background
        // filled white rectangle
        fillRect(px, size, size / 8, size / 8, size / 2, size / 3, rgb(240, 240, 240));
        // filled red square
        fillRect(px, size, size / 2, size / 2, size / 4, size / 4, rgb(220, 40, 40));
        // bright diagonal band
        for (int y = 0; y < size; y++) {
            int x = (y * 3) % size;
            for (int d = 0; d < size / 40 + 1 && x + d < size; d++) {
                px[y * size + x + d] = rgb(40, 200, 90);
            }
        }
        return new PixelImage(size, size, px);
    }

    private static void fillRect(int[] px, int w, int x0, int y0, int rw, int rh, int color) {
        int h = px.length / w;
        for (int y = y0; y < y0 + rh && y < h; y++) {
            for (int x = x0; x < x0 + rw && x < w; x++) {
                px[y * w + x] = color;
            }
        }
    }

    public static PixelImage load(String path) throws IOException {
        BufferedImage img = ImageIO.read(new File(path));
        int w = img.getWidth(), h = img.getHeight();
        int[] px = img.getRGB(0, 0, w, h, null, 0, w);
        for (int i = 0; i < px.length; i++) px[i] &= 0xFFFFFF; // drop alpha
        return new PixelImage(w, h, px);
    }

    public void save(String path) throws IOException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, width, height, pixels, 0, width);
        ImageIO.write(img, "png", new File(path));
    }

    public static int red(int p)   { return (p >> 16) & 0xFF; }
    public static int green(int p) { return (p >> 8) & 0xFF; }
    public static int blue(int p)  { return p & 0xFF; }
    public static int rgb(int r, int g, int b) { return (r << 16) | (g << 8) | b; }
}
