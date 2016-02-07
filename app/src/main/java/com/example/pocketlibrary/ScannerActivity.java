package com.example.pocketlibrary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;


public class ScannerActivity extends Activity implements ZBarScannerView.ResultHandler {

    private static final String LOG_TAG = ScannerActivity.class.getSimpleName();

    public static final String SCAN_RESULT = "SCAN_RESULT";

    private ZBarScannerView mScannerView;


    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZBarScannerView(this);    // Programmatically initialize the scanner view
        //mScannerView.startCamera();
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {

        if (rawResult != null) {

            Intent returnIntent = new Intent();
            returnIntent.putExtra(SCAN_RESULT, rawResult.getContents());

            setResult(RESULT_OK, returnIntent);

            // Do something with the result here
            Log.w(LOG_TAG, "rawResult: " + rawResult); // Prints scan results
//            Log.w(LOG_TAG, "rawResult.getContents(): " + rawResult.getContents()); // Prints scan results
//            Log.w(LOG_TAG, "rawResult.getBarcodeFormat().getName()): " + rawResult.getBarcodeFormat().getName()); // Prints the scan format (qrcode, pdf417 etc.)

            finish();
        }
    }

}
