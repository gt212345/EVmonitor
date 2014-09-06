package com.emlab.cguee.evmonitor;

import android.app.Activity;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DisplayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DisplayFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class DisplayFragment extends Fragment implements LocationListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ImageView batteryImage;
    private TextView speed,batteryPercent;

    private Handler handler;
    private HandlerThread handlerThread;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private MapView mapView;
    private GoogleMap mMap;

    private SpannableString speedStr;
    private String speedCur = "10";

    private int animat = 0;
    private Location myLocation;
    private LocationManager locationManager;

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
    public DisplayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
//        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,this);
        myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        super.onCreate(savedInstanceState);
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
        mMap = mapView.getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        while(true){
            if(myLocation != null){
                break;
            }
        }
        if(myLocation != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 16);
            mMap.animateCamera(cameraUpdate);
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
        batteryImage = (ImageView)getView().findViewById(R.id.batteryImage);
        batteryPercent = (TextView)getView().findViewById(R.id.batteryPercent);
        speed = (TextView)getView().findViewById(R.id.speed);
        speedStr = new SpannableString(speedCur + " km/hr");
        speedStr.setSpan(new RelativeSizeSpan(4f),0,2,0);
        speedStr.setSpan(new ForegroundColorSpan(Color.RED),0,2,0);
        speed.setText(speedStr);
        handlerThread = new HandlerThread("testAnimate");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        handler.post(testAnim0);



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
            handler.postDelayed(testAnim1,200);
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
            handler.postDelayed(testAnim2,150);
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
            handler.postDelayed(testAnim3,150);
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
            handler.postDelayed(testAnim4,150);
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
            handler.postDelayed(testAnim5,150);
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
            handler.postDelayed(testAnim6,150);
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
            handler.postDelayed(testAnim7,150);
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
            handler.postDelayed(testAnim8,150);
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
            handler.postDelayed(testAnim9,150);
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
            handler.postDelayed(testAnim10,150);
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
            handler.postDelayed(testAnim0,2000);
        }
    };


    private void setUpMapIfNeeded(View inflatedView) {
        if (mMap == null) {
            mMap = ((MapView) inflatedView.findViewById(R.id.map)).getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMyLocationEnabled(true);
        MapsInitializer.initialize(getActivity());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(120.58, 24.48), 10);
        mMap.animateCamera(cameraUpdate);
//        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));

    }

}
