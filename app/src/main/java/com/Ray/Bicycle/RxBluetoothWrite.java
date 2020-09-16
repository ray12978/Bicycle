package com.Ray.Bicycle;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static android.content.Context.MODE_PRIVATE;

public class RxBluetoothWrite extends AppCompatActivity {
    //private RxTimerUtil rxTimer = new RxTimerUtil();
    /**
     * Application
     **/
    private MyApp MyAppInst = MyApp.getAppInstance();
    String BTSendMsg = getSharedPreferences("BTMsg", MODE_PRIVATE)
            .getString("SendMsg", null);
    private MainActivity mainActivity = new MainActivity();
    public void TimeTest() {
        /*rxTimer.interval(3000, new RxTimerUtil.IRxNext() {
            @Override
            public void doNext(Object number) {
                Log.e("home_show_three", "======MainActivity======" + number);
                sub();
                System.out.println(number);
            }
        });*/
    }
    ObservableOnSubscribe<String> observableOnSubscribe = new ObservableOnSubscribe<String>() {
        @Override
        public void subscribe(ObservableEmitter<String> emitter) {
            System.out.println("已經訂閱：subscribe，获取发射器");
           // if (RxLocation != null)
            //    emitter.onNext(RxLocation);
            //
            if(MyAppInst.BTRevFlag.Flag){
               if(BTSendMsg == null) return;
               if(BTSendMsg.equals("null"))System.out.println("Msg null");
               emitter.onNext(BTSendMsg);
            }

            System.out.println("信號發射：onComplete");
        }
    };
    /**
     * 创建被观察者，并带上被观察者的订阅
     */
    Observable<String> observable = Observable.create(observableOnSubscribe);

    final Disposable[] disposable = new Disposable[1];

    Observer<String> observer = new Observer<String>() {
        @Override
        public void onSubscribe(Disposable d) {
            disposable[0] = d;
            System.out.println("已经订阅：onSubscribe，获取解除器");
        }

        @Override
        public void onNext(String string) {
            System.out.println("信号接收：onNext " + string);
          //  SetMark(integer);
            try {
                MyAppInst.writeBT(string);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onError(Throwable e) {
            System.out.println("信号接收：onError " + e.getMessage());
            cancel();
        }

        @Override
        public void onComplete() {
            System.out.println("信号接收：onComplete");
        }
    };

    public void sub() {
        System.out.println("開始訂閱：subscribe");
        observable.subscribe(observer);
    }

    public void cancel() {
        System.out.println("取消訂閱：unsubscribe");
        if (disposable[0] != null)
            disposable[0].dispose();
    }
}
