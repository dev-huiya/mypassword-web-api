package me.huiya.core.Restfull;

import me.huiya.core.Common.Common;
import me.huiya.core.Common.JWTManager;
import me.huiya.core.Common.UserAgentParser;
import me.huiya.core.Config.WithOutAuth;
import me.huiya.core.Entity.Result;
import me.huiya.core.Entity.Token;
import me.huiya.core.Entity.User;
import me.huiya.core.Exception.ParamRequiredException;
import me.huiya.core.Exception.TokenExpiredException;
import me.huiya.core.Repository.TokenRepository;
import me.huiya.core.Repository.UserRepository;
import me.huiya.core.Service.UserService;
import me.huiya.core.Type.Auth;
import me.huiya.core.Type.Http;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.beans.ConstructorProperties;
import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping(value="/auth")
public class AuthController {

    private static final String HEADER_TOKEN_KEY = "Bearer ";

    // Autowired 대신 추천되는 의존성 주입 방식
    private static TokenRepository TokenRepo;
    private static UserRepository UserRepo;
    private static JWTManager JWTManager;
    private static UserService UserService;

    @ConstructorProperties({
        "TokenRepository",
        "UserRepository",
        "JWTManager",
        "UserService",
    })
    public AuthController(
        TokenRepository TokenRepo,
        UserRepository UserRepo,
        JWTManager JWTManager,
        UserService UserService
    ) {
        this.TokenRepo = TokenRepo;
        this.UserRepo = UserRepo;
        this.JWTManager = JWTManager;
        this.UserService = UserService;
    }

    @PutMapping("/refresh")
    @WithOutAuth
    public Result refreshToken(@RequestBody Token requestToken, HttpServletRequest request) throws Exception {
        Result result = new Result();

        String refreshToken = requestToken.getRefreshToken();
        if(refreshToken == null) {
            throw new ParamRequiredException("Required params");
        }

        Token token = TokenRepo.findTokenByRefreshToken(refreshToken);

        if(token == null) {
            throw new TokenExpiredException(null);
        }

        User user = UserRepo.findUserByUserId(token.getUserId());
        Token newToken = null;
        String browser = UserAgentParser.getUserAgent(request);

        if(TokenRepo.findTokenByRefreshToken(token.getRefreshToken()) != null) {
            // 리프레시 토큰 정보가 디비에 있음
            // 지우고 새로 생성
            TokenRepo.deleteByTokenAndRefreshToken(token.getToken(), token.getRefreshToken());

            HashMap<String, Object> data = new HashMap<>();
            data.put("ip", Common.getClientIP(request));
            data.put("userAgent", browser);
            newToken = JWTManager.create(true, user, data);
        } else {
            // 리프레시 토큰 정보가 디비에 없음.
            // refresh api가 너무 빨리 두번 연속으로 호출되는 경우
            // 첫 리프레시 토큰 검색시에는 토큰을 가져오지만 이 시점에서는 토큰이 디비에서 삭제된 시점임.
            // 따라서 기존 토큰 재사용함.

            // 기존 정보로 방금 전에 생성된 토큰 가져오기
            newToken = TokenRepo.findFirstByUserIdAndBrowserOrderByRefreshExpireDesc(token.getUserId(), browser);
        }

        if(newToken == null) {
            // 토큰이 생성되지 못했음.
            result.setSuccess(false);
            result.setMessage(Auth.JWT_ERROR);
            result.setResult(null);
            return result;
        }

        result.setSuccess(true);
        result.setMessage(Auth.OK);
        result.setResult(newToken);
        return result;
    }

    @GetMapping(value="/verify")
    @WithOutAuth
    public Result verifyToken(@RequestHeader(required = false, value = "Authorization") String token) throws Exception {
        Result result = new Result();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("verify", false);

        try {
            HashMap<String, Object> info = JWTManager.read(token);
            hashMap.put("verify", true);
        } catch (Exception e) {
        }

        result.setSuccess(true);
        result.setMessage(Auth.OK);
        result.setResult(hashMap);

        return result;
    }

    @GetMapping(value="/key")
    public Result getPublicKey(@RequestHeader(value = "Authorization") String token) throws Exception {
        Result result = new Result();
        Token savedToken = TokenRepo.getTokenByToken(token.replace(HEADER_TOKEN_KEY, ""));
        if(savedToken == null) {
            throw new TokenExpiredException(null);
        }

        result.setSuccess(true);
        result.setMessage(Auth.OK);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("publicKey", savedToken.getPublicKey());

        result.setResult(hashMap);

        return result;
    }

    @PostMapping(value="/signin")
    @WithOutAuth
    public Result login(@RequestBody HashMap<String, Object> param, HttpServletRequest request) throws Exception {
        Result result = new Result();

        // 일반 로그인용
        String email = (String) param.get("email");
        String password = (String) param.get("password");
//        String recaptchaToken = (String) param.get("recaptchaToken");

        // 자동로그인 여부
        Boolean autoLogin = param.containsKey("autoLogin") ? (Boolean) param.get("autoLogin") : false;
        // 원래라면 containsKey 함수가 필요 없지만 true false 처리를 해주기 위해 사용했음.

        // ci가 없고 == sns 로그인이 아니고
        // 아이디나 비밀번호가 없으면
        if(
            email == null || email.equals("")
            || password == null || password.equals("")
        ) {
            throw new ParamRequiredException(null);
        }

//        if(recaptchaToken == null) {
//            result.setSuccess(false);
//            result.setMessage(Auth.CAPTCHA_EMPTY);
//            return result;
//        }
//
//        // 리캡챠 검증
//        if(!Recaptcha.verify(recaptchaToken)){
//            result.setSuccess(false);
//            result.setMessage(Auth.CAPTCHA_FAIL);
//            return result;
//        }

        User user = null;
        // 일반 로그인
        user = UserService.getUserbyEmailAndPassword(email, password);

        if(user == null) {
            result.setSuccess(false);
            result.setMessage(Auth.AUTH_WRONG);
            return result;
        }

//        if(user.isEmailVerify() == false) {
//            result.setSuccess(false);
//            result.setMessage(Auth.EMAIL_VERIFY_REQUIRED);
//
//            HashMap<String, Object> hashMap = new HashMap<>();
//            hashMap.put("email", user.getEmail());
//            hashMap.put("name", user.getNickName());
//            result.setResult(hashMap);
//
//            return result;
//        }

        user = UserRepo.save(user);

        HashMap<String, Object> data = new HashMap<>();
        data.put("ip", Common.getClientIP(request));
        data.put("userAgent", UserAgentParser.getUserAgent(request));
        Token token = JWTManager.create(autoLogin, user, data);

        if(token == null) {
            // 토큰이 생성되지 못했음.
            result.setSuccess(false);
            result.setMessage(Auth.JWT_ERROR);
            result.setResult(null);
            return result;
        }

        result.setSuccess(true);
        result.setMessage(Auth.OK);
        result.setResult(token);
        return result;
    }

    @DeleteMapping("/signout")
    public Result disposalToken(@RequestHeader(value = "Authorization") String token) {
        Result result = new Result();

        TokenRepo.deleteByToken(token.replace(HEADER_TOKEN_KEY, ""));

        result.setSuccess(true);
        result.setMessage(Http.OK);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("signout", true);

        result.setResult(hashMap);
        return result;
    }
}
