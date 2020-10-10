package com.Ray.Bicycle;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class Notification extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    String TAG = "mExample";
    RecyclerView mRecyclerView;
    MyListAdapter myListAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_page);
        //Layout Init
        BottomNavi_init();
        InitNavi();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
        swipeRefreshLayout.setOnRefreshListener(()->{
            arrayList.clear();
            makeData();
            myListAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);

        });
    }//onCreate
    private void BottomNavi_init(){
        BottomNavigationView bottomNavigationView
                = (BottomNavigationView) findViewById(R.id.include2);

        bottomNavigationView.getMenu().getItem(1).setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener((item) -> {
            switch (item.getItemId()) {
                case R.id.nav1:
                    Intent intent2 = new Intent(Notification.this, MainActivity.class);
                    startActivity(intent2);
                    break;
                case R.id.nav2:
                    break;
            }
            return true;
        });
    }
    private void InitNavi(){
        DrawerLayout drawer = findViewById(R.id.drawer_layout_Main);
        Toolbar toolbar = findViewById(R.id.toolbar);
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
                /*if (!BTConnFlag.Flag) {
                    Toast.makeText(this, "藍芽連線失敗，請先連線藍芽", Toast.LENGTH_SHORT).show();
                    break;
                }*/
                Intent intent3 = new Intent(Notification.this, SettingPage.class);
                startActivity(intent3);
                onStop();
                break;
        }
        //drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void makeData() {
        for (int i = 0;i<30;i++){
            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put("Id","座號："+String.format("%02d",i+1));
            hashMap.put("Sub1",String.valueOf(new Random().nextInt(80) + 20));
            hashMap.put("Sub2",String.valueOf(new Random().nextInt(80) + 20));
            hashMap.put("Avg",String.valueOf(
                    (Integer.parseInt(hashMap.get("Sub1"))
                            +Integer.parseInt(hashMap.get("Sub2")))/2));

            arrayList.add(hashMap);
        }
    }

    private class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder>{


        class ViewHolder extends RecyclerView.ViewHolder{
            private TextView tvId,tvSub1,tvSub2,tvAvg;
            private View mView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvId = itemView.findViewById(R.id.textView_Id);
                tvSub1 = itemView.findViewById(R.id.textView_sub1);
                tvSub2 = itemView.findViewById(R.id.textView_sub2);
                tvAvg  = itemView.findViewById(R.id.textView_avg);
                mView  = itemView;
            }
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.notify_item,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            int avgS = Integer.parseInt(arrayList.get(position).get("Avg"));
            if (avgS>=80){
                holder.tvId.setBackgroundColor(getColor(R.color.green_TOKIWA));
            }else if (avgS<80 &&avgS>=60){
                holder.tvId.setBackgroundColor(getColor(R.color.blue_RURI));
            }else if(avgS<60 &&avgS>=40){
                holder.tvId.setBackgroundColor(getColor(R.color.yellow_YAMABUKI));
            }else {
                holder.tvId.setBackgroundColor(getColor(R.color.red_GINSYU));
            }
            holder.tvId.setText(arrayList.get(position).get("Id"));
            holder.tvSub1.setText(arrayList.get(position).get("Sub1"));
            holder.tvSub2.setText(arrayList.get(position).get("Sub2"));
            holder.tvAvg.setText(arrayList.get(position).get("Avg"));

            holder.mView.setOnClickListener((v)->{
                Toast.makeText(getBaseContext(),holder.tvAvg.getText(),Toast.LENGTH_SHORT).show();
            });

        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }


}
