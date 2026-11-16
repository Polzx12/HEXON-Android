package com.hexon.security;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(8, 13, 18);
    private final int PANEL = Color.rgb(17, 27, 35);
    private final int GREEN = Color.rgb(53, 229, 139);
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private EditText targetInput, portsInput;
    private CheckBox permissionCheck;
    private TextView logView, statusView;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        buildUi();
        append("HEXON 0.1.0 • ARM64 starter");
        append("Safe mode aktif: pemeriksaan koneksi terbatas, tanpa exploit otomatis.");
        append("Engine embedded: belum dipaketkan. Lihat docs/EMBEDDED_ENGINES.md.");
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(18), dp(22), dp(18), dp(24));
        page.setBackgroundColor(BG);
        scroll.setBackgroundColor(BG);
        scroll.addView(page);

        TextView brand = new TextView(this);
        brand.setText("⬡  HEXON");
        brand.setTextColor(GREEN);
        brand.setTextSize(30);
        brand.setTypeface(Typeface.DEFAULT_BOLD);
        page.addView(brand);

        TextView subtitle = new TextView(this);
        subtitle.setText("ANDROID CYBERSECURITY TOOLKIT  •  ARM64");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(11);
        page.addView(subtitle);

        statusView = new TextView(this);
        statusView.setText("● SAFE MODE  |  Android 12+");
        statusView.setTextColor(GREEN);
        statusView.setPadding(0, dp(18), 0, dp(14));
        page.addView(statusView);

        TextView section = heading("TARGET SCOPE");
        page.addView(section);
        targetInput = edit("Domain / IP / localhost", "example.com");
        page.addView(targetInput);
        permissionCheck = new CheckBox(this);
        permissionCheck.setText("Saya pemilik target atau memiliki izin tertulis untuk mengujinya");
        permissionCheck.setTextColor(Color.WHITE);
        permissionCheck.setButtonTintList(android.content.res.ColorStateList.valueOf(GREEN));
        page.addView(permissionCheck);

        TextView pHead = heading("PORT CHECK • MAX 10 PORTS");
        page.addView(pHead);
        portsInput = edit("Port dipisahkan koma", "80,443,8080");
        page.addView(portsInput);

        Button portButton = actionButton("CHECK TCP PORTS");
        page.addView(portButton);
        portButton.setOnClickListener(v -> runPortCheck());

        Button httpButton = actionButton("HTTP SECURITY SNAPSHOT");
        page.addView(httpButton);
        httpButton.setOnClickListener(v -> runHttpCheck());

        TextView toolHead = heading("EMBEDDED TOOL ENGINES");
        page.addView(toolHead);
        page.addView(infoCard("NUCLEI", "Template-based scanner • adapter belum dipasang"));
        page.addView(infoCard("HTTPX", "HTTP probe • adapter belum dipasang"));
        page.addView(infoCard("SUBFINDER", "Subdomain discovery • adapter belum dipasang"));
        page.addView(infoCard("KATANA", "Web crawler • adapter belum dipasang"));
        page.addView(infoCard("FFUF", "Fuzzer • dinonaktifkan pada starter build"));

        TextView logHead = heading("ACTIVITY LOG");
        page.addView(logHead);
        logView = new TextView(this);
        logView.setTextColor(Color.rgb(180, 205, 194));
        logView.setTextSize(12);
        logView.setTypeface(Typeface.MONOSPACE);
        logView.setPadding(dp(12), dp(12), dp(12), dp(12));
        logView.setBackgroundColor(PANEL);
        page.addView(logView);

        TextView foot = new TextView(this);
        foot.setText("\nHEXON • Use only on systems you own or are authorized to test.");
        foot.setTextColor(Color.GRAY);
        foot.setTextSize(10);
        page.addView(foot);
        setContentView(scroll);
    }

    private TextView heading(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(GREEN);
        t.setTextSize(13);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(0, dp(18), 0, dp(8));
        return t;
    }

    private EditText edit(String hint, String value) {
        EditText e = new EditText(this);
        e.setSingleLine(true);
        e.setHint(hint);
        e.setText(value);
        e.setTextColor(Color.WHITE);
        e.setHintTextColor(Color.GRAY);
        e.setPadding(dp(12), dp(10), dp(12), dp(10));
        e.setBackgroundTintList(android.content.res.ColorStateList.valueOf(GREEN));
        return e;
    }

    private Button actionButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(Color.BLACK);
        b.setTextSize(12);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(GREEN));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(10);
        b.setLayoutParams(lp);
        return b;
    }

    private View infoCard(String title, String desc) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(10), dp(12), dp(10));
        box.setBackgroundColor(PANEL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(5);
        box.setLayoutParams(lp);
        TextView a = new TextView(this); a.setText(title); a.setTextColor(GREEN); a.setTypeface(null, Typeface.BOLD);
        TextView d = new TextView(this); d.setText(desc); d.setTextColor(Color.LTGRAY); d.setTextSize(11);
        box.addView(a); box.addView(d);
        return box;
    }

    private void runPortCheck() {
        final String host = targetInput.getText().toString().trim();
        final String portsText = portsInput.getText().toString().trim();
        if (!authorized(host)) return;
        final ArrayList<Integer> ports = new ArrayList<>();
        try {
            for (String s : portsText.split(",")) {
                if (s.trim().isEmpty()) continue;
                int p = Integer.parseInt(s.trim());
                if (p < 1 || p > 65535) throw new NumberFormatException();
                if (!ports.contains(p)) ports.add(p);
            }
            if (ports.isEmpty() || ports.size() > 10) throw new NumberFormatException();
        } catch (Exception ex) {
            toast("Masukkan 1–10 port valid, contoh: 80,443,8080");
            return;
        }
        append("TCP check dimulai: " + host + " • " + ports.size() + " port");
        worker.execute(() -> {
            for (int port : ports) {
                boolean open = false;
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(host, port), 1200);
                    open = true;
                } catch (Exception ignored) {}
                final String line = host + ":" + port + (open ? " OPEN" : " closed/filtered");
                runOnUiThread(() -> append(line));
            }
            runOnUiThread(() -> append("TCP check selesai."));
        });
    }

    private void runHttpCheck() {
        final String host = targetInput.getText().toString().trim();
        if (!authorized(host)) return;
        worker.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("https://" + host + "/");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(4000);
                conn.setInstanceFollowRedirects(false);
                conn.setRequestProperty("User-Agent", "HEXON-Authorized-Audit/0.1");
                int code = conn.getResponseCode();
                Map<String, List<String>> headers = conn.getHeaderFields();
                runOnUiThread(() -> {
                    append("HTTPS " + host + " → HTTP " + code);
                    showHeader(headers, "strict-transport-security");
                    showHeader(headers, "content-security-policy");
                    showHeader(headers, "x-content-type-options");
                    showHeader(headers, "x-frame-options");
                    append("HTTP snapshot selesai. Ini pemeriksaan header dasar, bukan audit penuh.");
                });
            } catch (Exception ex) {
                runOnUiThread(() -> append("HTTP error: " + ex.getClass().getSimpleName() + " — " + ex.getMessage()));
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private void showHeader(Map<String, List<String>> headers, String key) {
        List<String> value = headers.get(key);
        append(key + ": " + (value == null ? "not present" : value.toString()));
    }

    private boolean authorized(String host) {
        if (host.isEmpty() || host.contains("/") || host.contains(" ") || host.contains("@")) {
            toast("Masukkan hostname atau IP saja, tanpa URL/path.");
            return false;
        }
        if (!permissionCheck.isChecked()) {
            toast("Centang konfirmasi kepemilikan/izin target terlebih dahulu.");
            return false;
        }
        return true;
    }

    private void append(String line) {
        if (logView == null) return;
        logView.append("› " + line + "\n");
    }

    private void toast(String text) { Toast.makeText(this, text, Toast.LENGTH_LONG).show(); }
    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    @Override protected void onDestroy() {
        worker.shutdownNow();
        super.onDestroy();
    }
}
