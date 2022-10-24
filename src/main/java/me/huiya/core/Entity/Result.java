package me.huiya.core.Entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.huiya.core.Type.Type;

@Getter
@Setter
@ToString
public class Result {
    private Boolean success = false;
    private Enum message = Type.RESULT_NOT_SET;
    private Object resultData = null;

    public void setResult(Object resultData) {
        this.resultData = resultData;
    }

//    public Object getResult() { return this.resultData; }

    public void set(Boolean success, Enum message) {
        setSuccess(success);
        setMessage(message);
    }

    public void set(Boolean success, Enum message, Object resultData) {
        setSuccess(success);
        setMessage(message);
        setResultData(resultData);
    }

    public Result() {
    }

    public Result(Boolean success, Enum message) {
        setSuccess(success);
        setMessage(message);
    }

    public Result(Boolean success, Enum message, Object resultData) {
        setSuccess(success);
        setMessage(message);
        setResultData(resultData);
    }
}
