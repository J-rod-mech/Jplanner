package com.example.jplanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import static com.example.jplanner.Planner.myFormat;
import static com.example.jplanner.Planner.DELIM;
import static com.example.jplanner.Planner.timeZone;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;

public class FileManager {

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
    public static void readFile(Context context, DocumentFile folder) {
        System.out.println("start read");
        String fileName = "taskdata_" + Planner.zonedDate + ".txt";
        DocumentFile targetFile = folder.findFile(fileName);
        if (targetFile == null || !targetFile.exists()) {
            targetFile = folder.createFile("text/plain", fileName);
            if (targetFile == null) {
                Log.e("SAF", "Failed to create file (read)");
                Log.d("SAF", "canWrite = " + folder.canWrite());
                Log.d("SAF", "isDirectory = " + folder.isDirectory());
                Log.d("SAF", "exists = " + folder.exists());
                return;
            }
            return;
        }

        try (InputStream is = context.getContentResolver().openInputStream(targetFile.getUri())) {
            Scanner fileSC = new Scanner(is).useDelimiter("\n");
            Planner.tasks.clear();
            while (fileSC.hasNext()) {
                Scanner lineSC = new Scanner(fileSC.next()).useDelimiter(DELIM);
                String name = lineSC.next();
                System.out.println(name);
                Planner.tasks.add(new Task(name,
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
            Log.d("SAF", "error reading from file");
            e.printStackTrace();
        }
    }
    
    public static void writeFile(Context context, DocumentFile folder) {
        String fileName = "taskdata_" + Planner.zonedDate + ".txt";
        DocumentFile targetFile = folder.findFile(fileName);

        if (targetFile == null || !targetFile.exists()) {
            targetFile = folder.createFile("text/plain", fileName);
            if (targetFile == null) {
                Log.e("SAF", "Failed to create file (write)");
                Log.d("SAF", "canWrite = " + folder.canWrite());
                Log.d("SAF", "isDirectory = " + folder.isDirectory());
                Log.d("SAF", "exists = " + folder.exists());
                return;
            }
        }

        System.out.println("writing...");
        try (OutputStream os = context.getContentResolver().openOutputStream(targetFile.getUri(), "wt")) {
            StringBuilder out = new StringBuilder();
            for (Task t : Planner.tasks) {
                out.append("\n").append(t.getName()).append(DELIM).append(t.getTag()).append(DELIM)
                        .append(t.isComplete()).append(DELIM).append(t.getStart()).append(DELIM)
                        .append(t.getEnd()).append(DELIM).append(t.getNote());
            }
            os.write(out.toString().getBytes());
            System.out.println("wrote: " + out.toString());
        }
        catch (IOException e) {
            StringWriter error = new StringWriter();
            e.printStackTrace(new PrintWriter(error));
        }
        System.out.println("end write");
    }

    public static ArrayList<ArrayList<Task>> upcomingList(Context context, DocumentFile folder) {
        ArrayList<ArrayList<Task>> taskList = new ArrayList<ArrayList<Task>>();
        if (folder == null || !folder.isDirectory()) return null;

        DocumentFile[] files = folder.listFiles();

        Arrays.sort(files, (a, b) -> {
            String nameA = a.getName();
            String nameB = b.getName();

            if (nameA == null && nameB == null) return 0;
            if (nameA == null) return -1;
            if (nameB == null) return 1;

            return nameA.compareToIgnoreCase(nameB);
        });

        if (files != null) {
            int lo = 0;
            int hi = files.length - 1;
            while (lo < hi) {
                int mid = lo + (hi - lo)/2;
                String date = files[mid].getName().substring(9,19);
                if (LocalDate.parse(date, myFormat).isBefore(ZonedDateTime.now(ZoneId.of(timeZone)).toLocalDate())) {
                    lo = mid + 1;
                }
                else {
                    hi = mid;
                }
            }
            for (int i = lo; i < files.length; i++) {
                ArrayList<Task> dayTasks = new ArrayList<>();
                try (InputStream is = context.getContentResolver().openInputStream(files[i].getUri())) {
                    Scanner fileSC = new Scanner(is).useDelimiter("\n");
                    dayTasks.add(new Task(files[i].getName().substring(9,19), " ", 0, 1));
                    Planner.tasks.clear();
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
                    Log.d("SAF", "error reading list");
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
