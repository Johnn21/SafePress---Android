package com.example.v2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.io.File;
import java.sql.SQLInput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "codeChecker.db";

    private static final String CODE_TABLE = "code_table";

    private static final String CODE_ID = "code_id";
    private static final String VALID_CODE = "valid_code";
    private static final String INVALID_CODE = "invalid_code";
    private static final String CURRENT_CODE = "current_code";
    private static final String CURRENT_DATE_CODE = "current_date_code";


    private static final String DEVICE_TABLE = "device_table";

    private static final String DEVICE_ID = "device_id";
    private static final String DEVICE_NAME = "device_name";
    private static final String DEVICE_ADDRESS = "device_address";

    private static final String CURRENT_TABLE = "current_table";

    private static final String CURRENT_ID = "current_id";
    private static final String CURRENT_NAME = "current_name";
    private static final String CURRENT_ADDRESS = "current_address";

    private static final String SYSTEM_TABLE = "system_table";

    private static final String SYSTEM_ID = "system_id";
    private static final String SYSTEM_STATE = "system_state";

    private static final String PREV_PASS_TABLE = "prev_pass_table";

    private static final String PREV_PASS_ID = "prev_pass_id";
    private static final String PREV_PASS_CODE = "prev_pass_code";


    public DatabaseHelper(@Nullable Context context){
       super(context, DATABASE_NAME, null, 1);
   }

   @Override
    public void onCreate(SQLiteDatabase db){
       String createTable = "CREATE TABLE " + CODE_TABLE + "(code_id INTEGER PRIMARY KEY AUTOINCREMENT, " + " valid_code INTEGER, invalid_code INTEGER, current_code TEXT, current_date_code TEXT)";

       String createDeviceTable = "CREATE TABLE " + DEVICE_TABLE + "(device_id INTEGER PRIMARY KEY AUTOINCREMENT, " + " device_name TEXT, device_address TEXT)";

       String createCurrentTable = "CREATE TABLE " + CURRENT_TABLE + "(current_id INTEGER PRIMARY KEY AUTOINCREMENT, " + " current_name TEXT, current_address TEXT)";

       String createSystemTable = "CREATE TABLE " + SYSTEM_TABLE + "(system_id INTEGER PRIMARY KEY AUTOINCREMENT, " + " system_state TEXT)";

       String createPrevTable = "CREATE TABLE " + PREV_PASS_TABLE + "(prev_pass_id INTEGER PRIMARY KEY AUTOINCREMENT, " + " prev_pass_code TEXT)";


       db.execSQL(createTable);

       db.execSQL(createDeviceTable);

       db.execSQL(createCurrentTable);

       db.execSQL(createSystemTable);

       db.execSQL(createPrevTable);

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
       db.execSQL("DROP TABLE IF EXISTS " + CODE_TABLE);
       db.execSQL("DROP TABLE IF EXISTS " + DEVICE_TABLE);
       db.execSQL("DROP TABLE IF EXISTS " + CURRENT_TABLE);
       db.execSQL("DROP TABLE IF EXISTS " + SYSTEM_TABLE);
       db.execSQL("DROP TABLE IF EXISTS " + PREV_PASS_TABLE);


       onCreate(db);
    }


    public boolean addPreviousPassword(String prev_pass){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PREV_PASS_CODE, prev_pass);


        long result = db.insert(PREV_PASS_TABLE, null, contentValues);

        if(result == -1){
            return false;
        }else{
            return true;
        }
    }



    public boolean addDataCode(int valid_code, int invalid_code, String current_code, String current_date_code){
       SQLiteDatabase db = this.getWritableDatabase();
       ContentValues contentValues = new ContentValues();
       contentValues.put(VALID_CODE, valid_code);
       contentValues.put(INVALID_CODE, invalid_code);
       contentValues.put(CURRENT_CODE, current_code);
       contentValues.put(CURRENT_DATE_CODE, current_date_code);


       long result = db.insert(CODE_TABLE, null, contentValues);

       if(result == -1){
           return false;
       }else{
           return true;
       }
    }




    public List<String> getAllData()
    {


        SQLiteDatabase db = this.getReadableDatabase();

        List<String> list=new ArrayList<>();

        Cursor  cursor = db.rawQuery("select * from device_table",null);


        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String address = cursor.getString(cursor.getColumnIndex(DEVICE_ADDRESS));

                list.add(address);
                cursor.moveToNext();
            }
        }

        return list;
    }



    public List<String> getPreviousPasswords()
    {

        SQLiteDatabase db = this.getReadableDatabase();

        List<String> list=new ArrayList<>();


        Cursor  cursor = db.rawQuery("select * from prev_pass_table",null);

            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    String prev_pass_code = cursor.getString(cursor.getColumnIndex(PREV_PASS_CODE));

                    list.add(prev_pass_code);
                    cursor.moveToNext();
                }
            }

            return list;

    }



    public boolean addSystemSTate(String system_state){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SYSTEM_STATE, system_state);

        long result = db.insert(SYSTEM_TABLE, null, contentValues);

        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public boolean updateSystemState(String system_id, String system_state){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SYSTEM_ID, system_id);
        contentValues.put(SYSTEM_STATE, system_state);
        db.update(SYSTEM_TABLE, contentValues, "system_id = ?", new String[] {system_id});
        return true;
    }

    public String getSystemState(){
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = null;
        String systemState = null;
        try {
            cursor = db.rawQuery("SELECT system_state FROM system_table WHERE system_id = ?", new String[]{String.valueOf(1)});
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                systemState = cursor.getString(cursor.getColumnIndex("system_state"));
            }
            return systemState;
        }finally {
            cursor.close();
        }
    }

    public boolean checkDevice(){
        SQLiteDatabase db = this.getWritableDatabase();
        String count = "SELECT count(*) FROM device_table";
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
        if(icount>0)
            return true;
        else
            return false;
    }


    public boolean addDevices(String device_name, String device_address){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DEVICE_NAME, device_name);
        contentValues.put(DEVICE_ADDRESS, device_address);

        long result = db.insert(DEVICE_TABLE, null, contentValues);

        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Integer removeDevice (String device_address) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(DEVICE_TABLE, "device_address = ?",new String[] {device_address});
    }


    public boolean addCurrentDevice(String current_name, String current_address){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CURRENT_NAME, current_name);
        contentValues.put(CURRENT_ADDRESS, current_address);

        long result = db.insert(CURRENT_TABLE, null, contentValues);

        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public boolean updateCurrentDevice(String current_id, String current_name, String current_address){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CURRENT_ID, current_id);
        contentValues.put(CURRENT_NAME, current_name);
        contentValues.put(CURRENT_ADDRESS, current_address);
        db.update(CURRENT_TABLE, contentValues, "current_id = ?", new String[] {current_id});
        return true;
    }




    public int deleteDevice(String device_address){
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(DEVICE_TABLE, "device_address=?", new String[]{String.valueOf(device_address)});

    }




    public void deleteOldPasswords(){
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("delete from "+ PREV_PASS_TABLE);

    }


    public String getCurrentDeviceAddress(){
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = null;
        String currentAddress = null;
        try {
            cursor = db.rawQuery("SELECT current_address FROM current_table WHERE current_id = ?", new String[]{String.valueOf(1)});
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                currentAddress = cursor.getString(cursor.getColumnIndex("current_address"));
            }
            return currentAddress;
        }finally {
            cursor.close();
        }
    }

    public boolean searchDeviceAfterAddress(String device_address){
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = null;

        try{
            cursor = db.rawQuery("SELECT device_address FROM device_table WHERE device_address = ?", new String[]{String.valueOf(device_address)});
            if(cursor.getCount() > 0) {
                return true;
            }else{
                return false;
            }
        }
        finally {
            cursor.close();
        }
    }


    public boolean updateValidCode(String code_id, int valid_code){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CODE_ID, code_id);
        contentValues.put(VALID_CODE, valid_code);
        db.update(CODE_TABLE, contentValues, "code_id = ?", new String[] {code_id});
        return true;
    }

    public boolean updateInvalidCode(String code_id, int invalid_code){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CODE_ID, code_id);
        contentValues.put(INVALID_CODE, invalid_code);
        db.update(CODE_TABLE, contentValues, "code_id = ?", new String[] {code_id});
        return true;
    }

    public boolean updateCurrentCode(String code_id, String currentCode, String current_date_code){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CODE_ID, code_id);
        contentValues.put(CURRENT_CODE, currentCode);
        contentValues.put(CURRENT_DATE_CODE, current_date_code);
        db.update(CODE_TABLE, contentValues, "code_id = ?", new String[] {code_id});
        return true;
    }


    public String getCurrentCode(){
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = null;
        String currentCode = null;
        try {
            cursor = db.rawQuery("SELECT current_code FROM code_table WHERE code_id = ?", new String[]{String.valueOf(1)});
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                currentCode = cursor.getString(cursor.getColumnIndex("current_code"));
            }
            return currentCode;
        }finally {
            cursor.close();
        }
    }


    public String getCurrentTimeCode(){
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = null;
        String currentTime = null;
        try {
            cursor = db.rawQuery("SELECT current_date_code FROM code_table WHERE code_id = ?", new String[]{String.valueOf(1)});
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                currentTime = cursor.getString(cursor.getColumnIndex("current_date_code"));
            }
            return currentTime;
        }finally {
            cursor.close();
        }
    }


    public boolean incrementValidCode(String code_id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CODE_ID, code_id);
        db.execSQL("UPDATE " + CODE_TABLE + " SET " + VALID_CODE + "=" + VALID_CODE + "+1" + " WHERE " + CODE_ID + "=?",
                new String[] {code_id} );
        return true;
    }



    public boolean updateInputs(String code_id, int valid_code, int invalid_code){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CODE_ID, code_id);
        contentValues.put(VALID_CODE, valid_code);
        contentValues.put(INVALID_CODE, invalid_code);
        db.update(CODE_TABLE, contentValues, "code_id = ?", new String[] {code_id});
        return true;
    }




    public boolean resetRecords(String code_id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CODE_ID, code_id);
        contentValues.put(VALID_CODE, 0);
        contentValues.put(INVALID_CODE, 0);
        db.update(CODE_TABLE, contentValues, "code_id = ?", new String[] {code_id});
        return true;
    }


    public boolean incrementInvalidCode(String code_id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CODE_ID, code_id);
        db.execSQL("UPDATE " + CODE_TABLE + " SET " + INVALID_CODE + "=" + INVALID_CODE + "+1" + " WHERE " + CODE_ID + "=?",
                new String[] {code_id} );
        return true;
    }

    public int getValidCode(){
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = null;
        int validCode = 0;
        try {
            cursor = db.rawQuery("SELECT valid_code FROM code_table WHERE code_id = ?", new String[]{String.valueOf(1)});
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                validCode = cursor.getInt(cursor.getColumnIndex("valid_code"));
            }
            return validCode;
        }finally {
            cursor.close();
        }
    }

    public int getInvalidCode(){
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = null;
        int invalidCode = 0;
        try {
            cursor = db.rawQuery("SELECT invalid_code FROM code_table WHERE code_id = ?", new String[]{String.valueOf(1)});
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                invalidCode = cursor.getInt(cursor.getColumnIndex("invalid_code"));
            }
            return invalidCode;
        }finally {
            cursor.close();
        }
    }





}
