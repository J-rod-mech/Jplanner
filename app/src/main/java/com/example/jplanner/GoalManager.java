package com.example.jplanner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import static com.example.jplanner.Planner.directory;
import static com.example.jplanner.Planner.DELIM;
import static com.example.jplanner.Planner.timeZone;
import static com.example.jplanner.Planner.myFormat;

public class GoalManager {
    final static String GOAL_SUCCESS = "Goal %s added.";
    final static String GOAL_FAIL = "Goal %s already exists.";
    final static String DEL_SUCCESS = "Goal %s removed.";
    final static String DEL_ERROR = "Goal %s does not exist.";

    public static void printGoalData() {
        File myObj = new File(directory + "/plannerdata/goaldata.txt");
        Scanner fileSC = null;
        Scanner taskSC = null;
        try {
            fileSC = new Scanner(myObj).useDelimiter("\n");

            while (fileSC.hasNext()) {
                taskSC = new Scanner(fileSC.next());
                String name = taskSC.next();
                int len = name.length();
                int freq = Integer.parseInt(taskSC.next());
                System.out.println(name.substring(2, len - 2) + " (every" + (freq == 1 ? "day": " " + freq + " days") + "):");
                String date = taskSC.next();
                String currStreak = taskSC.next();

                System.out.print("Current streak: ");
                if (ChronoUnit.DAYS.between(LocalDate.parse(date, myFormat),
                        ZonedDateTime.now(ZoneId.of(timeZone)).toLocalDate()) <= freq) {
                    System.out.println(currStreak);
                }
                else {
                    System.out.println(0);
                }
                //System.out.println("Longest streak: " + taskSC.next());
                //System.out.println("Hours completed: " + Double.parseDouble(taskSC.next()) / 4);
                if (!date.equals("01-01-0001")) {
                    System.out.println("Last completed: " + date);
                }
                if (fileSC.hasNext())
                    System.out.println();
                
                taskSC.close();
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

    public static void writeGoalData(String task, int newVal, int nFreq) {
        String out = "";
        File myObj = new File(directory + "/plannerdata/goaldata.txt");
        Scanner fileSC = null;
        Scanner taskSC = null;
        
        try {
            fileSC = new Scanner(myObj).useDelimiter(DELIM + task + DELIM);
            
            out += fileSC.next();
            taskSC = new Scanner(fileSC.next());

            if (newVal == -1) {
                System.out.println(String.format(GOAL_FAIL, task));
                System.out.println();
                return;
            }

            int freq = Integer.parseInt(taskSC.next());
            String date = taskSC.next();
            LocalDate prevDate = LocalDate.parse(date, myFormat);
            long timeDiff = ChronoUnit.DAYS.between(prevDate,
                    ZonedDateTime.now(ZoneId.of(timeZone)).toLocalDate());
            
            out += DELIM + task + DELIM + " " + freq + " " +
                    ZonedDateTime.now(ZoneId.of(timeZone)).toLocalDate().format(myFormat);

            int currStreak = Integer.parseInt(taskSC.next());
            if (timeDiff <= freq && timeDiff > 0) {
                currStreak++;
            }
            else if (timeDiff > freq) {
                currStreak = 1;
            }
            out += " " + currStreak;

            int longStreak = Math.max(Integer.parseInt(taskSC.next()), currStreak);
            out += " " + longStreak;
            
            newVal += Integer.parseInt(taskSC.next());

            out += " " + newVal;

            if (taskSC.hasNextLine()) taskSC.nextLine();
            while (taskSC.hasNextLine()) out += "\n" + taskSC.nextLine();
        }
        catch (Exception e) {
            if (newVal != -1) return;

            out = "\n";
            try {
                if (fileSC != null) fileSC.close();
                fileSC = new Scanner(myObj);
                if (fileSC.hasNextLine()) fileSC.nextLine();
                while (fileSC.hasNextLine()) {
                    out += fileSC.nextLine() + "\n";
                }
                out += "##" + task + "## " + nFreq + " 01-01-0001 0 0 0";
                System.out.println(String.format(GOAL_SUCCESS, task));
                System.out.println();
            }
            catch (Exception e2) {
                out += "##" + task + "## " + nFreq + " 01-01-0001 0 0 0";
                System.out.println(String.format(GOAL_SUCCESS, task));
                System.out.println();
            }
        }
        finally {
            if (taskSC != null) taskSC.close();
            if (fileSC != null) fileSC.close();
        }
        
        try {
            FileWriter myWriter = new FileWriter(directory + "/plannerdata/goaldata.txt");
            myWriter.write(out);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeGoal(String task) {
        String out = "";
        File myObj = new File(directory + "/plannerdata/goaldata.txt");
        Scanner fileSC = null;
        boolean del = false;

        try {
            fileSC = new Scanner(myObj);
            while (fileSC.hasNext()) {
                String goal = fileSC.next();
                if (goal.equals(DELIM + task + DELIM)) {
                    del = true;
                    fileSC.nextLine();
                }
                else {
                    out += "\n" + goal + fileSC.nextLine();
                }
            }
        }
        catch (Exception e) {
            //ignore
        }

        try {
            FileWriter myWriter = new FileWriter(directory + "/plannerdata/goaldata.txt");
            myWriter.write(out);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (del) {
            System.out.println(String.format(DEL_SUCCESS, task));
        }
        else {
            System.out.println(String.format(DEL_ERROR, task));
        }
    }
}
