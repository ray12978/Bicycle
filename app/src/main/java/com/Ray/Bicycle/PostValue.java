package com.Ray.Bicycle;

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
    private void TimeTest() {

        rxTimer.interval(200, number -> {
            //Log.e("home_show_three", "======MainActivity======" + number);
            sub();
            //System.out.println(number);
        });

    }

    ObservableOnSubscribe<Integer> observableOnSubscribe = new ObservableOnSubscribe<Integer>() {
        @Override
        public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
            //System.out.println("已經訂閱：subscribe，获取发射器");
            emitter.onNext(1);
            //System.out.println("信號發射：onComplete");
        }
    };
    Observable<Integer> observable = Observable.create(observableOnSubscribe);

    final Disposable[] disposable = new Disposable[1];

    Observer<Integer> observer = new Observer<Integer>() {
        @Override
        public void onSubscribe(Disposable d) {
            disposable[0] = d;
            //System.out.println("已经订阅：onSubscribe，获取解除器");
        }

        @Override
        public void onNext(Integer integer) {
            //System.out.println("信号接收：onNext " + integer);
            // do here


        }

        @Override
        public void onError(Throwable e) {
            System.out.println("信号接收：onError " + e.getMessage());
            cancel();
        }

        @Override
        public void onComplete() {
            //System.out.println("信号接收：onComplete");
        }
    };

    public void sub() {
        //System.out.println("開始訂閱：subscribe");
        observable.subscribe(observer);
    }

    public void cancel() {
        System.out.println("取消訂閱：unsubscribe");
        if (disposable[0] != null)
            disposable[0].dispose();
    }
}
