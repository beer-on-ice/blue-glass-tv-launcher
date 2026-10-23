package com.doubao.glasshome;
final class PressLogic {
    private boolean pressed,held;
    void start(){pressed=true;held=false;}
    boolean hold(){if(!pressed||held)return false;held=true;return true;}
    boolean release(boolean canceled){boolean click=pressed&&!held&&!canceled;pressed=false;return click;}
    void cancel(){pressed=false;held=false;}
}
