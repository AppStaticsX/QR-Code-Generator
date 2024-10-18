package com.appstaticsx.app.quickqr;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.appstaticsx.app.quickqr.MainActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class ScannerActivity extends AppCompatActivity {

    private TextView resultText;
    private AppCompatButton copyContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        resultText = findViewById(R.id.scannedResultTV);
        copyContent = findViewById(R.id.copyContentBtn);

        Scancode();

        copyContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyTextToClipboard(resultText.getText().toString());
            }
        });
    }

    private void Scancode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("");
        options.setBeepEnabled(false);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureActivityX.class);
        barLauncher.launch(options);
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
            showCustomToast("CONTENT COPIED", R.drawable.baseline_done_24);
        }
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


}
