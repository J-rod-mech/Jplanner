package com.example.jplanner;

import android.content.Context;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import java.util.ArrayList;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.io.File;
import java.util.Arrays;
import androidx.compose.runtime.snapshots.SnapshotStateList;

public class Planner {
    //constants
    final static int HALF_HOURS = 12;
    final static int HALF_DIV = 2;
    final static int HUNDRED_DIV = 100;
    final static int FIFTEEN_DIV = 15;
    final static int MIN_HOUR = 0;
    final static int MAX_HOUR = 96;
    final static int MIN_TIME_REP = 100;
    final static int MAX_TIME_REP = 1245;
    final static int MAX_HOUR_TIME = 2400;
    final static int QUAD = 4;
    final static int HOUR_DIV = 60;
    final static int TENS_DIGIT = 10;
    final static int HOUR_OFFSET = 3;
    final static String ADD_SUCCESS = "%s added.";
    final static String REM_SUCCESS = "%s removed.";
    final static String TAG_REM_SUCCESS = "Tag %s removed from %s.";
    final static String MOVE_SUCCESS = "%s moved.";
    final static String NOTE_SUCCESS = "Note set to \"%s\".";
    final static String DATE_SUCCESS = "Date changed to %s.";
    final static String TICK_SUCCESS = "%s marked complete.";
    final static String HELP_MENU = "Choose from one of the following commands:\n"
            + "view [v]\n"
            + "add  [a]\n"
            + "rem  [r]\n"
            + "move [m]\n"
            + "note [n]\n"
            + "tick [t]\n"
            + "date [d]\n"
            + "list [l]\n"
            + "help [h]\n"
            + "quit [q]\n";
    final static String HELP_ADD = "add <task>: adds task at the end\n"
            + "add <task> <start> <end>: adds task to time slot\n"
            + "add <#> <tag>: creates tag\n"
            + "add <task> <#> <tag>: adds task with tag\n"
            + "add <task> <#> <tag> <start> <end>: adds task with tag to time slot\n";
    final static String HELP_REM = "rem <task>: removes all instances of a task\n"
            + "rem <task> <instance>: removes instance n of a task\n"
            + "rem <start> <end>: removes all tasks within time slot\n"
            + "rem <#> <tag>: removes tag\n"
            + "rem <task> <#> <tag>: removes tag from task\n";
    final static String HELP_VIEW = "view: displays schedule\n"
            + "view <task> <instance>: displays tag and note for instance n of a task\n";
    final static String HELP_MOVE = "move <task> <+/-> <time>: shifts all instances of a task\n"
            + "move <task> <instance> <+/-> <time>: shifts instance n of a task\n"
            + "move <task> <instance> <start> <end>: moves instance n of a task to time slot\n"
            + "move <start> <end> <+/-> <time>: shifts time slot\n";
    final static String HELP_NOTE = "note <task> <instance>: sets note for instance n of a task\n";
    final static String HELP_DATE = "date <date>: sets current working date\n"
            + "date <+/-> <days>: changes current working date by n amount of days\n";
    final static String HELP_LIST = "list: displays all current and future tasks\n"
            + "list <#>: displays all tags\n"
            + "list <#> <tag>: displays all current and future tasks with tag\n";
    final static String HELP_HELP = "help: displays list of commands\n"
            + "help <command>: displays uses of a command\n";
    final static String HELP_QUIT = "quit: quits program\n";
    final static String HELP_TICK = "tick <task>: marks all instances of a task complete\n"
            + "tick <task> <instace>: marks instance n of a task complete\n";
    final static String COMMAND_ERROR = "Invalid command.";
    final static String TIME_ERROR = "Invalid time slot.";
    final static String INC_ERROR = "Invalid time increment.";
    final static String DATE_ERROR = "Invalid date.";
    final static String WEEKDAY_ERROR = "Invalid day of the week.";
    final static String TASK_ERROR = "%s does not exist.";
    final static String BOUND_ERROR = "%s cannot be moved outside valid time bounds.";
    final static String TICK_ERROR = "%s already completed.";
    final static String REPEAT_ERROR = "Tag %s already exists";
    final static String TAG_SUCCESS = "Tag %s added.";
    final static String TAG_ERROR = "Tag %s does not exist.";
    final static String KEY_ERROR = "Cannot modify keyword %s.";
    final static String TAG_DEL_ERROR = "Tag %s does not exist.";
    final static String DAYS_ERROR = "Amount of days must be a positive integer.";
    final static String NOT_REM = "Nothing to remove.";
    final static String NOT_MOVE = "Nothing to move.";

    final static String DELIM = "##";
    final static String DELIM_ERROR = "Cannot include delimiter " + DELIM + ".";
    final static String KEY_TAG1 = "goal";
    final static String KEY_TAG2 = "habit";

    // custom parameters
    static String timeZone = "UTC-7";
    static String directory = "G:/My Drive";

    static DateTimeFormatter myFormat = DateTimeFormatter.ofPattern("MM-dd-uuuu").withResolverStyle(ResolverStyle.STRICT);
    static String zonedDate = ZonedDateTime.now(ZoneId.of(timeZone)).format(myFormat);
    static SnapshotStateList<Task> tasks = MainActivity.list.getTasks();

    public static Task testFunc1(String str) {
        return new Task(str, "", 1, 2);
    }
    public static void testFunc2(Context context) {
        tasks.add(new Task("test", " ", 1, 2));
        File dir = new File(context.getFilesDir(), "daily");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            File file = new File(dir, "bloop.txt");
            if (file.createNewFile()) {
                tasks.add(new Task("succ", " ", 1, 2));
            }
            else {
                tasks.add(new Task("fail", " ", 1, 2));
            }
        }
        catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            tasks.add(new Task(errors.toString(), " ", 0, 3));
        }
    }
    public static boolean inputEquals(String in, String command, int amt) {
        if (command.length() < amt) {
            return false;
        }

        if (command.toLowerCase().equals(in) ||
                command.substring(0, amt).toLowerCase().equals(in)) {
            return true;
        }

        return false;
    }

    public static int convertTime(String time) {
        int len = time.length();
        int slot = -1;
        if (len > 1 && time.substring(len - 2).toLowerCase().equals("am")) {
            time = time.substring(0, len - 2);
            try {
                slot = Integer.parseInt(time);
                if (slot < MIN_TIME_REP || slot > MAX_TIME_REP) {
                    return -1;
                }
                slot %= MAX_HOUR_TIME / HALF_DIV;
            }
            catch (NumberFormatException e) {
                return -1;
            }
        }
        else if (len > 1 && time.substring(len - 2).toLowerCase().equals("pm")) {
            time = time.substring(0, len - 2);
            try {
                slot = Integer.parseInt(time);
                if (slot < MIN_TIME_REP || slot > MAX_TIME_REP) {
                    return -1;
                }
                slot = slot % (MAX_HOUR_TIME / HALF_DIV)
                        + MAX_HOUR_TIME / HALF_DIV;
            }
            catch (NumberFormatException e) {
                return -1;
            }
        }
        else {
            try {
                slot = Integer.parseInt(time);
            }
            catch (NumberFormatException e) {
                return -1;
            }
        }

        if (slot < MIN_HOUR || slot > MAX_HOUR_TIME ||
                slot % HUNDRED_DIV % FIFTEEN_DIV != 0 ||
                slot % HUNDRED_DIV / HOUR_DIV != 0) {
                    return -1;
            }

        return slot / HUNDRED_DIV * QUAD + slot % HUNDRED_DIV / FIFTEEN_DIV;
    }

    public static String convertNumToTime(int num) {
        String out = "";
        int hour = (num % (HALF_HOURS * HOUR_DIV)) / HOUR_DIV;
        int min = num % HOUR_DIV;
        out += (hour > 0 ? hour: 12) + ":" + String.format("%02d", min);
        if (num >= HALF_HOURS * HOUR_DIV) {
            out += " PM";
        }
        else {
            out += " AM";
        }
        return out;
    }

    public static int convertInc(String time) {
        int slot = -1;
        try {
            slot = Integer.parseInt(time);
        }
        catch (NumberFormatException e) {
            return -1;
        }

        if (slot < MIN_HOUR || slot > MAX_HOUR_TIME ||
                slot % HUNDRED_DIV % FIFTEEN_DIV != 0 ||
                slot % HUNDRED_DIV / HOUR_DIV != 0) {
                    return -1;
            }

        return slot / HUNDRED_DIV * QUAD + slot % HUNDRED_DIV / FIFTEEN_DIV;
    }

    public static int getTaskIdx(String name, int start, int end) {
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            if (t.getName().equals(name) && t.getStart() == start && t.getEnd() == end) {
                return i;
            }
        }

        return -1;
    }

    public static Task newTask(String name, String tag, int start, int end, String note) {
        return new Task(name, tag, false, start, end, note);
    }

    public static void insertTask(String name, String tag, int start, int end) {
        int lo = 0;
        int hi = tasks.size() - 1;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (tasks.get(mid).getStart() > start) {
                hi = mid;
            }
            else if (tasks.get(mid).getStart() <= start) {
                lo = mid + 1;
            }
        }
        if (lo < tasks.size() && tasks.get(lo).getStart() <= start) {
            lo++;
        }
        tasks.add(lo, new Task(name, tag, start, end));
    }

    public static void insertTask(Task task) {

        int lo = 0;
        int hi = tasks.size() - 1;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (tasks.get(mid).getStart() > task.getStart()) {
                hi = mid;
            }
            else if (tasks.get(mid).getStart() <= task.getStart()) {
                lo = mid + 1;
            }
        }
        if (lo < tasks.size() && tasks.get(lo).getStart() <= task.getStart()) {
            lo++;
        }
        tasks.add(lo, task);
    }

    public static void replaceTask(
            int idx,
            String newName,
            String newTag,
            int newStart,
            int newEnd,
            String newNote
    ) {
        tasks.remove(idx);
        int lo = 0;
        int hi = tasks.size() - 1;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (tasks.get(mid).getStart() > newStart) {
                hi = mid;
            }
            else if (tasks.get(mid).getStart() <= newStart) {
                lo = mid + 1;
            }
        }
        if (lo < tasks.size() && tasks.get(lo).getStart() <= newStart) {
            lo++;
        }
        tasks.add(lo, new Task(newName, newTag, false, newStart, newEnd, newNote));
    }

    public static void printSchedule() {
        @SuppressWarnings("unchecked")
        ArrayList<String>[] schedule = new ArrayList[MAX_HOUR + 1];
        for (Task t : tasks) {
            for (int i = t.getStart(); i < t.getEnd(); i++) {
                if (schedule[i] == null) schedule[i] = new ArrayList<String>();
                schedule[i].add(t.getName() + (!t.getNote().equals(" ") ? "*" : ""));
            }
        }

        boolean isEmpty = true;

        String day = LocalDate.parse(zonedDate, myFormat).getDayOfWeek().toString();
        System.out.print(day.charAt(0) + day.substring(1).toLowerCase() + ", ");
        System.out.println(zonedDate + ":");
        for (int i = MIN_HOUR; i <= MAX_HOUR; i++) {
            int j = MAX_HOUR - i;
            if ((schedule[i] != null) && (i == MIN_HOUR || !(schedule[i].equals
                    (schedule[i - 1]))) || i > MIN_HOUR && schedule[i - 1]
                    != null && !schedule[i - 1].equals(schedule[i])) {
                if ((i + 44) % 48 / 36 == 0)
                    System.out.print(" ");
                System.out.print((HALF_HOURS - (HOUR_OFFSET + j) / QUAD
                        % HALF_HOURS) + ":" + (i * FIFTEEN_DIV % HOUR_DIV));
                if (i % QUAD == 0)
                    System.out.print("0");
                if (i / HALF_HOURS / QUAD % 2 == 0)
                    System.out.print(" A");
                else
                    System.out.print(" P");
                System.out.println("M");

                if (schedule[i] != null) {
                    isEmpty = false;
                    System.out.print("  |  ");
                    for (int k = 0; k < schedule[i].size(); k++) {
                        if (k > 0)
                            System.out.print(", ");
                        System.out.print(schedule[i].get(k));
                    }
                }
                System.out.println();
            }
        }
        if (isEmpty)
            System.out.println();
    }

    public static void printList(String tag) {
        String list = "";
        File dir = new File(directory + "/plannerdata/daily");
        File[] directoryListing = dir.listFiles();
        Arrays.sort(directoryListing);
        if (directoryListing != null) {
            for (int i = 0; i < directoryListing.length; i++) {
                boolean match = false;
                ArrayList<String> taskList = new ArrayList<String>();
                String date = directoryListing[i].getName().substring(9,19);
                if (!LocalDate.parse(date, myFormat).isBefore(ZonedDateTime.now(ZoneId.of(timeZone)).toLocalDate())) {
                    try {
                        Scanner fileSC = new Scanner(directoryListing[i]).useDelimiter("\n");
                        while (fileSC.hasNext()) {
                            Scanner lineSC = new Scanner(fileSC.next()).useDelimiter(DELIM);
                            String in = lineSC.next();
                            String in2 = lineSC.next();
                            if (!Boolean.parseBoolean(lineSC.next()) && !taskList.contains(in) && (tag == null || tag.equals(in2))) {
                                list += "[ ] " + date + ": " + in +
                                        (!in2.equals(" ") ? " # " + in2 : "") + "\n";
                                taskList.add(in + (!in2.equals(" ") ? DELIM + in2 : ""));
                                match = true;
                            }
                            lineSC.close();
                        }
                        fileSC.close();
                    }
                    catch (Exception e) {
                        System.out.println("Could not find schedule.");
                    }

                    try {
                        Scanner fileSC = new Scanner(directoryListing[i]).useDelimiter("\n");
                        while (fileSC.hasNext()) {
                            Scanner lineSC = new Scanner(fileSC.next()).useDelimiter(DELIM);
                            String in = lineSC.next();
                            String in2 = lineSC.next();
                            if (Boolean.parseBoolean(lineSC.next()) && !taskList.contains(in) && (tag == null || tag.equals(in2))) {
                                list += "[X] " + date + ": " + in +
                                        (!in2.equals(" ") ? " # " + in2 : "") + "\n";
                                taskList.add(in + (!in2.equals(" ") ? DELIM + in2 : ""));
                                match = true;
                            }
                            lineSC.close();
                        }
                        fileSC.close();
                    }
                    catch (Exception e) {
                        System.out.println("Could not find schedule.");
                    }

                    if (taskList.size() > 0 && match) {
                        list += "\n";
                    }
                }
            }
        } else {
            System.out.println("Could not find schedules.");
        }

        System.out.print(list);
    }

    /*public static void main(String[] args) {
        HabitManager.transferHabits();
        System.out.println(HELP_MENU);
        
        Scanner sc1 = new Scanner(System.in).useDelimiter(System.lineSeparator());
        while (sc1.hasNext()) {
            String in0 = sc1.next();
            Scanner sc2 = new Scanner(in0);

            //quit: quits program
            if (in0.toLowerCase().equals("quit") || in0.toLowerCase().equals("q")) {
                sc2.close();
                sc1.close();
                break;
            }
            
            while (true) {
                if (!sc2.hasNext()) {
                    break;
                }
                String in1 = sc2.next();
                if (inputEquals(in1, "add", 1)) {
                    if (!sc2.hasNext()) {
                        System.out.println(COMMAND_ERROR);
                        System.out.println();
                        break;
                    }
                    
                    //add <task>: adds task at the end
                    String name = sc2.next();
                    if (!sc2.hasNext()) {
                        if (name.contains(DELIM)) {
                            System.out.println(DELIM_ERROR);
                        }
                        else {
                            insertTask(name, " ", MAX_HOUR - 1, MAX_HOUR);
                            System.out.println(String.format(ADD_SUCCESS, name));
                        }
                        
                        System.out.println();
                        break;
                    }

                    String in2 = sc2.next();
                    int start = convertTime(in2);

                    //add <#> <tag>: creates tag
                    if (!sc2.hasNext()) {
                        if (name.equals("#")) {
                            if (in2.contains(DELIM)) {
                                System.out.println(DELIM_ERROR);
                                System.out.println();
                                break;
                            }
                            if (in2.equals(KEY_TAG1) || in2.equals(KEY_TAG2)) {
                                System.out.println(String.format(KEY_ERROR, in2));
                            }
                            else if (!TagManager.findTag(in2)) {
                                TagManager.writeTagData(in2);
                                System.out.println(String.format(TAG_SUCCESS, in2));
                            }
                            else {
                                System.out.println(String.format(REPEAT_ERROR, in2));
                            }
                        }
                        else {
                            System.out.println(COMMAND_ERROR);
                        }
                        System.out.println();
                        break;
                    }

                    String in3 = sc2.next();
                    int end = convertTime(in3);
                    if (!sc2.hasNext()) {
                        //add <task> <#> <tag>: adds task with tag
                        if (name.contains(DELIM)) {
                            System.out.println(DELIM_ERROR);
                            System.out.println();
                            break;
                        }
                        
                        if (in2.equals("#")) {
                            if (in3.toLowerCase().equals(KEY_TAG1)) {
                                sc2.close();
                                Scanner sc3 = null;
                                System.out.println("Set frequency for goal (days): ");
                                int freq;
                                try {
                                    sc3 = new Scanner(sc1.next()).useDelimiter(System.lineSeparator());
                                    if (!sc3.hasNext()) {
                                        System.out.println(COMMAND_ERROR);
                                        System.out.println();
                                        sc3.close();
                                        break;
                                    }
                                    try {
                                        freq = Integer.parseInt(sc3.next());
                                        if (freq <= 0) {
                                            System.out.println(DAYS_ERROR);
                                            System.out.println();
                                            sc3.close();
                                            break;
                                        }
                                    }
                                    catch (Exception e) {
                                        System.out.println(DAYS_ERROR);
                                        System.out.println();
                                        sc3.close();
                                        break;
                                    }
                                }
                                finally {
                                    if (sc3 != null) {
                                        sc3.close();
                                    }
                                }
                                GoalManager.writeGoalData(name, -1, freq);
                                break;
                            }
                            else if (in3.toLowerCase().equals(KEY_TAG2)) {
                                sc2.close();
                                Scanner sc3 = null;
                                try {
                                    System.out.println("Set days for habit: ");
                                    sc3 = new Scanner(sc1.next());
                                    char sched[] = "0000000".toCharArray();
                                    boolean breakOut = false;

                                    if (!sc3.hasNext()) {
                                        System.out.println(COMMAND_ERROR);
                                        System.out.println();
                                        sc3.close();
                                        break;
                                    }
                                    while (sc3.hasNext()) {
                                        String weekday = sc3.next();
                                        if (inputEquals(weekday, "monday", 3)) {
                                            sched[0] = '1';
                                        }
                                        else if (inputEquals(weekday, "tuesday", 3)) {
                                            sched[1] = '1';
                                        }
                                        else if (inputEquals( weekday,"wednesday", 3)) {
                                            sched[2] = '1';
                                        }
                                        else if (inputEquals(weekday, "thursday", 3)) {
                                            sched[3] = '1';
                                        }
                                        else if (inputEquals(weekday, "friday", 3)) {
                                            sched[4] = '1';
                                        }
                                        else if (inputEquals(weekday, "saturday", 3)) {
                                            sched[5] = '1';
                                        }
                                        else if (inputEquals(weekday, "sunday", 3)) {
                                            sched[6] = '1';
                                        }
                                        else {
                                            sc3.close();
                                            System.out.println(WEEKDAY_ERROR);
                                            System.out.println();
                                            breakOut = true;
                                            break;
                                        }
                                    }
                                    if (breakOut)
                                        break;
                                    sc3.close();

                                    System.out.println("Set tag for habit (optional): ");
                                    sc3 = new Scanner(sc1.next());
                                    
                                    String tag = " ";
                                    if (sc3.hasNext()) {
                                        String temp = sc3.next();
                                        if (TagManager.findTag(temp)) {
                                            tag = temp;
                                        }
                                        else {
                                            System.out.println(String.format(TAG_ERROR, temp));
                                            System.out.println();
                                            break;
                                        }
                                    }
                                    sc3.close();

                                    System.out.println("Set note for habit (optional): ");
                                    sc3 = new Scanner(sc1.next()).useDelimiter(System.lineSeparator());
                                    String note = " ";
                                    if (sc3.hasNext())
                                        note = sc3.next();
                                    sc3.close();

                                    HabitManager.addHabit(name, tag, new String(sched),
                                            MAX_HOUR - 1, MAX_HOUR, note);
                                    System.out.println();
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                    System.out.println(COMMAND_ERROR);
                                    System.out.println();
                                }
                                finally {
                                    if (sc3 != null) {
                                        sc3.close();
                                    }
                                }
                            }
                            else {
                                if (TagManager.findTag(in3)) {
                                    insertTask(name, in3, MAX_HOUR - 1, MAX_HOUR);
                                    System.out.println(String.format(ADD_SUCCESS, name));
                                }
                                else {
                                    System.out.println(String.format(TAG_ERROR, in3));
                                }
                                System.out.println();
                                break;
                            }
                            break;
                        }

                        int len = in3.length();
                        if (len > 1 && in3.substring(len - 2).toLowerCase().
                                equals("am") && end == 0) {
                            end = MAX_HOUR;
                        }

                        //add <task> <start> <end>: adds task to time slot
                        if (end > start && start >= 0) {
                            insertTask(name, " ", start, end);
                            System.out.println(String.format(ADD_SUCCESS, name));
                            System.out.println();
                            break;
                        }
                        else {
                            System.out.println(TIME_ERROR);
                            System.out.println();
                            break;
                        }
                    }

                    start = convertTime(sc2.next());
                    if (!sc2.hasNext()) {
                        System.out.println(COMMAND_ERROR);
                        System.out.println();
                        break;
                    }

                    end = convertTime(sc2.next());
                    if (sc2.hasNext()) {
                        System.out.println(COMMAND_ERROR);
                        System.out.println();
                        break;
                    }
                    
                    //add <task> <#> <tag> <start> <end>: adds task with tag to time slot
                    if (name.contains(DELIM)) {
                        System.out.println(DELIM_ERROR);
                        System.out.println();
                        break;
                    }
                    
                    if (!in2.equals("#")) {
                        System.out.println(COMMAND_ERROR);
                        System.out.println();
                        break;
                    }

                    if (in3.toLowerCase().equals(KEY_TAG1)) {
                        sc2.close();
                        Scanner sc3 = null;
                        System.out.println("Set frequency for goal (days): ");
                        int freq;
                        try {
                            sc3 = new Scanner(sc1.next()).useDelimiter(System.lineSeparator());
                            if (!sc3.hasNext()) {
                                System.out.println(COMMAND_ERROR);
                                System.out.println();
                                sc3.close();
                                break;
                            }
                            try {
                                freq = Integer.parseInt(sc3.next());
                                if (freq <= 0) {
                                    System.out.println(DAYS_ERROR);
                                    System.out.println();
                                    sc3.close();
                                    break;
                                }
                            }
                            catch (Exception e) {
                                System.out.println(DAYS_ERROR);
                                System.out.println();
                                sc3.close();
                                break;
                            }
                        }
                        finally {
                            if (sc3 != null) {
                                sc3.close();
                            }
                        }
                        GoalManager.writeGoalData(name, -1, freq);
                        break;
                    }
                    else if (in3.toLowerCase().equals(KEY_TAG2)) {
                        sc2.close();

                        if (end == 0) {
                            end = MAX_HOUR;
                        }
                        if (end <= start || start < 0) {
                            System.out.println(TIME_ERROR);
                            System.out.println();
                            break;
                        }

                        Scanner sc3 = null;
                        try {
                            System.out.println("Set days for habit: ");
                            sc3 = new Scanner(sc1.next());
                            char sched[] = "0000000".toCharArray();
                            boolean breakOut = false;

                            if (!sc3.hasNext()) {
                                System.out.println(COMMAND_ERROR);
                                System.out.println();
                                sc3.close();
                                break;
                            }
                            while (sc3.hasNext()) {
                                String weekday = sc3.next();
                                if (inputEquals(weekday, "monday", 3)) {
                                    sched[0] = '1';
                                }
                                else if (inputEquals(weekday, "tuesday", 3)) {
                                    sched[1] = '1';
                                }
                                else if (inputEquals( weekday,"wednesday", 3)) {
                                    sched[2] = '1';
                                }
                                else if (inputEquals(weekday, "thursday", 3)) {
                                    sched[3] = '1';
                                }
                                else if (inputEquals(weekday, "friday", 3)) {
                                    sched[4] = '1';
                                }
                                else if (inputEquals(weekday, "saturday", 3)) {
                                    sched[5] = '1';
                                }
                                else if (inputEquals(weekday, "sunday", 3)) {
                                    sched[6] = '1';
                                }
                                else {
                                    sc3.close();
                                    System.out.println(WEEKDAY_ERROR);
                                    System.out.println();
                                    breakOut = true;
                                    break;
                                }
                            }
                            if (breakOut)
                                break;
                            sc3.close();

                            System.out.println("Set tag for habit (optional): ");
                            sc3 = new Scanner(sc1.next());
                            
                            String tag = " ";
                            if (sc3.hasNext()) {
                                String temp = sc3.next();
                                if (TagManager.findTag(temp)) {
                                    tag = temp;
                                }
                                else {
                                    System.out.println(String.format(TAG_ERROR, temp));
                                    System.out.println();
                                    break;
                                }
                            }
                            sc3.close();

                            System.out.println("Set note for habit (optional): ");
                            sc3 = new Scanner(sc1.next()).useDelimiter(System.lineSeparator());
                            String note = " ";
                            if (sc3.hasNext())
                                note = sc3.next();
                            sc3.close();

                            HabitManager.addHabit(name, tag, new String(sched),
                                    start, end, note);
                            System.out.println();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            System.out.println(COMMAND_ERROR);
                            System.out.println();
                        }
                        finally {
                            if (sc3 != null) {
                                sc3.close();
                            }
                        }
                        break;
                    }
                    else if (!TagManager.findTag(in3)) {
                        System.out.println(String.format(TAG_ERROR, in3));
                        System.out.println();
                        break;
                    }

                    if (end == 0) {
                        end = MAX_HOUR;
                    }
                    if (end > start && start >= 0) {
                        insertTask(name, in3, start, end);
                        System.out.println(String.format(ADD_SUCCESS, name));
                        System.out.println();
                        break;
                    }
                    else {
                        System.out.println(TIME_ERROR);
                        System.out.println();
                        break;
                    }
                }
                else if (inputEquals(in1, "rem", 1)) {
                    if (!sc2.hasNext()) {
                        System.out.println(COMMAND_ERROR);
                        System.out.println();
                        break;
                    }
                    String in2 = sc2.next();

                    //rem <task>: removes all instances of a task
                    if (!sc2.hasNext()) {
                        if (getTaskIdx(in2, "1") == -1) {
                            System.out.println(String.format(TASK_ERROR, in2));
                            System.out.println();
                            break;
                        }

                        int n = -1;
                        String lTask = DELIM;
                        
                        for (int i = 0; i < tasks.size(); i++) {
                            if (tasks.get(i).getName().equals(lTask)) {
                                n++;
                            }
                            else {
                                n = 1;
                            }
                            lTask = tasks.get(i).getName();

                            if (tasks.get(i).getName().equals(in2)) {
                                tasks.remove(i);
                                i--;
                                System.out.println(String.format(REM_SUCCESS, in2 + " " + n));
                            }
                        }
                        
                        System.out.println();
                        break;
                    }
                    
                    String in3 = sc2.next();
                    if (!sc2.hasNext()) {
                        int n = -1;
                        try {
                            n = Integer.parseInt(in3);
                        }
                        catch (NumberFormatException e) {
                            //ignore
                        }
                        int start = convertTime(in2);
                        int end = convertTime(in3);
    
                        if (end == 0) {
                            end = MAX_HOUR;
                        }
    
                        //rem <#> <tag>: removes tag
                        if (in2.equals("#")) {
                            if (in3.equals(KEY_TAG1) || in3.equals(KEY_TAG2)) {
                                System.out.println(String.format(KEY_ERROR, in3));
                            }
                            else {
                                TagManager.removeTag(in3);
                            }
                            System.out.println();
                            break;
                        }
    
                        //rem <task> <instance>: removes instance n of a task
                        if (getTaskIdx(in2, in3) != -1) {
                            for (int i = 0; i < tasks.size(); i++) {
                                if (tasks.get(i).getName().equals(in2)) {
                                    n--;
                                    if (n == 0) {
                                        tasks.remove(i);
                                        break;
                                    }
                                }
                            }
                            System.out.println(String.format(REM_SUCCESS, in2 + " " + in3));
                        }
    
                        //rem <start> <end>: removes all tasks within time slot
                        else if (start >= 0) {                    
                            if (end > start) {
                                boolean found = false;
                                String lTask = DELIM;
                                int m = -1;

                                for (int i = 0; i < tasks.size(); i++) {
                                    if (tasks.get(i).getName().equals(lTask)) {
                                        m++;
                                    }
                                    else {
                                        m = 1;
                                        lTask = tasks.get(i).getName();
                                    }
                                    
                                    if (start < tasks.get(i).getEnd() &&
                                            end > tasks.get(i).getStart()) {
                                        found = true;
                                        System.out.println(String.format(REM_SUCCESS, tasks.get(i).getName() + " " + m));
                                        tasks.remove(i);
                                        i--;
                                    }
                                }

                                if (!found)
                                    System.out.println(NOT_REM);
                            }
                            else {
                                System.out.println(TIME_ERROR);
                            }
                        }
                        else {
                            System.out.println(String.format(TASK_ERROR, in2 + " " + in3));
                        }

                        System.out.println();
                        break;
                    }
                    
                    //rem <task> <#> <tag>: removes tag from task
                    String in4 = sc2.next();
                    if (sc2.hasNext()) {
                        System.out.println(COMMAND_ERROR);
                        System.out.println();
                        break;
                    }

                    if (!in3.equals("#")) {
                        System.out.println(COMMAND_ERROR);
                        System.out.println();
                        break;
                    }

                    if (in4.equals(KEY_TAG1)) {
                        GoalManager.removeGoal(in2);
                    }
                    else if (in4.equals(KEY_TAG2)) {
                        HabitManager.removeHabit(in2);
                    }
                    else if (TagManager.findTag(in4)) {
                        if (getTaskIdx(in2, "1") == -1) {
                            System.out.println(String.format(TASK_ERROR, in4));
                        }
                        else {
                            for (int i = 0; i < tasks.size(); i++) {
                                if (tasks.get(i).getName().equals(in2)) {
                                    tasks.get(i).setTag(" ");
                                }
                            }
                            System.out.println(String.format(TAG_REM_SUCCESS, in4, in2));
                        }
                    }
                    else {
                        System.out.println(String.format(TAG_ERROR, in4));
                    }

                    System.out.println();
                    break;
                }

                //view: displays schedule
                else if (inputEquals(in1, "view", 1)) {
                    if (!sc2.hasNext()) {
                        printSchedule();
                        break;
                    }

                    String in2 = sc2.next();
                    if (!sc2.hasNext()) {
                        System.out.println(COMMAND_ERROR);
                        System.out.println();
                        break;
                    }

                    String in3 = sc2.next();
                    if (sc2.hasNext()) {
                        System.out.println(COMMAND_ERROR);
                        System.out.println();
                        break;
                    }

                    //view <task> <instance>: displays tag and note for instance n of a task
                    
                    int idx = getTaskIdx(in2, in3);
                    if (idx == -1) {
                        System.out.println(String.format(TASK_ERROR, in2 + " " + in3));
                        System.out.println();
                        break;
                    }

                    Task task = tasks.get(idx);
                    System.out.print(in2);
                    if (!task.getTag().equals(" "))
                        System.out.print(" # " + task.getTag());
                    System.out.println(":");
                    System.out.println("\"" + task.getNote() + "\"");
                    System.out.println();
                    break;
                }

                //help: displays list of commands
                else if (inputEquals(in1, "help", 1)) {
                    if (!sc2.hasNext()) {
                        System.out.println(HELP_MENU);
                        break;
                    }
                    String in2 = sc2.next();

                    if (sc2.hasNext()) {
                        System.out.println(COMMAND_ERROR);
                        System.out.println();
                        break;
                    }
                    
                    //help <command>: displays uses of a command
                    if (inputEquals(in2, "view", 1)) {
                        System.out.println(HELP_VIEW);
                    }
                    else if (inputEquals(in2, "add", 1)) {
                        System.out.println(HELP_ADD);
                    }
                    else if (inputEquals(in2, "rem", 1)) {
                        System.out.println(HELP_REM);
                    }
                    else if (inputEquals(in2, "move", 1)) {
                        System.out.println(HELP_MOVE);
                    }
                    else if (inputEquals(in2, "note", 1)) {
                        System.out.println(HELP_NOTE);
                    }
                    else if (inputEquals(in2, "date", 1)) {
                        System.out.println(HELP_DATE);
                    }
                    else if (inputEquals(in2, "list", 1)) {
                        System.out.println(HELP_LIST);
                    }
                    else if (inputEquals(in2, "help", 1)) {
                        System.out.println(HELP_HELP);
                    }
                    else if (inputEquals(in2, "quit", 1)) {
                        System.out.println(HELP_QUIT);
                    }
                    else if (inputEquals(in2, "tick", 1)) {
                        System.out.println(HELP_TICK);
                    }
                    else {
                        System.out.println(COMMAND_ERROR);
                        System.out.println();
                    }

                    break;
                }
                else if (inputEquals(in1, "list", 1)) {
                    //list: displays all current and future tasks
                    if (!sc2.hasNext()) {
                        printList(null);
                        break;
                    }
                    
                    String in2 = sc2.next();
                    if (!sc2.hasNext()) {
                        //list <#>: displays all tags
                        if (in2.equals("#")) {
                            System.out.println(KEY_TAG1);
                            System.out.println(KEY_TAG2);
                            TagManager.printTagData();
                        }
                        else {
                            System.out.println(COMMAND_ERROR);
                            System.out.println();
                        }
                        break;
                    }

                    String in3 = sc2.next();
                    if (sc2.hasNext() || !in2.equals("#")) {
                        System.out.println(COMMAND_ERROR);
                        System.out.println();
                        break;
                    }
                    
                    //list <#> <tag>: displays all current and future tasks with tag
                    if (in3.equals(KEY_TAG1)) {
                        GoalManager.printGoalData();
                    }
                    else if (in3.equals(KEY_TAG2)) {
                        HabitManager.printHabitData();
                    }
                    else if (TagManager.findTag(in3)) {
                        printList(in3);
                    }
                    else {
                        System.out.println(String.format(TAG_ERROR, in3));
                        System.out.println();
                    }

                    break;
                }

                //date <date>: sets current working date
                else if (inputEquals(in1, "date", 1)) {
                    if (!sc2.hasNext()) {
                        System.out.println(COMMAND_ERROR);
                        System.out.println();
                        break;
                    }
                    String in2 = sc2.next();
                    
                    if (!sc2.hasNext()) {
                        try {
                            if (in2.length() == 10) {
                                LocalDate.parse(in2, myFormat);
                                zonedDate = in2;
                            }
                            else if (in2.length() == 5) {
                                LocalDate.parse(in2 + zonedDate.substring(5), myFormat);
                                zonedDate = in2 + zonedDate.substring(5);
                            }
                            else if (in2.length() == 2) {
                                LocalDate.parse(zonedDate.substring(0, 3) + in2 + zonedDate.substring(5), myFormat);
                                zonedDate = zonedDate.substring(0, 3) + in2 + zonedDate.substring(5);
                            }
                            else {
                                System.out.println(DATE_ERROR);
                                System.out.println();
                                break;
                            }
                            FileManager.readFile();
                            String day = LocalDate.parse(zonedDate, myFormat).getDayOfWeek().toString();
                            System.out.println(String.format(DATE_SUCCESS,
                                    day.charAt(0) + day.substring(1).toLowerCase() + ", " + zonedDate));
                            System.out.println();
                        }
                        catch(Exception e) {
                            System.out.println(DATE_ERROR);
                            System.out.println();
                            break;
                        }
                        break;
                    }
                    
                    //date <+/-> <days>: changes current working date by n amount of days
                    String in3 = sc2.next();
                    if (sc2.hasNext() || !(in2.equals("+") || in2.equals("-"))) {
                        System.out.println(COMMAND_ERROR);
                        System.out.println();
                        break;
                    }

                    try {
                        int temp = Integer.parseInt(in3);
                        if (temp < 1)
                            throw new NumberFormatException();
                    }
                    catch (NumberFormatException e) {
                        System.out.println(DAYS_ERROR);
                        System.out.println();
                        break;
                    }

                    int mult = 1;
                    if (in2.equals("-"))
                        mult = -1;
                    zonedDate = LocalDate.parse(zonedDate, myFormat).plusDays(Integer.parseInt(in3) * mult).format(myFormat);
                    FileManager.readFile();

                    String day = LocalDate.parse(zonedDate, myFormat).getDayOfWeek().toString();
                    System.out.println(String.format(DATE_SUCCESS,
                            day.charAt(0) + day.substring(1).toLowerCase() + ", " + zonedDate));
                    System.out.println();
                    break;
                }
                else if (inputEquals(in1, "move", 1)) {
                    if (!sc2.hasNext()) {
                        System.out.println(COMMAND_ERROR);
                        System.out.println();
                        break;
                    }

                    String in2 = sc2.next();
                    if (!sc2.hasNext()) {
                        System.out.println(COMMAND_ERROR);
                        System.out.println();
                        break;
                    }

                    String in3 = sc2.next();
                    if (!sc2.hasNext()) {
                        System.out.println(COMMAND_ERROR);
                        System.out.println();
                        break;
                    }

                    String in4 = sc2.next();
                    
                    //move <task> <+/-> <time>: shifts all instances of a task
                    if (!sc2.hasNext()) { //3 args
                        if (getTaskIdx(in2, "1") == -1) {
                            System.out.println(String.format(TASK_ERROR, in2));
                            System.out.println();
                            break;
                        }
                        else if (!in3.equals("+") && !in3.equals("-")) {
                            System.out.println(COMMAND_ERROR);
                            System.out.println();
                            break;
                        }

                        int mult = 1;
                        if (in3.equals("-"))
                            mult = -1;

                        int s = convertInc(in4) * mult;
                        if (s * mult < 0) {
                            System.out.println(INC_ERROR);
                            System.out.println();
                            break;
                        }

                        ArrayList<Task> movedTasks = new ArrayList<>();
                        ArrayList<Integer> taskNs = new ArrayList<>();
                        int n = 1;
                        for (int i = 0; i < tasks.size(); i++) {
                            if (tasks.get(i).getName().equals(in2)) {
                                if (tasks.get(i).getStart() + s < MIN_HOUR ||
                                        tasks.get(i).getEnd() + s > MAX_HOUR) {
                                    System.out.println(String.format(BOUND_ERROR, in2 + " " + n));
                                }
                                else {
                                    movedTasks.add(tasks.get(i));
                                    taskNs.add(n);
                                    tasks.remove(i);
                                    i--;
                                }
                                n++;
                            }
                        }

                        int i = 0;
                        for (Task t : movedTasks) {
                            insertTask(new Task(t.getName(), t.getTag(),
                                    t.isComplete(), t.getStart() + s,
                                    t.getEnd() + s, t.getNote()));
                            System.out.println(String.format(MOVE_SUCCESS, t.getName() + " " + taskNs.get(i)));
                            i++;
                        }
                        
                        System.out.println();
                    }
                    else { //4 args
                        String in5 = sc2.next();
                        if (sc2.hasNext()) {
                            System.out.println(COMMAND_ERROR);
                            System.out.println();
                            break;
                        }
                        
                        int idx = getTaskIdx(in2, in3);
                        int bStart = convertTime(in2);
                        int bEnd = convertTime(in3);
                        int start = convertTime(in4);
                        int end = convertTime(in5);

                        int len = in3.length();
                        if (len > 1 && in3.substring(len - 2).toLowerCase().
                                equals("am") && bEnd == 0) {
                            bEnd = MAX_HOUR;
                        }

                        len = in5.length();
                        if (len > 1 && in5.substring(len - 2).toLowerCase().
                                equals("am") && end == 0) {
                            end = MAX_HOUR;
                        }

                        if (idx != -1) {
                            Task task = tasks.get(idx);
                            
                            //move <task> <instance> <start> <end>: moves instance n of a task to time slot
                            if (start >= 0) {
                                if (end > start) {
                                    moveTask(idx, start, end);
                                    System.out.println(String.format(MOVE_SUCCESS, in2 + " " + in3));
                                }
                                else {
                                    System.out.println(TIME_ERROR);
                                }
                            }

                            //move <task> <instance> <+/-> <time>: shifts instance n of a task
                            else if (in4.equals("+") ||
                                    in4.equals("-")) {
                                int s = convertInc(in5);
                                if (s < 0) {
                                    System.out.println(INC_ERROR);
                                    System.out.println();
                                    break;
                                }
                                
                                if (in4.equals("-")) s *= -1;

                                if (task.getStart() + s < MIN_HOUR ||
                                        task.getEnd() + s > MAX_HOUR) {
                                    System.out.println(String.format(BOUND_ERROR, in2 + " " + in3));
                                }
                                else {
                                    moveTask(idx, task.getStart() + s, task.getEnd() + s);
                                    System.out.println(String.format(MOVE_SUCCESS, in2 + " " + in3));
                                }
                            }
                            else {
                                System.out.println(COMMAND_ERROR);
                            }
                           
                            System.out.println();
                        }
                        
                        //move <start> <end> <+/-> <time>: shifts time slot
                        else if (bStart >= 0) {
                            if (bEnd < bStart) {
                                System.out.println(TIME_ERROR);
                            }
                            else if (!in4.equals("+") && !in4.equals("-")) {
                                System.out.println(COMMAND_ERROR);
                            }
                            else if (convertInc(in5) < 0) {
                                System.out.println(INC_ERROR);
                            }
                            else {
                                ArrayList<Task> movedTasks = new ArrayList<>();
                                ArrayList<Integer> taskNs = new ArrayList<>();
                                int s = convertInc(in5);
                                if (in4.equals("-")) s *= -1;

                                int n = -1;
                                String lTask = DELIM;
                                for (int i = 0; i < tasks.size(); i++) {
                                    if (tasks.get(i).getName().equals(lTask)) {
                                            n++;
                                        }
                                    else {
                                        n = 1;
                                    }
                                    lTask = tasks.get(i).getName();

                                    if (bStart < tasks.get(i).getEnd() &&
                                            bEnd > tasks.get(i).getStart()) {
                                        if (tasks.get(i).getStart() + s < MIN_HOUR ||
                                                tasks.get(i).getEnd() + s > MAX_HOUR) {
                                            System.out.println(String.format(BOUND_ERROR, lTask + " " + n));
                                        }
                                        else {
                                            movedTasks.add(tasks.get(i));
                                            taskNs.add(n);
                                            tasks.remove(i);
                                            i--;
                                        }
                                    }
                                }

                                int i = 0;
                                lTask = DELIM;
                                for (Task t : movedTasks) {
                                    insertTask(new Task(t.getName(), t.getTag(),
                                            t.isComplete(), t.getStart() + s,
                                            t.getEnd() + s, t.getNote()));
                                    System.out.println(String.format(MOVE_SUCCESS, t.getName() + " " + taskNs.get(i)));
                                    i++;
                                }

                                if (movedTasks.size() == 0)
                                    System.out.println(NOT_MOVE);
                            }

                            System.out.println();
                        }

                        if (getTaskIdx(in2, in3) == -1 && !in4.equals("+")
                                && !in4.equals("-")) {
                            System.out.println(String.format(TASK_ERROR, in2 + " " + in3));
                            System.out.println();
                        }
                    }
                    break;
                }

                //note <task> <instance>: sets note for instance n of a task
                else if (inputEquals(in1, "note", 1)) {
                    if (!sc2.hasNext()) {
                        System.out.println(COMMAND_ERROR);
                        System.out.println();
                        break;
                    }
                    
                    String in2 = sc2.next();

                    if (!sc2.hasNext()) {
                        System.out.println(COMMAND_ERROR);
                        System.out.println();
                        break;
                    }
                    
                    String in3 = sc2.next();

                    if (sc2.hasNext()) {
                        System.out.println(COMMAND_ERROR);
                        System.out.println();
                        break;
                    }

                    int idx = getTaskIdx(in2, in3);
                    if (idx == -1) {
                        System.out.println(String.format(TASK_ERROR, in2 + " " + in3));
                        System.out.println();
                        break;
                    }
                    Task task = tasks.get(idx);
                    

                    sc2.close();
                    Scanner sc3 = null;
                    try {
                        sc3 = new Scanner(sc1.next()).useDelimiter(System.lineSeparator());
                        if (!sc3.hasNext()) {
                            System.out.println("No changes made.");
                            System.out.println();
                            sc3.close();
                            break;
                        }
                        String note = sc3.next();

                        if (note.contains(DELIM)) {
                            System.out.println(DELIM_ERROR);
                            System.out.println();
                            break;
                        }
                        
                        task.setNote(note);
                        System.out.println(String.format(NOTE_SUCCESS, note));
                        System.out.println();
                    }
                    finally {
                        if (sc3 != null) {
                            sc3.close();
                        }
                    }

                    break;
                }
                else if (inputEquals(in1, "tick", 1)) {
                    if (!sc2.hasNext()) {
                        System.out.println(COMMAND_ERROR);
                        System.out.println();
                        break;
                    }
                    
                    String in2 = sc2.next();

                    int sum = 0;

                    //tick <task>: marks all instances of a task complete
                    if (!sc2.hasNext()) {
                        boolean found = false;
                        int idx = getTaskIdx(in2, "1");
                        if (idx == -1) {
                            System.out.println(String.format(TASK_ERROR, in2));
                            System.out.println();
                            break;
                        }
                        
                        int n = -1;
                        String lTask = DELIM;
                        for (Task t : tasks) {
                            if (t.getName().equals(lTask)) {
                                n++;
                            }
                            else {
                                n = 1;
                            }
                            lTask = t.getName();

                            if (t.getName().equals(in2) && !t.isComplete()) {
                                found = true;
                                sum += t.getEnd() - t.getStart();
                                t.tick();
                                System.out.println(String.format(TICK_SUCCESS, in2 + " " + n));
                            }
                        }

                        if (!found) {
                            System.out.println(String.format(TICK_ERROR, in2));
                            System.out.println();
                            break;
                        }
                    }

                    //tick <task> <instace>: marks instance n of a task complete
                    else {
                        String in3 = sc2.next();
                        
                        if (sc2.hasNext()) {
                            System.out.println(COMMAND_ERROR);
                            System.out.println();
                            break;
                        }
                        
                        int idx = getTaskIdx(in2, in3);
                        if (idx == -1) {
                            System.out.println(String.format(TASK_ERROR, in2 + " " + in3));
                            System.out.println();
                            break;
                        }
                        Task task = tasks.get(idx);
    
                        if (task.isComplete()) {
                            System.out.println(String.format(TICK_ERROR, in2 + " " + in3));
                            System.out.println();
                            break;
                        }
                        
                        sum = task.getEnd() - task.getStart();
                        task.tick();
                        System.out.println(String.format(TICK_SUCCESS, in2 + " " + in3));
                    }

                    GoalManager.writeGoalData(in2, sum, -1);
                    System.out.println();
                    break;
                }
                else {
                    System.out.println(COMMAND_ERROR);
                    System.out.println();
                    break;
                }
            }
            FileManager.writeFile();
            sc2.close();
        }

    }*/
}