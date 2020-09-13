package com.example.v2;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.Toast;

public class LoadingDialog {

    Activity activity;
    AlertDialog dialog;


    LoadingDialog(Activity myActivity){
        activity = myActivity;
    }

    void startLoadingDialog(){
        AlertDialog.Builder loadingBuilder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        loadingBuilder.setView(inflater.inflate(R.layout.loading_dialog, null));

        loadingBuilder.setCancelable(false);

        dialog = loadingBuilder.create();
        dialog.show();
    }

    void dismissDialog(){
        dialog.dismiss();
    }
}
