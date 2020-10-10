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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;

import com.github.ivbaranov.rxbluetooth.RxBluetooth;
import com.github.ivbaranov.rxbluetooth.predicates.BtPredicate;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.UUID;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**rxJava**/
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private String SpeedLimit = "";
    private String address, Name;
    private String UserName;
    public StringBuffer BTSendMsg = new StringBuffer(">N00NNNN"); //[0]StartBit[1]Lock{L,F,N},[2]SpeedTen,[3]SpeedUnit,[4]SpeedConfirm,[5]Laser{T,J,N},[6]Buzzer{E,N},[7]CloudMode{Y,N}
    private int BTMsgLen = 8;
    public TextView text_Respond, SpeedView, MileageView;
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
    private RxTimerUtil rxTimer;
    private RxPostTimer rxPostTimer;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    /**
     * Setting Val
     **/
    boolean BTConnFlag;
    String id;
    boolean nb;
    public int postTime;
    int preAllM;
    /*******TimePicker*********/
    private TimePickerDialog dialog = new TimePickerDialog(this);
    /**
     * Shared
     **/
    private SharedPreferences BTWrData;
    private SharedPreferences BTReData;
    protected SharedPreferences userSetting;
    private Switch MuteNotify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        /***********SharedPreference***************/
        BTWrData = getSharedPreferences("BTMsg", MODE_PRIVATE);
        BTReData = getSharedPreferences("BTShare", MODE_PRIVATE);
        userSetting = getSharedPreferences("UserSetting", MODE_PRIVATE);
        ScanFirst();
        BTConnFlag = userSetting.getBoolean("btsta", false);
        //context = this;
        /*****************藍牙*************/
        final String deviceName = getSharedPreferences("BTDetail", MODE_PRIVATE)
                .getString("Name", "尚未選擇裝置");
        final String deviceAddress = getSharedPreferences("BTDetail", MODE_PRIVATE)
                .getString("Address", "null");
        address = deviceAddress;
        Name = deviceName;
        text_Respond = findViewById(R.id.text_Respond);
        SpeedView = findViewById(R.id.SpeedView);
        MileageView = findViewById(R.id.MileageView);
        //BTM = findViewById(R.id.id2);
        //SpeedLimit = findViewById(R.id.edit_SpeedLimit);
        loadingDialog = new LoadingDialog(MainActivity.this);
        /**********Layout Init***************/
        toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout_Main);
        toolbar.setTitle(String.format("%s %s", "藍芽裝置：" + deviceName, deviceName.equals("尚未選擇裝置") ? "" : MyAppInst.getBTState() ? "已連線" : "未連線"));

        InitNavi();
        BottomNavi_init();
        ButtonListen();
        //MyAppInst.startTimer();
        btBTConct.setText(MyAppInst.getBTState() ? "已連線" : "未連線");

        rxTimer = new RxTimerUtil();
        rxPostTimer = new RxPostTimer();
        CheckSetting();
        SpeedDialog();
        UpdateBTMsg();
        TimeTest();
        //rxBluetoothWrite.TimeTest();
        //MyAppInst.ScanDanger(Danger_Msg());
        //MyAppInst.ScanDanger();
    }

    void ScanFirst() {
        SharedPreferences shared = getSharedPreferences("is", MODE_PRIVATE);
        boolean isfer = shared.getBoolean("isfer", true);
        SharedPreferences.Editor editor = shared.edit();
        if (isfer) {
            //第一次進入跳轉
            System.out.println("is First");
            initUserSetting();
            postTime = userSetting.getInt("postTime", 15000);
            System.out.println(postTime);
            editor.putBoolean("isfer", false);
            editor.apply();
        }
    }

    ;

    void initUserSetting() {
        userSetting.edit()
                .putInt("postTime", 15000)
                .apply();
        BTReData.edit()
                .putInt("preM", 0)
                .apply();
    }

    private void CheckSetting() {
        //rxTimer.cancel();
        rxPostTimer.cancel();
        //MyAppInst.CancelPost();
        nb = userSetting.getBoolean("nb", false);
        //nb = (String) getSetting("nb","str");
        id = userSetting.getString("id", "null");
        PostFlag.Flag = userSetting.getBoolean("cloud", false);
        //NbFlag.Flag = nb.equals("phone");
        if (PostFlag.Flag && nb) BTSendMsg.replace(BTMsgLen-1, BTMsgLen, "Y");
        else BTSendMsg.replace(BTMsgLen-1, BTMsgLen, "N");
        postTime = userSetting.getInt("postTime", 15000);
        UpdateBTMsg();
        System.out.print("nb狀態:");
        System.out.println(nb);
        System.out.print("cloud狀態:");
        //System.out.println(NbFlag.Flag);
        System.out.println(PostFlag.Flag);
        System.out.print("id:");
        System.out.println(id);
        System.out.print("PostTime:");
        System.out.println(postTime);
    }

    public void DpBTConnState(boolean state) {
        btBTConct.setText(state ? "已連線" : "未連線");
        toolbar.setTitle(String.format("%s %s", "藍芽裝置：" + Name, Name.equals("尚未選擇裝置") ?
                "" : state ? "已連線" : "未連線"));
        //BTConnFlag = state;
        userSetting.edit()
                .putBoolean("btsta", state)
                .apply();
        BTConnFlag = userSetting.getBoolean("btsta", false);
        btBTConct.setEnabled(!state);
    }

    void SpeedDialog() {
        final String[] num = new String[1];
        dialog.onDialogRespond = new TimePickerDialog.OnDialogRespond() {
            @Override
            public void onRespond(String selected) {
                if (selected.equals("不限制")) selected = "0";
                num[0] = selected;
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

    void UpdateBTMsg() {
        if (addUserId()) BTWrData.edit()
                //.clear()
                .putString("SendMsg", BTSendMsg.toString())
                .apply();
        //System.out.println(getSharedPreferences("BTMsg", MODE_PRIVATE).getString("SendMsg", null));
    }

    void UpdateBTStrBuf() {
        String BTMsg = BTWrData.getString("SendMsg", "null");
        if (BTMsg.equals("null")) return;
        BTSendMsg = new StringBuffer();
        BTMsg.substring(0, BTMsgLen+1);
        BTSendMsg.append(BTMsg);
    }

    private void ButtonListen() {
        /**BT按鈕**/
        Button btBTOpen = findViewById(R.id.BTOpen);
        btBTConct = findViewById(R.id.btBTConct);
        Button btBTDiscont = findViewById(R.id.btBTDiscont);
        /**IO按鈕**/
        Button btLaser = findViewById(R.id.las_btn); //雷射按鈕
        Button btBuzz = findViewById(R.id.buzz_btn); //蜂鳴器按鈕
        Button btLck = findViewById(R.id.lck_btn); //上鎖按鈕
        /**Switch**/
        MuteNotify = findViewById(R.id.NotiMute);
        /**HTTP按鈕**/
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

        /**test btn**/
        Button testFall = findViewById(R.id.button);
        testFall.setOnClickListener(v -> {
            MyAppInst.getFall();
        });
        btBTConct.setOnClickListener(v -> {
            //loadingDialog.startLoadingDialog();
            if (address.equals("null")) {
                Toast.makeText(this, "藍芽連線失敗，請先設定藍芽", Toast.LENGTH_SHORT).show();
                return;
            }
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            if (!bluetoothAdapter.isEnabled()) {
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(intentBluetoothEnable);
                return;
            }

            if (MyAppInst.connDevice(device)) DpBTConnState(true);
            else DpBTConnState(false);
        });
        btLaser.setOnClickListener(v -> {
            BTMsg(BTMsgLen-3, BTMsgLen-2, "T", "J", LasFlag);
            int On = R.drawable.bike_open_white_48dp;
            int Off = R.drawable.ic_bike_icon_off_black;
            Button_exterior(btLaser, Off, On, BTMsgLen-3, 'J');
            UpdateBTMsg();

        });
        btBuzz.setOnClickListener(v -> {
            BTMsg(BTMsgLen-2, BTMsgLen-1, "E", "N", BuzFlag);
            int On = R.drawable.ic_baseline_volume_off_24;
            int Off = R.drawable.ic_baseline_volume_up_24;
            Button_exterior(btBuzz, Off, On, BTMsgLen-2, 'N');
            UpdateBTMsg();
            MyAppInst.DangerFlag.Flag = BuzFlag.Flag;

        });
        btLck.setOnClickListener(v -> {
            BTMsg(BTMsgLen-7, BTMsgLen-6, "L", "F", LckFlag);
            int On = R.drawable.ic_baseline_lock_24;
            int Off = R.drawable.ic_baseline_lock_open_24;
            Button_exterior(btLck, Off, On, BTMsgLen-7, 'F');

            if (!LckFlag.Flag) {
                btBuzz.setEnabled(true);
                btSpLit.setEnabled(false);
                btLaser.setEnabled(false);
            } else {
                boolean preState = BuzFlag.Flag;
                System.out.println("pre:"+preState);
                BuzFlag.Flag = false;
                int BOn = R.drawable.ic_baseline_volume_off_24;
                int BOff = R.drawable.ic_baseline_volume_up_24;
                BTSendMsg.replace(BTMsgLen-2,BTMsgLen-1,"N");
                Button_exterior(btBuzz, BOff, BOn, BTMsgLen-2, 'N');

                btBuzz.setEnabled(false);
                btSpLit.setEnabled(true);
                btLaser.setEnabled(true);
                BuzFlag.Flag = true;
                MyAppInst.DangerFlag.Flag = true;
            }
            UpdateBTMsg();
            //System.out.println("NOw:"+BTSendMsg.toString());
        });
        btSpLit.setOnClickListener(v -> {
            if (!BTConnFlag) {
                Toast.makeText(this, "請先連線藍芽", Toast.LENGTH_SHORT).show();
                return;
            }
            if (SpdFlag.Flag) dialog.showDialog();
            if (!SpdFlag.Flag) {
                BTSendMsg.replace(BTMsgLen-4, BTMsgLen-3, "N");
                userSetting.edit()
                        .putString("TopS", "0")
                        .apply();
                SpdFlag.Flag = true;

            } else {
                Toast.makeText(this, "請先設定時速", Toast.LENGTH_SHORT).show();
            }
            int On = R.drawable.ic_speed_white;
            int Off = R.drawable.ic_speed;
            Button_exterior(btSpLit, Off, On, BTMsgLen-4, 'N');
            UpdateBTMsg();
        });
        MuteNotify.setOnCheckedChangeListener((buttonView, isChecked) -> {
            MyAppInst.MuteFlag.Flag = buttonView.isChecked();
        });
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }
    private void BottomNavi_init(){
        BottomNavigationView bottomNavigationView
                = (BottomNavigationView) findViewById(R.id.include2);

        bottomNavigationView.getMenu().getItem(0).setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener((item) -> {
            switch (item.getItemId()) {
                case R.id.nav1:
                    break;
                case R.id.nav2:
                    Intent intent2 = new Intent(MainActivity.this, Notification.class);
                    startActivity(intent2);
                    break;
            }
            return true;
        });
    }
    private void InitNavi(){
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
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_map:
                Intent intent2 = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent2);
                break;
            case R.id.nav_share:
                /*if (!BTConnFlag.Flag) {
                    Toast.makeText(this, "藍芽連線失敗，請先連線藍芽", Toast.LENGTH_SHORT).show();
                    break;
                }*/
                Intent intent3 = new Intent(MainActivity.this, SettingPage.class);
                startActivity(intent3);
                onStop();
                break;
        }
        //drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onBackPressed() {
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
    }

    @Override
    protected void onStart() {
        initEventListeners();
        CheckSetting();
        MyAppInst.AutoPostVal();
        UpdateBTMsg();
        BTReData.edit()
                .putString("S", "0")
                .apply();
        super.onStart();
    }

    @Override
    protected void onStop() {
        BTReData.edit()
                .putString("S", "0")
                .apply();
        super.onStop();
    }

    protected void onDestroy() {
        SharedPreferences BTDetail = getApplicationContext().getSharedPreferences("BTDetail", MODE_PRIVATE);
        SharedPreferences.Editor BTEdit = BTDetail.edit();
        BTEdit.clear();
        BTEdit.apply();
        rxTimer.cancel();
        if (rxBluetooth != null) {
            // Make sure we're not doing discovery anymore
            rxBluetooth.cancelDiscovery();
        }
        BTReData.edit()
                .putString("S", "0")
                .apply();
        compositeDisposable.dispose();
        super.onDestroy();
    }

    /***********************藍牙副程式*******************************/

    private void BTMsg(int start, int end, String Msg1, String Msg2, FlagAddress SelectFlag) {
        UpdateBTStrBuf();
        MsgBtFlag.Flag = true;
        if (id.length() == 0) {
            Toast.makeText(this, "使用者名稱設定失敗，請先輸入id", Toast.LENGTH_SHORT).show();
            SendFlag.Flag = false;
            return;
        }
        if (!BTConnFlag) {
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

    private boolean addUserId() {
        if (id.equals("null")) {
            Toast.makeText(this, "請先設定ID,設定>>設定使用者id", Toast.LENGTH_SHORT).show();
            return false;
        }
        UserName = id;
        while (UserName.length() < 14) UserName += '@';
        if (BTSendMsg.length() >= BTMsgLen+1) {
            BTSendMsg.replace(BTMsgLen, BTMsgLen+14, UserName);
        } else {
            BTSendMsg.append(UserName);
        }
        return true;
    }

    void Speed_Limit() throws Exception {
        //String SpeedLimit = "" ;
        UpdateBTStrBuf();
        String SpLtVal;
        if (!BTConnFlag) {
            Toast.makeText(this, "請先連線藍芽", Toast.LENGTH_SHORT).show();
            return;
        }

        if (SpeedLimit.length() != 0 && /*SpdFlag.Flag && */SpeedLimit.length() < 3) {
            SpLtVal = SpeedLimit;
            System.out.print("SpLtVal is:");
            System.out.println(SpLtVal);
            btSpLit.setText(String.format("時速限制:%s", SpLtVal));
            if (SpeedLimit.length() == 1) {
                BTSendMsg.replace(BTMsgLen-6, BTMsgLen-5, "0");
                BTSendMsg.replace(BTMsgLen-5, BTMsgLen-4, SpLtVal);
            } else BTSendMsg.replace(BTMsgLen-6, BTMsgLen-4, SpLtVal);
            UpdateBTMsg();
            userSetting.edit()
                    .putString("TopS", SpLtVal)
                    .apply();
            BTMsg(BTMsgLen-4, BTMsgLen-3, "Y", "N", SpdFlag);

            int On = R.drawable.ic_speed_white;
            int Off = R.drawable.ic_speed;
            Button_exterior(btSpLit, Off, On, BTMsgLen-4, 'N');
            System.out.println(BTSendMsg);
            UpdateBTMsg();
        } else {
            BTSendMsg.replace(BTMsgLen-6, BTMsgLen-4, "00");
            userSetting.edit()
                    .putString("TopS", "0")
                    .apply();
        }

    }

    /*public AlertDialog Danger_Msg() {
        return new AlertDialog.Builder(MainActivity.this)
                .setIcon(R.drawable.ic_baseline_warning_48)
                .setTitle("警告：您的腳踏車發生異狀,請立即確認狀況")
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }*/

    /*******************************其他**********************************/
    void Button_exterior(Button btn, int one, int two, int bit, char condi) {
        if (id.length() == 0 || !BTConnFlag) return;
        btn.setCompoundDrawablesWithIntrinsicBounds(BTSendMsg.charAt(bit) == condi ?
                one : two, 0, 0, 0);
        /*btn.setBackgroundColor(BTSendMsg.charAt(bit) == condi ?
                0xFFD0D0D0 : 0xFF1A64D4);*/
        btn.setBackground(BTSendMsg.charAt(bit) == condi ?
                this.getResources().getDrawable(R.drawable.button_style_off) : this.getResources().getDrawable(R.drawable.button_style_on));

        btn.setTextColor(BTSendMsg.charAt(bit) == condi ? 0xFF606060 : 0xFFFFFFFF);
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

    public void TimeTest() {
        rxTimer.interval(200, new RxTimerUtil.IRxNext() {
            @Override
            public void doNext(Object number) {
                //Log.e("home_show_three", "======MainActivity======" + number);
                sub();
                //System.out.println(number);
            }
        });
    }

    ObservableOnSubscribe<String> observableOnSubscribe = new ObservableOnSubscribe<String>() {
        @Override
        public void subscribe(ObservableEmitter<String> emitter) {
            //System.out.println("SVMV已經訂閱：subscribe，获取发射器");
            // if (RxLocation != null)
            //    emitter.onNext(RxLocation);
            //
            String Sval = BTReData.getString("S", "0");
            //String Mval = BTReData.getString("M",null);
            int AllM = BTReData.getInt("Mi", 0) / 100;
            //System.out.println("Sval:"+Sval+','+"AllM:"+AllM);
            if (Sval != null && AllM != 0) {
                if (preAllM == AllM && Sval.equals("0")){
                    emitter.onNext("000"+','+AllM);
                    return;
                }


                //if(BTSendMsg.equals("null"))System.out.println("BTReData null");
                emitter.onNext(Sval + ',' + AllM);
                Sval = null;
                preAllM = AllM;
                AllM = 0;
                //System.out.println("SVMV信號發射：onComplete" + Sval + ',' + (AllM*1.21));
            }

            // System.out.println("SVMV信號發射：onComplete" + Sval + ',' + AllM);
        }
    };
    /**
     * 時速,里程數顯示串流
     */
    Observable<String> observable = Observable.create(observableOnSubscribe);

    final Disposable[] disposable = new Disposable[1];

    Observer<String> observer = new Observer<String>() {
        @Override
        public void onSubscribe(Disposable d) {
            disposable[0] = d;
            //System.out.println("已经订阅：onSubscribe，获取解除器");
        }

        @Override
        public void onNext(String string) {
            //System.out.println("SVMV信号接收：onNext " + string);
            int i = 0, b = 0;
            int[] pos = new int[4];
            String speed;
            String mileage;
            int intMileage;
            //System.out.println("St:"+string);
            //if(!string.equals("0")) {
            while (i < string.length()) {
                if (string.charAt(i) == ',') {
                    pos[b] = i;
                    b++;
                }
                i++;

            }
            if (string.charAt(0) == '0' && string.charAt(1) != ',') pos[1] = 1;
            else pos[1] = 0;
            if (string.charAt(0) == '0' && string.charAt(1) == '0' && string.charAt(2) != ',')
                pos[1] = 2;

            //System.out.println(pos[1]+','+pos[0]);
            if (!string.equals("0")) {
                speed = string.substring(pos[1], pos[0]);
                mileage = string.substring(pos[0] + 1);
            }else {
                speed = "0";
                mileage = "0";
            }
            intMileage = Integer.parseInt(mileage);
            if (intMileage / 1000 != 0) {
                    StringBuffer mile = new StringBuffer();
                    mile.append(mileage);
                    mile.insert(mile.length() - 3, '.');
                    MileageView.setText(String.format("總里程數:%s公里", mile));
                } else {
                    MileageView.setText(String.format("總里程數:%s公尺", mileage));
                }

                // System.out.println(speed);
                // System.out.println(mileage);
                SpeedView.setText(String.format("現在時速:%skm/h", speed));


            }

            @Override
            public void onError (Throwable e){
                //System.out.println("SVMV信号接收：onError " + e.getMessage());
                cancel();
            }

            @Override
            public void onComplete () {
                //System.out.println("SVMV信号接收：onComplete");
            }
        }

        ;

        public void sub() {
            //System.out.println("SVMV開始訂閱：subscribe");
            observable.subscribe(observer);
        }

        public void cancel() {
            // System.out.println("SVMV取消訂閱：unsubscribe");
            if (disposable[0] != null)
                disposable[0].dispose();
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
