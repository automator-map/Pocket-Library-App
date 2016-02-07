package com.example.pocketlibrary.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.example.pocketlibrary.MainActivity;
import com.example.pocketlibrary.R;
import com.example.pocketlibrary.data.PocketLibraryContract;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class BookService extends IntentService {

    private final String LOG_TAG = BookService.class.getSimpleName();

    public static final String FETCH_BOOK = "com.example.pocketlibrary.services.action.FETCH_BOOK";
    public static final String DELETE_BOOK = "com.example.pocketlibrary.services.action.DELETE_BOOK";

    public static final String ISBN = "com.example.pocketlibrary.services.extra.ISBN";

    public BookService() {
        super("PocketLibrary");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(LOG_TAG, "BookService onHandleIntent");

        if (intent != null) {
            final String action = intent.getAction();
            if (action.equals(FETCH_BOOK)) {
                final String isbn = intent.getStringExtra(ISBN);
                fetchBookData(isbn);
            } else if (action.equals(DELETE_BOOK)) {
                final String isbn = intent.getStringExtra(ISBN);
                deleteBook(isbn);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void deleteBook(String isbn) {
        if(isbn!=null && !isbn.equals("")) {
            getContentResolver().delete(PocketLibraryContract.BookEntry.buildBookUri(Long.parseLong(isbn)), null, null);
        }
    }

    /**
     * Handle action fetchBookData in the provided background thread with the provided
     * parameters.
     */
    private void fetchBookData(String isbn) {

        Log.d(LOG_TAG, "BookService fetchBookData");

        if(isbn.length()!=13){
            return;
        }

        Cursor bookEntry = getContentResolver().query(
                PocketLibraryContract.BookEntry.buildBookUri(Long.parseLong(isbn)),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if(bookEntry.getCount()>0){
            Log.d(LOG_TAG, "bookEntry.getCount()>0");
            bookEntry.close();
            return;
        }

        bookEntry.close();

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJsonString = null;

        try {
            final String GOOGLE_BOOKS_API_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
            final String QUERY_PARAM = "q";

            final String ISBN_PARAM = "isbn:" + isbn;

            Uri builtUri = Uri.parse(GOOGLE_BOOKS_API_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, ISBN_PARAM)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }

            if (buffer.length() == 0) {
                return;
            }
            bookJsonString = buffer.toString();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }

        }

        final String ITEMS = "items";

        final String VOLUME_INFO = "volumeInfo";

        final String TITLE = "title";
        final String SUBTITLE = "subtitle";
        final String AUTHORS = "authors";
        final String DESC = "description";
        final String CATEGORIES = "categories";
        final String IMG_URL_PATH = "imageLinks";
        final String IMG_URL = "thumbnail";

        try {
            JSONObject bookJson = new JSONObject(bookJsonString);
            JSONArray bookArray;
            if(bookJson.has(ITEMS)){
                bookArray = bookJson.getJSONArray(ITEMS);
            }else{
                Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
                messageIntent.putExtra(MainActivity.MESSAGE_KEY,getResources().getString(R.string.not_found));
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
                return;
            }

            JSONObject bookInfo = ((JSONObject) bookArray.get(0)).getJSONObject(VOLUME_INFO);

            String title = bookInfo.getString(TITLE);

            String subtitle = "";
            if(bookInfo.has(SUBTITLE)) {
                subtitle = bookInfo.getString(SUBTITLE);
            }

            String desc="";
            if(bookInfo.has(DESC)){
                desc = bookInfo.getString(DESC);
            }

            String imgUrl = "";
            if(bookInfo.has(IMG_URL_PATH) && bookInfo.getJSONObject(IMG_URL_PATH).has(IMG_URL)) {
                imgUrl = bookInfo.getJSONObject(IMG_URL_PATH).getString(IMG_URL);
            }

            writeBackBook(isbn, title, subtitle, desc, imgUrl);

            if(bookInfo.has(AUTHORS)) {
                writeBackAuthors(isbn, bookInfo.getJSONArray(AUTHORS));
            }
            if(bookInfo.has(CATEGORIES)){
                writeBackCategories(isbn,bookInfo.getJSONArray(CATEGORIES) );
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error ", e);
        }
    }

    private void writeBackBook(String isbn, String title, String subtitle, String desc, String imgUrl) {
        ContentValues values= new ContentValues();
        values.put(PocketLibraryContract.BookEntry._ID, isbn);
        values.put(PocketLibraryContract.BookEntry.TITLE, title);
        values.put(PocketLibraryContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(PocketLibraryContract.BookEntry.SUBTITLE, subtitle);
        values.put(PocketLibraryContract.BookEntry.DESC, desc);
        getContentResolver().insert(PocketLibraryContract.BookEntry.CONTENT_URI,values);
    }

    private void writeBackAuthors(String isbn, JSONArray jsonArray) throws JSONException {
        ContentValues values= new ContentValues();
        for (int i = 0; i < jsonArray.length(); i++) {
            values.put(PocketLibraryContract.AuthorEntry._ID, isbn);
            values.put(PocketLibraryContract.AuthorEntry.AUTHOR, jsonArray.getString(i));
            getContentResolver().insert(PocketLibraryContract.AuthorEntry.CONTENT_URI, values);
            values= new ContentValues();
        }
    }

    private void writeBackCategories(String isbn, JSONArray jsonArray) throws JSONException {
        ContentValues values= new ContentValues();
        for (int i = 0; i < jsonArray.length(); i++) {
            values.put(PocketLibraryContract.CategoryEntry._ID, isbn);
            values.put(PocketLibraryContract.CategoryEntry.CATEGORY, jsonArray.getString(i));
            getContentResolver().insert(PocketLibraryContract.CategoryEntry.CONTENT_URI, values);
            values= new ContentValues();
        }
    }
 }