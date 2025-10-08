package com.example.jplanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import static com.example.jplanner.Planner.directory;
import static com.example.jplanner.Planner.DELIM;

import android.content.Context;

import androidx.compose.runtime.snapshots.SnapshotStateList;

public class FileManager {
    public static void readFile(Context context) {
        File dir = new File(context.getFilesDir(), "daily");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File myObj = new File(dir, "taskdata_" + Planner.zonedDate + ".txt");
        try {
            myObj.createNewFile();
            Scanner fileSC = new Scanner(myObj).useDelimiter("\n");
            while (fileSC.hasNext()) {
                Scanner lineSC = new Scanner(fileSC.next()).useDelimiter(DELIM);
                Planner.tasks.add(new Task(lineSC.next(),
                        lineSC.next(),
                        Boolean.parseBoolean(lineSC.next()),
                        Integer.parseInt(lineSC.next()),
                        Integer.parseInt(lineSC.next()),
                        lineSC.next()));
                lineSC.close();
            }
            fileSC.close();
        }
        catch (IOException e) {
            Planner.tasks.add(new Task(Planner.zonedDate + " failed :(", " ", 1, 2));
        }
    }
    
    public static void writeFile(Context context) {
        File dir = new File(context.getFilesDir(), "daily");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File myObj = new File(dir, "taskdata_" + Planner.zonedDate + ".txt");
        try {
            myObj.createNewFile();
            FileWriter myWriter = new FileWriter(dir + "/taskdata_" + Planner.zonedDate + ".txt");
            String out = "";
            for (Task t : Planner.tasks) {
                out += "\n";
                out += t.getName() + DELIM + t.getTag() + DELIM + t.isComplete() + DELIM
                        + t.getStart() + DELIM + t.getEnd() + DELIM + t.getNote();
            }
            
            myWriter.write(out);
            myWriter.close();
        } catch (IOException e) {
            Planner.tasks.add(new Task(Planner.zonedDate + " failed :(", " ", 1, 2));
            e.printStackTrace();
        }
    }
}
