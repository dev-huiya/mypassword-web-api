package me.huiya.core.Common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Calendar;

@Component
public class JWTManagerCommon {

    private static String accessExpiredValue;
    private static String refreshExpiredValue;

    @Value("${core.JWT.access-expired}")
    public void setAccessExpiredValue(String value) {
        accessExpiredValue = value;
    }
    @Value("${core.JWT.refresh-expired}")
    public void setRefreshExpiredValue(String value) {
        refreshExpiredValue = value;
    }

    public static Integer getAccessExpireNumeric() {
        return Integer.parseInt(accessExpiredValue.replaceAll("\\D", "")); // \D 정규식 숫자제외 제거
    }

    public static int getAccessExpireTimeUnit() {
        return _getTimeUnit(accessExpiredValue.replaceAll("\\d", "")); // \d 정규식 숫자 제거
    }

    public static Integer getRefreshExpireNumeric() {
        return Integer.parseInt(refreshExpiredValue.replaceAll("\\D", "")); // \D 정규식 숫자제외 제거
    }

    public static int getRefreshExpireTimeUnit() {
        return _getTimeUnit(refreshExpiredValue.replaceAll("\\d", "")); // \d 정규식 숫자 제거
    }

    /**
     * 정해진 시간 단위를 Calendar 단위로 변환
     *
     * @param unit
     * @return
     */
    private static int _getTimeUnit(String unit) {
        int result;
        switch (unit) {
            case "s":
            case "S":
                result = Calendar.SECOND;
                break;
            case "m":
                result = Calendar.MINUTE;
                break;
            case "d":
            case "D":
                result = Calendar.DATE;
                break;
            case "M":
                result = Calendar.MONTH;
                break;
            case "y":
            case "Y":
                result = Calendar.YEAR;
                break;
            case "h":
            case "H":
            default:
                result = Calendar.HOUR;
                break;
        }

        return result;
    }

}
