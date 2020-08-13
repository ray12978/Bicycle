package com.Ray.Bicycle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Handler;
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
import android.os.HandlerThread;
import android.util.Log;
import android.view.MenuItem;
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
import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //private final UUID uuid = UUID.fromString("8c4102d5-f0f9-4958-806e-7ba5fd54ce7c");
    private final UUID serialPortUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private EditText id;
    private EditText BTM;
    private String SpeedLimit = "";
    private String address;
    public String SVal,PVal,MVal,TVal,GetVal;
    private String UserName;
    public StringBuffer BTSendMsg = new StringBuffer("N00NNNN"); //[0]Lock,[1]SpeedTen,[2]SpeedUnit,[3]SpeedConfirm,[4]Laser,[5]Buzzer,[6]CloudMode
    public StringBuffer BTValTmp = new StringBuffer();
    public byte[] buffer = new byte[256];
    public TextView text_Respond;
    /**Bluetooth**/
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private TextView textContent;
    private Button btBTConct;
    /*********************Notify*********************/
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TEST_NOTIFY_ID = "Bicycle_Danger_1";
    private static final int NOTYFI_REQUEST_ID = 300;
    public LoadingDialog loadingDialog;
    /*****************StringProcess******************/
    private int[] StrPosition = new int[4];
    /******************ButtonFlag********************/
    //FlagAddress MsgStaFlag = new FlagAddress(true);
    FlagAddress MsgBtFlag = new FlagAddress(true);
    FlagAddress LasFlag = new FlagAddress(true);
    FlagAddress LckFlag = new FlagAddress(true);
    FlagAddress BuzFlag = new FlagAddress(true);
    FlagAddress SpdFlag = new FlagAddress(true);
    FlagAddress DanFlag = new FlagAddress(false);
    FlagAddress StrFlag = new FlagAddress(false);
    FlagAddress PostFlag = new FlagAddress(false);
    /*******************Layout***********************/
    private DrawerLayout drawer;
    /********************Runnable********************/
    private Handler mUI_Handler=new Handler();
    private Handler mThreadHandler;
    private HandlerThread mThread;

    private Thread reader = new Thread(new Runnable() {
        @Override
        public void run() {
            readerStop = false;
            while (!readerStop && !DanFlag.Flag) {
                //read();
                if (!LckFlag.Flag){
                    Save_Val(BTValTmp);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Danger_Str();
                    if(DanFlag.Flag)Danger();
                }
                Save_Val(BTValTmp);
            }

        }
    });

    private Thread danger = new Thread(new Runnable() {
        @Override
        public void run() {
            readerStop = false;
        }
    });

    private boolean readerStop;
   // @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*****************藍牙*************/
        final String deviceName = getIntent().getStringExtra("DeviceName");
        final String deviceAddress = getIntent().getStringExtra("DeviceAddress");
        text_Respond = findViewById(R.id.text_Respond);
        String name = deviceName != null ? deviceName : "尚未選擇裝置";
        address = deviceAddress;
        setTitle(String.format("%s (%s)", address, name));
        id = findViewById(R.id.id);
        BTM = findViewById(R.id.id2);
        //SpeedLimit = findViewById(R.id.edit_SpeedLimit);
        textContent = findViewById(R.id.textContent);
        loadingDialog = new LoadingDialog(MainActivity.this);
        /***********Other***************/
        MyApp.appInstance.startTimer();
        /**********Layout***************/
        Toolbar toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        setSupportActionBar(toolbar);
        //toolbar.setTitle(String.format("%s (%s)", address, name));
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        setSpdPick();
        ButtonListen();
    }

    private void ButtonListen(){
        /**BT按鈕**/
        Button btBTSend = findViewById(R.id.btBTSend);
        Button btBTOpen = findViewById(R.id.BTOpen);
        btBTConct = findViewById(R.id.btBTConct);
        Button btBTDiscont = findViewById(R.id.btBTDiscont);
        Button btClear = findViewById(R.id.btClr);
        Button btDisplay = findViewById(R.id.btDisplay);
        Switch SWPost = findViewById(R.id.SWPost);
        /**IO按鈕*/
        Button btLaser = findViewById(R.id.las_btn); //雷射按鈕
        Button btBuzz = findViewById(R.id.buzz_btn); //蜂鳴器按鈕
        Button btLck = findViewById(R.id.lck_btn); //上鎖按鈕
        /**HTTP按鈕*/
        Button btPost = findViewById(R.id.button_POST);
        Button btGET = findViewById(R.id.button_GET);
        Button btSpLit = findViewById(R.id.SpLit_btn);
        btBTDiscont.setOnClickListener(view -> disconnect());
        btBuzz.setEnabled(false);
        id.setOnEditorActionListener((view, actionId, event) -> {
            BTSend(id.getText().toString());
            return false;
        });
        BTM.setOnEditorActionListener((view, actionId, event) -> {
            BTSend(BTM.getText().toString());
            return false;
        });
        /**藍牙按鈕動作**/
        btBTOpen.setOnClickListener(v -> {
            Intent BTListAct = new Intent(MainActivity.this, ConnectActivity.class);
            startActivity(BTListAct);
        });
        btBTSend.setOnClickListener(v -> {
            BTSend(BTM.getText().toString());

        });
        btBTConct.setOnClickListener(v -> {
            //loadingDialog.startLoadingDialog();
            BTConnect();
            //loadingDialog.dismissDialog();
        });
        btLaser.setOnClickListener(v -> {
            BTMsg(4, 5, "T", "J", LasFlag);
            int On = R.drawable.bike_open_white_48dp;
            int Off = R.drawable.ic_bike_icon_off_black;
            Button_exterior(btLaser, Off, On, 4, 'J');
            BTSend(BTSendMsg.toString());
            //BTSend("aaa");
            //loadingDialog.startLoadingDialog();
            try {

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            str_process();
            sendPOST();
        });
        btBuzz.setOnClickListener(v -> {
            BTMsg(5, 6, "E", "N", BuzFlag);
            int On = R.drawable.ic_baseline_volume_off_24;
            int Off = R.drawable.ic_baseline_volume_up_24;
            Button_exterior(btBuzz, Off, On, 5, 'N');
            BTSend(BTSendMsg.toString());
            //BTSend("bbb");
            try {

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            str_process();
            sendPOST();
        });
        btLck.setOnClickListener(v -> {
            BTMsg(0, 1, "L", "F", LckFlag);
            int On = R.drawable.ic_baseline_lock_24;
            int Off = R.drawable.ic_baseline_lock_open_24;
            Button_exterior(btLck, Off, On, 0, 'F');
            BTSend(BTSendMsg.toString());
            try {
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
            Speed_Limit();
            int On = R.drawable.ic_speed_white;
            int Off = R.drawable.ic_speed;
            Button_exterior(btSpLit, Off, On, 3, 'N');

        });
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btClear.setOnClickListener(v -> {
            textContent.setText("");
            BTValTmp.delete(0, BTValTmp.length());
        });
        btDisplay.setOnClickListener(v -> {
            Toast.makeText(this, BTValTmp, Toast.LENGTH_LONG).show();

            System.out.println("BTTmp:");
            System.out.println(BTValTmp.toString());
            System.out.println("PostFlag:");
            System.out.println(PostFlag);
            Log.d(BTValTmp.toString(), "Tmp");
            Log.d(SVal, "S");
            Log.d(MVal, "M");
            Log.d(TVal, "T");
            Log.d(PVal, "P");
            System.out.println("LckFlag:");
            System.out.println(LckFlag.Flag);
        });
        SWPost.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PostFlag.Flag = buttonView.isChecked();
        });
        /**HTTP按鈕動作**/
        /**傳送POST**/
        btPost.setOnClickListener(v -> {
            if (id.length() != 0) sendPOST();
            else {
                Toast.makeText(this, "使用者名稱設定失敗，請先輸入id", Toast.LENGTH_SHORT).show();
            }
        });
        btGET.setOnClickListener(v -> {
            sendGET();
        });
    }

    private void setSpdPick(){
        NumberPicker SpdPick = findViewById(R.id.SpeedPicker);
        final String[] SpdList = getResources().getStringArray(R.array.Speed_List);
        SpdPick.setMinValue(0);
        SpdPick.setMaxValue(SpdList.length - 1);
        SpdPick.setDisplayedValues(SpdList);
        SpdPick.setValue(0); // 設定預設位置
        SpdPick.setWrapSelectorWheel(false); // 是否循環顯示
        SpdPick.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // 不可編輯
        SpdPick.setOnValueChangedListener((picker, oldVal, newVal) -> {
        String[] aaa = SpdPick.getDisplayedValues();
        int a = SpdPick.getValue();
        SpeedLimit = SpdList[a];
        System.out.println(aaa);
        System.out.println(SpdList[a]);
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
            case R.id.nav_profile:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();
                break;
            case R.id.nav_share:
                Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_send:
                Toast.makeText(this, "Send", Toast.LENGTH_SHORT).show();
                break;
        }
        //drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onBackPressed() {
        /*if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }*/
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("確定要退出嗎?")
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
        readerStop = true;
        DanFlag.Flag = false;
        //loadingDialog.startLoadingDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reader.start();
//        loadingDialog.dismissDialog();
        //Danger.start();
    }

    /***********************藍牙副程式*******************************/
    private void BTConnect() {
        if(address == null){
            btBTConct.setText("未選擇裝置");
            return;
        }
        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        try {

            //loadingDialog.startLoadingDialog();
            btBTConct.setText("連線中");
            socket = device.createRfcommSocketToServiceRecord(serialPortUUID);
            socket.connect();
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            btBTConct.setText("已連線");

        } catch (IOException e) {
            e.printStackTrace();
            btBTConct.setText("連線超時");
        }
    }

    private void disconnect() {
        if (socket == null) return;

        try {
            socket.close();
            socket = null;
            inputStream = null;
            outputStream = null;
            btBTConct.setText("未連線");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void BTSend(String BTMsg) {
        if (outputStream == null) return;

        try {
            for(int i =0;i<5;i++) {
                outputStream.write(BTMsg.getBytes());
                outputStream.flush();
            }
            System.out.print("BT:");
            System.out.print(BTMsg);
            System.out.print(",");
            System.out.print(BTMsg.getBytes());
            BTM.setText("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void read() {
        if (inputStream == null) return;
        try {
            if (inputStream.available() <= 0) return;
            String a = new String(buffer, 0, inputStream.read(buffer));
            Log.d(a, "read: ");
            textContent.append(a);
            Thread.sleep(100);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void Save_Val(@NotNull StringBuffer StrBufTmp) {
        if (inputStream == null) return;
        try {
           // if (inputStream.available() <= 0)return;
            String a = new String(buffer, 0, inputStream.read(buffer));
            StrBufTmp.append(a);
            StrFlag.Flag = false;
            //Thread.sleep(100);
        } catch (IOException /*| InterruptedException*/ e) {
            e.printStackTrace();
        }
    }

    public void str_process() {
        int b = 0;
        //BTValTmp = new StringBuffer("S123M456T789P147"); //test
        //BTValTmp = new StringBuffer("B1"); //test
        if(BTValTmp.length() == 0)return;
        if (BTValTmp.toString().charAt(0) == 'S') {
            for (int i = 0; i < BTValTmp.length(); i++) {
                if (BTValTmp.toString().getBytes()[i] > 57) {
                    StrPosition[b] = i;
                    b++;
                }
            }
            SVal = BTValTmp.toString().substring(StrPosition[0] + 1, StrPosition[1]).trim();
            MVal = BTValTmp.toString().substring(StrPosition[1] + 1, StrPosition[2]).trim();
            TVal = BTValTmp.toString().substring(StrPosition[2] + 1, StrPosition[3]).trim();
            PVal = BTValTmp.toString().substring(StrPosition[3] + 1).trim();
            Log.d(BTValTmp.toString(), "Tmp");
            Log.d(SVal, "S");
            Log.d(MVal, "M");
            Log.d(TVal, "T");
            Log.d(PVal, "P");
            BTValTmp.delete(0, BTValTmp.length());
        } /*else if (BTValTmp.toString().charAt(0) == 'B') {
            String Status = BTValTmp.toString().substring(1, 2).trim();
            BTValTmp.delete(0, BTValTmp.length());
            DanFlag.Flag = Status.equals("1");
            System.out.println(DanFlag.Flag);
            //if (DanFlag.Flag) Danger_Msg();
        }*/
    }
    public void Danger_Str(){
        int index=0;
        if(BTValTmp.length() == 0)return;
        while (BTValTmp.length() != 0) {
            if(index<BTValTmp.length()-1)index++;
            System.out.println(BTValTmp.toString());
            if(BTValTmp.length()>=2) {
                if (BTValTmp.toString().charAt(index - 1) == 'B' && BTValTmp.toString().charAt(index) == '1') {
                    BTValTmp.delete(0, BTValTmp.length());
                    DanFlag.Flag = true;
                    BTValTmp.delete(0, BTValTmp.length());
                    System.out.println("danger!!");
                }
                BTValTmp.delete(0, BTValTmp.length());

            }
        }
    }

    private void BTMsg(int start, int end, String Msg1, String Msg2, FlagAddress SelectFlag) {
        MsgBtFlag.Flag = true;
        if(id.length() == 0){
            Toast.makeText(this, "使用者名稱設定失敗，請先輸入id", Toast.LENGTH_SHORT).show();
            return;
        }
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
        if(PostFlag.Flag)BTSendMsg.replace(6, 7, "Y");
        else BTSendMsg.replace(6, 7, "N");
        UserName = id.getText().toString();
        while (UserName.length()<16) UserName+='@';
        if(BTSendMsg.length()>=8) {
            BTSendMsg.replace(7,23, UserName);
        }
        else {
            BTSendMsg.append(UserName);
        }
        System.out.println(BTSendMsg);
    }

    void Speed_Limit() {
        //String SpeedLimit = "" ;
        if (SpeedLimit.length() != 0 && SpdFlag.Flag && SpeedLimit.length() < 3) {
            String SpLtVal = SpeedLimit;
            if (SpeedLimit.length() == 1) {
                BTSendMsg.replace(1, 2, "0");
                BTSendMsg.replace(2, 3, SpLtVal);
            } else {
                BTSendMsg.replace(1, 3, SpLtVal);
            }
            BTMsg(3, 4, "Y", "N", SpdFlag);
            System.out.println(BTSendMsg);
            BTSend(BTSendMsg.toString());
            try {

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            str_process();
            sendPOST();
        } else {
            if (!SpdFlag.Flag) {
                BTSendMsg.replace(3, 4, "N");
                SpdFlag.Flag = true;
                System.out.println(BTSendMsg);
                BTSend(BTSendMsg.toString());
                try {

                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                str_process();
                sendPOST();
            } else {
                Toast.makeText(this, "請先設定時速", Toast.LENGTH_SHORT).show();
            }
        }

    }

    /*******************************其他**********************************/
    void Button_exterior(Button btn, int one, int two, int bit, char condi) {
        if(id.length() == 0)return;
        btn.setCompoundDrawablesWithIntrinsicBounds(0, BTSendMsg.charAt(bit) == condi ?
                one : two, 0, 0);
        btn.setBackgroundColor(BTSendMsg.charAt(bit) == condi ?
                0xFFD0D0D0 : 0xFF1A64D4);
        btn.setTextColor(BTSendMsg.charAt(bit) == condi ? 0xFF606060 : 0xFFFFFFFF);
    }

    /**
     * 手機震動
     **/
    public void setVibrate(int time) {
        Vibrator myVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        myVibrator.vibrate(time);
    }

    void Danger() {
        if(!DanFlag.Flag)return;
        setVibrate(1000);
        //DanFlag.Flag = false;
        showNotification();
        //mediaPlayer.start();
        //if(Alert.Flag)Danger_Msg();
        //loadingDialog.startLoadingDialog();
        DanFlag.Flag = false;
    }

    public void showNotification() {
        Log.d(TAG, "showNotification: ");
        try {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                    NOTYFI_REQUEST_ID,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            final Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getApplicationContext().getPackageName() + "/" + R.raw.sound);
            System.out.println(soundUri);
            //final Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sound);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build();
            NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
            Notification.Builder builder = new Notification.Builder(this)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle("您的腳踏車發生異常狀況")
                    .setContentText("請立即前往確認")
                    .setLights(0xff00ff00, 300, 1000)
                    .setSmallIcon(R.drawable.ic_baseline_warning_48)
                    .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setSound(soundUri,audioAttributes)
                    .setContentIntent(pendingIntent);
            NotificationChannel channel;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channel = new NotificationChannel(TEST_NOTIFY_ID
                        , "Danger Msg"
                        , NotificationManager.IMPORTANCE_HIGH);
                channel.enableLights(true);
                channel.enableVibration(true);
                channel.shouldShowLights();
                channel.setLightColor(Color.GREEN);
                channel.setSound(soundUri, audioAttributes);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                builder.setChannelId(TEST_NOTIFY_ID);

                manager.createNotificationChannel(channel);
            } else {
                builder.setDefaults(Notification.DEFAULT_ALL)
                        .setVisibility(Notification.VISIBILITY_PUBLIC);
            }
            int testNotifyId = 12;
            manager.notify(testNotifyId,
                    builder.build());
        } catch (Exception e) {

        }
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
                tvRes.setText(e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**取得回傳*/
               // GetVal = response.body().string();
                GetVal = response.body().string();
                tvRes.setText("GET回傳：\n" + GetVal);
                //split();
                System.out.print("Get:");
                System.out.print(GetVal);
            }
        });
    }
    public List<String> split(String jsonArray) throws Exception {
        List<String> splittedJsonElements = new ArrayList<String>();
        ObjectMapper jsonMapper = new ObjectMapper();
        JsonNode jsonNode = jsonMapper.readTree(jsonArray);

        if (jsonNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) jsonNode;
            for (int i = 0; i <arrayNode.size(); i++) {
                JsonNode individualElement = arrayNode.get(i);
                splittedJsonElements.add(individualElement.toString());
            }
        }
        return splittedJsonElements;
    }

    private final OkHttpClient client = new OkHttpClient();
        public void run() throws Exception {
            Request request = new Request.Builder()
                    .url("http://publicobject.com/helloworld.txt")
                    .build();
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code "+response);
            Headers responseHeaders = response.headers();
            for (int i = 0; i < responseHeaders.size(); i ++) {
                System.out.println(responseHeaders.name(i)+ ": " +responseHeaders.value(i));
            }
            System.out.println(response.body().string());
        }

    private void sendPOST() {
        if (SVal == null || MVal == null || TVal == null || PVal == null) {
            return;
        }
        if (!PostFlag.Flag) return;
        TextView tvRes = findViewById(R.id.text_Respond);
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**設置傳送所需夾帶的內容*/
        int topspeed = 0;

        FormBody formBody = new FormBody.Builder()
                /*.add("id", "3")
                .add("speed", "1")
                .add("miliage", "1000")
                .add("exercise", "300")
                .add("topspeed", "30")*/
                .add("id", id.toString())
                .add("speed", SVal)
                .add("miliage", MVal)
                .add("exercise", TVal)
                .add("topspeed", PVal)
                .build();
        /**設置傳送需求*/
        Request request = new Request.Builder()
                .url("https://jsonplaceholder.typicode.com/posts")
                //.url("http://35.221.236.109:3000/api880509")//資料庫測試
                .addHeader("Content-Type", "x-www-form-urlencoded")

                //.url("https://maker.ifttt.com/trigger/line/with/key/0nl929cYWV-nv9f76AW_O?value1=2")
                .post(formBody)
                .build();
        /**設置回傳*/
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**如果傳送過程有發生錯誤*/
                tvRes.setText(e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**取得回傳*/
                tvRes.setText("POST回傳：\n" + response.body().string());
            }
        });
        SVal = null;
        MVal = null;
        TVal = null;
        PVal = null;
        System.out.println("SVal");
        System.out.println(SVal);
    }
}
