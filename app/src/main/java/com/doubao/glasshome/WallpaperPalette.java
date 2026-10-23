package com.doubao.glasshome;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
final class WallpaperPalette {
    static final String DEFAULT="blue_glass",CUSTOM="custom_image",PICK="pick_image",REMOVE="remove_image";
    static final class Preset {
        final String id,label;final int argb;final boolean solid;
        Preset(String id,String label,int argb,boolean solid){this.id=id;this.label=label;this.argb=argb;this.solid=solid;}
    }
    private static final List<Preset> PRESETS=Collections.unmodifiableList(Arrays.asList(
        new Preset(DEFAULT,"蓝色玻璃 · 原版",0xFF0F5081,false),
        new Preset("obsidian","墨黑",0xFF111418,true),
        new Preset("graphite","石墨灰",0xFF25292E,true),
        new Preset("midnight","深海蓝",0xFF14263C,true),
        new Preset("slate","岩蓝灰",0xFF2E3A47,true),
        new Preset("pine","松针绿",0xFF233B32,true),
        new Preset("stone","暖岩灰",0xFF413D36,true),
        new Preset("cocoa","可可棕",0xFF3D302B,true),
        new Preset("burgundy","酒红",0xFF442C35,true),
        new Preset("aubergine","烟紫",0xFF343046,true),
        new Preset("petrol","深青",0xFF1D4042,true)
    ));
    static List<Preset> presets(){return PRESETS;}
    static Preset preset(String id){for(Preset preset:PRESETS)if(preset.id.equals(id))return preset;return PRESETS.get(0);}
    static String normalize(String id,boolean photoExists){
        if(CUSTOM.equals(id))return photoExists?CUSTOM:DEFAULT;
        for(Preset preset:PRESETS)if(preset.id.equals(id))return id;
        return DEFAULT;
    }
}
