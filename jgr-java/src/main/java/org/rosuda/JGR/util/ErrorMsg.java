package org.rosuda.JGR.util;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class ErrorMsg {


    public ErrorMsg(Exception e) {
        String filename = "JGRError.log";
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        String error = "--------------------------------------\n\n";
        Calendar cal = new GregorianCalendar();


        int hour24 = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        error += day + "." + months[month] + "." + year + "  " + hour24 + ":" + min + "\n\n";
        error += "Message : " + e.getMessage() + "\n\n";
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
            out.write(error);
            out.flush();
            e.printStackTrace(out);
            out.flush();
            out.write("\n\n--------------------------------------\n\n");
            out.flush();
            out.close();
        } catch (IOException err) {
//            err.printStackTrace();
        }
    }

    public ErrorMsg(String msg) {
        String filename = "JGRError.log";
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        String error = "--------------------------------------\n\n";
        Calendar cal = new GregorianCalendar();


        int hour24 = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        error += day + "." + months[month] + "." + year + "  " + hour24 + ":" + min + "\n\n";
        error += "Message : " + msg + "\n\n";
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
            out.write(error);
            out.flush();
            out.flush();
            out.write("\n\n--------------------------------------\n\n");
            out.flush();
            out.close();
        } catch (IOException err) {
//            err.printStackTrace();
        }
    }
}