package kr.njbridge.core.Type;

public enum Verify {

    /**
     * OK
     */
    OK,

    /**
     * 유효하지 않은 인증 토큰
     */
    INVALID_VERIFY_TOKEN,

    /**
     * 날짜 만료됨
     */
    VERIFY_TOKEN_EXPIRED,
}
