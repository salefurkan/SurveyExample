package example.methods.surveyexample.push.model;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificationInterface {
    @Headers("Content-Type:application/json; charset=UTF-8")
    //104698647
    @POST("v1/105037533/messages:send")
    Call<PushResult> sendNotification(
            @Header("Authorization") String authorization, @Body NotificationMessageBody notificationMessageBody);

}
