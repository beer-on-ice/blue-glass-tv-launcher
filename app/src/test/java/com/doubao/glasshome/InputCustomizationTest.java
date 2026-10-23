package com.doubao.glasshome;
import org.junit.Test;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
public class InputCustomizationTest {
    @Test public void portNumbersSortNaturallyRatherThanByConnectionOrText(){
        List<String> ports=new ArrayList<>(Arrays.asList("HDMI 3 (eARC/ARC)","HDMI 10","HDMI 2","HDMI 1"));
        ports.sort(HomeLogic::comparePorts);
        assertEquals(Arrays.asList("HDMI 1","HDMI 2","HDMI 3 (eARC/ARC)","HDMI 10"),ports);
    }
    @Test public void missingPortNumberSortsAfterNumberedPorts(){assertTrue(HomeLogic.comparePorts("HDMI 4","输入设备")<0);}
    @Test public void multipleCecChildrenDoNotDuplicateHomePortCards(){assertEquals(Arrays.asList(0,2,3,5),HomeLogic.firstPerPort(Arrays.asList("port1","port1","port2","port3","port3","port4")));}
    @Test public void clearingNicknameRestoresSystemName(){assertEquals("ATV",HomeLogic.inputName("  ","ATV"));assertEquals("Apple TV",HomeLogic.inputName("  Apple TV  ","ATV"));}
    @Test public void longNicknameDoesNotSplitEmoji(){String name=String.join("",java.util.Collections.nCopies(30,"🎮"));String safe=HomeLogic.inputName(name,"ATV");assertEquals(24,safe.codePointCount(0,safe.length()));assertFalse(Character.isHighSurrogate(safe.charAt(safe.length()-1)));}
}
