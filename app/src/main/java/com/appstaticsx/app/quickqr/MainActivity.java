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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final int QR_SIZE = 2048;
    private static final int MAX_CHAR_COUNT = 2950;

    private TextInputEditText inputData;
    private TextInputEditText qrName;
    private ImageView qrImage;
    private TextView scanAnim;
    private MaterialButton generateBtn;
    private LinearLayout saveQr;
    private LinearLayout shareQr;
    private TextView letterCount;
    private ImageButton uppercase, lowercase, clearAllText;
    private FloatingActionButton floatActionBtn;

    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        permissionManager = new PermissionManager(this);
        permissionManager.checkStoragePermission();

        generateBtn.setOnClickListener(view -> generateQrCode());
        saveQr.setOnClickListener(view -> saveImageToStorage());
        shareQr.setOnClickListener(view -> shareQrCode());

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

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
                    letterCount.setTextColor(Color.parseColor("#FF0000"));
                    showCustomToast("Text Limit Reached!", R.drawable.baseline_warning_amber_red__24);
                } else {
                    letterCount.setTextColor(Color.parseColor("#000000"));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        uppercase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable editable = inputData.getText();

                int start  = inputData.getSelectionStart();
                int end = inputData.getSelectionEnd();

                if (start >= 0 && end > start) {
                    String selectedText = editable.subSequence(start, end).toString();
                    String uppercaseText = selectedText.toUpperCase();

                    editable.replace(start, end, uppercaseText);

                    Selection.setSelection(editable, start + uppercaseText.length());
                }
            }
        });

        lowercase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable editable = inputData.getText();

                int start  = inputData.getSelectionStart();
                int end = inputData.getSelectionEnd();

                if (start >= 0 && end > start) {
                    String selectedText = editable.subSequence(start, end).toString();
                    String lowercaseText = selectedText.toLowerCase();

                    editable.replace(start, end, lowercaseText);

                    Selection.setSelection(editable, start + lowercaseText.length());
                }
            }
        });

        clearAllText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear the text in EditText
                inputData.setText("");
            }
        });

        floatActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
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
    }

    private void generateQrCode() {
        String text = inputData.getText().toString().trim();
        String name = qrName.getText().toString().trim();

        if (name.isEmpty()) {
            scanAnim.setVisibility(View.VISIBLE);
            showCustomToast( "Please Enter a Name", R.drawable.baseline_drive_file_outline);
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
                qrImage.setImageBitmap(bitmap);
            } catch (WriterException e) {
                scanAnim.setVisibility(View.VISIBLE);
                qrImage.setVisibility(View.GONE);
                showCustomToast("Error generating QR Code", R.drawable.baseline_warning_amber_red__24);
            }
        } else {
            showCustomToast("Please Enter Text", R.drawable.baseline_drive_file_outline);
        }
    }


    private void saveImageToStorage() {
        if (qrImage.getDrawable() == null) {
            scanAnim.setVisibility(View.VISIBLE);
            showCustomToast("No QR Code to Save!", R.drawable.baseline_warning_amber_24);
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
            showCustomToast("Fail to save image!", R.drawable.baseline_close_24);
        }
    }

    private void saveImageToMediaStore(Bitmap bitmap) throws Exception {
        String name = qrName.getText().toString();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "QuickQR_" + name + ".jpg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QRImages");

        Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        if (imageUri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(imageUri)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                showCustomToast("Saved Successfully!", R.drawable.baseline_done_24);
            }
        }
    }

    private void saveImageLegacy(Bitmap bitmap) {
        String name = qrName.getText().toString().trim();
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
            showCustomToast("QR Code saved successfully!", R.drawable.baseline_done_24);
        } catch (IOException e) {
            e.printStackTrace();
            showCustomToast("Fail to save image!", R.drawable.baseline_close_24);
        }
    }

    private void checkStoragePermission() {
        permissionManager.checkStoragePermission();
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

    private void showCustomToast(String message, int imageResId) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_background,
                (ViewGroup) findViewById(R.id.toast_layout_root));

        ImageView image = layout.findViewById(R.id.image);
        image.setImageResource(imageResId);

        TextView text = layout.findViewById(R.id.text);
        text.setGravity(Gravity.CENTER_VERTICAL);
        text.setText(message);
        text.setTextColor(getResources().getColor(R.color.black));

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    private void shareQrCode() {
        if (qrImage.getDrawable() == null) {
            showCustomToast("No QR Code to Share!", R.drawable.baseline_warning_amber_24);
            return;
        }

        Bitmap bitmap = ((BitmapDrawable) qrImage.getDrawable()).getBitmap();
        try {
            // Save the bitmap to a file
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "QRCode_" + qrName.getText().toString() + ".jpg");
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
            showCustomToast("Failed to share QR Code!", R.drawable.baseline_close_24);
        }
    }
}