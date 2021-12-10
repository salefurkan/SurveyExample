package example.methods.surveyexample.push.model;


import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public interface AccesTokenInterface {
    @FormUrlEncoded
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=UTF-8")
    @POST("oauth2/v3/token")
    Call<AccessToken> getAccessToken(
            @Field("grant_type") String grantType,
            @Field("client_id") int clientId,
            @Field("client_secret") String clientSecret,
            @Field("open_uid") boolean uId);
}
