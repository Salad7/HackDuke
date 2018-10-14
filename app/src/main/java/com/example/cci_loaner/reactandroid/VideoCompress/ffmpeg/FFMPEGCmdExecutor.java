package com.example.cci_loaner.reactandroid.VideoCompress.ffmpeg;

import android.content.Context;

/**
 * Created by TangChao on 2017/9/14.
 */

public interface FFMPEGCmdExecutor {
    void exec(String cmd);
    boolean killRunningProcesses(String tag);
    void init(Context context);
}
