package example.methods.surveyexample.push;

import static example.methods.surveyexample.model.CloudDBZoneWrapper.no;
import static example.methods.surveyexample.model.CloudDBZoneWrapper.yes;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;

import example.methods.surveyexample.push.client.AccessTokenClient;
import example.methods.surveyexample.push.client.NotificationClient;
import example.methods.surveyexample.push.model.AccesTokenInterface;
import example.methods.surveyexample.push.model.AccessToken;
import example.methods.surveyexample.push.model.NotificationInterface;
import example.methods.surveyexample.push.model.NotificationMessageBody;
import example.methods.surveyexample.push.model.PushResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PushManager {

    public static final String ACCESS_TOKEN = "https://login.cloud.huawei.com/oauth2/";//(Base URL)
    public static String pushToken;
    private static String accessToken;

    public static void getToken(Context context) {
        String TAG = "GET_TOKEN";
        // Create a thread.
        new Thread() {
            @Override
            public void run() {
                try {
                    // Obtain the app ID from the agconnect-service.json file.
                    //String appId = "104698647";
                    // Set tokenScope to HCM.
                    String tokenScope = "HCM";
                    // privPushToken = HmsInstanceId.getInstance(context).getToken(appId, tokenScope);
                    pushToken = HmsInstanceId.getInstance(context).getToken("105037533", tokenScope);

                    Log.i(TAG, "get token: " + pushToken);
                    Log.i(TAG, "appId: " + 105037533);
                    sendRegTokenToServer(pushToken);
                    // Check whether the token is empty.
                    if (!TextUtils.isEmpty(pushToken)) {
                        sendRegTokenToServer(pushToken);
                    }
                } catch (ApiException e) {
                    Log.e(TAG, "get token failed, " + e.getStatusCode());
                }
            }
        }.start();
    }

    private static void sendRegTokenToServer(String token) {
        String TAG = "GET_TOKEN";
        Log.i(TAG, "sending token to server. token:" + token);
    }


    public static void getAccessToken() { // Access Token alacağınız fonksiyon


        String TAG = "GET_ACCESS_TOKEN";

        String app_secret = "a13cff277defdcce7b7e8db1ee3ca9a15995f5405b4658cd27992aa1ec21f791";


        AccesTokenInterface apiInterface;

        apiInterface = AccessTokenClient.getClient().create(AccesTokenInterface.class);
        Call<AccessToken> call = apiInterface.getAccessToken("client_credentials", Integer.valueOf(105037533), app_secret, false);
        call.enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                AccessToken newAccessToken = response.body();
                if (newAccessToken != null) {
                    Log.d(TAG, "Token " + response.body().getAccess_token());
                    accessToken = response.body().getAccess_token().replace("\\", "");
                }
            }

            @Override
            public void onFailure(Call<AccessToken> call, Throwable t) {
                Log.d("Push Activity", "ERROR " + t.getMessage());
            }
        });

    }

    public static void sendNotification(String pushToken, int choose, Context context) {

        float resultYes, resultNo;
        resultYes = (yes * 100) / (yes + no);
        resultNo = (no * 100) / (no + yes);
        Log.w("process", "processQueryResult: "+yes+" "+no );
        NotificationInterface apiInterface = NotificationClient.getPush().create(NotificationInterface.class);
        NotificationMessageBody notificationMessage = new NotificationMessageBody.Builder(
                "Güncel Sonuç", "Evet %" + resultYes + " Hayır %" + resultNo, pushToken)
                .build(choose);
        Call<PushResult> call = apiInterface.sendNotification(
                "Bearer " + accessToken,
                notificationMessage
        );
        call.enqueue(new Callback<PushResult>() {
            @Override
            public void onResponse(Call<PushResult> call, Response<PushResult> response) {
                Log.i("SEND NOTİFİCATİON", "accessToken: " + accessToken);
                if (accessToken == null) {
                    Toast.makeText(context, "Since you are logging in for the first time, you need to log in again.", Toast.LENGTH_LONG).show();
                }
                Log.e("SEND", "onResponse: " + response.errorBody() + " " + response.body() + " "
                        + response.message() + " " + response.code());
            }

            @Override
            public void onFailure(Call<PushResult> call, Throwable t) {
                Log.w("PUSH TOKEN", "ERROR" + notificationMessage);
                Log.w("PUSH TOKEN", "ERROR" + t.getLocalizedMessage());
                Log.w("PUSH TOKEN", "ERROR" + t.getMessage());
            }
        });


    }


}
