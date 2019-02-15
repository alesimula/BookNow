package com.pdd.booknow;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.pdd.booknow.database.user.User;
import com.pdd.booknow.database.user.UserDatabase;

public class MainActivity extends AppCompatActivity {
    Context mContext;

    EditText name, password;
    CardView cardViewName, cardViewPassword;
    Button login;
    User user;
    public static final String EXTRA_USER = User.class.getCanonicalName();

    private void fillUserDatabase() {
        User.add(mContext, "admin@booknow.com", "administrator", "admin01");
        try {
            User admin = User.get(mContext,"admin@booknow.com", "admin01");
            admin.setName(mContext, "Adminolfi");
        }
        catch (Exception e) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getApplicationContext();
        fillUserDatabase();

        setContentView(R.layout.activity_main);

        name = (EditText)findViewById(R.id.name);
        password = (EditText)findViewById(R.id.password);
        login = (Button)findViewById(R.id.login);

        cardViewName = (CardView) findViewById(R.id.cardViewName);
        cardViewPassword = (CardView) findViewById(R.id.cardViewPassword);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = login();
                if (user!=null) {
                    Intent showResults = new Intent(MainActivity.this, ShowResults.class);
                    showResults.putExtra(EXTRA_USER, user);
                    startActivity(showResults);
                }
            }
        });
    }

    private User login()
    {
        String errorCodeName = null;
        String errorCodePassword = null;
        User user = null;
        boolean isEmail = name.getText().toString().contains("@");

        if (password.getText()==null || password.getText().length()==0) errorCodePassword = "Insert password";
        else if (!User.patternMatch(password.getText().toString(), User.MATCH_USERNAME_PASS)) errorCodePassword = "Incorrect password syntax";
        if (name.getText()==null || name.getText().length()==0) errorCodeName = "Insert username";
        else if (!User.patternMatch(name.getText().toString(), isEmail?User.MATCH_EMAIL:User.MATCH_USERNAME_PASS))
            errorCodeName = "Incorrect "+(isEmail?"e-mail":"username")+" syntax";
        else {
            try {
                user = User.get(mContext, name.getText().toString(), password.getText().toString());
                if (user==null) errorCodeName = "Invalid "+(isEmail?"e-mail":"username");
            }
            catch (UserDatabase.UserInvalidPasswordException e) {
                if (errorCodePassword==null) errorCodePassword = "Wrong password";
            }
        }

        if(errorCodeName!=null) {
            name.setError(errorCodeName);
            cardViewName.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.colorCardBackgroundError));
        }
        else {
            name.setError(null);
            cardViewName.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.colorCardBackground));
        }

        if(errorCodePassword!=null) {
            password.setError(errorCodePassword);
            cardViewPassword.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.colorCardBackgroundError));
        }
        else {
            password.setError(null);
            cardViewPassword.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.colorCardBackground));
        }

        return user;
    }
}
