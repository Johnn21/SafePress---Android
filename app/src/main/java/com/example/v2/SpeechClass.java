package com.example.v2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class SpeechClass {

    private static Activity activity;
    private static DatabaseHelper db;
    private static RelativeLayout relativeId;
    private static TextToSpeech textToSpeech;
    private static AudioManager mAudioManager;

    private static boolean isAskedToTurnOn = false;
    private static boolean isAskedToTurnOff = false;
    private static long lastDown = 0;
    private static long lastDuration;


    public SpeechClass(Activity activity, DatabaseHelper db, RelativeLayout relativeId, TextToSpeech textToSpeech, AudioManager mAudioManager){
        this.activity = activity;
        this.db = db;
        this.relativeId = relativeId;
        this.textToSpeech = textToSpeech;
        this.mAudioManager = mAudioManager;
    }

    public static void listenUser(){


        final SpeechRecognizer speechRecognizer;
        final Intent speechRecognizerIntent;

        final LoadingDialog loadingDialog = new LoadingDialog(activity);


        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {


                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
                ArrayList<String> matchesFound = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if(matchesFound != null){
                    String keeper = matchesFound.get(0);

                    Toast.makeText(activity, "Result = "+keeper , Toast.LENGTH_LONG).show();

                    if(keeper.toLowerCase().contains("record") || keeper.toLowerCase().contains("tell") || keeper.toLowerCase().contains("me")){

                        isAskedToTurnOff = false;
                        isAskedToTurnOn = false;

                        if(db.getValidCode() == 1){
                            String s = "You have " + db.getValidCode() + " valid code and "+ db.getInvalidCode() + " invalid codes";

                            int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);
                        }

                        if(db.getInvalidCode() == 1){
                            String s = "You have " + db.getValidCode() + " valid codes and "+ db.getInvalidCode() + " invalid code";

                            int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);
                        }

                        if(db.getInvalidCode() == 1 && db.getValidCode() == 1){
                            String s = "You have " + db.getValidCode() + " valid code and "+ db.getInvalidCode() + " invalid code";

                            int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);
                        }

                        if(db.getValidCode() !=1 && db.getInvalidCode() != 1){
                            String s = "You have " + db.getValidCode() + " valid codes and "+ db.getInvalidCode() + " invalid codes";

                            int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);
                        }

                    }
                    if(keeper.toLowerCase().contains("thank")){
                        String s = "You are welcome !";

                        int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);
                    }


                    if(keeper.toLowerCase().contains("on")){
                        String s = "Are you sure about that?";

                        isAskedToTurnOn = true;
                        isAskedToTurnOff = false;

                        keeper = "";

                        int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);
                    }

                    if(keeper.toLowerCase().contains("of")){
                        String s = "Are you sure about that?";

                        isAskedToTurnOff = true;
                        isAskedToTurnOn = false;
                        keeper = "";
                        int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);
                    }

                    if(keeper.toLowerCase().contains("yes") && isAskedToTurnOff){

                        boolean switchState;

                        if(db.getSystemState().contains("on")){
                            switchState = true;
                        }else{
                            switchState = false;
                        }


                        if(switchState){

                            if(BluetoothActivity.hasDevice){

                                db.updateSystemState("1", "off");

                                BluetoothActivity.keepEvidence.interrupt();

                                try {
                                    BluetoothActivity.keepEvidence.join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                ConnectionClass connect;
                                connect = new ConnectionClass(BluetoothActivity.mBTSocket);
                                connect.write("3");

                                loadingDialog.startLoadingDialog();

                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadingDialog.dismissDialog();
                                        //      dialog.show(getSupportFragmentManager(), "dialog");
                                        Toast.makeText(activity, "System Off!", Toast.LENGTH_LONG).show();

                                        ConnectionClass connect =  new ConnectionClass(BluetoothActivity.mBTSocket);
                                        BluetoothActivity.keepEvidence = new BluetoothActivity.keepBluetoothEvidence(connect, db);
                                        BluetoothActivity.keepEvidence.start();

                                    }
                                }, 800);


                                String s = "System off!";


                                MenuActivity.isSwitched = true;

                                int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);

                                isAskedToTurnOff = false;
                                isAskedToTurnOn = false;

                            }else{
                                String s = "There is no connection!";

                                int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);

                                isAskedToTurnOff = false;
                                isAskedToTurnOn = false;


                            }

                        }else{

                            if(BluetoothActivity.hasDevice){
                                String s = "System is already off!";

                                int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);

                                isAskedToTurnOff = false;
                                isAskedToTurnOn = false;
                            }else{
                                String s = "There is no connection!";

                                int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);

                                isAskedToTurnOff = false;
                                isAskedToTurnOn = false;
                            }

                        }
                    }else if(keeper.toLowerCase().contains("yes") && isAskedToTurnOn){
                        boolean switchState;

                        if(db.getSystemState().contains("on")){
                            switchState = true;
                        }else{
                            switchState = false;
                        }

                        if(!switchState){
                            if(BluetoothActivity.hasDevice){

                                db.updateSystemState("1", "on");

                                BluetoothActivity.keepEvidence.interrupt();

                                try {
                                    BluetoothActivity.keepEvidence.join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                ConnectionClass connect;
                                connect = new ConnectionClass(BluetoothActivity.mBTSocket);
                                connect.write("2");

                                loadingDialog.startLoadingDialog();

                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadingDialog.dismissDialog();
                                        Toast.makeText(activity, "System On!", Toast.LENGTH_LONG).show();

                                        ConnectionClass connect =  new ConnectionClass(BluetoothActivity.mBTSocket);
                                        BluetoothActivity.keepEvidence = new BluetoothActivity.keepBluetoothEvidence(connect, db);
                                        BluetoothActivity.keepEvidence.start();

                                    }
                                }, 800);

                                String s = "System On!";

                                MenuActivity.isSwitched = true;

                                int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);

                                isAskedToTurnOff = false;
                                isAskedToTurnOn = false;

                            }else{
                                String s = "There is no connection!";

                                int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);

                                isAskedToTurnOff = false;
                                isAskedToTurnOn = false;
                            }
                        }else{
                            if(BluetoothActivity.hasDevice){
                                String s = "System is already on!";

                                int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);

                                isAskedToTurnOff = false;
                                isAskedToTurnOn = false;
                            }else{
                                String s = "There is no connection!";

                                int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);

                                isAskedToTurnOff = false;
                                isAskedToTurnOn = false;
                            }
                        }

                    }else if(keeper.toLowerCase().contains("reset") || keeper.toLowerCase().contains("input") || keeper.toLowerCase().contains("password")){
                        if(BluetoothActivity.hasDevice) {

                            BluetoothActivity.keepEvidence.interrupt();

                            try {
                                BluetoothActivity.keepEvidence.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            ConnectionClass connect;
                            connect = new ConnectionClass(BluetoothActivity.mBTSocket);
                            connect.write("4");

                            loadingDialog.startLoadingDialog();

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    loadingDialog.dismissDialog();
                                    //      dialog.show(getSupportFragmentManager(), "dialog");
                                    String s = "Input Reset!";

                                    int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);

                                    ConnectionClass connect =  new ConnectionClass(BluetoothActivity.mBTSocket);
                                    BluetoothActivity.keepEvidence = new BluetoothActivity.keepBluetoothEvidence(connect, db);
                                    BluetoothActivity.keepEvidence.start();

                                }
                            }, 800);

                        }else if(db.getSystemState().contains("off")){
                            String s = "System is off!";

                            int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);
                        }else{
                            String s = "There is no connection!";

                            int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);

                        }


                    }

                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

        relativeId.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    lastDown = System.currentTimeMillis();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    lastDuration = System.currentTimeMillis() - lastDown;
                    if(lastDuration >= 1000){
                        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                        speechRecognizer.startListening(speechRecognizerIntent);
                    }

                }

                return true;

            }
        });


    }






}
