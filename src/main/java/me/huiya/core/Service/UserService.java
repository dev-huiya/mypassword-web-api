package me.huiya.core.Service;

import me.huiya.core.Common.Common;
import me.huiya.core.Encrypt.SHA256Util;
import me.huiya.core.Entity.User;
import me.huiya.core.Entity.Verify;
import me.huiya.core.Repository.UserRepository;
import me.huiya.core.Repository.VerifyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.beans.ConstructorProperties;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class UserService {

    // Autowired 대신 추천되는 의존성 주입 방식
    private static UserRepository UserRepo;
    private static VerifyRepository VerifyRepo;
    private static Email EmailService;

    @ConstructorProperties({
        "UserRepository",
        "VerifyRepository",
        "Email",
    })
    public UserService(
        UserRepository UserRepo,
        VerifyRepository VerifyRepo,
        Email EmailService
    ) {
        this.UserRepo = UserRepo;
        this.VerifyRepo = VerifyRepo;
        this.EmailService = EmailService;
    }

    public static User getUserbyEmailAndPassword(String email, String password) {
        String salt = UserRepo.findSaltByEmail(email);
        SHA256Util sha256Util = new SHA256Util();
        return UserRepo.findUserByEmailAndPassword(email, sha256Util.encrypt(salt + password));
    }

    public static boolean emailCheck(String email) {
        Integer count = 1;
        if (email != null && email != "") {
            count = UserRepo.countByEmail(email);
        }
        return count <= 0 ? true : false;
    }

    public static boolean nickNameCheck(String nickName) {
        Integer count = 1;
        if (nickName != null && !nickName.equals("")) {
            count = UserRepo.countByNickName(nickName);
        }
        return count <= 0 ? true : false;
    }

    public static String createEmailVerifyCode() {

        String code = null;
        do {
            code = Common.createRandom(16);
        } while (!(code != null && VerifyRepo.countByCode(code) <= 0));

        return code;
    }

    public static void sendJoinVerifyEmail(User user) throws Exception {
        if (user.getEmail() == null) {
            throw new Exception("Email required");
        }

        // 랜덤 코드 발급
        String code = createEmailVerifyCode();

        // 현재 시간
        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);

        // 만료시간 하루 뒤로 설정
        cal.add(Calendar.DATE, 1);
        Date expire = cal.getTime();

        // 인증정보 생성
        Verify verify = new Verify();
        verify.setCode(code);
        verify.setType("JOIN");
        verify.setUserId(user.getUserId());
        verify.setExpire(expire);

        VerifyRepo.save(verify);

        // 이메일 발송
        HashMap<String, String> emailValues = new HashMap<>();
        emailValues.put("name", user.getNickName());
        emailValues.put("verifyCode", code);

        EmailService.send("트레이드인포 이메일 인증", user.getEmail(), "email-verify", emailValues);
    }

    public static void sendPasswordResetEmail(User user) throws Exception {
        if (user.getEmail() == null) {
            throw new Exception("Email required");
        }

        // 랜덤 코드 발급
        String code = createEmailVerifyCode();

        // 현재 시간
        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);

        // 만료시간 한시간 뒤로 설정
        cal.add(Calendar.HOUR, 1);
        Date expire = cal.getTime();

        // 인증정보 생성
        Verify verify = new Verify();
        verify.setCode(code);
        verify.setType("PASSWORD");
        verify.setUserId(user.getUserId());
        verify.setExpire(expire);

        VerifyRepo.save(verify);

        // 이메일 발송
        HashMap<String, String> emailValues = new HashMap<>();
        emailValues.put("name", user.getNickName());
        emailValues.put("verifyCode", code);

        EmailService.send("트레이드인포 비밀번호 재설정", user.getEmail(), "password-reset", emailValues);
    }

    public static void sendWelcomeMail(User user) throws Exception {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");

        // 이메일 발송
        HashMap<String, String> emailValues = new HashMap<>();
        emailValues.put("name", user.getNickName());
        emailValues.put("time", dateFormat.format(user.getCreateTime() != null ? user.getCreateTime() : new Date()));
        emailValues.put("email", user.getEmail());

        EmailService.send("트레이드인포 회원가입을 환영합니다.", user.getEmail(), "welcome", emailValues);
    }

    public static boolean leaveUser(Integer userId) throws Exception {
        User user = UserRepo.findUserByUserId(userId);

        if(user == null) {
            return false;
        }

        UserRepo.delete(user);
        return true;
    }
}
