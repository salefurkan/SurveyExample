package example.methods.surveyexample.push.client;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NotificationClient {

    private static Retrofit retrofit = null;
    private static final String pushBaseUrl = "https://push-api.cloud.huawei.com/";
    private static final int TIMEOUT = 500000;

    public static Retrofit getInstance() {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(loggingInterceptor).build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(pushBaseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    private Retrofit getClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor).build();
        return new Retrofit.Builder().baseUrl(pushBaseUrl).client(client).addConverterFactory(GsonConverterFactory.create()).build();
    }

    public static Retrofit getPush() {
        // change your base URL
        if (retrofit == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT, TimeUnit.MICROSECONDS)
                    .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .addInterceptor(interceptor).build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(pushBaseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            //return retrofit;
        }
        return retrofit;
    }

    public static Retrofit getPushExp() {
        // change your base URL
        if (retrofit == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT, TimeUnit.MICROSECONDS)
                    .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .addInterceptor(interceptor).build();
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://push-api.cloud.huawei.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            //return retrofit;
        }
        return retrofit;
    }
}
