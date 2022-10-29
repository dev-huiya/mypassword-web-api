package me.huiya.core.Entity;

import com.fasterxml.jackson.annotation.JsonInclude;
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String errorMessage = null;

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

    public void set(Boolean success, Enum message, Object resultData, String errorMessage) {
        setSuccess(success);
        setMessage(message);
        setResultData(resultData);
        setErrorMessage(errorMessage);
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

    public Result(Boolean success, Enum message, Object resultData, String errorMessage) {
        setSuccess(success);
        setMessage(message);
        setResultData(resultData);
        setErrorMessage(errorMessage);
    }
}
