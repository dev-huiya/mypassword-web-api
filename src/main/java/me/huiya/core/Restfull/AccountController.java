package me.huiya.core.Restfull;

import me.huiya.core.Common.Common;
import me.huiya.core.Common.CommonObjectUtils;
import me.huiya.core.Common.FileManager;
import me.huiya.core.Common.JWTManager;
import me.huiya.core.Config.WithOutAuth;
import me.huiya.core.Encrypt.AES256Util;
import me.huiya.core.Encrypt.SHA256Util;
import me.huiya.core.Entity.Result;
import me.huiya.core.Entity.User;
import me.huiya.core.Entity.Verify;
import me.huiya.core.Exception.ParamRequiredException;
import me.huiya.core.Repository.UserRepository;
import me.huiya.core.Repository.VerifyRepository;
import me.huiya.core.Service.Email;
import me.huiya.core.Service.UserService;
import me.huiya.core.Type.API;
import me.huiya.core.Type.Auth;
import me.huiya.core.Type.Type;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.beans.ConstructorProperties;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value="/account")
public class AccountController {

    // Autowired 대신 추천되는 의존성 주입 방식
    private static UserRepository UserRepo;
    private static Email EmailService;
    private static VerifyRepository VerifyRepo;
    private static JWTManager JWTManager;

    private static String AES_IV;

    @Value("${core.AES-iv}")
    public void setIV(String iv) { AES_IV = iv; }

    @ConstructorProperties({
        "UserRepository",
        "Email",
        "VerifyRepository",
        "JWTManager",
    })
    public AccountController(
        UserRepository UserRepo,
        Email EmailService,
        VerifyRepository VerifyRepo,
        JWTManager JWTManager
    ) {
        this.UserRepo = UserRepo;
        this.EmailService = EmailService;
        this.VerifyRepo = VerifyRepo;
        this.JWTManager = JWTManager;
    }

    @GetMapping(value="/signup-check/email")
    @WithOutAuth
    public Result emailCheck(@RequestParam(required = false) String email) {
        Result result = new Result();

        result.setSuccess(true);
        result.setMessage(Type.OK);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("usage", UserService.emailCheck(email));

        result.setResult(hashMap);
        return result;
    }

    @GetMapping(value="/signup-check/nickname")
    @WithOutAuth
    public Result nickCheck(@RequestParam(required = false) String nickName) {
        Result result = new Result();

        result.setSuccess(true);
        result.setMessage(Type.OK);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("usage", UserService.nickNameCheck(nickName));

        result.setResult(hashMap);
        return result;
    }

    @PostMapping(value="/signup")
    @WithOutAuth
    public Result join(
        @RequestPart @RequestParam(required = false) MultipartFile profile,
        @RequestParam(required = false, defaultValue = "") String email,
        @RequestParam(required = false, defaultValue = "") String password,
        @RequestParam(required = false, defaultValue = "") String nickName,

        // 리캡챠
        @RequestParam(required = false, defaultValue = "") String recaptchaToken
    ) throws Exception {
        Result result = new Result();

        // validation 엔티티에 포함되지 않는 토큰이나 이런 것 때문에 @valid 어노테이션 사용안했음
        // hw.kim 2020-12-19 10:24
        if(
            email == null
            || email.equals("")
//            || password == null
            || nickName == null
            || nickName.equals("")
        ) {
        	throw new ParamRequiredException(null);
        }

        // 리캡챠 검증
//        Recaptcha recaptcha = new Recaptcha();
//        if(!recaptcha.verify(recaptchaToken)){
//            result.setSuccess(false);
//            result.setMessage(Auth.CAPTCHA_FAIL);
//            return result;
//        }

        // 이메일 중복 검증
        if(!UserService.emailCheck(email)) {
            result.setSuccess(false);
            result.setMessage(Auth.JOIN_DUPLICATE);
            return result;
        }

        // 닉네임 중복 검증
        if(!UserService.nickNameCheck(nickName)) {
            result.setSuccess(false);
            result.setMessage(Auth.JOIN_DUPLICATE);
            return result;
        }

        // 솔트 넣는건 수동임
        String salt = Common.createSecureRandom(32);

        // 유저 생성
        User user = new User();
        user.setEmail(email);
        user.setPassword(SHA256Util.encrypt(salt + password));
        user.setSalt(salt);

        user.setNickName(nickName);
        user.setEmailVerify(false);
        // AES 키는 일정 길이(32바이트)가 필요해서 패스워드를 SHA256(64byte) 후 32로 잘라서 암호화 키로 사용함.
        // 2022-10-30 19:41 Hawon Kim
        String connectionKey = SHA256Util.encrypt(password).substring(0, 32);
        user.setMasterKey(AES256Util.encrypt(Common.createSecureRandom(32), connectionKey, AES_IV));

        // 유저 저장
        user = UserRepo.save(user);
        boolean isUserEdit = false;

        // 프로필 저장
        if(profile != null) {
            String hash = FileManager.save(profile, user.getUserId());
            user.setProfileImage(hash);
            UserRepo.save(user);
        }

//        UserService.sendJoinVerifyEmail(user);
//
//        UserService.sendWelcomeMail(user);

        result.setSuccess(true);
        result.setMessage(Type.OK);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("join", true);
        result.setResult(hashMap);

        return result;
    }

    // TODO: login에 RSA 키 발급 기능 추가: private key를 aes로 암호화해 넘겼다가 돌아올때 검증

    @GetMapping(value="/info")
    public Result getInfo(@RequestHeader(value = "Authorization") String token) throws Exception {
    	Result result = new Result();

		HashMap<String, Object> info = JWTManager.read(token);
		User user = UserRepo.findUserByUserId((Integer) info.get("id"));

        Map map = CommonObjectUtils.convertObjectToMap(user);

//        map.remove("userId");
        map.remove("password");
        map.remove("salt");
        map.put("createTime", user.getCreateTime());

		result.setSuccess(true);
		result.setMessage(Type.OK);
		result.setResult(map);

		return result;
	}

    @PatchMapping(value="/info")
    public Result patchInfo(
		@RequestHeader(value = "Authorization") String token,
		@RequestPart @RequestParam(required = false) MultipartFile profile,
        @RequestPart @RequestParam(required = false) MultipartFile nameCard,
        @RequestParam(required = false) String nickName
    ) throws Exception {
    	Result result = new Result();

    	//토큰 아이디 확인
    	HashMap<String, Object> info = JWTManager.read(token);
		User user = UserRepo.findUserByUserId((Integer) info.get("id"));

		// 별명 업데이트
		if(nickName != null){
			user.setNickName(nickName);
		};

		// 프로필 넣기
		if(profile != null) {
            String hash = FileManager.save(profile, user.getUserId());
            user.setProfileImage(hash);
        }

		UserRepo.save(user);

		result.setSuccess(true);
	    result.setMessage(Type.OK);

	    HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("update", true);
        result.setResult(hashMap);

		return result;
    }

    @PatchMapping(value="/password")
    public Result newPassword(
		@RequestHeader(value = "Authorization") String token,
		@RequestBody HashMap<String, Object> param,
		HttpServletRequest request
    ) throws Exception {
    	Result result = new Result();

    	HashMap<String, Object> info = JWTManager.read(token);
    	User user = UserRepo.findUserByUserId((Integer) info.get("id"));

    	String password = (String) param.get("password");
    	String newPassword = (String) param.get("newPassword");

    	if(password == null || newPassword == null) {
    		throw new ParamRequiredException(null);
    	}

    	String salt = user.getSalt();
    	String email = user.getEmail();
    	String newSaltedPassword = (SHA256Util.encrypt(salt + password));
    	User userinfo = UserRepo.findUserByEmailAndPassword(email, newSaltedPassword);
    	if(userinfo == null) {
    		result.setSuccess(false);
            result.setMessage(Auth.PASSWORD_CHANGE_FAIL);
            return result;
    	}

    	String newSalt = Common.createSecureRandom(32);
    	userinfo.setSalt(newSalt);
    	userinfo.setPassword(SHA256Util.encrypt(newSalt + newPassword));

    	UserRepo.save(userinfo);

    	result.setSuccess(true);
    	result.setMessage(Type.OK);

    	HashMap<String, Object> hashMap = new HashMap<>();
    	hashMap.put("update", true);
		result.setResult(hashMap);

		return result;
    }

    @PostMapping("/email/verify/resend")
    @WithOutAuth
    public Result resendEmailVerify(
        @RequestBody HashMap<String, Object> param
    ) throws Exception {
        Result result = new Result();

        String email = (String) param.get("email");

        if (email.equals("")
            || email == null
        ) {
            throw new ParamRequiredException(null);
        }

        User user = UserRepo.findUserByEmailAndEmailVerifyFalse(email);

        if(user == null) {
            result.setSuccess(false);
            result.setMessage(API.DATA_NOT_FOUND);
            return result;
        }

        VerifyRepo.deleteAllByUserId(user.getUserId());

        UserService.sendJoinVerifyEmail(user);

        result.setSuccess(true);
        result.setMessage(Type.OK);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("work", true);
        result.setResult(hashMap);

        return result;
    }

    @PostMapping("/email/verify/{code}")
    @WithOutAuth
    public Result emailVerify(
        @PathVariable String code
    ) throws Exception {
        Result result = new Result();

        Verify verify = VerifyRepo.findVerifyByTypeAndCode("JOIN", code);

        if(verify == null) {
            result.setSuccess(false);
            result.setMessage(me.huiya.core.Type.Verify.INVALID_VERIFY_TOKEN);
            return result;
        }

        User user = UserRepo.findUserByUserId(verify.getUserId());

        if(user == null) {
            result.setSuccess(false);
            result.setMessage(API.USER_EMPTY);
            return result;
        }

        if (verify.getExpire().before(new Date())) {
            result.setSuccess(false);
            result.setMessage(me.huiya.core.Type.Verify.VERIFY_TOKEN_EXPIRED);
            return result;
        }

        if(user.isEmailVerify() != true
            || verify.isUsed() != true
        ) {
            // 둘중에 하나라도 true가 아닐때만 실행

            user.setEmailVerify(true);
            UserRepo.save(user);

            verify.setUsed(true);
            VerifyRepo.save(verify);
        }

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("url", "/login");

        result.setSuccess(true);
        result.setMessage(API.OK);
        result.setResult(hashMap);
        return result;
    }

    @PutMapping("/password/reset")
    @WithOutAuth
    public Result sendResetPassword(
        @RequestBody HashMap<String, Object> param
    ) throws Exception {
        Result result = new Result();

        String email = (String) param.get("email");
        String name = (String) param.get("name");
        String phone = (String) param.get("phone");

        if( email == null || email.equals("")
            || name == null || name.equals("")
            || phone == null || phone.equals("")
        ) {
            throw new ParamRequiredException(null);
        }

        User user = UserRepo.findUserByEmail(email);

        if(user == null) {
            result.setSuccess(false);
            result.setMessage(API.USER_EMPTY);
            return result;
        }

        // 이메일 인증 메일 발송
//        UserService.sendPasswordResetEmail(user);

        result.setSuccess(true);
        result.setMessage(Type.OK);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("send", true);
        result.setResult(hashMap);

        return result;
    }

    @PostMapping("/password/reset")
    @WithOutAuth
    public Result resetPassword(
        @RequestBody HashMap<String, Object> param
    ) throws Exception {
        Result result = new Result();

        String verifyCode = (String) param.get("code");
        String password = (String) param.get("password");

        if (password == null || password.equals("")) {
            throw new ParamRequiredException(null);
        }

        Verify verify = VerifyRepo.findVerifyByTypeAndCode("PASSWORD", verifyCode);

        // 토큰 정보가 없을때
        if(verify == null) {
            result.setSuccess(false);
            result.setMessage(me.huiya.core.Type.Verify.INVALID_VERIFY_TOKEN);
            return result;
        }

        User user = UserRepo.findUserByUserId(verify.getUserId());

        // 유저 정보가 없을때
        if(user == null) {
            result.setSuccess(false);
            result.setMessage(API.USER_EMPTY);
            return result;
        }

        // 토큰 만료시
        if (verify.getExpire().before(new Date())) {
            result.setSuccess(false);
            result.setMessage(me.huiya.core.Type.Verify.VERIFY_TOKEN_EXPIRED);
            return result;
        }

        String newSalt = Common.createSecureRandom(32);
        user.setSalt(newSalt);
        user.setPassword(SHA256Util.encrypt(newSalt + password));

        UserRepo.save(user);
        VerifyRepo.delete(verify);

        result.setSuccess(true);
        result.setMessage(Type.OK);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("update", true);
        result.setResult(hashMap);

        return result;
    }

    @DeleteMapping("/leave")
    public Result leave(
        @RequestHeader(value = "Authorization") String token
    ) throws Exception {
        Result result = new Result();

        HashMap<String, Object> info = JWTManager.read(token);
//        User user = UserRepo.findUserByUserId((Integer) info.get("id"));

        if(UserService.leaveUser((Integer) info.get("id"))) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("delete", true);
            result.set(true, API.OK, hashMap);
        } else {
            result.set(false, API.DATA_NOT_FOUND);
        }

        return result;
    }
}
