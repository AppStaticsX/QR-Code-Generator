package com.appstaticsx.app.quickqr;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast {

    private final Context context;

    public CustomToast(Context context) {
        this.context = context;
    }

    public void show(String message, int imageResId) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.toast_background,
                ((Activity) context).findViewById(R.id.toast_layout_root));

        ImageView image = layout.findViewById(R.id.image);
        image.setImageResource(imageResId);

        TextView text = layout.findViewById(R.id.text);
        text.setText(message);
        text.setGravity(Gravity.CENTER_VERTICAL);
        text.setTextColor(context.getResources().getColor(R.color.textColor, null));

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}

