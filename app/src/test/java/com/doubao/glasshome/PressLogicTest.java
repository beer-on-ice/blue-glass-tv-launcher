package com.doubao.glasshome;
import org.junit.Test;
import static org.junit.Assert.*;
public class PressLogicTest {
    @Test public void quickPressClicksOnlyOnRelease(){PressLogic p=new PressLogic();p.start();assertTrue(p.release(false));assertFalse(p.release(false));}
    @Test public void longPressRunsOnceAndNeverBecomesAShortClick(){PressLogic p=new PressLogic();p.start();assertTrue(p.hold());assertFalse(p.hold());assertFalse(p.release(false));}
    @Test public void canceledOrFocusLostPressCannotLaunch(){PressLogic p=new PressLogic();p.start();assertFalse(p.release(true));p.start();p.cancel();assertFalse(p.release(false));}
}
