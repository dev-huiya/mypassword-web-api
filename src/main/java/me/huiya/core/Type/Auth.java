package me.huiya.core.Type;

public enum Auth {
    /**
     * JWT 암호화 키가 없음 : 서버 에러
     */
    JWT_KEY_EMPTY,

    /**
     * JWT 생성 실패 : 서버 에러
     */
    JWT_ERROR,

    /**
     * JWT 만료됨
      */
    JWT_EXPIRED_ERROR,

    /**
     * JWT 검증 실패
     */
    JWT_VERIFY_ERROR,

    /**
     * JWT 아직 활성화되지 않음, jjwt 라이브러리에 명확한 오류 구분자가 없어 제거됨
     */
    //JWT_BEFORE_ERROR,

    /**
     * 올바르지 않은 토큰 정보
     */
    JWT_INVALID_CLAIM,

    /**
     * 지원하지 않는 알고리즘 : 서버 에러
     */
    JWT_ALGORITHM_ERROR,

    /**
     * 잘못된 계정 정보 (아이디 혹은 비밀번호)
     */
    AUTH_WRONG,

    /**
     * 로그인 필요
     */
    AUTH_REQUIRED,

    /**
     * 관리자 권한 필요
     */
    MANAGER_AUTHORITY_REQUIRED,

    /**
     * 계정정보 중복됨
     */
    JOIN_DUPLICATE,

    /**
     * 캡챠 토큰 없음
     */
    CAPTCHA_EMPTY,

    /**
     * 캡챠 실패
     */
    CAPTCHA_FAIL,
    
    /**
     * 입력된 패스워드가 맞지 않습니다.
     */
    PASSWORD_CHANGE_FAIL,

    /**
     * 차단됨.
     */
    BANNED,

    /**
     * Email 미인증
     */
    EMAIL_VERIFY_REQUIRED,

    /**
     * 성공
     */
    OK
}