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

        super.onStart();
    }
    @Override
    protected void onDestroy(){

        super.onDestroy();
    }
    @Override
    protected void onStop(){

        super.onStop();
    }


    public Object getSetting(String Select){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean Cloud = prefs.getBoolean("cloud",false);
        String NbSet =  prefs.getString("nbIot","null");
        boolean NbBool = NbSet.equals("nbiot");
        boolean PhBool = NbSet.equals("phone");
        boolean NotiBool = prefs.getBoolean("noti",false);
        String userID = prefs.getString("id",null);
        boolean PTimeSw = prefs.getBoolean("develop",false);
        String postVal = prefs.getString("postTime","15000");
        int postTime = postVal == null ? 15000:Integer.parseInt(postVal);
        System.out.print("posttime:");
        System.out.println(postTime);
        switch (Select){
            case "cloud":
                return Cloud;
            case "nb":
                return Cloud && NbBool;
            case "id":
                return userID;
            case "time":
                return PTimeSw ? postTime*1000 : 15000;
            case "postTimeSw":
                return PTimeSw;
            case "ph":
                return Cloud && PhBool;
            case "noti":
                return NotiBool;
            default:
                return null;
        }
    }
    public void onBackPressed() {
        ShareSetting();
        finish();
    }
    public void ShareSetting(){ //need keep run
        Setting.edit()
                .putBoolean("cloud",(Boolean) getSetting("cloud"))
                .putBoolean("nb",(Boolean) getSetting("nb"))
                .putBoolean("postTimeSw",(Boolean) getSetting("postTimeSw"))
                .putBoolean("ph",(Boolean) getSetting("ph"))
                .putString("id",(String) getSetting("id"))
                .putInt("postTime",(Integer) getSetting("time"))
                .putBoolean("noti",(Boolean) getSetting("noti"))
                .apply();
        System.out.println("Shared!");
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:   //返回键的id
                ShareSetting();
                this.finish();
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        EditTextPreference postPreference = findPreference("postTime");
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            this.preference = ((EditTextPreference) getPreferenceScreen() //put this in the onCreate
                    .findPreference("id"));
            this.postPreference = getPreferenceScreen().findPreference("postTime");
            preference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD); // set only numbers allowed to input
                    editText.selectAll(); // select all text
                    int maxLength = 14;
                    editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)}); // set maxLength to 2
                }
            });
            postPreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener(){
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED); // set only numbers allowed to input
                editText.selectAll(); // select all text
                int maxLength = 4;
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)}); // set maxLength to 2
            }
        });
        }


    }
}