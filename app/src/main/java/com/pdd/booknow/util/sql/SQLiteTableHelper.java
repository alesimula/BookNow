package com.pdd.booknow.util.sql;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pdd.booknow.database.user.UserDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * An extension of {@link SQLiteOpenHelper} with utility methods for a single table DB.
 * Any exception during write operations are ignored, and any version change causes a DB reset.
 */
public abstract class SQLiteTableHelper {
    private static final String TAG = "SQLiteTableHelper";

    private final String mTableName;
    private final MySQLiteOpenHelper mOpenHelper;

    private boolean mIgnoreWrites;

    public SQLiteTableHelper(Context context, String name, int version, String tableName) {
        mTableName = tableName;
        mOpenHelper = new MySQLiteOpenHelper(context, name, version);

        mIgnoreWrites = false;
    }

    /**
     * @see SQLiteDatabase#delete(String, String, String[])
     */
    public void delete(String whereClause, String[] whereArgs) {
        if (mIgnoreWrites) {
            return;
        }
        try {
            mOpenHelper.getWritableDatabase().delete(mTableName, whereClause, whereArgs);
        } catch (SQLiteFullException e) {
            onDiskFull(e);
        } catch (SQLiteException e) {
            Log.d(TAG, "Ignoring sqlite exception", e);
        }
    }

    /**
     * @see SQLiteDatabase#insertWithOnConflict(String, String, ContentValues, int)
     */
    public long insertOrRollback(ContentValues values) {
        if (mIgnoreWrites) {
            return -1;
        }
        try {
            return mOpenHelper.getWritableDatabase().insertWithOnConflict(
                    mTableName, null, values, SQLiteDatabase.CONFLICT_ROLLBACK);
        } catch (SQLiteFullException e) {
            onDiskFull(e);
        } catch (SQLiteException e) {
            Log.d(TAG, "Ignoring sqlite exception", e);
        }
        return -1;
    }

    private void onDiskFull(SQLiteFullException e) {
        Log.e(TAG, "Disk full, all write operations will be ignored", e);
        mIgnoreWrites = true;
    }

    /**
     * @see SQLiteDatabase#query(String, String[], String, String[], String, String, String)
     */
    public Cursor query(String[] columns, String selection, String[] selectionArgs) {
        return mOpenHelper.getReadableDatabase().query(
                mTableName, columns, selection, selectionArgs, null, null, null);
    }

    /**
     * @see SQLiteDatabase#update(String, ContentValues, String, String[])
     */
    public int update(ContentValues values, String selection, String[] selectionArgs) {
        return mOpenHelper.getReadableDatabase().update(
                mTableName, values, selection, selectionArgs);
    }

    public void clear() {
        mOpenHelper.clearDB(mOpenHelper.getWritableDatabase());
    }

    protected abstract void onCreateTable(SQLiteDatabase db);

    public SQLiteDatabase getDb() {
        return mOpenHelper.getWritableDatabase();
    }

    public static void putValue(@NonNull ContentValues values, @NonNull String key, Object value) {
        //Default values
        if (value==null) values.put(key,(byte[])null);
        else if (value instanceof byte[]) values.put(key,(byte[])value);
        else if (value instanceof String) values.put(key,(String)value);
        else if (Integer.class.isInstance(value)) values.put(key,(Integer) value);
        else if (Boolean.class.isInstance(value)) values.put(key,(Boolean) value);
        else if (Short.class.isInstance(value)) values.put(key,(Short) value);
        else if (Float.class.isInstance(value)) values.put(key,(Float) value);
        else if (Long.class.isInstance(value)) values.put(key,(Long) value);
        else if (Byte.class.isInstance(value)) values.put(key,(Byte) value);
        else if (Double.class.isInstance(value)) values.put(key,(Double) value);
        //Custom values
        else if (value instanceof BigInteger) values.put(key,((BigInteger)value).toByteArray());
        else if (value instanceof Bitmap) values.put(key,encodeBitmap((Bitmap)value));
        else if (value instanceof Date) values.put(key,encodeDate((Date) value));
        else throw new RuntimeException(TAG+" - putValue; Invalid value type: "+value.getClass().getSimpleName());
    }

    public static String encodeDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return format.format(date);
    }
    public static Date decodeDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            return format.parse(date);
        }
        catch (ParseException e) {
            return null;
        }
    }
    public static byte[] encodeBitmap(Bitmap bitmap) {
        // Try go guesstimate how much space the icon will take when serialized
        // to avoid unnecessary allocations/copies during the write.
        if (bitmap==null) return null;
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            Log.w(UserDatabase.class.getSimpleName(), "Could not write bitmap");
            return null;
        }
    }
    public static BigInteger encodePassword(@Nullable BigInteger savedPassword, String password) {
        if (password==null || password.isEmpty()) return null;
        try {
            byte[] hash;
            if (savedPassword!=null) hash = savedPassword.toByteArray();
            else {
                hash = new byte[64];
                new SecureRandom().nextBytes(hash);
            }
            System.arraycopy(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(new PBEKeySpec(password.toCharArray(), Arrays.copyOfRange(hash, 32, 64), 1000, 32*8)).getEncoded(), 0, hash, 0, 32);

            return new BigInteger(hash);
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException("Failed to hash password");
        }
    }
    public static BigInteger encodePassword(String password) {
        return encodePassword(null, password);
    }
    public static String parseSalt(BigInteger password) {
        if (password!=null) {
            BigInteger salt = new BigInteger(Arrays.copyOfRange(password.toByteArray(), 32, 64));
            if (salt.compareTo(BigInteger.ZERO)<0) salt=salt.add(BigInteger.ONE.shiftLeft(8*32));
            return salt.toString(16);
        }
        else return null;
    }

    /**
     * A private inner class to prevent direct DB access.
     */
    private class MySQLiteOpenHelper extends SQLiteOpenHelper {

        public MySQLiteOpenHelper(Context context, String name, int version) {
            super(new NoLocaleSqliteContext(context), name, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            onCreateTable(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion != newVersion) {
                clearDB(db);
            }
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion != newVersion) {
                clearDB(db);
            }
        }

        private void clearDB(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + mTableName);
            onCreate(db);
        }
    }

    private class NoLocaleSqliteContext extends ContextWrapper {

        public NoLocaleSqliteContext(Context context) {
            super(context);
        }

        @Override
        public SQLiteDatabase openOrCreateDatabase(
                String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
            return super.openOrCreateDatabase(
                    name, mode | Context.MODE_NO_LOCALIZED_COLLATORS, factory, errorHandler);
        }
    }
}