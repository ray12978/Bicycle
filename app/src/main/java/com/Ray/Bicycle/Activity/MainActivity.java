package com.Ray.Bicycle.Activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.Ray.Bicycle.Util.FlagAddress;
import com.Ray.Bicycle.Util.MyApp;
import com.Ray.Bicycle.R;
import com.Ray.Bicycle.RxJava.RxPostTimer;
import com.Ray.Bicycle.RxJava.RxTimerUtil;
import com.Ray.Bicycle.Component.TimePickerDialog;
import com.Ray.Bicycle.View.BottomNavigation;
import com.github.ivbaranov.rxbluetooth.RxBluetooth;
import com.github.ivbaranov.rxbluetooth.predicates.BtPredicate;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * rxJava
 **/

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private String SpeedLimit = "";
    private String BTAddress, BTName;
    private String UserName;
    public StringBuffer BTSendMsg = new StringBuffer(">N00NNNN"); //[0]StartBit[1]Lock{L,F,N},[2]SpeedTen,[3]SpeedUnit,[4]SpeedConfirm,[5]Laser{T,J,N},[6]Buzzer{E,N},[7]CloudMode{Y,N}
    private int BTMsgLen = 8;

    /**
     * Bluetooth
     **/
    private BluetoothAdapter bluetoothAdapter;
    private Button btSpLit;
    private Button btLaser; //雷射按鈕
    private Button btBuzz; //蜂鳴器按鈕
    private Button btLck; //上鎖按鈕
    /*********************Notify*********************/
    private static final String TAG = MainActivity.class.getSimpleName();

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
    private MaterialToolbar materialToolbar;
    public TextView text_Respond, SpeedView, MileageView, MileUnitTV;
    private TextView BTInfoText, BTStaText;
    private ImageView BTLight;

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
    public SharedPreferences userSetting;
    private SharedPreferences FallData;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main); //test
        super.onCreate(savedInstanceState);
        Initialize();
        InitToolbar();
        InitNavi();
        BottomNavInit();
        ButtonListen();
        CheckSetting();
        SpeedDialog();
        UpdateBTMsg();
        TimeTest();
        initBTSta();
        setBTInfoText(BTName);
    }

    private void Initialize() {

        /**************View Bind***************/
        BTStaText = findViewById(R.id.BTStaTV);
        BTInfoText = findViewById(R.id.BTInfoTV);
        BTLight = findViewById(R.id.BTStaLight);
        text_Respond = findViewById(R.id.text_Respond);
        SpeedView = findViewById(R.id.SpeedView);
        MileageView = findViewById(R.id.MileageView);
        MileUnitTV = findViewById(R.id.MileUnit);
        /***********SharedPreference***************/
        BTWrData = getSharedPreferences("BTMsg", MODE_PRIVATE);
        BTReData = getSharedPreferences("BTShare", MODE_PRIVATE);
        userSetting = getSharedPreferences("UserSetting", MODE_PRIVATE);
        ScanFirst();
        FallData = getSharedPreferences("FallData", MODE_PRIVATE);

        /*****************藍牙*************/
        final String deviceName = getSharedPreferences("BTDetail", MODE_PRIVATE)
                .getString("Name", getString(R.string.device_select_not_yet_text));
        final String deviceAddress = getSharedPreferences("BTDetail", MODE_PRIVATE)
                .getString("Address", "null");
        BTAddress = deviceAddress;
        BTName = deviceName;


        rxTimer = new RxTimerUtil();
        rxPostTimer = new RxPostTimer();


    }


    void ScanFirst() {
        SharedPreferences shared = getSharedPreferences("is", MODE_PRIVATE);
        boolean isFirst = shared.getBoolean("isFirst", true);
        SharedPreferences.Editor editor = shared.edit();
        if (isFirst) {
            //第一次進入跳轉
            System.out.println("is First");
            initUserSetting();
            InitFallData();
            postTime = userSetting.getInt("postTime", 15000);
            System.out.println(postTime);
            editor.putBoolean("isFirst", false);
            editor.apply();
        }
    }

    private void InitToolbar() {
        materialToolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout_Main);

        materialToolbar.setOnMenuItemClickListener(item -> {
            int ID = item.getItemId();
            if (ID == R.id.ConnBT) {
                if (!bluetoothAdapter.isEnabled()) {
                    Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(intentBluetoothEnable);
                }
                if (BTAddress.equals("") || BTAddress.equals("null")) {
                    makeSnack(getString(R.string.select_correct_device_first_text));
                    return false;
                }
                if (!MyApp.getConnected()) {
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(BTAddress);
                    MyAppInst.connDevice(device);
                }

            } else if (ID == R.id.DisconnBT)
                if (MyApp.getConnected()) MyAppInst.disconnect();

            return false;
        });
    }

    protected void InitFallData() {
        FallData = getSharedPreferences("FallData", MODE_PRIVATE);
        int i = 1;
        while (i < 13) {
            String index = String.valueOf(i);
            String Fall = "Fall" + index;
            FallData.edit()
                    .putInt(Fall, 0)
                    .apply();
            i++;
        }
        System.out.println(FallData.getBoolean("11", false));
    }

    void initUserSetting() {
        userSetting.edit()
                .putInt("postTime", 15000)
                .apply();
        BTReData.edit()
                .putInt("preM", 0)
                .apply();
    }

    void initButton() {
        int LasOn = R.drawable.bike_open_white_48dp;
        int LasOff = R.drawable.ic_bike_icon_off_black;
        Button_exterior(btLaser, LasOff, LasOn, BTMsgLen - 3, 'J');

        int On = R.drawable.ic_baseline_volume_off_24;
        int Off = R.drawable.ic_baseline_volume_up_24;
        Button_exterior(btBuzz, Off, On, BTMsgLen - 2, 'N');

        int BuzzOn = R.drawable.ic_baseline_volume_off_24;
        int BuzzOff = R.drawable.ic_baseline_volume_up_24;
        Button_exterior(btBuzz, BuzzOff, BuzzOn, BTMsgLen - 2, 'N');

        int LockOn = R.drawable.ic_baseline_lock_24;
        int LockOff = R.drawable.ic_baseline_lock_open_24;
        Button_exterior(btLck, LockOff, LockOn, BTMsgLen - 7, 'F');
    }

    private void CheckSetting() {
        rxPostTimer.cancel();
        nb = userSetting.getBoolean("nb", false);
        id = userSetting.getString("id", "null");
        PostFlag.Flag = userSetting.getBoolean("cloud", false);
        if (PostFlag.Flag && nb) BTSendMsg.replace(BTMsgLen - 1, BTMsgLen, "Y");
        else BTSendMsg.replace(BTMsgLen - 1, BTMsgLen, "N");
        postTime = userSetting.getInt("postTime", 15000);
        UpdateBTMsg();
        /*System.out.print("nb狀態:");
        System.out.println(nb);
        System.out.print("cloud狀態:");
        System.out.println(PostFlag.Flag);
        System.out.print("id:");
        System.out.println(id);
        System.out.print("PostTime:");
        System.out.println(postTime);*/
    }

    private void setBTInfoText(@NonNull String BTname) {
        if (BTname.equals("Bicycle")) {
            BTInfoText.setText(R.string.device_text);
        } else if (BTname.equals(getString(R.string.device_select_not_yet_text))) {
            BTInfoText.setText(R.string.device_select_not_yet_text);
        } else {
            BTInfoText.setText(R.string.unknown_device_text);
        }
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

    private void makeSnack(String msg) {
        Snackbar snackbar = Snackbar.make(SpeedView, msg, Snackbar.LENGTH_LONG)
                .setAction("OK", view -> Log.i("SNACKBAR", "OK"));
        snackbar.show();
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
        BTMsg.substring(0, BTMsgLen + 1);
        BTSendMsg.append(BTMsg);
    }

    private void ButtonListen() {
        /**IO按鈕**/
        btLaser = findViewById(R.id.las_btn); //雷射按鈕
        btBuzz = findViewById(R.id.buzz_btn); //蜂鳴器按鈕
        btLck = findViewById(R.id.lck_btn); //上鎖按鈕

        /**Switch**/
        SwitchMaterial muteNotify = findViewById(R.id.NotiMute);

        /**HTTP按鈕**/
        btSpLit = findViewById(R.id.SpLit_btn);
        btBuzz.setEnabled(false);

        /**藍牙按鈕動作**/
        btLaser.setOnClickListener(v -> {
            BTMsg(BTMsgLen - 3, BTMsgLen - 2, "T", "J", LasFlag);
            int On = R.drawable.bike_open_white_48dp;
            int Off = R.drawable.ic_bike_icon_off_black;
            Button_exterior(btLaser, Off, On, BTMsgLen - 3, 'J');
            UpdateBTMsg();

        });
        btBuzz.setOnClickListener(v -> {
            BTMsg(BTMsgLen - 2, BTMsgLen - 1, "E", "N", BuzFlag);
            int On = R.drawable.ic_baseline_volume_off_24;
            int Off = R.drawable.ic_baseline_volume_up_24;
            Button_exterior(btBuzz, Off, On, BTMsgLen - 2, 'N');
            UpdateBTMsg();
            MyAppInst.DangerFlag.Flag = BuzFlag.Flag;

        });
        btLck.setOnClickListener(v -> {
            BTMsg(BTMsgLen - 7, BTMsgLen - 6, "L", "F", LckFlag);
            int On = R.drawable.ic_baseline_lock_24;
            int Off = R.drawable.ic_baseline_lock_open_24;
            Button_exterior(btLck, Off, On, BTMsgLen - 7, 'F');

            if (!LckFlag.Flag) {
                btBuzz.setEnabled(true);
                btSpLit.setEnabled(false);
                btLaser.setEnabled(false);
            } else {
                boolean preState = BuzFlag.Flag;
                System.out.println("pre:" + preState);
                BuzFlag.Flag = false;
                int BOn = R.drawable.ic_baseline_volume_off_24;
                int BOff = R.drawable.ic_baseline_volume_up_24;
                BTSendMsg.replace(BTMsgLen - 2, BTMsgLen - 1, "N");
                Button_exterior(btBuzz, BOff, BOn, BTMsgLen - 2, 'N');

                btBuzz.setEnabled(false);
                btSpLit.setEnabled(true);
                btLaser.setEnabled(true);
                BuzFlag.Flag = true;
                MyAppInst.DangerFlag.Flag = true;
            }
            UpdateBTMsg();
        });
        btSpLit.setOnClickListener(v -> {
            if (!MyApp.getConnected()) {
                makeSnack("請先連線藍芽");
                return;
            }
            if (SpdFlag.Flag) dialog.showDialog();
            if (!SpdFlag.Flag) {
                BTSendMsg.replace(BTMsgLen - 4, BTMsgLen - 3, "N");
                userSetting.edit()
                        .putString("TopS", "0")
                        .apply();
                SpdFlag.Flag = true;

            } else {
                makeSnack("請先設定時速");
            }
            int On = R.drawable.ic_speed_white;
            int Off = R.drawable.ic_speed;
            Button_exterior(btSpLit, Off, On, BTMsgLen - 4, 'N');
            UpdateBTMsg();
        });
        muteNotify.setOnCheckedChangeListener((buttonView, isChecked) -> {
            MyAppInst.MuteFlag.Flag = buttonView.isChecked();
        });
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    private void getBTStatus() {
        MyAppInst.onConnectedDevice = connected -> {
            if (!connected) makeSnack(getString(R.string.bluetooth_conn_failed_text));
        };
    }

    private void BottomNavInit() {
        BottomNavigationView MyBtmNav = findViewById(R.id.include2);
        BottomNavigation BtmNav = new BottomNavigation(this, MyBtmNav, 0);
        BtmNav.init();
    }

    private void InitNavi() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, materialToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    /***********Navigation*************/
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_map:
                Intent intent2 = new Intent(this, MapsActivity.class);
                startActivity(intent2);
                break;
            case R.id.nav_share:
                Intent intent3 = new Intent(this, SettingPage.class);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        getBTStatus();
        initEventListeners();
        CheckSetting();
        MyAppInst.AutoPostVal();
        MyAppInst.FallListen();
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
            makeSnack("使用者名稱設定失敗，請先輸入id");
            SendFlag.Flag = false;
            return;
        }
        if (!MyApp.getConnected()) {
            makeSnack("藍芽連線失敗，請先連線藍芽");
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
            makeSnack("請先設定ID,設定>>設定使用者id");
            return false;
        }
        UserName = id;
        while (UserName.length() < 14) UserName += '@';
        if (BTSendMsg.length() >= BTMsgLen + 1) {
            BTSendMsg.replace(BTMsgLen, BTMsgLen + 14, UserName);
        } else {
            BTSendMsg.append(UserName);
        }
        return true;
    }

    void Speed_Limit() throws Exception {
        //String SpeedLimit = "" ;
        UpdateBTStrBuf();
        String SpLtVal;
        if (!MyApp.getConnected()) {
            makeSnack("請先連線藍芽");
            return;
        }

        if (SpeedLimit.length() != 0 && /*SpdFlag.Flag && */SpeedLimit.length() < 3) {
            SpLtVal = SpeedLimit;
            System.out.print("SpLtVal is:");
            System.out.println(SpLtVal);
            btSpLit.setText(String.format("時速限制:%s", SpLtVal));
            if (SpeedLimit.length() == 1) {
                BTSendMsg.replace(BTMsgLen - 6, BTMsgLen - 5, "0");
                BTSendMsg.replace(BTMsgLen - 5, BTMsgLen - 4, SpLtVal);
            } else BTSendMsg.replace(BTMsgLen - 6, BTMsgLen - 4, SpLtVal);
            UpdateBTMsg();
            userSetting.edit()
                    .putString("TopS", SpLtVal)
                    .apply();
            BTMsg(BTMsgLen - 4, BTMsgLen - 3, "Y", "N", SpdFlag);

            int On = R.drawable.ic_speed_white;
            int Off = R.drawable.ic_speed;
            Button_exterior(btSpLit, Off, On, BTMsgLen - 4, 'N');
            System.out.println(BTSendMsg);
            UpdateBTMsg();
        } else {
            BTSendMsg.replace(BTMsgLen - 6, BTMsgLen - 4, "00");
            userSetting.edit()
                    .putString("TopS", "0")
                    .apply();
        }

    }

    /*******************************其他**********************************/
    void Button_exterior(Button btn, int one, int two, int bit, char condi) {
        if (id.length() == 0 || !MyApp.getConnected()) return;
        btn.setCompoundDrawablesWithIntrinsicBounds(BTSendMsg.charAt(bit) == condi ?
                one : two, 0, 0, 0);
        btn.setBackground(BTSendMsg.charAt(bit) == condi ?
                this.getResources().getDrawable(R.drawable.button_style_off) : this.getResources().getDrawable(R.drawable.button_style_on));

        btn.setTextColor(BTSendMsg.charAt(bit) == condi ? 0xFF606060 : 0xFFFFFFFF);
    }

    private void initBTSta() {
        if (MyApp.getConnected()) {
            BTStaText.setText(R.string.device_connected);
            BTLight.setImageResource(R.drawable.drawable_circle);
        } else {
            BTStaText.setText(R.string.device_not_connected);
            BTLight.setImageResource(R.drawable.drawable_circle_gray);
        }
    }

    private void setBTSta(boolean connected) {
        if (connected) {
            BTStaText.setText(R.string.device_connected);
            MyApp.isConnected = true;
            BTLight.setImageResource(R.drawable.drawable_circle);
            System.out.println("connected");
        }
        if (!connected) {
            BTStaText.setText(R.string.device_not_connected);
            MyApp.isConnected = false;
            BTLight.setImageResource(R.drawable.drawable_circle_gray);
            System.out.println("not connected");
            MyApp.isDisconnected();
        }
    }

    /**
     * RxJava
     **/
    protected void initEventListeners() {
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
                            //DpBTConnState(true);
                            setBTSta(true);
                            break;
                        case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                            Log.e(TAG, "Device is disconnected");
                            //DpBTConnState(false);
                            setBTSta(false);
                            break;
                        case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                            Log.e(TAG, "Device is Requested disconnected");
                            break;
                        default:
                            Log.e(TAG, "None Device");
                            //DpBTConnState(false);
                            setBTSta(false);
                            break;

                    }
                }));
        compositeDisposable.add(rxBluetooth.observeBluetoothState()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .filter(BtPredicate.in(BluetoothAdapter.STATE_ON))
                .subscribe(integer -> initBTSta()));
        compositeDisposable.add(rxBluetooth.observeBluetoothState()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .filter(BtPredicate.in(BluetoothAdapter.STATE_OFF))
                .subscribe(integer -> {
                    setBTSta(false);
                    BTStaText.setText(R.string.bluetooth_not_open);
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
            String Sval = BTReData.getString("S", "0");
            //String Mval = BTReData.getString("M",null);
            int AllM = BTReData.getInt("Mi", 0) / 100;
            //System.out.println("Sval:"+Sval+','+"AllM:"+AllM);
            if (Sval != null && AllM != 0) {
                if (preAllM == AllM && Sval.equals("0")) {
                    emitter.onNext("000" + ',' + AllM);
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
            } else {
                speed = "0";
                mileage = "0";
            }
            intMileage = Integer.parseInt(mileage);
            if (intMileage / 1000 != 0) {
                StringBuffer mile = new StringBuffer();
                mile.append(mileage);
                mile.insert(mile.length() - 3, '.');
                //MileageView.setText(String.format("總里程數:%s公里", mile));
                MileageView.setText(mile);
                MileUnitTV.setText(R.string.kilo_text);
            } else {
                //MileageView.setText(String.format("總里程數:%s公尺", mileage));
                MileageView.setText(mileage);
                MileUnitTV.setText(R.string.meter_text);
            }

            // System.out.println(speed);
            // System.out.println(mileage);
            //SpeedView.setText(String.format("現在時速:%skm/h", speed));
            SpeedView.setText(speed);

        }

        @Override
        public void onError(Throwable e) {
            //System.out.println("SVMV信号接收：onError " + e.getMessage());
            cancel();
        }

        @Override
        public void onComplete() {
            //System.out.println("SVMV信号接收：onComplete");
        }
    };

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
