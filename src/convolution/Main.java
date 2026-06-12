package convolution;

import java.io.File;
import java.io.IOException;

/**
 * Visual demo: generate a shapes image, apply every filter sequentially, and save the
 * outputs so a human can confirm each filter does what it should.
 */
public final class Main {
    public static void main(String[] args) throws IOException {
        new File("results").mkdirs();
        PixelImage src = PixelImage.generateShapes(512);
        src.save("results/demo_input.png");
        for (Filter f : Filter.defaultSet()) {
            int[] out = Convolver.convolveSequential(src, f);
            new PixelImage(src.width, src.height, out).save("results/demo_" + f.name + ".png");
            System.out.println("Wrote results/demo_" + f.name + ".png");
        }
    }
}
