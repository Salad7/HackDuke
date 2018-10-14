package com.example.cci_loaner.reactandroid.Models;

import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by cci-loaner on 3/1/18.
 */

public class Bucket {
    String bucketName;
    long length;
    String fullText;
    ArrayList<String> transcripts;
    String sampleText;
    String recordingURL;
    String gsURl;
    String fbKey;
    int day;
    int month;
    int year;
    String dateInText;



    public void setDateMonthDayYear(int month, int day, int year){
        this.month = month;
        this.day = day;
        this.year = year;
        dateInText = "";
    }

    public void setDateInText(String s){
        dateInText = s;
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

    public String getDate(){
        if(dateInText.equals("")){
            return getMonthName(month-1) + " "+ ordinal(day)+ " "+year;
        }
        else {
            return dateInText;
        }
    }

    public String getMonthName(int month){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month);
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
        String month_name = month_date.format(calendar.getTime());

        return month_name;
    }

    public String getFbKey() {
        return fbKey;
    }

    public void setFbKey(String fbKey) {
        this.fbKey = fbKey;
    }

    public String getGsURl() {
        return gsURl;
    }

    public void setGsURl(String gsURl) {
        this.gsURl = gsURl;
    }

    public String getRecordingURL() {
        return recordingURL;
    }

    public void setRecordingURL(String recordingURL) {
        this.recordingURL = recordingURL;
    }

    public String getSampleText() {
        return sampleText;
    }

    public void setSampleText(String sampleText) {
        this.sampleText = sampleText;
    }

    public Bucket() {
        transcripts = new ArrayList<>();
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
        if(fullText.length() > 20) {
            setSampleText(fullText.substring(0,20));
        }
        else {
            setSampleText(fullText.substring(0,fullText.length()));
        }
    }

    public void addTranscript(ArrayList<String> bucketTranscript){
        transcripts.addAll(bucketTranscript);
    }

    public ArrayList<String> getTranscripts() {
        return transcripts;
    }


}
