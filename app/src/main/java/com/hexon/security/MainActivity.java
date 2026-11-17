package com.hexon.security;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {

    private final int BG = Color.rgb(7, 11, 15);
    private final int PANEL = Color.rgb(16, 25, 33);
    private final int PANEL2 = Color.rgb(22, 34, 43);
    private final int GREEN = Color.rgb(45, 235, 145);
    private final int WHITE = Color.rgb(235, 245, 240);
    private final int MUTED = Color.rgb(145, 165, 158);
    private final int BORDER = Color.rgb(37, 65, 57);

    private LinearLayout root, page, nav;
    private TextView logView;
    private EditText targetInput, portsInput;
    private CheckBox permissionCheck;

    private SharedPreferences prefs;
    private final ExecutorService worker =
            Executors.newSingleThreadExecutor();

    private volatile boolean scanRunning = false;
    private volatile boolean stopRequested = false;
    private volatile HttpURLConnection activeConnection;

    private String currentPage = "Dashboard";

    private final String[][] modules = {
        {"Reconnaissance", "Pemetaan domain dan informasi target"},
        {"Network Scanner", "Pemeriksaan port dan konektivitas"},
        {"Web Security", "Pemeriksaan konfigurasi keamanan web"},
        {"Attack Lab", "Simulasi pengujian di lingkungan lab"},
        {"API Security", "Pemeriksaan endpoint dan konfigurasi API"},
        {"Authentication Audit", "Audit mekanisme autentikasi"},
        {"Firewall & TLS", "Pemeriksaan TLS dan konfigurasi firewall"},
        {"Secret Scanner", "Pencarian secret pada source milik sendiri"},
        {"Load Testing", "Uji beban dengan batas aman"},
        {"Scan History", "Riwayat pemeriksaan sebelumnya"},
        {"Reports", "Ringkasan dan laporan pemeriksaan"},
        {"Pengaturan", "Konfigurasi aplikasi HEXON"}
    };

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("hexon_data", MODE_PRIVATE);
        buildShell();
        showPage("Dashboard");
        append("HEXON 0.2.0 • Dashboard baru dibuka");
    }

    private void buildShell() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(18), dp(13), dp(18), dp(12));

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.VERTICAL);

        TextView logo = text("⬡ HEXON", 27, GREEN, true);
        brand.addView(logo);

        TextView tagline = text(
                "SECURITY • SCAN • PROTECT", 10, MUTED, true);
        brand.addView(tagline);

        header.addView(brand,
                new LinearLayout.LayoutParams(0, -2, 1));

        TextView safe = text("● SAFE MODE", 11, GREEN, true);
        safe.setPadding(dp(10), dp(8), dp(10), dp(8));
        safe.setBackground(shape(PANEL2, BORDER, 30));
        header.addView(safe);

        root.addView(header);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(17), dp(8), dp(17), dp(22));

        scroll.addView(page);
        root.addView(scroll,
                new LinearLayout.LayoutParams(-1, 0, 1));

        nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(5), dp(7), dp(5), dp(7));
        nav.setBackgroundColor(PANEL);

        root.addView(nav);
        setContentView(root);
        buildNav();
    }

    private void buildNav() {
        nav.removeAllViews();

        addNav("⌂", "Dashboard");
        addNav("▦", "Tools");
        addNav("⌕", "Scan");
        addNav("≡", "Log");
    }

    private void addNav(String icon, String label) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(7), dp(7), dp(7), dp(7));

        TextView symbol = text(icon, 22,
                currentPage.equals(label) ? GREEN : MUTED, true);
        TextView caption = text(label, 10,
                currentPage.equals(label) ? GREEN : MUTED, true);

        item.addView(symbol);
        item.addView(caption);

        if (currentPage.equals(label)) {
            item.setBackground(shape(PANEL2, BORDER, 14));
        }

        item.setOnClickListener(v -> showPage(label));

        nav.addView(item,
                new LinearLayout.LayoutParams(0, -2, 1));
    }

    private void showPage(String name) {
        currentPage = name;
        page.removeAllViews();
        logView = null;
        buildNav();

        switch (name) {
            case "Dashboard":
                showDashboard();
                break;
            case "Tools":
                showTools();
                break;
            case "Scan":
            case "Network Scanner":
                currentPage = name;
                buildNav();
                showScanner();
                break;
            case "Log":
            case "Scan History":
                showLogs();
                break;
            case "Reports":
                showReports();
                break;
            case "Pengaturan":
                showSettings();
                break;
            default:
                showModule(name);
                break;
        }
    }

    private void showDashboard() {
        heading("PUSAT KENDALI");

        LinearLayout hero = card();
        hero.addView(text("SELAMAT DATANG DI HEXON", 11,
                GREEN, true));
        hero.addView(text("Security • Scan • Protect", 23,
                WHITE, true));
        hero.addView(text(
                "Toolkit keamanan Android ARM64 untuk pemeriksaan sistem yang kamu miliki atau diizinkan.",
                13, MUTED, false));
        page.addView(hero);

        heading("RINGKASAN");
        LinearLayout metrics = new LinearLayout(this);

        metric(metrics, "02", "Pemeriksaan dasar");
        metric(metrics, "ARM64", "Arsitektur");

        page.addView(metrics);

        heading("AKSES CEPAT");

        LinearLayout actions = new LinearLayout(this);
        actions.addView(actionCard(
                "NETWORK SCANNER",
                "Periksa port TCP",
                "Buka pemeriksaan jaringan",
                () -> showPage("Scan")));

        actions.addView(actionCard(
                "KOLEKSI TOOLS",
                "Jelajahi modul",
                "Lihat seluruh menu",
                () -> showPage("Tools")));

        page.addView(actions);

        heading("MODUL UTAMA");
        addMenuCard("Reconnaissance",
                "Pemetaan informasi target",
                () -> showPage("Reconnaissance"));
        addMenuCard("Web Security",
                "Pemeriksaan keamanan web",
                () -> showPage("Web Security"));
        addMenuCard("API Security",
                "Pemeriksaan keamanan API",
                () -> showPage("API Security"));
        addMenuCard("Firewall & TLS",
                "Pemeriksaan konfigurasi TLS",
                () -> showPage("Firewall & TLS"));

        addNote("STATUS ENGINE",
                "Nuclei, HTTPX, Subfinder, Naabu, Katana, FFUF dan engine lainnya belum terintegrasi.");
    }

    private void showTools() {
        heading("KOLEKSI TOOLS");
        addNote("HEXON MODULES",
                "Pilih modul untuk melihat fungsi dan status implementasinya.");

        for (String[] module : modules) {
            final String name = module[0];
            addMenuCard(name, module[1],
                    () -> showPage(name));
        }
    }

    private void showScanner() {
        heading("NETWORK SCANNER");

        addNote("TARGET SCOPE",
                "Masukkan hostname atau IP. Gunakan hanya target milik sendiri atau yang sudah mendapat izin.");

        targetInput = input("Hostname / IP", "example.com");
        page.addView(targetInput);

        permissionCheck = new CheckBox(this);
        permissionCheck.setText(
                "Saya memiliki izin untuk menguji target ini");
        permissionCheck.setTextColor(WHITE);
        permissionCheck.setButtonTintList(
                android.content.res.ColorStateList.valueOf(GREEN));
        page.addView(permissionCheck);

        heading("TCP PORT CHECK");
        portsInput = input("Port dipisahkan koma", "80,443,8080");
        page.addView(portsInput);

        Button tcp = button("CHECK TCP PORTS");
        page.addView(tcp);
        tcp.setOnClickListener(v -> runPortCheck());

        Button http = button("HTTPS SECURITY SNAPSHOT");
        page.addView(http);
        http.setOnClickListener(v -> runHttpCheck());

        Button stop = button("STOP PEMERIKSAAN");
        stop.setBackground(shape(PANEL2, BORDER, 12));
        stop.setTextColor(GREEN);
        page.addView(stop);
        stop.setOnClickListener(v -> stopScan());

        heading("CATATAN");
        addNote("PEMERIKSAAN DASAR",
                "TCP Check memeriksa koneksi port. HTTPS Snapshot membaca beberapa header keamanan dasar, bukan audit penuh.");
    }

    private void showModule(String name) {
        heading(name.toUpperCase(Locale.ROOT));

        String description = "Modul pemeriksaan keamanan HEXON.";

        for (String[] item : modules) {
            if (item[0].equals(name)) {
                description = item[1];
                break;
            }
        }

        addNote("FUNGSI MODUL", description);

        if (name.equals("Attack Lab")) {
            addNote("BATAS PENGUJIAN",
                    "Simulasi hanya untuk lab lokal atau sistem yang kamu miliki. Tidak menyediakan serangan destruktif.");
        }

        if (name.equals("Load Testing")) {
            addNote("PENGUJIAN TERKENDALI",
                    "Modul ini belum aktif. Rancangan nantinya memakai batas laju, durasi, dan tombol penghentian.");
        }

        addNote("STATUS IMPLEMENTASI",
                "Belum terintegrasi pada build ini. Menu sudah tersedia, tetapi fungsi engine belum boleh dianggap aktif.");

        Button back = button("KEMBALI KE TOOLS");
        page.addView(back);
        back.setOnClickListener(v -> showPage("Tools"));
    }

    private void showLogs() {
        heading("ACTIVITY LOG");

        logView = text(
                prefs.getString("activity_log", "Belum ada aktivitas."),
                12, WHITE, false);
        logView.setTypeface(Typeface.MONOSPACE);
        logView.setPadding(dp(13), dp(13), dp(13), dp(13));
        logView.setBackground(shape(PANEL, BORDER, 12));
        page.addView(logView);

        Button clear = button("HAPUS RIWAYAT LOG");
        page.addView(clear);
        clear.setOnClickListener(v -> {
            prefs.edit().remove("activity_log").apply();
            logView.setText("Belum ada aktivitas.");
        });
    }

    private void showReports() {
        heading("REPORTS");
        addNote("LAPORAN PEMERIKSAAN",
                "Riwayat aktivitas tersimpan di perangkat. Ekspor laporan terstruktur belum tersedia.");

        Button logs = button("LIHAT ACTIVITY LOG");
        page.addView(logs);
        logs.setOnClickListener(v -> showPage("Log"));
    }

    private void showSettings() {
        heading("PENGATURAN");
        addNote("INFORMASI APLIKASI",
                "HEXON Android • Versi 0.2.0 • ARM64");
        addNote("MODE KEAMANAN",
                "Safe Mode aktif. Pemeriksaan membutuhkan konfirmasi izin target.");
        addNote("PENYIMPANAN",
                "Activity Log disimpan secara lokal menggunakan SharedPreferences.");
    }

    private void metric(LinearLayout row,
                        String value, String label) {
        LinearLayout box = card();
        box.addView(text(value, 20, GREEN, true));
        box.addView(text(label, 10, MUTED, false));

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(0, -2, 1);
        lp.setMargins(dp(3), 0, dp(3), 0);
        row.addView(box, lp);
    }

    private View actionCard(String title, String desc,
                            String detail, Runnable action) {
        LinearLayout box = card();
        box.addView(text("⬡ " + title, 13, GREEN, true));
        box.addView(text(desc, 16, WHITE, true));
        box.addView(text(detail, 10, MUTED, false));
        box.setOnClickListener(v -> action.run());

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(0, -2, 1);
        lp.setMargins(dp(3), 0, dp(3), 0);
        box.setLayoutParams(lp);
        return box;
    }

    private void addMenuCard(String title, String desc,
                             Runnable action) {
        LinearLayout box = card();
        box.addView(text("⬡ " + title, 14, GREEN, true));
        box.addView(text(desc, 12, MUTED, false));
        box.setOnClickListener(v -> action.run());
        page.addView(box);
    }

    private void addNote(String title, String desc) {
        LinearLayout box = card();
        box.addView(text(title, 12, GREEN, true));
        box.addView(text(desc, 12, MUTED, false));
        page.addView(box);
    }

    private LinearLayout card() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14), dp(13), dp(14), dp(13));
        box.setBackground(shape(PANEL, BORDER, 15));

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(5), 0, dp(6));
        box.setLayoutParams(lp);
        return box;
    }

    private void heading(String value) {
        TextView t = text(value, 13, GREEN, true);
        t.setPadding(0, dp(17), 0, dp(7));
        page.addView(t);
    }

    private TextView text(String value, int size,
                          int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private EditText input(String hint, String value) {
        EditText e = new EditText(this);
        e.setSingleLine(true);
        e.setHint(hint);
        e.setText(value);
        e.setTextColor(WHITE);
        e.setHintTextColor(MUTED);
        e.setPadding(dp(12), dp(10), dp(12), dp(10));
        e.setBackground(shape(PANEL2, GREEN, 10));
        return e;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(13);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(Color.BLACK);
        b.setBackground(shape(GREEN, GREEN, 12));

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(6), 0, dp(6));
        b.setLayoutParams(lp);
        return b;
    }

    private GradientDrawable shape(int color,
                                   int stroke, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        d.setStroke(dp(1), stroke);
        return d;
    }

    private int dp(int value) {
        return (int) (value *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    private boolean authorized(String host) {
        if (host.isEmpty() || host.contains("/") ||
                host.contains(" ") || host.contains("@")) {
            toast("Masukkan hostname atau IP saja.");
            return false;
        }

        if (!permissionCheck.isChecked()) {
            toast("Konfirmasi izin target terlebih dahulu.");
            return false;
        }

        return true;
    }

    private void runPortCheck() {
        if (scanRunning) {
            toast("Pemeriksaan masih berjalan.");
            return;
        }

        final String host =
                targetInput.getText().toString().trim();
        final String raw =
                portsInput.getText().toString().trim();

        if (!authorized(host)) return;

        ArrayList<Integer> ports = new ArrayList<>();

        try {
            for (String item : raw.split(",")) {
                int p = Integer.parseInt(item.trim());
                if (p < 1 || p > 65535)
                    throw new NumberFormatException();
                if (!ports.contains(p)) ports.add(p);
            }

            if (ports.isEmpty() || ports.size() > 10)
                throw new NumberFormatException();

        } catch (Exception e) {
            toast("Masukkan 1–10 port valid.");
            return;
        }

        final ArrayList<Integer> selected = ports;
        stopRequested = false;
        scanRunning = true;
        append("TCP check dimulai: " + host);

        worker.execute(() -> {
            for (int port : selected) {
                if (stopRequested) break;

                boolean open = false;
                try (Socket socket = new Socket()) {
                    socket.connect(
                            new InetSocketAddress(host, port), 1200);
                    open = true;
                } catch (Exception ignored) {}

                final String result = host + ":" + port +
                        (open ? " OPEN" : " closed/filtered");

                runOnUiThread(() -> append(result));
            }

            scanRunning = false;
            runOnUiThread(() -> append(
                    stopRequested ? "Pemeriksaan dihentikan."
                                  : "TCP check selesai."));
        });
    }

    private void runHttpCheck() {
        if (scanRunning) {
            toast("Pemeriksaan masih berjalan.");
            return;
        }

        final String host =
                targetInput.getText().toString().trim();

        if (!authorized(host)) return;

        stopRequested = false;
        scanRunning = true;
        append("HTTPS snapshot dimulai: " + host);

        worker.execute(() -> {
            HttpURLConnection conn = null;

            try {
                URL url = new URL("https://" + host + "/");
                conn = (HttpURLConnection) url.openConnection();
                activeConnection = conn;

                conn.setRequestMethod("GET");
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(4000);
                conn.setInstanceFollowRedirects(false);
                conn.setRequestProperty(
                        "User-Agent", "HEXON-Authorized-Audit/0.2");

                int code = conn.getResponseCode();
                Map<String, List<String>> headers =
                        conn.getHeaderFields();

                final String result = "HTTPS " + host +
                        " → HTTP " + code;

                runOnUiThread(() -> {
                    append(result);
                    showHeader(headers, "strict-transport-security");
                    showHeader(headers, "content-security-policy");
                    showHeader(headers, "x-content-type-options");
                    showHeader(headers, "x-frame-options");
                });

            } catch (Exception ex) {
                runOnUiThread(() -> append(
                        "HTTPS error: " +
                        ex.getClass().getSimpleName()));

            } finally {
                if (conn != null) conn.disconnect();
                activeConnection = null;
                scanRunning = false;
                runOnUiThread(() -> append(
                        stopRequested ? "Pemeriksaan dihentikan."
                                      : "HTTPS snapshot selesai."));
            }
        });
    }

    private void showHeader(Map<String, List<String>> headers,
                            String key) {
        List<String> value = null;

        for (Map.Entry<String, List<String>> entry :
                headers.entrySet()) {
            if (entry.getKey() != null &&
                    entry.getKey().equalsIgnoreCase(key)) {
                value = entry.getValue();
                break;
            }
        }

        append(key + ": " +
                (value == null ? "not present" : value.toString()));
    }

    private void stopScan() {
        if (!scanRunning) {
            toast("Tidak ada pemeriksaan aktif.");
            return;
        }

        stopRequested = true;

        HttpURLConnection conn = activeConnection;
        if (conn != null) conn.disconnect();

        append("Permintaan STOP dikirim.");
    }

    private void append(String line) {
        String old = prefs.getString("activity_log", "");
        String updated = old + "› " + line + "\n";

        if (updated.length() > 12000) {
            updated = updated.substring(
                    updated.length() - 12000);
        }

        prefs.edit().putString("activity_log", updated).apply();

        if (logView != null) {
            logView.setText(updated);
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message,
                Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        stopRequested = true;
        HttpURLConnection conn = activeConnection;
        if (conn != null) conn.disconnect();
        worker.shutdownNow();
        super.onDestroy();
    }
}
