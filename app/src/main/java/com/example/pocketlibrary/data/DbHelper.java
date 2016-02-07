package com.example.pocketlibrary.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "pocketlibrary.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_BOOK_TABLE = "CREATE TABLE " + PocketLibraryContract.BookEntry.TABLE_NAME + " ("+
                PocketLibraryContract.BookEntry._ID + " INTEGER PRIMARY KEY," +
                PocketLibraryContract.BookEntry.TITLE + " TEXT NOT NULL," +
                PocketLibraryContract.BookEntry.SUBTITLE + " TEXT ," +
                PocketLibraryContract.BookEntry.DESC + " TEXT ," +
                PocketLibraryContract.BookEntry.IMAGE_URL + " TEXT, " +
                "UNIQUE ("+ PocketLibraryContract.BookEntry._ID +") ON CONFLICT IGNORE)";

        final String SQL_CREATE_AUTHOR_TABLE = "CREATE TABLE " + PocketLibraryContract.AuthorEntry.TABLE_NAME + " ("+
                PocketLibraryContract.AuthorEntry._ID + " INTEGER," +
                PocketLibraryContract.AuthorEntry.AUTHOR + " TEXT," +
                " FOREIGN KEY (" + PocketLibraryContract.AuthorEntry._ID + ") REFERENCES " +
                PocketLibraryContract.BookEntry.TABLE_NAME + " (" + PocketLibraryContract.BookEntry._ID + "))";

        final String SQL_CREATE_CATEGORY_TABLE = "CREATE TABLE " + PocketLibraryContract.CategoryEntry.TABLE_NAME + " ("+
                PocketLibraryContract.CategoryEntry._ID + " INTEGER," +
                PocketLibraryContract.CategoryEntry.CATEGORY + " TEXT," +
                " FOREIGN KEY (" + PocketLibraryContract.CategoryEntry._ID + ") REFERENCES " +
                PocketLibraryContract.BookEntry.TABLE_NAME + " (" + PocketLibraryContract.BookEntry._ID + "))";


        Log.d("sql-statments",SQL_CREATE_BOOK_TABLE);
        Log.d("sql-statments",SQL_CREATE_AUTHOR_TABLE);
        Log.d("sql-statments",SQL_CREATE_CATEGORY_TABLE);

        db.execSQL(SQL_CREATE_BOOK_TABLE);
        db.execSQL(SQL_CREATE_AUTHOR_TABLE);
        db.execSQL(SQL_CREATE_CATEGORY_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
