package me.huiya.core.Common;

import ua_parser.Client;
import ua_parser.Parser;

import javax.servlet.http.HttpServletRequest;

public class UserAgentParser {

    /**
     * 유저 에이전트를 정해진 포맷으로 출력한다
     * @param request HttpServletRequest 객체
     * @return ex) "Windows 10 / Chrome 87"
     */
    public static String getUserAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");

        Parser uaParser = new Parser();
        Client c = uaParser.parse(ua);

        // TODO: 모바일 혹은 앱을 구분할 수 있는 규칙이 필요함.

        return c.os.family + " " + c.os.major + " / " + c.userAgent.family + " " + c.userAgent.major;
    }
}
