
package pngstofont;

/**
 *
 * @author Coder Kap' <fojjta.wgz.cz>
 */
public class Letter {
    
    private int id, x, y, width, height, xoffset, yoffset, xadvance, page, chnl;
    private int params[];
    private String paramsnames[];

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
        
        params = new int[]{id, x, y, width, height, xoffset, yoffset, xadvance, page, chnl};
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

    public void setChnl(int chnl) {
        this.chnl = chnl;
    }

    public int[] getParams() {
        return params;
    }

    public String[] getParamsnames() {
        return paramsnames;
    }
    
}
