
package pngstofont;

import java.awt.image.BufferedImage;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

/**
 *
 * @author Coder Kap' <fojjta.wgz.cz>
 */
public class FontInit {
    
    private static final String EXPORT = "export/";
    
    Letter[] letts;

    public FontInit() {
        letts = readXML(PNGsToFONT.FNTINPUT);
        if (letts==null) {
            System.out.println("Nothing to be done! Exiting...");
            return;
        }
        System.out.println(letts.length+" chars loaded.");
        savePNGs(PNGsToFONT.BMPINPUT);
        
    }
    
    /**
     * This method reads the original .fnt file and it loads information about letters from it.
     * @param file
     * @return Array containing loaded Letters.
     */
    private Letter[] readXML(String file) {
        Letter[] letters = null;
        try {
            File fXmlFile = new File(file);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            
            NodeList nList = doc.getElementsByTagName("char");
            letters = new Letter[nList.getLength()];
            for (int i = 0; i < nList.getLength(); i++) {
		Node nNode = nList.item(i);
		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			Element eElement = (Element) nNode;
                        letters[i] = new Letter(Integer.parseInt(eElement.getAttribute("id")),
                                Integer.parseInt(eElement.getAttribute("x")), Integer.parseInt(eElement.getAttribute("y")),
                                Integer.parseInt(eElement.getAttribute("width")), Integer.parseInt(eElement.getAttribute("height")),
                                Integer.parseInt(eElement.getAttribute("xoffset")), Integer.parseInt(eElement.getAttribute("yoffset")),
                                Integer.parseInt(eElement.getAttribute("xadvance")), Integer.parseInt(eElement.getAttribute("page")),
                                Integer.parseInt(eElement.getAttribute("chnl")));
		}
            }
        } catch (ParserConfigurationException | SAXException | IOException | DOMException ex) {
            Logger.getLogger(FontInit.class.getName()).log(Level.SEVERE, null, ex);
        }
        return letters;
    }
    
    /**
     * This method takes all of the loaded letters in letts and saves single png file from each of them.
     * @param source 
     */
    private void savePNGs(String source) {
        try {
            new File(PNGsToFONT.PATH+EXPORT).mkdir();
            BufferedImage im_source = ImageIO.read(new File(source));
            for (Letter l : letts) {
                BufferedImage image = getCroppedImage(im_source.getSubimage(l.getX(), l.getY(), l.getWidth(), l.getHeight()));
                
                // Save as new image
                ImageIO.write(image, "PNG", new File(PNGsToFONT.PATH+EXPORT, l.getId()+".png"));
            }
        } catch (IOException ex) {
            Logger.getLogger(FontInit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This method handles situation, when we're setting new dimensions of letter.
     * It prevents from a crash caused by empty letters.
     * @param x
     * @param y
     * @return 
     */
    private int handle(int x, int y) {
        return (x - y < 0 ? Math.abs(y) : x - y);
    }
    
    /**
     * This method crops image to its minimum size.
     * @param source
     * @return Cropped image as BufferedImage object.
     */
    public BufferedImage getCroppedImage(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();

        int topY = height, topX = width;
        int bottomY = -1, bottomX = -1;
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                if ((source.getRGB(x, y) >> 24) != 0x00) {
                    if (x < topX) topX = x;
                    if (y < topY) topY = y;
                    if (x > bottomX) bottomX = x;
                    if (y > bottomY) bottomY = y;
                }
            }
        }
        
        BufferedImage destination = new BufferedImage(handle(bottomX+1,topX), handle(bottomY+1,topY), BufferedImage.TYPE_INT_ARGB);

        destination.getGraphics().drawImage(source, 0, 0, 
               destination.getWidth(), destination.getHeight(), 
               topX, topY, bottomX, bottomY, null);

        return destination;
    }
    
    public static void main(String[] a) {
        new FontInit();
    }

}
