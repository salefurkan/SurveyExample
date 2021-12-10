package example.methods.surveyexample.push.client;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AccessTokenClient {
    static String accessTokenBaseUrl = "https://oauth-login.cloud.huawei.com/";
    static int TIMEOUT = 500000;

    public static Retrofit getClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor).build();
        return new Retrofit.Builder().baseUrl(accessTokenBaseUrl).client(client).addConverterFactory(GsonConverterFactory.create()).build();
    }
}
