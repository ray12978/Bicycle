package com.Ray.Bicycle.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.Ray.Bicycle.R;
import com.Ray.Bicycle.View.BottomNavigation;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class DashboardActivity extends AppCompatActivity {
    protected WebView dashboardView;
    SwipeRefreshLayout swipeRefreshLayout;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        String url = "http://35.229.164.35:3000/bicycle/app/blank-page";
        dashboardView = findViewById(R.id.DBView);
        dashboardView.getSettings().setJavaScriptEnabled(true);
        dashboardView.setWebViewClient(new WebViewClient()); //不調用系統瀏覽器
        dashboardView.getSettings().setDomStorageEnabled(true);
        dashboardView.loadUrl(url);
        BottomNavInit();
        //下拉刷新
        swipeRefreshLayout = findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            //dashboardView.loadUrl("http://35.229.164.35:3000");
            dashboardView.loadUrl(url);
            swipeRefreshLayout.setRefreshing(false);
        });
    }
    private void BottomNavInit() {
        BottomNavigationView MyBtmNav = findViewById(R.id.include2);
        BottomNavigation BtmNav = new BottomNavigation(this, MyBtmNav,2);
        BtmNav.init();
    }

}
