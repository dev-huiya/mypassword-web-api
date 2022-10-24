package me.huiya.core.Encrypt;

import java.security.*;
import java.util.Base64;

public class RSAUtils {

    private PublicKey publicKey;
    private PrivateKey privateKey;

    public RSAUtils() {
            try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();

            publicKey = kp.getPublic();
            privateKey = kp.getPrivate();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 문자열로 된 공개키 가져오기
     * @return
     */
    public String getPublic() {
        return "-----BEGIN PUBLIC KEY-----\n" + new String(Base64.getEncoder().encode(publicKey.getEncoded())) + "\n-----END PUBLIC KEY-----\n";
    }

    /**
     * 문자열로된 비밀키 가져오기
     * @return
     */
    public String getPrivate() {
        return "-----BEGIN RSA PRIVATE KEY-----\n" + new String(Base64.getEncoder().encode(privateKey.getEncoded())) + "\n-----END RSA PRIVATE KEY-----\n";
    }

    /**
     * 객체로된 공개키 가져오기
     * @return
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * 객체로된 비밀키 가져오기
     * @return
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    // TODO: 암복호화 수행 메소드 필요함.
}
