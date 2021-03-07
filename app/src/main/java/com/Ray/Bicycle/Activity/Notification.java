package com.Ray.Bicycle.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.Ray.Bicycle.R;
import com.Ray.Bicycle.View.BottomNavigation;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class Notification extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    String TAG = "mExample";
    RecyclerView mRecyclerView;
    MyListAdapter myListAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    private SharedPreferences FallData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.notification_page);
        super.onCreate(savedInstanceState);

        //Layout Init
        System.out.println("Created");
        BottomNavInit();
        InitNavi();
        FallData = getSharedPreferences("FallData",MODE_PRIVATE);
        //InitFallData();
        //製造資料
        makeData();

        //設置RecycleView
        mRecyclerView = findViewById(R.id.NotifyView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        myListAdapter = new MyListAdapter();
        mRecyclerView.setAdapter(myListAdapter);
        //下拉刷新
        swipeRefreshLayout = findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            arrayList.clear();
            makeData();
            myListAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);

        });
    }//onCreate
    private void BottomNavInit() {
        BottomNavigationView MyBtmNav = findViewById(R.id.include2);
        BottomNavigation BtmNav = new BottomNavigation(this, MyBtmNav,1);
        BtmNav.init();
    }

    private void InitNavi() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_Main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("通知");
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    /***********Navigation*************/
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                Intent intent = new Intent(Notification.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_map:
                Intent intent2 = new Intent(Notification.this, MapsActivity.class);
                startActivity(intent2);
                break;
            case R.id.nav_share:
                Intent intent3 = new Intent(Notification.this, SettingPage.class);
                startActivity(intent3);
                onStop();
                break;
            case R.id.nav_sel_device:
                Intent BTListAct = new Intent(this, ConnectActivity.class);
                startActivity(BTListAct);
                break;
        }
        //drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void makeData() {
        int i = 1;
        while (i < 13) {
            String index = Integer.toString(i);
            String Fall = "Fall" + index;
            String DateI = "Date" + index;
            System.out.println("Fall:"+Fall);
            System.out.println("DateI:"+DateI);
            int Val = FallData.getInt(Fall, 0);
            if (Val!=0) {
                String date = FallData.getString(DateI, "");
                HashMap<String, String> hashMap = new HashMap<>();
                if(Val==1) hashMap.put("Msg", "偵測到自行車遭竊");
                if(Val==2) hashMap.put("Msg", "偵測到使用者跌倒");
                hashMap.put("Date", date);
                arrayList.add(hashMap);
            }
            i++;
        }
    }


    private class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {


        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tvId, tvSub1;
            private View mView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvId = itemView.findViewById(R.id.textView_Id);
                tvSub1 = itemView.findViewById(R.id.textView_sub1);
                mView = itemView;
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.notify_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String Msg = arrayList.get(position).get("Msg");
            String AT = "偵測到自行車遭竊";
            String Fall = "偵測到使用者跌倒";
            if(Msg.equals(AT)) holder.tvId.setBackgroundColor(getColor(R.color.red_GINSYU));
            if(Msg.equals(Fall)) holder.tvId.setBackgroundColor(getColor(R.color.yellow_YAMABUKI));
            holder.tvId.setText(Msg);
            holder.tvSub1.setText(arrayList.get(position).get("Date"));

            holder.mView.setOnClickListener((v) -> {
                Intent intent = new Intent(Notification.this, MapsActivity.class);
                startActivity(intent);
            });

        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }


}
