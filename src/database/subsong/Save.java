/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database.subsong;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

/**
 *
 * @author Jeffrey
 */
public class Save {
    
    /**
     * Establish database connection
     * 
     * @param database Database to access
     * @return 
     */
    public static Connection startconnection(String database){ //connection code is from http://www.mkyong.com/jdbc/connect-to-oracle-db-via-jdbc-driver-java/
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
	} catch (ClassNotFoundException e) {
            System.out.println("Where is your Oracle JDBC Driver?");
            e.printStackTrace();
            return null;
	}
 
	Connection connection = null;
 
	try {
            connection = DriverManager.getConnection( //get connection to specified database
			"jdbc:oracle:thin:@localhost:1521:"+database, "sys as sysdba",
			"oracle10g");
	} catch (SQLException e) {
		System.out.println("Connection Failed! Check output console");
		e.printStackTrace();
                return null;
	}
 
	if (connection != null) {
		System.out.println("Connected.");
	} else {
		System.out.println("Failed to make connection.");
	}
        return connection;
    }
    
    /**
     * Saves subsong data to the database
     * 
     * @param con Database connection
     * @param path Path to the directory containing all the song folders
     */
    public static void save(Connection con, String path){
        File musicdir = new File(path);
        File[] allfiles = musicdir.listFiles(new FilenameFilter(){
            @Override
            public boolean accept(File dir, String name){
                return new File(dir,name).isDirectory(); //get all subdirectories
            }
        });
        
        for(int i = 0; i<allfiles.length; i++){
            String arffFile = allfiles[i].getAbsolutePath() + "\\" + allfiles[i].getName() + "_c.arff";
            System.out.println(arffFile);
            File[] wavfiles = new File(allfiles[i].getAbsolutePath()).listFiles(new FilenameFilter(){
                @Override
                public boolean accept(File dir, String name){
                    return name.toLowerCase().endsWith(".wav"); //get all WAV files in the song folder
                }
            });
            
            float[] lengths = new float[wavfiles.length];
            int[] moods = new int[wavfiles.length];
            Arrays.sort(wavfiles); 
            int position = 0;
            //get the lengths of each segment
            for(File current : wavfiles){
                try{
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(current);
                    AudioFormat format = audioInputStream.getFormat();
                    long audioFileLength = current.length();
                    int frameSize = format.getFrameSize();
                    float frameRate = format.getFrameRate();
                    float durationInSeconds = (audioFileLength / (frameSize * frameRate));
                    lengths[position] = durationInSeconds;
                    //System.out.println(durationInSeconds);
                } catch (Exception e){
                    e.printStackTrace();
                }
                position++;
            }
            
            //get the moods of each segment
            try(BufferedReader br = new BufferedReader(new FileReader(arffFile))) {
                String line = "";
                boolean data = false;
                int mposition = 0;
                while ((line = br.readLine()) != null) {
                    if(data){
                        moods[mposition]=Integer.parseInt(line.substring(line.length()-1)); //read the last value at the end of each data row (which is the mood)
                        //System.out.println(moods[mposition]);
                        mposition++;
                    }
                    if(line.equals("@data")){
                        data = true;
                    }
                }
                br.close();
            } catch (Exception ex){
                ex.printStackTrace();
            }
            
            //get the title and artistid
            String title = "";
            int artistid = -1;
            Statement stmt = null;
            try {
                stmt = con.createStatement();
                ResultSet rs = null;
                if(allfiles[i].getName().equals("Y.M.C.A")){ //formatting issue with folder
                    rs = stmt.executeQuery("SELECT TITLE, ARTISTID FROM SONGTABLE WHERE DIR='" + "Y.M.C.A." + ".wav'"); //get row with the correct songs
                } else {
                    rs = stmt.executeQuery("SELECT TITLE, ARTISTID FROM SONGTABLE WHERE DIR='" + allfiles[i].getName().replace("'", "''") + ".wav'"); //get row with the correct songs
                }
                while (rs.next()) {
                    title = rs.getString("TITLE");
                    artistid = rs.getInt("ARTISTID");
                }
            } catch (SQLException e) {
                //System.out.println(allfiles[i].getName());
                e.printStackTrace();
            } finally {
                if (stmt != null) { try {stmt.close(); } catch (SQLException ex) { ex.printStackTrace();}} //close connection
            }
            
            //add in subsong moods
            for(int j = 0; j<moods.length; j++){
                Statement stmt2 = null;
                try {
                    stmt2 = con.createStatement();
                    ResultSet rs = stmt2.executeQuery("INSERT INTO SUBSONGTABLE (TITLE,ARTISTID,SEQUENCE,MOOD,LENGTH) VALUES ('"+title+"',"+artistid+","+j+","+moods[j]+","+lengths[j]+")"); //get row with the correct songs
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    if (stmt2 != null) { try {stmt2.close(); } catch (SQLException ex) { ex.printStackTrace();}} //close connection
                }
            }
        }
    }
}
