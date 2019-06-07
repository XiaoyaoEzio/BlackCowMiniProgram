package com.superyc.heiniu.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA算法工具类
 */
@Component
public class RSAUtils {
    private static final Logger LOG = LoggerFactory.getLogger(RSAUtils.class);
    private static final String ALGORITHM_WTIH_PADDING = "RSA/ECB/OAEPWITHSHA-1ANDMGF1PADDING";
    private static final String PKCS8_BEGIN = "-----BEGIN PUBLIC KEY-----\n";
    private static final String PKCS8_END = "-----END PUBLIC KEY-----";
    private static final String RSA = "RSA";

    public static String encrypt(String data, String pemPath) throws IOException {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(ALGORITHM_WTIH_PADDING);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            LOG.error(e.getMessage());
        }

        Assert.notNull(cipher);

        try {
            cipher.init(Cipher.ENCRYPT_MODE, getPublicKeyFromPem(pemPath));
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        String encryption = null;
        try {
            encryption =  Base64Utils.encodeToString(cipher.doFinal(data.getBytes()));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        }

        Assert.notNull(encryption);
        return encryption;
    }

    /**
     * 从pem文件中读取RSA public key，pem文件为PKCS#8格式
     */
    private static PublicKey getPublicKeyFromPem(String pemPath) throws IOException {
        // 读取文件内容
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(pemPath));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        String publicKeyPem = sb.toString();

        // 清理注释
        publicKeyPem = publicKeyPem.replace(PKCS8_BEGIN, "");
        publicKeyPem = publicKeyPem.replace(PKCS8_END, "");

        // 解码
        byte[] bytes = Base64Utils.decodeFromString(publicKeyPem);

        // 获取public key
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance(RSA);
        } catch (NoSuchAlgorithmException e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        }
        Assert.notNull(keyFactory);

        PublicKey publicKey = null;
        try {
            publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(bytes));
        } catch (InvalidKeySpecException e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        }

        Assert.notNull(publicKey);
        return publicKey;
    }
}
