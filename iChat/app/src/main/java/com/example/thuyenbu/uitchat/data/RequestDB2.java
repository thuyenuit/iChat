package com.example.thuyenbu.uitchat.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.example.thuyenbu.uitchat.model.UserRequest;

import java.util.ArrayList;
import java.util.List;

public class RequestDB2 {

    private static RequestDB2Helper mDbHelper = null;

    private RequestDB2() { }

    private static RequestDB2 instance = null;

    public static RequestDB2 getInstance(Context context) {
        if (instance == null) {
            instance = new RequestDB2();
            mDbHelper = new RequestDB2Helper(context);
        }
        return instance;
    }

    public long CheckUser(String idSender, String idReceiver) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return DatabaseUtils.queryNumEntries(db, FeedEntry.TABLE_NAME
                                        +  " WHERE " + FeedEntry.COLUMN_NAME_SENDER_USER_ID +"='"+ idSender +"'"
                                        + " AND " + FeedEntry.COLUMN_NAME_RECEIVER_ID +"='"+ idReceiver +"'");
    }

    public long addRequest(UserRequest user) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_SENDER_ID, user.userIdSender);
        values.put(FeedEntry.COLUMN_NAME_RECEIVER_ID, user.userIdReceiver);
        values.put(FeedEntry.COLUMN_NAME_SENDER_USER_ID, user.userIdSender);
        values.put(FeedEntry.COLUMN_NAME_TYPE_REQUEST, user.requestType);

        if(CheckUser(user.userIdSender, user.userIdReceiver) == 0){
            // Insert the new row, returning the primary key value of the new row
            return db.insert(FeedEntry.TABLE_NAME, null, values);
        }
       return  0;
    }

    public void deleteRequest(String userIdSender, String userIdReceiver)
    {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + FeedEntry.TABLE_NAME+ " WHERE "
                + FeedEntry.COLUMN_NAME_SENDER_USER_ID +"='"+ userIdSender +"'"
                + " AND "+ FeedEntry.COLUMN_NAME_RECEIVER_ID +"='"+ userIdReceiver +"'");
    }

    public long deleteAllRequest()
    {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.delete(FeedEntry.TABLE_NAME, null, null);
    }


    public List<UserRequest> getListRequest(String userLogin) {
        List<UserRequest> listRequest = new ArrayList<UserRequest>();

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // Define a projection that specifies which columns from the database
// you will actually use after this query.
        try {
            Cursor cursor = db.rawQuery("select * from " + FeedEntry.TABLE_NAME
                                        + " WHERE " + FeedEntry.COLUMN_NAME_RECEIVER_ID + "='" + userLogin+ "'"
                                        + " AND " + FeedEntry.COLUMN_NAME_SENDER_USER_ID + "<>'" + userLogin+ "'", null);
            while (cursor.moveToNext()) {
                UserRequest request = new UserRequest();
                request.userIdSender = cursor.getString(0);
                request.userIdReceiver = cursor.getString(1);
                request.requestType = cursor.getString(3);
                listRequest.add(request);
            }
            cursor.close();
        }catch (Exception e){
            return new ArrayList<UserRequest>();
        }
        return listRequest;
    }

    public void dropDB(){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        static final String TABLE_NAME = "requestuser";
        static final String COLUMN_NAME_SENDER_ID = "senderId";
        static final String COLUMN_NAME_RECEIVER_ID = "receiverId";
        static final String COLUMN_NAME_SENDER_USER_ID = "userSenderId";
        static final String COLUMN_NAME_TYPE_REQUEST = "typerequest";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry.COLUMN_NAME_SENDER_ID + " TEXT PRIMARY KEY," +
                    FeedEntry.COLUMN_NAME_RECEIVER_ID + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_SENDER_USER_ID + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_TYPE_REQUEST + TEXT_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;


    private static class RequestDB2Helper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        static final int DATABASE_VERSION = 1;
        static final String DATABASE_NAME = "RequestUser2.db";

        RequestDB2Helper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
