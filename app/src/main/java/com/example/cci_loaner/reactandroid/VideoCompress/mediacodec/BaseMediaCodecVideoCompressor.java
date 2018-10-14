package com.example.cci_loaner.reactandroid.VideoCompress.mediacodec;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;


import com.example.cci_loaner.reactandroid.VideoCompress.GiraffeCompressor;

import java.io.IOException;

/**
 * Created by TangChao on 2017/5/22.
 */

public abstract class BaseMediaCodecVideoCompressor extends GiraffeCompressor {
    static class SampleInfo{
        public int index;
        public MediaCodec.BufferInfo bufferInfo;
        public boolean isVertical;

        public SampleInfo(int index, MediaCodec.BufferInfo bufferInfo) {
            this.index = index;
            this.bufferInfo = bufferInfo;
            isVertical = false;
        }
    }

    public void setVertical(boolean b){
        isVertical = b;
    }



    protected MediaCodec encoder;
    protected MediaCodec decoder;
    protected MediaFormat outputVideoMediaFormat;
    protected InputSurface inputSurface;
    protected OutputSurface outputSurface;
    protected MediaMuxer muxer;
    protected TrackInfo trackInfo;
    protected MediaExtractor mediaExtractor;

    protected void initTrackInfo() throws IOException {
        mediaExtractor = new MediaExtractor();
        mediaExtractor.setDataSource(inputFile.getAbsolutePath());
        trackInfo = TrackInfo.from(mediaExtractor);
    }

    protected MediaFormat initOutputVideoMediaFormat(TrackInfo trackInfo) {
        int width = trackInfo.getVideoMediaFormat().getInteger(MediaFormat.KEY_WIDTH);
        int height = trackInfo.getVideoMediaFormat().getInteger(MediaFormat.KEY_HEIGHT);
        MediaFormat format;

//        if(isVertical)
//        {
            format = MediaFormat.createVideoFormat("video/avc", (int) (height*resizeFactor), (int) (width*resizeFactor));
//        }
//        else {
//            format = MediaFormat.createVideoFormat("video/avc", (int) (width*resizeFactor), (int) (height*resizeFactor));
//
//        }
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 24);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        return format;
    }
}
