/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pngstofont;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Coder Kap' <fojjta.wgz.cz>
 */
public class MainWindow extends javax.swing.JFrame {

    public Letter[] letters;
    private LettersCanvas lc;
    Map<String, Integer> charset;
    
    int processed_char=-1;
    
    FontInit fi;
    PNGsToFONT ptf;

    public MainWindow() {
        initComponents();
        
        ptf = new PNGsToFONT(this);
        fi = new FontInit(this);
        
        setStat("Please load bitmap font to start work with or choose font to be build.");
    }
//!!! zmenit nacitani charsetu !!!
    private void editLetters() {
        setLetters();
        if (lc==null) return;
        CharsetHandler chh = new CharsetHandler();
//        System.out.println("file = "+this.getClass().getResource("files/ISO-8859-2").getFile());
        charset = chh.loadCharset(this.getClass().getResourceAsStream("/files/ISO-8859-2"));
//        charset = chh.loadCharset("/home/kap/Plocha/mkfont/ISO-8859-2");
        sliderMoved(jSlider1.getValue());
        
        Canvas_jPanel.removeAll();
        Canvas_jPanel.add(lc);
        setStat("Bitmap font succesfully loaded.");
    }
    
    private void setLetters() {
        this.letters = fi.readXML();
//        for (final Letter l : letters)
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                l.loadImage();
//            }
//        });
        Map<String, String> head = fi.readXMLHead();
        if (head==null) {
            setStat("Head of the fnt file is empty!"); return;
        }
        int outline = Integer.parseInt(head.get("outline"));
        int lineHeight = Integer.parseInt(head.get("lineHeight"));
        lc = new LettersCanvas(letters, outline, lineHeight, this);
        ptf.dimensions = new Point(Integer.parseInt(head.get("scaleW")), Integer.parseInt(head.get("scaleH")));
        
        outlineSliderMoved(outline);
    }

    private void changeText(String text) {
        if (text == null) {
            setStat("There is no text to be showed!"); return;
        } if (charset == null) {
            setStat("Charset is not loaded properly!"); return;
        }
        lc.setText(getIDs(text));
    }
    
    private int[] getIDs(String text) {
        List<Integer> ids = new ArrayList<>();
        for (char c : text.toCharArray()) {
            System.out.println(c+" = " + (int) c);
////            File f = new File(PNGsToFONT.EXPORTPATH+((int)c)+".png");
////            if (f.exists()) ids.add((int)c);
//            ids.add((int)c);
            ids.add(charset.get(String.valueOf(c)));
        }
        System.out.println("Size = "+ids.size());
        if (ids.isEmpty()) return null;
        return toIntArray(ids);
    }
    
    int[] toIntArray(List<Integer> list)  {
        if (list == null) return null;
        int[] ret = new int[list.size()];
        int i = 0;
        for (Integer e : list)  
            if (e != null) ret[i++] = e.intValue();
        return ret;
    }
    
    private void changeProcessedChar(int char_value) {
        processed_char=-1;
        for (int i=0; i<letters.length; i++) {
            if (letters[i].getId() == char_value) {
                processed_char = i; break;
            }
        }
        if (processed_char==-1) return;
        
        Letter l = letters[processed_char];
        character_jLabel.setText("temp-"+(char)char_value);
        xoffset_jSpinner.setValue(l.getXoffset());
        yoffset_jSpinner.setValue(l.getYoffset());
        xadvance_jSpinner.setValue(l.getXadvance());
        
        lc.setEdited(char_value);
    }
    
    private void setNewLetterParams() {
        if (processed_char==-1) return;
        
        Letter l = letters[processed_char];
        l.setXoffset((int)xoffset_jSpinner.getValue());
        l.setYoffset((int)yoffset_jSpinner.getValue());
        l.setXadvance((int)xadvance_jSpinner.getValue());
        letters[processed_char] = l;
        lc.setLetters(letters);
        
        saveLetters();
    }
    
    private void exportLetters() {
        // need to adjust
        fi.process();
    }                                            

    private void createFont() {
        ptf.process();
    }                                               

    private void showAboutWindow() {
        JOptionPane.showMessageDialog(this, "Bitmap font editor\n\nVersion: \t"+PNGsToFONT.version+"\nAuthor: \tKAPPA",
                "About", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public File[] getFontFiles() {
//        setLetters();
        File[] fontfs = new File[2];
        
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new MyFilter());
        
        while(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
            File[] sel = fc.getSelectedFiles();
            for (File f : sel)
                System.out.println("name = "+f.getName());
//            if (checkFontFilesValidity(sel))
//                return new File[]{new File("bmp"), new File("fnt")};
            if (sel.length==0) return fontfs;
            String nmo = sel[0].getName().toLowerCase();
            if (nmo.endsWith("png")) fontfs[0]=sel[0];
            else if (nmo.endsWith("fnt")) fontfs[1]=sel[0];
            if (sel.length==2) {
                String nmt = sel[1].getName().toLowerCase();
                if (nmt.endsWith("png")) fontfs[0]=sel[1];
                else if (nmt.endsWith("fnt")) fontfs[1]=sel[1];
            }
            else {
                setStat("You need to specify both of the font files (.fnt & .png)!");
            }
            if (fontfs[0]!=null && fontfs[1]!=null) {
                setStat("Bitmap font was succesfuly specified.");
                ptf.setBmpInput(fontfs[0]); ptf.setFntInput(fontfs[1]);
                return fontfs;
            }
        }
        setStat("Aborting!");
        return fontfs;
    }
    
    public File getExportDir() {
        setStat("You need to specify export directory!");
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        if (fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
            setStat("Selected export directory: "+fc.getSelectedFile().getPath());
            ptf.setExportDir(fc.getSelectedFile());
            return fc.getSelectedFile();
        }
        setStat("Aborting!");
        return null;
    }
    
    public File getImportDir() {
        setStat("You need to specify directory with input .png files!");
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        if (fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
            setStat("Selected input directory: "+fc.getSelectedFile().getPath());
            ptf.setImportDir(fc.getSelectedFile());
            return fc.getSelectedFile();
        }
        setStat("Aborting!");
        return null;
    }
    
    private void saveLetters() {
        ptf.xmlWrite(letters);
    }
    
    private void redoLetterParams() {
        if (processed_char==-1) return;
        
        Letter l = letters[processed_char];
        l.setXoffset(l.getDefParam("xoffset"));
        l.setYoffset(l.getDefParam("yoffset"));
        l.setXadvance(l.getDefParam("xadvance"));
        letters[processed_char] = l;
        
        changeProcessedChar((int)value_jSpinner.getValue());
    }
    
    private void sliderMoved(int position) {
        if (lc==null) return;
        lc.changeBackGround(position);
    }
    
    private void outlineSliderMoved(int position) {
        if (lc==null) return;
        lc.changeOutline(position);
    }
    
    private void underlineSliderMoved(int position) {
        if (lc==null) return;
        lc.changeUnderline(position);
    }

    public void setStat(String status) {
        status_jLabel.setText(status);
    }
    
    class MyFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) return true;

//            String suff = f.getName().substring(f.getName().indexOf(".")+1);
//            if (suff != null) {
//                if (suff.toLowerCase().equals("fnt") || suff.toLowerCase().equals("png")) return true;
//                else return false;
//            }
            if (f.getName().toLowerCase().equals("font.fnt") || f.getName().toLowerCase().equals("font.png"))
                return true;

            return false;
        }

        @Override
        public String getDescription() {
            return "Bitmap font files (font.fnt & font.png)";
        }
        
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Canvas_jPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jSlider1 = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jSlider3 = new javax.swing.JSlider();
        jLabel4 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        character_jLabel = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        xoffset_jSpinner = new javax.swing.JSpinner();
        jLabel10 = new javax.swing.JLabel();
        yoffset_jSpinner = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        xadvance_jSpinner = new javax.swing.JSpinner();
        value_jSpinner = new javax.swing.JSpinner();
        set_char_values_jButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        textInput_jTextField = new javax.swing.JTextField();
        status_jLabel = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        edit_jMenuItem = new javax.swing.JMenuItem();
        export_jMenuItem = new javax.swing.JMenuItem();
        build_jMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        exit_jMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        about_jMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Bitmap Font Profiler by KAPPA");

        Canvas_jPanel.setBackground(new java.awt.Color(254, 222, 189));
        Canvas_jPanel.setToolTipText("");
        Canvas_jPanel.setLayout(new javax.swing.BoxLayout(Canvas_jPanel, javax.swing.BoxLayout.LINE_AXIS));

        jPanel2.setBackground(new java.awt.Color(185, 255, 194));

        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider1StateChanged(evt);
            }
        });

        jLabel1.setText("Background color:");

        jLabel3.setText("Outline:");

        jSlider3.setMajorTickSpacing(10);
        jSlider3.setMaximum(50);
        jSlider3.setMinorTickSpacing(1);
        jSlider3.setSnapToTicks(true);
        jSlider3.setValue(0);
        jSlider3.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider3StateChanged(evt);
            }
        });

        jLabel4.setText("Underline:");

        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(70, 0, 100, 1));
        jSpinner1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner1StateChanged(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(247, 254, 136));

        jLabel5.setText("Character:");

        character_jLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        character_jLabel.setText("char");

        jLabel7.setText("Value:");

        jLabel9.setText("Xoffset:");

        xoffset_jSpinner.setModel(new javax.swing.SpinnerNumberModel());

        jLabel10.setText("Yoffset:");

        yoffset_jSpinner.setModel(new javax.swing.SpinnerNumberModel());

        jLabel11.setText("Xadvance:");

        xadvance_jSpinner.setModel(new javax.swing.SpinnerNumberModel());

        value_jSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(32), null, null, Integer.valueOf(1)));
        value_jSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                value_jSpinnerStateChanged(evt);
            }
        });

        set_char_values_jButton.setText("Set values");
        set_char_values_jButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                set_char_values_jButtonActionPerformed(evt);
            }
        });

        jButton1.setText("Default");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(set_char_values_jButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel7)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(character_jLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(xoffset_jSpinner)
                            .addComponent(yoffset_jSpinner)
                            .addComponent(xadvance_jSpinner)
                            .addComponent(value_jSpinner))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(character_jLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(value_jSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(xoffset_jSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(yoffset_jSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addComponent(xadvance_jSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(set_char_values_jButton)
                    .addComponent(jButton1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSlider1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3)
                            .addComponent(jSlider3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSlider3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(186, 180, 255));

        textInput_jTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textInput_jTextFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(textInput_jTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 636, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(textInput_jTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        status_jLabel.setFont(new java.awt.Font("Arial", 1, 20)); // NOI18N
        status_jLabel.setText(" ");

        jMenu1.setText("File");

        edit_jMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        edit_jMenuItem.setText("Edit font");
        edit_jMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                edit_jMenuItemMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                edit_jMenuItemMouseExited(evt);
            }
        });
        edit_jMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                edit_jMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(edit_jMenuItem);

        export_jMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        export_jMenuItem.setText("Export letters");
        export_jMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                export_jMenuItemMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                export_jMenuItemMouseExited(evt);
            }
        });
        export_jMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                export_jMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(export_jMenuItem);

        build_jMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.CTRL_MASK));
        build_jMenuItem.setText("Build font");
        build_jMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                build_jMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(build_jMenuItem);
        jMenu1.add(jSeparator1);

        exit_jMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        exit_jMenuItem.setText("Exit");
        exit_jMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exit_jMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(exit_jMenuItem);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Help");

        about_jMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        about_jMenuItem.setText("About");
        about_jMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                about_jMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(about_jMenuItem);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Canvas_jPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(status_jLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(Canvas_jPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 436, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(status_jLabel)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void textInput_jTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textInput_jTextFieldActionPerformed
        changeText(textInput_jTextField.getText());
    }//GEN-LAST:event_textInput_jTextFieldActionPerformed

    private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider1StateChanged
        sliderMoved(jSlider1.getValue());
    }//GEN-LAST:event_jSlider1StateChanged

    private void jSlider3StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider3StateChanged
        outlineSliderMoved(jSlider3.getValue());
    }//GEN-LAST:event_jSlider3StateChanged

    private void jSpinner1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner1StateChanged
        underlineSliderMoved((int)jSpinner1.getValue());
    }//GEN-LAST:event_jSpinner1StateChanged

    private void value_jSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_value_jSpinnerStateChanged
        changeProcessedChar((int)value_jSpinner.getValue());
    }//GEN-LAST:event_value_jSpinnerStateChanged

    private void set_char_values_jButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_set_char_values_jButtonActionPerformed
        setNewLetterParams();
    }//GEN-LAST:event_set_char_values_jButtonActionPerformed

    private void exit_jMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exit_jMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exit_jMenuItemActionPerformed

    private void edit_jMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_edit_jMenuItemActionPerformed
        editLetters();
    }//GEN-LAST:event_edit_jMenuItemActionPerformed

    private void export_jMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_export_jMenuItemActionPerformed
        exportLetters();
    }//GEN-LAST:event_export_jMenuItemActionPerformed

    private void build_jMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_build_jMenuItemActionPerformed
        createFont();
    }//GEN-LAST:event_build_jMenuItemActionPerformed

    private void about_jMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_about_jMenuItemActionPerformed
        showAboutWindow();
    }//GEN-LAST:event_about_jMenuItemActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        redoLetterParams();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void edit_jMenuItemMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_edit_jMenuItemMouseEntered
        setStat("Loads an existing bitmap font from .fnt & .png files.");
    }//GEN-LAST:event_edit_jMenuItemMouseEntered

    private void edit_jMenuItemMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_edit_jMenuItemMouseExited
        setStat(" ");
    }//GEN-LAST:event_edit_jMenuItemMouseExited

    private void export_jMenuItemMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_export_jMenuItemMouseEntered
        setStat("Exports each letter from current bitmap font (font must be currently loaded).");
    }//GEN-LAST:event_export_jMenuItemMouseEntered

    private void export_jMenuItemMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_export_jMenuItemMouseExited
        setStat(" ");
    }//GEN-LAST:event_export_jMenuItemMouseExited

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainWindow().setVisible(true);
//                new MainWindow(new Letter[0]).setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Canvas_jPanel;
    private javax.swing.JMenuItem about_jMenuItem;
    private javax.swing.JMenuItem build_jMenuItem;
    private javax.swing.JLabel character_jLabel;
    private javax.swing.JMenuItem edit_jMenuItem;
    private javax.swing.JMenuItem exit_jMenuItem;
    private javax.swing.JMenuItem export_jMenuItem;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JSlider jSlider3;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JButton set_char_values_jButton;
    private javax.swing.JLabel status_jLabel;
    private javax.swing.JTextField textInput_jTextField;
    private javax.swing.JSpinner value_jSpinner;
    private javax.swing.JSpinner xadvance_jSpinner;
    private javax.swing.JSpinner xoffset_jSpinner;
    private javax.swing.JSpinner yoffset_jSpinner;
    // End of variables declaration//GEN-END:variables

}
