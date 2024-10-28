package com.appstaticsx.app.quickqr;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScannerActivity extends BaseActivity {

    private TextView resultText, urlTV;
    private FloatingActionButton createQR, scanQRC, plusButton;
    private Boolean isAllFabsVisible;
    private LinearLayout urlsection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar));

        resultText = findViewById(R.id.scannedResultTV);
        AppCompatButton copyContent = findViewById(R.id.copyContentBtn);
        createQR = findViewById(R.id.createQR);
        scanQRC = findViewById(R.id.scanQRC);
        plusButton = findViewById(R.id.plusButton);
        urlTV = findViewById(R.id.urlTV);
        AppCompatButton browseUrl = findViewById(R.id.browseUrlButton);
        urlsection = findViewById(R.id.urlSection);

        isAllFabsVisible = false;

        setupTextWatcher();

        Scancode();

        copyContent.setOnClickListener(v -> copyTextToClipboard(resultText.getText().toString()));

        createQR.setOnClickListener(view -> {
            Intent intent = new Intent(ScannerActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        scanQRC.setOnClickListener(view -> {
            Intent intent = new Intent(ScannerActivity.this, ScannerActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        plusButton.setOnClickListener(view -> {
            if (!isAllFabsVisible) {
                createQR.setVisibility(View.VISIBLE);
                scanQRC.setVisibility(View.VISIBLE);


                plusButton.setImageResource(R.drawable.cross_svgrepo_com);

                Animation showAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_show);
                createQR.startAnimation(showAnimation);
                scanQRC.startAnimation(showAnimation);

                isAllFabsVisible = true;
            } else {
                createQR.setVisibility(View.GONE);
                scanQRC.setVisibility(View.GONE);

                plusButton.setImageResource(R.drawable.plus_large_svgrepo_com);

                Animation hideAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_hide);
                createQR.startAnimation(hideAnimation);
                scanQRC.startAnimation(hideAnimation);

                isAllFabsVisible = false;
            }
        });

        browseUrl.setOnClickListener(v -> openBrowser());
    }

    private void Scancode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("");
        options.setBeepEnabled(false);
        options.setOrientationLocked(true);
        options.setCameraId(0);
        options.setCaptureActivity(CaptureActivityX.class);
        barLauncher.launch(options);
    }

    @SuppressLint("MissingSuperCall")
    public void onBackPressed(){
        Intent intent = new Intent(ScannerActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    @SuppressLint("SetTextI18n")
    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        try {
            if (result.getContents() != null) {
                resultText.setText(result.getContents());
            } else {
                resultText.setText("LOOK LIKE THE QRCODE/BARCODE DOESN'T CONTAIN ANY DATA.");
            }
        } catch (Exception e) {
            resultText.setText("AN ERROR OCCURRED: " + e.getMessage());
            Log.e("SCAN ERROR", "ERROR PROCESSING SCAN RESULT", e);
        }
    });

    private void copyTextToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE); // Get the clipboard manager
        ClipData clip = ClipData.newPlainText("Copied Text", text); // Create a clip with the text


        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            CustomToast customToast = new CustomToast(this);
            customToast.show("CONTENT COPIED", R.drawable.tick_square_svgrepo_com);
        }
    }

    @SuppressLint("SetTextI18n")
    private void setupTextWatcher() {
        resultText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed before text is changed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                recognizeUrlFromText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed after text has been changed
            }
        });
    }

    private void recognizeUrlFromText(String text) {
        extractUrl(text);
        // Optionally handle the case when no URL is found
    }

    private String extractUrl(String text) {
        String urlPattern = "(https?://\\S+|www\\.\\S+)";
        Pattern pattern = Pattern.compile(urlPattern);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            urlTV.setText(matcher.group(0));
            urlsection.setVisibility(View.VISIBLE);// Update urlTV with the found URL
            return matcher.group(0);
        } else {
            urlTV.setText(""); // Clear urlTV if no URL is found
        }

        return null;
    }


    private void openBrowser() {
        try {
            String text = resultText.getText().toString();
            String url = extractUrl(text);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
