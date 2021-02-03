package com.Ray.Bicycle.Component;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import com.Ray.Bicycle.R;

public class LoadingDialog {
    Activity activity;
    AlertDialog dialog;

    public LoadingDialog(Activity myActivity){
        activity = myActivity;
    }
    void startLoadingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.load_dialog,null));
        builder.setCancelable(true);

        dialog = builder.create();
        dialog.show();
    }
    void dismissDialog(){
        dialog.dismiss();
    }
}
