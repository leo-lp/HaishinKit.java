package com.haishinkit.rtmp;

import com.haishinkit.util.Log;

import org.junit.Test;

public final class RTMPConnectionTests {
    @Test
    public void connect() {
        RTMPConnection connection = new RTMPConnection();
        //RTMPStream stream = new RTMPStream(connection);
        connection.connect("rtmp://192.168.179.3/live");
        //stream.publish("live");
        try {
            Thread.sleep(1000 * 20);
        } catch (InterruptedException e) {
            Log.i("RTMPConnection#connect", e.toString());
        }
    }
}
