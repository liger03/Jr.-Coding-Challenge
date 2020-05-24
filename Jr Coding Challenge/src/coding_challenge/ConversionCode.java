/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coding_challenge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author john_
 */
public class ConversionCode 
{
    public class DatabaseThread implements Runnable  //a thread for adding database entries in parallel
    {
        List<String[]> data;
        String url;
        public DatabaseThread(List<String[]> input, String u)
        {
            data=input;
            url=u;
        }
        public void run()
        {
            addEntriesToDatabase(data, url);
        }
    }
    public static ArrayList<String[]> readCode(String uri) throws IOException, FileNotFoundException
    {
        ArrayList<String[]> storedData = new ArrayList<>();
        CSVReader reader = new CSVReader(new FileReader(uri));
        Iterator iter = reader.iterator();
        while(iter.hasNext())
        storedData.add((String[])iter.next());
        
        return storedData;
    }
    public static void splitEntries(ArrayList<String[]> entries, ArrayList<String[]> good,  ArrayList<String[]> bad)
    {
        for (String[] entry: entries)   //for every entry in the list of entries,
        {
            Boolean complete=true;
            for(String word: entry)    //check each word in each entry
                if(complete && (word.equals("")))     //if any of the words are blank or an entry isn't long enough,
                    complete=false;     //the entry isn't complete.
            if(!complete || entry.length!=10)           //if it isn't complete,
            {
                bad.add(entry);     //then add it to the malformed entry list
            }
            else
                good.add(entry);    //otherwise, add it to the proper entry list
        }
    }
    public static void writeEntries(ArrayList<String[]> entries)
    {
        for (String[] entry:entries)    //for every entry in the input list of entries,
        {
            for(String word:entry)      //for each word in the entry
            {
                System.out.print(word+"\t");    //write it out.
            }
            System.out.println();       //end each entry with a newline.
        }
    }
    public void createDatabase(ArrayList<String[]> entries, String url, String filename) //creates and populates the database
    {
        String newURL = new File(url).getParent()+"/"+filename+".db";
        newURL="jdbc:sqlite:"+newURL;

        try(Connection conn = DriverManager.getConnection(newURL))    //try, provided the connection is closed after trying
        {
            if(conn!=null)
            {
                Statement stmt = conn.createStatement();
                stmt.execute("CREATE TABLE IF NOT EXISTS parsed_entries (\n"
                        + "A text,\n"
                        + "B text,\n"
                        + "C text,\n"
                        + "D text,\n"
                        + "E text,\n"
                        + "F text,\n"
                        + "G text,\n"
                        + "H text,\n"
                        + "I text,\n"
                        + "J text \n"
                        +");"); //create the table with entries A through J.
                stmt.close();
            }
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
        ExecutorService pool = Executors.newCachedThreadPool();
        pool.submit(new DatabaseThread(entries.subList(0,entries.size()), newURL));
        try {
            if(!pool.awaitTermination(1, TimeUnit.HOURS))
                System.out.println("Database entry is taking longer than an hour! Are you sure SQLite is a good fit for this database?");
        } catch (InterruptedException ex) {
            System.out.println("The main thread was interrupted! Exception message: "+ex.getMessage());
        }
    }
    public static void addEntriesToDatabase(List<String[]> entries, String url)
    {
        try(Connection conn = DriverManager.getConnection(url))    //try, provided the connection is closed after trying
        {
            int batchSize=0;
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO parsed_entries VALUES(?,?,?,?,?,?,?,?,?,?)");
            for(String[] entry : entries)
            {
                    for(int i=0; i<entry.length; i++)
                        {
                            stmt.setString(i+1, entry[i]);
                        }
                stmt.addBatch();
                batchSize++;
                if(batchSize==100)
                {
                    stmt.executeBatch();
                    stmt.clearBatch();
                    batchSize=0;
                }
            }
            stmt.executeBatch();
            stmt.close();
        } catch (SQLException ex) {
            System.out.println("A SQLException occured. Error message: "+ex.getMessage());
        }
    }
    public static void logData(int received, int successful, String url)
    {
        
    }
    
}
