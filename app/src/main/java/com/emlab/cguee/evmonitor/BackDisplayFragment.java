package com.emlab.cguee.evmonitor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by hrw on 14/9/3.
 */
public class BackDisplayFragment extends Fragment implements SurfaceHolder.Callback {
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Socket socket;
    private ServerSocket serverSocket;
    private InputStream inputStream;
    private BufferedInputStream bufferedInputStream;
    private MediaCodec mediaCodec;
    private BufferedOutputStream bufferedOutputStream;
    private static final String TAG = "EVmoniterActivity";
    private byte[] temp;
    private boolean isCon = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_backdisplay,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        surfaceView = (SurfaceView)getView().findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        VideoCodecInit();
        temp = new byte[1382400];
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(2025);
                    socket = serverSocket.accept();
                    Log.w(TAG, "Socket connected");
                    inputStream = socket.getInputStream();
                    bufferedInputStream = new BufferedInputStream(inputStream);
                    Log.w(TAG,"thread finished");
                    isCon = true;
                } catch (IOException e) {
                    Log.w(TAG,e.toString());
                }
            }
        }).start();
        while(!isCon){

        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(isCon){
                    try {
                        bufferedInputStream.read(temp,0,temp.length);
                        offerDecoder(temp,0);
                    } catch (IOException e) {
                        Log.w(TAG,e.toString());
                    }
                }
            }
        }).start();
    }
    public void VideoCodecInit() {
        try {
            mediaCodec = MediaCodec.createDecoderByType("video/avc");
        } catch (IOException e) {
            Log.w(TAG,"createDecoderByType failed");
        }
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", 1280, 720);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 2500000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        mediaCodec.configure(mediaFormat,null , null, 0);
        mediaCodec.start();
    }


    // called from Camera.setPreviewCallbackWithBuffer(...) in other class
    public void offerDecoder(byte[] input1,int offset) {
        try {
            Log.w("AVC Codec","Start decode");
            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
            int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(input1, offset, input1.length);
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, input1.length, 0, 0);
            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            while (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);
//                if (bufferInfo.offset != 0)
//                {
//                    bufferedOutputStream.write(outData, bufferInfo.offset, outData.length
//                            - bufferInfo.offset);
//                }
//                else
//                {
//                    bufferedOutputStream.write(outData, 0, outData.length);
//                }
//                    bufferedOutputStream.write(outData, 0, outData.length);
                Bitmap bitmap = BitmapFactory.decodeByteArray(outData, 0, outData.length);
                Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawBitmap(bitmap, 0, 0, null);
                surfaceHolder.unlockCanvasAndPost(canvas);
                Log.i("AvcEncoder", outData.length + " bytes written");
                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);

            }
        } catch (Throwable t) {
            Log.w(TAG,"Codec: "+t.toString());
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
