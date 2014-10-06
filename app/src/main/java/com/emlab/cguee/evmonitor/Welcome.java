package com.emlab.cguee.evmonitor;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;


public class Welcome extends Activity {
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    private void storeDialog(ProgressDialog progressDialog){
        this.progressDialog = progressDialog;
    }

    public ProgressDialog getDialog() {
        return this.progressDialog;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.welcome, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private EditText password;
        private Button confirm;
        private FragmentManager fragmentManager;
        private Fragment fragment;
        private ProgressDialog progressDialog;
        private HandlerThread handlerThread;
        private Handler handler;
        private MediaPlayer mediaPlayer;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            fragmentManager = getFragmentManager();
            mediaPlayer = new MediaPlayer();
            View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            try {
                mediaPlayer.setDataSource("/sdcard/Download/unlock.mp3");
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.toString();
            }
            handlerThread = new HandlerThread("PD");
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
            password = (EditText)getView().findViewById(R.id.password);
            confirm = (Button)getView().findViewById(R.id.confirm);
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(password.getText().toString().equals("develop") || password.getText().toString().equals("")){
                        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                        progressDialog = ProgressDialog.show(getActivity(), "please wait", "Logging in", true);
                        ((Welcome)getActivity()).storeDialog(progressDialog);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                fragment = new ActiveFragment();
                                mediaPlayer.release();
                                fragmentManager.beginTransaction().replace(R.id.container,fragment).commit();
                            }
                        },1000);
                    }else{
                        Toast.makeText(getActivity(),"Permission denied",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
