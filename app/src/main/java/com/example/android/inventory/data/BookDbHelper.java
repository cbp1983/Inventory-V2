package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.inventory.data.BookContract.BookEntry;

public class BookDbHelper extends SQLiteOpenHelper {

    // Set some constant values to use
    private static final String LOG_TAG = BookDbHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "books.db";
    private static final int DATABASE_VERSION = 1;

    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + BookEntry.TABLE_NAME + "(" +
                BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BookEntry.COLUMN_BOOK_NAME + " TEXT NOT NULL, " +
                BookEntry.COLUMN_BOOK_PRICE + " TEXT, " +
                BookEntry.COLUMN_BOOK_QUANTITY + " INTEGER, " +
                BookEntry.COLUMN_BOOK_SUPPLIER + " INTEGER NOT NULL AUTOINCREMENT DEFAULT 0, " +
                BookEntry.COLUMN_BOOK_PHONE + " TEXT NOT NULL);";

        Log.d(LOG_TAG, "OnCreate: " + SQL_CREATE_BOOKS_TABLE);

        db.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // no version change as yet
    }
}