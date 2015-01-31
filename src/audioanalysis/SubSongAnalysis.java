/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audioanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;

/**
 *
 * @author Jeffrey
 */
public class SubSongAnalysis {
    /**
     * Condenses a directory of sub-song files into one ARFF for each song
     * 
     * @param file Directory containing all other sub-song folders of songs
     */
    public static void extract(String file){
        File root = new File(file);
        File[] folders = root.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
               return new File(dir,name).isDirectory();
            }
        });
        
        try {
            for(File current : folders){ //iterate  
                System.out.println(current.getName());
                jAudioRunner jRunner = new jAudioRunner("F:\\Jeffrey\\Desktop\\Science Project 2014-2015\\similarity tests\\subsongrun3", "F:\\Jeffrey\\Documents\\GitHub\\msj2013\\jaudioout\\definitions.xml");
                String outputName = current.getAbsolutePath()+"\\"+current.getName(); //name of output file
                jRunner.run(current.getAbsolutePath(), outputName+".tmp", false); //create a temporary file

                PrintWriter writer = new PrintWriter(outputName+".arff", "UTF-8"); //create final .arff file
                
                String output = "";
                boolean data = false;
                try(BufferedReader br = new BufferedReader(new FileReader(outputName+".tmp"))) {
                    String line = ""; 
                    while ((line = br.readLine()) != null) {
                        if(data){ //if we are in lines of data
                            writer.println(line+",?"); //append unknown mood class
                        } else {
                            writer.println(line); //otherwise just copy normally
                        }
                        if(line.contains("@ATTRIBUTE \"Area Method of Moments of MFCCs Overall Average9\" NUMERIC")){ //if we are at the last attribute
                            writer.println("@ATTRIBUTE MOODCLASS {0,1,2,3,4,5,6,7}"); //add in the mood attribute
                        }
                        if(line.contains("@DATA")){ //if the data start flag is found
                            data = true;
                        }
                    }
                    br.close();
                    File delete = new File(outputName+".tmp"); //delete temporary file
                    delete.delete();
                } catch (Exception ex){
                }
                writer.close();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
