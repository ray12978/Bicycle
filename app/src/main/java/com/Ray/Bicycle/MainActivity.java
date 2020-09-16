package com.Ray.Bicycle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;
import android.content.DialogInterface;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.ivbaranov.rxbluetooth.RxBluetooth;
import com.github.ivbaranov.rxbluetooth.predicates.BtPredicate;
import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**rxJava**/
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //private final UUID uuid = UUID.fromString("8c4102d5-f0f9-4958-806e-7ba5fd54ce7c");
    private final UUID serialPortUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // private EditText id;
    private EditText BTM;
    private String SpeedLimit = "";
    private String address, Name;
    private String UserName;
    public StringBuffer BTSendMsg = new StringBuffer("N00NNNN"); //[0]Lock{L,F,N},[1]SpeedTen,[2]SpeedUnit,[3]SpeedConfirm,[4]Laser{T,J,N},[5]Buzzer{E,N},[6]CloudMode{Y,N}
    public TextView text_Respond;
    /**
     * Bluetooth
     **/
    private BluetoothAdapter bluetoothAdapter;
    public Button btBTConct, btSpLit;
    /*********************Notify*********************/
    private static final String TAG = MainActivity.class.getSimpleName();

    public LoadingDialog loadingDialog;
    /******************ButtonFlag********************/
    FlagAddress SendFlag = new FlagAddress(false);
    FlagAddress MsgBtFlag = new FlagAddress(true);
    FlagAddress LasFlag = new FlagAddress(true);
    FlagAddress LckFlag = new FlagAddress(true);
    FlagAddress BuzFlag = new FlagAddress(true);
    FlagAddress SpdFlag = new FlagAddress(true);
    FlagAddress DanFlag = new FlagAddress(false);
    FlagAddress PostFlag = new FlagAddress(false);
    /*******************Layout***********************/
    private DrawerLayout drawer;
    private Toolbar toolbar;
    /**
     * Application
     **/
    private MyApp MyAppInst = MyApp.getAppInstance();
    /**
     * RxJava
     **/
    RxBluetooth rxBluetooth = new RxBluetooth(this);
    //RxBluetoothWrite rxBluetoothWrite = new RxBluetoothWrite();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    /**
     * Http
     **/
    private String GetVal;
    /**
     * Setting Val
     **/
    FlagAddress NbFlag = new FlagAddress(false);
    FlagAddress BTConnFlag = new FlagAddress(false);
    String id;
    String nb;
    /*******TimePicker*********/
    private TimePickerDialog dialog = new TimePickerDialog(this);
    /**
     * Shared
     **/
    private SharedPreferences BTWrData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //context = this;
        /*****************藍牙*************/
        final String deviceName = getSharedPreferences("BTDetail", MODE_PRIVATE)
                .getString("Name", "尚未選擇裝置");
        final String deviceAddress = getSharedPreferences("BTDetail", MODE_PRIVATE)
                .getString("Address", "null");
        address = deviceAddress;
        Name = deviceName;
        text_Respond = findViewById(R.id.text_Respond);
        BTM = findViewById(R.id.id2);
        //SpeedLimit = findViewById(R.id.edit_SpeedLimit);
        loadingDialog = new LoadingDialog(MainActivity.this);
        /***********SharedPreference***************/
        BTWrData = getSharedPreferences("BTMsg" , MODE_PRIVATE);
        /**********Layout***************/
        toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout_Main);
        toolbar.setTitle(String.format("%s %s", "藍芽裝置：" + deviceName, deviceName.equals("尚未選擇裝置") ? "" : MyAppInst.getBTState() ? "已連線" : "未連線"));
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        ButtonListen();
        MyAppInst.startTimer();
        btBTConct.setText(MyAppInst.getBTState() ? "已連線" : "未連線");
        initEventListeners();
        CheckSetting();
        SpeedDialog();
        UpdateBTMsg();
        //rxBluetoothWrite.TimeTest();
        //MyAppInst.ScanDanger(Danger_Msg());
        //MyAppInst.ScanDanger();
    }

    protected Object getSetting(String Sel) {
        if (Sel.equals("cloud")) {
            boolean ans = getSharedPreferences("UserSetting", MODE_PRIVATE)
                    .getBoolean(Sel, false);
            return ans;
        }
        String ans = getSharedPreferences("UserSetting", MODE_PRIVATE)
                .getString(Sel, "null");

        //System.out.println(Sel);
        if (Sel.equals("nb") || Sel.equals("id")) return ans;
        assert ans != null;
        return !ans.equals("null");
    }

    private void CheckSetting() {
        nb = (String) getSetting("nb");
        id = (String) getSetting("id");
        PostFlag.Flag = (Boolean) getSetting("cloud");
        NbFlag.Flag = nb.equals("使用NB-IoT上傳");
        if (PostFlag.Flag) BTSendMsg.replace(6, 7, "Y");
        else BTSendMsg.replace(6, 7, "N");
        System.out.print("nb狀態:");
        System.out.println(nb);
        System.out.print("cloud狀態:");
        System.out.println(NbFlag.Flag);
        System.out.print("id:");
        System.out.println(id);
    }

    public void DpBTConnState(boolean state) {
        btBTConct.setText(state ? "已連線" : "未連線");
        toolbar.setTitle(String.format("%s %s", "藍芽裝置：" + Name, Name.equals("尚未選擇裝置") ?
                "" : state ? "已連線" : "未連線"));
        BTConnFlag.Flag = state;
        btBTConct.setEnabled(!state);
    }


    void SpeedDialog() {
        final String[] num = new String[1];
        dialog.onDialogRespond = new TimePickerDialog.OnDialogRespond() {
            @Override
            public void onRespond(String selected) {
                if(selected.equals("不限制"))selected = "0";
                num[0] = selected;
                System.out.print("number is ");
                //System.out.println(selected);
            }

            @Override
            public void onResult(boolean ans) throws Exception {
                if (ans) {
                    SpeedLimit = (num[0]);
                    text_Respond.setText(num[0]);
                    System.out.println(num[0]);
                    System.out.println("Sel true");
                    Speed_Limit();
                } else System.out.println("Sel false");
            }
        };
    }
    void UpdateBTMsg(){

        BTWrData.edit()
                .clear()
                .putString("SendMsg" , BTSendMsg.toString())
                .apply();
        System.out.println(getSharedPreferences("BTMsg", MODE_PRIVATE)
                .getString("SendMsg", null));
    }
    private void ButtonListen() {
        /**BT按鈕**/
        Button btBTSend = findViewById(R.id.btBTSend);
        Button btBTOpen = findViewById(R.id.BTOpen);
        btBTConct = findViewById(R.id.btBTConct);
        Button btBTDiscont = findViewById(R.id.btBTDiscont);
        Button btClear = findViewById(R.id.btClr);
        Button btDisplay = findViewById(R.id.btDisplay);
        /**IO按鈕*/
        Button btLaser = findViewById(R.id.las_btn); //雷射按鈕
        Button btBuzz = findViewById(R.id.buzz_btn); //蜂鳴器按鈕
        Button btLck = findViewById(R.id.lck_btn); //上鎖按鈕
        /**HTTP按鈕*/
        Button btPost = findViewById(R.id.button_POST);
        Button btGET = findViewById(R.id.button_GET);
        btSpLit = findViewById(R.id.SpLit_btn);
        btBTDiscont.setOnClickListener((view) -> {
            MyAppInst.disconnect(btBTConct);
        });
        btBuzz.setEnabled(false);
        /**藍牙按鈕動作**/
        btBTOpen.setOnClickListener(v -> {
            Intent BTListAct = new Intent(MainActivity.this, ConnectActivity.class);
            startActivity(BTListAct);
        });
        btBTSend.setOnClickListener(v -> {
            try {
                MyAppInst.writeBT("bbb");

                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        btBTConct.setOnClickListener(v -> {
            //loadingDialog.startLoadingDialog();
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            if (MyAppInst.connDevice(device)) DpBTConnState(true);
            else DpBTConnState(false);
            //loadingDialog.dismissDialog();
        });
        btLaser.setOnClickListener(v -> {
            BTMsg(4, 5, "T", "J", LasFlag);
            addUserId();
            int On = R.drawable.bike_open_white_48dp;
            int Off = R.drawable.ic_bike_icon_off_black;
            Button_exterior(btLaser, Off, On, 4, 'J');
            UpdateBTMsg();
            /*try {
                if (SendFlag.Flag) MyAppInst.writeBT(BTSendMsg.toString());
                //if(SendFlag.Flag) MyAppInst.writeBT("aaa");
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            //loadingDialog.startLoadingDialog();
            //MyAppInst.str_process();
            //sendPOST();
        });
        btBuzz.setOnClickListener(v -> {
            BTMsg(5, 6, "E", "N", BuzFlag);
            addUserId();
            int On = R.drawable.ic_baseline_volume_off_24;
            int Off = R.drawable.ic_baseline_volume_up_24;
            Button_exterior(btBuzz, Off, On, 5, 'N');
            UpdateBTMsg();
            /*try {
                if (SendFlag.Flag) MyAppInst.writeBT(BTSendMsg.toString());
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            System.out.println("Buzz:"+ BuzFlag.Flag);
            MyAppInst.DangerFlag.Flag = BuzFlag.Flag;
            //DanFlag.Flag = BuzFlag.Flag;
            //MyAppInst.str_process();
            //sendPOST();
        });
        btLck.setOnClickListener(v -> {
            BTMsg(0, 1, "L", "F", LckFlag);
            addUserId();
            int On = R.drawable.ic_baseline_lock_24;
            int Off = R.drawable.ic_baseline_lock_open_24;
            Button_exterior(btLck, Off, On, 0, 'F');
            UpdateBTMsg();
            /*try {
                if (SendFlag.Flag) MyAppInst.writeBT(BTSendMsg.toString());
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            //str_process();
            if (!LckFlag.Flag) {
                btBuzz.setEnabled(true);
                btSpLit.setEnabled(false);
                btLaser.setEnabled(false);
                //setVibrate(1000);
                //Notify();
                /*new AlertDialog.Builder(MainActivity.this)
                        .setIcon(R.drawable.ic_baseline_warning_48)
                        .setTitle("警告：您的腳踏車發生異狀,請立即確認狀況")
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                VibFlag.Flag = false;
                            }
                        })
                        .show();*/
            } else {
                btBuzz.setEnabled(false);
                btSpLit.setEnabled(true);
                btLaser.setEnabled(true);
            }
        });
        btSpLit.setOnClickListener(v -> {
            if (!BTConnFlag.Flag) {
                Toast.makeText(this, "請先連線藍芽", Toast.LENGTH_SHORT).show();
                return;
            }
            if (SpdFlag.Flag) dialog.showDialog();
            if (!SpdFlag.Flag) {
                BTSendMsg.replace(3, 4, "N");
                SpdFlag.Flag = true;
                System.out.println(BTSendMsg);
                /*try {
                    MyAppInst.writeBT(BTSendMsg.toString());
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                MyAppInst.str_process();
                //sendPOST();
            } else {
                Toast.makeText(this, "請先設定時速", Toast.LENGTH_SHORT).show();
            }
            int On = R.drawable.ic_speed_white;
            int Off = R.drawable.ic_speed;
            Button_exterior(btSpLit, Off, On, 3, 'N');
            UpdateBTMsg();
        });
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btClear.setOnClickListener(v -> {

            //MyAppInst.BTValTmp.delete(0, MyAppInst.BTValTmp.length());
            //Danger_Msg();
            //MyAppInst.ScanDanger();
        });
        btDisplay.setOnClickListener(v -> {
            Toast.makeText(this, MyAppInst.getVal('A'), Toast.LENGTH_LONG).show();
            MyAppInst.str_process();
            System.out.println("BTTmp:");
            System.out.println(MyAppInst.getVal('A'));
            System.out.println("PostFlag:");
            System.out.println(PostFlag);
            Log.d(MyAppInst.getVal('A'), "Tmp");
            Log.d(MyAppInst.getVal('S'), "S");
            Log.d(MyAppInst.getVal('M'), "M");
            Log.d(MyAppInst.getVal('T'), "T");
            Log.d(MyAppInst.getVal('P'), "P");
            System.out.println("LckFlag:");
            System.out.println(LckFlag.Flag);

        });

        /**HTTP按鈕動作**/
        /**傳送POST**/
        btPost.setOnClickListener(v -> {
            /*if (id.length() != 0) //sendPOST();
            else {
                Toast.makeText(this, "使用者名稱設定失敗，請先輸入id", Toast.LENGTH_SHORT).show();
            }*/
        });

    }

    /***********Navigation*************/
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_map:
                Intent intent2 = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent2);
                break;
            case R.id.nav_share:
                if (!BTConnFlag.Flag) {
                    Toast.makeText(this, "藍芽連線失敗，請先連線藍芽", Toast.LENGTH_SHORT).show();
                    break;
                }
                Intent intent3 = new Intent(MainActivity.this, SettingPage.class);
                startActivity(intent3);
                onStop();
                break;
        }
        //drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onBackPressed() {
        /*if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }*/
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("要結束應用程式嗎?")
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();//Exit Activity
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).create().show();
    }

    /***/
    @Override
    protected void onPause() {
        super.onPause();
        //readerStop = true;
        DanFlag.Flag = false;
        //loadingDialog.startLoadingDialog();
    }

    @Override
    protected void onResume() {

        super.onResume();
        //reader.start();
        // readerStop = false;
//        loadingDialog.dismissDialog();
        //danger.start();

    }

    @Override
    protected void onStart() {
        CheckSetting();
        addUserId();
        //MyAppInst.ScanDanger();
        try {
            MyAppInst.writeBT(BTSendMsg.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    protected void onDestroy() {
        SharedPreferences BTDetail = getApplicationContext().getSharedPreferences("BTDetail", MODE_PRIVATE);
        SharedPreferences.Editor BTEdit = BTDetail.edit();
        BTEdit.clear();
        BTEdit.apply();
        if (rxBluetooth != null) {
            // Make sure we're not doing discovery anymore
            rxBluetooth.cancelDiscovery();
        }
        compositeDisposable.dispose();
        super.onDestroy();
    }

    /***********************藍牙副程式*******************************/

    private void BTMsg(int start, int end, String Msg1, String Msg2, FlagAddress SelectFlag) {
        MsgBtFlag.Flag = true;
        if (id.length() == 0) {
            Toast.makeText(this, "使用者名稱設定失敗，請先輸入id", Toast.LENGTH_SHORT).show();
            SendFlag.Flag = false;
            return;
        }
        if (!BTConnFlag.Flag) {
            Toast.makeText(this, "藍芽連線失敗，請先連線藍芽", Toast.LENGTH_SHORT).show();
            SendFlag.Flag = false;
            return;
        }
        SendFlag.Flag = true;
        if (SelectFlag.Flag && MsgBtFlag.Flag) {
            BTSendMsg.replace(start, end, Msg1);
            MsgBtFlag.Flag = false;
            SelectFlag.Flag = false;
        }
        if (!SelectFlag.Flag && MsgBtFlag.Flag) {
            BTSendMsg.replace(start, end, Msg2);
            MsgBtFlag.Flag = false;
            SelectFlag.Flag = true;
        }

        //if(NbFlag.Flag)BTSendMsg.replace()
        System.out.println(BTSendMsg);
    }

    void addUserId() {
        UserName = id;
        while (UserName.length() < 14) UserName += '@';
        if (BTSendMsg.length() >= 8) {
            BTSendMsg.replace(7, 21, UserName);
        } else {
            BTSendMsg.append(UserName);
        }
    }

    void Speed_Limit() throws Exception {
        //String SpeedLimit = "" ;
        String SpLtVal;
        if (!BTConnFlag.Flag) {
            Toast.makeText(this, "請先連線藍芽", Toast.LENGTH_SHORT).show();
            return;
        }

        if (SpeedLimit.length() != 0 && SpdFlag.Flag && SpeedLimit.length() < 3) {
            SpLtVal = SpeedLimit;
            System.out.print("SpLtVal is:");
            System.out.println(SpLtVal);
            btSpLit.setText(String.format("速度限制%s", SpLtVal));
            if (SpeedLimit.length() == 1) {
                BTSendMsg.replace(1, 2, "0");
                BTSendMsg.replace(2, 3, SpLtVal);
            } else {
                BTSendMsg.replace(1, 3, SpLtVal);
            }
            BTMsg(3, 4, "Y", "N", SpdFlag);
            addUserId();
            int On = R.drawable.ic_speed_white;
            int Off = R.drawable.ic_speed;
            Button_exterior(btSpLit, Off, On, 3, 'N');
            System.out.println(BTSendMsg);
            if (SendFlag.Flag) MyAppInst.writeBT(BTSendMsg.toString());
            try {

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            MyAppInst.str_process();
            //sendPOST();
        } else {

        }

    }

    public AlertDialog Danger_Msg() {
        return new AlertDialog.Builder(MainActivity.this)
                .setIcon(R.drawable.ic_baseline_warning_48)
                .setTitle("警告：您的腳踏車發生異狀,請立即確認狀況")
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    /*******************************其他**********************************/
    void Button_exterior(Button btn, int one, int two, int bit, char condi) {
        if (id.length() == 0 || !BTConnFlag.Flag) return;
        btn.setCompoundDrawablesWithIntrinsicBounds(BTSendMsg.charAt(bit) == condi ?
                one : two, 0, 0, 0);
        /*btn.setBackgroundColor(BTSendMsg.charAt(bit) == condi ?
                0xFFD0D0D0 : 0xFF1A64D4);*/
        btn.setBackground(BTSendMsg.charAt(bit) == condi ?
                this.getResources().getDrawable(R.drawable.button_style_off) : this.getResources().getDrawable(R.drawable.button_style_on));

        btn.setTextColor(BTSendMsg.charAt(bit) == condi ? 0xFF606060 : 0xFFFFFFFF);
    }


    /*****************************HTTP副程式******************************/
    private void sendGET() {
        TextView tvRes = findViewById(R.id.text_Respond);
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**設置傳送需求*/
        Request request = new Request.Builder()
                .url("https://jsonplaceholder.typicode.com/posts/1")
                //.url("http://35.221.236.109:3000/getSetting")
                // .url("http://35.221.236.109:3000/getSetting/id123")
                //.url("http://35.221.236.109:3000/getGps/ID123456789ABC")
                //資料庫測試        .url("http://35.221.236.109:3000/api880509")
                //.url("https://maker.ifttt.com/trigger/line/with/key/0nl929cYWV-nv9f76AW_O?value1=1")
//                .header("Cookie","")//有Cookie需求的話則可用此發送
//                .addHeader("","")//如果API有需要header的則可使用此發送
                .build();
        /**設置回傳*/
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**如果傳送過程有發生錯誤*/
                // tvRes.setText(e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**取得回傳*/
                // GetVal = response.body().string();
                GetVal = Objects.requireNonNull(response.body()).string();
                //  tvRes.setText("GET回傳：\n" + GetVal);

                System.out.print("Get:");
                System.out.print(GetVal);
                try {
                    //split(GetVal);
                    //System.out.print(split(GetVal));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * RxJava
     **/
    protected void initEventListeners() {
        compositeDisposable.add(rxBluetooth.observeBluetoothState()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .filter(BtPredicate.in(BluetoothAdapter.STATE_ON))
                .subscribe(integer -> {
                    //start.setBackgroundColor(getResources().getColor(R.color.colorActive));
                }));

        compositeDisposable.add(rxBluetooth.observeBluetoothState()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .filter(BtPredicate.in(BluetoothAdapter.STATE_OFF, BluetoothAdapter.STATE_TURNING_OFF,
                        BluetoothAdapter.STATE_TURNING_ON))
                .subscribe(integer -> {
                    // start.setBackgroundColor(getResources().getColor(R.color.colorInactive));
                }));
        /**
         * get bluetooth Connection State
         */
        compositeDisposable.add(rxBluetooth.observeAclEvent()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(event -> {
                    switch (event.getAction()) {
                        case BluetoothDevice.ACTION_ACL_CONNECTED:
                            Log.e(TAG, "Device is connected");
                            DpBTConnState(true);
                            break;
                        case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                            Log.e(TAG, "Device is disconnected");
                            DpBTConnState(false);
                            break;
                        case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                            Log.e(TAG, "Device is Requested disconnected");
                            break;
                        default:
                            Log.e(TAG, "None Device");
                            DpBTConnState(false);
                            break;

                    }
                }));
    }

    /**
     * hide keyboard
     **/
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // 獲得當前得到焦點的View，一般情況下就是EditText（特殊情況就是軌跡求或者實體案件會移動焦點）
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                hideSoftInput(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token,
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 根據EditText所在座標和用戶點擊的座標相對比，來判斷是否隱藏鍵盤，因爲當用戶點擊EditText時沒必要隱藏
     *
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 點擊EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦點不是EditText則忽略，這個發生在視圖剛繪製完，第一個焦點不在EditView上，和用戶用軌跡球選擇其他的焦點
        return false;
    }
}
