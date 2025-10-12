package com.example.jplanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import static com.example.jplanner.Planner.myFormat;
import static com.example.jplanner.Planner.directory;
import static com.example.jplanner.Planner.DELIM;
import static com.example.jplanner.Planner.timeZone;

import android.content.Context;

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
            Planner.tasks.clear();
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

    public static ArrayList<ArrayList<Task>> upcomingList(Context context) {
        ArrayList<ArrayList<Task>> taskList = new ArrayList<ArrayList<Task>>();
        File dir = new File(context.getFilesDir(), "daily");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File[] directoryListing = dir.listFiles();
        Arrays.sort(directoryListing);
        if (directoryListing != null) {
            int lo = 0;
            int hi = directoryListing.length - 1;
            while (lo < hi) {
                int mid = lo + (hi - lo)/2;
                String date = directoryListing[mid].getName().substring(9,19);
                if (LocalDate.parse(date, myFormat).isBefore(ZonedDateTime.now(ZoneId.of(timeZone)).toLocalDate())) {
                    lo = mid + 1;
                }
                else {
                    hi = mid;
                }
            }
            for (int i = lo; i < directoryListing.length; i++) {
                ArrayList<Task> dayTasks = new ArrayList<>();
                try {
                    Scanner fileSC = new Scanner(directoryListing[i]).useDelimiter("\n");
                    dayTasks.add(new Task(directoryListing[i].getName().substring(9,19), " ", 0, 1));
                    while (fileSC.hasNext()) {
                        Scanner lineSC = new Scanner(fileSC.next()).useDelimiter(DELIM);
                        dayTasks.add(new Task(lineSC.next(),
                                lineSC.next(),
                                Boolean.parseBoolean(lineSC.next()),
                                Integer.parseInt(lineSC.next()),
                                Integer.parseInt(lineSC.next()),
                                lineSC.next()));
                        lineSC.close();
                    }
                    fileSC.close();
                }
                catch (Exception e) {
                    System.out.println("Could not find schedule.");
                }
                if (dayTasks.size() > 1) {
                    taskList.add(dayTasks);
                }
            }
        } else {
            System.out.println("Could not find schedules.");
        }

        return taskList;
    }
}
