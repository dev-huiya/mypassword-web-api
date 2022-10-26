package kr.njbridge.tradeinfo.Restfull;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kr.njbridge.core.Common.Common;
import kr.njbridge.core.Common.CommonObjectUtils;
import kr.njbridge.core.Common.FileManager;
import kr.njbridge.core.Config.ManagerAuthority;
import kr.njbridge.core.Config.WithOutAuth;
import kr.njbridge.core.Exception.ParamMismatchException;
import kr.njbridge.core.Exception.ServerErrorException;
import kr.njbridge.core.Service.Email;
import kr.njbridge.core.Type.*;
import kr.njbridge.tradeinfo.Entity.*;
import kr.njbridge.tradeinfo.Entity.Verify;
import kr.njbridge.tradeinfo.Repository.*;
import kr.njbridge.tradeinfo.Service.UserManager;
import kr.njbridge.core.Entity.Result;
import kr.njbridge.core.Exception.ParamRequiredException;
import kr.njbridge.core.Encrypt.SHA256Util;
import kr.njbridge.core.Common.JWTManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.beans.ConstructorProperties;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.*;

@RestController
@RequestMapping(value="/account")
public class UserController {

    // Autowired 대신 추천되는 의존성 주입 방식
    private static RateRepository RateRepo;
    private static UserRepository UserRepo;
    private static Email EmailService;
    private static PayRepository PayRepo;
    private static VerifyRepository VerifyRepo;
    private static PaySimpleRepository PaySimpleRepo;
    private static SnsRepository SnsRepo;

    @ConstructorProperties({
        "RateRepository",
        "UserRepository",
        "Email",
        "PayRepository",
        "VerifyRepository",
        "PaySimpleRepository",
        "SnsRepository",
    })
    public UserController(
        RateRepository RateRepo,
        UserRepository UserRepo,
        Email EmailService,
        PayRepository PayRepo,
        VerifyRepository VerifyRepo,
        PaySimpleRepository PaySimpleRepo,
        SnsRepository SnsRepo
    ) {
        this.RateRepo = RateRepo;
        this.UserRepo = UserRepo;
        this.EmailService = EmailService;
        this.PayRepo = PayRepo;
        this.VerifyRepo = VerifyRepo;
        this.PaySimpleRepo = PaySimpleRepo;
        this.SnsRepo = SnsRepo;
    }

    @GetMapping(value="/signup-check/email")
    @WithOutAuth
    public Result emailCheck(@RequestParam(required = false) String email) {
        Result result = new Result();

        result.setSuccess(true);
        result.setMessage(Type.OK);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("usage", UserManager.emailCheck(email));

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
        hashMap.put("usage", UserManager.nickNameCheck(nickName));

        result.setResult(hashMap);
        return result;
    }

    @PostMapping(value="/signup")
    @WithOutAuth
    public Result join(
        @RequestPart @RequestParam(required = false) MultipartFile profile,
        @RequestPart @RequestParam(required = false) MultipartFile nameCard,
        @RequestParam(required = false, defaultValue = "") String email,
        @RequestParam(required = false, defaultValue = "") String password,
        @RequestParam(required = false, defaultValue = "") String nickName,
        @RequestParam(required = false, defaultValue = "") String name,
        @RequestParam(required = false, defaultValue = "") String phone,
        @RequestParam(required = false, defaultValue = "") Integer rate,
        @RequestParam(required = false, defaultValue = "") Boolean adAgree,

        // sns 회원가입
        @RequestParam(required = false, defaultValue = "") String snsType,
        @RequestParam(required = false, defaultValue = "") String profileImage,
        @RequestParam(required = false, defaultValue = "") Boolean emailVerified,
        @RequestParam(required = false, defaultValue = "") String ci,
        @RequestParam(required = false, defaultValue = "") String accessToken,
        @RequestParam(required = false, defaultValue = "") Long accessTokenExpire,
        @RequestParam(required = false, defaultValue = "") String refreshToken,
        @RequestParam(required = false, defaultValue = "") Long refreshTokenExpire,

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
            || name == null
            || name.equals("")
            || rate == null
            || rate.equals("")
            || phone == null
            || phone.equals("")
            || adAgree == null
            || (ci.equals("")
                && (
                    password == null
                    || password.equals("")
                )
            )
        ) {
        	throw new ParamRequiredException(null);
        }

        if(
            snsType != null && !snsType.equals("")
            && (ci == null)
            // snsType이 있는데 ci가 없으면
        ) {
            throw new ParamMismatchException(null);
        }

        // 리캡챠 검증
//        Recaptcha recaptcha = new Recaptcha();
//        if(!recaptcha.verify(recaptchaToken)){
//            result.setSuccess(false);
//            result.setMessage(Auth.CAPTCHA_FAIL);
//            return result;
//        }

        // 이메일 중복 검증
        if(!UserManager.emailCheck(email)) {
            result.setSuccess(false);
            result.setMessage(Auth.JOIN_DUPLICATE);
            return result;
        }

        // 닉네임 중복 검증
        if(!UserManager.nickNameCheck(nickName)) {
            result.setSuccess(false);
            result.setMessage(Auth.JOIN_DUPLICATE);
            return result;
        }

        Rate rateEntity = RateRepo.findAllById(rate);
        if(rateEntity == null) {
            result.setSuccess(false);
            result.setMessage(Type.INVALID_VALUE);
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
        user.setName(name);
        user.setPhone(phone);
        user.setEmailVerify(false);
        user.setPoint(0);
        user.setRate(rateEntity);
        user.setAdAgree(adAgree);

        // 유저 저장
        user = UserRepo.save(user);
        boolean isUserEdit = false;

        // 프로필 저장
        if(profile != null) {
            String hash = FileManager.save(profile, user.getUserId());
            user.setProfileImage(hash);
            isUserEdit = true;
        }

        // sns 프로필 저장
        if(
            profile == null &&
                profileImage != null && !profileImage.equals("")
            // 업로드된 프로필이 없고 프로필 이미지 url만 입력될 경우
        ) {

//            byte[] _profile = Unirest.get(profileImage)
//                .asBytes()
//                .getBody();
            HttpResponse<byte[]> response = Unirest.get(profileImage)
                .asBytes();
            byte[] _profile = response.getBody();

            // get file name
            String fileName = "profile-image.png";
            String urlPath = (new URL(profileImage)).getPath();
            String originalName = urlPath.substring(urlPath.lastIndexOf('/') + 1);
            if(originalName.indexOf(".") >= 0) {
                // 파일명에 .이 있어야 파일명이 아니라고 간주.
                fileName = originalName;
            }

            // byte[]를 MultipartFile로 변환
            MultipartFile multipartFile = new MockMultipartFile(fileName, _profile);

            // 실제 저장
            String hash = FileManager.save(multipartFile, user.getUserId());

            // 파일 해시값 유저에 등록
            user.setProfileImage(hash);
            isUserEdit = true;
        }

        // 명함 저장
        if(nameCard != null) {
            String hash = FileManager.save(nameCard, user.getUserId());
            user.setNameCard(hash);
            isUserEdit = true;
        }

        // sns 정보 저장
        if(snsType != null && !snsType.equals("")) {
            // snsType이 있으면 ci가 있는건 보장됨. 위에서 검증했음.

            Sns sns = new Sns();
            sns.setType(snsType.toUpperCase());
            sns.setCi(ci);
            sns.setAccessToken(accessToken);
            sns.setAccessTokenExpire(new Date(accessTokenExpire));
            if(refreshToken != null) {
                sns.setRefreshToken(refreshToken);
            }
            if(refreshTokenExpire != null) {
                sns.setRefreshTokenExpire(new Date(refreshTokenExpire));
            }
            sns.setUser(user);
            SnsRepo.save(sns);
        }

        // sns가 있고 이메일이 검증되었을 경우 인증메일 스킵
        if(
            snsType != null && !snsType.equals("")
            && emailVerified == true
        ) {
            user.setEmailVerify(true);
            isUserEdit = true;
        } else {
            // 이메일 인증 메일 발송
            UserManager.sendJoinVerifyEmail(user);
        }

        if(isUserEdit == true) {
            UserRepo.save(user);
        }

        UserManager.sendWelcomeMail(user);

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
        if(!user.getRate().isShow()) {
            map.put("isManager", true);
        }

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

        // 명함 넣기
        if(nameCard != null) {
            String hash = FileManager.save(nameCard, user.getUserId());
            user.setNameCard(hash);
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
    	String nowPassword = (SHA256Util.encrypt(salt + password));
    	User userinfo = UserRepo.getUserByEmailAndPassword(email, nowPassword);
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

        UserManager.sendJoinVerifyEmail(user);

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
            result.setMessage(kr.njbridge.core.Type.Verify.INVALID_VERIFY_TOKEN);
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
            result.setMessage(kr.njbridge.core.Type.Verify.VERIFY_TOKEN_EXPIRED);
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

    @GetMapping("/list")
    @ManagerAuthority
    public Result getUserList(
        @PageableDefault(page = 0, size = 10, sort = "createTime", direction = Sort.Direction.DESC) Pageable paging,
        @RequestParam(required = false) HashMap<String, Object> searchParams
    ) throws Exception {
        Result result = new Result();

        result.setSuccess(false);
        result.setMessage(Type.RESULT_NOT_SET);

        Page<UserBoard> list = UserManager.getUserBoardList(paging, searchParams);

        if (list != null) {
            result.setSuccess(true);
            result.setMessage(Http.OK);
            result.setResult(list);
        } else {
            throw new ServerErrorException(null);
        }

        return result;
    }

    @GetMapping("/pay")
    public Result getPayInfo(
        @RequestHeader(value = "Authorization") String token,
        @RequestParam String type
    ) throws Exception {
        Result result = new Result();

        //토큰 아이디 확인
        HashMap<String, Object> info = JWTManager.read(token);
        User user = UserRepo.findUserByUserId((Integer) info.get("id"));

        kr.njbridge.tradeinfo.Type.Pay payType = null;

        try {
            payType = kr.njbridge.tradeinfo.Type.Pay.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ParamMismatchException(null);
        }

        List<PaySimple> list = PaySimpleRepo.getPayByUserId(user.getUserId(), payType.name(), PageRequest.of(0, 1));
        PaySimple paySimple = null;
        if(list.size() > 0) {
            paySimple = list.get(0);
        }

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("isPayed", paySimple != null);
        hashMap.put("pay", paySimple);

        result.setSuccess(true);
        result.setMessage(API.OK);
        result.setResult(hashMap);
        return result;
    }

    @GetMapping("/search")
    public Result searchNickName(
        @RequestHeader(value = "Authorization") String token,
        @PageableDefault(page = 0, size = 10, sort = "userId", direction = Sort.Direction.DESC) Pageable paging,
        @RequestParam String nickName
    ) throws Exception {
        Result result = new Result();

        HashMap<String, Object> info = JWTManager.read(token);
        User user = UserRepo.findUserByUserId((Integer) info.get("id"));

        HashMap<String, Object> search = new HashMap<>();
        search.put("NICKNAME", nickName);
        search.put("WITHOUT_EMAIL", user.getEmail());
        Page<UserBoard> list = UserManager.getUserBoardList(paging, search);

        if (list != null) {
            result.setSuccess(true);
            result.setMessage(Http.OK);
            result.setResult(list);
        } else {
            throw new ServerErrorException(null);
        }
        return result;
    }

    @PostMapping("/find/id")
    @WithOutAuth
    public Result findId(
        @RequestBody HashMap<String, Object> param
    ) throws Exception {
        Result result = new Result();
        HashMap<String, Object> hashMap = new HashMap<>();

        String name = (String) param.get("name");
        String phone = (String) param.get("phone");

        if(name == null || name.equals("")
        || phone == null || phone.equals("")) {
            throw new ParamRequiredException(null);
        }

        User user = UserRepo.findUserByNameAndPhone(name, phone);

        if(user == null) {
            result.setSuccess(false);
            result.setMessage(API.USER_EMPTY);
            return result;
        }

        hashMap.put("email", Common.emailMasking(user.getEmail()));

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

        User user = UserRepo.findUserByEmailAndNameAndPhone(email, name, phone);

        if(user == null) {
            result.setSuccess(false);
            result.setMessage(API.USER_EMPTY);
            return result;
        }

        // 이메일 인증 메일 발송
        UserManager.sendPasswordResetEmail(user);

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
            result.setMessage(kr.njbridge.core.Type.Verify.INVALID_VERIFY_TOKEN);
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
            result.setMessage(kr.njbridge.core.Type.Verify.VERIFY_TOKEN_EXPIRED);
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

        if(UserManager.leaveUser((Integer) info.get("id"))) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("delete", true);
            result.set(true, API.OK, hashMap);
        } else {
            result.set(false, API.DATA_NOT_FOUND);
        }

        return result;
    }
}
