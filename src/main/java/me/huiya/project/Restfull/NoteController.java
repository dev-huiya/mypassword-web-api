package me.huiya.project.Restfull;

import me.huiya.core.Common.JWTManager;
import me.huiya.core.Entity.Result;
import me.huiya.core.Entity.Token;
import me.huiya.core.Entity.User;
import me.huiya.core.Exception.ParamRequiredException;
import me.huiya.core.Repository.TokenRepository;
import me.huiya.core.Repository.UserRepository;
import me.huiya.core.Type.API;
import me.huiya.project.Encrypt.Encrypt;
import me.huiya.project.Entity.Note;
import me.huiya.project.Entity.Password;
import me.huiya.project.Repository.NoteRepository;
import me.huiya.project.Repository.PasswordRepository;
import me.huiya.project.Type.NoteType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.beans.ConstructorProperties;
import java.net.URL;
import java.util.HashMap;

@RestController
@RequestMapping(value="/note")
public class NoteController {

    private static TokenRepository TokenRepo;
    private static UserRepository UserRepo;
    private static NoteRepository NoteRepo;
    private static JWTManager JWTManager;

    @ConstructorProperties({
        "TokenRepository",
        "UserRepository",
        "NoteRepository",
        "JWTManager",
    })
    public NoteController(
        TokenRepository TokenRepo,
        UserRepository UserRepo,
        NoteRepository NoteRepo,
        JWTManager JWTManager
    ) {
        this.TokenRepo = TokenRepo;
        this.UserRepo = UserRepo;
        this.NoteRepo = NoteRepo;
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

        Page<Note> list = NoteRepo.findNotesByUserId(user.getUserId(), pageable);
        for(Note password : list.getContent()) {
            password.setContents(Encrypt.serverToClient(password.getContents(), token));
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

        NoteType _type = null;
        String type = (String) param.get("type");
        String contents = (String) param.get("contents");

        try {
            _type = NoteType.valueOf(type);
        } catch (IllegalArgumentException exception) {
            throw new ParamRequiredException("note type not valid");
        }

        if(
            _type == null ||
            !StringUtils.hasText(contents)
        ) {
            throw new ParamRequiredException("All values are required when saving the note");
        }

        Token tokenEntity = TokenRepo.getTokenByToken(token.replace(JWTManager.HEADER_TOKEN_KEY, ""));

        Note note = new Note();
        note.setUserId(user.getUserId());
        note.setType(_type);
        note.setContents(Encrypt.clientToServer(contents, tokenEntity));

        NoteRepo.save(note);

        if(note.getId() != null) {
            HashMap<String, Object> data = new HashMap<>();
            data.put("success", true);
            result.set(true, API.OK, data);
        } else {
            result.set(false, API.CREATE_FAIL);
        }
        return result;
    }

    @GetMapping("/{noteId}")
    public Result getDetail(
        @RequestHeader(value = "Authorization") String token,
        @PathVariable(name = "noteId") Integer noteId
    ) throws Exception {
        Result result = new Result();

        //토큰 아이디 확인
        HashMap<String, Object> info = JWTManager.read(token);
        User user = UserRepo.findUserByUserId((Integer) info.get("id"));
        Token tokenEntity = TokenRepo.getTokenByToken(token.replace(JWTManager.HEADER_TOKEN_KEY, ""));

        Note note = NoteRepo.findAllByIdAndUserId(noteId, user.getUserId());

        if(note != null) {
            note.setContents(Encrypt.serverToClient(note.getContents(), tokenEntity));
            result.set(true, API.OK, note);
        } else {
            result.set(false, API.DATA_NOT_FOUND);
        }
        return result;
    }

    @PutMapping("/{noteId}")
    public Result update(
        @RequestHeader(value = "Authorization") String token,
        @PathVariable(name = "noteId") Integer noteId,
        @RequestBody HashMap<String, Object> param
    ) throws Exception {
        Result result = new Result();

        //토큰 아이디 확인
        HashMap<String, Object> info = JWTManager.read(token);
        User user = UserRepo.findUserByUserId((Integer) info.get("id"));
        Token tokenEntity = TokenRepo.getTokenByToken(token.replace(JWTManager.HEADER_TOKEN_KEY, ""));

        Note note = NoteRepo.findAllByIdAndUserId(noteId, user.getUserId());

        if(note == null) {
            return result.set(false, API.DATA_NOT_FOUND);
        }

        String contents = (String) param.get("contents");

        Boolean isEdit = false;

        if(StringUtils.hasText(contents)) {
            note.setContents(Encrypt.clientToServer(contents, tokenEntity));
            isEdit = true;
        }

        if(isEdit) {
            NoteRepo.save(note);
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("update", true);
        return result.set(true, API.OK, data);
    }

    @DeleteMapping("/{noteId}")
    public Result delete(
        @RequestHeader(value = "Authorization") String token,
        @PathVariable(name = "noteId") Integer noteId
    ) throws Exception {
        Result result = new Result();

        //토큰 아이디 확인
        HashMap<String, Object> info = JWTManager.read(token);
        User user = UserRepo.findUserByUserId((Integer) info.get("id"));

        Note note = NoteRepo.findAllByIdAndUserId(noteId, user.getUserId());

        if(note == null) {
            return result.set(false, API.DATA_NOT_FOUND);
        }

        NoteRepo.delete(note);

        HashMap<String, Object> data = new HashMap<>();
        data.put("delete", true);
        return result.set(true, API.OK, data);
    }
}
