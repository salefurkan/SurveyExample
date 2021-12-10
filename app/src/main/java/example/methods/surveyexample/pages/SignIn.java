package example.methods.surveyexample.pages;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.account.service.AccountAuthService;

import example.methods.surveyexample.MainActivity;
import example.methods.surveyexample.databinding.ActivitySignInBinding;

public class SignIn extends AppCompatActivity {

    public static final int IS_LOG = 1;
    //login
    public static final int REQUEST_SIGN_IN_LOGIN = 1002;
    //login by code
    public static final int REQUEST_SIGN_IN_LOGIN_CODE = 1003;
    //Log tag
    public static final String TAG = "HuaweiIdActivity";
    private AccountAuthService mAuthManager;
    private AccountAuthParams mAuthParam;

    public static String userId, userName, userSurname;


    ActivitySignInBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        binding.huaweId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuthParam = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                        .setProfile()
                        .setEmail()
                        .setIdToken()
                        .setAccessToken()
                        .createParams();
                mAuthManager = AccountAuthManager.getService(SignIn.this, mAuthParam);
                startActivityForResult(mAuthManager.getSignInIntent(), REQUEST_SIGN_IN_LOGIN);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SIGN_IN_LOGIN) {
            //login success
            //get user message by parseAuthResultFromIntent
            Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data);
            if (authAccountTask.isSuccessful()) {
                AuthAccount authAccount = authAccountTask.getResult();
                dealWithResultOfSignIn(authAccount);
                finish();
                startActivity(new Intent(SignIn.this, MainActivity.class));

                Log.i(TAG, authAccount.getDisplayName() + " signIn success " + authAccount.getEmail());
                Log.i(TAG, "AccessToken: " + authAccount.getAccessToken());
            } else {
                Log.i(TAG, "signIn failed: " + ((ApiException) authAccountTask.getException()).getStatusCode());
            }
        }
        if (requestCode == REQUEST_SIGN_IN_LOGIN_CODE) {
            //login success
            Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data);
            if (authAccountTask.isSuccessful()) {
                AuthAccount authAccount = authAccountTask.getResult();
                Log.i(TAG, "signIn get code success.");
                Log.i(TAG, "ServerAuthCode: " + authAccount.getAuthorizationCode());

            } else {
                Log.i(TAG, "signIn get code failed: " + ((ApiException) authAccountTask.getException()).getStatusCode());
            }
        }
    }

    private void dealWithResultOfSignIn(AuthAccount authAccount) {
        userId = authAccount.getOpenId();
        userName = authAccount.getDisplayName();
        userSurname = authAccount.getEmail();
        Log.i(TAG, "display: " + authAccount.getDisplayName());
        Log.i(TAG, "photo uri string: " + authAccount.getAvatarUri());
        Log.i(TAG, "photo uri: " + authAccount.getAvatarUri());
        Log.i(TAG, "email: " + authAccount.getEmail());
        Log.i(TAG, "openid: " + authAccount.getOpenId());
        Log.i(TAG, "unionid: " + authAccount.getUnionId());
    }


}