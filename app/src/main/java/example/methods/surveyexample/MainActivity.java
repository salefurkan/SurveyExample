package example.methods.surveyexample;

import static example.methods.surveyexample.model.CloudDBZoneWrapper.no;
import static example.methods.surveyexample.model.CloudDBZoneWrapper.yes;
import static example.methods.surveyexample.pages.SignIn.TAG;
import static example.methods.surveyexample.pages.SignIn.userId;
import static example.methods.surveyexample.pages.SignIn.userName;
import static example.methods.surveyexample.pages.SignIn.userSurname;
import static example.methods.surveyexample.push.PushManager.getAccessToken;
import static example.methods.surveyexample.push.PushManager.getToken;
import static example.methods.surveyexample.push.PushManager.pushToken;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.agconnect.auth.SignInResult;
import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.ListenerHandler;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.analytics.HiAnalyticsTools;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.service.AccountAuthService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import example.methods.surveyexample.adapter.SurveyAdapter;
import example.methods.surveyexample.databinding.ActivityMainBinding;
import example.methods.surveyexample.model.CloudDBZoneWrapper;
import example.methods.surveyexample.model.LoginHelper;
import example.methods.surveyexample.model.Votes;
import example.methods.surveyexample.pages.SignIn;

public class MainActivity extends AppCompatActivity implements CloudDBZoneWrapper.UiCallBack, LoginHelper.OnLoginEventCallBack {

    ActivityMainBinding binding;

    private AccountAuthService mAuthManager;
    private AccountAuthParams mAuthParam;
    private static HiAnalyticsInstance instance;
    private AGConnectCloudDB mCloudDB;
    private ListenerHandler mRegister;
    private LoginHelper mLoginHelper;

    private CloudDBZoneConfig mConfig;
    private Handler mHandler = null;
    private CloudDBZoneWrapper.UiCallBack mUiCallBack;
    private SurveyAdapter mSurveyAdapter;
    private CloudDBZoneWrapper mCloudDBZoneWrapper;

    public MainActivity() {
        mCloudDBZoneWrapper = new CloudDBZoneWrapper();
    }

    private SignIn sActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle("TURKEY PULL");

        mHandler = new Handler(Looper.getMainLooper());
        mSurveyAdapter = new SurveyAdapter(getApplicationContext());

        mLoginHelper = new LoginHelper(this);

        // TODO: Initialize Analytics Kit.
        // Enable Analytics Kit logging.
        HiAnalyticsTools.enableLog();
        getToken(MainActivity.this);
        getAccessToken();
        // Generate an Analytics Kit instance.
        instance = HiAnalytics.getInstance(this);

        instance.setAnalyticsEnabled(true);
        instance.setRestrictionEnabled(false);
        instance.setUserId(userId);
        instance.setUserProfile(userName, userSurname);

        Votes surveyInfo = new Votes();
        mHandler.post(() -> {
            LoginHelper loginHelper = this.getLoginHelper();
            loginHelper.addLoginCallBack(this);
            loginHelper.login();
        });
        mHandler.post(() -> {
            mCloudDBZoneWrapper.addCallBacks(MainActivity.this);
            mCloudDBZoneWrapper.createObjectType();
            mCloudDBZoneWrapper.openCloudDBZoneV2(MainActivity.this);

        });
        getToken(MainActivity.this);
        binding.report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getToken(MainActivity.this);
                getAccessToken();
                if (binding.yesAnswer.isChecked()) {
                    surveyInfo.setId(0);
                    surveyInfo.setNo(no);
                    surveyInfo.setYes(yes + 1);
                    mHandler.post(() -> {
                        mCloudDBZoneWrapper.upsertBookInfos(surveyInfo, MainActivity.this);
                    });
                    reportAnswerEvt("yes", MainActivity.this);
                } else if (binding.notAnswer.isChecked()) {
                    surveyInfo.setId(0);
                    surveyInfo.setYes(yes);
                    surveyInfo.setNo(no + 1);
                    mHandler.post(() -> {
                        mCloudDBZoneWrapper.upsertBookInfos(surveyInfo, MainActivity.this);
                    });
                    reportAnswerEvt("no", MainActivity.this);
                } else if (!binding.notAnswer.isChecked() && !binding.yesAnswer.isChecked()) {
                    Toast.makeText(MainActivity.this, "Lütfen bir adet cevap seçiniz.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ChartActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.exit) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Do You Want To Exit? ");
            builder.setCancelable(true);

            builder.setNegativeButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    startActivity(new Intent(MainActivity.this, SignIn.class));
                }
            });
            builder.setPositiveButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.design_default_color_primary));
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.hwid_auth_button_color_red));

        }

        return true;
        //return super.onOptionsItemSelected(item);
    }

    LoginHelper getLoginHelper() {
        return mLoginHelper;
    }

    public static void reportAnswerEvt(String answer, Context context) {

        // TODO: Report a custom event.
        // Event Name: Answer
        // Event Parameters:
        //  -- question: String
        //  -- answer: String
        //  -- answerTime: String
        // Initialize parameters.

        Bundle bundle = new Bundle();
        bundle.putString("answer", answer);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        bundle.putString("answerTime", sdf.format(new Date()));

        // Report a custom event.

        instance.onEvent("Answer", bundle);
        instance.setPushToken(pushToken);

        //sendNotification(pushToken, 0, context);

        Toast.makeText(context, "Cevap başarıyla gönderildi.", Toast.LENGTH_SHORT).show();

    }


    private void signOut() {
        Task<Void> signOutTask = mAuthManager.signOut();
        signOutTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.i("Main", "onSuccess: signOut");
                Intent intent = new Intent(MainActivity.this, SignIn.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.i("Main", "onFailure: signOut");
            }
        });
    }


    private void cancelAuthorization() {
        mAuthParam = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setProfile()
                .setAuthorizationCode()
                .createParams();
        mAuthManager = AccountAuthManager.getService(MainActivity.this, mAuthParam);
        Task<Void> task = mAuthManager.cancelAuthorization();
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Intent intent = new Intent(MainActivity.this, SignIn.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                Log.i(TAG, "cancelAuthorization success");
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.i(TAG, "cancelAuthorization failure：" + e.getClass().getSimpleName());
            }
        });
    }


    @Override
    public void onAddOrQuery(List<Votes> bookInfoList) {
        Log.i(TAG, "Using default onAddOrQuery");
        Log.i(TAG, "onAddOrQuery: TEST " + bookInfoList.get(0).getYes());
    }

    @Override
    public void isDataUpsert(Boolean state) {
        Log.i(TAG, "Using add value");
    }

    @Override
    public void updateUiOnError(String errorMessage) {
        Log.i(TAG, "Using default updateUiOnError weqwqe");
    }


    @Override
    public void onLogin(boolean showLoginUserInfo, SignInResult signInResult) {

    }

    @Override
    public void onLogOut(boolean showLoginUserInfo) {

    }
}