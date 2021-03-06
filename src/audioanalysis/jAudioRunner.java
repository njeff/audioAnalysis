/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package audioanalysis;

import jAudioFeatureExtractor.ACE.DataTypes.Batch;
import jAudioFeatureExtractor.ACE.XMLParsers.XMLDocumentParser;
import jAudioFeatureExtractor.DataModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runs jAudio (adapted from previous year's code)
 * 
 * @author Jeffrey
 */
public class jAudioRunner {
    String xmlOut = "";
    String batchFile = "";
    
    /**
     * Instantiates jAudioRunner
     * 
     * @param batchFile Full batch file directory
     * @param definitionOutput XML definition output
     */
    jAudioRunner(String batchFile, String definitionOutput){
        this.batchFile = batchFile;
        this.xmlOut = definitionOutput;
    }
       
    /**
     * Runs jAudio feature extractor
     * 
     * @param songPath Path of the song (or path to folder when in sub-song analysis mode)
     * @param outputFile Full path to .arff output (including file and file extension)
     * @param mode True for sub-song segmentation, false for sub-song analysis (the sub-song WAVs -> one ARFF)
     */
    void run(String songPath, String outputFile, boolean mode){
        try{
            File allfiles[] = null;
            File song = new File(songPath);
            if(song.getName().toLowerCase().endsWith(".wav")){ //if WAV
                System.out.println(song.getName());
                allfiles = new File[]{song};
            } else {
                if(!mode){ //if in sub-song analysis mode
                    allfiles = song.listFiles(new FilenameFilter() { //get all the WAV files in the directory
                        @Override
                        public boolean accept(File dir, String name) {
                           return name.toLowerCase().endsWith(".wav");
                        }
                    });
                    if(allfiles.length == 0||allfiles == null){
                        System.out.println("Invalid File.");
                        return; 
                    }
                } else { //otherwise bad file
                    System.out.println("Invalid File.");
                    return; 
                }
            }
  
            int windowSize = 4096; //size of the analysis window in samples
            double windowOverlap = 0; //percent overlap as a value between 0 and 1
            double samplingRate = 11025; //number of samples per second
            boolean normalize = false; //should the file be normalized before execution
            boolean perWindow = true; //should features be extracted on a window by window basis
            boolean overall = false; //should global features be extracted
            int outputType = 1; //what output format should extracted features be stored in
            String featureLocation = xmlOut; //location of the feature definition file
            
            if(!mode){ //if sub-song analysis mode
                windowSize = 512; //size of the analysis window in samples
                windowOverlap = 0; //percent overlap as a value between 0 and 1
                samplingRate = 16000; //number of samples per second
                normalize = false; //should the file be normalized before execution
                perWindow = false; //should features be extracted on a window by window basis
                overall = true; //should global features be extracted
                outputType = 1; //what output format should extracted features be stored in
                featureLocation = xmlOut; //location of the feature definition file
            }
            
            Object[] o = new Object[] {};
            try {
                    o = (Object[]) XMLDocumentParser.parseXMLDocument(this.batchFile, //location of file with batch settings (make sure the "dummy file" is valid!
                                    "batchFile");
            } catch (Exception e) {
                    System.out.println("Error parsing the batch file");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    System.exit(3);
            }
            String featureDestination;
            Batch b;
            DataModel dm = new DataModel("F:\\Jeffrey\\Desktop\\Science Project 2014-2015\\Libraries\\jAudio\\features.xml",null);
            for (int i = 0; i < o.length; ++i) {
                featureDestination = outputFile; //location where extracted features should be stored
                b = (Batch) o[i];
                b.setDestination(featureLocation,featureDestination);
                dm.featureKey = new FileOutputStream(new File(b.getDestinationFK()));
                dm.featureValue = new FileOutputStream(new File(b.getDestinationFV()));
                b.setDataModel(dm);
                b.setSettings(windowSize,windowOverlap,samplingRate,normalize,perWindow,overall,outputType);
                Arrays.sort(allfiles);
                b.setRecordings(allfiles);
                b.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Removes header from jAudio output
     * 
     * @param input Path to .arff
     */
    static void jAudioCleaner(String input){
        String line = "";
        String complete = "";
        boolean save = false;
        try(BufferedReader br = new BufferedReader(new FileReader(input))){
            while((line = br.readLine()) != null){
                if(save){
                    complete += line + System.getProperty("line.separator");
                }
                if(line.equals("@DATA")){
                    save = true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        if(!complete.isEmpty()){
            PrintWriter out = null;
            try{
                out = new PrintWriter(input);
                out.print(complete.trim()); //remove all trailing space and save to file
                out.close();
            } catch (Exception ex){
                ex.printStackTrace();
            } finally {
                try{
                    out.close();
                } catch (Exception ex){
                    
                }
            }
        }
    }
}
