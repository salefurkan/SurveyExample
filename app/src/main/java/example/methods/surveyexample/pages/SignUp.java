package example.methods.surveyexample.pages;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.EmailUser;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.agconnect.auth.VerifyCodeResult;
import com.huawei.agconnect.auth.VerifyCodeSettings;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hmf.tasks.TaskExecutors;

import java.util.Locale;

import example.methods.surveyexample.MainActivity;
import example.methods.surveyexample.R;

public class SignUp extends AppCompatActivity {
    private EditText countryCodeEdit;
    private EditText accountEdit;
    private EditText passwordEdit;
    private EditText verifyCodeEdit;
    private TextView registerBtn;
    private RelativeLayout sendBtn;
    private boolean mShowPass = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initView();

        findViewById(R.id.showPass).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShowPass = !mShowPass;
                if (mShowPass) {
                    passwordEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    passwordEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                passwordEdit.setSelection(passwordEdit.getText().length());
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode();
            }
        });
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        getSupportActionBar().hide();
    }

    private void initView() {
        accountEdit = findViewById(R.id.email_input);
        passwordEdit = findViewById(R.id.pass);
        verifyCodeEdit = findViewById(R.id.verifyCode);
        registerBtn = findViewById(R.id.registerBtn);
        sendBtn = findViewById(R.id.verifyCodeSend);
    }

    private void login() {
        String email = accountEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();
        String verifyCode = verifyCodeEdit.getText().toString().trim();
        // build email user
        EmailUser emailUser = new EmailUser.Builder()
                .setEmail(email)
                .setPassword(password)//optional,if you set a password, you can log in directly using the password next time.
                .setVerifyCode(verifyCode)
                .build();
        // create email user
        AGConnectAuth.getInstance().createUser(emailUser)
                .addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {
                        // After a user is created, the user has logged in by default.
                        startActivity(new Intent(SignUp.this, MainActivity.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(SignUp.this, "createUser fail:" + e, Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void sendVerificationCode() {
        VerifyCodeSettings settings = VerifyCodeSettings.newBuilder()
                .action(VerifyCodeSettings.ACTION_REGISTER_LOGIN)// action type
                .sendInterval(30) //shortest send interval ，30-120s
                .locale(Locale.ENGLISH) //optional,must contain country and language eg:zh_CN
                .build();
        String email = accountEdit.getText().toString().trim();
        // apply for a verification code by email, indicating that the email is owned by you.
        Task<VerifyCodeResult> task = AGConnectAuth.getInstance().requestVerifyCode(email, settings);
        task.addOnSuccessListener(TaskExecutors.uiThread(), new OnSuccessListener<VerifyCodeResult>() {
            @Override
            public void onSuccess(VerifyCodeResult verifyCodeResult) {
                Toast.makeText(SignUp.this, "send email verify code success", Toast.LENGTH_SHORT).show();
                //You need to get the verification code from your email
            }
        }).addOnFailureListener(TaskExecutors.uiThread(), new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SignUp.this, "requestVerifyCode fail:" + e, Toast.LENGTH_SHORT).show();
            }
        });
    }

}