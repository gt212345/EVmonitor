package com.emlab.cguee.evmonitor;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

/**
 * Created by hrw on 14/9/18.
 */
public class ActiveFragment extends Fragment {
    private TextView stat;
    private Button active,swi;
    private ProgressDialog progressDialog;
    private Handler handler;
    private HandlerThread handlerThread;
    private boolean isActivated = false;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice btd;
    private BluetoothSocket bts;
    private OutputStream outputStream;
    private MediaPlayer mediaPlayer;

    private static final String TAG = "ActiveFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_active,container,false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            mediaPlayer.setDataSource("/sdcard/Download/unlock_success.mp3");
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Log.w("WelcomeActivity",e.toString());
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        progressDialog = ((WelcomeActivity)getActivity()).getDialog();
        progressDialog.dismiss();
        handlerThread = new HandlerThread("");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        stat = (TextView) getActivity().findViewById(R.id.stat);
        active = (Button) getActivity().findViewById(R.id.active);
        swi = (Button) getActivity().findViewById(R.id.swi);
        swi.setClickable(false);
        swi.setVisibility(View.INVISIBLE);
        swi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), EVmoniterActivity.class);
                try {
                    outputStream.close();
                    bts.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
                getActivity().finish();
            }
        });
        active.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = ProgressDialog.show(getActivity(),"Please wait","Processing......",true);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(bts == null) {
                            findBT();
                        } else {
                            handler.post(onclick);
                        }
                    }
                });
            }
        });
    }
    void findBT() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                .getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("HC-05")) {
                    btd = device;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            openBT();
                        }
                    });
                    break;
                }
            }
        }
    }
    void openBT() {
//        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard
        // SerialPortService
        // ID
        UUID uuid = btd.getUuids()[0].getUuid();
        try {
            Log.w(TAG, "Trying to connect with standard method");
            bts = btd.createRfcommSocketToServiceRecord(uuid);
            if (!bts.isConnected()) {
                bts.connect();
                Log.w(TAG, "Device connected with standard method");
//                inputStream = new DataInputStream(bts.getInputStream());
                outputStream = bts.getOutputStream();
                handler.post(onclick);
                progressDialog.dismiss();
            }
        } catch (IOException e) {
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource("/sdcard/Download/power_off.mp3");
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e1) {
                Log.w(TAG,"???");
            }
            progressDialog.dismiss();
//            try {
//                Log.w(TAG, "standard method failed, trying with reflect method......");
//                if (!bts.isConnected()) {
//                    Method m = btd.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
//                    bts = (BluetoothSocket) m.invoke(btd, 1);
//                    bts.connect();
//                    Log.w(TAG, "Device connected with reflect method");
//                    outputStream = bts.getOutputStream();
//                    handler.post(onclick);
//                    progressDialog.dismiss();
//                }
//            } catch (NoSuchMethodException e1) {
//                progressDialog.dismiss();
//                Log.w(TAG, e1.toString());
//            } catch (InvocationTargetException e1) {
//                progressDialog.dismiss();
//                Log.w(TAG, e1.toString());
//            } catch (IllegalAccessException e1) {
//                progressDialog.dismiss();
//                Log.w(TAG, e1.toString());
//            } catch (IOException e1) {
//                progressDialog.dismiss();
//                Log.w(TAG, "reflect method failed, shut down process");
//                runToastOnUIThread("Device not found");
//            }
        }
    }

    private Runnable onclick = new Runnable() {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!isActivated) {
                        if(bts.isConnected()) {
                            try {
                                outputStream.write(1);
                                Log.w("Active Code","Sent");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            stat.setText("啟動");
                            stat.setTextColor(getResources().getColor(R.color.green));
                            isActivated = true;
                            active.setText("終止");
                            swi.setVisibility(View.VISIBLE);
                            swi.setClickable(true);
                            progressDialog.dismiss();
                        } else {
                            findBT();
                        }
                    }else{
                        if(bts.isConnected()) {
                            try {
                                outputStream.write(0);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            stat.setText("未啟動");
                            stat.setTextColor(getResources().getColor(R.color.red));
                            isActivated = false;
                            active.setText("啟動");
                            swi.setVisibility(View.INVISIBLE);
                            swi.setClickable(false);
                            try {
                                bts.close();
                                progressDialog.dismiss();
                            } catch (IOException e) {
                                Log.w("終止",e.toString());
                            }
                        } else {
                            Toast.makeText(getActivity(),"Device already offline",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    };
}
