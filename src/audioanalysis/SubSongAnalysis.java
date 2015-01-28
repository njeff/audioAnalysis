/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audioanalysis;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author Jeffrey
 */
public class SubSongAnalysis {
    /**
     * 
     * @param file Directory containing all other folders of songs
     */
    public static void extract(String file){
        File root = new File(file);
        File[] folders = root.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
               return new File(dir,name).isDirectory();
            }
        });
        
        for(File current : folders){
            jAudioRunner jRunner = new jAudioRunner("F:\\Jeffrey\\Desktop\\Science Project 2014-2015\\similarity tests\\subsongrun2", "F:\\Jeffrey\\Documents\\GitHub\\msj2013\\jaudioout\\definitions.xml");
            jRunner.run(current.getAbsolutePath(),current.getAbsolutePath()+"\\"+current.getName()+".arff",false);
        }
    }
}
