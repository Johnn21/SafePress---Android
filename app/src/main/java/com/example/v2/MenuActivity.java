package com.example.v2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class MenuActivity extends AppCompatActivity {

    DatabaseHelper db;
    SharedPreferences prefs = null;

    private ConnectionClass connect;

    private RelativeLayout relativeId;


    TextToSpeech textToSpeech;

    public static boolean isSwitched = false;

    AudioManager mAudioManager;

    SpeechClass speechClass;

    Thread t;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_layout);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(bluetoothState, filter);

        final LoadingDialog loadingDialog = new LoadingDialog(MenuActivity.this);


        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);


        final Switch switchSystem = (Switch)findViewById(R.id.switchSystem);

        relativeId = findViewById(R.id.relativeId);

        textToSpeech =  new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int lang = textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });

        db = new DatabaseHelper(this);



        speechClass = new SpeechClass(MenuActivity.this, db, relativeId, textToSpeech, mAudioManager);
        speechClass.listenUser();

        checkVoiceCommand();


        if(!BluetoothActivity.hasDevice){
            db.updateCurrentDevice("1", " ", " ");
        }


        prefs = getSharedPreferences("com.mycompany.myAppName", MODE_PRIVATE);


        if(prefs.getBoolean("firstrun", true)){

            boolean insertCode = db.addDataCode(0 , 0, "0", "0-0-0");
            boolean insertCurrentDevice = db.addCurrentDevice(" "," ");
            boolean insertSystemState = db.addSystemSTate("off");

            if(insertCode == true && insertCurrentDevice == true && insertSystemState){
                Toast.makeText(MenuActivity.this, "Data Successfully!", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(MenuActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
            }

            prefs.edit().putBoolean("firstrun", false).commit();
        }



        t = new Thread(){
            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {

                        Thread.sleep(1000);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (db.getSystemState().contains("on")) {
                                    switchSystem.setChecked(true);
                                } else {
                                    switchSystem.setChecked(false);
                                }
                            }
                        });
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };

        t.start();



        switchSystem.setOnTouchListener(new View.OnTouchListener() {
           @Override
           public boolean onTouch(View v, MotionEvent event) {
               return event.getActionMasked() == MotionEvent.ACTION_MOVE;
           }
       });

        switchSystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Switch btn = (Switch) v;
                final boolean switchChecked = btn.isChecked();


                if (btn.isChecked()) {
                    btn.setChecked(false);
                } else {
                    btn.setChecked(true);
                }
                if(BluetoothActivity.hasDevice){

                    String message = "Are you sure you want to turn off the system?";
                    if (!btn.isChecked()) {
                        message = "Are you sure you want to turn on the system?";
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                    builder.setMessage(message)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {

                                    if (switchChecked) {

                                        if(BluetoothActivity.hasDevice){

                                            BluetoothActivity.keepEvidence.interrupt();

                                            try {
                                                BluetoothActivity.keepEvidence.join();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }


                                            connect = new ConnectionClass(BluetoothActivity.mBTSocket);
                                            connect.write("2");

                                            loadingDialog.startLoadingDialog();

                                            Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    loadingDialog.dismissDialog();
                                              //      dialog.show(getSupportFragmentManager(), "dialog");
                                                    Toast.makeText(MenuActivity.this, "System On!", Toast.LENGTH_LONG).show();

                                                    ConnectionClass connect =  new ConnectionClass(BluetoothActivity.mBTSocket);
                                                    BluetoothActivity.keepEvidence = new BluetoothActivity.keepBluetoothEvidence(connect, db);
                                                    BluetoothActivity.keepEvidence.start();

                                                }
                                            }, 800);


                                            btn.setChecked(true);


                                          db.updateSystemState("1", "on");

                                        }
                                    } else {


                                        if(BluetoothActivity.hasDevice){

                                            BluetoothActivity.keepEvidence.interrupt();

                                            try {
                                                BluetoothActivity.keepEvidence.join();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }


                                            connect = new ConnectionClass(BluetoothActivity.mBTSocket);
                                            connect.write("3");


                                            loadingDialog.startLoadingDialog();

                                            Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    loadingDialog.dismissDialog();
                                                    Toast.makeText(MenuActivity.this, "System Off!", Toast.LENGTH_LONG).show();

                                                    ConnectionClass connect =  new ConnectionClass(BluetoothActivity.mBTSocket);
                                                    BluetoothActivity.keepEvidence = new BluetoothActivity.keepBluetoothEvidence(connect, db);
                                                    BluetoothActivity.keepEvidence.start();

                                                }
                                            }, 800);


                                            btn.setChecked(false);

                                            db.updateSystemState("1", "off");

                                        }

                                    }


                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }else{
                    Toast.makeText(MenuActivity.this, "There Is No Connection!", Toast.LENGTH_LONG).show();
                }



            }
        });

        ImageButton bluetoothMenuButton = (ImageButton) findViewById(R.id.bluetoothMenuButton);
        bluetoothMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MenuActivity.this, BluetoothActivity.class);
                startActivity(intent);
            }
        });

        ImageButton inputButton = (ImageButton) findViewById(R.id.inputButton);
        inputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MenuActivity.this, InputActivity.class);
                startActivity(intent);
            }
        });

        ImageButton historyButton = (ImageButton) findViewById(R.id.historyButton);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MenuActivity.this, History.class);
                startActivity(intent);

            }
        });

        ImageButton exitAppButton = (ImageButton) findViewById(R.id.exitAppButton);
        exitAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                builder.setCancelable(true);
                builder.setTitle("Are you sure you want to exit?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                System.exit(0);
                            }
                        });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });


                builder.create();
                builder.show();

            }
        });

        Button showCurrentCode = (Button) findViewById(R.id.showCurrentCode);
        showCurrentCode.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {


                List<String> passwordsList = db.getPreviousPasswords();

                if (passwordsList != null && passwordsList.isEmpty()){
                    Toast.makeText(MenuActivity.this, "Empty List!", Toast.LENGTH_LONG).show();
                }else{
                    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MenuActivity.this, android.R.layout.simple_list_item_1, passwordsList);

                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MenuActivity.this);
                    final View customLayout = getLayoutInflater().inflate(R.layout.history_dialog, null);
                    alertDialog.setView(customLayout);
                    ListView listOldPasswords = customLayout.findViewById(R.id.listOldPasswords);


                    listOldPasswords.setAdapter(adapter);

                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    alertDialog.setNeutralButton("Delete List", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            AlertDialog.Builder confirmDialog = new AlertDialog.Builder(MenuActivity.this);
                            confirmDialog.setMessage("Are you sure you want to delete the list?");
                            confirmDialog.setCancelable(true);

                            confirmDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    db.deleteOldPasswords();
                                    Toast.makeText(MenuActivity.this, "Passwords Deleted!", Toast.LENGTH_LONG).show();
                                }
                            });

                            confirmDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                            confirmDialog.show();

                        }
                    });

                    AlertDialog alert = alertDialog.create();
                    alert.setCanceledOnTouchOutside(false);
                    alert.show();

                }

            }
        });

        if (ContextCompat.checkSelfPermission(MenuActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MenuActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }


    }

    private void checkVoiceCommand(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(!(ContextCompat.checkSelfPermission(MenuActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)){
                Intent intent =  new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"+ getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }


    @Override
    protected void onResume(){
        super.onResume();


        Button showCurrentCode = (Button) findViewById(R.id.showCurrentCode);
        showCurrentCode.setText("Current Password: "+ db.getCurrentCode());



        final Switch switchSystem = (Switch)findViewById(R.id.switchSystem);


        if(db.getSystemState().contains("on")){
            switchSystem.setChecked(true);
        }else{
            switchSystem.setChecked(false);
        }


    }

    final BroadcastReceiver bluetoothState = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();


            if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                  //  mBluetoothStatus.setText("Bluetooth Enabled");

                    if(BluetoothActivity.mBTSocket != null) {
                        try {
                            BluetoothActivity.mBTSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        BluetoothActivity.hasDevice = false;

                        BluetoothActivity.currentDevice = null;

                        db.updateCurrentDevice("1", " ", " ");

                        Toast.makeText(getApplicationContext(),"Disconnected", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }
    };


}
