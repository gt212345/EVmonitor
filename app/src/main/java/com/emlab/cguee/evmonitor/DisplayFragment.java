package com.emlab.cguee.evmonitor;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DisplayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DisplayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DisplayFragment extends Fragment implements LocationListener {
    // TODO: Rename parameter arguments, choose names that match
    private static final String TAG = "DisplayFragment";
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int HEADER_SIGNAL = 111;
    private ImageView batteryImage,cgu,dnr,radio,navigation;
    private TextView speed, batteryPercent, voltage, current;
    private float header;

    private BigDecimal bigDecimal;

    private Handler handler;
    private HandlerThread handlerThread;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private MapView mapView;
    private GoogleMap mMap;

    private SpannableString speedStr;

    private int animat = 0;
    private Location myLocation;
    private LocationManager locationManager;

    private ProgressDialog progressDialog;
    private Thread BTThread, ListThread;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice btd;
    private BluetoothSocket bts;
    private InputStream inputStream;
    private boolean isFind = false;
    private boolean isOpen = false;
    private boolean stopWorker = false;
    private byte[] input;
    private float soc, vol, cur, ac, spe;

    private MediaPlayer mediaPlayer;

    public DisplayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DisplayFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DisplayFragment newInstance(String param1, String param2) {
        DisplayFragment fragment = new DisplayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
        Log.w(TAG, "LocationManager initialized");
//        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,this);
        myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Log.w(TAG, "Current location request sent");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.w(TAG, "BluetoothAdapter initialized");
        mediaPlayer = new MediaPlayer();
        Log.w(TAG, "MediaPlayer construction completed");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_display, container, false);
        mapView = (MapView) v.findViewById(R.id.map);
        mapView.setClickable(true);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        MapsInitializer.initialize(this.getActivity());
        Log.w(TAG, "MapView constructed");
        mMap = mapView.getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        while (true) {
            if (myLocation != null) {
                break;
            }
        }
        if (myLocation != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 17);
            mMap.animateCamera(cameraUpdate);
            Log.w(TAG, "MapView location set");
        }
        return v;
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        cgu = (ImageView) getView().findViewById(R.id.cgu);
        navigation = (ImageView) getView().findViewById(R.id.navigation);
        radio = (ImageView) getView().findViewById(R.id.radio);
        dnr = (ImageView) getView().findViewById(R.id.dnr);
        batteryImage = (ImageView) getView().findViewById(R.id.batteryImage);
        batteryPercent = (TextView) getView().findViewById(R.id.batteryPercent);
        speed = (TextView) getView().findViewById(R.id.speed);
        voltage = (TextView) getView().findViewById(R.id.voltage);
        current = (TextView) getView().findViewById(R.id.current);
        batteryPercent = (TextView) getView().findViewById(R.id.batteryPercent);
        speedStr = new SpannableString("00" + " km/hr");
        speedStr.setSpan(new RelativeSizeSpan(4f), 0, 2, 0);
        speedStr.setSpan(new ForegroundColorSpan(Color.RED), 0, 2, 0);
        speed.setText(speedStr);
        try {
            mediaPlayer.setDataSource("/sdcard/Download/welcome.mp3");
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        radio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i;
                PackageManager manager = getActivity().getPackageManager();
                try {
                    i = manager.getLaunchIntentForPackage("com.littlehilllearning.twradio");
                    if (i == null)
                        throw new PackageManager.NameNotFoundException();
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    startActivity(i);
                } catch (PackageManager.NameNotFoundException e) {

                }
            }
        });
        navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i;
                PackageManager manager = getActivity().getPackageManager();
                try {
                    i = manager.getLaunchIntentForPackage("com.ultimarom.launchnavigation");
                    if (i == null)
                        throw new PackageManager.NameNotFoundException();
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    startActivity(i);
                } catch (PackageManager.NameNotFoundException e) {

                }
            }
        });
        progressDialog = ProgressDialog.show(getActivity(), "please wait", "Searching for device", true);
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "bluetooth not support");
        }
        if (!mBluetoothAdapter.enable()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOnIntent, 1);
            }
        }
        Log.w(TAG, "Bluetooth functionable, start searching for HC-05");
        findBT();
        BTThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (isFind) {
                    Log.w(TAG, "HC-05 found, trying to connect......");
                    openBT();
                    if (isOpen) {
                        ListThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (!Thread.currentThread().isInterrupted()
                                        && !stopWorker) {
                                    try {
                                        while(inputStream.available() >= 9) {
//                                            header = inputStream.read();
                                            input = new byte[9];
                                            inputStream.read(input);
                                            if (input[0] == HEADER_SIGNAL) {
                                                Log.w(TAG, "Header confirmed");
//                                                input = new byte[8];
//                                                inputStream.read(input);
                                                soc = input[2];
                                                vol = (input[3] + input[4] / 10);
                                                cur = (input[5] + input[6] / 10);
                                                ac = input[7];
                                                spe = input[8];
//                                                soc = inputStream.read();
//                                                soc = inputStream.read();
//                                                float volTemp = inputStream.read();
//                                                vol = volTemp + (float)inputStream.read()/10;
//                                                float curTemp = inputStream.read();
//                                                cur = curTemp + (float)inputStream.read()/10;
//                                                ac = inputStream.read();
//                                                spe = inputStream.read();
//                                            bigDecimal = new BigDecimal((float)inputStream.read());
//                                            soc = bigDecimal.setScale(5,BigDecimal.ROUND_HALF_UP).floatValue();
//                                            bigDecimal = new BigDecimal(inputStream.read() + (inputStream.read() / 10));
//                                            vol = bigDecimal.setScale(5,BigDecimal.ROUND_HALF_UP).floatValue();
//                                            bigDecimal = new BigDecimal(inputStream.read() + (inputStream.read() / 10));
//                                            cur = bigDecimal.setScale(5,BigDecimal.ROUND_HALF_UP).floatValue();
//                                            bigDecimal = new BigDecimal(inputStream.read());
//                                            ac = bigDecimal.setScale(5,BigDecimal.ROUND_HALF_UP).floatValue();
//                                            bigDecimal = new BigDecimal(inputStream.read());
//                                            spe = bigDecimal.setScale(5,BigDecimal.ROUND_HALF_UP).floatValue();
                                                Log.w(TAG, "input = :" + soc + "," + vol + "," + cur + "," + ac + "," + spe);
                                                getActivity().runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        if(soc < 100 && soc >= 0 && vol < 27 && vol >= 22 && cur < 21 && cur >=0 ) {
                                                            batteryPercent.setText((int) soc + "%");
                                                            voltage.setText("電壓\n" + vol + "V");
                                                            current.setText("電流\n" + cur + "A");
                                                            if (soc >= 95) {
                                                                batteryImage.setImageResource(R.drawable.b04);
                                                            } else if (soc >= 67 && soc < 95) {
                                                                batteryImage.setImageResource(R.drawable.b03);
                                                            } else if (soc >= 34 && soc < 67) {
                                                                batteryImage.setImageResource(R.drawable.b02);
                                                            } else if (soc >= 0 && soc < 34) {
                                                                batteryImage.setImageResource(R.drawable.b01);
                                                            }
                                                        }
                                                        if(spe < 20 && spe >= 0 && ac <= 10 && ac >= 0) {
                                                            speedStr = new SpannableString((int) spe + " km/hr");
                                                            speedStr.setSpan(new RelativeSizeSpan(4f), 0, String.valueOf(spe).length() - 1, 0);
                                                            speedStr.setSpan(new ForegroundColorSpan(Color.RED), 0, String.valueOf(spe).length() - 1, 0);
                                                            speed.setText(speedStr);
                                                            switch ((int) ac) {
                                                                case 0:
                                                                    speed.setBackgroundResource(R.drawable.a00);
                                                                    break;
                                                                case 1:
                                                                    speed.setBackgroundResource(R.drawable.a01);
                                                                    break;
                                                                case 2:
                                                                    speed.setBackgroundResource(R.drawable.a02);
                                                                    break;
                                                                case 3:
                                                                    speed.setBackgroundResource(R.drawable.a03);
                                                                    break;
                                                                case 4:
                                                                    speed.setBackgroundResource(R.drawable.a04);
                                                                    break;
                                                                case 5:
                                                                    speed.setBackgroundResource(R.drawable.a05);
                                                                    break;
                                                                case 6:
                                                                    speed.setBackgroundResource(R.drawable.a06);
                                                                    break;
                                                                case 7:
                                                                    speed.setBackgroundResource(R.drawable.a07);
                                                                    break;
                                                                case 8:
                                                                    speed.setBackgroundResource(R.drawable.a08);
                                                                    break;
                                                                case 9:
                                                                    speed.setBackgroundResource(R.drawable.a09);
                                                                    break;
                                                                case 10:
                                                                    speed.setBackgroundResource(R.drawable.a10);
                                                                    break;
                                                            }
                                                        }
                                                    }
                                                });
                                            }else{
                                                inputStream.skip(1);
                                            }
                                        }
                                    } catch (IOException e) {
                                        Log.w(TAG, e.toString()+e.getMessage());
                                        stopWorker = true;
                                    }
                                }
                            }
                        });
                        ListThread.start();
                    }
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Device not found", Toast.LENGTH_SHORT).show();
                        }
                    });
                    progressDialog.dismiss();
                }
            }
        });
        BTThread.start();
//        handlerThread = new HandlerThread("testAnimate");
//        handlerThread.start();
//        handler = new Handler(handlerThread.getLooper());
//        handler.post(testAnim0);
    }

    void findBT() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                .getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("HC-05")) {
                    btd = device;
                    isFind = true;
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
                inputStream = bts.getInputStream();
                isOpen = true;
                runToastOnUIThread("Device connected");
            }
        } catch (IOException e) {
            try {
                Log.w(TAG, "standard method failed, trying with reflect method......");
                if (!bts.isConnected()) {
                    Method m = btd.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                    bts = (BluetoothSocket) m.invoke(btd, 1);
                    bts.connect();
                    Log.w(TAG, "Device connected with reflect method");
                    inputStream = new DataInputStream(bts.getInputStream());
                    runToastOnUIThread("Device connected");
                }
            } catch (NoSuchMethodException e1) {
                Log.w(TAG, e1.toString());
            } catch (InvocationTargetException e1) {
                Log.w(TAG, e1.toString());
            } catch (IllegalAccessException e1) {
                Log.w(TAG, e1.toString());
            } catch (IOException e1) {
                Log.w(TAG, "reflect method failed, shut down process");
//                runToastOnUIThread("Device not found");
            }
        }
        progressDialog.dismiss();
    }

    private float[] getFloatArray(InputStream inputStreamTemp) {
        try {
            return (float[]) new ObjectInputStream(inputStreamTemp).readObject();
        } catch (ClassNotFoundException e) {
            Log.w(TAG, e.toString());
            return null;
        } catch (OptionalDataException e) {
            Log.w(TAG, "getFloatArray: " + e.toString());
            return null;
        } catch (StreamCorruptedException e) {
            Log.w(TAG, "getFloatArray: " + e.toString());
            return null;
        } catch (IOException e) {
            Log.w(TAG, "getFloatArray: " + e.toString());
            return null;
        }
    }

    void runToastOnUIThread(final String text) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private Runnable testAnim0 = new Runnable() {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    speed.setBackgroundResource(R.drawable.a00);
                    batteryImage.setImageResource(R.drawable.b04);
                }
            });
            handler.postDelayed(testAnim1, 200);
        }
    };
    private Runnable testAnim1 = new Runnable() {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    speed.setBackgroundResource(R.drawable.a01);
                }
            });
            handler.postDelayed(testAnim2, 150);
        }
    };
    private Runnable testAnim2 = new Runnable() {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    speed.setBackgroundResource(R.drawable.a02);
                }
            });
            handler.postDelayed(testAnim3, 150);
        }
    };
    private Runnable testAnim3 = new Runnable() {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    speed.setBackgroundResource(R.drawable.a03);
                    batteryImage.setImageResource(R.drawable.b03);
                }
            });
            handler.postDelayed(testAnim4, 150);
        }
    };
    private Runnable testAnim4 = new Runnable() {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    speed.setBackgroundResource(R.drawable.a04);
                }
            });
            handler.postDelayed(testAnim5, 150);
        }
    };
    private Runnable testAnim5 = new Runnable() {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    speed.setBackgroundResource(R.drawable.a05);
                }
            });
            handler.postDelayed(testAnim6, 150);
        }
    };
    private Runnable testAnim6 = new Runnable() {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    speed.setBackgroundResource(R.drawable.a06);
                    batteryImage.setImageResource(R.drawable.b02);
                }
            });
            handler.postDelayed(testAnim7, 150);
        }
    };
    private Runnable testAnim7 = new Runnable() {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    speed.setBackgroundResource(R.drawable.a07);
                }
            });
            handler.postDelayed(testAnim8, 150);
        }
    };
    private Runnable testAnim8 = new Runnable() {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    speed.setBackgroundResource(R.drawable.a08);
                }
            });
            handler.postDelayed(testAnim9, 150);
        }
    };
    private Runnable testAnim9 = new Runnable() {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    speed.setBackgroundResource(R.drawable.a09);
                    batteryImage.setImageResource(R.drawable.b01);
                }
            });
            handler.postDelayed(testAnim10, 150);
        }
    };

    private Runnable testAnim10 = new Runnable() {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    speed.setBackgroundResource(R.drawable.a10);
                }
            });
            handler.postDelayed(testAnim0, 2000);
        }
    };


}
