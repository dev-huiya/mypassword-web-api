package me.huiya.core.Common;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import me.huiya.core.Encrypt.AES256Util;
import me.huiya.core.Encrypt.RSAUtils;
import me.huiya.core.Entity.Token;
import me.huiya.core.Entity.User;
import me.huiya.core.Exception.AuthRequiredException;
import me.huiya.core.Exception.TokenExpiredException;
import me.huiya.core.Repository.TokenRepository;
import org.springframework.stereotype.Component;

import javax.xml.crypto.Data;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

@Component
public class JWTManager {

    private static final String HEADER_TOKEN_KEY = "Bearer ";

    // Autowired 대신 추천되는 의존성 주입 방식
    private static TokenRepository TokenRepo;
    public JWTManager(TokenRepository TokenRepo) {
        this.TokenRepo = TokenRepo;
    }

    /**
     * 토큰을 생성하고 DB에 저장한다.
     *
     * @param user 유저 정보
     * @param userAgent 토큰 생성을 요청한 유저 에이전트 정보
     * @return Token 객체
     */
    public static Token create(User user, String userAgent, Boolean isAutoLogin) {

        if(user.getUserId() <= 0) {
            // 사용자 고유값이 없으면 사용자 정보가 없다고 판정하고 토큰 생성하지 않음.
            return null;
        }

        try {
            RSAUtils rsa = new RSAUtils();
            Token token = new Token();

            Date refreshExpireDate = null;
            String refreshToken = null;

            // 현재 시간
            Date currentDate = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentDate);

            // access token 만료 시간은 12시간으로 설정
            cal.add(Calendar.HOUR, 12);
            Date accessExpireDate = cal.getTime();

            // 토큰에 넣을 유저 정보 준비
            HashMap<String, Object> userInfo = new HashMap<>();

            // 민감한 정보
            userInfo.put("id", AES256Util.encrypt(Integer.toString(user.getUserId())));

            // 평문 정보
            userInfo.put("nickName", user.getNickName());
//            userInfo.put("email", user.getEmail());
//            userInfo.put("penName", user.getPenName());
//            userInfo.put("lastDate",
//                    user.getLastDate() instanceof Timestamp
//                            ? new Date(user.getLastDate().getTime())
//                            : user.getLastDate());
//            userInfo.put("profile", user.getProfile() != null ? user.getProfile() : "");

            if(isAutoLogin) {
                // 자동 로그인이 켜져있으면 refresh_token 발급

                // refresh token 만료시간은 1주로 설정
                cal.setTime(currentDate);
                cal.add(Calendar.DATE, 7);
                refreshExpireDate = cal.getTime();
                refreshToken = createRefreshToken();
                userInfo.put("refreshToken", refreshToken);
                userInfo.put("refreshExpireDate", refreshExpireDate);
            }

            // 토큰 생성
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) rsa.getPublicKey(), (RSAPrivateKey) rsa.getPrivateKey());
            String tokenStr = JWT.create()
                    .withIssuer("api.tradeinfo.kr") // 토큰 발급자
                    .withAudience("tradeinfo.kr") // 토큰 수신자
                    //.withNotBefore(refreshExpireDate) // 토큰 활성화 되는 시간 (미사용 예정)
                    .withIssuedAt(currentDate) // 토큰 발급시간
                    .withExpiresAt(accessExpireDate) // 토큰 만료시간
                    .withClaim("info", userInfo) // 유저 정보 토큰에 넣기
                    .sign(algorithm); // 토큰에 사이닝

            // 토큰 정보 디비에 저장 준비
            token.setUserId(user.getUserId());
            token.setToken(tokenStr);
            token.setExpire(cal.getTime());
            token.setPublicKey(rsa.getPublic());
            token.setPrivateKey(rsa.getPrivate());
            token.setBrowser(userAgent);
            token.setRefreshExpire(refreshExpireDate);
            token.setRefreshToken(refreshToken);

            return TokenRepo.save(token);
        } catch (JWTCreationException e){
            //Invalid Signing configuration / Couldn't convert Claims.
            // throw new RuntimeException(e);
            return null;
        }
    }

    /**
     * 키를 DB에서 불러와 토큰을 검증한다.
     *
     * @param token 토큰
     * @return
     */
    public static DecodedJWT verify(String token) throws Exception {

        Token savedToken = TokenRepo.getTokenByToken(token.replace(HEADER_TOKEN_KEY, ""));
        if(savedToken == null) {
            // 디비에 저장된 키가 없으면 만료로 판정
            throw new TokenExpiredException(null);
        }

        KeyFactory kf = KeyFactory.getInstance("RSA");

        String publicKeyStr = savedToken.getPublicKey().replace("-----BEGIN PUBLIC KEY-----\n", "").replace("\n-----END PUBLIC KEY-----\n", "");
        String privateKeyStr = savedToken.getPrivateKey().replace("-----BEGIN RSA PRIVATE KEY-----\n", "").replace("\n-----END RSA PRIVATE KEY-----\n", "");

        // public key 불러오기
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyStr));
        PublicKey publicKey = kf.generatePublic(publicSpec);

        // private key 불러오기
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyStr));
        PrivateKey privateKey = kf.generatePrivate(spec);

        Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) publicKey, (RSAPrivateKey) privateKey);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("api.tradeinfo.kr")
                .build(); //Reusable verifier instance

        return verifier.verify(token);
    }

    /**
     * 갱신 토큰 생성.
     * 토큰 자체는 랜덤 문자열이고, DB에 저장해야 사용가능함.
     *
     * @return 랜덤한 문자열.
     */
    public static String createRefreshToken() {
        String random = Common.createSecureRandom(20);
        Token result = TokenRepo.findTokenByRefreshToken(random);

        if(result != null) {
            return createRefreshToken();
        } else {
            return random;
        }
    }

    /**
     * 토큰에서 데이터를 불러온다.
     *
     * @param token Header로 전달된 JWT token
     * @return 토큰에 담겨있는 데이터 (userInfo)
     * @throws Exception
     */
    public static HashMap<String, Object> read(String token) throws Exception {
        HashMap<String, Object> info = null;

        if(token == null) {
            throw new AuthRequiredException("Required token");
        }

        Claim jws = verify(token.replace(HEADER_TOKEN_KEY, "")).getClaim("info");
        if(jws != null) {
            info = (HashMap<String, Object>) jws.asMap();
            info.put("id", Integer.parseInt(AES256Util.decrypt((String) info.get("id"))));
            info.put("rateId", Integer.parseInt(AES256Util.decrypt((String) info.get("rateId"))));
        }
        return info;
    }
}
