
package pngstofont;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Coder KAPPA <fojjta.cekuj.net>
 */
public class CharsetHandler {
    
    
    public CharsetHandler() {}

    public Map<String, Integer> loadCharset(InputStream is) {
        BufferedReader br = null;
        if (is==null) return null;
        try {
//            File f = new File(file);
//            br = new BufferedReader(new FileReader(f));
            br = new BufferedReader(new InputStreamReader(is));
            
            Map<String, Integer> charset = new HashMap<>();
            String nl;
            while ((nl=br.readLine()) != null) {
                String[] parts = nl.split("\\s");
                String lett = ""; if (parts.length>2) lett = new String(parts[2].getBytes("UTF-8"));
                int lett_val = 63; if (parts.length>0) lett_val = Integer.parseInt(parts[0]);
                charset.put(lett, lett_val);
            }
            return charset;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CharsetHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CharsetHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
//        finally {
//            try {
//                br.close();
//            } catch (IOException ex) {
//                Logger.getLogger(CharsetHandler.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
        return null;
    }
    
}
