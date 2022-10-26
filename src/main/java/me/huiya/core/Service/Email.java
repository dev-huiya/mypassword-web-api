package me.huiya.core.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.HashMap;

@Component
public class Email {

    // Autowired 대신 추천되는 의존성 주입 방식
    private static JavaMailSender javaMailSender;
    private static SpringTemplateEngine templateEngine;

    @ConstructorProperties({"JavaMailSender", "SpringTemplateEngine"})
    public Email(JavaMailSender javaMailSender, SpringTemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    private static String UI_SERVER_URL;

    @Value("${core.SERVER.UI}")
    public void setServerUrl(String serverUrl) { UI_SERVER_URL = serverUrl; }

    @Value("${spring.mail.username}")
    private String emailSender;

    /**
     * 이메일 발송 함수
     * @param title 이메일 제목
     * @param to 받는 사람
     * @param templateName 이메일 템플릿
     * @param values 이메일에 들어가는 값
     * @throws MessagingException
     * @throws IOException
     */
    @Async
    public void send(String title, String to, String templateName, HashMap<String, String> values) throws MessagingException, IOException {

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        //메일 제목 설정
        helper.setSubject(title);

        //수신자 설정
        helper.setTo(to);

        //발신자 설정
        helper.setFrom(emailSender);

        //템플릿에 전달할 데이터 설정
        Context context = new Context();
        context.setVariable("UI_SERVER_URL", UI_SERVER_URL);
        values.forEach((key, value)->{
            context.setVariable(key, value);
        });

        //메일 내용 설정 : 템플릿 프로세스
        String html = templateEngine.process(templateName, context);
        helper.setText(html, true);

        //메일 보내기
        javaMailSender.send(message);
    }
}