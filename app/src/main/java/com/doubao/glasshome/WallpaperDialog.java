package com.doubao.glasshome;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

final class WallpaperDialog extends Dialog {
    interface Selection {void select(String id);}
    private final List<Swatch> tiles=new ArrayList<>();
    private final int width,height;
    private final Swatch initial;
    WallpaperDialog(Context context,String selected,boolean photoExists,Selection selection){
        super(context);requestWindowFeature(Window.FEATURE_NO_TITLE);
        width=Math.round(context.getResources().getDisplayMetrics().widthPixels*.89f);
        height=Math.round(context.getResources().getDisplayMetrics().heightPixels*.85f);
        LinearLayout panel=new LinearLayout(context);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(dp(24),dp(18),dp(24),dp(12));
        GradientDrawable background=new GradientDrawable();background.setColor(0xFC151E2A);background.setCornerRadius(dp(18));background.setStroke(dp(1),0x606A8DA5);panel.setBackground(background);
        TextView title=new TextView(context);title.setText("桌面壁纸");title.setTextSize(23);title.setTextColor(0xFFE0F3FF);panel.addView(title);
        TextView hint=new TextView(context);hint.setText("10 款纯色 · 原版蓝色玻璃 · 本地图片    选择后立即保存");hint.setTextSize(13);hint.setTextColor(0xFFABBAC9);hint.setPadding(0,dp(5),0,dp(12));panel.addView(hint);
        ScrollView scroll=new ScrollView(context);GridLayout grid=new GridLayout(context);grid.setColumnCount(4);scroll.addView(grid);
        List<WallpaperPalette.Preset> choices=new ArrayList<>(WallpaperPalette.presets());
        if(photoExists)choices.add(new WallpaperPalette.Preset(WallpaperPalette.CUSTOM,"已选本地图片",0xFF243849,true));
        choices.add(new WallpaperPalette.Preset(WallpaperPalette.PICK,"从本地选择图片",0xFF243849,true));
        if(photoExists)choices.add(new WallpaperPalette.Preset(WallpaperPalette.REMOVE,"移除本地图片",0xFF342E32,true));
        Swatch current=null;int tileWidth=(width-dp(48))/4;
        for(WallpaperPalette.Preset choice:choices){
            Swatch tile=new Swatch(context,choice,choice.id.equals(selected));tiles.add(tile);
            GridLayout.LayoutParams params=new GridLayout.LayoutParams();params.width=tileWidth;params.height=tileWidth*11/20;grid.addView(tile,params);
            tile.setOnClickListener(v->{dismiss();selection.select(choice.id);});
            if(choice.id.equals(selected))current=tile;
        }
        for(int i=0;i<tiles.size();i++){
            Swatch tile=tiles.get(i);tile.setNextFocusLeftId(tiles.get(i%4==0?i:i-1).getId());
            tile.setNextFocusRightId(tiles.get(i%4==3||i+1==tiles.size()?i:i+1).getId());
            tile.setNextFocusUpId(tiles.get(i<4?i:i-4).getId());
            tile.setNextFocusDownId(tiles.get((i/4+1)*4<tiles.size()?Math.min(i+4,tiles.size()-1):i).getId());
        }
        initial=current==null?tiles.get(0):current;
        panel.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));setContentView(panel);
        Window window=getWindow();if(window!=null){window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);WindowManager.LayoutParams attrs=window.getAttributes();attrs.dimAmount=.45f;window.setAttributes(attrs);}
    }
    @Override public void show(){super.show();Window window=getWindow();if(window!=null)window.setLayout(width,height);initial.post(()->initial.requestFocus());}
    private int dp(float value){return Math.round(value*getContext().getResources().getDisplayMetrics().density);}
    private static final class Swatch extends View {
        private final WallpaperPalette.Preset preset;
        private final boolean selected;
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Shader blue=new LinearGradient(8,8,190,75,0xFF0F5081,0xFF05224F,Shader.TileMode.CLAMP);
        Swatch(Context context,WallpaperPalette.Preset preset,boolean selected){
            super(context);this.preset=preset;this.selected=selected;setId(View.generateViewId());setFocusable(true);setClickable(true);setDefaultFocusHighlightEnabled(false);setContentDescription(preset.label+(selected?"，当前壁纸":""));
        }
        @Override protected void drawableStateChanged(){super.drawableStateChanged();invalidate();}
        @Override protected void onDraw(Canvas canvas){
            canvas.save();canvas.scale(getWidth()/200f,getHeight()/110f);
            paint.setStyle(Paint.Style.FILL);paint.setShader(preset.solid?null:blue);paint.setColor(preset.argb);canvas.drawRoundRect(9,7,191,77,10,10,paint);paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);paint.setColor(isFocused()?0xFFE1F5FF:0xFF596675);paint.setStrokeWidth(isFocused()?3:1);canvas.drawRoundRect(9,7,191,77,10,10,paint);
            paint.setStyle(Paint.Style.FILL);paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(13);paint.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));paint.setColor(isFocused()?Color.WHITE:0xFFCBD4DF);canvas.drawText(preset.label,100,99,paint);
            if(selected){paint.setTextAlign(Paint.Align.RIGHT);paint.setTextSize(12);paint.setColor(Color.WHITE);canvas.drawText("✓ 已选",180,27,paint);}
            if(WallpaperPalette.PICK.equals(preset.id)||WallpaperPalette.CUSTOM.equals(preset.id))Glyph.draw(canvas,"folder",100,44,33,0xFFCBDDEA);
            if(WallpaperPalette.REMOVE.equals(preset.id)){paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(28);canvas.drawText("×",100,53,paint);}
            canvas.restore();
        }
    }
}
