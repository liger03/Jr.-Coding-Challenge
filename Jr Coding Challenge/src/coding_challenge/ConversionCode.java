/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coding_challenge;

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
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

/**
 *
 * @author john_
 */
public class ConversionCode 
{

    public class DatabaseThread extends SwingWorker<Integer, Integer>  //a thread for adding database entries in parallel
    {
        List<String[]> data;
        String url;
        JProgressBar bar;
        public DatabaseThread(List<String[]> input, String u, JProgressBar barIn)
        {
            data=input; //the data this ConversionCode object will process
            url=u;      //the url this ConversionCode object will load to
            bar=barIn;  //the progress bar this ConversionCode will update
        }
        @Override
        public Integer doInBackground()
        {
            try(Connection conn = DriverManager.getConnection(url))    //try, provided the connection is closed after trying
            {
                publish(0);             //publish that the bars are empty
                for(int i=0; i<100; i++)
                {
                    if(i<99)        //split up the databse entries into ninety-nine even parts
                        addEntriesToDatabase(conn, data.subList(data.size()*i/100, data.size()*(i+1)/100), url);
                    else
                        addEntriesToDatabase(conn, data.subList(data.size()*99/100, data.size()), url); //with an extra last one containing the remainder
                    publish(i); //and publish progress
                }
                publish(100);   //when the loop is done, ensure the bar is set to 100%
            } catch (SQLException ex) {
            ex.printStackTrace();   //and print the stack trace on any exceptions
        }
            return 1;
        }
        @Override
        protected void process(List<Integer> progress)  //command run in the main thread to update progress bars
        {
            int prog=progress.get(progress.size()-1);   //get the latest value of program's progress
            bar.setValue(prog);     //set the code's bar to this progress
            if(prog<100)
                bar.setString(""+prog+"%"); //if the work isn't finished, show a percentage
            else
                bar.setString("Complete!"); //otherwise, say it's complete
            bar.repaint();                  //repaint the bar to make sure it's visually updated
        }
    }
    public static ArrayList<String[]> readCode(String url) throws IOException, FileNotFoundException
    {
        ArrayList<String[]> storedData = new ArrayList<>(); //declare variables
        CSVReader reader = new CSVReader(new FileReader(url));  //a reader to read CSV files
        Iterator iter = reader.iterator();  //and an iterator on the reader
        while(iter.hasNext())
            storedData.add((String[])iter.next());  //simply read each entry as string arrays
        return storedData;  //and return the read entries as an arraylist
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
    public void createDatabase(ArrayList<String[]> entries, String url, String filename, JProgressBar bar) //creates and populates the database
    {
        String newURL = new File(url).getParent()+"/"+filename+".db";
        newURL="jdbc:sqlite:"+newURL;               //convert the input URL into an sqlite database url

        try(Connection conn = DriverManager.getConnection(newURL))    //try, provided the connection is closed after trying
        {
            if(conn!=null)  //if the connection works,
            {
                Statement stmt = conn.createStatement();                        //create a new statement
                stmt.execute("CREATE TABLE IF NOT EXISTS parsed_entries (\n"    //the statement will create a 10-column table in the databse
                        + "A text,\n"
                        + "B text,\n"
                        + "C text,\n"
                        + "D text,\n"
                        + "E text,\n"
                        + "F text,\n"
                        + "G text,\n"
                        + "H text,\n"
                        + "I text,\n"
                        + "J text \n"                                           //all of them text entries named alphabetically
                        +");");
                stmt.close();               //and close up the statement
            }
        }
        catch (SQLException e){             //and report any SQLExceptions
            System.out.println("SWLException detected! Message: "+e.getMessage());
        }
        DatabaseThread task = new DatabaseThread(entries, newURL, bar); //then create an execute a thread to add to the database.
        task.execute();
    }
    public void addEntriesToDatabase(Connection conn, List<String[]> entries, String url) throws SQLException
    {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO parsed_entries VALUES(?,?,?,?,?,?,?,?,?,?)");
            int batchSize=0;                //declare variables, a prepared entry statement and a batch counter
            for(String[] entry : entries)
            {
                    for(int i=0; i<entry.length; i++)
                        {
                            stmt.setString(i+1, entry[i]);  //prepare batches of insert statements in groups of 100
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
            if(batchSize!=0)        //if there's still some un-sent entries after the batches of 100 are processed,
                stmt.executeBatch();    //send the excess entries.
            stmt.close();           //and close the statement.
        
    }
    public static void writeCSV(List<String[]> data, String url, String filename)
    {
        String newURL = new File(url).getParent()+"/"+filename+".csv";  //change the input url to represent the new CSV file
        try(CSVWriter writer = new CSVWriter (new FileWriter(newURL)))  //create the new display file
        {   
        Iterator iter = data.iterator();
        while(iter.hasNext())                       //for every item in the data list,
        {
            writer.writeNext((String[])iter.next());    //write the data as en entry in the .csv file
        }
        } catch (IOException ex) {  //report an IOException if the file fails to write.
            System.out.println("ERROR: Something went wrong while writing the bad.csv file. Error message: "+ex.getMessage());
        }
    }
    public void logData(int received, int successful, String url, String name)
    {
        try {
            String newURL = (new File(url).getParent())+"/"+name+".log";    //create the log file's URL,
            FileWriter writer = new FileWriter(newURL, false);              //create the file from the URL
            writer.write("Number of records received: "+received+"\n"       //and track the number of good and bad records.
            +"Number of records successful: "+successful+"\n"
            +"Number of records failed: "+(received-successful));
            writer.flush();                                                 //flush to make sure it's written,
        } catch (IOException ex) {
            System.out.println("ERROR: Something went wrong while writing the log file. Error message: "+ex.getMessage());
        }
    }
}