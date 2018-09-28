package com.d.tabview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.d.lib.tabview.TabView;

public class MainActivity extends AppCompatActivity implements TabView.OnTabSelectedListener {
    private TextView tvTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTips = (TextView) findViewById(R.id.tv_tips);
        TabView tabView = (TabView) findViewById(R.id.tabv_tab);
        tabView.setOnTabSelectedListener(this);
    }

    @Override
    public void onTabSelected(int position) {
        tvTips.setText("onTab: " + (position + 1));
    }
}
