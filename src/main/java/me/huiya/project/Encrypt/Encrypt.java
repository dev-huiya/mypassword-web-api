package me.huiya.project.Encrypt;

import me.huiya.core.Common.Common;
import me.huiya.core.Common.JWTManager;
import me.huiya.core.Encrypt.AES256Util;
import me.huiya.core.Encrypt.SHA256Util;
import me.huiya.core.Entity.Token;
import me.huiya.core.Repository.TokenRepository;
import me.huiya.core.Repository.UserRepository;
import me.huiya.project.Repository.PasswordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.beans.ConstructorProperties;

@Component
public class Encrypt {

    private static TokenRepository TokenRepo;
    private static String AES_IV;

    @Value("${core.AES-iv}")
    public void setIV(String iv) { AES_IV = iv; }

    @ConstructorProperties({
        "TokenRepository",
    })
    public Encrypt(
        TokenRepository TokenRepo
    ) {
        this.TokenRepo = TokenRepo;
    }

    public static String encrypt(String plainText, Token token) {
        return encrypt(plainText, token.getPublicKey());
    }

    public static String encrypt(String plainText, String publicKey) {

        // 퍼블릭키를 해시한 후 32바이트로 잘라내어 사용함.
        return AES256Util.encrypt(
            plainText,
            getTokenKey(publicKey),
            AES_IV
        );
    }

    public static String decrypt(String encryptedText, Token token) {
        return decrypt(encryptedText, token.getPublicKey());
    }

    public static String decrypt(String encryptedText, String publicKey) {
        return AES256Util.decrypt(
            encryptedText,
            getTokenKey(publicKey),
            AES_IV
        );
    }

    private static String getTokenKey(String publicKey) {
        return SHA256Util.encrypt(publicKey).substring(0, 32);
    }

    public static String serverToClient(String masterEncryptedText, String tokenStr) {
        Token token = TokenRepo.getTokenByToken(tokenStr.replace(JWTManager.HEADER_TOKEN_KEY, ""));
        return serverToClient(masterEncryptedText, token);
    }

    public static String serverToClient(String masterEncryptedText, Token token) {
        String tokenKey = getTokenKey(token.getPublicKey());

        return AES256Util.encrypt(
            // 민감정보 복호화
            AES256Util.decrypt(
                masterEncryptedText,
                // master key
                AES256Util.decrypt(
                    token.getMasterKey(),
                    tokenKey,
                    AES_IV
                ),
                AES_IV
            ),
            // 재 암호화용 encryptKey (클라이언트와 공유된 키 복호화
            AES256Util.decrypt(
                token.getEncryptKey(),
                tokenKey,
                AES_IV
            ),
            AES_IV
        );
    }

    public static String clientToServer(String encryptedText, String tokenStr) {
        Token token = TokenRepo.getTokenByToken(tokenStr.replace(JWTManager.HEADER_TOKEN_KEY, ""));
        return clientToServer(encryptedText, token);
    }

    public static String clientToServer(String encryptedText, Token token) {
        String tokenKey = getTokenKey(token.getPublicKey());

        return AES256Util.encrypt(
            // 민감정보 복호화
            AES256Util.decrypt(
                encryptedText,
                // 클라이언트와 공유된 키 복호화
                AES256Util.decrypt(
                    token.getEncryptKey(),
                    tokenKey,
                    AES_IV
                ),
                AES_IV
            ),
            // 사용자의 마스터 암호화 키로 재 암호화
            AES256Util.decrypt(
                token.getMasterKey(),
                tokenKey,
                AES_IV
            ),
            AES_IV
        );
    }
}
