package com.Ray.Bicycle;


import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.app.AlertDialog;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimePickerDialog {
    private Activity activity;
    /**使InterFace可以被類別使用*/
    OnDialogRespond onDialogRespond;

    public TimePickerDialog(Activity activity) {
        this.activity = activity;
    }

    public void showDialog() {
        /**關於AlertDialog相關的設置請參考這篇文章：
         * https://thumbb13555.pixnet.net/blog/post/310777160*/
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.number_picker_dialog, null);
        mBuilder.setView(view);
        mBuilder.setPositiveButton("確定", null);
        mBuilder.setNegativeButton("取消", null);
        AlertDialog dialog = mBuilder.create();
        /**這裡是設置NumberPicker相關*/
        NumberPicker SpdPick;
        SpdPick = view.findViewById(R.id.numberPicker_M);
        Date date = new Date();

        /**設置NumberPicker的最大、最小以及NumberPicker現在要顯示的內容*/

        final String[] SpdList = activity.getResources().getStringArray(R.array.Speed_List);
        SpdPick.setMinValue(0);
        SpdPick.setMaxValue(SpdList.length - 1);
        SpdPick.setDisplayedValues(SpdList);
        SpdPick.setValue(0); // 設定預設位置
        SpdPick.setWrapSelectorWheel(false); // 是否循環顯示
        SpdPick.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // 不可編輯
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((v -> {
            /**格式化字串*/
            //String s = String.format("%02d", SpdPick.getValue());
            String[] sss = SpdPick.getDisplayedValues();
            int i = SpdPick.getValue();
            String s = sss[i];
            /**這邊將值放進OnDialogRespond中*/
            onDialogRespond.onRespond(s);
            try {
                onDialogRespond.onResult(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            dialog.dismiss();
        }));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener((v -> {
            try {
                onDialogRespond.onResult(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            dialog.dismiss();
        }));
    }
    /**設置Interface，使取到的直可以被回傳*/
    interface OnDialogRespond{
        void onRespond(String selected);
        void onResult(boolean ans) throws Exception;
    }
}