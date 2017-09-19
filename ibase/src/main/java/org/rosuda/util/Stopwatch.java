package org.rosuda.util;


public class Stopwatch {
    long ts_start;
    long ts_stop = 0;
    long ts_elapsed = -1;
    boolean quiet = false;


    public Stopwatch() {
        ts_start = System.currentTimeMillis();
    }


    public Stopwatch(boolean beQuiet) {
        this();
        quiet = beQuiet;
    }


    public long stop() {
        ts_stop = System.currentTimeMillis();
        return ts_elapsed = ts_stop - ts_start;
    }


    public long start() {
        return ts_start = System.currentTimeMillis();
    }


    public long last() {
        return ts_elapsed;
    }


    public long elapsed() {
        return System.currentTimeMillis() - ts_start;
    }


    public long restart() {
        stop();
        ts_start = ts_stop;
        return ts_elapsed;
    }


    public void profile() {
        restart();
        if (Global.PROFILE > 0) {
            System.out.println("time elapsed " + ts_elapsed + " ms");
        }
    }


    public void profile(String s) {
        restart();
        if (Global.PROFILE > 0) {
            System.out.println(s + " " + ts_elapsed + " ms"
                    + ((Global.PROFILE > 1) ? " [mem free " + Runtime.getRuntime().freeMemory() + "]" : ""));
        }
    }

}