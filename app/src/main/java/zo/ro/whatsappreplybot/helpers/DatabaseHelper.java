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
import java.util.Calendar;

import zo.ro.whatsappreplybot.models.Message;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "MADARA";
    private static final String DATABASE_NAME = "whatsappMessages.db";
    private static final int DATABASE_VERSION = 1;
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    // Step 1: Modify deleteOldMessages to delete messages older than 7 days
    public void deleteOldMessages() {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            @SuppressLint("SimpleDateFormat")
            String sevenDaysAgo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(getDateBeforeDays(7));
            String whereClause = COLUMN_TIMESTAMP + " < ?";
            String[] whereArgs = {sevenDaysAgo};
            db.delete(TABLE_MESSAGES, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting old messages", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // Utility method to get the date X days ago
    private Date getDateBeforeDays(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -days);
        return calendar.getTime();
    }

    // Step 2: Modify getChatHistoryBySender to retrieve messages from the last 7 days
    public List<Message> getChatHistoryBySender(String sender) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            String sevenDaysAgo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(getDateBeforeDays(7));
            String query = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + COLUMN_SENDER + " = ? " +
                    "AND " + COLUMN_TIMESTAMP + " >= ? ORDER BY " + COLUMN_TIMESTAMP + " DESC";
            cursor = db.rawQuery(query, new String[]{sender, sevenDaysAgo});

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
            Log.e(TAG, "getChatHistoryBySender: ", e);
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
