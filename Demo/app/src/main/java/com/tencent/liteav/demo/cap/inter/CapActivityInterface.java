package com.tencent.liteav.demo.cap.inter;

import com.tencent.liteav.demo.rtcroom.RTCRoom;

/**
 * Created by jac on 2017/11/1.
 * Copyright © 2013-2017 Tencent Cloud. All Rights Reserved.
 */

public interface CapActivityInterface {
    RTCRoom getRTCRoom();
//    void    showGlobalLog(boolean enable);
//    void    printGlobalLog(String format, Object... args);
    void backToStartRecord();
    void    setTitle(String s);
}
