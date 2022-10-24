package me.huiya.core.Type;

public enum API {
    /**
     * 성공
     */
    OK,

    /**
     * API 서버 에러
     */
    SERVER_ERROR,

    /**
     * API 제한량 초과
     */
    QUOTA_LIMIT,

    /**
     * 권한 없음
     */
    PERMISSION_DENIED,

    /**
     * 데이터 찾을 수 없음
     */
    DATA_NOT_FOUND,

    CREATE_FAIL,

    READ_FAIL,

    UPDATE_FAIL,

    DELETE_FAIL,

    /**
     * 중복되면 안되는 데이터가 중복됨
     */
    DATA_DUPLICATE_ERROR,

    /**
     * 필수값 누락됨
     */
    PARAM_REQUIRED,

    /**
     * 파라매터 타입 일치하지 않음
     */
    PARAM_MISMATCH,

    /**
     * 잘못된 파라매터
     */
    PARAM_WRONG,

    /**
     * 사용자 정보 없음
     */
    USER_EMPTY,
}
