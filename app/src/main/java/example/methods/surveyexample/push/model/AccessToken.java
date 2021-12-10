package example.methods.surveyexample.push.model;

import com.google.gson.annotations.SerializedName;

public class AccessToken {
    @SerializedName("grant_type")
    private String grantType;
    @SerializedName("client_id")
    private int client_id;
    @SerializedName("client_secret")
    private String client_secret;
    @SerializedName("access_token")
    private String access_token;

    /////////Getters///////////
    public String getGrantType() {
        return grantType;
    }

    public int getClient_id() {
        return client_id;
    }

    public String getClient_secret() {
        return client_secret;
    }

    public String getAccess_token() {
        return access_token;
    }

    /////////Setters///////////
    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public void setClient_id(int client_id) {
        this.client_id = client_id;
    }

    public void setClient_secret(String client_secret) {
        this.client_secret = client_secret;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }
}
