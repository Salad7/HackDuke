package com.example.cci_loaner.reactandroid.CustomRecording.model;

import android.media.AudioFormat;

public enum AudioChannel {
    STEREO,
    MONO;

    public int getChannel(){
        switch (this){
            case MONO:
                return AudioFormat.CHANNEL_IN_MONO;
            default:
                return AudioFormat.CHANNEL_IN_STEREO;
        }
    }
}