package me.huiya.core.Encrypt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

@Component
public class AES256Util {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static byte[] KEY;
    private static byte[] IV;

    @Value("${core.AES-key}")
    public void setKEY(String key) {
        KEY = key.getBytes();
    }

    @Value("${core.AES-iv}")
    public void setIV(String iv) { IV = iv.getBytes(); }

    public static String encrypt(String plainText) {
        return _encrypt(plainText, KEY, IV);
    }

    public static String encrypt(String plainText, String key, String iv) {
        return _encrypt(plainText, key.getBytes(), iv.getBytes());
    }

    private static String _encrypt(String plainText, byte[] _key, byte[] _iv) {
        if(plainText == null || plainText == "") {
            return plainText;
        }

        Key key = new SecretKeySpec(_key, "AES");
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(_iv));
            return new String(Base64.getEncoder().encode(cipher.doFinal(plainText.getBytes())));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(String encryptedText) {
        return _decrypt(encryptedText, KEY, IV);
    }

    public static String decrypt(String encryptedText, String key, String iv) {
        return _decrypt(encryptedText, key.getBytes(), iv.getBytes());
    }

    private static String _decrypt(String encryptedText, byte[] _key, byte[] _iv) {
        if(encryptedText == null || encryptedText == "") {
            return encryptedText;
        }

        Key key = new SecretKeySpec(_key, "AES");
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(_iv));
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedText)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
