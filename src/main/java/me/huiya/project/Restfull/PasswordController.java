package me.huiya.project.Restfull;

import me.huiya.core.Common.JWTManager;
import me.huiya.core.Entity.Result;
import me.huiya.core.Entity.Token;
import me.huiya.core.Entity.User;
import me.huiya.core.Exception.ParamRequiredException;
import me.huiya.core.Repository.TokenRepository;
import me.huiya.core.Repository.UserRepository;
import me.huiya.core.Service.UserService;
import me.huiya.core.Type.API;
import me.huiya.project.Encrypt.Encrypt;
import me.huiya.project.Entity.Password;
import me.huiya.project.Repository.PasswordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.beans.ConstructorProperties;
import java.net.URL;
import java.util.HashMap;

@RestController
@RequestMapping(value="/password")
public class PasswordController {

    private static TokenRepository TokenRepo;
    private static UserRepository UserRepo;
    private static PasswordRepository PasswordRepo;
    private static JWTManager JWTManager;

    @ConstructorProperties({
        "TokenRepository",
        "UserRepository",
        "PasswordRepository",
        "JWTManager",
    })
    public PasswordController(
        TokenRepository TokenRepo,
        UserRepository UserRepo,
        PasswordRepository PasswordRepo,
        JWTManager JWTManager
    ) {
        this.TokenRepo = TokenRepo;
        this.UserRepo = UserRepo;
        this.PasswordRepo = PasswordRepo;
        this.JWTManager = JWTManager;
    }

    @GetMapping({"s", "/list", "/all"})
    public Result getList(
        @RequestHeader(value = "Authorization") String token,
        @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) throws Exception {
        Result result = new Result();

        //토큰 아이디 확인
        HashMap<String, Object> info = JWTManager.read(token);
        User user = UserRepo.findUserByUserId((Integer) info.get("id"));

        Page<Password> list = PasswordRepo.getListByUserId(user.getUserId(), pageable);
        for(Password password : list.getContent()) {
            password.setUsername(Encrypt.serverToClient(password.getUsername(), token));
        }

        if(list != null) {
            result.set(true, API.OK, list);
        } else {
            result.set(false, API.SERVER_ERROR);
        }

        return result;
    }

    @PostMapping({"", "/new"})
    public Result create(
        @RequestHeader(value = "Authorization") String token,
        @RequestBody HashMap<String, Object> param
    ) throws Exception {
        Result result = new Result();

        //토큰 아이디 확인
        HashMap<String, Object> info = JWTManager.read(token);
        User user = UserRepo.findUserByUserId((Integer) info.get("id"));

        String _url = (String) param.get("url");
        String _username = (String) param.get("username");
        String _password = (String) param.get("password");

        if(
            _url == null
            || _url.equals("")
            || _username == null
            || _username.equals("")
            || _password == null
            || _password.equals("")
        ) {
            throw new ParamRequiredException("All values are required when saving the password");
        }

        URL url = new URL(_url);
        Token tokenEntity = TokenRepo.getTokenByToken(token.replace(JWTManager.HEADER_TOKEN_KEY, ""));

        Password password = new Password();
        password.setUserId(user.getUserId());
        password.setUrl(_url);
        password.setProtocol(url.getProtocol());
        password.setHost(url.getHost());
        password.setPort(url.getPort());
        password.setPath(url.getPath());
        password.setQuery(url.getQuery());

        password.setUsername(Encrypt.clientToServer(_username, tokenEntity));
        password.setPassword(Encrypt.clientToServer(_password, tokenEntity));

        PasswordRepo.save(password);

        if(password.getId() != null) {
            HashMap<String, Object> data = new HashMap<>();
            data.put("success", true);
            result.set(true, API.OK, data);
        } else {
            result.set(false, API.CREATE_FAIL);
        }
        return result;
    }

    @GetMapping("/{passwordId}")
    public Result getDetail(
        @RequestHeader(value = "Authorization") String token,
        @PathVariable(name = "passwordId") Integer passwordId
    ) throws Exception {
        Result result = new Result();

        //토큰 아이디 확인
        HashMap<String, Object> info = JWTManager.read(token);
        User user = UserRepo.findUserByUserId((Integer) info.get("id"));

        Password password = PasswordRepo.findAllByIdAndUserId(passwordId, user.getUserId());

        if(password != null) {
            result.set(true, API.OK, password);
        } else {
            result.set(false, API.DATA_NOT_FOUND);
        }
        return result;
    }

    @PutMapping("/{passwordId}")
    public Result update(
        @RequestHeader(value = "Authorization") String token,
        @PathVariable(name = "passwordId") Integer passwordId,
        @RequestBody HashMap<String, Object> param
    ) throws Exception {
        Result result = new Result();

        //토큰 아이디 확인
        HashMap<String, Object> info = JWTManager.read(token);
        User user = UserRepo.findUserByUserId((Integer) info.get("id"));
        Token tokenEntity = TokenRepo.getTokenByToken(token.replace(JWTManager.HEADER_TOKEN_KEY, ""));

        Password password = PasswordRepo.findAllByIdAndUserId(passwordId, user.getUserId());

        if(password == null) {
            return result.set(false, API.DATA_NOT_FOUND);
        }

        String _url = (String) param.get("url");
        String _username = (String) param.get("username");
        String _password = (String) param.get("password");

        if(_url != null && !_url.equals("")) {
            URL url = new URL(_url);
            password.setUrl(_url);
            password.setProtocol(url.getProtocol());
            password.setHost(url.getHost());
            password.setPort(url.getPort());
            password.setPath(url.getPath());
            password.setQuery(url.getQuery());
        }

        if(_username != null && !_password.equals("")) {
            password.setUsername(Encrypt.clientToServer(_username, tokenEntity));
        }

        if(_password != null && !_password.equals("")) {
            password.setPassword(Encrypt.clientToServer(_password, tokenEntity));
        }

        if(password != null) {
            return result.set(true, API.OK, password);
        }
        return result.set(false, API.UPDATE_FAIL);
    }

    @DeleteMapping("/{passwordId}")
    public Result delete(
        @RequestHeader(value = "Authorization") String token,
        @PathVariable(name = "passwordId") Integer passwordId
    ) throws Exception {
        Result result = new Result();

        //토큰 아이디 확인
        HashMap<String, Object> info = JWTManager.read(token);
        User user = UserRepo.findUserByUserId((Integer) info.get("id"));

        Password password = PasswordRepo.findAllByIdAndUserId(passwordId, user.getUserId());

        if(password == null) {
            return result.set(false, API.DATA_NOT_FOUND);
        }

        PasswordRepo.delete(password);

        if(password != null) {
            return result.set(true, API.OK, password);
        }
        return result.set(false, API.DELETE_FAIL);
    }
}
