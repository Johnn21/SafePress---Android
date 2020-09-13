package com.example.v2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Locale;

public class History extends AppCompatActivity {

    private DatabaseHelper db;
    private TextView validCode;
    private TextView invalidCode;


    private keepEvidence keep;
    RelativeLayout relativeIdHistory;

    static TextToSpeech textToSpeech;

    AudioManager mAudioManager;

    SpeechClass speechClass;

    private static int warning = 0;

    private static int checkMessage = 0;
    private static boolean showAgain = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_layout);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(bluetoothState, filter);


        db = new DatabaseHelper(this);

        validCode = (TextView)findViewById(R.id.validNumber);
        invalidCode = (TextView)findViewById(R.id.invalidNumber);

        validCode.setText("" + db.getValidCode());
        invalidCode.setText("" + db.getInvalidCode());

        relativeIdHistory = (RelativeLayout)findViewById(R.id.relativeIdHistory);


        textToSpeech =  new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int lang = textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });

        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);


        speechClass = new SpeechClass(History.this, db, relativeIdHistory, textToSpeech, mAudioManager);

        speechClass.listenUser();

        keep = new keepEvidence(db.getValidCode(), db.getInvalidCode(), db);
        keep.start();

        validCode.setOnClickListener(new DoubleClickListener() {

                @Override
				public void onDoubleClick() {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(History.this);
                    final View view = getLayoutInflater().inflate(R.layout.update_valid_codes, null);

                    final EditText editValidCode;


                    editValidCode = view.findViewById(R.id.editValidCode);


                    builder.setView(view)
                            .setTitle("Edit Valid Codes")
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    editValidCode.getText().toString();


                                    try{
                                        db.updateValidCode("1",    Integer.parseInt(editValidCode.getText().toString()));
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }

                                }
                            });



                    builder.create();
                    builder.show();

				}
			});

        invalidCode.setOnClickListener(new DoubleClickListener() {

            @Override
            public void onDoubleClick() {

                final AlertDialog.Builder builder = new AlertDialog.Builder(History.this);
                final View view = getLayoutInflater().inflate(R.layout.update_invalid_codes, null);

                final EditText editInvalidCode;


                editInvalidCode = view.findViewById(R.id.editInvalidCode);


                builder.setView(view)
                        .setTitle("Edit Invalid Codes")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                editInvalidCode.getText().toString();

                                try{
                                    db.updateInvalidCode("1",  Integer.parseInt(editInvalidCode.getText().toString()));
                                }catch (Exception e){
                                    e.printStackTrace();
                                }

                            }
                        });



                builder.create();
                builder.show();


            }
        });

        if(db.getValidCode() >= 2100000000 && db.getInvalidCode() >= 2100000000) {
            warning = 1;
        }else if(db.getValidCode() >= 2100000000 && db.getInvalidCode() < 2100000000){
            warning = 2;
        }else if(db.getValidCode() < 2100000000 && db.getInvalidCode() >= 2100000000){
            warning = 3;
        }else{
            warning = 0;
        }

        if(warning == 1 && checkMessage == 0 && showAgain){
            AlertDialog.Builder dialog = new AlertDialog.Builder(History.this);
            dialog.setCancelable(true);
            dialog.setTitle("Warning!");
            dialog.setMessage("It looks that both values that represent the number of valid codes and invalid codes are pretty big!" +
                    " We strongly advice you to reset your records or modify them or the app may not continue to update future records.");
            dialog.setNeutralButton("Don`t show again", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showAgain = false;
                }
            });

            dialog.setPositiveButton("I understand", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });


            dialog.show();
        }else if(warning == 2 && checkMessage == 0 && showAgain){
            AlertDialog.Builder dialog = new AlertDialog.Builder(History.this);
            dialog.setCancelable(true);
            dialog.setTitle("Warning!");
            dialog.setMessage("It looks that the value that represents the number of valid codes is pretty big!" +
                    " We strongly advice you to reset your records or modify this value or the app may not continue to update future records.");
            dialog.setNeutralButton("Don`t show again", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showAgain = false;
                }
            });

            dialog.setPositiveButton("I understand", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });


            dialog.show();
        }else if(warning == 3 && checkMessage == 0 && showAgain){
            AlertDialog.Builder dialog = new AlertDialog.Builder(History.this);
            dialog.setCancelable(true);
            dialog.setTitle("Warning!");
            dialog.setMessage("It looks that the value that represents the number of invalid codes is pretty big!" +
                    " We strongly advice you to reset your records or modify this value or the app may not continue to update future records.");
            dialog.setNeutralButton("Don`t show again", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showAgain = false;
                }
            });

            dialog.setPositiveButton("I understand", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });


            dialog.show();
        }




        final Button resetRecordButton = (Button) findViewById(R.id.resetRecordButton);
        resetRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(History.this);
                dialog.setMessage("Are you sure you want to reset the user record?");
                dialog.setCancelable(true);

                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.resetRecords("1");
                        validCode.setText("" + db.getValidCode());
                        invalidCode.setText("" + db.getInvalidCode());
                        Toast.makeText(History.this, "Record Reset!", Toast.LENGTH_LONG).show();
                    }
                });

                dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                dialog.show();


            }
        });

    }

    @Override
    protected void onResume(){
        super.onResume();

        if(warning != 0){

            checkMessage++;

            if(checkMessage == 5){
                checkMessage = 0;
            }

        }else{
            checkMessage = 0;
        }
    }

    @Override
    protected void onStop(){
        super.onStop();

        keep.interrupt();


    }

    public class keepEvidence extends Thread{

        int validCodeNumber, invalidCodeNumber;

        TextView validCode = (TextView)findViewById(R.id.validNumber);
        TextView invalidCode = (TextView)findViewById(R.id.invalidNumber);

        DatabaseHelper db;

        public keepEvidence(int validCodeNumber, int invalidCodeNumber, DatabaseHelper db) {
            this.validCodeNumber = validCodeNumber;
            this.invalidCodeNumber = invalidCodeNumber;
            this.db = db;
        }

        public void run(){
            while(true){

                if(currentThread().isInterrupted()){
                    return ;
                }

                if(validCodeNumber != db.getValidCode() || invalidCodeNumber != db.getInvalidCode()){


                    validCode.setText("" + db.getValidCode());
                    invalidCode.setText("" + db.getInvalidCode());
                }

            }

        }

    }

    public abstract class DoubleClickListener implements View.OnClickListener {


        private static final long DEFAULT_QUALIFICATION_SPAN = 200;
        private long doubleClickQualificationSpanInMillis;
        private long timestampLastClick;

        public DoubleClickListener() {
            doubleClickQualificationSpanInMillis = DEFAULT_QUALIFICATION_SPAN;
            timestampLastClick = 0;
        }

        public DoubleClickListener(long doubleClickQualificationSpanInMillis) {
            this.doubleClickQualificationSpanInMillis = doubleClickQualificationSpanInMillis;
            timestampLastClick = 0;
        }

        @Override
        public void onClick(View v) {
            if((SystemClock.elapsedRealtime() - timestampLastClick) < doubleClickQualificationSpanInMillis) {
                onDoubleClick();
            }
            timestampLastClick = SystemClock.elapsedRealtime();
        }

        public abstract void onDoubleClick();

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
