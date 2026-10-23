package com.doubao.glasshome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

final class GlassTile extends View {
    static final int INPUT=0,APP=1,ICON=2,PILL=3;
    final int kind;
    private String title,glyph,eyebrow,detail;
    private final float logicalWidth,logicalHeight;
    private final TextPaint text=new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int ink=0xFFE0F3FF;
    private final PressLogic press=new PressLogic();
    private final Runnable holdAction=()->{if(isLongClickable()&&press.hold()){setPressed(false);performLongClick();}};
    GlassTile(Context context,int kind,String title,String glyph,String eyebrow,String detail,float w,float h) {
        super(context);this.kind=kind;this.title=title;this.glyph=glyph;this.eyebrow=eyebrow;this.detail=detail;logicalWidth=w;logicalHeight=h;
        setId(View.generateViewId());setFocusable(true);setClickable(true);setSoundEffectsEnabled(true);
        setContentDescription(title+(detail.isEmpty()?"":"，"+detail));setDefaultFocusHighlightEnabled(false);
        setOnFocusChangeListener((v,focused)->{animate().scaleX(focused?1.012f:1).scaleY(focused?1.012f:1).setDuration(110).start();invalidate();});
    }
    void content(String title,String glyph,String eyebrow,String detail) {
        this.title=title;this.glyph=glyph;this.eyebrow=eyebrow;this.detail=detail;
        setContentDescription(title+(detail.isEmpty()?"":"，"+detail));invalidate();
    }
    @Override protected void drawableStateChanged(){super.drawableStateChanged();invalidate();}
    @Override public boolean onKeyDown(int code,KeyEvent event){
        if(code==KeyEvent.KEYCODE_DPAD_CENTER||code==KeyEvent.KEYCODE_ENTER){
            if(event.getRepeatCount()==0){press.start();setPressed(true);removeCallbacks(holdAction);postDelayed(holdAction,ViewConfiguration.getLongPressTimeout());event.startTracking();}
            else if(event.isLongPress())holdAction.run();
            return true;
        }
        return super.onKeyDown(code,event);
    }
    @Override public boolean onKeyLongPress(int code,KeyEvent event){
        if((code==KeyEvent.KEYCODE_DPAD_CENTER||code==KeyEvent.KEYCODE_ENTER)&&isLongClickable()){holdAction.run();return true;}
        return super.onKeyLongPress(code,event);
    }
    @Override public boolean onKeyUp(int code,KeyEvent event){
        if(code==KeyEvent.KEYCODE_DPAD_CENTER||code==KeyEvent.KEYCODE_ENTER){
            removeCallbacks(holdAction);setPressed(false);if(press.release(event.isCanceled()))performClick();return true;
        }
        return super.onKeyUp(code,event);
    }
    @Override public void onWindowFocusChanged(boolean focused){
        super.onWindowFocusChanged(focused);if(!focused){removeCallbacks(holdAction);press.cancel();setPressed(false);}
    }
    @Override protected void onDetachedFromWindow(){removeCallbacks(holdAction);press.cancel();super.onDetachedFromWindow();}
    @Override protected void onDraw(Canvas raw) {
        raw.save();raw.scale(getWidth()/logicalWidth,getHeight()/logicalHeight);
        float w=logicalWidth,h=logicalHeight,r=kind==PILL?h/2:kind==ICON?16:19;
        boolean focus=isFocused();
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(0,0,w,h,new int[]{focus?0x3859C2EE:0x1D4BA6E8,focus?0x2060B7E8:0x0D1C69BD},null,Shader.TileMode.CLAMP));
        raw.drawRoundRect(2,2,w-2,h-2,r,r,paint);paint.setShader(null);
        if(isPressed()){paint.setColor(0x25FFFFFF);raw.drawRoundRect(2,2,w-2,h-2,r,r,paint);}
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(focus?2.2f:1.1f);paint.setColor(focus?0xFFB0E8FF:0x608BC7EE);
        raw.drawRoundRect(2,2,w-2,h-2,r,r,paint);
        if(focus){paint.setStrokeWidth(1);paint.setColor(0x307DE2FF);raw.drawRoundRect(5,5,w-5,h-5,r-2,r-2,paint);}
        if(kind==INPUT) {
            label(raw,eyebrow,25,39,12,0xC9C6E7FC,false,w-50);
            label(raw,title,25,79,25,ink,true,w-113);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.3f);paint.setColor(0xB9C6E7FC);raw.drawCircle(30,h-36,5,paint);
            label(raw,detail,43,h-31,13,0xD8C5E9FF,false,w-62);
            Glyph.draw(raw,glyph,w-47,83,44,ink);
        } else if(kind==APP) {
            Glyph.draw(raw,glyph,w/2,51,43,ink);
            label(raw,title,w/2,109,15,ink,false,w-20,true);
        } else if(kind==ICON) {
            Glyph.draw(raw,glyph,w/2,h/2,29,ink);
        } else {
            label(raw,title,w/2,h/2+5,14,ink,false,w-23,true);
        }
        raw.restore();
    }
    private void label(Canvas c,String value,float x,float y,float size,int color,boolean bold,float max) {label(c,value,x,y,size,color,bold,max,false);}
    private void label(Canvas c,String value,float x,float y,float size,int color,boolean bold,float max,boolean centered) {
        text.setTextSize(size);text.setColor(color);text.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));text.setTextAlign(centered?Paint.Align.CENTER:Paint.Align.LEFT);
        String fit=TextUtils.ellipsize(value,text,Math.max(1,max),TextUtils.TruncateAt.END).toString();c.drawText(fit,x,y,text);
    }
}
