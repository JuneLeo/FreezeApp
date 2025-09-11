package com.john.adb;

import android.annotation.TargetApi;

import com.android.org.conscrypt.Conscrypt;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

@TargetApi(android.os.Build.VERSION_CODES.Q)
public class AdbPairingClient implements Closeable {

    private static final String TAG = "AdbPairClient";

    private static final byte kCurrentKeyHeaderVersion = 1;
    private static final byte kMinSupportedKeyHeaderVersion = 1;
    private static final byte kMaxSupportedKeyHeaderVersion = 1;
    private static final int kMaxPeerInfoSize = 8192;
    private static final int kMaxPayloadSize = kMaxPeerInfoSize * 2;

    private static final String kExportedKeyLabel = "adb-label\u0000";
    private static final int kExportedKeySize = 64;

    private static final int kPairingPacketHeaderSize = 6;


    private final String host;
    private final int port;
    private final String pairCode;
    private final AdbKey key;


    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;


    private final PeerInfo peerInfo;
    private PairingContext pairingContext;
    private State state = State.Ready;

    public AdbPairingClient(String host, AdbKey key, String pairCode, int port) {
        this.host = host;
        this.key = key;
        this.pairCode = pairCode;
        this.port = port;
        this.peerInfo = new PeerInfo(PeerInfo.Type.ADB_RSA_PUB_KEY.getValue(), key.getAdbPublicKey());
    }


    public boolean start() throws IOException, AdbInvalidPairingCodeException {
        try {
            setupTlsConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        state = State.ExchangingMsgs;

        if (!doExchangeMsgs()) {
            state = State.Stopped;
            return false;
        }

        state = State.ExchangingPeerInfo;

        if (!doExchangePeerInfo()) {
            state = State.Stopped;
            return false;
        }

        state = State.Stopped;
        return true;
    }


    private void setupTlsConnection() throws Exception {
        socket = new Socket(host, port);
        socket.setTcpNoDelay(true);

        SSLContext sslContext = key.getSslContext();
        SSLSocket sslSocket = (SSLSocket) sslContext.getSocketFactory().createSocket(socket, host, port, true);
        sslSocket.startHandshake();
        CommonLog.log("Handshake succeeded.");

        inputStream = new DataInputStream(sslSocket.getInputStream());
        outputStream = new DataOutputStream(sslSocket.getOutputStream());

        byte[] pairCodeBytes = pairCode.getBytes(StandardCharsets.UTF_8);
        byte[] keyMaterial = null;

        keyMaterial = Conscrypt.exportKeyingMaterial(sslSocket, kExportedKeyLabel, null, kExportedKeySize);

        ByteBuffer byteBuffer = ByteBuffer.allocate(pairCodeBytes.length + keyMaterial.length);
        byteBuffer.put(pairCodeBytes);
        byteBuffer.put(keyMaterial);

        PairingContext pairingContext = PairingContext.create(byteBuffer.array());
        this.pairingContext = pairingContext;
    }


    private PairingPacketHeader createHeader(PairingPacketHeader.Type type, int payloadSize) {
        return new PairingPacketHeader(kCurrentKeyHeaderVersion, type.value, payloadSize);
    }

    private PairingPacketHeader readHeader() throws IOException {
        byte[] bytes = new byte[kPairingPacketHeaderSize];
        inputStream.readFully(bytes);
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
        return PairingPacketHeader.readFrom(buffer);
    }

    private void writeHeader(PairingPacketHeader header, byte[] payload) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(kPairingPacketHeaderSize).order(ByteOrder.BIG_ENDIAN);
        header.writeTo(buffer);

        outputStream.write(buffer.array());
        outputStream.write(payload);
        CommonLog.log("write payload, size=${payload.size}");
    }

    private boolean doExchangeMsgs() throws IOException {
        byte[] msg = pairingContext.getMsg();
        int size = msg.length;

        PairingPacketHeader ourHeader = createHeader(PairingPacketHeader.Type.SPAKE2_MSG, size);
        writeHeader(ourHeader, msg);

        PairingPacketHeader theirHeader = readHeader();
        if (theirHeader == null) {
            return false;
        }

        if (theirHeader.type != PairingPacketHeader.Type.SPAKE2_MSG.value) return false;

        byte[] theirMessage = new byte[theirHeader.payload];
        inputStream.readFully(theirMessage);

        if (!pairingContext.initCipher(theirMessage)) return false;
        return true;
    }

    private boolean doExchangePeerInfo() throws IOException, AdbInvalidPairingCodeException {
        ByteBuffer buf = ByteBuffer.allocate(kMaxPeerInfoSize).order(ByteOrder.BIG_ENDIAN);
        peerInfo.writeTo(buf);
        byte[] outbuf = pairingContext.encrypt(buf.array());
        if (outbuf == null) {
            return false;
        }

        PairingPacketHeader ourHeader = createHeader(PairingPacketHeader.Type.PEER_INFO, outbuf.length);
        writeHeader(ourHeader, outbuf);

        PairingPacketHeader theirHeader = readHeader();
        if (theirHeader == null) {
            return false;
        }
        if (theirHeader.type != PairingPacketHeader.Type.PEER_INFO.value) return false;

        byte[] theirMessage = new byte[theirHeader.payload];
        inputStream.readFully(theirMessage);

        byte[] decrypted = pairingContext.decrypt(theirMessage);
        if (decrypted == null) {
            throw new AdbInvalidPairingCodeException();
        }

        if (decrypted.length != kMaxPeerInfoSize) {
            CommonLog.error("Got size=${decrypted.size} PeerInfo.size=$kMaxPeerInfoSize");
            return false;
        }
        PeerInfo theirPeerInfo = PeerInfo.readFrom(ByteBuffer.wrap(decrypted));
        CommonLog.log(theirPeerInfo.toString());
        return true;
    }

    @Override
    public void close() {
        try {
            inputStream.close();
        } catch (Throwable e) {
        }
        try {
            outputStream.close();
        } catch (Throwable e) {
        }
        try {
            socket.close();
        } catch (Exception e) {
        }

        if (state != State.Ready) {
            pairingContext.destroy();
        }
    }


    private enum State {
        Ready,
        ExchangingMsgs,
        ExchangingPeerInfo,
        Stopped
    }


    private static class PeerInfo {
        private byte type;
        private byte[] data = new byte[kMaxPeerInfoSize - 1];

        public PeerInfo(byte type, byte[] data) {
            this.type = type;
            System.arraycopy(data, 0, this.data, 0, Math.min(data.length, this.data.length));
        }

        enum Type {
            ADB_RSA_PUB_KEY((byte) 0),
            ADB_DEVICE_GUID((byte) 0);

            byte value;

            Type(byte value) {
                this.value = value;
            }

            byte getValue() {
                return value;
            }
        }

        void writeTo(ByteBuffer buffer) {
            buffer.put(type);
            buffer.put(data);

            CommonLog.log("write PeerInfo ${toStringShort()}");
        }

        @Override
        public String toString() {
            return "PeerInfo{" +
                    "data=" + Arrays.toString(data) +
                    ", type=" + type +
                    '}';
        }

        public static PeerInfo readFrom(ByteBuffer buffer) {
            byte type = buffer.get();
            byte[] data = new byte[kMaxPeerInfoSize - 1];
            buffer.get(data);
            return new PeerInfo(type, data);
        }
    }

    private static class PairingPacketHeader {
        private byte version;
        private byte type;
        private int payload;

        public PairingPacketHeader(byte version, byte type, int payload) {
            this.payload = payload;
            this.type = type;
            this.version = version;
        }

        enum Type {
            SPAKE2_MSG((byte) 0),
            PEER_INFO((byte) 1);

            byte value;

            Type(byte value) {
                this.value = value;
            }
        }

        void writeTo(ByteBuffer buffer) {
            buffer.put(version);
            buffer.put(type);
            buffer.putInt(payload);

            CommonLog.log("write PairingPacketHeader ${toStringShort()}");
        }

        @Override
        public String toString() {
            return "PairingPacketHeader{" +
                    "payload=" + payload +
                    ", version=" + version +
                    ", type=" + type +
                    '}';
        }

        public static PairingPacketHeader readFrom(ByteBuffer buffer) {
            byte version = buffer.get();
            byte type = buffer.get();
            int payload = buffer.getInt();

            if (version < kMinSupportedKeyHeaderVersion || version > kMaxSupportedKeyHeaderVersion) {
                CommonLog.error("PairingPacketHeader version mismatch");
                return null;
            }
            if (type != Type.SPAKE2_MSG.value && type != Type.PEER_INFO.value) {
                CommonLog.error("Unknown PairingPacket type=${type}");
                return null;
            }
            if (payload <= 0 || payload > kMaxPayloadSize) {
                CommonLog.error("header payload not within a safe payload size (size=${payload})");
                return null;
            }

            PairingPacketHeader header = new PairingPacketHeader(version, type, payload);
            CommonLog.log("read PairingPacketHeader ${header.toStringShort()}");
            return header;
        }
    }
}
