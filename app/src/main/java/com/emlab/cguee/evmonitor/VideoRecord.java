package com.emlab.cguee.evmonitor;

import android.hardware.Camera;
import android.media.MediaRecorder;

import java.io.IOException;

/**
 * Created by hrw on 14/9/18.
 */
public class VideoRecord {
    private MediaRecorder mrec;
    private Camera mCamera;
    private String filename;
    private int width;
    private int height;

    public VideoRecord(String filename,Camera mCamera,int width,int height){
        mrec = new MediaRecorder();
        this.filename = filename;
        this.mCamera = mCamera;
        this.width = width;
        this.height = height;
    }

    public void startEncoding() throws IOException {
        mrec.setCamera(mCamera);
        mCamera.unlock();
        mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mrec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mrec.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        mrec.setVideoSize(320,240);
        mrec.setOutputFile("/sdcard/"+filename+".mp4");
        mrec.prepare();
        mrec.start();
    }

    public void stopEncoding(){
        mrec.stop();
        mrec.reset();
        mrec.release();
    }


}
