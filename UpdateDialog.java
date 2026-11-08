package com.devil;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.*;
import org.json.JSONObject;
import java.io.*;
import java.net.*;

public class UpdateDialog {
    // Apnar json.txt ar direct link ekhane boshan
    static final String JSON_URL = "https://yoursite.com/json.txt";

    public static void show(final Activity a) {
        new Thread(new Runnable() { public void run() {
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(JSON_URL).openConnection();
                c.setConnectTimeout(8000);
                c.setReadTimeout(8000);
                BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String l;
                while ((l = r.readLine()) != null) sb.append(l);
                r.close();
                final JSONObject j = new JSONObject(sb.toString());
                if (!j.optBoolean("show", true)) return;
                a.runOnUiThread(new Runnable() { public void run() {
                    if (!a.isFinishing()) build(a, j);
                }});
            } catch (Exception e) {}
        }}).start();
    }

    static int dp(Context c, int v) {
        return (int) (v * c.getResources().getDisplayMetrics().density);
    }

    static void build(final Activity a, JSONObject j) {
        try {
            PackageManager pm = a.getPackageManager();
            ApplicationInfo ai = a.getApplicationInfo();

            LinearLayout root = new LinearLayout(a);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setPadding(dp(a,20), dp(a,20), dp(a,20), dp(a,20));
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(Color.parseColor("#1B1B1F"));
            bg.setCornerRadius(dp(a,24));
            root.setBackground(bg);

            // Top row: logo left, name right
            LinearLayout top = new LinearLayout(a);
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(Gravity.CENTER_VERTICAL);
            ImageView logo = new ImageView(a);
            logo.setImageDrawable(pm.getApplicationIcon(ai));
            top.addView(logo, new LinearLayout.LayoutParams(dp(a,52), dp(a,52)));
            TextView name = new TextView(a);
            name.setText(pm.getApplicationLabel(ai));
            name.setTextColor(Color.WHITE);
            name.setTextSize(20);
            name.setTypeface(Typeface.DEFAULT_BOLD);
            name.setPadding(dp(a,14), 0, 0, 0);
            top.addView(name);
            root.addView(top);

            // Description
            ScrollView sv = new ScrollView(a);
            TextView desc = new TextView(a);
            desc.setText(j.optString("description", "New update available"));
            desc.setTextColor(Color.parseColor("#CCCCCC"));
            desc.setTextSize(15);
            desc.setPadding(0, dp(a,18), 0, dp(a,10));
            sv.addView(desc);
            root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1f));

            // Update Now button
            final String link = j.optString("link", "");
            Button b = new Button(a);
            b.setText(j.optString("button", "Update Now"));
            b.setTextColor(Color.WHITE);
            b.setAllCaps(false);
            GradientDrawable bb = new GradientDrawable();
            bb.setColor(Color.parseColor("#E53935"));
            bb.setCornerRadius(dp(a,14));
            b.setBackground(bb);
            b.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
                try { a.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link))); } catch (Exception e) {}
            }});
            root.addView(b, new LinearLayout.LayoutParams(-1, dp(a,50)));

            Dialog d = new Dialog(a);
            d.requestWindowFeature(Window.FEATURE_NO_TITLE);
            d.setCancelable(false);
            d.setCanceledOnTouchOutside(false);
            d.setContentView(root);
            Window w = d.getWindow();
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            DisplayMetrics m = a.getResources().getDisplayMetrics();
            int width = (int) (m.widthPixels * 0.85f);
            w.setLayout(width, width * 4 / 3); // 3:4 ratio
            d.show();
        } catch (Exception e) {}
    }
}
