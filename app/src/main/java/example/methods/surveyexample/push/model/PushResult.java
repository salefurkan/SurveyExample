package example.methods.surveyexample.push.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PushResult {
    @SerializedName("code")
    @Expose
    private Integer code;
    @SerializedName("msg")
    @Expose
    private String msg;
    @SerializedName("requestId")
    @Expose
    private String requestId;

    public String getMsg() {
        return msg;
    }
}