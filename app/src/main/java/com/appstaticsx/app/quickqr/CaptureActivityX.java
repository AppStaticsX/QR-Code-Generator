package com.appstaticsx.app.quickqr;

import android.content.Intent;

import com.journeyapps.barcodescanner.CaptureActivity;

public class CaptureActivityX extends CaptureActivity {
    public void onBackPressed(){
        CustomToast customToast = new CustomToast(this);
        customToast.show("Cancelled by User!", R.drawable.camera_slash_svgrepo_com);
        Intent intent = new Intent(CaptureActivityX.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}