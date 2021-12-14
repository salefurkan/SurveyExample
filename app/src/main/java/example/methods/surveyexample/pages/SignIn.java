package example.methods.surveyexample.pages;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.EmailAuthProvider;
import com.huawei.agconnect.auth.EmailUser;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.agconnect.auth.VerifyCodeResult;
import com.huawei.agconnect.auth.VerifyCodeSettings;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hmf.tasks.TaskExecutors;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.account.service.AccountAuthService;

import java.util.Locale;

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
    private boolean mShowPass = false;
    EditText editText;


    ActivitySignInBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
        SharedPreferences sharedPreferences = this.getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (AGConnectAuth.getInstance().getCurrentUser() != null) {
            editor.putString("email", AGConnectAuth.getInstance().getCurrentUser().getEmail());
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        editText = binding.emailInput;

        binding.verifyCodeSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode();
            }
        });

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        binding.showPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShowPass = !mShowPass;
                if (mShowPass) {
                    binding.pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    binding.pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                binding.pass.setSelection(binding.pass.getText().length());
            }
        });
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmailUser emailUser = new EmailUser.Builder().
                        setEmail(editText.getText().toString())
                        .setPassword(binding.pass.getText().toString()).build();

                AGConnectAuth.getInstance().createUser(emailUser)
                        .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                            @Override
                            public void onSuccess(SignInResult signInResult) {
                                editor.putString("email", signInResult.getUser().getEmail());
                                Log.i(TAG, signInResult.getUser().getDisplayName() + " signIn success " + signInResult.getUser().getEmail());

                                Intent intent = new Intent(SignIn.this, MainActivity.class);
                                startActivity(intent);
                                finish();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.i(TAG, "onFailure: sigIn" + e.getMessage());
                    }
                });
            }
        });
        Log.i(TAG, "onCreate: ");

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

        binding.signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignIn.this, SignUp.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        SharedPreferences sharedPreferences = this.getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (requestCode == REQUEST_SIGN_IN_LOGIN) {
            //login success
            //get user message by parseAuthResultFromIntent
            Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data);
            if (authAccountTask.isSuccessful()) {
                AuthAccount authAccount = authAccountTask.getResult();
                dealWithResultOfSignIn(authAccount);
                editor.putString("email", authAccount.getEmail());

                finish();
                Intent intent = new Intent(SignIn.this, MainActivity.class);
                startActivity(intent);

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

    private void sendVerificationCode() {
        VerifyCodeSettings settings = VerifyCodeSettings.newBuilder()
                .action(VerifyCodeSettings.ACTION_REGISTER_LOGIN)
                .sendInterval(30) //shortest send interval ，30-120s
                .locale(Locale.ENGLISH) //optional,must contain country and language eg:zh_CN
                .build();
        String email = binding.emailInput.getText().toString().trim();
        Task<VerifyCodeResult> task = AGConnectAuth.getInstance().requestVerifyCode(email, settings);
        task.addOnSuccessListener(TaskExecutors.uiThread(), new OnSuccessListener<VerifyCodeResult>() {
            @Override
            public void onSuccess(VerifyCodeResult verifyCodeResult) {
                Toast.makeText(SignIn.this, "send email verify code success", Toast.LENGTH_SHORT).show();
                //You need to get the verification code from your email
            }
        }).addOnFailureListener(TaskExecutors.uiThread(), new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SignIn.this, "requestVerifyCode fail:" + e, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void login() {
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.pass.getText().toString().trim();
        String verifyCode = binding.verifyCode.getText().toString().trim();
        AGConnectAuthCredential credential;
        if (TextUtils.isEmpty(verifyCode)) {
            credential = EmailAuthProvider.credentialWithPassword(email, password);
        } else {
            //If you do not have a password, param password can be null
            credential = EmailAuthProvider.credentialWithVerifyCode(email, password, verifyCode);
        }
        signIn(credential);
    }

    private void signIn(AGConnectAuthCredential credential) {
        AGConnectAuth.getInstance().signIn(credential)
                .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {
                        startActivity(new Intent(SignIn.this, MainActivity.class));
                        SignIn.this.finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(SignIn.this, "signIn fail:" + e, Toast.LENGTH_SHORT).show();
                    }
                });
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