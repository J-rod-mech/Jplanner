package com.example.jplanner;

import static com.example.jplanner.Planner.directory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class TagManager {
    final static String DEL_SUCCESS = "Tag %s removed.";
    final static String DEL_ERROR = "Tag %s does not exist.";
    
    public static boolean findTag(String tag) {
        File myObj = new File(directory + "/plannerdata/tagdata.txt");
        Scanner fileSC = null;

        try {
            fileSC = new Scanner(myObj);
            
            while (fileSC.hasNext()) {
                if (fileSC.next().equals(tag)) {
                    return true;
                }
            }
        }
        catch (Exception e) {
            //ignore
        }
        finally {
            if (fileSC != null) fileSC.close();
        }

        return false;
    }
    
    public static void printTagData() {
        File myObj = new File(directory + "/plannerdata/tagdata.txt");
        Scanner fileSC = null;

        try {
            fileSC = new Scanner(myObj).useDelimiter("\n");

            while (fileSC.hasNext()) {
                System.out.println(fileSC.next());
            }
        }
        catch (Exception e) {
            //ignore
        }
        finally {
            if (fileSC != null) fileSC.close();
        }

        System.out.println();
    }

    public static void writeTagData(String tag) {
        String out = "";
        File myObj = new File(directory + "/plannerdata/tagdata.txt");
        Scanner fileSC = null;

        try {
            fileSC = new Scanner(myObj).useDelimiter("\n");
            
            while (fileSC.hasNext()) {
                out += fileSC.next();
                if (fileSC.hasNext()) {
                    out += "\n";
                }
            }

            if (!tag.equals(" ")) {
                out += "\n" + tag;
            }
        }
        catch (FileNotFoundException e) {
            out += tag;
        }
        finally {
            if (fileSC != null) fileSC.close();
        }

        try {
            FileWriter myWriter = new FileWriter(directory + "/plannerdata/tagdata.txt");
            myWriter.write(out);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeTag(String tag) {
        String out = "";
        File myObj = new File(directory + "/plannerdata/tagdata.txt");
        Scanner fileSC = null;
        boolean del = false;

        try {
            fileSC = new Scanner(myObj);
            while (fileSC.hasNext()) {
                String next = fileSC.next();
                if (!next.equals(tag)) {
                    out += next;
                    if (fileSC.hasNext()) {
                        out+= "\n";
                    }
                }
                else {
                    del = true;
                    System.out.println(String.format(DEL_SUCCESS, tag));
                }
            }
        }
        catch (Exception e) {
            //ignore
        }

        try {
            FileWriter myWriter = new FileWriter(directory + "/plannerdata/tagdata.txt");
            myWriter.write(out);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!del) {
            System.out.println(String.format(DEL_ERROR, tag));
        }
    }
}
