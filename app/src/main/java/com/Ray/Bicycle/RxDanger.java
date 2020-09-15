package com.Ray.Bicycle;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RxDanger {
    private Flowable<String> dangerStream;
    public Flowable<String> RxDangerStream(String Msg){
        if(dangerStream == null) {
            dangerStream = Flowable.create(new FlowableOnSubscribe<String>() {
                @Override
                public void subscribe(final FlowableEmitter<String> emitter) throws Exception {
                    while (!emitter.isCancelled()) {
                        try {
                            emitter.onNext(Msg);
                            //emitter.onNext(2);
                            //emitter.onNext(3);
                            //emitter.onComplete();
                        } catch (Exception e) {
                            emitter.onError(e);
                        }
                    }
                }
            }, BackpressureStrategy.LATEST).share();
        }
        return dangerStream;
    }


}
