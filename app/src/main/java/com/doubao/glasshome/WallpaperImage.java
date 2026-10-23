package com.doubao.glasshome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.util.AtomicFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** Private, size-bounded copy: no storage permission or long-lived URI grant required. */
final class WallpaperImage {
    static File file(Context context){return new File(context.getFilesDir(),"wallpaper.jpg");}
    static Bitmap decode(File source)throws IOException{
        if(Build.VERSION.SDK_INT>=28){
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(source),(decoder,info,src)->{
                int width=info.getSize().getWidth(),height=info.getSize().getHeight();
                float scale=Math.min(1f,Math.min(1920f/width,1080f/height));
                decoder.setTargetSize(Math.max(1,Math.round(width*scale)),Math.max(1,Math.round(height*scale)));
                decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
            });
        }
        BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(source.getPath(),bounds);
        if(bounds.outWidth<=0||bounds.outHeight<=0)throw new IOException("Invalid image");
        BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=1;
        while(bounds.outWidth/options.inSampleSize>1920||bounds.outHeight/options.inSampleSize>1080)options.inSampleSize*=2;
        Bitmap bitmap=BitmapFactory.decodeFile(source.getPath(),options);
        if(bitmap==null)throw new IOException("Cannot decode image");
        return bitmap;
    }
    static Bitmap importImage(Context context,Uri uri)throws IOException{
        File incoming=File.createTempFile("wallpaper-",".image",context.getCacheDir());
        Bitmap bitmap=null;
        try{
            try(InputStream in=context.getContentResolver().openInputStream(uri);FileOutputStream out=new FileOutputStream(incoming)){
                if(in==null)throw new IOException("No image stream");
                byte[] buffer=new byte[16384];int size=0,count;
                while((count=in.read(buffer))!=-1){size+=count;if(size>32*1024*1024)throw new IOException("Image over 32 MB");out.write(buffer,0,count);}
            }
            bitmap=decode(incoming);
            AtomicFile target=new AtomicFile(file(context));FileOutputStream output=null;
            try{
                output=target.startWrite();
                if(!bitmap.compress(Bitmap.CompressFormat.JPEG,92,output))throw new IOException("Cannot save image");
                target.finishWrite(output);
            }catch(IOException|RuntimeException error){if(output!=null)target.failWrite(output);throw error;}
            return bitmap;
        }catch(IOException|RuntimeException error){if(bitmap!=null)bitmap.recycle();throw error;}
        finally{incoming.delete();}
    }
}
