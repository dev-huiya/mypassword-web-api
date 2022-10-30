package me.huiya.project.Restfull;

import me.huiya.core.Common.JWTManager;
import me.huiya.core.Entity.Result;
import me.huiya.core.Repository.TokenRepository;
import me.huiya.core.Repository.UserRepository;
import me.huiya.core.Service.UserService;
import me.huiya.project.Repository.PasswordRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.beans.ConstructorProperties;

@RestController
@RequestMapping(value="/password")
public class PasswordController {

    private static TokenRepository TokenRepo;
    private static UserRepository UserRepo;
    private static PasswordRepository PasswordRepo;

    @ConstructorProperties({
        "TokenRepository",
        "UserRepository",
        "PasswordRepository",
    })
    public PasswordController(
        TokenRepository TokenRepo,
        UserRepository UserRepo,
        PasswordRepository PasswordRepo
    ) {
        this.TokenRepo = TokenRepo;
        this.UserRepo = UserRepo;
        this.PasswordRepo = PasswordRepo;
    }

    @GetMapping({"s", "/list", "/all"})
    public Result getList(@RequestHeader(value = "Authorization") String token) {
        Result result = new Result();

        return result;
    }
}
