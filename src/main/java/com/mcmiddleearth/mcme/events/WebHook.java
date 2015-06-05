/*
 * This file is part of MCME-Events.
 * 
 * MCME-Events is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MCME-Events is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MCME-Events.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 */
package com.mcmiddleearth.mcme.events;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Donovan <dallen@dallen.xyz>
 */
public class WebHook {
    
    public static boolean Download(){
        boolean success = true;
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            URL url = new URL("");
            URLConnection urlConn = url.openConnection();

            is = urlConn.getInputStream();
            fos = new FileOutputStream(Main.getPluginDirectory()+System.getProperty("file.seperator")+"MCME-Events.jar");  

            byte[] buffer = new byte[8192];
            int len;

            //while we have availble data, continue downloading and storing to local file
            while ((len = is.read(buffer)) > 0){
                fos.write(buffer, 0, len);
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(WebHook.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        } catch (IOException ex) {
            Logger.getLogger(WebHook.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(WebHook.class.getName()).log(Level.SEVERE, null, ex);
                success = false;
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ex) {
                        Logger.getLogger(WebHook.class.getName()).log(Level.SEVERE, null, ex);
                        success = false;
                    }
                }
            }
        }
        return success;
    }
    
    public static void switchIn(){
        //not yet :/
    }
}
