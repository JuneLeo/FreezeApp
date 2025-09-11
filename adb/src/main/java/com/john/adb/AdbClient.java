package com.john.adb;

import static com.john.adb.AdbProtocol.ADB_AUTH_RSAPUBLICKEY;
import static com.john.adb.AdbProtocol.ADB_AUTH_SIGNATURE;
import static com.john.adb.AdbProtocol.ADB_AUTH_TOKEN;
import static com.john.adb.AdbProtocol.A_AUTH;
import static com.john.adb.AdbProtocol.A_CLSE;
import static com.john.adb.AdbProtocol.A_CNXN;
import static com.john.adb.AdbProtocol.A_MAXDATA;
import static com.john.adb.AdbProtocol.A_OKAY;
import static com.john.adb.AdbProtocol.A_OPEN;
import static com.john.adb.AdbProtocol.A_STLS;
import static com.john.adb.AdbProtocol.A_STLS_VERSION;
import static com.john.adb.AdbProtocol.A_VERSION;
import static com.john.adb.AdbProtocol.A_WRTE;

import android.os.Build;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

public class AdbClient implements Closeable {
    private String host;
    private int port;
    private AdbKey key;

    public AdbClient(String host, int port, AdbKey key) {
        this.host = host;
        this.key = key;
        this.port = port;
        CommonLog.log("AdbClient host =" + host + ", port = " + port);
    }


    private Socket socket;
    private DataInputStream plainInputStream;
    private DataOutputStream plainOutputStream;

    private boolean useTls = false;

    private SSLSocket tlsSocket;
    private DataInputStream tlsInputStream;
    private DataOutputStream tlsOutputStream;

    private DataInputStream getInputStream() {
        return useTls ? tlsInputStream : plainInputStream;
    }

    private DataOutputStream getOutputStream() {
        return useTls ? tlsOutputStream : plainOutputStream;
    }

    private void write(int command, int arg0, int arg1, byte[] data) throws IOException {
        write(new AdbMessage(command, arg0, arg1, data));
    }

    private void write(AdbMessage message) throws IOException {
        OutputStream outputStream = getOutputStream();
        outputStream.write(message.toByteArray());
        outputStream.flush();
        CommonLog.log("write=" + message.toStringShort());
    }

    private void write(int command, int arg0, int arg1, String data) throws IOException {
        write(new AdbMessage(command, arg0, arg1, data));
    }

    private AdbMessage read() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(AdbMessage.HEADER_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        DataInputStream inputStream = getInputStream();

        inputStream.readFully(buffer.array(), 0, 24);

        int command = buffer.getInt();
        int arg0 = buffer.getInt();
        int arg1 = buffer.getInt();
        int dataLength = buffer.getInt();
        int checksum = buffer.getInt();
        int magic = buffer.getInt();
        byte[] data;
        if (dataLength >= 0) {
            data = new byte[dataLength];
            inputStream.readFully(data, 0, dataLength);
        } else {
            data = null;
        }
        AdbMessage message = new AdbMessage(command, arg0, arg1, dataLength, checksum, magic, data);
        message.validateOrThrow();
        CommonLog.log( "read " + message.toStringShort());
        return message;
    }

    public void connect() throws IOException, IllegalStateException {

        CommonLog.log("AdbClient host =" + host + ", port = " + port);
        socket = new Socket(host, port);
        socket.setTcpNoDelay(true);
        plainInputStream = new DataInputStream(socket.getInputStream());
        plainOutputStream = new DataOutputStream(socket.getOutputStream());

        write(A_CNXN, A_VERSION, A_MAXDATA, "host::");

        AdbMessage message = read();
        if (message.getCommand() == A_STLS) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                throw new IllegalStateException("Connect to adb with TLS is not supported before Android 9");
            }
            write(A_STLS, A_STLS_VERSION, 0, (byte[]) null);

            SSLContext sslContext = key.getSslContext();
            tlsSocket = (SSLSocket) sslContext.getSocketFactory().createSocket(socket, host, port, true);
            tlsSocket.startHandshake();
            CommonLog.log( "Handshake succeeded.");

            tlsInputStream = new DataInputStream(tlsSocket.getInputStream());
            tlsOutputStream = new DataOutputStream(tlsSocket.getOutputStream());
            useTls = true;

            message = read();
        } else if (message.getCommand() == A_AUTH) {
            if (message.getCommand() != A_AUTH && message.getArg0() != ADB_AUTH_TOKEN) {
                throw new IllegalStateException("not A_AUTH ADB_AUTH_TOKEN");
            }
            try {
                write(A_AUTH, ADB_AUTH_SIGNATURE, 0, key.sign(message.getData()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            message = read();
            if (message.getCommand() != A_CNXN) {
                write(A_AUTH, ADB_AUTH_RSAPUBLICKEY, 0, key.getAdbPublicKey());
                message = read();
            }
        }

        if (message.getCommand() != A_CNXN) {
            throw new IllegalStateException("not A_CNXN");
        }
    }

    @Override
    public void close() throws IOException {
        if (plainInputStream != null) {
            try {
                plainInputStream.close();
                plainInputStream = null;
            } catch (Throwable e) {
            }
        }
        if (plainOutputStream != null) {
            try {
                plainOutputStream.close();
                plainOutputStream = null;
            } catch (Throwable e) {
            }
        }
        if (socket != null) {
            try {
                socket.close();
                socket = null;
            } catch (Throwable e) {
            }
        }

        if (useTls) {
            if (tlsInputStream != null) {
                try {
                    tlsInputStream.close();
                    tlsInputStream = null;
                } catch (Throwable e) {
                }
            }
            if (tlsOutputStream != null) {
                try {
                    tlsOutputStream.close();
                    tlsOutputStream = null;
                } catch (Throwable e) {
                }
            }
            if (tlsSocket != null) {
                try {
                    tlsSocket.close();
                    tlsSocket = null;
                } catch (Throwable e) {
                }
            }
        }
    }

    public interface ShellCommandCallback {
        void callback(byte[] bytes);
    }

    public void shellCommand(String command, ShellCommandCallback callback) throws IOException, IllegalStateException {
        int localId = 1;
        write(A_OPEN, localId, 0, "shell:" + command);

        AdbMessage message = read();
        switch (message.getCommand()) {
            case A_OKAY: {
                while (true) {
                    message = read();
                    int remoteId = message.getArg0();
                    if (message.getCommand() == A_WRTE) {
                        if (message.getData_length() > 0) {
                            callback.callback(message.getData());
                        }
                        write(A_OKAY, localId, remoteId, (byte[]) null);
                    } else if (message.getCommand() == A_CLSE) {
                        write(A_CLSE, localId, remoteId, (byte[]) null);
                        break;
                    } else {
                        throw new IllegalStateException("not A_WRTE or A_CLSE");
                    }
                }
            }
            break;
            case A_CLSE: {
                int remoteId = message.getArg0();
                write(A_CLSE, localId, remoteId, (byte[]) null);
            }
            break;
            default:
                throw new IllegalStateException("not A_OKAY or A_CLSE");
        }
    }

}
