
package pngstofont;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author      Coder Kap' <fojjta.cekuj.net>
 * @date        13.9.2013
 * @version     13.09.24.21
 *
 * List of changes, pattern for version is year.month.day.hour:
 * ----------------------------------------------------------
 * 13.09.13.23 - Setting up the project.
 * 13.09.14.14 - It can now save files into one bitmap image.
 * 13.09.14.16 - It can now save files into one bitmap image + save .fnt (XML based)
 *               file containing information about letters.
 * 13.09.16.0  - It can now load bitmap font from .fnt & .png, crop these chars, save them
 *               and then later it can again assemble them
 * 13.09.16.11 - Removed troubles with head of XML file.
 * 13.09.24.21 - Work on GUI of this app.
 * 13.09.27.17 - Work on GUI of this app - make new menu on the top of the main window.
 * 
 */
public class PNGsToFONT {
    
    public final static String version = "13.09.27.17";
    
    private File bmp_in=null, fnt_in=null, bmp_out, fnt_out;
    private File import_dir, export_dir;
    
    private final static String SUFFIX = ".png";
    
    private final static int XOFFSET = 0;
    private final static int PAGE = 0;
    private final static int CHNL = 15;
    
    private List<File> srcfiles;
    private int[] outparams;
    private Letter[] letters;
    
    public Point dimensions;
    
    private boolean debug = false;
    
    private MainWindow mw;
    

    public PNGsToFONT(MainWindow mainwindow) {
//        debug = true;
        mw = mainwindow;
    }
    
    /**
     * Method for setup some needed parameters for creating bitmap font.
     * @return True if there are some given letters.
     */
    private boolean init() {
        if (getImportDir()==null) {
            System.out.println("Please first set the import dir!");
            mw.setStat("Please first set the import dir!");
            return false;
        }
        srcfiles = searchFolderForNumberedPNGFiles(getImportDir().getAbsolutePath(), SUFFIX, true);
        if (srcfiles.isEmpty()) return false;
        srcfiles = sortFiles(srcfiles);
        outparams = goThrough(srcfiles);
        letters = new Letter[srcfiles.size()];
//        letters = mw.letters;
        
        System.out.println(srcfiles.size()+" files will be processed.");
        for (File f : srcfiles) System.out.println("Input file: "+f.getName());
        return true;
    }
    
    /**
     * This method tries to create bitmap font from configured .png files and letters data field.
     */
    public String process() {
        if (!init()) {
            System.out.println("Nothing to be done! Exiting...");
            return "Nothing to be done! Exiting...";
//            System.exit(0);
        }
        outputBitmap(outparams[0], outparams[1], outparams[2], outparams[3], srcfiles);
        xmlWrite(letters, outparams[2], outparams[3]);
        return "Bitmap font was succesfully created!";
    }
    
    /**
     * This method just sort files from smallest to biggest nubers in their names.
     * @param files
     * @return List of sorted files.
     */
    private List<File> sortFiles(List<File> files) {
        ObjectComparator comparator = new ObjectComparator();
        Collections.sort(files, comparator);
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
     * @param files
     * @return 
     */
    int[] goThrough(List<File> files) {
        int m_w=0, m_h=0;
        for (int i=0; i<files.size(); i++) {
            try {
                BufferedImage image = mw.fi.getCroppedImage(ImageIO.read(files.get(i)));
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
        
        return new int[]{m_w, m_h, (int)Math.pow(2, pow), (int)Math.pow(2, pow)};
    }
    
    /**
     * This method supposes, that dimensions are the same!
     * It creates output png font image file created by concating all letters.
     * 
     * @param width Width of output png font image.
     * @param height Height of output png font image.
     * @param dimensionx
     * @param dimensiony
     * @param files List of png files to concat.
     */
    void outputBitmap(int width, int height, int dimensionx, int dimensiony, List<File> files) {
        if (getBmpOutput()==null) {
            System.out.println("Please initiate bmp output file!");
            mw.setStat("Please first initiate bmp output file!");
            return;
        }
        try {
            BufferedImage combined = new BufferedImage(dimensionx, dimensiony, BufferedImage.TYPE_INT_ARGB_PRE);
            // paint both images, preserving the alpha channels
            Graphics g = combined.getGraphics();
            
            int act_x, act_y, act_px=0, act_py=0;
            
            int max_in_row = (int)Math.floor(dimensionx/width);
            System.out.println("Max number of letters in row is "+max_in_row);
            
            // for every letter - draw into combined & write to xml
            for (int i=0; i<files.size(); i++) {
                BufferedImage image = mw.fi.getCroppedImage(ImageIO.read(files.get(i)));
                int letw = image.getWidth(), leth = image.getHeight();
                
                act_x=act_px*width; act_y=act_py*height;
                
                int char_value = Integer.parseInt(files.get(i).getName().replace(".png", ""));
                
                letters[i] = new Letter(char_value, act_x, act_y, letw, leth, XOFFSET, height-leth-10, (int)(letw), PAGE, CHNL);
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
            ImageIO.write(combined, "PNG", getBmpOutput());
        } catch (IOException ex) {
            Logger.getLogger(PNGsToFONT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    boolean xmlWrite(Letter[] letters, int dimx, int dimy) {
        dimensions = new Point(dimx, dimy);
        return xmlWrite(letters);
    }
    
    /**
     * Write given letters data into .fnt file with XML parser help.
     * @param letters Field of letters to be processed into .fnt file.
     */
    boolean xmlWrite(Letter[] letters) {
        if (dimensions == null) {
            System.out.println("Please initiate dimensions!");
            mw.setStat("Please initiate dimensions!");
            return false;
        }
        if (getFntInput()==null || getBmpOutput()==null || getFntOutput()==null) {
            System.out.println("Please initiate files!");
            mw.setStat("Please first initiate input/output files!");
            return false;
        }
        try {
                // First, load some information from original .fnt file.
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(getFntInput());
                doc.getDocumentElement().normalize();
                
                Node temp = doc.getElementsByTagName("info").item(0);
                NamedNodeMap infoatribs = temp.getAttributes();
                temp = doc.getElementsByTagName("common").item(0);
                NamedNodeMap commonatribs = temp.getAttributes();
                
                
                // Now about the new xml file.
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
		// root elements
		doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("font");
		doc.appendChild(rootElement);
 
                Element info = doc.createElement("info");
                rootElement.appendChild(info);
                for (int i=0; i<infoatribs.getLength(); i++)
                    info.setAttribute(infoatribs.item(i).getNodeName(), infoatribs.item(i).getNodeValue());
                
                Element common = doc.createElement("common");
                rootElement.appendChild(common);
                for (int i=0; i<commonatribs.getLength(); i++)
                    common.setAttribute(commonatribs.item(i).getNodeName(), commonatribs.item(i).getNodeValue());
                common.setAttribute("scaleW", dimensions.x+"");
                common.setAttribute("scaleH", dimensions.y+"");
                
		// staff elements
		Element pages = doc.createElement("pages");
		rootElement.appendChild(pages);
 
                Element page = doc.createElement("page");
                pages.appendChild(page);
                page.setAttribute("id", "0");
                page.setAttribute("file", getBmpOutput().getName());
                
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
		StreamResult result = new StreamResult(getFntOutput());
 
		transformer.transform(source, result);
 
		System.out.println("File "+getFntOutput().getAbsolutePath()+" saved!");
 
	  } catch (ParserConfigurationException | TransformerException pce) {
              System.err.println("Error: "+pce);
	  } catch (SAXException ex) {
            Logger.getLogger(PNGsToFONT.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PNGsToFONT.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    /**
     * Comparator for integer files names.
     */
    public class ObjectComparator implements Comparator<File> {

        @Override
        public int compare(File o1, File o2) {
            int i1 = Integer.parseInt(o1.getName().replace(".png", ""));
            int i2 = Integer.parseInt(o2.getName().replace(".png", ""));
            return Integer.compare(i1, i2);
        }

    }
    
    public void setFntInput(File fntinput) {
        fnt_in=fntinput;
    }
    
    public void setBmpInput(File bmpinput) {
        bmp_in=bmpinput;
    }

    public void setFntOutput(File fnt_out) {
        this.fnt_out = fnt_out;
    }

    public void setBmpOutput(File bmp_out) {
        this.bmp_out = bmp_out;
    }

    public void setImportDir(File import_dir) {
        this.import_dir = import_dir;
    }

    public void setExportDir(File export_dir) {
        this.export_dir = export_dir;
    }
    
    public File getBmpInput() { if (bmp_in==null) return mw.getFontFiles()[0]; return bmp_in; }
    
    public File getFntInput() { if (fnt_in==null) return mw.getFontFiles()[1]; return fnt_in; }

    public File getBmpOutput() { if (export_dir==null) return mw.getExportDir(); return new File(export_dir.getAbsolutePath(), "font.png"); }

    public File getFntOutput() { if (export_dir==null) return mw.getExportDir(); return new File(export_dir.getAbsolutePath(), "font.fnt"); }

    public File getImportDir() { if (import_dir==null) return mw.getImportDir(); return import_dir; }

    public File getExportDir() { if (export_dir==null) return mw.getExportDir(); return export_dir; }
    
}