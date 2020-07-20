package com.Ray.Bicycle;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import static android.content.Context.NOTIFICATION_SERVICE;

public class MessageFragment extends Fragment {
    private View view;
    //private final UUID uuid = UUID.fromString("8c4102d5-f0f9-4958-806e-7ba5fd54ce7c");
    private final UUID serialPortUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private EditText id;
    private EditText BTM;
    private String SpeedLimit = "";
    private String address;
    public String SVal;
    public String MVal;
    public String TVal;
    public String PVal;
    public StringBuffer BTSendMsg = new StringBuffer("N00NNN"); //[0]Lock,[1]SpeedTen,[2]SpeedUnit,[3]SpeedConfirm,[4]Laser,[5]Buzzer
    public StringBuffer BTValTmp = new StringBuffer(16);
    public byte[] buffer = new byte[256];
    public TextView text_Respond;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private TextView textContent;
    private Switch SWPost;
    boolean PostFlag;
    /*********************Notify*********************/
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TEST_NOTIFY_ID = "Bicycle_Danger";
    private static final int NOTYFI_REQUEST_ID = 300;
    private int testNotifyId = 11;
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
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_message, container, false);
        return view;
    }
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        super.onCreate(savedInstanceState);
        getActivity().setContentView(R.layout.activity_main);
        /*****************藍牙*************/
        final String deviceName = getActivity().getIntent().getStringExtra("DeviceName");
        final String deviceAddress = getActivity().getIntent().getStringExtra("DeviceAddress");
        text_Respond = view.findViewById(R.id.text_Respond);
        String name = deviceName != null ? deviceName : "裝置名稱未顯示";
        address = deviceAddress;
        getActivity().setTitle(String.format("%s (%s)", address, name));
        Button btBTSend = view.findViewById(R.id.btBTSend);
        Button btBTOpen = view.findViewById(R.id.BTOpen);
        Button btBTConct = view.findViewById(R.id.btBTConct);
        Button btBTDiscont = view.findViewById(R.id.btBTDiscont);
        Button btClear = view.findViewById(R.id.btClr);
        Button btDisplay = view.findViewById(R.id.btDisplay);
        Button SwPage = view.findViewById(R.id.SwPage);
        Switch SWPost = getActivity().findViewById(R.id.SWPost);
        id = getActivity().findViewById(R.id.id);
        BTM = getActivity().findViewById(R.id.id2);
        //SpeedLimit = findViewById(R.id.edit_SpeedLimit);
        textContent = getActivity().findViewById(R.id.textContent);
        NumberPicker SpdPick = getActivity().findViewById(R.id.SpeedPicker);
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
        /**IO按鈕*/
        Button btLaser = view.findViewById(R.id.las_btn); //雷射按鈕
        Button btBuzz = view.findViewById(R.id.buzz_btn); //蜂鳴器按鈕
        Button btLck = view.findViewById(R.id.lck_btn); //上鎖按鈕
        /**HTTP按鈕*/
        Button btPost = view.findViewById(R.id.button_POST);
        Button btSpLit = view.findViewById(R.id.SpLit_btn);

        btBTDiscont.setOnClickListener(view -> disconnect());

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
            Intent BTListAct = new Intent(getActivity(), ConnectActivity.class);
            startActivity(BTListAct);
        });
        btBTSend.setOnClickListener(v -> {
            BTSend(BTM.getText().toString());

        });
        btBTConct.setOnClickListener(v -> {
            BTConnect();
        });
        SwPage.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });
        btLaser.setOnClickListener(v -> {
            BTMsg(4, 5, "T", "J", LasFlag);
            int On = R.drawable.bike_open_white_48dp;
            int Off = R.drawable.ic_bike_icon_off_black;
            Button_exterior(btLaser, Off, On, 4, 'J');
            BTSend(BTSendMsg.toString());
        });
        btBuzz.setOnClickListener(v -> {
            BTMsg(5, 6, "E", "N", BuzFlag);
            int On = R.drawable.ic_baseline_volume_off_24;
            int Off = R.drawable.ic_baseline_volume_up_24;
            Button_exterior(btBuzz, Off, On, 5, 'N');
            BTSend(BTSendMsg.toString());
        });
        btLck.setOnClickListener(v -> {
            BTMsg(0, 1, "L", "F", LckFlag);
            int On = R.drawable.ic_baseline_lock_24;
            int Off = R.drawable.ic_baseline_lock_open_24;
            Button_exterior(btLck, Off, On, 0, 'F');
            BTSend(BTSendMsg.toString());
            if (!LckFlag.Flag) {
                btBuzz.setEnabled(false);
                btSpLit.setEnabled(false);
                btLaser.setEnabled(false);
                //setVibrate(1000);
                Notify();
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
                btBuzz.setEnabled(true);
                btSpLit.setEnabled(true);
                btLaser.setEnabled(true);
            }
        });
        btSpLit.setOnClickListener(v -> {
            Speed_Limit();
            int On = R.drawable.ic_speed_white;
            int Off = R.drawable.ic_speed;
            Button_exterior(btSpLit, Off, On, 3, 'N');
            BTSend(BTSendMsg.toString());
        });
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btClear.setOnClickListener(v -> {
            textContent.setText("");
            BTValTmp.delete(0, BTValTmp.length());
        });
        btDisplay.setOnClickListener(v -> {
            Toast toast = Toast.makeText(getActivity(), BTValTmp, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 200);
            toast.show();
            str_process();
            System.out.println("BTTmp:");
            System.out.println(BTValTmp.toString());
            System.out.println("PostFlag:");
            System.out.println(PostFlag);
        });
        SWPost.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isChecked()) {
                System.out.println('a');
                PostFlag = true;
                if (BuzFlag.Flag) System.out.println('G');
            } else {
                System.out.println('b');
                PostFlag = false;
            }
        });
        /**HTTP按鈕動作**/
        /**傳送POST**/
        btPost.setOnClickListener(v -> {
            if (id.length() != 0) sendPOST();
            else {
                Toast toast = Toast.makeText(getActivity(), "請先輸入id", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM, 0, 200);
                toast.show();
            }
        });
    }
    /***********************藍牙副程式*******************************/
    private void BTConnect() {
        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

        try {
            socket = device.createRfcommSocketToServiceRecord(serialPortUUID);
            socket.connect();
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        if (socket == null) return;

        try {
            socket.close();
            socket = null;
            inputStream = null;
            outputStream = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void BTSend(String BTMsg) {
        if (outputStream == null) return;

        try {
            outputStream.write(BTMsg.getBytes());
            outputStream.flush();
            System.out.println("BT:");
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
            if (inputStream.available() <= 0) return;
            String a = new String(buffer, 0, inputStream.read(buffer));
            Log.d(a, "read: ");
            StrBufTmp.append(a);
            Thread.sleep(100);
        } catch (IOException | InterruptedException e) {
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
                    System.out.println("a");
                    System.out.println(StrPosition[b]);
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
        } else if (BTValTmp.toString().charAt(0) == 'B') {
            String Status = BTValTmp.toString().substring(1, 2).trim();
            BTValTmp.delete(0, BTValTmp.length());
            DanFlag.Flag = Status.equals("1");
            System.out.println(DanFlag.Flag);
            if (DanFlag.Flag) Danger_Msg();
        }
    }

    private void BTMsg(int start, int end, String Msg1, String Msg2, FlagAddress SelectFlag) {
        MsgBtFlag.Flag = true;
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
        System.out.println(BTSendMsg);
    }

    void Speed_Limit() {
        //String SpeedLimit = "" ;
        if (SpeedLimit.length() != 0 && SpdFlag.Flag && SpeedLimit.length() < 3) {
            String SpLtVal = SpeedLimit.toString();
            if (SpeedLimit.length() == 1) {
                BTSendMsg.replace(1, 2, "0");
                BTSendMsg.replace(2, 3, SpLtVal);
            } else {
                BTSendMsg.replace(1, 3, SpLtVal);
            }
            BTMsg(3, 4, "Y", "N", SpdFlag);
            System.out.println(BTSendMsg);
        } else {
            if (!SpdFlag.Flag) {
                BTSendMsg.replace(3, 4, "N");
                SpdFlag.Flag = true;
                System.out.println(BTSendMsg);
            } else {
                Toast toast = Toast.makeText(getActivity(), "請先設定時速", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM, 0, 200);
                toast.show();
            }
        }

    }

    /*******************************其他**********************************/
    void Button_exterior(Button btn, int one, int two, int bit, char condi) {
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
        Vibrator myVibrator = (Vibrator) Objects.requireNonNull(getActivity()).getSystemService(Service.VIBRATOR_SERVICE);
        myVibrator.vibrate(time);
    }

    void Danger() {
        if (!DanFlag.Flag) return;
        setVibrate(1000);
        System.out.println("danger");
        //DanFlag.Flag = false;
        //Notify();
        showNotification();
        DanFlag.Flag = false;
    }

    void Danger_Msg() {
        new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_baseline_warning_48)
                .setTitle("警告：您的腳踏車發生異狀,請立即確認狀況")
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DanFlag.Flag = false;
                    }
                })
                .show();
    }

    public void Notify() {

        NotificationManager mNotificationManager
                = (NotificationManager) Objects.requireNonNull(getActivity()).getSystemService(NOTIFICATION_SERVICE);
        Intent notifyIntent = new Intent(getActivity(), MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent appIntent
                = PendingIntent.getActivity(getActivity(), 0, notifyIntent, 0);
        Notification notification
                = new Notification.Builder(getActivity())
                .setContentIntent(appIntent)
                .setSmallIcon(R.drawable.ic_baseline_warning_48) // 設置狀態列裡面的圖示（小圖示）　　
                //.setLargeIcon(BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.ic_launcher)) // 下拉下拉清單裡面的圖示（大圖示）
                .setTicker("notification on status bar.") // 設置狀態列的顯示的資訊
                .setWhen(System.currentTimeMillis())// 設置時間發生時間
                .setAutoCancel(false) // 設置通知被使用者點擊後是否清除  //notification.flags = Notification.FLAG_AUTO_CANCEL;
                .setContentTitle("Notification Title") // 設置下拉清單裡的標題
                .setContentText("Notification Content")// 設置上下文內容
                .setOngoing(true)      //true使notification变为ongoing，用户不能手动清除  // notification.flags = Notification.FLAG_ONGOING_EVENT; notification.flags = Notification.FLAG_NO_CLEAR;

                //.setDefaults(Notification.DEFAULT_ALL) //使用所有默認值，比如聲音，震動，閃屏等等
// .setDefaults(Notification.DEFAULT_VIBRATE) //使用默認手機震動提示
// .setDefaults(Notification.DEFAULT_SOUND) //使用默認聲音提示
// .setDefaults(Notification.DEFAULT_LIGHTS) //使用默認閃光提示
// .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND) //使用默認閃光提示 與 默認聲音提示

// .setVibrate(vibrate) //自訂震動長度
// .setSound(uri) //自訂鈴聲
// .setLights(0xff00ff00, 300, 1000) //自訂燈光閃爍 (ledARGB, ledOnMS, ledOffMS)
                .build();

//把指定ID的通知持久的發送到狀態條上
        mNotificationManager.notify(0, notification);
    }

    public void showNotification() {
        Log.d(TAG, "showNotification: ");
        try {
            Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(getActivity().getApplicationContext(),
                    NOTYFI_REQUEST_ID,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationManager manager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
            Notification.Builder builder = new Notification.Builder(getActivity())
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle("您的腳踏車發生異常狀況")
                    .setContentText("請立即前往確認")

                    .setSmallIcon(R.drawable.ic_baseline_warning_48)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentIntent(pendingIntent);
            NotificationChannel channel;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channel = new NotificationChannel(TEST_NOTIFY_ID
                        , "Danger Msg"
                        , NotificationManager.IMPORTANCE_HIGH);
                channel.enableLights(true);
                channel.enableVibration(true);
                channel.shouldShowLights();
                builder.setChannelId(TEST_NOTIFY_ID);
                manager.createNotificationChannel(channel);
            } else {
                builder.setDefaults(Notification.DEFAULT_ALL)
                        .setVisibility(Notification.VISIBILITY_PUBLIC);
            }
            manager.notify(testNotifyId,
                    builder.build());
        } catch (Exception e) {

        }
    }

    /*****************************HTTP副程式******************************/
    private void sendGET() {
        TextView tvRes = view.findViewById(R.id.text_Respond);
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**設置傳送需求*/
        Request request = new Request.Builder()
                // .url("https://jsonplaceholder.typicode.com/posts/1")
                //資料庫測試        .url("http://35.221.236.109:3000/api880509")
                .url("https://maker.ifttt.com/trigger/line/with/key/0nl929cYWV-nv9f76AW_O?value1=1")
//                .header("Cookie","")//有Cookie需求的話則可用此發送
//                .addHeader("","")//如果API有需要header的則可使用此發送
                .build();
        /**設置回傳*/
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**如果傳送過程有發生錯誤*/
                //tvRes.setText(e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**取得回傳*/
                tvRes.setText("GET回傳：\n" + response.body().string());
            }
        });
    }

    private void sendPOST() {
        if (SVal == null || MVal == null || TVal == null || PVal == null) {
            return;
        }
        if (!PostFlag) return;
        TextView tvRes = view.findViewById(R.id.text_Respond);
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

