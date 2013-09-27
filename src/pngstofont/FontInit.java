
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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

/**
 *
 * @author Coder Kap' <fojjta.wgz.cz>
 */
public class FontInit {
    
    Letter[] letts;
    
//    PNGsToFONT ptf;
    MainWindow mw;

    public FontInit(MainWindow mainwindow) {
//        ptf = pngstofont;
        mw = mainwindow;
    }
    
    public String process() {
        letts = readXML();
        if (letts==null) {
            System.out.println("Nothing to be done! Exiting...");
            return "Nothing to be done! Exiting...";
        }
        System.out.println(letts.length+" chars loaded.");
        if (mw.ptf.getBmpOutput()==null) {
            System.out.println("Please initiate bmp output file!");
            mw.setStat("Please first initiate bmp output file!");
            return "Please initiate bmp output file!";
        }
        savePNGs(mw.ptf.getBmpInput());
        return "Letters were succesfully exported!";
    }
    
    public Map<String, String> readXMLHead() {
        Map<String, String> atributes = new HashMap<>();
        if (mw.ptf.getFntInput()==null) {
            System.out.println("Please initiate fnt input file!");
            mw.setStat("Please initiate fnt input file!");
            return null;
        }
        try {
            File fXmlFile = mw.ptf.getFntInput();
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
                
            Node temp = doc.getElementsByTagName("info").item(0);
            NamedNodeMap infoatribs = temp.getAttributes();
            temp = doc.getElementsByTagName("common").item(0);
            NamedNodeMap commonatribs = temp.getAttributes();
            for (int i=0; i<infoatribs.getLength(); i++)
                atributes.put(infoatribs.item(i).getNodeName(), infoatribs.item(i).getNodeValue());
            for (int i=0; i<commonatribs.getLength(); i++)
                atributes.put(commonatribs.item(i).getNodeName(), commonatribs.item(i).getNodeValue());
                    
        } catch (ParserConfigurationException | SAXException | IOException | DOMException ex) {
            Logger.getLogger(FontInit.class.getName()).log(Level.SEVERE, null, ex);
        }
        return atributes;
    }
    
    /**
     * This method reads the original .fnt file and it loads information about letters from it.
     * @param file
     * @return Array containing loaded Letters.
     */
    public Letter[] readXML() {
        Letter[] letters = null;
        if (mw.ptf.getFntInput()==null) {
            System.out.println("Please initiate fnt input file!");
            mw.setStat("Please initiate fnt input file!");
            return letters;
        }
        try {
            File fXmlFile = mw.ptf.getFntInput();
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
            System.out.println("Font can't be loaded! Corrupted fnt file! Exiting...");
            System.exit(0);
        }
        return letters;
    }
    
    /**
     * This method takes all of the loaded letters in letts and saves single png file from each of them.
     * @param source 
     */
    private void savePNGs(File sourcefile) {
        if (mw.ptf.getExportDir()==null) {
            System.out.println("Please initiate export dir!");
            mw.setStat("Please initiate export dir!");
            return;
        }
        try {
            mw.ptf.getExportDir().mkdir();
            BufferedImage im_source = ImageIO.read(sourcefile);
            for (Letter l : letts) {
                BufferedImage image = getCroppedImage(im_source.getSubimage(l.getX(), l.getY(), l.getWidth(), l.getHeight()));
//                BufferedImage image = im_source.getSubimage(l.getX(), l.getY(), l.getWidth(), l.getHeight());
                
                // Save as new image
                ImageIO.write(image, "PNG", new File(mw.ptf.getExportDir().getAbsolutePath(), l.getId()+".png"));
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
        return (x < y ? Math.abs(y) : x - y);
    }
    
    /**
     * This method crops image to its minimum size.
     * @param source
     * @return Cropped image as BufferedImage object.
     */
    public BufferedImage getCroppedImage(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();

        int topY = height, leftX = width;
        int bottomY = -1, rightX = -1;
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                if (((source.getRGB(x, y) >> 24) != 0x0)) {
                    if (x < leftX) leftX = x;
                    if (y < topY) topY = y;
                    if (x > rightX) rightX = x;
                    if (y > bottomY) bottomY = y;
//                    source.setRGB(x, y, Color.RED.getRGB());
                }
            }
        }
//        BufferedImage destination = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//
//        destination.getGraphics().drawImage(source, 0, 0, 
//               destination.getWidth(), destination.getHeight(), 
//               0, 0, width, height, null);
        
        BufferedImage destination = new BufferedImage(handle(rightX+2,leftX), handle(bottomY+2,topY), BufferedImage.TYPE_INT_ARGB);

        destination.getGraphics().drawImage(source, 0, 0, 
               destination.getWidth(), destination.getHeight(), 
               leftX-1, topY-1, rightX+1, bottomY+1, null);

        return destination;
    }

}
