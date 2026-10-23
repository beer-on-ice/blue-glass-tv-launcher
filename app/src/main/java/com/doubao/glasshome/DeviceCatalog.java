package com.doubao.glasshome;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class DeviceCatalog {
    static final class Input {
        final String id, portId, title, port, glyph;
        final int state;
        Input(String id, String portId, String title, String port, String glyph, int state) {
            this.id=id; this.portId=portId; this.title=title; this.port=port; this.glyph=glyph; this.state=state;
        }
        String status() {
            if (state == TvInputManager.INPUT_STATE_CONNECTED) return "设备已连接";
            if (state == TvInputManager.INPUT_STATE_DISCONNECTED) return "未连接设备";
            if (state == TvInputManager.INPUT_STATE_CONNECTED_STANDBY) return "设备待机中";
            return "选择此输入";
        }
    }
    static final class App {
        final String pkg, label, glyph;
        final Intent intent;
        App(String pkg, String label, Intent intent) {
            this.pkg=pkg; this.label=label; this.intent=intent; this.glyph=glyphFor(pkg,label);
        }
    }
    static List<Input> inputs(Context context) {
        TvInputManager manager=(TvInputManager)context.getSystemService(Context.TV_INPUT_SERVICE);
        List<Input> result=new ArrayList<>();
        if (manager==null) return result;
        try {
            List<TvInputInfo> all=manager.getTvInputList();
            Map<String,TvInputInfo> byId=new HashMap<>();
            Map<String,List<String>> children=new HashMap<>();
            for(TvInputInfo info:all) byId.put(info.getId(),info);
            for(TvInputInfo info:all) {
                if(info.getParentId()!=null && byId.containsKey(info.getParentId()) && !info.isHidden(context))
                    children.computeIfAbsent(info.getParentId(),key->new ArrayList<>()).add(info.getId());
            }
            int ports=0, connected=0;
            for(TvInputInfo info:all) {
                if(info.getType()!=TvInputInfo.TYPE_HDMI || info.getParentId()!=null || !info.isPassthroughInput()) continue;
                ports++;
                if(manager.getInputState(info.getId())==TvInputManager.INPUT_STATE_CONNECTED) connected++;
            }
            for(TvInputInfo info:all) {
                if(!info.isPassthroughInput() || info.isHidden(context) || byId.containsKey(info.getParentId())) continue;
                if(info.getType()!=TvInputInfo.TYPE_HDMI) continue;
                String port=string(info.loadLabel(context));
                if(port.isEmpty()) port="HDMI";
                if(!port.toUpperCase(Locale.ROOT).contains("HDMI")) port="HDMI "+port;
                List<String> childIds=children.get(info.getId());
                if(childIds==null) childIds=Collections.emptyList();
                for(String target:HomeLogic.inputTargets(info.getId(),childIds)) {
                    TvInputInfo selected=byId.get(target);
                    String title=string(info.loadCustomLabel(context));
                    if(!target.equals(info.getId())) {
                        String childCustom=string(selected.loadCustomLabel(context));
                        if(!childCustom.isEmpty()) title=childCustom;
                        else if(title.isEmpty()) title=string(selected.loadLabel(context));
                    }
                    if(title.isEmpty()) title=port;
                    int state=HomeLogic.inputState(manager.getInputState(selected.getId()),ports,connected);
                    String glyph=glyphFor("",title);
                    if("play".equals(glyph))glyph="monitor";
                    result.add(new Input(selected.getId(),info.getId(),title,port,glyph,state));
                }
            }
            Collections.sort(result,(a,b)->HomeLogic.comparePorts(a.port,b.port));
        } catch(RuntimeException ignored) {
            // An OEM may temporarily deny enumeration during wake-up; the UI keeps a system input entry.
        }
        return result;
    }
    static List<App> apps(Context context) {
        PackageManager pm=context.getPackageManager();
        Map<String,App> entries=new LinkedHashMap<>();
        for(String category:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER}) {
            Intent query=new Intent(Intent.ACTION_MAIN).addCategory(category);
            for(ResolveInfo info:pm.queryIntentActivities(query,0)) {
                String pkg=info.activityInfo.packageName;
                if(pkg.equals(context.getPackageName()) || entries.containsKey(pkg)) continue;
                Intent launch=new Intent(Intent.ACTION_MAIN).addCategory(category)
                    .setClassName(pkg,info.activityInfo.name).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                entries.put(pkg,new App(pkg,string(info.loadLabel(pm)),launch));
            }
        }
        List<App> result=new ArrayList<>(entries.values());
        Collator collator=Collator.getInstance(Locale.CHINA);
        Collections.sort(result,(a,b)->collator.compare(a.label,b.label));
        return result;
    }
    private static String string(CharSequence value) { return value==null?"":value.toString().trim(); }
    static String glyphFor(String pkg,String label) {
        String s=(pkg+" "+label).toLowerCase(Locale.ROOT);
        if(s.contains("playstation")||s.contains("ps5")||s.contains("xbox")) return "game";
        if(s.contains("switch")) return "switch";
        if(s.contains("cast")||s.contains("hpplay")||s.contains("投屏")||s.contains("mirroring")) return "cast";
        if(s.contains("music")||s.contains("音乐")) return "music";
        if(s.contains("xplore")||s.contains("文件")||s.contains("媒体")) return "folder";
        if(s.contains("store")||s.contains("market")||s.contains("应用")) return "apps";
        if(s.contains("setting")||s.contains("设置")) return "settings";
        if(s.contains("tvbox")||s.contains("mytv")||s.contains("影视")||s.contains("电视")) return "tv";
        if(s.contains("emby")||s.contains("player")) return "monitor";
        if(s.contains("blbl")||s.contains("哔")) return "bili";
        if(s.contains("hdmi")||s.contains("apple")) return "monitor";
        return "play";
    }
}
