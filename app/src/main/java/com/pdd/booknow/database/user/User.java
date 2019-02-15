package com.pdd.booknow.database.user;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.content.res.AppCompatResources;

import com.pdd.booknow.R;
import com.pdd.booknow.proto.UserInfo;
import com.pdd.booknow.util.sql.SQLiteTableHelper;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class User implements Serializable {
    static final byte[] INFO_EMPTY() {return UserInfo.Info.newBuilder().build().toByteArray();}
    private static final byte[] INFO_GUEST() {return UserInfo.Info.newBuilder().setName("Guest").build().toByteArray();}

    private String email, username;
    private BigInteger password;
    private byte[] info;

    public static Pattern MATCH_EMAIL = Pattern.compile("([a-zA-Z\\d_%\\+\\.\\-]+)(@)([a-zA-Z\\d\\.\\-]+)(\\.)([a-zA-Z]{2,64})");
    public static Pattern MATCH_USERNAME_PASS = Pattern.compile("([a-zA-Z\\d_\\.\\-]{6,64})");

    public User() {
        this("guest@booknow.com", "guest", (BigInteger)null, INFO_GUEST());
    }
    User(String email, String username, BigInteger password, byte[] info) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.info = info;
    }
    private User(String email, String username, String password) {
        this(email, username, UserDatabase.encodePassword(password), INFO_EMPTY());
    }

    public static User add(Context context, String email, String username, String password) {
        UserDatabase db = UserDatabase.getInstance(context);
        if (!patternMatch(email, MATCH_EMAIL) || !patternMatch(username, MATCH_USERNAME_PASS) || !patternMatch(password, MATCH_USERNAME_PASS)) return null;
        User user_e = db.getUser(email);
        User user_u = db.getUser(username);
        if (user_e==null && user_u==null) {
            User user = new User(email, username, password);
            if (db.insertUser(email, username, user.password, user.info, null) >= 0) return user;
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

    public void setName(Context context, String name){
        byte[] info = getInfo(context).toBuilder().setName(name).build().toByteArray();
        if (UserDatabase.getInstance(context).updateValue(this, UserDatabase.COLUMN_INFO, info) > 0)
            this.info = info;
    }

    public void setSurname(Context context, String surname){
        byte[] info = getInfo(context).toBuilder().setSurname(surname).build().toByteArray();
        if (UserDatabase.getInstance(context).updateValue(this, UserDatabase.COLUMN_INFO, info) > 0)
            this.info = info;
    }

    public void addPhone(Context context, long number, UserInfo.Info.PhoneType type){
        UserInfo.Info.PhoneNumber phoneNumber = UserInfo.Info.PhoneNumber.newBuilder().setNumber(number).setType(type).build();
        byte[] info = getInfo(context).toBuilder().addPhones(phoneNumber).build().toByteArray();
        if (UserDatabase.getInstance(context).updateValue(this, UserDatabase.COLUMN_INFO, info) > 0)
            this.info = info;
    }

    public void addCreditCard(Context context, String name, String surname, String number, int cvv, int expMonth, int expYear){
        UserInfo.Info.CreditCard creditCard = UserInfo.Info.CreditCard.newBuilder().setOwnerName(name).setOwnerSurname(surname)
                .setNumber(number).setCvv(cvv).setExpMonth(expMonth).setExpYear(expYear).build();
        byte[] info = getInfo(context).toBuilder().addCreditCards(creditCard).build().toByteArray();
        if (UserDatabase.getInstance(context).updateValue(this, UserDatabase.COLUMN_INFO, info) > 0)
            this.info = info;
    }

    public UserInfo.Info getInfo() {
        return getInfo(null);
    }
    public UserInfo.Info getInfo(@Nullable Context context) {
        try {
            if (context==null) return UserInfo.Info.parseFrom(this.info);
            UserDatabase db = UserDatabase.getInstance(context);
            byte[] updatedInfo = db.getUserInfo(email);
            if (updatedInfo!=null) this.info = updatedInfo;
            if (this.info == null) {
                UserInfo.Info info = UserInfo.Info.newBuilder().build();
                db.updateValue(this, UserDatabase.COLUMN_INFO, info);
                this.info = info.toByteArray();
                return info;
            } else return UserInfo.Info.parseFrom(this.info);
        }
        catch (Exception e){
            return UserInfo.Info.newBuilder().build();
        }
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
