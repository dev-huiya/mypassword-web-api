package me.huiya.core.Restfull;

import me.huiya.core.Config.WithOutAuth;
import me.huiya.core.Entity.Result;
import me.huiya.core.Type.Type;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping(value="/")
public class RootController {

    @Value("${core.version}")
    private String projectVersion;

    @RequestMapping(value={"/", "/status"})
    @WithOutAuth
    public Result index() {
        Result result = new Result();
        result.setSuccess(true);
        result.setMessage(Type.OK);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("isServerRun", true);
        hashMap.put("version", projectVersion);
        result.setResult(hashMap);
        return result;
    }
}
