package com.doubao.glasshome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.view.View;

final class AmbientView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private WallpaperPalette.Preset preset=WallpaperPalette.preset(WallpaperPalette.DEFAULT);
    private Bitmap image;
    private final RectF destination=new RectF();
    private Shader base,left,right,bottom;
    AmbientView(Context context){super(context);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    void wallpaper(String id,Bitmap bitmap){preset=WallpaperPalette.preset(id);image=bitmap;imageBounds();invalidate();}
    @Override protected void onSizeChanged(int w,int h,int oldW,int oldH){
        if(w<=0||h<=0)return;
        base=new LinearGradient(0,0,w,h,new int[]{Color.rgb(15,80,129),Color.rgb(8,59,111),Color.rgb(5,34,79)},new float[]{0,.52f,1},Shader.TileMode.CLAMP);
        left=new RadialGradient(w*.16f,h*.3f,w*.72f,new int[]{0x402BBAE3,0x001B98CF},null,Shader.TileMode.CLAMP);
        right=new RadialGradient(w*.94f,h*.81f,w*.6f,new int[]{0x333E8DFA,0x002064CC},null,Shader.TileMode.CLAMP);
        bottom=new LinearGradient(0,h*.86f,0,h,new int[]{0x00021B41,0x70021B41},null,Shader.TileMode.CLAMP);
        imageBounds();
    }
    private void imageBounds(){
        if(image==null)return;
        float scale=Math.max(getWidth()/(float)image.getWidth(),getHeight()/(float)image.getHeight());
        float width=image.getWidth()*scale,height=image.getHeight()*scale;
        destination.set((getWidth()-width)/2,(getHeight()-height)/2,(getWidth()+width)/2,(getHeight()+height)/2);
    }
    @Override protected void onDraw(Canvas canvas) {
        float w=getWidth(),h=getHeight();
        if(image!=null){
            paint.setShader(null);paint.setFilterBitmap(true);canvas.drawBitmap(image,null,destination,paint);
            canvas.drawColor(0x66080D18);return;
        }
        if(preset.solid){canvas.drawColor(preset.argb);return;}
        paint.setShader(base);
        canvas.drawRect(0,0,w,h,paint);
        paint.setShader(left);
        canvas.drawRect(0,0,w,h,paint);
        paint.setShader(right);
        canvas.drawRect(0,0,w,h,paint);
        paint.setShader(bottom);
        canvas.drawRect(0,h*.86f,w,h,paint);
        paint.setShader(null);
    }
}
