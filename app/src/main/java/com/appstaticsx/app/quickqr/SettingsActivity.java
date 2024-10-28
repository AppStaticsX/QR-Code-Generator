package com.appstaticsx.app.quickqr;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.mikhaellopez.circularimageview.CircularImageView;

public class SettingsActivity extends BaseActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar));

        // Load the saved theme first
        loadTheme();
        setContentView(R.layout.activity_settings);

        CircularImageView buttonLight = findViewById(R.id.button_green);
        CircularImageView buttonDark = findViewById(R.id.button_gold);

        buttonLight.setOnClickListener(v -> setLightTheme());

        buttonDark.setOnClickListener(v -> setDarkTheme());
    }

    private void loadTheme() {
        int theme = sharedPreferences.getInt("theme", AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(theme);
    }

    private void setLightTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        saveTheme(AppCompatDelegate.MODE_NIGHT_NO);
        overridePendingTransition(R.anim.flip_in, R.anim.flip_out);
        startActivity(new Intent(this, MainActivity.class)); // Start light theme activity
        finish(); // Close settings activity
    }

    private void setDarkTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        saveTheme(AppCompatDelegate.MODE_NIGHT_YES);
        overridePendingTransition(R.anim.flip_in, R.anim.flip_out);
        startActivity(new Intent(this, MainActivity.class)); // Start light theme activity
        finish();
    }


    private void saveTheme(int theme) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("theme", theme);
        editor.apply();
    }

    @SuppressLint("MissingSuperCall")
    public void onBackPressed() {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        startActivity(intent);
        finish();
    }
}
