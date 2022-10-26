package me.huiya.core.Common;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Random;

public class Common {


    /**
     * 안전한 랜덤 문자열을 생성하는 메소드.
     *
     * @param length 길이
     * @return base64 인코딩된 SecureRandom byte
     */
    public static String createSecureRandom(Integer length) {
        final Random r = new SecureRandom();
        byte[] salt = new byte[length];
        r.nextBytes(salt);
        Base64.Encoder encoder = Base64.getEncoder();
        return new String(encoder.encode(salt));
    }

    /**
     * 무작위 대소문자를 생성하는 메소드
     *
     * @param length 길이
     * @return 랜덤한 대소문자
     */
    public static String createRandom(Integer length) {
        Random rnd = new SecureRandom();
        String random = "";

        for(int i=0;i<length;i++) {
            if(rnd.nextBoolean()) {
                random += (char)((int)(rnd.nextInt(26))+97); // 소문자
            } else {
                random += (char)((int)(rnd.nextInt(26))+65); // 대문자
            }
        }

        return random;
    }

    /**
     * 숫자를 포함한 무작위 대소문자를 생성하는 메소드
     *
     * @param length 길이
     * @return 숫자를 포함한 랜덤한 대소문자
     */
    public static String createRandomWithInt(Integer length) {
        Random rnd = new SecureRandom();
        StringBuffer buf = new StringBuffer();
        for(int i=0;i<length;i++){
            // rnd.nextBoolean() 는 랜덤으로 true, false 를 리턴. true일 시 랜덤 한 소문자를, false 일 시 랜덤 한 숫자를 StringBuffer 에 append 한다.
            if(rnd.nextBoolean()){
                if(rnd.nextBoolean()) {
                    buf.append((char)((int)(rnd.nextInt(26))+97)); // 소문자
                } else {
                    buf.append((char)((int)(rnd.nextInt(26))+65)); // 대문자
                }
            }else{
                buf.append((rnd.nextInt(10)));
            }
        }

        return new String(buf);
    }

    /**
     * Byte array to HEX
     *
     * @param str byte array
     * @return
     */
    public static String byteArrayToHex(byte[] str) {
        StringBuilder sb = new StringBuilder();
        for(final byte b: str)
            sb.append(String.format("%02x", b&0xff));
        return sb.toString();
    }

    /**
     * 주어진 범위 안에서 랜덤한 정수 하나를 뽑아온다.
     * @param min 최소값
     * @param max 최대값
     * @return
     */
    public static Integer getRandom(Integer min, Integer max) {
        return (new Random()).nextInt(max + 1 - min) + min;
    }

    public static String emailMasking(String email) {
        return email.replaceAll("(^[^@]{3}|(?!^)\\G)[^@]", "$1*");
    }

    public static HashMap<String, Object> queryStringToMap(String queryString) throws UnsupportedEncodingException {
        HashMap<String, Object> res = new HashMap<>();

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            res.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }

        return res;
    }

    public static String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }
}
