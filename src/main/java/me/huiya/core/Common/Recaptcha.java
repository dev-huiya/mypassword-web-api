package me.huiya.core.Common;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Recaptcha {
    private static final Logger logger = LoggerFactory.getLogger(FileManager.class.getClass());

    private static String secretKey;
    @Value("${core.recaptcha-secret-key}")
    public void setSecretKey(String key) {
        secretKey = key;
    }

    /**
     * 구글 리캡챠를 검증한다.
     * 구글 서버 응답이 true고 점수가 0.5 이상일때 true로 판정한다.
     * @param recaptchaToken
     * @return
     */
    public static boolean verify(String recaptchaToken) {
        try {
            HttpResponse<JsonNode> request = Unirest.post("https://www.google.com/recaptcha/api/siteverify")
                    .field("secret", secretKey)
                    .field("response", recaptchaToken)
                    .asJson();

            JSONObject res = request.getBody().getObject();

            logger.debug("recaptcha response: " + res);

            if((Boolean) res.get("success") != true) {
                return false;
            } else if((Boolean) res.get("success") == true && (Double) res.get("score") > 0.5) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

    }
}
