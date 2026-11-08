package com.devil;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
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

    static GradientDrawable shape(int[] colors, float radius, int strokeW, int strokeC) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
        g.setCornerRadius(radius);
        if (strokeW > 0) g.setStroke(strokeW, strokeC);
        return g;
    }

    static void build(final Activity a, JSONObject j) {
        try {
            PackageManager pm = a.getPackageManager();
            ApplicationInfo ai = a.getApplicationInfo();

            // Bahire padding: jate 3D shadow kete na jay
            FrameLayout outer = new FrameLayout(a);
            outer.setPadding(dp(a,14), dp(a,14), dp(a,14), dp(a,18));

            // ===== Liquid Glass card (white) =====
            FrameLayout glass = new FrameLayout(a);
            glass.setBackground(shape(
                new int[]{Color.parseColor("#F8FFFFFF"), Color.parseColor("#E9F1F6FF")},
                dp(a,34), dp(a,2), Color.parseColor("#E6FFFFFF")));
            glass.setElevation(dp(a,16));

            // Upore glossy highlight (3D look)
            View gloss = new View(a);
            GradientDrawable gl = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#B3FFFFFF"), Color.parseColor("#00FFFFFF")});
            float r = dp(a,34);
            gl.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
            gloss.setBackground(gl);
            glass.addView(gloss, new FrameLayout.LayoutParams(-1, dp(a,110), Gravity.TOP));

            LinearLayout content = new LinearLayout(a);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setPadding(dp(a,20), dp(a,22), dp(a,20), dp(a,20));
            glass.addView(content, new FrameLayout.LayoutParams(-1, -1));

            // ===== Top row: logo (left) + name (right) =====
            LinearLayout top = new LinearLayout(a);
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(Gravity.CENTER_VERTICAL);

            FrameLayout tile = new FrameLayout(a);
            tile.setBackground(shape(
                new int[]{Color.WHITE, Color.parseColor("#E8EEF7")},
                dp(a,16), dp(a,1), Color.parseColor("#FFFFFFFF")));
            tile.setElevation(dp(a,8));
            tile.setPadding(dp(a,5), dp(a,5), dp(a,5), dp(a,5));
            ImageView logo = new ImageView(a);
            logo.setImageDrawable(pm.getApplicationIcon(ai));
            tile.addView(logo, new FrameLayout.LayoutParams(-1, -1));
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(dp(a,58), dp(a,58));
            top.addView(tile, tlp);

            TextView name = new TextView(a);
            name.setText(pm.getApplicationLabel(ai));
            name.setTextColor(Color.parseColor("#1C1C1E"));
            name.setTextSize(20);
            name.setTypeface(Typeface.DEFAULT_BOLD);
            name.setSingleLine(true);
            name.setEllipsize(android.text.TextUtils.TruncateAt.END);
            name.setPadding(dp(a,14), 0, 0, 0);
            top.addView(name, new LinearLayout.LayoutParams(0, -2, 1f));
            content.addView(top, new LinearLayout.LayoutParams(-1, -2));

            // ===== Description (inner glass card) =====
            ScrollView sv = new ScrollView(a);
            sv.setBackground(shape(
                new int[]{Color.parseColor("#CCFFFFFF"), Color.parseColor("#80F2F6FC")},
                dp(a,20), dp(a,1), Color.parseColor("#FFFFFFFF")));
            sv.setVerticalScrollBarEnabled(false);
            TextView desc = new TextView(a);
            desc.setText(j.optString("description", "New update available"));
            desc.setTextColor(Color.parseColor("#3A3A3C"));
            desc.setTextSize(15);
            desc.setLineSpacing(0, 1.15f);
            desc.setPadding(dp(a,16), dp(a,14), dp(a,16), dp(a,14));
            sv.addView(desc);
            LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-1, 0, 1f);
            slp.topMargin = dp(a,18);
            slp.bottomMargin = dp(a,18);
            content.addView(sv, slp);

            // ===== Update Now button (iOS blue, glossy 3D pill) =====
            final String link = j.optString("link", "");
            TextView b = new TextView(a);
            b.setText(j.optString("button", "Update Now"));
            b.setTextColor(Color.WHITE);
            b.setTextSize(17);
            b.setTypeface(Typeface.DEFAULT_BOLD);
            b.setGravity(Gravity.CENTER);
            GradientDrawable base = shape(
                new int[]{Color.parseColor("#5AB4FF"), Color.parseColor("#0A7AFF")},
                dp(a,28), dp(a,1), Color.parseColor("#80FFFFFF"));
            GradientDrawable shine = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#66FFFFFF"), Color.parseColor("#00FFFFFF")});
            shine.setCornerRadius(dp(a,24));
            LayerDrawable ld = new LayerDrawable(new Drawable[]{base, shine});
            ld.setLayerInset(1, dp(a,4), dp(a,3), dp(a,4), dp(a,25));
            b.setBackground(ld);
            b.setElevation(dp(a,8));
            b.setClickable(true);
            b.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    a.startActivity(i);
                } catch (Exception e) {}
            }});
            content.addView(b, new LinearLayout.LayoutParams(-1, dp(a,54)));

            outer.addView(glass, new FrameLayout.LayoutParams(-1, -1));

            Dialog d = new Dialog(a);
            d.requestWindowFeature(Window.FEATURE_NO_TITLE);
            d.setCancelable(false);            // back button e bondho hobe na
            d.setCanceledOnTouchOutside(false); // bairer touch e bondho hobe na
            d.setContentView(outer);
            Window w = d.getWindow();
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            w.setDimAmount(0.30f);
            // Pichoner blur (Android 12+ e kaj korbe, onno version e skip hobe)
            try {
                WindowManager.LayoutParams lp = w.getAttributes();
                WindowManager.LayoutParams.class.getMethod("setBlurBehindRadius", int.class)
                    .invoke(lp, dp(a,20));
                w.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
                w.setAttributes(lp);
            } catch (Throwable t) {}
            DisplayMetrics m = a.getResources().getDisplayMetrics();
            int width = (int) (m.widthPixels * 0.90f);
            w.setLayout(width, width * 4 / 3);   // 3:4 ratio
            d.show();
        } catch (Exception e) {}
    }
}
