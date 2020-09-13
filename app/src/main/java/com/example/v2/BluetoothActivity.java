package com.example.v2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {

    private ImageView bluetoothIv;
    private TextView mBluetoothStatus;
    private Button mScanBtn;
    private Button mOffBtn;
    private Button mListPairedDevicesBtn;
    private Button mDiscoverBtn;
    private Button mDisconnectDevice;


    public static BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;
    public static BluetoothSocket mBTSocket = null;

    private DatabaseHelper db;

    public static String currentDevice = null;
    public static boolean hasDevice = false;

    public static int valid = 0;

    public static Handler handler;


    private static int hasEntered = 0;
    private static int discoveredDevices = 0;


    private static ConnectionClass connect;

    private Handler mHandler;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final static int REQUEST_ENABLE_BT = 1;
    public final static int MESSAGE_READ = 2;
    private final static int CONNECTING_STATUS = 3;

    private static int checkView = 0;
    private static int checkViewPaired = 0;

    private static int cntResume = 0;

    private int invalidInputs = 0;
    private int validInputs = 0;


    public static keepBluetoothEvidence keepEvidence;


    private AlertDialog.Builder dialogPairedDevices ;
    private AlertDialog.Builder dialogDiscoverDevices ;

    private AlertDialog dialog;

    RelativeLayout relativeIdBluetooth;

    static TextToSpeech textToSpeech;

    AudioManager mAudioManager;

    SpeechClass speechClass;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_layout);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(bluetoothState, filter);

        dialogPairedDevices= new AlertDialog.Builder(BluetoothActivity.this);
        dialogDiscoverDevices  = new AlertDialog.Builder(BluetoothActivity.this);

        textToSpeech =  new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int lang = textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });

        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        createNotificationChannel();

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "notification")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle("User Entered A Code:")
                .setContentText("Valid Code!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        final NotificationCompat.Builder builder1 = new NotificationCompat.Builder(this, "notification")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle("User Entered A Code:")
                .setContentText("Invalid Code")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        final NotificationCompat.Builder builder2 = new NotificationCompat.Builder(this, "notification")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle("User Entered A Code:")
                .setContentText("You Have New Attempts")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);



        db = new DatabaseHelper(this);

        relativeIdBluetooth = (RelativeLayout)findViewById(R.id.relativeIdBluetooth);

        speechClass = new SpeechClass(BluetoothActivity.this, db, relativeIdBluetooth, textToSpeech, mAudioManager);

        speechClass.listenUser();


        bluetoothIv = (ImageView)findViewById(R.id.bluetoothIv);
        mBluetoothStatus = (TextView)findViewById(R.id.bluetoothStatus);
        mScanBtn = (Button)findViewById(R.id.scan);
        mOffBtn = (Button)findViewById(R.id.off);
        mDiscoverBtn = (Button)findViewById(R.id.discover);
        mListPairedDevicesBtn = (Button)findViewById(R.id.PairedBtn);
        mDisconnectDevice = (Button)findViewById(R.id.disconnectPairedDevice);


        if (ContextCompat.checkSelfPermission(BluetoothActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }


        mBTArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();

        if(!mBTAdapter.isEnabled()){

            mBluetoothStatus.setText("Bluetooth Disabled");
            bluetoothIv.setImageResource(R.drawable.ic_action_off);


            if(mBTSocket != null) {
                try {
                    mBTSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                db.updateCurrentDevice("1", " ", " ");

                hasDevice = false;

            }


        }else if(currentDevice!= null){

            mBluetoothStatus.setText(" "+ currentDevice);
            bluetoothIv.setImageResource(R.drawable.ic_action_on);
        }else {
            mBluetoothStatus.setText("Bluetooth Enabled");
            bluetoothIv.setImageResource(R.drawable.ic_action_on);
        }



        mDevicesListView= new ListView(this);
        mDevicesListView.setAdapter(mBTArrayAdapter);
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);


        mHandler = new Handler(){
            public void handleMessage(android.os.Message msg){

                if(msg.what == CONNECTING_STATUS){
                    if(msg.arg1 == 1){
                        currentDevice = ("Connected to Device: " + (String)(msg.obj));
                        hasDevice = true;
                        mBluetoothStatus.setText(" "+ currentDevice);
                    }
                    else {

                        mBTSocket = null;

                        CountDownTimer messageCountDown;
                        messageCountDown = new CountDownTimer(2000, 1000){

                            @Override
                            public void onTick(long millisUntilFinished) {
                                mBluetoothStatus.setText("Connection Failed");
                            }

                            @Override
                            public void onFinish() {
                                mBluetoothStatus.setText("Bluetooth Enabled");
                            }
                        };

                        messageCountDown.start();

                    }
                }
            }
        };

        handler = new Handler(){
            @SuppressLint("HandlerLeak")
            public void handleMessage(android.os.Message msg){


                if(msg.what == MESSAGE_READ){
                    String readMessage = "";
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }


                    mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);


                    if(readMessage.matches(".*[,].*") && !(readMessage.matches(".*[n].*")) && !(readMessage.matches(".*[z].*"))){
                        int untilComma = readMessage.indexOf(",");

                        String subString = null;
                        if(untilComma != -1){
                            subString = readMessage.substring(0, untilComma);
                        }

                        validInputs = Integer.parseInt(subString);


                        subString = readMessage.substring(readMessage.indexOf(",") + 1);

                        untilComma = subString.indexOf(",");

                        String subStringTwo = null;

                        if(untilComma != -1){
                            subStringTwo = subString.substring(0, untilComma);
                        }

                        invalidInputs = Integer.parseInt(subStringTwo);

                        validInputs = validInputs + db.getValidCode();
                        invalidInputs = invalidInputs + db.getInvalidCode();

                        db.updateInputs("1", validInputs, invalidInputs);


                        notificationManager.notify(100, builder2.build());


                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                Toast.makeText(BluetoothActivity.this, "You Have New Attempts!", Toast.LENGTH_LONG).show();

                                String s = "You Have New Attempts!";

                                int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);

                            }
                        }, 2000);




                    }else if(readMessage.matches(".*[n].*") || readMessage.matches(".*[z].*")){

                        int result = 2;

                        if(readMessage.matches(".*[n].*")){
                            result = 1;
                        }else if(readMessage.matches(".*[z].*")){
                            result = 0;
                        }


                        if(result == 1){

                            boolean update;
                            update = db.incrementValidCode("1");
                            if(update == true){
                                valid = 1;

                                notificationManager.notify(100, builder.build());

                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        Toast.makeText(BluetoothActivity.this, "Valid Code Entered!", Toast.LENGTH_LONG).show();
                                        String s = "Valid Code Entered!";
                                        int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);

                                    }
                                }, 2000);


                            }
                        }else if(result == 0){

                            boolean update;
                            update  = db.incrementInvalidCode("1");
                            if(update == true){

                                valid = 0;

                                notificationManager.notify(100, builder1.build());

                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        Toast.makeText(BluetoothActivity.this, "Invalid Code Entered!", Toast.LENGTH_LONG).show();
                                        String s = "Invalid Code Entered!";
                                        int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);

                                    }
                                }, 2000);



                            }
                        }

                    }

                }

            }
        };


        if (mBTArrayAdapter == null) {

            mBluetoothStatus.setText("Device Doest Not Support Bluetooth");
            bluetoothIv.setImageResource(R.drawable.ic_action_off);

        }
        else {


            mScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOn(v);
                }
            });

            mOffBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    bluetoothOff(v);
                }
            });

            mDisconnectDevice.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    try {
                        disconnectDevice(v);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){

                    listPairedDevices(v);
                }
            });

            mDiscoverBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    discover(v);
                }
            });
        }
    }

    private void bluetoothOn(View view){
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mBluetoothStatus.setText("Enabling Bluetooth...");


        }
        else{
            Toast.makeText(getApplicationContext(),"Bluetooth is already on", Toast.LENGTH_SHORT).show();
        }
    }

    private void disconnectDevice(View view) throws IOException {
        if(!hasDevice){

            Toast.makeText(getApplicationContext(),"You are not connected to any device", Toast.LENGTH_SHORT).show();
        }else{


            connect.write("0");

            currentDevice = null;

            if(mBTSocket != null) {
                mBTSocket.close();
                mBluetoothStatus.setText("Bluetooth Enabled");
            }

            db.updateCurrentDevice("1", " ", " ");

            hasDevice = false;
            mBluetoothStatus.setText("Bluetooth Enabled");
            Toast.makeText(getApplicationContext(),"Disconnected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {

        super.onActivityResult(requestCode, resultCode, Data);
        if (requestCode == REQUEST_ENABLE_BT) {

            if (resultCode == RESULT_OK) {

                bluetoothIv.setImageResource(R.drawable.ic_action_on);
                mBluetoothStatus.setText("Bluetooth Enabled");
            } else{

                hasDevice = false;
                mBluetoothStatus.setText("Bluetooth Disabled");
                bluetoothIv.setImageResource(R.drawable.ic_action_off);
            }

        }
    }

    private void bluetoothOff(View view){

        hasDevice = false;


        db.updateCurrentDevice("1", " ", " ");

        if(mBTSocket != null){

            connect.write("0");

            try {
                mBTSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        mBTAdapter.disable();
        bluetoothIv.setImageResource(R.drawable.ic_action_off);
        mBluetoothStatus.setText("Bluetooth Disabled");
        Toast.makeText(getApplicationContext(),"Bluetooth turned Off", Toast.LENGTH_SHORT).show();
    }

    private void discover(View view){

        if(mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(),"Discovery stopped",Toast.LENGTH_SHORT).show();
        }
        else{
            if(mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear();


                IntentFilter filter = new IntentFilter();

                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);


                registerReceiver(blReceiver, filter);

                mBTAdapter.startDiscovery();
            }
            else{
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    final BroadcastReceiver bluetoothState = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                if (mBTAdapter.getState() == BluetoothAdapter.STATE_TURNING_OFF) {

                    mBluetoothStatus.setText("Bluetooth Disabled");
                    bluetoothIv.setImageResource(R.drawable.ic_action_off);

                    if(mBTSocket != null) {
                        try {
                            mBTSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        db.updateCurrentDevice("1", " ", " ");

                        hasDevice = false;
                    }

                }else if(mBTAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON){
                    mBluetoothStatus.setText("Bluetooth Enabled");
                    bluetoothIv.setImageResource(R.drawable.ic_action_on);
                }
            }

            if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    mBluetoothStatus.setText("Bluetooth Enabled");

                    if(mBTSocket != null) {
                        try {
                            mBTSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        hasDevice = false;

                        currentDevice = null;

                        db.updateCurrentDevice("1", " ", " ");

                        Toast.makeText(getApplicationContext(),"Disconnected", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }
    };


    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

                Toast.makeText(getApplicationContext(), "Searching Devices...", Toast.LENGTH_SHORT).show();
                discoveredDevices = 0;

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                hasEntered = 0;

                Toast.makeText(getApplicationContext(), "Search Finished", Toast.LENGTH_SHORT).show();

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {


                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(!(db.searchDeviceAfterAddress(device.getAddress()))){


                    discoveredDevices++;

                    if(device.getName() == null){
                        mBTArrayAdapter.add("-" + "\n" + device.getAddress());
                    }else {
                        mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    }

                    if(hasEntered == 0){

                        if(checkView == 1){
                            if(mDevicesListView != null){
                                if(mDevicesListView.getParent() != null)
                                    ((ViewGroup)mDevicesListView.getParent()).removeView(mDevicesListView);
                                checkView = 0;
                            }

                        }else if(checkViewPaired == 1){
                            if(mDevicesListView != null){
                                if(mDevicesListView.getParent() != null)
                                    ((ViewGroup)mDevicesListView.getParent()).removeView(mDevicesListView);
                                checkViewPaired = 0; //!!
                            }
                        }

                        dialogDiscoverDevices.setTitle("Discovered Devices");
                        dialogDiscoverDevices.setCancelable(true);


                        dialogDiscoverDevices.setView(mDevicesListView);

                        dialogDiscoverDevices.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                mBTAdapter.cancelDiscovery();

                                if(mDevicesListView != null){
                                    ((ViewGroup)mDevicesListView.getParent()).removeView(mDevicesListView);
                                    checkViewPaired = 0; //!!
                                    checkView = 0;
                                }


                                Toast.makeText(getApplicationContext(),"Discovery Stopped",Toast.LENGTH_SHORT).show();
                            }
                        });

                        dialogDiscoverDevices.setNegativeButton("Cancel", new Dialog.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                mBTAdapter.cancelDiscovery();
                                arg0.dismiss();

                                if(mDevicesListView != null){
                                    ((ViewGroup)mDevicesListView.getParent()).removeView(mDevicesListView);
                                    checkViewPaired = 0; //!!
                                    checkView = 0;
                                }


                                Toast.makeText(getApplicationContext(),"Discovery Stopped",Toast.LENGTH_SHORT).show();
                            }

                        });


                        dialog = dialogDiscoverDevices.show();
                        checkView = 1;
                        hasEntered++;
                    }

                }

            }
        }
    };


    @Override
    protected void onResume(){
        super.onResume();

        if(cntResume == 0){
            checkView = 0;

            if(checkViewPaired == 1){

                if(checkViewPaired == 1){
                    if(mDevicesListView != null){
                        if(mDevicesListView.getParent() != null)
                            ((ViewGroup)mDevicesListView.getParent()).removeView(mDevicesListView);
                    }

                }
            }

            checkViewPaired = 0;
            cntResume++;
        }

    }

    @Override
    protected  void onStop(){
        super.onStop();

        cntResume = 0;


        if(mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
        }


    }

    private void listPairedDevices(View view){


        if(mBTAdapter.isDiscovering()) {
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(), "Discovery stopped", Toast.LENGTH_SHORT).show();
        }


        mPairedDevices = mBTAdapter.getBondedDevices();
        dialogPairedDevices.setTitle("Paired Devices");
        dialogPairedDevices.setCancelable(true);

        if(checkViewPaired == 1){
            if(mDevicesListView != null){
                if(mDevicesListView.getParent() != null)
                    ((ViewGroup)mDevicesListView.getParent()).removeView(mDevicesListView);
            }

        }else if(checkView == 1){
            if(mDevicesListView != null){
                if(mDevicesListView.getParent() != null)
                    ((ViewGroup)mDevicesListView.getParent()).removeView(mDevicesListView);
            }
        }

        dialogPairedDevices.setView(mDevicesListView);

        if(mBTAdapter.isEnabled()) {

            List<String> list = db.getAllData();

            for(int i = 0;i < list.size(); i++){
                int k = 0;
                for(BluetoothDevice device : mPairedDevices){
                    if(list.get(i).equals(device.getAddress())){
                        k++;
                        break;
                    }
                }
                if(k == 0){
                    db.deleteDevice(list.get(i));
                }
            }

            for(BluetoothDevice device : mPairedDevices){
                int k = 0;
                for(int i = 0;i < list.size(); i++) {
                    if(list.get(i).equals(device.getAddress())){
                        k = 1;
                        break;
                    }
                }
                if(k == 0){
                    db.addDevices(device.getName(), device.getAddress());
                }
            }


            if(db.checkDevice()){
                mBTArrayAdapter.clear();
                for (BluetoothDevice device : mPairedDevices) {

                    mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

                }

                dialogPairedDevices.setNegativeButton("Cancel", null);

                dialog = dialogPairedDevices.show();

                checkViewPaired = 1;


                Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), "No Paired Device", Toast.LENGTH_SHORT).show();
            }

        }
        else
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }

    private void connectDevice(final String address, final String name){


        if(dialog != null){
            dialog.dismiss();
        }

        if(mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
        }


        mBluetoothStatus.setText("Connecting...");

        String currentAddress = db.getCurrentDeviceAddress();

        if(currentAddress.equals(address)){

            CountDownTimer messageCountDown;
            messageCountDown = new CountDownTimer(2000, 1000){

                @Override
                public void onTick(long millisUntilFinished) {
                    mBluetoothStatus.setText("Already Connected");
                }

                @Override
                public void onFinish() {
                    mBluetoothStatus.setText(currentDevice);
                }
            };

            messageCountDown.start();


        }else {

            if (!(currentAddress.equals(" "))) {


                try {
                    if (mBTSocket != null) {

                        connect.write("0");

                        mBTSocket.close();

                        db.updateCurrentDevice("1", " ", " ");
                        currentDevice = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            new Thread() {
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                    try {
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;

                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    try {
                        mBTSocket.connect();
                    } catch (IOException e) {

                        fail = true;


                        mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                .sendToTarget();

                    }


                    if (fail == false) {
                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();

                        connect = new ConnectionClass(mBTSocket);
                        connect.write("1");
                        connect.start();

                        keepEvidence = new keepBluetoothEvidence(connect, db);
                        keepEvidence.start();

                        db.updateCurrentDevice("1", name, address);

                        if (name == null) { //!!!
                            db.addDevices("-", address);
                        } else {
                            db.addDevices(name, address);
                        }


                    }

                }
            }.start();


        }
    }

    public AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {


        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {



            if(dialog != null){
                dialog.dismiss();
            }

            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0,info.length() - 17);

            if(!mBTAdapter.isDiscovering()){
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(BluetoothActivity.this);
                dialogBuilder.setTitle("Connect or Unpair?");

                dialogBuilder.setPositiveButton("Connect Device", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        connectDevice(address, name);

                    }
                });

                dialogBuilder.setNegativeButton("Unpair Device", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Device Unpaired", Toast.LENGTH_LONG).show();
                        BluetoothDevice device = mBTAdapter.getRemoteDevice(address);
                        db.removeDevice(address);
                        Method m = null;
                        try {
                            m = device.getClass().getMethod("removeBond", (Class[]) null);
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }

                        try {
                            m.invoke(device, (Object[]) null);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }

                        try {

                            String currentDevice = db.getCurrentDeviceAddress();

                            if(currentDevice.equals(address)){
                                db.updateCurrentDevice("1", " ", " ");
                                hasDevice = false;

                                connect.write("0");

                                if(mBTSocket != null) {
                                    mBluetoothStatus.setText("Bluetooth Enabled");
                                    currentDevice = null;
                                    mBTSocket.close();
                                }

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });


                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
            }else{

                hasDevice = false;

                connectDevice(address, name);
            }


        }
    };



    public BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "codeChannel";
            String description = "Channel for code notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notification", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static class keepBluetoothEvidence extends Thread{

        private final ConnectionClass connect;
        DatabaseHelper db;


        public keepBluetoothEvidence(ConnectionClass connect, DatabaseHelper db) {
            this.connect = connect;
            this.db = db;
        }

        public void run(){
            while(true){

                if(!mBTAdapter.isEnabled()){
                    try {
                        mBTSocket.close();
                        currentDevice = null;
                        db.updateCurrentDevice("1", " ", " ");
                        hasDevice = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


                if(currentThread().isInterrupted()){

                    return ;
                }

                connect.write("1");
                SystemClock.sleep(1000);
            }

        }

    }






}