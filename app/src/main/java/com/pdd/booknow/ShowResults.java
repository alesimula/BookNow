package com.pdd.booknow;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pdd.booknow.database.user.User;

import java.io.Serializable;

public class ShowResults extends AppCompatActivity {

    Context mContext;

    User user;
    RelativeLayout header;
    TextView welcomeText;
    Button back;
    boolean fail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getApplicationContext();

        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        Serializable obj = intent.getSerializableExtra(
                MainActivity.EXTRA_USER
        );

        if(obj instanceof User) user = (User)obj;
        else{
            user = new User();
            fail = true;
        }

        header = (RelativeLayout) findViewById(R.id.header);
        welcomeText = (TextView) findViewById(R.id.welcomeText);
        back = (Button) findViewById(R.id.back);

        if (!fail) welcomeText.setText(welcomeText.getText().toString().replace("%s", user.getEmail()));
        else {
            welcomeText.setText("Access denied");
            header.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorBackgroundDarkError));
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                finishActivity(0);
            }
        });
    }
}
