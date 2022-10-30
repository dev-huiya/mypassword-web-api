package me.huiya.core.Common;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import me.huiya.core.Encrypt.AES256Util;
import me.huiya.core.Encrypt.RSAUtils;
import me.huiya.core.Encrypt.SHA256Util;
import me.huiya.core.Entity.Token;
import me.huiya.core.Entity.User;
import me.huiya.core.Exception.AuthRequiredException;
import me.huiya.core.Exception.ParamRequiredException;
import me.huiya.core.Exception.TokenExpiredException;
import me.huiya.core.Repository.TokenRepository;
import me.huiya.project.Encrypt.Encrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.beans.ConstructorProperties;
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
import java.util.function.Consumer;

@Component
public class JWTManager {

    public final static String HEADER_TOKEN_KEY = "Bearer ";

    private String UI_SERVER_URL;
    private String API_SERVER_URL;

    private static String AES_IV;

    @Value("${core.SERVER.UI}")
    public void setUiServerUrl(String uiServerUrl) { UI_SERVER_URL = uiServerUrl; }

    @Value("${core.SERVER.API}")
    public void setApiServerUrl(String apiServerUrl) { API_SERVER_URL = apiServerUrl; }

    @Value("${core.AES-iv}")
    public void setIV(String iv) { AES_IV = iv; }

    // Autowired 대신 추천되는 의존성 주입 방식
    private TokenRepository TokenRepo;

    @ConstructorProperties({
        "TokenRepository",
    })
    public JWTManager(TokenRepository TokenRepo) {
        this.TokenRepo = TokenRepo;
    }

    /**
     * 토큰을 생성하고 DB에 입력한다.
     * @param isAutoLogin refresh token 생성 여부
     * @param data db 저장시 참조할 값. 동시에 모든 값이 info claim에 입력됨. 다른 타입 필요시 setClaim 수동 설정 할 것.
     * @return
     */
    public Token create(Boolean isAutoLogin, User user, HashMap<String, Object> data) throws ParamRequiredException, Exception {
        Token token  = this._create(isAutoLogin, user, data, (_data) -> {
           data.forEach((key, value) -> {
               _data.put(key, value);
           });
        });

        return this.save(token, data);
    }

    /**
     * 토큰을 생성하고 DB에 입력한다.
     * @param isAutoLogin refresh token 생성 여부
     * @param data db 저장시 참조할 값.
     * @param setClaim info claim을 설정할 람다식
     * @return
     */
    public Token create(Boolean isAutoLogin, User user, HashMap<String, Object> data, Consumer<HashMap<String, Object>> setClaim) throws ParamRequiredException, Exception {
        Token token  = this._create(isAutoLogin, user, data, setClaim);

        return this.save(token, data);
    }

    public Token _create(Boolean isAutoLogin, User user, HashMap<String, Object> data, Consumer<HashMap<String, Object>> setClaim) throws ParamRequiredException, Exception {
        if(user == null || user.getUserId() <= 0) {
            throw new ParamRequiredException("JWTManager:: A User is required to create a JWT token.");
        }
        if(!data.containsKey("masterKey")) {
            throw new ParamRequiredException("JWTManager:: masterKey required.");
        }

        try {
            RSAUtils rsa = new RSAUtils();
            Token token = new Token();

            Date refreshExpireDate = null;
            String refreshToken = null;

            // 이 토큰과의 통신에서 사용할 데이터 암호화 키
            // 퍼블릭키를 해시한 후 32바이트로 잘라내어 사용함.
            String encryptKey = Encrypt.encrypt(Common.createSecureRandom(32), rsa.getPublic());

            // 현재 시간
            Date currentDate = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentDate);

            // access token 만료 시간
            cal.add(JWTManagerCommon.getAccessExpireTimeUnit(), JWTManagerCommon.getAccessExpireNumeric());
            Date accessExpireDate = cal.getTime();

            // 토큰에 넣을 유저 정보 준비
            HashMap<String, Object> userInfo = new HashMap<>();

            // 민감한 정보
            userInfo.put("id", AES256Util.encrypt(Integer.toString(user.getUserId())));
            
            // 기본 정보
            userInfo.put("encryptKey", encryptKey);

            // 토큰에 람다식으로 값 입력
            setClaim.accept(userInfo);

            if(isAutoLogin) {
                // 자동 로그인이 켜져있으면 refresh_token 발급

                // refresh token 만료시간
                cal.setTime(currentDate);
                cal.add(JWTManagerCommon.getRefreshExpireTimeUnit(), JWTManagerCommon.getRefreshExpireNumeric());
                refreshExpireDate = cal.getTime();
                refreshToken = createRefreshToken();
                userInfo.put("refreshToken", refreshToken);
                userInfo.put("refreshExpireDate", refreshExpireDate);
            }

            // 토큰 생성
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) rsa.getPublicKey(), (RSAPrivateKey) rsa.getPrivateKey());
            String tokenStr = JWT.create()
                    .withIssuer(this.API_SERVER_URL) // 토큰 발급자
                    .withAudience(this.UI_SERVER_URL) // 토큰 수신자
                    //.withNotBefore(refreshExpireDate) // 토큰 활성화 되는 시간 (미사용 예정)
                    .withIssuedAt(currentDate) // 토큰 발급시간
                    .withExpiresAt(accessExpireDate) // 토큰 만료시간
                    .withClaim("info", userInfo) // 유저 정보 토큰에 넣기
                    .sign(algorithm); // 토큰에 사이닝

            token.setUserId(user.getUserId());
            token.setToken(tokenStr);
            token.setExpire(accessExpireDate);
            token.setPublicKey(rsa.getPublic());
            token.setPrivateKey(rsa.getPrivate());
            token.setRefreshExpire(refreshExpireDate);
            token.setRefreshToken(refreshToken);

            // add project code
            token.setEncryptKey(encryptKey);
            token.setMasterKey(Encrypt.encrypt((String) data.get("masterKey"), rsa.getPublic()));
            return token;
        } catch (JWTCreationException e){
            //Invalid Signing configuration / Couldn't convert Claims.
            // throw new RuntimeException(e);
            return null;
        }
    }

    private Token save(Token token, HashMap<String, Object> data) {

        if(data.containsKey("ip")) {
            token.setIpAddress((String) data.get("ip"));
        }
        if(data.containsKey("user")) {
            token.setUserId(((User) data.get("user")).getUserId());
        }
        if(data.containsKey("userAgent")) {
            token.setBrowser((String) data.get("userAgent"));
        }

        // 토큰 정보 디비에 저장
        return this.TokenRepo.save(token);
    }

    /**
     * 키를 DB에서 불러와 토큰을 검증한다.
     *
     * @param token 토큰
     * @return
     */
    public DecodedJWT verify(String token) throws Exception {

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
    public String createRefreshToken() {
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
    public HashMap<String, Object> read(String token) throws Exception {
        HashMap<String, Object> info = null;

        if(token == null) {
            throw new AuthRequiredException("Required token");
        }

        Claim jws = this.verify(token.replace(HEADER_TOKEN_KEY, "")).getClaim("info");
        if(jws != null) {
            info = (HashMap<String, Object>) jws.asMap();
            
            if(info.containsKey("id")) {
                info.put("id", Integer.parseInt(AES256Util.decrypt((String) info.get("id"))));
            }

        }
        return info;
    }
}
