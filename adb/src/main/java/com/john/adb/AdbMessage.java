package com.john.adb;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AdbMessage {
    private int command;
    private int arg0;
    private int arg1;
    private int data_length;
    private int data_crc32;
    private int magic;
    private byte[] data;
    public static final int HEADER_LENGTH = 24;

    public AdbMessage(int command, int arg0, int arg1, int data_length, int data_crc32, int magic, byte[] data) {
        this.command = command;
        this.arg0 = arg0;
        this.arg1 = arg1;
        this.data_length = data_length;
        this.data_crc32 = data_crc32;
        this.magic = magic;
        this.data = data;
    }

    public AdbMessage(int command, int arg0, int arg1, String data) {
        this(command, arg0, arg1, (data + '\u0000').getBytes(StandardCharsets.UTF_8));
    }

    public AdbMessage(int command, int arg0, int arg1, byte[] data) {

        this(command, arg0, arg1, data != null ? data.length : 0, crc32(data), (int) ((long) command ^ 0xFFFFFFFFL), data);
    }

    public final int getCommand() {
        return this.command;
    }

    public final int getArg0() {
        return this.arg0;
    }

    public final int getArg1() {
        return this.arg1;
    }

    public final int getData_length() {
        return this.data_length;
    }

    public final int getData_crc32() {
        return this.data_crc32;
    }

    public final int getMagic() {
        return this.magic;
    }

    public final byte[] getData() {
        return this.data;
    }


    public final boolean validate() {
        if (this.command != ~this.magic) {
            return false;
        } else {
            return this.data_length == 0 || crc32(this.data) == this.data_crc32;
        }
    }

    public final void validateOrThrow() {
        if (!this.validate()) {
            throw new IllegalArgumentException("bad message " + this.toStringShort());
        }
    }


    public final byte[] toByteArray() {

        int length = 24 + (this.data != null ? this.data.length : 0);

        ByteBuffer byteBuffer = ByteBuffer.allocate(length);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(command);
        byteBuffer.putInt(arg0);
        byteBuffer.putInt(arg1);
        byteBuffer.putInt(data_length);
        byteBuffer.putInt(data_crc32);
        byteBuffer.putInt(magic);
        if (data != null) {
            byteBuffer.put(data);
        }

        return byteBuffer.array();
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AdbMessage)) return false;

        AdbMessage other = ((AdbMessage) obj);

        if (command != other.command) return false;
        if (arg0 != other.arg0) return false;
        if (arg1 != other.arg1) return false;
        if (data_length != other.data_length) return false;
        if (data_crc32 != other.data_crc32) return false;
        if (magic != other.magic) return false;
        if (data != null) {
            if (other.data == null) return false;
            return Arrays.equals(data, other.data);
        } else return other.data == null;
    }


    @Override
    public int hashCode() {
        int result = command;
        result = 31 * result + arg0;
        result = 31 * result + arg1;
        result = 31 * result + data_length;
        result = 31 * result + data_crc32;
        result = 31 * result + magic;
        result = 31 * result + (data != null ? Arrays.hashCode(data) : 0);
        return result;
    }

    @Override
    public String toString() {
        return toStringShort();
    }

    public String toStringShort() {
        String label = "";
        switch (command) {
            case AdbProtocol.A_SYNC:
                label = "A_SYNC";
                break;
            case AdbProtocol.A_CNXN:
                label = "A_CNXN";
                break;
            case AdbProtocol.A_AUTH:
                label = "A_AUTH";
                break;
            case AdbProtocol.A_OPEN:
                label = "A_OPEN";
                break;
            case AdbProtocol.A_OKAY:
                label = "A_OKAY";
                break;
            case AdbProtocol.A_CLSE:
                label = "A_CLSE";
                break;
            case AdbProtocol.A_WRTE:
                label = "A_WRTE";
                break;
            case AdbProtocol.A_STLS:
                label = "A_STLS";
                break;
            default:
                label = String.valueOf(command);
                break;

        }
        return "AdbMessage{" +
                "arg0=" + arg0 +
                ", command=" + label +
                ", arg1=" + arg1 +
                ", data_length=" + data_length +
                ", data_crc32=" + data_crc32 +
                ", magic=" + magic +
                ", data=" + Arrays.toString(data) +
                '}';
    }

    private static int crc32(byte[] data) {
        if (data == null) {
            return 0;
        } else {
            int res = 0;
            int var3 = 0;

            for (int var4 = data.length; var3 < var4; ++var3) {
                byte b = data[var3];
                if (b >= 0) {
                    res += b;
                } else {
                    res += b + 256;
                }
            }

            return res;
        }
    }
}
