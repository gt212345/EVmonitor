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

    public VideoRecord(String filename,Camera mCamera){
        mrec = new MediaRecorder();
        this.filename = filename;
        this.mCamera = mCamera;
    }

    public void startEncoding() throws IOException {
        mrec.setCamera(mCamera);
        mCamera.unlock();
        mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mrec.setAudioSource(MediaRecorder.AudioSource.MIC);
        mrec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mrec.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mrec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mrec.setVideoSize(720,480);
        mrec.setVideoEncodingBitRate(30000000);
        mrec.setAudioEncodingBitRate(196608);
        mrec.setOrientationHint(270);
        mrec.setVideoFrameRate(30);
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
