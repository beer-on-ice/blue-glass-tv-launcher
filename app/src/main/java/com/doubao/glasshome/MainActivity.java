package com.doubao.glasshome;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.tv.TvContract;
import android.media.tv.TvInputManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends Activity {
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private SharedPreferences prefs;
    private HomeLayout home;
    private final GlassTile[] inputs=new GlassTile[3],favorites=new GlassTile[6];
    private List<DeviceCatalog.Input> allInputs=new ArrayList<>(),shownInputs=new ArrayList<>();
    private List<DeviceCatalog.App> allApps=new ArrayList<>(),favoriteApps=new ArrayList<>();
    private TvInputManager inputManager;
    private boolean resumed=false,loaded=false;
    private int generation=0;
    private int wallpaperGeneration=0;
    private static final int PICK_WALLPAPER=41;
    private Dialog activeDialog;
    private final Runnable clock=new Runnable(){@Override public void run(){updateClock();if(resumed)handler.postDelayed(this,1000*(60-new Date().getSeconds()));}};
    private final TvInputManager.TvInputCallback inputCallback=new TvInputManager.TvInputCallback(){
        @Override public void onInputStateChanged(String id,int state){reload();}
        @Override public void onInputAdded(String id){reload();}
        @Override public void onInputRemoved(String id){reload();}
        @Override public void onInputUpdated(String id){reload();}
    };
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        prefs=getSharedPreferences("glass_home",MODE_PRIVATE);
        inputManager=(TvInputManager)getSystemService(TV_INPUT_SERVICE);
        home=new HomeLayout(this);
        GlassTile wifi=new GlassTile(this,GlassTile.ICON,"网络设置","wifi","","",54,54);
        wifi.setOnClickListener(v->openSystem(Settings.ACTION_WIFI_SETTINGS));
        home.addTile(wifi,0,1087,54,54,54);
        GlassTile settings=new GlassTile(this,GlassTile.ICON,"桌面与电视设置","settings","","",54,54);
        settings.setOnClickListener(v->showSettings());
        home.addTile(settings,0,1162,54,54,54);
        GlassTile sources=new GlassTile(this,GlassTile.PILL,"全部输入 ›","","","",116,31);
        sources.setOnClickListener(v->showInputs());home.addTile(sources,1,1100,155,116,31);
        float inputWidth=(1152-40)/3f;
        for(int i=0;i<3;i++){
            final int slot=i;
            inputs[i]=new GlassTile(this,GlassTile.INPUT,"读取输入设备","monitor","HDMI","正在读取",inputWidth,165);
            inputs[i].setOnClickListener(v->{if(slot<shownInputs.size())openInput(shownInputs.get(slot));else showInputs();});
            inputs[i].setOnLongClickListener(v->{if(slot<shownInputs.size())renameInput(shownInputs.get(slot));else showInputNames();return true;});
            home.addTile(inputs[i],2,64+i*(inputWidth+20),202,inputWidth,165);
        }
        float appWidth=(1152-5*18)/6f;
        for(int i=0;i<6;i++){
            final int slot=i;
            favorites[i]=new GlassTile(this,GlassTile.APP,"读取应用","apps","","",appWidth,145);
            favorites[i].setOnClickListener(v->{if(slot<favoriteApps.size())openApp(favoriteApps.get(slot));else showApps(slot);});
            favorites[i].setOnLongClickListener(v->{showApps(slot);return true;});
            home.addTile(favorites[i],3,64+i*(appWidth+18),442,appWidth,145);
        }
        GlassTile all=new GlassTile(this,GlassTile.PILL,"全部应用 ›","","","",135,38);
        all.setOnClickListener(v->showApps(-1));home.addTile(all,4,1081,611,135,38);
        setContentView(home);restoreWallpaper();home.post(()->home.initialFocus());updateClock();
    }
    @Override protected void onResume(){
        super.onResume();resumed=true;
        if(inputManager!=null)try{inputManager.registerCallback(inputCallback,handler);}catch(RuntimeException ignored){}
        handler.removeCallbacks(clock);handler.post(clock);reload();
    }
    @Override protected void onPause(){
        resumed=false;handler.removeCallbacks(clock);
        if(inputManager!=null)try{inputManager.unregisterCallback(inputCallback);}catch(RuntimeException ignored){}
        super.onPause();
    }
    @Override protected void onDestroy(){
        generation++;wallpaperGeneration++;handler.removeCallbacksAndMessages(null);worker.shutdownNow();
        if(activeDialog!=null&&activeDialog.isShowing())activeDialog.dismiss();super.onDestroy();
    }
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);if(activeDialog!=null)activeDialog.dismiss();}
    @Override public boolean dispatchKeyEvent(KeyEvent event){
        if(event.getAction()==KeyEvent.ACTION_DOWN){
            int key=event.getKeyCode();int direction=key==KeyEvent.KEYCODE_DPAD_LEFT?0:key==KeyEvent.KEYCODE_DPAD_RIGHT?1:key==KeyEvent.KEYCODE_DPAD_UP?2:key==KeyEvent.KEYCODE_DPAD_DOWN?3:-1;
            if(direction>=0&&home.move(direction))return true;
            if(key==KeyEvent.KEYCODE_MENU){showSettings();return true;}
        }
        return super.dispatchKeyEvent(event);
    }
    @Override public void onBackPressed(){if(activeDialog!=null&&activeDialog.isShowing())activeDialog.dismiss();else home.initialFocus();}
    private void reload(){
        if(!resumed||worker.isShutdown())return;
        int serial=++generation;
        worker.execute(()->{
            List<DeviceCatalog.Input> foundInputs=DeviceCatalog.inputs(this);
            List<DeviceCatalog.App> foundApps;
            try{foundApps=DeviceCatalog.apps(this);}catch(RuntimeException e){foundApps=new ArrayList<>();}
            final List<DeviceCatalog.App> apps=foundApps;
            handler.post(()->{if(isFinishing()||isDestroyed()||serial!=generation)return;allInputs=foundInputs;allApps=apps;render();});
        });
    }
    private void render(){
        List<DeviceCatalog.Input> ordered=new ArrayList<>(allInputs);
        ordered.sort((a,b)->HomeLogic.comparePorts(a.port,b.port));
        List<String> ports=new ArrayList<>();for(DeviceCatalog.Input input:ordered)ports.add(input.portId);
        shownInputs=new ArrayList<>();for(int index:HomeLogic.firstPerPort(ports))shownInputs.add(ordered.get(index));
        for(int i=0;i<3;i++){
            if(i<shownInputs.size()){
                DeviceCatalog.Input input=shownInputs.get(i);String title=inputName(input),glyph=DeviceCatalog.glyphFor("",title);
                inputs[i].content(title,"play".equals(glyph)?"monitor":glyph,input.port,input.status());
            }else inputs[i].content(i==0?"选择输入源":"索尼电视","monitor",i==0?"INPUTS":"TV / AV",i==0?"打开系统输入选择":"查看全部输入设备");
        }
        List<String> available=new ArrayList<>();for(DeviceCatalog.App app:allApps)available.add(app.pkg);
        String defaults="com.teamsmart.videomanager.tv|com.xx.blbl|com.mygithub0.tvbox0.osdX|tv.emby.embyatv|app.minireel.minireel.tv|com.ghosten.player";
        List<String> chosen=HomeLogic.favorites(Arrays.asList(prefs.getString("favorites",defaults).split("\\|")),available,6);
        favoriteApps.clear();
        for(String pkg:chosen)for(DeviceCatalog.App app:allApps)if(app.pkg.equals(pkg)){favoriteApps.add(app);break;}
        for(int i=0;i<6;i++){
            if(i<favoriteApps.size())favorites[i].content(favoriteApps.get(i).label,favoriteApps.get(i).glyph,"","");
            else favorites[i].content("添加应用","apps","","");
        }
        if(!loaded){loaded=true;home.initialFocus();}
        updateClock();
    }
    private void updateClock(){
        if(home==null)return;
        Date now=new Date();String network="网络未连接";
        try{
            ConnectivityManager manager=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            NetworkCapabilities caps=manager==null?null:manager.getNetworkCapabilities(manager.getActiveNetwork());
            if(caps!=null){
                if(caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))network="以太网已连接";
                else if(caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))network="Wi-Fi 已连接";
                else network="网络已连接";
            }
        }catch(RuntimeException ignored){}
        home.information(prefs.getString("room","客厅"),new SimpleDateFormat("HH:mm",Locale.CHINA).format(now),new SimpleDateFormat("M月d日 EEEE",Locale.CHINA).format(now),network);
    }
    private void openApp(DeviceCatalog.App app){
        try{startActivity(app.intent);}catch(ActivityNotFoundException|SecurityException e){toast("应用暂时无法启动");reload();}
    }
    private void openInput(DeviceCatalog.Input input){
        Intent view=new Intent(Intent.ACTION_VIEW).setDataAndType(TvContract.buildChannelUriForPassthroughInput(input.id),"vnd.android.cursor.item/channel").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Sony's system TV player owns the passthrough channels on this set.
        try{startActivity(new Intent(view).setPackage("com.sony.dtv.tvx"));return;}catch(ActivityNotFoundException|SecurityException ignored){}
        try{startActivity(view);}catch(ActivityNotFoundException|SecurityException e){toast("无法直接切换，请使用系统输入源面板");openSourcePanel();}
    }
    private void openSourcePanel(){
        try{startActivity(new Intent("com.android.tv.action.VIEW_INPUTS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}
        catch(ActivityNotFoundException|SecurityException e){toast("请按遥控器的输入源键");}
    }
    private void showInputs(){
        List<String> names=new ArrayList<>();for(DeviceCatalog.Input input:allInputs)names.add(inputName(input)+"  ·  "+input.port+"\n"+input.status());
        names.add("打开系统输入源面板（含直播电视 / AV）");
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("输入设备").setItems(names.toArray(new String[0]),(d,index)->{if(index<allInputs.size())openInput(allInputs.get(index));else openSourcePanel();}).setNegativeButton("返回",null).create();
        present(dialog);
    }
    private void showSettings(){
        List<String> items=new ArrayList<>(Arrays.asList("电视系统设置","网络设置","更改客厅名称","桌面壁纸","输入设备名称","全部输入设备","选择默认桌面"));
        Intent candidate=getPackageManager().getLeanbackLaunchIntentForPackage("com.gathin.tvhome");
        if(candidate==null)candidate=getPackageManager().getLaunchIntentForPackage("com.gathin.tvhome");
        final Intent previous=candidate;
        if(previous!=null)items.add("打开 CleanYourTV");
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("客厅桌面设置").setItems(items.toArray(new String[0]),(d,index)->{
            switch(index){
                case 0:openSystem(Settings.ACTION_SETTINGS);break;
                case 1:openSystem(Settings.ACTION_WIFI_SETTINGS);break;
                case 2:renameRoom();break;
                case 3:showWallpapers();break;
                case 4:showInputNames();break;
                case 5:showInputs();break;
                case 6:openSystem(Settings.ACTION_HOME_SETTINGS);break;
                case 7:
                    if(previous!=null)try{startActivity(previous);}catch(RuntimeException e){toast("CleanYourTV 暂时不可用");}
                    else toast("未安装 CleanYourTV");
                    break;
            }
        }).setNegativeButton("返回",null).create();present(dialog);
    }
    private String inputName(DeviceCatalog.Input input){return HomeLogic.inputName(prefs.getString("input_name."+input.portId,""),input.title);}
    private void showInputNames(){
        java.util.LinkedHashMap<String,DeviceCatalog.Input> ports=new java.util.LinkedHashMap<>();
        for(DeviceCatalog.Input input:allInputs)if(!ports.containsKey(input.portId))ports.put(input.portId,input);
        List<DeviceCatalog.Input> choices=new ArrayList<>(ports.values());
        if(choices.isEmpty()){toast("暂未读取到 HDMI 端口，请稍后再试");return;}
        List<String> names=new ArrayList<>();for(DeviceCatalog.Input input:choices)names.add(input.port+"  ·  "+inputName(input));
        present(new AlertDialog.Builder(this).setTitle("输入设备名称").setItems(names.toArray(new String[0]),(d,index)->renameInput(choices.get(index))).setNegativeButton("返回",null).create());
    }
    private void renameInput(DeviceCatalog.Input input){
        EditText edit=new EditText(this);edit.setSingleLine(true);edit.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_WORDS);edit.setHint("例如：ATV、蓝光、游戏机");edit.setText(inputName(input));edit.selectAll();
        LinearLayout wrap=new LinearLayout(this);wrap.setPadding(dp(24),dp(10),dp(24),dp(8));wrap.addView(edit,new LinearLayout.LayoutParams(-1,-2));
        String key="input_name."+input.portId;
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(input.port+" · 显示名称").setMessage("仅修改本桌面的名称；留空可恢复系统名称。").setView(wrap)
            .setPositiveButton("保存",(d,w)->{
                String name=HomeLogic.inputName(edit.getText().toString(),"");
                SharedPreferences.Editor change=prefs.edit();if(name.isEmpty())change.remove(key);else change.putString(key,name);change.apply();render();
            }).setNeutralButton("恢复系统名称",(d,w)->{prefs.edit().remove(key).apply();render();}).setNegativeButton("取消",null).create();
        present(dialog);
    }
    private void restoreWallpaper(){
        String selected=WallpaperPalette.normalize(prefs.getString("wallpaper_id",WallpaperPalette.DEFAULT),WallpaperImage.file(this).isFile());
        if(!WallpaperPalette.CUSTOM.equals(selected)){home.wallpaper(selected,null);return;}
        int serial=++wallpaperGeneration;
        worker.execute(()->{
            Bitmap found=null;
            try{found=WallpaperImage.decode(WallpaperImage.file(this));}catch(java.io.IOException|RuntimeException|OutOfMemoryError ignored){}
            final Bitmap image=found;
            handler.post(()->{
                if(isDestroyed()||serial!=wallpaperGeneration){if(image!=null)image.recycle();return;}
                home.wallpaper(image==null?WallpaperPalette.DEFAULT:WallpaperPalette.CUSTOM,image);
                if(image==null){prefs.edit().putString("wallpaper_id",WallpaperPalette.DEFAULT).apply();toast("原图片无法读取，已恢复蓝色玻璃");}
            });
        });
    }
    private void showWallpapers(){
        String selected=WallpaperPalette.normalize(prefs.getString("wallpaper_id",WallpaperPalette.DEFAULT),WallpaperImage.file(this).isFile());
        if(activeDialog!=null)activeDialog.dismiss();
        activeDialog=new WallpaperDialog(this,selected,WallpaperImage.file(this).isFile(),id->{
            if(WallpaperPalette.PICK.equals(id)){pickWallpaper();return;}
            if(WallpaperPalette.REMOVE.equals(id)){
                wallpaperGeneration++;
                if(WallpaperPalette.CUSTOM.equals(prefs.getString("wallpaper_id",WallpaperPalette.DEFAULT))){prefs.edit().putString("wallpaper_id",WallpaperPalette.DEFAULT).apply();home.wallpaper(WallpaperPalette.DEFAULT,null);}
                worker.execute(()->{boolean removed=WallpaperImage.file(this).delete();handler.post(()->{if(!isDestroyed())toast(removed?"本地壁纸副本已移除":"图片暂时无法移除");});});
                return;
            }
            wallpaperGeneration++;prefs.edit().putString("wallpaper_id",id).apply();
            if(WallpaperPalette.CUSTOM.equals(id))restoreWallpaper();else home.wallpaper(id,null);
        });activeDialog.show();
    }
    private void pickWallpaper(){
        for(String action:new String[]{Intent.ACTION_OPEN_DOCUMENT,Intent.ACTION_GET_CONTENT}){
            Intent query=new Intent(action).addCategory(Intent.CATEGORY_OPENABLE).setType("image/*");
            for(android.content.pm.ResolveInfo candidate:getPackageManager().queryIntentActivities(query,0)){
                // Some Android TV builds ship a placeholder that only reports an unavailable picker.
                if("com.google.android.tv.frameworkpackagestubs".equals(candidate.activityInfo.packageName))continue;
                try{startActivityForResult(new Intent(query).setClassName(candidate.activityInfo.packageName,candidate.activityInfo.name),PICK_WALLPAPER);return;}
                catch(ActivityNotFoundException|SecurityException ignored){}
            }
        }
        toast("电视没有可用的图片选择器，请安装文件管理器或使用预设纯色");
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request!=PICK_WALLPAPER||result!=RESULT_OK||data==null||data.getData()==null)return;
        int serial=++wallpaperGeneration;android.net.Uri uri=data.getData();toast("正在处理壁纸…");
        worker.execute(()->{
            Bitmap found=null;
            try{found=WallpaperImage.importImage(this,uri);}catch(java.io.IOException|RuntimeException|OutOfMemoryError ignored){}
            final Bitmap image=found;
            handler.post(()->{
                if(isDestroyed()||serial!=wallpaperGeneration){if(image!=null)image.recycle();return;}
                if(image==null){toast("图片无法读取或超过 32 MB，原壁纸已保留");return;}
                prefs.edit().putString("wallpaper_id",WallpaperPalette.CUSTOM).apply();home.wallpaper(WallpaperPalette.CUSTOM,image);toast("壁纸已保存");
            });
        });
    }
    private void renameRoom(){
        EditText edit=new EditText(this);edit.setSingleLine(true);edit.setInputType(InputType.TYPE_CLASS_TEXT);edit.setText(prefs.getString("room","客厅"));edit.selectAll();
        LinearLayout wrap=new LinearLayout(this);wrap.setPadding(dp(24),dp(10),dp(24),dp(8));wrap.addView(edit,new LinearLayout.LayoutParams(-1,-2));
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("空间名称").setView(wrap).setPositiveButton("保存",(d,w)->{
            String name=edit.getText().toString().trim();if(!name.isEmpty()){prefs.edit().putString("room",name.substring(0,Math.min(name.length(),16))).apply();updateClock();}
        }).setNegativeButton("取消",null).create();present(dialog);
    }
    private void openSystem(String action){
        try{startActivity(new Intent(action));}catch(ActivityNotFoundException|SecurityException e){
            if(!Settings.ACTION_SETTINGS.equals(action)){toast("此系统不提供该入口，打开电视设置");openSystem(Settings.ACTION_SETTINGS);}else toast("系统设置暂时不可用");
        }
    }
    private void present(AlertDialog dialog){if(activeDialog!=null)activeDialog.dismiss();activeDialog=dialog;dialog.show();}
    private void showApps(int replace){
        if(allApps.isEmpty()){toast("应用列表尚未就绪");return;}
        if(activeDialog!=null)activeDialog.dismiss();
        Dialog dialog=new Dialog(this);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);activeDialog=dialog;
        int width=(int)(getResources().getDisplayMetrics().widthPixels*.89f),height=(int)(getResources().getDisplayMetrics().heightPixels*.83f);
        LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(dp(22),dp(20),dp(22),dp(18));
        GradientDrawable bg=new GradientDrawable();bg.setColor(0xFA0B345E);bg.setCornerRadius(dp(19));bg.setStroke(dp(1),0x7075B9E5);panel.setBackground(bg);
        TextView heading=new TextView(this);heading.setText(replace<0?"全部应用":"更换第 "+(replace+1)+" 个常用应用");heading.setTextSize(23);heading.setTextColor(0xFFE0F3FF);panel.addView(heading);
        TextView subtitle=new TextView(this);subtitle.setText(replace<0?allApps.size()+" 个应用  ·  按返回关闭":"选择后立即保存；其他应用不会卸载");subtitle.setTextSize(13);subtitle.setTextColor(0xAAC6E7FC);subtitle.setPadding(0,dp(7),0,dp(14));panel.addView(subtitle);
        ScrollView scroll=new ScrollView(this);scroll.setClipToPadding(false);scroll.setPadding(dp(3),dp(3),dp(3),dp(8));
        GridLayout grid=new GridLayout(this);grid.setColumnCount(5);grid.setPadding(0,0,0,dp(10));scroll.addView(grid);
        int tileWidth=(width-dp(50)-dp(10)*5)/5,tileHeight=(int)(tileWidth*.82f);
        List<GlassTile> tiles=new ArrayList<>();
        for(DeviceCatalog.App app:allApps){
            GlassTile tile=new GlassTile(this,GlassTile.APP,app.label,app.glyph,"","",154,125);tiles.add(tile);
            GridLayout.LayoutParams lp=new GridLayout.LayoutParams();lp.width=tileWidth;lp.height=tileHeight;lp.setMargins(dp(5),dp(5),dp(5),dp(5));grid.addView(tile,lp);
            tile.setOnClickListener(v->{dialog.dismiss();if(replace<0)openApp(app);else setFavorite(replace,app.pkg);});
        }
        for(int i=0;i<tiles.size();i++){
            GlassTile tile=tiles.get(i);int row=i/5,column=i%5;
            tile.setNextFocusLeftId(tiles.get(column==0?i:i-1).getId());
            tile.setNextFocusRightId(tiles.get(column==4||i+1==tiles.size()?i:i+1).getId());
            tile.setNextFocusUpId(tiles.get(row==0?i:i-5).getId());
            tile.setNextFocusDownId(tiles.get(i+5<tiles.size()?i+5:i).getId());
        }
        panel.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));dialog.setContentView(panel);
        Window window=dialog.getWindow();if(window!=null){window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);WindowManager.LayoutParams attrs=window.getAttributes();attrs.dimAmount=.55f;window.setAttributes(attrs);}
        dialog.show();if(window!=null)window.setLayout(width,height);
        grid.post(()->tiles.get(0).requestFocus());
    }
    private void setFavorite(int slot,String pkg){
        List<String> order=new ArrayList<>();for(DeviceCatalog.App app:favoriteApps)order.add(app.pkg);
        if(slot<order.size()){
            int other=order.indexOf(pkg);String old=order.get(slot);order.set(slot,pkg);if(other>=0&&other!=slot)order.set(other,old);
        }else order.add(pkg);
        prefs.edit().putString("favorites",String.join("|",order)).apply();render();toast("常用应用已更新");
    }
    private int dp(float size){return Math.round(size*getResources().getDisplayMetrics().density);}
    private void toast(String text){Toast.makeText(this,text,Toast.LENGTH_SHORT).show();}
}
