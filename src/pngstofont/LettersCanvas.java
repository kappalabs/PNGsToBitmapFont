
package pngstofont;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import javax.swing.JPanel;

/**
 *
 * @author Coder Kap' <fojjta.wgz.cz>
 */
public class LettersCanvas extends JPanel {
    
    private final static int YOFF = 70;
    private final static int LOFF = 10;

//    int width, height;
    String text;
    Letter[] letters;
    
    private int outline, lineHeight, underline=YOFF;
    private int edited=-1;
    private int[] names;
    private boolean ready=false;
    
    private MainWindow mw;

    public LettersCanvas(Letter[] letters, int outline, int lineHeight, MainWindow mainWindow) {
        this.letters = letters;
        this.outline = outline;
        this.lineHeight = lineHeight;
        mw = mainWindow;
        
        setLayout(new GridLayout());
        setVisible(true);
    }
    
    public void changeBackGround(int value) {
        setBackground(new Color((int)(255.0*(value/100.0)), (int)(255.0*(value/100.0)), (int)(255.0*(value/100.0))));
//        invalidate();
//        update(getGraphics());
    }
    
    public void setText(int[] names) {
        ready = true;
        if (names==null) ready = false;
        this.names = names;
        
        repaint();
    }
    
    public void setEdited(int value) {
        edited = value;
        repaint();
    }
    
    public void changeOutline(int value) {
        outline = value;
        repaint();
    }
    
    public void changeUnderline(int value) {
        underline = value;
        repaint();
    }
    
    @Override
    public void paint(Graphics gr) {
        super.paint(gr); //To change body of generated methods, choose Tools | Templates.
        Graphics2D g = (Graphics2D) gr;
        
        int x=(int)(getWidth()*(LOFF/100.0));
        int y=(int)(getHeight()*(YOFF/100.0))-lineHeight;
        if (ready) {
            for (int in : names) {
                for (Letter l : letters) {
                    if (l.getId() == in) {
                        g.drawImage(l.getBi(mw.ptf), x+l.getXoffset(), y+l.getYoffset(), null);
                        x+=l.getXadvance()+outline;
                    }
                }
            }
//            ready = false;
        }
        if (edited>-1) {
            for (Letter l : letters) {
                if (l.getId() == edited) {
                    g.setColor(Color.red);
                    g.drawRect(x+l.getXoffset(), y+l.getYoffset(), l.getWidth(), l.getHeight());
                    g.drawImage(l.getBi(mw.ptf), x+l.getXoffset(), y+l.getYoffset(), null);
                    x+=l.getXadvance()+outline;
                }
            }
        }
        
        // configurable text base line
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        g.setColor(Color.blue);
        g.drawLine((int)(getWidth()*(LOFF/100.0)), (int)(getHeight()*(underline/100.0)), (int)(getWidth()*((Math.abs(LOFF-100))/100.0)), (int)(getHeight()*(underline/100.0)));
        
        // main base line
        g.setColor(Color.red);
        g.drawLine((int)(getWidth()*(LOFF/100.0)), (int)(getHeight()*(YOFF/100.0)), (int)(getWidth()*((Math.abs(LOFF-100))/100.0)), (int)(getHeight()*(YOFF/100.0)));
        
        
    }
    
    public void setLetters(Letter[] lets) {
        letters = lets;
        repaint();
    }
    
}
