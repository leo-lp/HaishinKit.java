package com.haishinkit.rtmp.messages;

import com.haishinkit.lang.IRawValue;
import com.haishinkit.rtmp.RTMPChunk;
import com.haishinkit.rtmp.RTMPConnection;
import com.haishinkit.rtmp.RTMPSocket;
import com.haishinkit.rtmp.RTMPStream;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.HashMap;

public final class RTMPUserControlMessage extends RTMPMessage {
    private static int CAPACITY = 6;

    public enum Event implements IRawValue<Short> {
        STREAM_BEGIN((short) 0x00),
        STREAM_EOF((short) 0x01),
        STREAM_DRY((short) 0x02),
        SET_BUFFER((short) 0x03),
        RECORDED((short) 0x04),
        PING((short) 0x06),
        PONG((short) 0x07),
        BUFFER_EMPTY((short) 0x1F),
        BUFFER_FULL((short) 0x20),
        UNKNOWN((short) 0xFF);

        public static Event rawValue(final short rawValue) {
            switch (rawValue) {
                case 0x00:
                    return STREAM_BEGIN;
                case 0x01:
                    return STREAM_EOF;
                case 0x02:
                    return STREAM_DRY;
                case 0x03:
                    return SET_BUFFER;
                case 0x04:
                    return RECORDED;
                case 0x06:
                    return PING;
                case 0x07:
                    return PONG;
                case 0x1F:
                    return BUFFER_EMPTY;
                case 0x20:
                    return BUFFER_FULL;
            }
            return UNKNOWN;
        }

        private final short rawValue;

        Event(final short rawValue) {
            this.rawValue = rawValue;
        }

        public Short rawValue() {
            return rawValue;
        }
    }

    private Event event = Event.UNKNOWN;
    private int value = 0;

    public RTMPUserControlMessage() {
        super(Type.USER);
    }

    public Event getEvent() {
        return event;
    }

    public RTMPUserControlMessage setEvent(final Event event) {
        this.event = event;
        return this;
    }

    public int getValue() {
        return value;
    }

    public RTMPUserControlMessage setValue(final int value) {
        this.value = value;
        return this;
    }

    @Override
    public ByteBuffer encode(final RTMPSocket socket) {
        if (socket == null) {
            throw new IllegalArgumentException();
        }
        ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
        buffer.putShort(getEvent().rawValue());
        buffer.putInt(value);
        return buffer;
    }

    @Override
    public RTMPMessage decode(final ByteBuffer buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException();
        }
        return setEvent(Event.rawValue(buffer.getShort())).setValue(buffer.getInt());
    }

    @Override
    public RTMPMessage execute(final RTMPConnection connection) {
        switch (getEvent()) {
            case PING:
                connection.getSocket().doOutput(
                        RTMPChunk.ZERO,
                        new RTMPUserControlMessage()
                            .setEvent(Event.PONG)
                            .setChunkStreamID(RTMPChunk.CONTROL)
                );
                break;
            case BUFFER_FULL:
            case BUFFER_EMPTY:
                RTMPStream stream = connection.getStreams().get(getValue());
                if (stream != null) {
                    Map<String, Object> data = (getEvent() == Event.BUFFER_FULL) ?
                            RTMPStream.Codes.BUFFER_FLUSH.data(null) :
                            RTMPStream.Codes.BUFFER_EMPTY.data(null);
                    stream.dispatchEventWith(com.haishinkit.events.Event.RTMP_STATUS, false, data);
                }
                break;
            default:
                break;
        }
        return this;
    }
}

