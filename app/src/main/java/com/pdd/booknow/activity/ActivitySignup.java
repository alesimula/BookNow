package com.pdd.booknow.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.pdd.booknow.R;
import com.pdd.booknow.database.user.User;

import java.io.Serializable;

import androidx.appcompat.app.AppCompatActivity;

public class ActivitySignup extends AppCompatActivity {

    Context mContext;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getApplicationContext();
        getSupportActionBar().hide();

        setContentView(R.layout.activity_signup);

        Intent intent = getIntent();
        Serializable obj = intent.getSerializableExtra(
                MainActivity.EXTRA_USER
        );
    }
}
