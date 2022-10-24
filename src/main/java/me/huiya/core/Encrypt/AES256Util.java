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

    public static String encrypt(String planText) {
        if(planText == null || planText == "") {
            return planText;
        }

        Key key = new SecretKeySpec(KEY, "AES");
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV));
            return new String(Base64.getEncoder().encode(cipher.doFinal(planText.getBytes())));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(String encryptedText) {
        if(encryptedText == null || encryptedText == "") {
            return encryptedText;
        }

        Key key = new SecretKeySpec(KEY, "AES");
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV));
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedText)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
