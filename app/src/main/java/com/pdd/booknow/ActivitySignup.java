package com.pdd.booknow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pdd.booknow.database.user.User;

import java.io.Serializable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class ActivitySignup extends AppCompatActivity {

    Context mContext;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getApplicationContext();

        setContentView(R.layout.activity_signup);

        Intent intent = getIntent();
        Serializable obj = intent.getSerializableExtra(
                MainActivity.EXTRA_USER
        );
    }
}
