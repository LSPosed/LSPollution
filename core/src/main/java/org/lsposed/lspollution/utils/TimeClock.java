package org.lsposed.lspollution.utils;

/**
 * Created by YangJing on 2019/04/19 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class TimeClock {

    private final long startTime;

    public TimeClock() {
        startTime = System.currentTimeMillis();
    }

    public String getCoast() {
        return (System.currentTimeMillis() - startTime) + "";
    }
}
