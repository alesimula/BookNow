package com.pdd.booknow.activity;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.content.ContextCompat;
import androidx.databinding.BindingMethod;
import androidx.databinding.BindingMethods;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.pdd.booknow.R;
import com.pdd.booknow.Utilities;
import com.pdd.booknow.database.user.User;
import com.pdd.booknow.database.user.UserDatabase;

@BindingMethods({
        @BindingMethod(
                type = Guideline.class,
                attribute = "app:layout_constraintGuide_percent",
                method = "setGuidelinePercent")
})
public class MainActivity extends AppCompatActivity {
    Context mContext;

    EditText name, password;
    CardView cardViewName, cardViewPassword;
    Button login, signup, guestLogin;
    User user;
    public static final String EXTRA_USER = User.class.getCanonicalName();

    private void fillUserDatabase() {
        User.add(mContext, "admin@booknow.com", "administrator", "admin01");
        User.add(mContext, "luiggi@hotmail.com", "luiggi", "luigi01");
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
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null) actionBar.setTitle("Login");
        if (Utilities.INSTANCE.isLandscape() && actionBar!=null) actionBar.hide();
        fillUserDatabase();

        //DataBindingUtil.setContentView(this, R.layout.activity_main);
        ViewDataBinding v = DataBindingUtil.setContentView(this, R.layout.activity_main);
        //setContentView(R.layout.activity_main);

        cardViewName = findViewById(R.id.tinputcard);
        cardViewPassword = findViewById(R.id.tinputcard2);

        name = (EditText)cardViewName.findViewById(R.id.input_card_text);
        //javax.xml.bind.JAXBException e;
        password = (EditText)cardViewPassword.findViewById(R.id.input_card_text);
        login = (Button)findViewById(R.id.login);
        signup = (Button)findViewById(R.id.signup);
        //guestLogin = (Button)findViewById(R.id.guestLogin);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = login();
                if (user!=null) {
                    Intent showResults = new Intent(MainActivity.this, ActivityIDK.class);
                    showResults.putExtra("logged", true);
                    startActivity(showResults);
                }
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent activitySignup = new Intent(MainActivity.this, ActivitySignup.class);
                startActivity(activitySignup);
            }
        });

        /*guestLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showResults = new Intent(MainActivity.this, ActivityIDK.class);
                showResults.putExtra(EXTRA_USER, new User());
                startActivity(showResults);
            }
        });*/
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("cardcolor_name", cardViewName.getCardBackgroundColor());
        outState.putParcelable("cardcolor_password", cardViewPassword.getCardBackgroundColor());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        cardViewName.setCardBackgroundColor(savedInstanceState.getParcelable("cardcolor_name"));
        cardViewPassword.setCardBackgroundColor(savedInstanceState.getParcelable("cardcolor_password"));
    }
}
