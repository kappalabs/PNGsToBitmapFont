/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pngstofont;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * @author      Coder Kap' <fojjta.wgz.cz>
 * @date        13.9.2013
 * @version     ...
 *
 * Prehled zmen v dane verzi dle vzorce rok.mesic.den.hodina:
 * ----------------------------------------------------------
 * 
 * 
 */
public class PNGsToFONT {

    public PNGsToFONT() {
        doSomething();
    }
    
    private void doSomething() {
        try {
            File path = new File("/home/kap/Plocha/");

            // load source images
            BufferedImage image = ImageIO.read(new File(path, "32.png"));
            BufferedImage overlay = ImageIO.read(new File(path, "33.png"));

            // create the new image, canvas size is the max. of both image sizes
            int w = Math.max(image.getWidth(), overlay.getWidth());
            int h = Math.max(image.getHeight(), overlay.getHeight());
            BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            // paint both images, preserving the alpha channels
            Graphics g = combined.getGraphics();
            g.drawImage(image, 0, 0, null);
            g.drawImage(overlay, 0, 0, null);

            // Save as new image
            ImageIO.write(combined, "PNG", new File(path, "combined.png"));
        } catch (IOException ex) {
            Logger.getLogger(PNGsToFONT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new PNGsToFONT();
    }

}
