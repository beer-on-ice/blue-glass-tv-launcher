package com.doubao.glasshome;
import org.junit.Test;
import java.util.HashSet;
import static org.junit.Assert.*;
public class WallpaperPaletteTest {
    @Test public void paletteContainsTenDistinctOpaqueReadableSolids(){
        HashSet<String> ids=new HashSet<>();HashSet<Integer> colors=new HashSet<>();int count=0;
        for(WallpaperPalette.Preset p:WallpaperPalette.presets()){
            assertTrue(ids.add(p.id));
            if(!p.solid)continue;
            count++;assertEquals(255,p.argb>>>24);assertTrue(colors.add(p.argb));
            double l=.2126*linear((p.argb>>16)&255)+.7152*linear((p.argb>>8)&255)+.0722*linear(p.argb&255);
            assertTrue("white text contrast for "+p.id,1.05/(l+.05)>=4.5);
        }
        assertEquals(10,count);
    }
    @Test public void invalidAndMissingPhotoSelectionsFallBackToOriginal(){
        assertEquals(WallpaperPalette.DEFAULT,WallpaperPalette.normalize("deleted-preset",true));
        assertEquals(WallpaperPalette.DEFAULT,WallpaperPalette.normalize(WallpaperPalette.CUSTOM,false));
        assertEquals(WallpaperPalette.CUSTOM,WallpaperPalette.normalize(WallpaperPalette.CUSTOM,true));
    }
    private static double linear(int v){double s=v/255.0;return s<=.04045?s/12.92:Math.pow((s+.055)/1.055,2.4);}
}
