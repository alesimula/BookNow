package com.pdd.booknow.database.user;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.content.res.AppCompatResources;

import com.pdd.booknow.R;
import com.pdd.booknow.util.sql.SQLiteTableHelper;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class User implements Serializable {
    private String email, username;
    private BigInteger password;

    public static Pattern MATCH_EMAIL = Pattern.compile("([a-zA-Z\\d_%\\+\\.\\-]+)(@)([a-zA-Z\\d\\.\\-]+)(\\.)([a-zA-Z]{2,64})");
    public static Pattern MATCH_USERNAME_PASS = Pattern.compile("([a-zA-Z\\d_\\.\\-]{6,64})");

    public User() {
        this("guest@booknow.com", "guest", (BigInteger)null);
    }
    User(String email, String username, BigInteger password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }
    private User(String email, String username, String password) {
        this(email, username, UserDatabase.encodePassword(password));
    }

    public static User add(Context context, String email, String username, String password) {
        UserDatabase db = UserDatabase.getInstance(context);
        if (!patternMatch(email, MATCH_EMAIL) || !patternMatch(username, MATCH_USERNAME_PASS) || !patternMatch(password, MATCH_USERNAME_PASS)) return null;
        User user_e = db.getUser(email);
        User user_u = db.getUser(username);
        if (user_e==null && user_u==null) {
            User user = new User(email, username, password);
            if (db.insertUser(email, username, user.password, null) >= 0) return user;
        }
        return null;
    }
    public static User get(Context context, String key, String password) throws UserDatabase.UserInvalidPasswordException {
        User user = UserDatabase.getInstance(context).getUser(key);
        if (user==null) return null;
        if (password==null || password.isEmpty() || !UserDatabase.encodePassword(user,password).equals(user.password))
            throw new UserDatabase.UserInvalidPasswordException(user);
        return user;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(Context context, String email) {
        if (UserDatabase.getInstance(context).updateValue(this, UserDatabase.COLUMN_EMAIL, email) > 0)
            this.email = email;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(Context context, String username) {
        if (UserDatabase.getInstance(context).updateValue(this, UserDatabase.COLUMN_USERNAME, username) > 0)
            this.username = username;
    }

    public BigInteger getPassword() {
        return password;
    }
    public void setPassword(Context context, String password) {
        BigInteger pass = UserDatabase.encodePassword(this,password);
        if (UserDatabase.getInstance(context).updateValue(this, UserDatabase.COLUMN_PASSWORD, pass) > 0)
            this.password = pass;
    }

    public String getSalt() {
        return SQLiteTableHelper.parseSalt(password);
    }

    public Drawable getProfileIcon(Context context) {
        Bitmap bitmap = UserDatabase.getInstance(context).getUserProfileIcon(email);
        if (bitmap != null) return new BitmapDrawable(context.getResources(), bitmap);
        return AppCompatResources.getDrawable(context, R.drawable.ic_user_default);
    }
    public void setProfileIcon(Context context, Bitmap bitmap) {
        UserDatabase.getInstance(context).updateValue(this, UserDatabase.COLUMN_ICON, bitmap);
    }

    public static boolean patternMatch(String string, Pattern pattern) {
        if (string==null) return false;
        try {
            String matched=null;
            Matcher m = pattern.matcher(string);
            if (m.matches()) matched = m.group(0);
            if (matched!=null && matched.equals(string)) return true;
        } catch (Exception e) {}
        return false;
    }
}
