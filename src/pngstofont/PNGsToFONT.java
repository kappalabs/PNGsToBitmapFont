
package pngstofont;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
 
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author      Coder Kap' <fojjta.cekuj.net>
 * @date        13.9.2013
 * @version     13.09.13.23
 *
 * List of changes, pattern for version is year.month.day.hour:
 * ----------------------------------------------------------
 * 13.09.13.23 - Setting up the project.
 * 13.09.14.14 - It can now save files into one bitmap image.
 * 13.09.14.16 - It can now save files into one bitmap image + save .fnt (XML based)
 *               file containing information about letters.
 * 13.09.16.0  - It can now load bitmap font from .fnt & .png, crop these chars, save them
 *               and then later it can again assemble them
 * 
 */
public class PNGsToFONT {
    
    public final static String PATH = "/home/kap/Plocha/mkfont/";
    private final static String SUFFIX = ".png";
    private final static String BMPOUTPUT = "font.png";
    private final static String FNTOUTPUT = "font.fnt";
    
    private final static int XOFFSET = 0;
    private final static int PAGE = 0;
    private final static int CHNL = 15;
    
    private List<File> srcfiles;
    private int[] outparams;
    private Letter[] letters;
    
    private boolean debug = false;

    public PNGsToFONT() {
//        debug = true;
        if (!init(PATH)) {
            System.out.println("Nothing to be done! Exiting...");
            System.exit(0);
        }
        process(PATH);
    }
    
    private boolean init(String path) {
        srcfiles = searchFolderForNumberedPNGFiles(path, SUFFIX, true);
        if (srcfiles.isEmpty()) return false;
        srcfiles = sortFiles(srcfiles);
        outparams = goThrough(path, srcfiles);
        letters = new Letter[srcfiles.size()];
        
        System.out.println(srcfiles.size()+" files will be processed.");
        for (File f : srcfiles) System.out.println("Input file: "+f.getName());
        return true;
    }
    
    private void process(String path) {
        outputBitmap(outparams[0], outparams[1], outparams[2], path, srcfiles);
        xmltry(letters, outparams[2]);
    }
    
    private List<File> sortFiles(List<File> files) {
        ObjectComparator comparator = new ObjectComparator();
        Collections.sort(files, comparator);
//        Collections.sort( files );
        return files;
    }
    
    /**
     * Looks for PNG files with number in name in given directory.
     * @param pathToFolder home search destiny
     * @param suffix suffix of wanted file
     * @param recursively allows recursive searching
     * @return List of file names with specific suffix & number in name.
     */
    List<File> searchFolderForNumberedPNGFiles(String pathToFolder, String suffix, boolean recursively) {
        List<File> textFiles = new ArrayList<>();
        File dir = new File(pathToFolder);
        for (File file : dir.listFiles()) {
            if (file.getName().toLowerCase().matches("\\d+.png$"))
                textFiles.add(file);
            else if (recursively && file.isDirectory())
                textFiles.addAll(searchFolderForNumberedPNGFiles(file.getAbsolutePath(), suffix, true));
        }
        
        return textFiles;
}
    
    /**
     * This method finds out appropriate dimensions of output image and size of single letter.
     * @param path
     * @param files
     * @return 
     */
    int[] goThrough(String path, List<File> files) {
        int m_w=0, m_h=0;
        for (int i=0; i<files.size(); i++) {
            try {
                BufferedImage image = ImageIO.read(files.get(i));
                m_w = Math.max(m_w, image.getWidth());
                m_h = Math.max(m_h, image.getHeight());
            } catch (IOException ex) {
                System.out.println("Error: "+ex);
            }
        }
        System.out.println("Standard size set to "+m_w+" x "+m_h);
        int v = (int)Math.round(Math.ceil(Math.sqrt(m_w*m_h*files.size())));
        int pow=0;
        while (Math.pow(2, pow)<v || Math.ceil(files.size()/Math.floor(Math.pow(2, pow)/m_w))*m_h>Math.pow(2, pow)) pow++;
        
        return new int[]{m_w, m_h, (int)Math.pow(2, pow)};
    }
    
    void outputBitmap(int width, int height, int dimensions, String path, List<File> files) {
        try {
            BufferedImage combined = new BufferedImage(dimensions, dimensions, BufferedImage.TYPE_INT_ARGB);
            // paint both images, preserving the alpha channels
            Graphics g = combined.getGraphics();
            
            int act_x, act_y, act_px=0, act_py=0;
            
            int max_in_row = (int)Math.floor(dimensions/width);
            System.out.println("Max number of letters in row is "+max_in_row);
            
            // for every letter - draw into combined & write to xml
            for (int i=0; i<files.size(); i++) {
                BufferedImage image = ImageIO.read(files.get(i));
                int letw = image.getWidth(), leth = image.getHeight();
                
                act_x=act_px*width; act_y=act_py*height;
                
                int char_value = Integer.parseInt(files.get(i).getName().replace(".png", ""));
                
                letters[i] = new Letter(char_value, act_x, act_y, letw, leth, XOFFSET, height-leth, (int)(letw/1.3), PAGE, CHNL);
                g.drawImage(image, act_x, act_y, null);
                if (debug) {
                    g.setColor(Color.red);
                    g.drawRect(act_x, act_y, letw, leth);
                    FontMetrics fm = g.getFontMetrics();
                    g.drawString(""+char_value, act_x, act_y+leth+fm.getHeight());
                }
                
                if(++act_px/max_in_row >= 1.0) {
                    act_px=0; act_py++;
                }
            }

            // Save as new image
            ImageIO.write(combined, "PNG", new File(path, BMPOUTPUT));
        } catch (IOException ex) {
            Logger.getLogger(PNGsToFONT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    void xmltry(Letter[] letters, int dimensions) {
        try {
 
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("font");
		doc.appendChild(rootElement);
 
                Element info = doc.createElement("info");
                rootElement.appendChild(info);
                info.setAttribute("face", "Belisa plumilla manual");
                info.setAttribute("size", "50");
                info.setAttribute("bold", "1");
                info.setAttribute("italic", "0");
                info.setAttribute("charset", "");
                info.setAttribute("unicode", "1");
                info.setAttribute("stretchH", "100");
                info.setAttribute("smooth", "1");
                info.setAttribute("aa", "1");
                info.setAttribute("padding", "4,4,4,4");
                info.setAttribute("spacing", "4,4");
                info.setAttribute("outline", "2");
                
                Element common = doc.createElement("common");
                rootElement.appendChild(common);
                common.setAttribute("lineHeight", "50");
                common.setAttribute("base", "41");
                common.setAttribute("scaleW", dimensions+"");
                common.setAttribute("scaleH", dimensions+"");
                common.setAttribute("pages", "1");
                common.setAttribute("packed", "0");
                common.setAttribute("alphaChnl", "1");
                common.setAttribute("redChnl", "0");
                common.setAttribute("greenChnl", "0");
                common.setAttribute("blueChnl", "0");
                
		// staff elements
		Element pages = doc.createElement("pages");
		rootElement.appendChild(pages);
 
                Element page = doc.createElement("page");
                pages.appendChild(page);
                page.setAttribute("id", "0");
                page.setAttribute("file", BMPOUTPUT);
                
                Element chars = doc.createElement("chars");
                rootElement.appendChild(chars);
                chars.setAttribute("count", letters.length+"");
 
                for (Letter let : letters) {
                    Element letter = doc.createElement("char");
                    int ind=0;
                    for (int k : let.getParams())
                        letter.setAttribute(let.getParamsnames()[ind++], String.valueOf(k));
                    chars.appendChild(letter);
                }
 
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(PATH, FNTOUTPUT));
 
		transformer.transform(source, result);
 
		System.out.println("File saved!");
 
	  } catch (ParserConfigurationException | TransformerException pce) {
              System.err.println("Error: "+pce);
	  }
    }
    
    
    public static void main(String[] args) {
        new PNGsToFONT();
    }


    public class ObjectComparator implements Comparator<File> {

        @Override
        public int compare(File o1, File o2) {
            int i1 = Integer.parseInt(o1.getName().replace(".png", ""));
            int i2 = Integer.parseInt(o2.getName().replace(".png", ""));
            return Integer.compare(i1, i2);
        }

    }

}