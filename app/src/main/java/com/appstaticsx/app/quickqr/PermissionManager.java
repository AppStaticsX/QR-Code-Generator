package com.appstaticsx.app.quickqr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionManager {
    private static final int REQUEST_MANAGE_STORAGE_PERMISSION = 101;
    private static final int REQUEST_STORAGE_PERMISSION = 100;

    private final AppCompatActivity activity;

    public PermissionManager(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                requestManageExternalStoragePermission();
            }
        } else {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void requestManageExternalStoragePermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.CustomAlertDialog);
        builder.setTitle("Permission Required")
                .setMessage("This app requires access to manage storage to function properly. Please grant the permission.")
                .setPositiveButton("Allow", null)
                .setNegativeButton("Deny", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            // Set the colors for the buttons
            positiveButton.setTextColor(Color.BLACK);
            negativeButton.setTextColor(Color.RED);

            positiveButton.setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, REQUEST_MANAGE_STORAGE_PERMISSION);
                dialog.dismiss();
            });

            negativeButton.setOnClickListener(v -> {
                showPermissionDeniedDialog();
                dialog.dismiss();
            });
        });

        dialog.show();
    }


    private void showPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.CustomAlertDialog);
        builder.setTitle("Permission Denied")
                .setMessage("You must allow storage permission to continue using the app.")
                .setPositiveButton("Retry", null)
                .setNegativeButton("Exit", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            positiveButton.setTextColor(Color.BLACK);
            negativeButton.setTextColor(Color.RED);

            positiveButton.setOnClickListener(v -> {
                checkStoragePermission();
                dialog.dismiss();
            });

            negativeButton.setOnClickListener(v -> activity.finish());
        });

        dialog.show();
    }


    public void onRequestPermissionsResult(int requestCode, int[] grantResults, Runnable onSuccess) {
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onSuccess.run();
            } else {
                showPermissionDeniedDialog();
            }
        }
    }

    public void onActivityResult(int requestCode) {
        if (requestCode == REQUEST_MANAGE_STORAGE_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                // You can define what happens after permission is granted
            } else {
                showPermissionDeniedDialog();
            }
        }
    }
}