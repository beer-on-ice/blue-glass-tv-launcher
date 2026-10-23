package com.doubao.glasshome;

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.*;

public class HomeLogicTest {
    @Test public void horizontalEdgesStayInTheirRow() {
        assertArrayEquals(new int[]{1,0}, HomeLogic.step(1,0,0,new int[]{2,3,6,1}));
        assertArrayEquals(new int[]{1,2}, HomeLogic.step(1,2,1,new int[]{2,3,6,1}));
    }
    @Test public void downPreservesRelativeColumnAcrossDifferentRowSizes() {
        assertArrayEquals(new int[]{2,5}, HomeLogic.step(1,2,3,new int[]{2,3,6,1}));
        assertArrayEquals(new int[]{1,1}, HomeLogic.step(2,2,2,new int[]{2,3,6,1}));
    }
    @Test public void verticalNavigationSkipsEmptyRows() {
        assertArrayEquals(new int[]{2,3}, HomeLogic.step(0,1,3,new int[]{2,0,4,1}));
        assertArrayEquals(new int[]{0,0}, HomeLogic.step(0,0,2,new int[]{2,0,4,1}));
    }
    @Test public void favoritesRemoveDuplicatesAndUnavailableApps() {
        assertEquals(Arrays.asList("b","a","c"), HomeLogic.favorites(
                Arrays.asList("gone","b","b"), Arrays.asList("a","b","c"), 6));
    }
    @Test public void favoritesRespectLimitAndEmptyCatalog() {
        assertEquals(Arrays.asList("c","a"), HomeLogic.favorites(Arrays.asList("c"),Arrays.asList("a","b","c"),2));
        assertEquals(Collections.emptyList(), HomeLogic.favorites(Arrays.asList("gone"),Collections.emptyList(),6));
    }
    @Test public void sonyAllPortsConnectedIsNotPresentedAsSignalConfirmation() {
        assertEquals(-1,HomeLogic.inputState(0,4,4));
        assertEquals(0,HomeLogic.inputState(0,4,1));
        assertEquals(2,HomeLogic.inputState(2,4,1));
    }
    @Test public void everyCecDeviceBehindAReceiverRemainsSelectable() {
        assertEquals(Arrays.asList("cec-apple","cec-console"),HomeLogic.inputTargets("hdmi-3",Arrays.asList("cec-apple","cec-console")));
        assertEquals(Arrays.asList("hdmi-2"),HomeLogic.inputTargets("hdmi-2",Collections.emptyList()));
    }
}
