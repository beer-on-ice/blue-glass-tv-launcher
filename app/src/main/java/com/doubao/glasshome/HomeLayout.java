package com.doubao.glasshome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

final class HomeLayout extends ViewGroup {
    private static final class Slot {
        final View view;final float x,y,w,h;
        Slot(View view,float x,float y,float w,float h){this.view=view;this.x=x;this.y=y;this.w=w;this.h=h;}
    }
    private final List<Slot> slots=new ArrayList<>();
    private final List<List<GlassTile>> rows=new ArrayList<>();
    private final AmbientView background;
    private final Labels labels;
    HomeLayout(Context context){
        super(context);setClipChildren(false);setClipToPadding(false);
        background=new AmbientView(context);labels=new Labels(context);addView(background);addView(labels);
        for(int i=0;i<5;i++) rows.add(new ArrayList<>());
    }
    void addTile(GlassTile tile,int row,float x,float y,float w,float h){
        rows.get(row).add(tile);slots.add(new Slot(tile,x,y,w,h));addView(tile);
    }
    void information(String room,String time,String date,String network){labels.room=room;labels.time=time;labels.date=date;labels.network=network;labels.invalidate();}
    void wallpaper(String id,android.graphics.Bitmap image){background.wallpaper(id,image);}
    void initialFocus(){if(!rows.get(2).isEmpty())rows.get(2).get(0).requestFocus();}
    boolean move(int direction){
        View focus=findFocus();
        for(int r=0;r<rows.size();r++){
            int col=rows.get(r).indexOf(focus);if(col<0)continue;
            int[] sizes=new int[rows.size()];for(int i=0;i<sizes.length;i++)sizes[i]=rows.get(i).size();
            int[] target=HomeLogic.step(r,col,direction,sizes);
            rows.get(target[0]).get(target[1]).requestFocus();return true;
        }
        return false;
    }
    @Override protected void onMeasure(int wSpec,int hSpec){
        int width=MeasureSpec.getSize(wSpec),height=MeasureSpec.getSize(hSpec);setMeasuredDimension(width,height);
        background.measure(MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY));
        labels.measure(MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY));
        float scale=Math.min(width/1280f,height/720f);
        for(Slot slot:slots)slot.view.measure(MeasureSpec.makeMeasureSpec(Math.round(slot.w*scale),MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(Math.round(slot.h*scale),MeasureSpec.EXACTLY));
    }
    @Override protected void onLayout(boolean changed,int l,int t,int r,int b){
        int width=r-l,height=b-t;background.layout(0,0,width,height);labels.layout(0,0,width,height);
        float scale=Math.min(width/1280f,height/720f),dx=(width-1280*scale)/2,dy=(height-720*scale)/2;
        for(Slot slot:slots){int x=Math.round(dx+slot.x*scale),y=Math.round(dy+slot.y*scale);slot.view.layout(x,y,x+slot.view.getMeasuredWidth(),y+slot.view.getMeasuredHeight());}
    }
    private static final class Labels extends View {
        String room="客厅",time="",date="",network="";
        private final TextPaint p=new TextPaint(Paint.ANTI_ALIAS_FLAG);
        Labels(Context context){super(context);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(Canvas c){
            float s=Math.min(getWidth()/1280f,getHeight()/720f);c.save();c.translate((getWidth()-1280*s)/2,(getHeight()-720*s)/2);c.scale(s,s);
            text(c,room,64,89,36,0xFFE0F3FF,true,450);
            p.setTextSize(10);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));p.setColor(0xBCD4ECFA);
            String word="BRAVIA / HOME";float x=66;
            for(int i=0;i<word.length();i++){String ch=word.substring(i,i+1);c.drawText(ch,x,115,p);x+=p.measureText(ch)+2.1f;}
            text(c,time,936,80,29,0xFFE0F3FF,false,134);
            text(c,date,937,104,12,0xC5D2ECFC,false,140);
            text(c,"输入设备",64,179,18,0xE8D5EDFC,true,300);
            text(c,"常用应用",64,418,18,0xE8D5EDFC,true,400);
            text(c,"长按输入卡片改名 · 长按应用卡片更换",64,638,12,0x80CBE7F9,false,600);
            text(c,network,64,686,11,0x88C5E2F6,false,500);
            Paint line=new Paint(Paint.ANTI_ALIAS_FLAG);line.setColor(0x1672B3E7);c.drawLine(64,663,1216,663,line);
            text(c,"BLUE GLASS  /  1.1.0",1090,686,10,0x65C5E2F6,false,170);
            c.restore();
        }
        private void text(Canvas c,String s,float x,float y,float size,int color,boolean bold,float max){
            p.setTextSize(size);p.setColor(color);p.setTypeface(Typeface.create(bold?"sans-serif-medium":"sans-serif",Typeface.NORMAL));
            c.drawText(TextUtils.ellipsize(s,p,max,TextUtils.TruncateAt.END).toString(),x,y,p);
        }
    }
}
