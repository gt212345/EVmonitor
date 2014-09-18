package com.emlab.cguee.evmonitor;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_active,container,false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        progressDialog = ((Welcome)getActivity()).getDialog();
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
                startActivity(intent);
                getActivity().finish();
            }
        });
        active.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = ProgressDialog.show(getActivity(),"Please wait","Processing......",true);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(!isActivated) {
                                    stat.setText("啟動");
                                    stat.setTextColor(getResources().getColor(R.color.green));
                                    isActivated = true;
                                    active.setText("終止");
                                    swi.setVisibility(View.VISIBLE);
                                    swi.setClickable(true);
                                }else{
                                    stat.setText("未啟動");
                                    stat.setTextColor(getResources().getColor(R.color.red));
                                    isActivated = false;
                                    active.setText("啟動");
                                    swi.setVisibility(View.INVISIBLE);
                                    swi.setClickable(false);
                                }
                            }
                        });
                    }
                },1000);
            }
        });
    }
}
