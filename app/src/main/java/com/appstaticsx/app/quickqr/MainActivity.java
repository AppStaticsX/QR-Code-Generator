package com.appstaticsx.app.quickqr;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class MainActivity extends BaseActivity {

    private static final int QR_SIZE = 2048;
    private static final int MAX_CHAR_COUNT = 2950;

    private TextInputEditText inputData;
    private TextInputEditText qrName;
    private TextInputLayout textDatalayout, qrcodeName;
    private ImageView qrImage;
    private TextView scanAnim;
    private MaterialButton generateBtn;
    private LinearLayout saveQr;
    private LinearLayout shareQr;
    private TextView letterCount;
    private ImageButton uppercase, lowercase, clearAllText;
    private FloatingActionButton floatActionBtn;
    private MaterialCardView qrcardview;
    private LinearLayout viewQR, settings;

    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.quantum_orange400));


        initializeViews();
        permissionManager = new PermissionManager(this);
        permissionManager.checkStoragePermission();

        generateBtn.setOnClickListener(view -> generateQrCode());
        saveQr.setOnClickListener(view -> saveImageToStorage());
        shareQr.setOnClickListener(view -> shareQrCode());

        inputData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                letterCount.setText(charSequence.length()+"/2950");
                if (charSequence.length() > MAX_CHAR_COUNT) {
                    inputData.setText(charSequence.subSequence(0, MAX_CHAR_COUNT));
                    inputData.setSelection(MAX_CHAR_COUNT);
                    letterCount.setTextColor(getResources().getColor(R.color.red, null));

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialog);
                    builder.setTitle("Data Limit Exceeded!")
                            .setMessage("We are Sorry!, You have reached the maximum data entry limit.")
                            .setPositiveButton("GOT IT", null);

                    AlertDialog dialog = builder.create();
                    dialog.setOnShowListener(dialogInterface -> {
                        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

                        // Set the colors for the buttons
                        positiveButton.setTextColor(Color.GREEN);

                        positiveButton.setOnClickListener(v -> dialog.dismiss());
                    });

                    dialog.show();

                } else {
                    letterCount.setTextColor(getResources().getColor(R.color.quantum_orange400, null));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        qrName.setOnFocusChangeListener((v, hasFocus) -> qrcodeName.setBoxStrokeColor(getResources().getColor(R.color.quantum_orange400, null)));

        inputData.setOnFocusChangeListener((v, hasFocus) -> textDatalayout.setBoxStrokeColor(getResources().getColor(R.color.quantum_orange400, null)));

        uppercase.setOnClickListener(view -> {
            Editable editable = inputData.getText();

            int start  = inputData.getSelectionStart();
            int end = inputData.getSelectionEnd();

            if (start >= 0 && end > start) {
                assert editable != null;
                String selectedText = editable.subSequence(start, end).toString();
                String uppercaseText = selectedText.toUpperCase();

                editable.replace(start, end, uppercaseText);

                Selection.setSelection(editable, start + uppercaseText.length());
            }
        });

        lowercase.setOnClickListener(view -> {
            Editable editable = inputData.getText();

            int start  = inputData.getSelectionStart();
            int end = inputData.getSelectionEnd();

            if (start >= 0 && end > start) {
                assert editable != null;
                String selectedText = editable.subSequence(start, end).toString();
                String lowercaseText = selectedText.toLowerCase();

                editable.replace(start, end, lowercaseText);

                Selection.setSelection(editable, start + lowercaseText.length());
            }
        });

        clearAllText.setOnClickListener(v -> {
            // Clear the text in EditText
            inputData.setText("");
        });

        floatActionBtn.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        viewQR.setOnClickListener(view -> {

        });

        settings.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            overridePendingTransition(R.anim.flip_in, R.anim.flip_out);
            startActivity(intent);
            finish();
        });
    }

    private void initializeViews() {
        inputData = findViewById(R.id.textData);
        qrName = findViewById(R.id.qrName);
        qrImage = findViewById(R.id.qrImage);
        generateBtn = findViewById(R.id.generateBtn);
        scanAnim = findViewById(R.id.qr_code_anim1);
        saveQr = findViewById(R.id.save_img);
        shareQr = findViewById(R.id.share_qr);
        uppercase = findViewById(R.id.uppercase);
        lowercase = findViewById(R.id.lowercase);
        letterCount = findViewById(R.id.letterCount);
        clearAllText = findViewById(R.id.clearAllText);
        floatActionBtn = findViewById(R.id.floatActionBtn);
        qrcardview = findViewById(R.id.qrCardView);
        viewQR = findViewById(R.id.view_img);
        textDatalayout = findViewById(R.id.userText);
        qrcodeName = findViewById(R.id.qrCodename);
        settings = findViewById(R.id.settingApp);
    }

    private void generateQrCode() {
        String text = Objects.requireNonNull(inputData.getText()).toString().trim();
        String name = Objects.requireNonNull(qrName.getText()).toString().trim();

        if (name.isEmpty()) {
            scanAnim.setVisibility(View.VISIBLE);
            CustomToast customToast = new CustomToast(this);
            customToast.show( "PLEASE ENTER A NAME", R.drawable.edit_2_svgrepo_com);
            return; // Exit the method if name is empty
        }

        if (!text.isEmpty()) {
            try {
                scanAnim.setVisibility(View.GONE);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
                Bitmap coloredBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(coloredBitmap);
                Paint paint = new Paint();

                // Set the color you want for the QR code
                paint.setColor(Color.BLUE); // Change this to your desired color
                canvas.drawRect(0, 0, coloredBitmap.getWidth(), coloredBitmap.getHeight(), paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                canvas.drawBitmap(bitmap, 0, 0, paint);

                qrImage.setVisibility(View.VISIBLE);
                qrcardview.setVisibility(View.VISIBLE);
                qrImage.setImageBitmap(bitmap);
            } catch (WriterException e) {
                scanAnim.setVisibility(View.VISIBLE);
                qrcardview.setVisibility(View.GONE);
                qrImage.setVisibility(View.GONE);
                CustomToast customToast = new CustomToast(this);
                customToast.show("ERROR GENERATING QR", R.drawable.danger_svgrepo_com);
            }
        } else {
            CustomToast customToast = new CustomToast(this);
            customToast.show("PLEASE ENTER TEXT", R.drawable.edit_2_svgrepo_com);
        }
    }


    private void saveImageToStorage() {
        if (qrImage.getDrawable() == null) {
            scanAnim.setVisibility(View.VISIBLE);
            CustomToast customToast = new CustomToast(this);
            customToast.show("NO QRCODE TO SAVE!", R.drawable.danger_svgrepo_com);
            return;
        }

        Bitmap bitmap = ((BitmapDrawable) qrImage.getDrawable()).getBitmap();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveImageToMediaStore(bitmap);
            } else {
                saveImageLegacy(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            CustomToast customToast = new CustomToast(this);
            customToast.show("FAILED TO SAVE!", R.drawable.close_square_svgrepo_com);
        }
    }

    private void saveImageToMediaStore(Bitmap bitmap) throws Exception {
        String name = Objects.requireNonNull(qrName.getText()).toString();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "QuickQR_" + name);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QRImages");

        Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        if (imageUri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(imageUri)) {
                assert outputStream != null;
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                CustomToast customToast = new CustomToast(this);
                customToast.show("SAVED SUCCESSFULLY!", R.drawable.tick_square_svgrepo_com);
            }
        }
    }

    private void saveImageLegacy(Bitmap bitmap) {
        String name = Objects.requireNonNull(qrName.getText()).toString().trim();
        String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/QRImages").getPath();

        // Create directory if it doesn't exist
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, "QuickQR_" + name + ".jpg");
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            CustomToast customToast = new CustomToast(this);
            customToast.show("SAVED SUCCESSFULLY!", R.drawable.tick_square_svgrepo_com);
        } catch (IOException e) {
            e.printStackTrace();
            CustomToast customToast = new CustomToast(this);
            customToast.show("FAILED TO SAVE!", R.drawable.close_square_svgrepo_com);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionManager.onRequestPermissionsResult(requestCode, grantResults, this::saveImageToStorage);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        permissionManager.onActivityResult(requestCode);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void shareQrCode() {
        if (qrImage.getDrawable() == null) {
            CustomToast customToast = new CustomToast(this);
            customToast.show("NO QRCODE TO SHARE!", R.drawable.danger_svgrepo_com);
            return;
        }

        Bitmap bitmap = ((BitmapDrawable) qrImage.getDrawable()).getBitmap();
        try {
            // Save the bitmap to a file
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "QRCode_" + Objects.requireNonNull(qrName.getText()) + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            // Create share intent
            Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/jpeg");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share QR Code"));

        } catch (IOException e) {
            e.printStackTrace();
            CustomToast customToast = new CustomToast(this);
            customToast.show("FAILED TO SHARE!", R.drawable.close_square_svgrepo_com);
        }
    }
}