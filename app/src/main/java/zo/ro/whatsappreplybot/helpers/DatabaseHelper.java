package zo.ro.whatsappreplybot.helpers;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import zo.ro.whatsappreplybot.models.Message;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "MADARA";
    private static final String DATABASE_NAME = "whatsappMessages.db";
    private static final int DATABASE_VERSION = 1; // Increment version for schema change
    public static final String TABLE_MESSAGES = "messages";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_REPLY = "reply";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_MESSAGES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_SENDER + " TEXT, " +
                    COLUMN_MESSAGE + " TEXT, " +
                    COLUMN_TIMESTAMP + " TEXT, " +
                    COLUMN_REPLY + " TEXT" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        if (oldVersion < 2) {
//            db.execSQL("ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COLUMN_REPLY + " TEXT;");
//        }
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

//    ----------------------------------------------------------------------------------------------

    public void insertMessage(String sender, String message, String timestamp, String reply) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SENDER, sender);
        values.put(COLUMN_MESSAGE, message);
        values.put(COLUMN_TIMESTAMP, timestamp);
        values.put(COLUMN_REPLY, reply);
        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    public void deleteOldMessages() {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            @SuppressLint("SimpleDateFormat")
            String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String whereClause = COLUMN_TIMESTAMP + " < ?";
            String[] whereArgs = {currentDate + " 00:00:00"};
            db.delete(TABLE_MESSAGES, whereClause, whereArgs);
        } catch (Exception e) {
            // Log the exception
            Log.e("DatabaseHelper", "Error deleting old messages", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }


    public List<Message> getChatHistoryBySender(String sender) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            // Ensure the database is opened
            db = this.getReadableDatabase();

            String query = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + COLUMN_SENDER + " = ? " +
                    "ORDER BY " + COLUMN_TIMESTAMP + " DESC LIMIT 7";
            cursor = db.rawQuery(query, new String[]{sender});

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String message = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE));
                    String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));
                    String reply = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPLY));

                    Message msg = new Message(id, sender, message, timestamp, reply);
                    messages.add(msg);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "getLast5MessagesBySender: ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return messages;
    }

    public List<Message> getAllMessagesBySender(String sender) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            // Ensure the database is opened
            db = this.getReadableDatabase();

            String query = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + COLUMN_SENDER + " = ? " +
                    "ORDER BY " + COLUMN_TIMESTAMP + " DESC";
            cursor = db.rawQuery(query, new String[]{sender});

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String message = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE));
                    String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));
                    String reply = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPLY));

                    Message msg = new Message(id, sender, message, timestamp, reply);
                    messages.add(msg);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "getLast5MessagesBySender: ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return messages;
    }
}
