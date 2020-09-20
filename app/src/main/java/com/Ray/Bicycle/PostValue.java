package com.Ray.Bicycle;

import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class PostValue extends AppCompatActivity {
    private RxTimerUtil rxTimer = new RxTimerUtil();


    @Override
    protected void onStart() {
        //TimeTest();
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        rxTimer.cancel();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        rxTimer.cancel();
        super.onStop();
    }

    /**
     * Timer
     **/
    public void AutoPostVal() {
        MainActivity mainActivity = new MainActivity();
        //SharedPreferences userSetting = getSharedPreferences("UserSetting" , MODE_PRIVATE);
        SharedPreferences userSetting = mainActivity.userSetting;
        int postTime =  mainActivity.postTime;
        //int postTime = userSetting.getInt("postTime",15000);
        rxTimer.interval(postTime, number -> {
            //Log.e("home_show_three", "======MainActivity======" + number);
            System.out.println("PostTime:"+postTime);
            SubPost();
            //System.out.println(number);
        });

    }

    ObservableOnSubscribe<Integer> AutoPostOnSubscribe = emitter -> {
        System.out.println("Post已經訂閱：subscribe，获取发射器");
        emitter.onNext(1);
        System.out.println("Post發射：onComplete");
    };
    Observable<Integer> PostObservable = Observable.create(AutoPostOnSubscribe);

    final Disposable[] disposable = new Disposable[1];

    Observer<Integer> PostObserver = new Observer<Integer>() {
        @Override
        public void onSubscribe(Disposable d) {
            disposable[0] = d;
            System.out.println("Post已经订阅：onSubscribe，获取解除器");
        }

        @Override
        public void onNext(Integer integer) {
            System.out.println("Post接收：onNext " + integer);
            // do here


        }

        @Override
        public void onError(Throwable e) {
            System.out.println("Post接收：onError " + e.getMessage());
            CancelPost();
        }

        @Override
        public void onComplete() {
            System.out.println("Post接收：onComplete");
        }
    };

    public void SubPost() {
        System.out.println("Post開始訂閱：subscribe");
        PostObservable.subscribe(PostObserver);
    }

    public void CancelPost() {
        System.out.println("Post取消訂閱：unsubscribe");
        if (disposable[0] != null)
            disposable[0].dispose();
    }
}
