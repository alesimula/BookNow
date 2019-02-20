package com.pdd.booknow.database.user;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import com.pdd.booknow.util.sql.SQLiteTableHelper;

import java.math.BigInteger;

/**
 * Database locale per simulare la registrazione degli utenti
 * Non proprio hack-proof, ma vabbè
 */
public class UserDatabase {

    private final UserTable mUserDb;
    public final static String COLUMN_EMAIL = "user_email";
    public final static String COLUMN_USERNAME = "user_username";
    public final static String COLUMN_PASSWORD = "user_password";
    public final static String COLUMN_INFO = "user_info";
    public final static String COLUMN_ICON = "user_icon";

    private static UserDatabase instance;
    public static UserDatabase getInstance(Context context) {
        return (instance==null)?(instance=new UserDatabase(context)):instance;
    }
    private UserDatabase(Context context) {
        mUserDb = new UserTable(context);
    }

    public User getUser(@NonNull String key) {
        String[] tableColumns = new String[]{this.COLUMN_EMAIL, this.COLUMN_USERNAME, this.COLUMN_PASSWORD, this.COLUMN_INFO};
        String whereClause = (key.contains("@") ? this.COLUMN_EMAIL : this.COLUMN_USERNAME) + " = ?";
        try (Cursor c = mUserDb.query(tableColumns, whereClause, new String[]{key})) {
            if (c.moveToNext()) {
                return new User(
                        c.getString(0),
                        c.getString(1),
                        new BigInteger(c.getBlob(2)),
                        c.getBlob(3)
                );
            }
        } catch (SQLiteException e) {
            Log.i(this.getClass().getSimpleName(), "No database entry for "+(key.contains("@") ? "email" : "username")+' '+key);
        }
        return null;
    }

    public Bitmap getUserProfileIcon(@NonNull String key) {
        String[] tableColumns = new String[]{this.COLUMN_ICON};
        String whereClause = (key.contains("@") ? this.COLUMN_EMAIL : this.COLUMN_USERNAME) + " = ?";
        try (Cursor c = mUserDb.query(tableColumns, whereClause, new String[]{key})) {
            if (c.moveToNext()) {
                byte[] data = c.isNull(0) ? null : c.getBlob(0);
                if (data!=null) try {
                    return BitmapFactory.decodeByteArray(data, 0, data.length, null);
                } catch (Exception e) {}
            }
        } catch (SQLiteException e) {
            Log.i(this.getClass().getSimpleName(), "No database entry for "+(key.contains("@") ? "email" : "username")+' '+key);
        }
        return null;
    }

    public byte[] getUserInfo(@NonNull String key) {
        String[] tableColumns = new String[]{this.COLUMN_INFO};
        String whereClause = (key.contains("@") ? this.COLUMN_EMAIL : this.COLUMN_USERNAME) + " = ?";
        try (Cursor c = mUserDb.query(tableColumns, whereClause, new String[]{key})) {
            if (c.moveToNext()) {
                return c.isNull(0) ? null : c.getBlob(0);
            }
        } catch (SQLiteException e) {
            Log.i(this.getClass().getSimpleName(), "No database entry for "+(key.contains("@") ? "email" : "username")+' '+key);
        }
        return null;
    }

    private long insertUser(@NonNull String email, @NonNull String username, @NonNull byte[] password, byte[] info, Bitmap icon) {
        ContentValues values = new ContentValues();
        values.put(this.COLUMN_EMAIL, email);
        values.put(this.COLUMN_USERNAME, username);
        values.put(this.COLUMN_PASSWORD, password);
        values.put(this.COLUMN_INFO, info);
        values.put(this.COLUMN_ICON, SQLiteTableHelper.encodeBitmap(icon));
        return mUserDb.insertOrRollback(values);
    }
    public long insertUser(@NonNull String email, @NonNull String username, @NonNull BigInteger password, byte[] info, Bitmap icon) {
        if (info==null) info = User.INFO_EMPTY().toByteArray();
        return insertUser(email, username, password.toByteArray(), info, icon);
    }

    private int updateValue(@NonNull String key, @NonNull String column, Object value) {
        ContentValues values = new ContentValues();
        SQLiteTableHelper.putValue(values, column, value);
        String whereClause = (key.contains("@") ? this.COLUMN_EMAIL : this.COLUMN_USERNAME) + " = ?";
        try {
            return mUserDb.update(values, whereClause, new String[]{key});
        } catch (Exception e) {
            Log.i(this.getClass().getSimpleName(), "No database entry for "+(key.contains("@") ? "email" : "username")+' '+key);
        }
        return 0;
    }
    int updateValue(@NonNull User user, @NonNull String column, Object value) {
        return updateValue(user.getEmail(), column, value);
    }
    public int updateValue(@NonNull User user, @NonNull String password, @NonNull String column, Object value) throws UserInvalidPasswordException {
        if (encodePassword(user, password).equals(user.getPassword())) return updateValue(user.getEmail(), column, value);
        throw new UserInvalidPasswordException(user);
    }

    static BigInteger encodePassword(String password) {
        return SQLiteTableHelper.encodePassword(password);
    }
    public static BigInteger encodePassword(User user, String password) {
        return SQLiteTableHelper.encodePassword(user!=null ? user.getPassword() : null ,password);
    }

    public static class UserInvalidPasswordException extends IllegalAccessException {
        UserInvalidPasswordException(User user) {
            super(UserDatabase.class.getSimpleName()+": Invalid password for "+user.getEmail());
        }
    }

    private static final class UserTable extends SQLiteTableHelper {

        private final static String TABLE_NAME = "users";
        public UserTable(Context context) {
            super(context, TABLE_NAME+".db", 1, TABLE_NAME);
        }

        @Override
        protected void onCreateTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" (" +
                    COLUMN_EMAIL+" VARCHAR PRIMARY KEY CHECK ("+COLUMN_EMAIL+" REGEXP '[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}' AND "+COLUMN_EMAIL+" <> '"+User.GUEST_EMAIL+"'), " +
                    COLUMN_USERNAME+" VARCHAR UNIQUE NOT NULL CHECK ("+COLUMN_USERNAME+" REGEXP '[A-Z0-9a-z._-]{6,64}' AND "+COLUMN_USERNAME+" <> '"+User.GUEST_USERNAME+"'), " +
                    COLUMN_PASSWORD+" BLOB NOT NULL CHECK (LENGTH("+COLUMN_PASSWORD+") = 64), " +
                    COLUMN_INFO+" BLOB NOT NULL, " +
                    COLUMN_ICON+" BLOB " +
                    ");");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_username ON "+TABLE_NAME+" ("+COLUMN_USERNAME+")");
        }

        private void clearDB() {
            SQLiteDatabase db = getDb();
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreateTable(db);
        }
    }
}