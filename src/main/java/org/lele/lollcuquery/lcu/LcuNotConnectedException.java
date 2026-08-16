package org.lele.lollcuquery.lcu;

public class LcuNotConnectedException extends RuntimeException {

    public LcuNotConnectedException() {
        super("请先启动英雄联盟客户端");
    }
}
