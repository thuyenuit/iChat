package com.example.thuyenbu.uitchat.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.example.thuyenbu.uitchat.model.Friend;
import com.example.thuyenbu.uitchat.model.FriendRequest;
import com.example.thuyenbu.uitchat.model.ListFriend;
import com.example.thuyenbu.uitchat.model.ListFriendRequest;
import com.example.thuyenbu.uitchat.model.Request;

public class RequestDB {
    private static RequestDBHelper mDbHelper = null;

    private RequestDB() { }

    private static RequestDB instance = null;

    public static RequestDB getInstance(Context context) {
        if (instance == null) {
            instance = new RequestDB();
            mDbHelper = new RequestDBHelper(context);
        }
        return instance;
    }

    public long addFriendRequest(FriendRequest friend, String userId) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_ID, friend.id);
        values.put(FeedEntry.COLUMN_NAME_EMAIL, friend.email);
        values.put(FeedEntry.COLUMN_NAME_STATUS, "0");
        values.put(FeedEntry.COLUMN_NAME_USER_ID, userId);
        long checkUser = DatabaseUtils.queryNumEntries(db, FeedEntry.TABLE_NAME
                                                    +  " WHERE " + FeedEntry.COLUMN_NAME_ID +"='"+ friend.id +"'"
                                                    +  " AND " + FeedEntry.COLUMN_NAME_USER_ID +"='"+ userId +"'");
        if(checkUser > 0)
        {
            return 0;
        }
        // Insert the new row, returning the primary key value of the new row
        return db.insert(FeedEntry.TABLE_NAME, null, values);
    }

    /*public void addListFriendRequest(ListFriendRequest listFriend){
        for(FriendRequest friend: listFriend.getListRequest()){
            addFriendRequest(friend);
        }
    }*/

    public ListFriendRequest getListFriendRequest(String userID) {
        ListFriendRequest listFriend = new ListFriendRequest();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // Define a projection that specifies which columns from the database
// you will actually use after this query.
        try {
            Cursor cursor = db.rawQuery("select * from " + FeedEntry.TABLE_NAME +
                                            " where " +  FeedEntry.COLUMN_NAME_STATUS + "='0'"
                                            + " and " +  FeedEntry.COLUMN_NAME_USER_ID + "='"+ userID +"'", null);
            while (cursor.moveToNext()) {
                FriendRequest friend = new FriendRequest();
                friend.id = cursor.getString(0);
                friend.email = cursor.getString(1);

                listFriend.getListRequest().add(friend);
            }
            cursor.close();
        }catch (Exception e){
            return new ListFriendRequest();
        }
        return listFriend;
    }

    public void dropDB(){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        static final String TABLE_NAME = "friend";
        static final String COLUMN_NAME_ID = "friendID";
        static final String COLUMN_NAME_EMAIL = "email";
        static final String COLUMN_NAME_STATUS = "status";
        static final String COLUMN_NAME_USER_ID = "userID";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry.COLUMN_NAME_ID + " TEXT PRIMARY KEY," +
                    FeedEntry.COLUMN_NAME_STATUS + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_USER_ID + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_EMAIL + ")";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;


    private static class RequestDBHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        static final int DATABASE_VERSION = 1;
        static final String DATABASE_NAME = "RequestChat.db";

        RequestDBHelper(Context context) {
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