package com.doubao.glasshome;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

final class Glyph {
    static void draw(Canvas canvas,String kind,float cx,float cy,float size,int color) {
        canvas.save();canvas.translate(cx-size/2,cy-size/2);canvas.scale(size/48,size/48);
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.7f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
        switch(kind) {
            case "wifi":
                canvas.drawArc(3,7,45,44,220,100,false,p);canvas.drawArc(11,17,37,43,220,100,false,p);canvas.drawArc(18,27,30,39,220,100,false,p);
                p.setStyle(Paint.Style.FILL);canvas.drawCircle(24,37,1.8f,p);break;
            case "settings":
                Path gear=new Path();
                for(int i=0;i<32;i++){double angle=Math.PI*2*i/32;float radius=(i%4==0||i%4==3)?17:13.5f;float gx=24+(float)Math.cos(angle)*radius,gy=24+(float)Math.sin(angle)*radius;if(i==0)gear.moveTo(gx,gy);else gear.lineTo(gx,gy);}
                gear.close();canvas.drawPath(gear,p);canvas.drawCircle(24,24,5.2f,p);break;
            case "apps":
                for(int y=0;y<2;y++)for(int x=0;x<2;x++)canvas.drawRoundRect(8+x*19,8+y*19,21+x*19,21+y*19,2,2,p);break;
            case "folder":
                Path f=new Path();f.moveTo(6,16);f.lineTo(6,36);f.quadTo(6,39,9,39);f.lineTo(39,39);f.quadTo(42,39,42,36);f.lineTo(42,16);f.lineTo(24,16);f.lineTo(20,11);f.lineTo(9,11);f.quadTo(6,11,6,14);f.close();canvas.drawPath(f,p);canvas.drawLine(6,21,42,21,p);break;
            case "cast":
                Path cast=new Path();cast.moveTo(8,17);cast.lineTo(8,10);cast.lineTo(40,10);cast.lineTo(40,35);cast.lineTo(29,35);canvas.drawPath(cast,p);
                canvas.drawArc(-8,22,28,58,270,90,false,p);canvas.drawArc(-3,29,17,49,270,90,false,p);p.setStyle(Paint.Style.FILL);canvas.drawCircle(8,40,1.7f,p);break;
            case "music":
                canvas.drawLine(21,34,21,12,p);canvas.drawLine(21,12,36,8,p);canvas.drawLine(36,8,36,30,p);canvas.drawOval(11,30,21,38,p);canvas.drawOval(26,26,36,34,p);break;
            case "game":
                Path g=new Path();g.moveTo(15,15);g.cubicTo(8,15,3,35,7,36);g.cubicTo(11,39,16,29,19,29);g.lineTo(29,29);g.cubicTo(32,29,37,39,41,36);g.cubicTo(45,35,40,15,33,15);g.close();canvas.drawPath(g,p);canvas.drawLine(12,22,22,22,p);canvas.drawLine(17,17,17,27,p);canvas.drawCircle(31,21,1.5f,p);canvas.drawCircle(36,26,1.5f,p);break;
            case "switch":
                canvas.drawRoundRect(9,7,22,41,5,5,p);canvas.drawRoundRect(27,7,40,41,5,5,p);canvas.drawCircle(15.5f,17,3,p);canvas.drawCircle(33.5f,30,3,p);break;
            case "tv": case "bili":
                canvas.drawRoundRect(7,15,41,38,4,4,p);canvas.drawLine(17,7,24,14,p);canvas.drawLine(31,7,24,14,p);
                if(kind.equals("bili")){canvas.drawLine(17,23,17,28,p);canvas.drawLine(31,23,31,28,p);}else{canvas.drawLine(15,23,31,23,p);canvas.drawLine(15,29,25,29,p);}break;
            case "monitor":
                canvas.drawRoundRect(6,9,42,34,3,3,p);canvas.drawLine(24,34,24,40,p);canvas.drawLine(17,40,31,40,p);
                Path m=new Path();m.moveTo(21,16);m.lineTo(29,21.5f);m.lineTo(21,27);m.close();canvas.drawPath(m,p);break;
            default:
                canvas.drawCircle(24,24,17,p);Path play=new Path();play.moveTo(20,15);play.lineTo(32,24);play.lineTo(20,33);play.close();canvas.drawPath(play,p);
        }
        canvas.restore();
    }
}
