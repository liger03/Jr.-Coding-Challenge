/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coding_challenge;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author john_
 */
public class ConversionCode 
{
    public static Vector<String[]> readCode(String uri) throws IOException, FileNotFoundException
    {
        Vector<String[]> storedData = new Vector<String[]>();
        BufferedReader reader = new BufferedReader(new FileReader(uri));
        String line=reader.readLine();
        if(line!=null)  //if there's at least one line in the CSV file,
            do
            {
                line=line.trim();   //trim off any whitespace before starting
                storedData.add(line.split(","));    //split it into tokens and store it in the output variable
                line=reader.readLine();     //and go to the next line
            }while(line !=null);            //until the next line is empty and we're at the end of the file
        return storedData;
    }
    public static void splitEntries(Vector<String[]> entries, Vector<String[]> good,  Vector<String[]> bad)
    {
        for (String[] entry: entries)   //for every entry in the list of entries,
        {
            Boolean complete=true;
            for(String word: entry)    //check each word in each entry
                if(word.equals(""))     //if any of the words are blank,
                    complete=false;     //the entry isn't complete.
            if(!complete)           //if it isn't,
            {
                bad.add(entry);     //then add it to the malformed entry list
            }
            else
                good.add(entry);    //otherwise, add it to the proper entry list
        }
    }
    public static void writeEntries(Vector<String[]> entries)
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
    
}
