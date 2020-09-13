package com.example.v2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class InputActivity extends AppCompatActivity {


    private EditText passwordText;
    private EditText passwordConfirmationText;



    private Pattern PASSWORD_LENGTH =

            Pattern.compile("^" +
                    ".{4,5}" +
                    "$"
            );

    private Pattern PASSWORD_DIGITS =

            Pattern.compile(
                            "^[0-1]*$"
            );


    private int cntOne = 0;
    private int cntZero = 0;

    private DatabaseHelper db;

    private ConnectionClass connect;

    static TextToSpeech textToSpeech;

    static RelativeLayout relativeId;

    AudioManager mAudioManager;

    SpeechClass speechClass;

    @SuppressLint({"ClickableViewAccessibility", "HandlerLeak"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_layout);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(bluetoothState, filter);

        final LoadingDialog loadingDialog = new LoadingDialog(InputActivity.this);


        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        db = new DatabaseHelper(this);


        passwordText = (EditText)findViewById(R.id.passwordText);
        passwordConfirmationText = (EditText)findViewById(R.id.passwordConfirmationText);


        textToSpeech =  new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int lang = textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });


        relativeId = findViewById(R.id.relativeIdInput);

        speechClass = new SpeechClass(InputActivity.this, db, relativeId, textToSpeech, mAudioManager);

        speechClass.listenUser();


        ImageButton infoButton = (ImageButton) findViewById(R.id.infobutton);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(InputActivity.this);
                final View customLayout = getLayoutInflater().inflate(R.layout.info_dialog, null);
                alertDialog.setView(customLayout);

                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog alert = alertDialog.create();
                alert.setCanceledOnTouchOutside(false);
                alert.show();

            }
        });

        Button confirmCode = (Button)findViewById(R.id.confirmCode);
        confirmCode.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onClick(View v) {


                int ok = 1;

                String passwordInput = passwordText.getText().toString();
                String passwordConfirmationInput = passwordConfirmationText.getText().toString();

                passwordText.setError(null);
                passwordConfirmationText.setError(null);


                if(passwordInput.isEmpty()){
                    passwordText.setError("Empty Field");
                    ok = 0;
                }else if(!PASSWORD_LENGTH.matcher(passwordInput).matches()){
                    passwordText.setError("Invalid Length");
                    ok = 0;
                }else if(!PASSWORD_DIGITS.matcher(passwordInput).matches()) {
                    passwordText.setError("Invalid Characters");
                    ok = 0;
                }else{
                    cntOne = 0;
                    cntZero = 0;
                    for(int i  = 0 ; i < passwordInput.length(); i++){
                        if(passwordInput.charAt(i) == '1'){
                            cntOne++;
                        }else if(passwordInput.charAt(i) == '0'){
                            cntZero++;
                        }
                    }

                    if(cntOne == 0 || cntZero == 0){

                        if(cntOne == 0){
                            passwordText.setError("Password Must Contain At Least One Long Press");
                            passwordConfirmationText.setError("Password Must Contain At Least One Long Press");
                            ok = 0;
                        }else if(cntZero == 0){
                            passwordText.setError("Password Must Contain At Least One Short Press");
                            passwordConfirmationText.setError("Password Must Contain At Least One Short Press");
                            ok = 0;
                        }

                    }

                }

                if(passwordConfirmationInput.isEmpty()){
                    passwordConfirmationText.setError("Empty Field");
                    ok = 0;
                }else if(!PASSWORD_LENGTH.matcher(passwordInput).matches()) {
                    passwordConfirmationText.setError("Invalid Length");
                    ok = 0;
                }else if(!PASSWORD_DIGITS.matcher(passwordInput).matches()) {
                    passwordConfirmationText.setError("Invalid Characters");
                    ok = 0;
                }else{
                    cntOne = 0;
                    cntZero = 0;
                    for(int i  = 0 ; i < passwordConfirmationInput.length(); i++){
                        if(passwordConfirmationInput.charAt(i) == '1'){
                            cntOne++;
                        }else if(passwordConfirmationInput.charAt(i) == '0'){
                            cntZero++;
                        }
                    }

                    if(cntOne == 0 || cntZero == 0){

                        if(cntOne == 0){
                            passwordConfirmationText.setError("Password Must Contain At Least One Long Press");
                            ok = 0;
                        }else if(cntZero == 0){
                            passwordConfirmationText.setError("Password Must Contain At Least One Short Press");
                            ok = 0;
                        }

                    }else{
                        if(!passwordConfirmationInput.equals(passwordInput)){
                            passwordConfirmationText.setError("Passwords Do Not Match");
                            ok = 0;
                        }

                    }
                }

                if(ok == 1){
                    if(BluetoothActivity.mBTAdapter != null){
                        if(BluetoothActivity.mBTAdapter.isEnabled() && BluetoothActivity.hasDevice) {

                            BluetoothActivity.keepEvidence.interrupt();

                            try {
                                BluetoothActivity.keepEvidence.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }



                            connect = new ConnectionClass(BluetoothActivity.mBTSocket);
                            connect.write(passwordInput);

                            loadingDialog.startLoadingDialog();

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    loadingDialog.dismissDialog();

                                    AlertDialog.Builder builder = new AlertDialog.Builder(InputActivity.this);
                                    builder.setTitle("Code Set Successfully!")
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            });

                                    builder.create();
                                    builder.show();

                                    ConnectionClass connect =  new ConnectionClass(BluetoothActivity.mBTSocket);
                                    BluetoothActivity.keepEvidence = new BluetoothActivity.keepBluetoothEvidence(connect, db);
                                    BluetoothActivity.keepEvidence.start();

                                }
                            }, 800);


                            String both = db.getCurrentCode() + " - " + db.getCurrentTimeCode();

                            if(!db.getCurrentCode().equals("0")){
                                db.addPreviousPassword(both);
                            }


                            Date currentTime = Calendar.getInstance().getTime();

                            db.updateCurrentCode("1", passwordInput, String.valueOf(currentTime));


                            passwordText.setText("");
                            passwordConfirmationText.setText("");
                        }else{

                            AlertDialog.Builder builder = new AlertDialog.Builder(InputActivity.this);
                            builder.setTitle("There Is No Connection!")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });


                            builder.create();
                            builder.show();

                            passwordText.setText("");
                            passwordConfirmationText.setText("");
                        }
                    }else{

                        AlertDialog.Builder builder = new AlertDialog.Builder(InputActivity.this);
                        builder.setTitle("There Is No Connection!")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });

                        builder.create();
                        builder.show();

                        passwordText.setText("");
                        passwordConfirmationText.setText("");
                    }

                }

            }
        });

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
