package com.john.adb;

import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Base64;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;


public class AdbKey {
    private final String name;
    private AdbKeyStore adbKeyStore;
    private Key encryptionKey;
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;
    private X509Certificate certificate;
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String ENCRYPTION_KEY_ALIAS = "_adbkey_encryption_key_";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_SIZE_IN_BYTES = 12;
    private static final int TAG_SIZE_IN_BYTES = 16;
    private static final byte[] PADDING = new byte[]{
            0x00, 0x01, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0x00,
            0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2b, 0x0e, 0x03, 0x02, 0x1a, 0x05, 0x00,
            0x04, 0x14
    };


    public AdbKey(AdbKeyStore adbKeyStore, final String name) {
        this.adbKeyStore = adbKeyStore;
        this.name = name;
        this.encryptionKey = getOrCreateEncryptionKey();
        if (this.encryptionKey == null) {
            throw new RuntimeException("Failed to generate encryption key with AndroidKeyManager.");
        }
        this.privateKey = getOrCreatePrivateKey();

        try {
            this.publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(privateKey.getModulus(), RSAKeyGenParameterSpec.F4));
            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey);
            X509CertificateHolder x509Certificate = new X509v3CertificateBuilder(new X500Name("CN=00"),
                    BigInteger.ONE,
                    new Date(0),
                    new Date(2461449600L * 1000),
                    Locale.ROOT,
                    new X500Name("CN=00"),
                    SubjectPublicKeyInfo.getInstance(publicKey.getEncoded())
            ).build(signer);
            this.certificate = (X509Certificate) CertificateFactory.getInstance("X.509")
                    .generateCertificate(new ByteArrayInputStream(x509Certificate.getEncoded()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        CommonLog.log(privateKey.toString());

    }

    public final byte[] getAdbPublicKey() {
        return adbEncoded(publicKey, name);
    }

    public byte[] sign(byte[] data) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            cipher.update(PADDING);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    public SSLContext getSslContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
            sslContext.init(new KeyManager[]{
                    new X509ExtendedKeyManager() {

                        private String alias = "key";

                        @Override
                        public String[] getClientAliases(String keyType, Principal[] issuers) {
                            return new String[0];
                        }

                        @Override
                        public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
                            CommonLog.log("chooseClientAlias: keyType=${keyTypes.contentToString()}, issuers=${issuers?.contentToString()}");
                            for (String s : keyType) {
                                if (TextUtils.equals("RSA", s)) return alias;
                            }
                            return null;
                        }

                        @Override
                        public String[] getServerAliases(String keyType, Principal[] issuers) {
                            return new String[0];
                        }

                        @Override
                        public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
                            return "";
                        }

                        @Override
                        public X509Certificate[] getCertificateChain(String alias) {
                            CommonLog.log("getCertificateChain: alias=$alias");
                            return TextUtils.equals(alias, this.alias) ? new X509Certificate[]{
                                    certificate
                            } : null;
                        }

                        @Override
                        public PrivateKey getPrivateKey(String alias) {
                            CommonLog.log("getPrivateKey: alias=$alias");
                            return TextUtils.equals(alias, this.alias) ? privateKey : null;
                        }
                    }
            }, new TrustManager[]{
                    new X509ExtendedTrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {

                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {

                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {

                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {

                        }
                    }
            }, new SecureRandom());
            return sslContext;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] decrypt(byte[] ciphertext, byte[] aad) {
        if (ciphertext.length < IV_SIZE_IN_BYTES + TAG_SIZE_IN_BYTES) {
            return null;
        }
        try {
            GCMParameterSpec params = new GCMParameterSpec(8 * TAG_SIZE_IN_BYTES, ciphertext, 0, IV_SIZE_IN_BYTES);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, params);
            cipher.updateAAD(aad);
            return cipher.doFinal(ciphertext, IV_SIZE_IN_BYTES, ciphertext.length - IV_SIZE_IN_BYTES);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private RSAPrivateKey getOrCreatePrivateKey() {
        RSAPrivateKey privateKey = null;


        byte[] bytes = "adbkey".getBytes(StandardCharsets.UTF_8);
        ByteBuffer allocate = ByteBuffer.allocate(16);
        allocate.put(bytes);
        byte[] aad = allocate.array();

        byte[] ciphertext = adbKeyStore.get();
        if (ciphertext != null) {
            try {
                byte[] plaintext = decrypt(ciphertext, aad);

                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                privateKey = (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(plaintext));
            } catch (Exception e) {
            }
        }
        if (privateKey == null) {
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA);
                keyPairGenerator.initialize(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4));
                KeyPair keyPair = keyPairGenerator.generateKeyPair();
                privateKey = (RSAPrivateKey) keyPair.getPrivate();

                ciphertext = encrypt(privateKey.getEncoded(), aad);
                if (ciphertext != null) {
                    adbKeyStore.put(ciphertext);
                }
            } catch (Exception e) {
                //
            }
        }
        return privateKey;
    }

    private byte[] encrypt(byte[] plaintext, byte[] aad) {
        if (plaintext.length > Integer.MAX_VALUE - IV_SIZE_IN_BYTES - TAG_SIZE_IN_BYTES) {
            return null;
        }
        byte[] ciphertext = new byte[IV_SIZE_IN_BYTES + plaintext.length + TAG_SIZE_IN_BYTES];
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
            cipher.updateAAD(aad);
            cipher.doFinal(plaintext, 0, plaintext.length, ciphertext, IV_SIZE_IN_BYTES);
            System.arraycopy(cipher.getIV(), 0, ciphertext, 0, IV_SIZE_IN_BYTES);
            return ciphertext;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Key getOrCreateEncryptionKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);

            Key key = keyStore.getKey(ENCRYPTION_KEY_ALIAS, null);

            if (key != null) {
                return key;
            }

            KeyGenParameterSpec parameterSpec = new KeyGenParameterSpec.Builder(ENCRYPTION_KEY_ALIAS, KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build();
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
            keyGenerator.init(parameterSpec);
            return keyGenerator.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final int ANDROID_PUBKEY_MODULUS_SIZE = 2048 / 8;
    private static final int ANDROID_PUBKEY_MODULUS_SIZE_WORDS = ANDROID_PUBKEY_MODULUS_SIZE / 4;
    private static final int RSAPublicKey_Size = 524;


    private int[] toAdbEncoded(BigInteger bigInteger) {

        int[] endcoded = new int[ANDROID_PUBKEY_MODULUS_SIZE_WORDS];
        BigInteger r32 = BigInteger.ZERO.setBit(32);

        BigInteger tmp = bigInteger.add(BigInteger.ZERO);

        for (int i = 0; i < ANDROID_PUBKEY_MODULUS_SIZE_WORDS; i++) {
            BigInteger[] out = tmp.divideAndRemainder(r32);
            tmp = out[0];
            endcoded[i] = out[1].intValue();
        }
        return endcoded;
    }

    private byte[] adbEncoded(RSAPublicKey publicKey, String name) {

        BigInteger r32 = BigInteger.ZERO.setBit(32);
        BigInteger n0inv = publicKey.getModulus().remainder(r32).modInverse(r32).negate();
        BigInteger r = BigInteger.ZERO.setBit(ANDROID_PUBKEY_MODULUS_SIZE * 8);
        BigInteger rr = r.modPow(BigInteger.valueOf(2), publicKey.getModulus());

        ByteBuffer buffer = ByteBuffer.allocate(RSAPublicKey_Size).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(ANDROID_PUBKEY_MODULUS_SIZE_WORDS);
        buffer.putInt(n0inv.intValue());
        for (int j : toAdbEncoded(publicKey.getModulus())) {
            buffer.putInt(j);
        }

        for (int j : toAdbEncoded(rr)) {
            buffer.putInt(j);
        }

        buffer.putInt(publicKey.getPublicExponent().intValue());

        byte[] base64Bytes = Base64.encode(buffer.array(), Base64.NO_WRAP);
        byte[] nameBytes = (' ' + name + '\u0000').getBytes(StandardCharsets.UTF_8);
        ByteBuffer byteBuffer = ByteBuffer.allocate(base64Bytes.length + nameBytes.length);
        byteBuffer.put(base64Bytes);
        byteBuffer.put(nameBytes);

        return byteBuffer.array();
    }

    public interface AdbKeyStore {
        void put(byte[] bytes);

        byte[] get();
    }


    public static class PreferenceAdbKeyStore implements AdbKeyStore {

        SharedPreferences preference;

        public PreferenceAdbKeyStore(SharedPreferences preference) {
            this.preference = preference;
        }

        private static final String preferenceKey = "adbkey";


        @Override
        public void put(byte[] bytes) {
            preference.edit().putString(preferenceKey, new String(Base64.encode(bytes, Base64.NO_WRAP), StandardCharsets.UTF_8)).apply();
        }

        @Override
        public byte[] get() {
            if (!preference.contains(preferenceKey)) return null;
            return Base64.decode(preference.getString(preferenceKey, null), Base64.NO_WRAP);
        }
    }
}
