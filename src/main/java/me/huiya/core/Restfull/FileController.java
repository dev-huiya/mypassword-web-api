package me.huiya.core.Restfull;

import me.huiya.core.Common.FileManager;
import me.huiya.core.Common.JWTManager;
import me.huiya.core.Config.WithOutAuth;
import me.huiya.core.Entity.File;
import me.huiya.core.Entity.Result;
import me.huiya.core.Entity.User;
import me.huiya.core.Exception.ForbiddenException;
import me.huiya.core.Repository.FileRepository;
import me.huiya.core.Repository.UserRepository;
import me.huiya.core.Type.API;
import me.huiya.core.Type.Http;
import me.huiya.core.Type.Type;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.beans.ConstructorProperties;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@RestController
@RequestMapping(value="/")
public class FileController {

    // Autowired 대신 추천되는 의존성 주입 방식
    private static FileRepository FileRepo;
    private static UserRepository UserRepo;
    private static JWTManager JWTManager;
    private static SpringTemplateEngine templateEngine;

    @ConstructorProperties({
        "FileRepository",
        "UserRepository",
        "JWTManager",
        "SpringTemplateEngine",
    })
    public FileController(
        FileRepository FileRepo,
        UserRepository UserRepo,
        JWTManager JWTManager,
        SpringTemplateEngine templateEngine
    ) {
        this.FileRepo = FileRepo;
        this.UserRepo = UserRepo;
        this.templateEngine = templateEngine;
        this.JWTManager = JWTManager;
    }

    private static String UI_SERVER_URL;

    @Value("${core.SERVER.UI}")
    public void setUiServerUrl(String uiServerUrl) { UI_SERVER_URL = uiServerUrl; }

    @Cacheable("image")
    @RequestMapping(value={"/image/{hash}"})
    @WithOutAuth
    public ResponseEntity<byte[]> getImage(@PathVariable String hash) throws Exception {

        File file = FileRepo.findByHash(hash);
        if(file == null) {
            throw new ForbiddenException("Forbidden");
        }

        byte[] fileBytes = FileManager.get(hash);
        if(fileBytes == null) {
            // return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            throw new ForbiddenException("Forbidden");
        }

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDisposition(
                ContentDisposition.builder("inline")
                        .filename(URLEncoder.encode(file.getName(),"UTF-8").replace("+", "%20"))
                        .build()
        );
        //headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//        headers.setCacheControl("must-revalidate");
//        headers.setAccessControlMaxAge(24 * 60 * 1000);

        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
    }

    @RequestMapping("/file/{hash}")
    @WithOutAuth
    public ResponseEntity<byte[]> getFile(@PathVariable String hash) throws Exception {

        File file = FileRepo.findByHash(hash);
        byte[] fileBytes = FileManager.get(hash);

        if(file == null || fileBytes == null) {
//            throw new ForbiddenException("Forbidden");

            HashMap<String, String> values = new HashMap<>();

            //템플릿에 전달할 데이터 설정
            Context context = new Context();
            context.setVariable("UI_SERVER_URL", UI_SERVER_URL);
            values.forEach((key, value)->{
                context.setVariable(key, value);
            });

            //템플릿 프로세스
            String html = templateEngine.process("file-forbidden", context);

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);

            return new ResponseEntity<>(html.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
        }

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                        .filename(URLEncoder.encode(file.getName(),"UTF-8").replace("+", "%20"))
                        .build()
        );
//        headers.setCacheControl("must-revalidate");
//        headers.setAccessControlMaxAge(24 * 60 * 1000);

        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
    }

    @RequestMapping(value={"/file/{hash}/info"})
    public Result getFileInfo(@PathVariable String hash) throws Exception {
        Result result = new Result();

        File file = FileRepo.findByHash(hash);
        if(file == null) {
            result.setSuccess(false);
            result.setMessage(Http.NOT_FOUND);
            return result;
        }

        result.setSuccess(true);
        result.setMessage(Http.OK);
        result.setResult(file);

        return result;
    }

    @RequestMapping(value = "/file/upload")
    public Result upload(
        @RequestHeader(value = "Authorization") String token,
        @RequestPart @RequestParam(required = false) MultipartFile file
    ) throws Exception {
        Result result = new Result();
        result.setSuccess(false);
        result.setMessage(Type.RESULT_NOT_SET);

        //토큰 아이디 확인
        HashMap<String, Object> info = JWTManager.read(token);
        User user = UserRepo.findUserByUserId((Integer) info.get("id"));

        String hash = FileManager.save(file, user.getUserId());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("hash", hash);
        hashMap.put("filename", file.getOriginalFilename());
        hashMap.put("size", file.getSize());
        result.setResult(hashMap);

        result.setSuccess(true);
        result.setMessage(API.OK);

        return result;
    }
}
