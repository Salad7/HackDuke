package com.example.cci_loaner.reactandroid.Models;

/**
 * Created by msalad on 4/10/2018.
 */

public class Video {
    public String title;
    public String size;
    public String length;
    public String url;
    public String userToSaveUnder;
    public String date;
    public String videoStoragePath;
    public String thumbnailURL;
    public Video() {
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public String getVideoStoragePath() {
        return videoStoragePath;
    }

    public void setVideoStoragePath(String videoStoragePath) {
        this.videoStoragePath = videoStoragePath;
    }

    public String getUserToSaveUnder() {
        return userToSaveUnder;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setUserToSaveUnder(String userToSaveUnder) {
        this.userToSaveUnder = userToSaveUnder;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
