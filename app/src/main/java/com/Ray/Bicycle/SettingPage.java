package com.Ray.Bicycle;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import com.google.android.gms.maps.model.LatLng;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class SettingPage extends AppCompatActivity  {
    private boolean flag = true;
    private SharedPreferences Setting;
    private RxTimerUtil rxTimer = new RxTimerUtil();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        Setting = getSharedPreferences("UserSetting" , MODE_PRIVATE);
        //if (savedInstanceState == null) {
            // Create the fragment only when the activity is created for the first time.
            // ie. not after orientation changes
        setToolbar();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();

       // }
    }
    @Override
    protected void onStart() {
        TimeTest();
        super.onStart();
    }
    @Override
    protected void onDestroy(){
        rxTimer.cancel();
        super.onDestroy();
    }
    @Override
    protected void onStop(){
        rxTimer.cancel();
        super.onStop();
    }


    public Object getSetting(String Select){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean Cloud = prefs.getBoolean("cloud",false);
        String NbSet =  prefs.getString("nbIot",null);
        String userID = prefs.getString("id",null);
        switch (Select){
            case "cloud":
                return Cloud;
            case "nb":
                return Cloud ? NbSet:"null";
            case "id":
                return userID;
            default:
                return null;
        }
    }

    public void ShareSetting(){ //need keep run
        Setting.edit()
                .putBoolean("cloud",(Boolean) getSetting("cloud"))
                .putString("nb",(String) getSetting("nb"))
                .putString("id",(String) getSetting("id"))
                .apply();
        System.out.println("Shared!");
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:   //返回键的id
                this.finish();
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    /**Timer**/
    private void TimeTest(){

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
            //SetMark(integer);
            ShareSetting();

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
    public void cancel(){
        System.out.println("取消訂閱：unsubscribe");
        if(disposable[0] != null)
            disposable[0].dispose();
    }
    private void setToolbar(){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("設定");
        }
    }
    public static class SettingsFragment extends PreferenceFragmentCompat {
        EditTextPreference preference = findPreference("id");
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            this.preference = ((EditTextPreference) getPreferenceScreen() //put this in the onCreate
                    .findPreference("id"));
            preference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType( InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD); // set only numbers allowed to input
                    editText.selectAll(); // select all text
                    int maxLength = 14;
                    editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)}); // set maxLength to 2
                }
            });
        }


    }
}