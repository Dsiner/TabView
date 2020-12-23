package com.d.tabview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.d.lib.tabview.TabView;

public class MainActivity extends AppCompatActivity {
    private TextView[] tvTips = new TextView[3];
    private TabView[] tabViews = new TabView[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindView();
        init();
    }

    private void bindView() {
        tvTips[0] = (TextView) findViewById(R.id.tv_tips0);
        tvTips[1] = (TextView) findViewById(R.id.tv_tips1);
        tvTips[2] = (TextView) findViewById(R.id.tv_tips2);
        tabViews[0] = (TabView) findViewById(R.id.tabv_tab0);
        tabViews[1] = (TabView) findViewById(R.id.tabv_tab1);
        tabViews[2] = (TabView) findViewById(R.id.tabv_tab2);
    }

    private void init() {
        for (int i = 0; i < tabViews.length; i++) {
            final int finalI = i;
            tabViews[finalI].setOnTabSelectedListener(new TabView.OnTabSelectedListener() {
                @Override
                public void onTabSelected(int index) {
                    tvTips[finalI].setText("On tab: " + (index + 1));
                }
            });
        }
    }
}
