package com.emlab.cguee.evmonitor;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
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
    private Button active;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_active,container,false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        stat = (TextView) getActivity().findViewById(R.id.stat);
        active = (Button) getActivity().findViewById(R.id.active);
        active.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stat.setText("啟動");
                stat.setTextColor(getResources().getColor(R.color.green));
                Intent intent = new Intent(getActivity(),EVmoniterActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
    }
}
