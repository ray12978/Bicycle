package com.Ray.Bicycle.View;

import android.content.Context;
import android.content.Intent;

import com.Ray.Bicycle.Activity.DashboardActivity;
import com.Ray.Bicycle.kt.Custom;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.Ray.Bicycle.Activity.Notification;
import com.Ray.Bicycle.Activity.MainActivity;

import com.Ray.Bicycle.R;

public final class BottomNavigation {
    Context context;
    BottomNavigationView bottomNav;
    int index;

    public BottomNavigation(Context context, BottomNavigationView nav, int index){
        this.context = context;
        this.bottomNav = nav;
        this.index = index;
    }
    public void init(){
        bottomNav.getMenu().getItem(index).setChecked(true);

        bottomNav.setOnNavigationItemSelectedListener((item) -> {
            switch (item.getItemId()) {
                case R.id.nav1:
                    Intent intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                    break;
                case R.id.nav2:
                    Intent intent2 = new Intent(context, Notification.class);
                    context.startActivity(intent2);
                    break;
                case R.id.nav3:
                    Intent intent3 = new Intent(context, DashboardActivity.class);
                    //Intent intent3 = new Intent(context, Custom.class);
                    context.startActivity(intent3);
                    break;
            }
            return true;
        });
    }
}
