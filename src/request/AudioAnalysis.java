/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package request;

import audioanalysis.*;
import database.subsong.Save;
import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

/**
 *
 * @author Jeffrey
 */
public class AudioAnalysis {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
//        String dir = "F:\\Jeffrey\\Documents\\GitHub\\msj2013\\Songs\\wav"; //directory for WAV
//        File musicdir = new File(dir);
//        //array of MP3 files (to get artist and title)
//        File[] allfiles = musicdir.listFiles(new FilenameFilter(){ //use filter to make sure we don't read any album art files (.jpg)
//            @Override
//            public boolean accept(File dir, String name){
//                return name.toLowerCase().endsWith(".wav");
//            }
//        }
//        );
//        
//        for(File current : allfiles){
//           SubSong.createSubSong(current.getAbsolutePath()); 
//        }
        
        //SubSongAnalysis.extract("F:\\Jeffrey\\Documents\\GitHub\\msj2013\\Songs\\wav");
        Connection con = Save.startconnection("orcl");
        Save.save(con, "F:\\Jeffrey\\Documents\\GitHub\\msj2013\\Songs\\wav");
    }
}
