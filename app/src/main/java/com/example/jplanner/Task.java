package com.example.jplanner;

public class Task {
    //constants
    
    private String name;
    private String tag;
    private boolean complete;
    private int start;
    private int end;
    private String note;

    public Task(String name, String tag, int start, int end) {
        this.name = name;
        this.tag = tag;
        this.complete = false;
        this.start = start;
        this.end = end;
        this.note = " ";
    }

    public Task(String name, String tag, Boolean complete, int start, int end, String note) {
        this.name = name;
        this.tag = tag;
        this.complete = complete;
        this.start = start;
        this.end = end;
        this.note = note;
    }

    public String getName() {
        return this.name;
    }

    public int getStart() {
        return this.start;
    }

    public int getEnd() {
        return this.end;
    }

    public String getNote() {
        return this.note;
    }

    public boolean isComplete() {
        return this.complete;
    }

    public String getTag() {
        return this.tag;
    }

    public void tick() {
        this.complete = true;
    }

    public void untick() { this.complete = false; }

    public void setTime(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}