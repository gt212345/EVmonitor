package com.emlab.cguee.evmonitor;

import android.app.Activity;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VideoRecordFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VideoRecordFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class VideoRecordFragment extends Fragment implements SurfaceHolder.Callback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Camera mCamera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private TextView record;
    private VideoRecord videoRecord;
    private boolean isNotRec = true;

    private Handler handler;
    private HandlerThread handlerThread;

    private static final String TAG = "VideoRecordFragment";

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VideoRecordFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VideoRecordFragment newInstance(String param1, String param2) {
        VideoRecordFragment fragment = new VideoRecordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public VideoRecordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        handlerThread = new HandlerThread("");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_record, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        videoRecord.stopEncoding();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCamera = getCameraInstance();
        surfaceView = (SurfaceView) getView().findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        videoRecord = new VideoRecord("Record",mCamera,surfaceView.getWidth(),surfaceView.getHeight());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHoldert, int i, int i2, int i3) {
        try {
            mCamera.setPreviewDisplay(surfaceHoldert);
        } catch (IOException e) {
            Log.w(TAG,e.toString());
        }
        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.stopPreview();
        mCamera.release();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        record = (TextView) getActivity().findViewById(R.id.record);
        record.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isNotRec) {
                isNotRec = false;
                handler.post(recordAnimUp);
                Toast.makeText(getActivity(), "Record Start", Toast.LENGTH_SHORT).show();
                try {
                    videoRecord.startEncoding();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                isNotRec = true;
                Toast.makeText(getActivity(), "Record Stop", Toast.LENGTH_SHORT).show();
                videoRecord.stopEncoding();
            }
        }
    };

    public Camera getCameraInstance() {
        Camera c = null;
        try {
            c = openBackFacingCamera();
        }
        catch (Exception e){
            Log.w("getfrontcamera", e.toString());
        }
        return c;
    }

    private Camera openBackFacingCamera() {
        int cameraCount;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx<cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    cam = Camera.open(camIdx);
                    Log.w("Camera","No:"+String.valueOf(camIdx)+" get");
                    Camera.Parameters param = cam.getParameters();
                    param.set( "cam_mode", 1 );
                    param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    cam.setParameters( param );
                } catch (RuntimeException e) {
                    Log.e("Your_TAG", "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }
        return cam;
    }

    private Runnable recordAnimUp = new Runnable() {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    record.setText("REC");
                }
            });
            handler.postDelayed(recordAnimNon, 500);
        }
    };

    private Runnable recordAnimNon = new Runnable() {
        @Override
        public void run() {
            if(!isNotRec) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        record.setText("");
                    }
                });
            }
            handler.postDelayed(recordAnimUp, 500);
        }
    };

}
