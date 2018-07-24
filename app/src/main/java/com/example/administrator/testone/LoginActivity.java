package com.example.administrator.testone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button register = (Button)findViewById(R.id.register);
        Button login = (Button)findViewById(R.id.login);
        Button backUp = (Button)findViewById(R.id.closeAPP);
        login.setOnClickListener(this);
        register.setOnClickListener(this);
        backUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.register:
                Intent intent1 = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent1);
                break;
            case R.id.login:
                MainActivity.isLogin = true;
                finish();
                break;
            case R.id.closeAPP:
                finish();
                break;
            default:
                break;
        }
    }
}
