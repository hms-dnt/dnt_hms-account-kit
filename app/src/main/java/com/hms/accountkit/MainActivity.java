package com.hms.accountkit;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;


public class MainActivity extends AppCompatActivity {

    private int REQUEST_SIGN_IN_LOGIN = 1002;
    private int REQUEST_SIGN_IN_LOGIN_CODE = 1003;

    private TextView textName;
    private TextView textToken;
    private HuaweiIdAuthService mHuaweiIdAuthService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addListener();

        textName = findViewById(R.id.textViewName);
        textToken = findViewById(R.id.textViewToken);

    }

    void singIn(int mode) {
        HuaweiIdAuthParams mHuaweiIdAuthParams;
        if (mode == REQUEST_SIGN_IN_LOGIN) {
            mHuaweiIdAuthParams = new HuaweiIdAuthParamsHelper (HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setIdToken().createParams();
        } else {
            mHuaweiIdAuthParams = new HuaweiIdAuthParamsHelper (HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setAuthorizationCode().createParams();
        }

        mHuaweiIdAuthService = HuaweiIdAuthManager.getService (MainActivity.this, mHuaweiIdAuthParams);

        startActivityForResult(mHuaweiIdAuthService.getSignInIntent(), mode);
    }

    public void addListener() {
        Button buttonToken = findViewById(R.id.buttonToken);
        buttonToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singIn(REQUEST_SIGN_IN_LOGIN);
            }
        });

        Button buttonCode= findViewById(R.id.buttonCode);
        buttonCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singIn(REQUEST_SIGN_IN_LOGIN_CODE);
            }
        });

        Button buttonSignOut= findViewById(R.id.buttonSignout);
        buttonSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mHuaweiIdAuthService.equals(null)) {
                    Task<Void> signOutTask = mHuaweiIdAuthService.signOut();
                    signOutTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            setText("", "");
                        }
                    });
                }
            }
        });

    }

    void setText(String name, String token) {
        textName.setText(name);
        textToken.setText(token);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SIGN_IN_LOGIN || requestCode == REQUEST_SIGN_IN_LOGIN_CODE) {

            Task< AuthHuaweiId> authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data);
            if (authHuaweiIdTask.isSuccessful()) {
                AuthHuaweiId huaweiAccount = authHuaweiIdTask.getResult();
                if ( requestCode == REQUEST_SIGN_IN_LOGIN ) {
                    setText("S " + huaweiAccount.getDisplayName(), huaweiAccount.getIdToken());
                } else {
                    setText(huaweiAccount.getDisplayName(), huaweiAccount.getAuthorizationCode());
                }
            } else {
                setText("Error","CODE " + ((ApiException) authHuaweiIdTask.getException()).getStatusCode());
            }
        } else {
            Log.i(String.valueOf(requestCode), String.valueOf(resultCode));
            setText("Error", "CODE " + resultCode);
        }
    }

    void alert(String message){

        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();

        dialog.setTitle(message);
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                            }
        });
        dialog.show();

    }
}
