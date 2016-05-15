package com.examplesnake.snake;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database {
    /*Here we have all strings, which is needed by database*/
    public final static String TEXT_TYPE = "text";
    public final static String INT_TYPE = "integer";
    public final static String TABLE1_NAME = "player";
    public final static String TABLE2_NAME = "position";
    public final static String ID_FIELD = "_id " + INT_TYPE + " PRIMARY KEY AUTOINCREMENT";
    public final static String PLAYER_NAME_FIELD = "name";
    public final static String WAY_FIELD = "way";
    public final static String I_FIELD = "i_index";
    public final static String J_FIELD = "j_index";
    public final static String SCORES_FIELD = "scores";
    public final static String SPEED_FIELD = "speed";
    public final static String UPDATED_TIME_FIELD = "time";
    public final static String START_TIME_FIELD = "start_time";
    public final static String SWAPBUF_TIME_FIELD = "swapbuf_time";
    public final static String DATABASE_NAME = "snake";

    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private Context context;

    /**
     * Public constructor for database
     */
    public Database(Context context) {
        this.context = context;
    }

    /**
     * Open database conection
     */
    public void open() {
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    /**
     * Close database conection
     */
    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    /**
     * Puts snake positions into the database
     *
     * @param snake
     */
    public void updatePositions(Snake snake) {
        ContentValues cv;
        clearPosition();
        for (int[] i : snake.getSnake()) {
            cv = new ContentValues();
            cv.put(WAY_FIELD, i[0]);
            cv.put(I_FIELD, i[1]);
            cv.put(J_FIELD, i[2]);
            db.insert(TABLE2_NAME, null, cv);
        }
    }

    /**
     * Updating player parameter int the database
     *
     * @param name         - name
     * @param score        - score
     * @param speed        - speed
     * @param time         - updatedTime from stopwatch
     * @param startTime    - startTime from stopwatch
     * @param swapBufTime- swapBufTime from stopwatch
     */
    public void updatePlayer(String name, int score,
                             int speed, long time,
                             long startTime, long swapBufTime) {
        ContentValues cv = new ContentValues();
        cv.put(PLAYER_NAME_FIELD, name);
        cv.put(SPEED_FIELD, speed);
        cv.put(SCORES_FIELD, score);
        cv.put(UPDATED_TIME_FIELD, time);
        cv.put(START_TIME_FIELD, startTime);
        cv.put(SWAPBUF_TIME_FIELD, swapBufTime);
        if (db.update(TABLE1_NAME, cv, PLAYER_NAME_FIELD + "=\'" + name + "\'", null) == 0) {
            db.insert(TABLE1_NAME, null, cv);
        }
    }

    /**
     * Clear all positions in the second table
     */
    public void clearPosition() {
        db.delete(TABLE2_NAME, null, null);
    }

    /**
     * Get Cursor with player positions. Last player in the table
     */
    public Cursor readPlayer() {
        Cursor cursor = db.query(TABLE1_NAME,
                new String[]{
                        PLAYER_NAME_FIELD, SPEED_FIELD,
                        SCORES_FIELD, UPDATED_TIME_FIELD,
                        START_TIME_FIELD, SWAPBUF_TIME_FIELD},
                null, null, null, null, "_id DESC", "1");
        if (cursor != null) {
            return cursor;
        } else {
            return null;
        }
    }

    /**
     * Get Cursor with player positions. Player with defined name
     */
    public Cursor readPlayer(String name) {
        Cursor cursor = db.query(TABLE1_NAME,
                new String[]{
                        SPEED_FIELD, SCORES_FIELD, UPDATED_TIME_FIELD,
                        START_TIME_FIELD, SWAPBUF_TIME_FIELD},
                PLAYER_NAME_FIELD + "=?", new String[]{name}, null, null, null);
        if (cursor != null) {
            return cursor;
        } else {
            return null;
        }
    }

    /**
     * Reading snake positions
     *
     * @return cursor with positions
     */
    public Cursor readSnake() {
        Cursor cursor = db.query(TABLE2_NAME,
                new String[]{WAY_FIELD, I_FIELD, J_FIELD}, null, null, null, null, null);
        if (cursor != null)
            return cursor;
        else
            return null;
    }

    /**
     * Checking is database have positions in second table
     *
     * @return boolean value
     */
    public boolean isContinueGame() {
        Cursor cursor = db.query(TABLE2_NAME,
                new String[]{WAY_FIELD, I_FIELD, J_FIELD}, null, null, null, null, null);
        if (cursor == null) return false;

        else if (cursor.moveToNext()) {
            cursor.close();
            return true;
        } else return false;
    }

    /**
     * Reading scores for leaderboard.
     *
     * @return cursor with scores
     */
    public Cursor readLeaderBoard() {
        Cursor cursor = db.query(TABLE1_NAME,
                new String[]{"_id", PLAYER_NAME_FIELD, SCORES_FIELD}, null, null, null, null, SCORES_FIELD + " DESC", "10");
        if (cursor != null)
            return cursor;
        return null;
    }

    /**
     * Initialization of database
     */
    class DBHelper extends SQLiteOpenHelper {
        // _id | name | scores | updatedTime | startTime | swapBufTime | speed
        public final String CREATE_TABLE_1 = "CREATE TABLE "
                + TABLE1_NAME + "(" + ID_FIELD + "," + PLAYER_NAME_FIELD + " "
                + TEXT_TYPE + "," + SCORES_FIELD + " " + INT_TYPE +
                " DEFAULT 0," + UPDATED_TIME_FIELD + " " + INT_TYPE + " DEFAULT 0,"
                + START_TIME_FIELD + " " + INT_TYPE + " DEFAULT 0,"
                + SWAPBUF_TIME_FIELD + " " + INT_TYPE + " DEFAULT 0,"
                + SPEED_FIELD + " " + INT_TYPE + " DEFAULT 0);";
        // _id | way | i | j |
        public final String CREATE_TABLE_2 = "CREATE TABLE "
                + TABLE2_NAME + "(" + ID_FIELD + ","
                + WAY_FIELD + " " + INT_TYPE +
                " DEFAULT 0," + I_FIELD + " " + INT_TYPE +
                " DEFAULT 0," + J_FIELD + " " + INT_TYPE +
                " DEFAULT 0);";

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_1);
            db.execSQL(CREATE_TABLE_2);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
