
package pngstofont;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Coder Kap' <fojjta.wgz.cz>
 */
public class Letter {
    
    private int id=-1, x, y, width, height, xoffset, yoffset, xadvance, page, chnl;
    private int def_params[];
    private String paramsnames[];
    
    private BufferedImage bi;

    public Letter(int id, int x, int y, int width, int height, int xoffset, int yoffset, int xadvance, int page, int chnl) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.xoffset = xoffset;
        this.yoffset = yoffset;
        this.xadvance = xadvance;
        this.page = page;
        this.chnl = chnl;
        
        def_params = new int[]{id, x, y, width, height, xoffset, yoffset, xadvance, page, chnl};
        paramsnames = new String[]{"id", "x", "y", "width", "height", "xoffset", "yoffset", "xadvance", "page", "chnl"};
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getXoffset() {
        return xoffset;
    }

    public int getYoffset() {
        return yoffset;
    }

    public int getXadvance() {
        return xadvance;
    }

    public int getPage() {
        return page;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setChnl(int chnl) {
        this.chnl = chnl;
    }

    public int[] getParams() {
        return new int[]{getId(), getX(), getY(), getWidth(), getHeight(), getXoffset(), getYoffset(), getXadvance(), getPage(), getChnl()};
    }
    
    public int[] getDefParams() {
        return def_params;
    }

    public String[] getParamsnames() {
        return paramsnames;
    }

    public void setXoffset(int xoffset) {
        this.xoffset = xoffset;
    }

    public void setYoffset(int yoffset) {
        this.yoffset = yoffset;
    }

    public void setXadvance(int xadvance) {
        this.xadvance = xadvance;
    }

    public BufferedImage getBi(PNGsToFONT pngstofont) {
        if (bi==null) loadImage(pngstofont);
        return bi;
    }

    public void setBi(BufferedImage bi) {
        this.bi = bi;
    }

    public int getChnl() {
        return chnl;
    }
    
    public int getDefParam(String name) {
        for (int i=0; i<paramsnames.length; i++)
            if (paramsnames[i].equals(name))
                return def_params[i];
        return -1;
    }
    
    public boolean setDefParam(String name, int value) {
        for (int i=0; i<paramsnames.length; i++)
            if (paramsnames[i].equals(name)) {
                def_params[i] = value; return true;
            }
        return false;
    }
    
    public void loadImage(PNGsToFONT pngstofont) {
        try {
            if (id==-1) return;
            File in = pngstofont.getBmpInput();
            BufferedImage in_im = ImageIO.read(in);
            
            bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            bi = in_im.getSubimage(x, y, Math.min(width, in_im.getWidth()), Math.min(height, in_im.getHeight()));
        } catch (IOException ex) {
            Logger.getLogger(Letter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
