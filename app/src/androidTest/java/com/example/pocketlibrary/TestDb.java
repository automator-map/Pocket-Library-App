package com.example.pocketlibrary;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

import com.example.pocketlibrary.data.PocketLibraryContract;
import com.example.pocketlibrary.data.DbHelper;

public class TestDb extends AndroidTestCase {
    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public final static long isbn = 9780137903955L;
    public final static String title = "Artificial Intelligence";
    public final static String subtitle = "A Modern Approach";
    public final static String imgUrl = "http://books.google.com/books/content?id=KI2WQgAACAAJ&printsec=frontcover&img=1&zoom=1";
    public final static String desc = "Presents a guide to artificial intelligence, covering such topics as intelligent agents, problem-solving, logical agents, planning, uncertainty, learning, and robotics.";
    public final static String author = "Stuart Jonathan Russell";
    public final static String category = "Computers";

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(DbHelper.DATABASE_NAME);
        SQLiteDatabase db = new DbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {

        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = getBookValues();

        long retEan = db.insert(PocketLibraryContract.BookEntry.TABLE_NAME, null, values);
        assertEquals(isbn, retEan);

        String[] columns = {
                PocketLibraryContract.BookEntry._ID,
                PocketLibraryContract.BookEntry.TITLE,
                PocketLibraryContract.BookEntry.IMAGE_URL,
                PocketLibraryContract.BookEntry.SUBTITLE,
                PocketLibraryContract.BookEntry.DESC
        };

        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                PocketLibraryContract.BookEntry.TABLE_NAME,  // Table to Query
                columns,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(cursor, values);

        values = getAuthorValues();


        retEan = db.insert(PocketLibraryContract.AuthorEntry.TABLE_NAME, null, values);

        columns = new String[]{
                PocketLibraryContract.AuthorEntry._ID,
                PocketLibraryContract.AuthorEntry.AUTHOR
        };

        cursor = db.query(
                PocketLibraryContract.AuthorEntry.TABLE_NAME,  // Table to Query
                columns,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(cursor, values);
        // test category table

        values = getCategoryValues();
        retEan = db.insert(PocketLibraryContract.CategoryEntry.TABLE_NAME, null, values);

        columns = new String[]{
                PocketLibraryContract.CategoryEntry._ID,
                PocketLibraryContract.CategoryEntry.CATEGORY
        };

        cursor = db.query(
                PocketLibraryContract.CategoryEntry.TABLE_NAME,  // Table to Query
                columns,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(cursor, values);

        dbHelper.close();

    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(columnName,idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }

    public static ContentValues getBookValues() {

        final ContentValues values = new ContentValues();
        values.put(PocketLibraryContract.BookEntry._ID, isbn);
        values.put(PocketLibraryContract.BookEntry.TITLE, title);
        values.put(PocketLibraryContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(PocketLibraryContract.BookEntry.SUBTITLE, subtitle);
        values.put(PocketLibraryContract.BookEntry.DESC, desc);

        return values;
    }

    public static ContentValues getAuthorValues() {

        final ContentValues values= new ContentValues();
        values.put(PocketLibraryContract.AuthorEntry._ID, isbn);
        values.put(PocketLibraryContract.AuthorEntry.AUTHOR, author);

        return values;
    }

    public static ContentValues getCategoryValues() {

        final ContentValues values= new ContentValues();
        values.put(PocketLibraryContract.CategoryEntry._ID, isbn);
        values.put(PocketLibraryContract.CategoryEntry.CATEGORY, category);

        return values;
    }

    public static ContentValues getFullDetailValues() {

        final ContentValues values= new ContentValues();
        values.put(PocketLibraryContract.BookEntry.TITLE, title);
        values.put(PocketLibraryContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(PocketLibraryContract.BookEntry.SUBTITLE, subtitle);
        values.put(PocketLibraryContract.BookEntry.DESC, desc);
        values.put(PocketLibraryContract.AuthorEntry.AUTHOR, author);
        values.put(PocketLibraryContract.CategoryEntry.CATEGORY, category);
        return values;
    }

    public static ContentValues getFullListValues() {

        final ContentValues values= new ContentValues();
        values.put(PocketLibraryContract.BookEntry.TITLE, title);
        values.put(PocketLibraryContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(PocketLibraryContract.AuthorEntry.AUTHOR, author);
        values.put(PocketLibraryContract.CategoryEntry.CATEGORY, category);
        return values;
    }
}