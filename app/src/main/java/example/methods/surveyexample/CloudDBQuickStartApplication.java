package example.methods.surveyexample;

import static example.methods.surveyexample.push.PushManager.getAccessToken;
import android.app.Application;
import example.methods.surveyexample.model.CloudDBZoneWrapper;

public class CloudDBQuickStartApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CloudDBZoneWrapper.initAGConnectCloudDB(this);
        getAccessToken();
    }
    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
