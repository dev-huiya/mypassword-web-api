package me.huiya.project.Encrypt;

import me.huiya.core.Common.Common;
import me.huiya.core.Encrypt.AES256Util;
import me.huiya.core.Encrypt.SHA256Util;
import me.huiya.core.Entity.Token;
import me.huiya.core.Repository.TokenRepository;
import org.springframework.beans.factory.annotation.Value;

import java.beans.ConstructorProperties;

public class Encrypt {

    private static String AES_IV;

    @Value("${core.AES-iv}")
    public void setIV(String iv) { AES_IV = iv; }

    public static String encrypt(String plainText, Token token) {
        return encrypt(plainText, token.getPublicKey());
    }

    public static String encrypt(String plainText, String publicKey) {

        System.out.println("encrypted key: " + plainText);
        System.out.println("public key: " + publicKey);
        System.out.println("public hashed: " + SHA256Util.encrypt(publicKey));
        System.out.println("public hashed 32: " + SHA256Util.encrypt(publicKey).substring(0, 32));

        System.out.println("encryptKey: " + AES256Util.decrypt(plainText, SHA256Util.encrypt(publicKey).substring(0, 32), AES_IV));

        // 퍼블릭키를 해시한 후 32바이트로 잘라내어 사용함.
        return AES256Util.encrypt(
            plainText,
            SHA256Util.encrypt(publicKey).substring(0, 32),
            AES_IV
        );
    }

    public static String decrypt(String encryptedText, Token token) {
        return decrypt(encryptedText, token.getPublicKey());
    }

    public static String decrypt(String encryptedText, String publicKey) {
        return AES256Util.decrypt(
            encryptedText,
            SHA256Util.encrypt(publicKey).substring(0, 32),
            AES_IV
        );
    }
}
