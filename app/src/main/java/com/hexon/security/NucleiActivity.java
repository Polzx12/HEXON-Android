package com.hexon.security;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.content.Intent;
import java.io.*;
import java.util.*;

public class NucleiActivity extends Activity {
    private final int BG=Color.rgb(7,11,15), PANEL=Color.rgb(16,25,33), GREEN=Color.rgb(45,235,145), WHITE=Color.rgb(235,245,240), MUTED=Color.rgb(145,165,158);
    private LinearLayout root;
    private EditText targets, templates, severity, tags, excludeTags, ids, excludeIds, types, authors, headers, proxy, rate, concurrency, timeout, retries, advanced;
    private CheckBox consent, code, dast, headless, fileProto, selfContained, globalMatchers, redirects, noInteractsh, stats, omitRaw;
    private TextView console, status;
    private volatile Process process;
    private volatile boolean running=false;
    private File engine, templateDir, reportDir, logFile;
    private final StringBuilder output=new StringBuilder();

    @Override public void onCreate(Bundle b){ super.onCreate(b); getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG); setupFiles(); build(); checkEngine(); }
    private void setupFiles(){ engine=new File(getFilesDir(),"nuclei"); templateDir=new File(getFilesDir(),"nuclei-templates"); reportDir=new File(getFilesDir(),"nuclei-reports"); reportDir.mkdirs(); logFile=new File(reportDir,"latest.log"); }
    private void build(){
        ScrollView sc=new ScrollView(this); root=new LinearLayout(this); root.setOrientation(1); root.setPadding(dp(16),dp(12),dp(16),dp(24)); root.setBackgroundColor(BG); sc.addView(root); setContentView(sc);
        TextView title=text("⬡ NUCLEI SCANNER",23,GREEN,true); root.addView(title); root.addView(text("HEXON • TEMPLATE-BASED SECURITY ENGINE • ARM64",10,MUTED,true));
        status=text("Engine: memeriksa…",12,GREEN,true); status.setPadding(0,dp(12),0,dp(8)); root.addView(status);
        field("TARGET / URL / IP (bisa beberapa baris)","https://example.com"); targets=last;
        consent=new CheckBox(this); consent.setText("Saya pemilik target atau memiliki izin tertulis untuk mengujinya"); consent.setTextColor(WHITE); consent.setButtonTintList(android.content.res.ColorStateList.valueOf(GREEN)); root.addView(consent);
        field("TEMPLATE PATH (kosong = semua template)",""); templates=last;
        field("SEVERITY: info,low,medium,high,critical","critical,high,medium"); severity=last;
        field("TAGS INCLUDE"," "); tags=last;
        field("TAGS EXCLUDE"," "); excludeTags=last;
        field("TEMPLATE IDS INCLUDE"," "); ids=last;
        field("TEMPLATE IDS EXCLUDE"," "); excludeIds=last;
        field("PROTOCOL TYPES: dns,file,http,headless,tcp,ssl,websocket,whois,code,javascript"," "); types=last;
        field("AUTHORS"," "); authors=last;
        field("ADVANCED NUCLEI ARGUMENTS (satu argumen per baris; semua opsi CLI versi engine)",""); advanced=last;
        field("CUSTOM HTTP HEADERS (Header: value; pisahkan ;)",""); headers=last;
        field("HTTP / SOCKS5 PROXY (opsional)",""); proxy=last;
        LinearLayout limits=new LinearLayout(this); limits.setOrientation(0); root.addView(limits);
        rate=smallField(limits,"RATE/SEC","5"); concurrency=smallField(limits,"CONCURRENCY","5"); timeout=smallField(limits,"TIMEOUT","10"); retries=smallField(limits,"RETRIES","1");
        code=check("Enable code templates (butuh izin eksplisit)"); dast=check("Enable DAST / fuzz templates"); headless=check("Enable headless browser templates (mungkin tidak tersedia di Android)"); fileProto=check("Enable file protocol templates"); selfContained=check("Enable self-contained templates"); globalMatchers=check("Enable global matchers"); redirects=check("Follow HTTP redirects"); noInteractsh=check("Disable Interactsh / OAST (default aman)"); noInteractsh.setChecked(true); stats=check("Show scan statistics"); omitRaw=check("Omit raw request/response in reports"); omitRaw.setChecked(true);
        button("UPDATE TEMPLATE DATABASE",()->updateTemplates()); button("VALIDATE TEMPLATES",()->validateTemplates()); button("LIST TEMPLATE IDS (-tl)",()->listTemplates()); button("CHECK ENGINE VERSION",()->showVersion());
        button("RUN NUCLEI SCAN",()->runScan()); button("STOP SCAN",()->stopScan()); button("CLEAR CONSOLE",()->{output.setLength(0); render("Console dibersihkan.");}); button("SAVE / SHARE LOG",()->shareLog());
        TextView h=text("NUCLEI CONSOLE",13,GREEN,true); h.setPadding(0,dp(16),0,dp(6)); root.addView(h);
        console=text("Console siap. Update templates sebelum scan pertama.\n",11,WHITE,false); console.setTypeface(Typeface.MONOSPACE); console.setPadding(dp(12),dp(12),dp(12),dp(12)); console.setBackgroundColor(PANEL); root.addView(console,new LinearLayout.LayoutParams(-1,dp(260)));
        button("KEMBALI",()->finish());
    }
    private EditText last;
    private void field(String label,String value){ root.addView(text(label,10,MUTED,true)); EditText e=new EditText(this); e.setText(value.trim()); e.setTextColor(WHITE); e.setHintTextColor(MUTED); e.setTextSize(13); e.setPadding(dp(10),dp(7),dp(10),dp(7)); e.setBackgroundColor(PANEL); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,dp(2),0,dp(8)); root.addView(e,lp); last=e; }
    private EditText smallField(LinearLayout row,String label,String value){ EditText e=new EditText(this); e.setSingleLine(true); e.setText(value); e.setHint(label); e.setTextColor(WHITE); e.setTextSize(12); e.setBackgroundColor(PANEL); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(48),1); lp.setMargins(dp(2),0,dp(2),dp(8)); row.addView(e,lp); return e; }
    private CheckBox check(String label){ CheckBox c=new CheckBox(this); c.setText(label); c.setTextSize(12); c.setTextColor(WHITE); c.setButtonTintList(android.content.res.ColorStateList.valueOf(GREEN)); root.addView(c); return c; }
    private TextView text(String s,int size,int color,boolean bold){ TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); if(bold)t.setTypeface(Typeface.DEFAULT_BOLD); return t; }
    private void button(String label,Runnable action){ Button b=new Button(this); b.setText(label); b.setAllCaps(false); b.setTextColor(Color.BLACK); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(GREEN)); root.addView(b); b.setOnClickListener(v->action.run()); }
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private void render(String s){ runOnUiThread(()->{output.append(s).append('\n'); if(output.length()>60000)output.delete(0,output.length()-60000); console.setText(output.toString()); try(FileWriter w=new FileWriter(logFile,true)){w.write(s);w.write(System.lineSeparator());}catch(IOException e){android.util.Log.e("HEXON","Gagal menyimpan log",e);} }); }
    private void checkEngine(){
        if(engine.exists() && engine.canExecute()){ status.setText("Engine: Nuclei ARM64 siap • "+engine.length()/1024/1024+" MB"); return; }
        try(InputStream in=getAssets().open("nuclei"); FileOutputStream out=new FileOutputStream(engine)){ byte[] buf=new byte[65536]; int n; while((n=in.read(buf))>0)out.write(buf,0,n); out.flush(); engine.setExecutable(true,false); status.setText(engine.canExecute()?"Engine: Nuclei ARM64 siap":"Engine: gagal memberi izin eksekusi"); }
        catch(Exception e){status.setText("Engine belum ada di APK. Build workflow harus menyertakan asset nuclei."); render("Engine asset tidak ditemukan: "+e.getMessage());}
    }
    private boolean ready(){ if(running){toast("Engine sedang berjalan");return false;} if(!engine.exists()||!engine.canExecute()){checkEngine();toast("Binary Nuclei belum tersedia di APK");return false;} return true; }
    private void execute(List<String> args){ if(!ready())return; running=true; render("$ nuclei "+String.join(" ",args)); new Thread(()->{ try{ ArrayList<String> cmd=new ArrayList<>(); cmd.add(engine.getAbsolutePath()); cmd.addAll(args); ProcessBuilder pb=new ProcessBuilder(cmd); pb.redirectErrorStream(true); pb.environment().put("HOME",getFilesDir().getAbsolutePath()); pb.environment().put("NUCLEI_CONFIG_DIR",new File(getFilesDir(),".config/nuclei").getAbsolutePath()); process=pb.start(); try(BufferedReader r=new BufferedReader(new InputStreamReader(process.getInputStream()))){String line; while((line=r.readLine())!=null)render(line);} int code=process.waitFor(); render("Nuclei selesai. Exit code: "+code); }catch(Exception e){render("Engine error: "+e.getClass().getSimpleName()+": "+e.getMessage());}finally{running=false;process=null;}}).start(); }
    private void updateTemplates(){ if(!ready())return; templateDir.mkdirs(); execute(Arrays.asList("-update-templates","-update-template-dir",templateDir.getAbsolutePath(),"-disable-update-check")); }
    private void validateTemplates(){ if(!ready())return; if(!templateDir.exists()){toast("Update template dahulu");return;} execute(Arrays.asList("-validate","-t",templateDir.getAbsolutePath(),"-disable-update-check")); }
    private void listTemplates(){ if(!ready())return; if(!templateDir.exists()){toast("Update template dahulu");return;} execute(Arrays.asList("-tl","-t",templateDir.getAbsolutePath(),"-disable-update-check")); }
    private void showVersion(){ if(!ready())return; execute(Arrays.asList("-version")); }
    private void runScan(){
        if(!ready())return; if(!consent.isChecked()){toast("Konfirmasi izin target dahulu");return;} String raw=targets.getText().toString().trim(); if(raw.isEmpty()){toast("Target wajib diisi");return;}
        ArrayList<String> a=new ArrayList<>(); String[] lines=raw.split("\\s*\\n\\s*"); if(lines.length==1){a.add("-target");a.add(lines[0].trim());}else{try{File f=new File(getFilesDir(),"nuclei-targets.txt");try(FileWriter w=new FileWriter(f)){for(String x:lines)if(!x.trim().isEmpty())w.write(x.trim()+"\n");}a.add("-list");a.add(f.getAbsolutePath());}catch(Exception e){toast("Gagal menulis daftar target");return;}}
        String templatePath=templates.getText().toString().trim(); if(templatePath.isEmpty())templatePath=templateDir.getAbsolutePath(); if(!new File(templatePath).exists()){toast("Template belum tersedia. Jalankan UPDATE TEMPLATE DATABASE.");return;} add(a,"-t",templatePath); add(a,"-severity",severity.getText().toString()); add(a,"-tags",tags.getText().toString()); add(a,"-exclude-tags",excludeTags.getText().toString()); add(a,"-template-id",ids.getText().toString()); add(a,"-exclude-id",excludeIds.getText().toString()); add(a,"-type",types.getText().toString()); add(a,"-author",authors.getText().toString());
        String h=headers.getText().toString().trim(); if(!h.isEmpty())for(String x:h.split(";"))add(a,"-header",x.trim()); add(a,"-proxy",proxy.getText().toString());
        add(a,"-rate-limit",rate.getText().toString()); add(a,"-concurrency",concurrency.getText().toString()); add(a,"-timeout",timeout.getText().toString()); add(a,"-retries",retries.getText().toString());
        String extra=advanced.getText().toString().trim(); if(!extra.isEmpty())for(String line:extra.split("\\n")){String token=line.trim();if(!token.isEmpty())a.add(token);}
        if(code.isChecked())a.add("-code"); if(dast.isChecked())a.add("-dast"); if(headless.isChecked())a.add("-headless"); if(fileProto.isChecked())a.add("-file"); if(selfContained.isChecked())a.add("-enable-self-contained"); if(globalMatchers.isChecked())a.add("-enable-global-matchers"); if(redirects.isChecked())a.add("-follow-redirects"); if(noInteractsh.isChecked())a.add("-no-interactsh"); if(stats.isChecked())a.add("-stats"); if(omitRaw.isChecked())a.add("-omit-raw");
        a.add("-disable-update-check"); a.add("-jsonl-export"); a.add(new File(reportDir,"latest.jsonl").getAbsolutePath()); execute(a);
    }
    private void add(List<String>a,String flag,String value){String v=value.trim(); if(!v.isEmpty()) {a.add(flag);a.add(v);}}
    private void stopScan(){ Process p=process; if(p==null){toast("Tidak ada scan aktif");return;} p.destroy(); render("STOP diminta oleh pengguna."); }
    private void shareLog(){ try{StringBuilder b=new StringBuilder(); if(logFile.exists()){try(BufferedReader r=new BufferedReader(new FileReader(logFile))){String x;while((x=r.readLine())!=null)b.append(x).append('\n');}}else{b.append(output);} if(b.length()==0)b.append("Belum ada log."); Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_SUBJECT,"HEXON Nuclei Scan Log");i.putExtra(Intent.EXTRA_TEXT,b.toString());startActivity(Intent.createChooser(i,"Simpan / bagikan log"));}catch(Exception e){toast("Gagal membuka menu berbagi log");} }
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    @Override protected void onDestroy(){Process p=process;if(p!=null)p.destroy();super.onDestroy();}
}
