package com.example.pocketlibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.pocketlibrary.data.PocketLibraryContract;
import com.example.pocketlibrary.services.BookService;
import com.example.pocketlibrary.services.DownloadImage;


public class AddBookFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = AddBookFragment.class.getSimpleName();

    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";
    private EditText mEditText;
    private final int LOADER_ID = 1;
    private View mRootView;
    private final String EAN_CONTENT="eanContent";

    private static final String SCAN_FORMAT = "scanFormat";
    private static final String SCAN_CONTENTS = "scanContents";

    private static final String BOOK_SAVED = "book saved";

    private String mScanFormat = "Format:";
    private String mScanContents = "Contents:";

    static final int SCAN_BARCODE_REQUEST = 1;

    private Boolean mBookSaved = false;
    private String mScanResult;

    private String mIsbn;

    public AddBookFragment(){
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(mIsbn != null) {
            outState.putString(EAN_CONTENT, mIsbn);
        }

        if (mBookSaved) {
            outState.putBoolean(BOOK_SAVED, mBookSaved);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

       mRootView = inflater.inflate(R.layout.fragment_add_book, container, false);
       mEditText = (EditText) mRootView.findViewById(R.id.isbn);

//        Bundle arguments = getArguments();
//        if (arguments != null) {
//
//            String data = arguments.getString(ScannerActivity.SCAN_RESULT);
//
//            //Once we have an ISBN, start a book intent
//            Intent bookIntent = new Intent(getActivity(), BookService.class);
//            bookIntent.putExtra(BookService.isbn, data);
//            bookIntent.setAction(BookService.FETCH_BOOK);
//            getActivity().startService(bookIntent);
//            AddBookFragment.this.restartLoader();
//        }

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {

                String isbnCode = s.toString();
                if ((isbnCode.length() == 10 && !isbnCode.startsWith("978")) || isbnCode.length() > 12) {
                    validateAndStartBookService(isbnCode, false);
                }
            }
        });

        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    View view = getActivity().getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
                return false;
            }
        });

        // Close the keyboard with the done button once all numbers are entered.
        mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);


        mRootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is the callback method that the system will invoke when your button is
                // clicked. You might do this by launching another app or by including the
                //functionality directly in this app.
                // Hint: Use a Try/Catch block to handle the Intent dispatch gracefully, if you
                // are using an external app.
                //when you're done, remove the toast below.

                Intent intent = new Intent(getContext(), ScannerActivity.class);
                startActivityForResult(intent, SCAN_BARCODE_REQUEST);

            }
        });

        mRootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // The input in mEditText is saved to mIsbn before being cleared in case the user wants to
                // delete the book immediately after adding it, on the same screen.
                mIsbn = mEditText.getText().toString();

//                mEditText.setText("");
                mBookSaved = true;
                Toast.makeText(getActivity(), getString(R.string.book_saved_confirmation), Toast.LENGTH_LONG).show();
            }
        });

        mRootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteBook();
                clearFields();
                Toast.makeText(getActivity(), getString(R.string.book_deleted_confirmation), Toast.LENGTH_LONG).show();
            }
        });

        if(savedInstanceState!=null){
            mIsbn = savedInstanceState.getString(EAN_CONTENT);

            mEditText.setText(mIsbn);
            mEditText.setHint("");

            mBookSaved = savedInstanceState.getBoolean(BOOK_SAVED);
        }

        return mRootView;
    }

//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        getLoaderManager().initLoader(LOADER_ID, null, this);
//    }

    private void restartLoader() {

        Log.w(LOG_TAG, "restartLoader");
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Log.w(LOG_TAG, "onCreateLoader mEditText.getText(): " + mEditText.getText());

        if (mIsbn.length()==0) {
            return null;
        }

        String eanStr= mIsbn;
        if (eanStr.length()==10 && !eanStr.startsWith("978")) {
            eanStr="978"+eanStr;
        }

        return new CursorLoader(
                getActivity(),
                PocketLibraryContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(PocketLibraryContract.BookEntry.TITLE));
        ((TextView) mRootView.findViewById(R.id.bookTitle)).setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(PocketLibraryContract.BookEntry.SUBTITLE));
        ((TextView) mRootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(PocketLibraryContract.AuthorEntry.AUTHOR));

        // Verify if the book has an author. If not, set "No Author" value in Author field.
        if (authors != null) {
            String[] authorsArr = authors.split(",");
            ((TextView) mRootView.findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) mRootView.findViewById(R.id.authors)).setText(authors.replace(",","\n"));
        } else {
            ((TextView) mRootView.findViewById(R.id.authors)).setText(getString(R.string.no_author));
        }


        String imgUrl = data.getString(data.getColumnIndex(PocketLibraryContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            new DownloadImage((ImageView) mRootView.findViewById(R.id.bookCover)).execute(imgUrl);
            mRootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(PocketLibraryContract.CategoryEntry.CATEGORY));
        ((TextView) mRootView.findViewById(R.id.categories)).setText(categories);

        mRootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        Log.d(LOG_TAG, "onLoaderReset");
    }

    private void clearFields(){
        ((TextView) mRootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) mRootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) mRootView.findViewById(R.id.authors)).setText("");
        ((TextView) mRootView.findViewById(R.id.categories)).setText("");
        mRootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        mRootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        mRootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }



    private void validateAndStartBookService(String inputStr, boolean isScanResult) {

        if (!inputStr.matches("[0-9]+")) {
            Toast.makeText(getActivity(), getString(R.string.isbn_format_not_valid), Toast.LENGTH_LONG).show();
            return;
        }

        if (inputStr.length() == 0) {
            Log.w(LOG_TAG, "validateAndStartBookService scanResult.length() == 0");
            return;
        }

        if (isScanResult) {
            mEditText.setText(mScanResult);
        }


        // Validate that an internet connection is available and if not notify the user.
        if (!Utility.isNetworkAvailable(getActivity())) {
            Toast.makeText(getActivity(), getString(R.string.no_internet_connection_message), Toast.LENGTH_LONG).show();
            return;
        }

        //catch isbn10 numbers
        if(inputStr.length()==10 && !inputStr.startsWith("978")){
            inputStr = "978" + inputStr;
        }

        if(!inputStr.startsWith("978") && inputStr.length()>12){
            clearFields();
            Toast.makeText(getActivity(), getString(R.string.isbn_start_not_valid), Toast.LENGTH_LONG).show();
            return;
        }

        if(inputStr.length()<13 || inputStr.length()>=14){
            clearFields();
            Toast.makeText(getActivity(), getString(R.string.isbn_not_valid), Toast.LENGTH_LONG).show();
            return;
        }

        mIsbn = inputStr;


        // Once we have an ISBN, start a book intent
        Intent bookIntent = new Intent(getActivity(), BookService.class);
        bookIntent.putExtra(BookService.ISBN, mIsbn);
        bookIntent.setAction(BookService.FETCH_BOOK);
        getActivity().startService(bookIntent);

        Log.w(LOG_TAG, "mIsbn: " + mIsbn);

        restartLoader();


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        // Check which request we're responding to
        if (requestCode == SCAN_BARCODE_REQUEST) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {

                mScanResult = data.getStringExtra(ScannerActivity.SCAN_RESULT);

                Log.d(LOG_TAG, "mScanResult: " + mScanResult);

                if (mScanResult != null) {
                    validateAndStartBookService(mScanResult, true);
                }
                // The scan result is reset after it's used in the validate method.
                mScanResult = null;
            }
        }
    }

    private void deleteBook() {
        Intent bookIntent = new Intent(getActivity(), BookService.class);

        String isbnToDelete;

        if (mIsbn != null) {
            // mIsbn is used here instead of mEditText.getText() because mEditText is cleared immediately
            // after the user saves a book.
            isbnToDelete = mIsbn;
        } else {
            isbnToDelete = mEditText.getText().toString();
        }

        if(isbnToDelete.length()==10 && !isbnToDelete.startsWith("978")){
            isbnToDelete="978"+isbnToDelete;
        }

        bookIntent.putExtra(BookService.ISBN, isbnToDelete);
        bookIntent.setAction(BookService.DELETE_BOOK);
        getActivity().startService(bookIntent);
        Log.d(LOG_TAG, "Book deleted: " + mEditText.getText().toString());
        mEditText.setText("");

        mIsbn = null;
    }


    @Override
    public void onStop() {

        Log.d(LOG_TAG, "AddBookFragment onStop()");
        Log.d(LOG_TAG, "mBookSaved: " + mBookSaved);

        if (!mBookSaved) {
            deleteBook();
        }

        mIsbn = null;

        super.onStop();
    }

}
