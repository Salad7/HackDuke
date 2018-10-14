package com.example.cci_loaner.reactandroid.Models;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by cci-loaner on 4/9/18.
 */

public class Note {

    String text;
    String time;
    String date;
   public Note(){

    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
       return date;
    }

    public void setDateByEnglish(String d){
       date = d;
    }

    public void setDate(int month, int day, int year) {
        this.date = getMonthName(month-1) + " "+ ordinal(day)+ " "+year;
    }

    public static String ordinal(int i) {
        String[] sufixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + sufixes[i % 10];

        }
    }



    public String getMonthName(int month){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month);
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
        String month_name = month_date.format(calendar.getTime());

        return month_name;
    }
}
