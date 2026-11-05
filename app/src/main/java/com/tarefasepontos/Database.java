package br.com.familia.tarefasepontos;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Database extends SQLiteOpenHelper {
    private static final String DB_NAME = "tarefas_pontos.db";
    private static final int VERSION = 1;

    public Database(Context context) { super(context, DB_NAME, null, VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE members (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, photo TEXT)");
        db.execSQL("CREATE TABLE tasks (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, points INTEGER NOT NULL, member_id INTEGER NOT NULL, active INTEGER NOT NULL DEFAULT 1)");
        db.execSQL("CREATE TABLE completions (id INTEGER PRIMARY KEY AUTOINCREMENT, task_id INTEGER, member_id INTEGER NOT NULL, member_name TEXT NOT NULL, task_title TEXT NOT NULL, points INTEGER NOT NULL, day TEXT NOT NULL, photo TEXT, UNIQUE(task_id,day))");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Future migrations belong here; never delete family data on upgrade.
    }

    private String today() { return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()); }

    public long addMember(String name) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        return getWritableDatabase().insert("members", null, v);
    }

    public Cursor members() {
        return getReadableDatabase().rawQuery("SELECT id,name,photo FROM members ORDER BY name COLLATE NOCASE", null);
    }

    public void setMemberPhoto(long id, String photo) {
        ContentValues v = new ContentValues();
        v.put("photo", photo);
        getWritableDatabase().update("members", v, "id=?", new String[]{String.valueOf(id)});
    }

    public long addTask(String title, int points, long memberId) {
        ContentValues v = new ContentValues();
        v.put("title", title);
        v.put("points", points);
        v.put("member_id", memberId);
        return getWritableDatabase().insert("tasks", null, v);
    }

    public Cursor tasks() {
        return getReadableDatabase().rawQuery(
            "SELECT t.id,t.title,t.points,m.name,CASE WHEN c.id IS NULL THEN 0 ELSE 1 END,c.photo " +
            "FROM tasks t JOIN members m ON m.id=t.member_id " +
            "LEFT JOIN completions c ON c.task_id=t.id AND c.day=? " +
            "WHERE t.active=1 ORDER BY t.id DESC", new String[]{today()});
    }

    public void complete(long taskId, String photo) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try (Cursor c = db.rawQuery(
            "SELECT t.title,t.points,m.id,m.name FROM tasks t JOIN members m ON m.id=t.member_id WHERE t.id=? AND t.active=1",
            new String[]{String.valueOf(taskId)})) {
            if (c.moveToFirst()) {
                ContentValues v = new ContentValues();
                v.put("task_id", taskId);
                v.put("task_title", c.getString(0));
                v.put("points", c.getInt(1));
                v.put("member_id", c.getLong(2));
                v.put("member_name", c.getString(3));
                v.put("day", today());
                v.put("photo", photo);
                db.insertWithOnConflict("completions", null, v, SQLiteDatabase.CONFLICT_IGNORE);
            }
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }

    public void undoToday(long taskId) {
        getWritableDatabase().delete("completions", "task_id=? AND day=?",
            new String[]{String.valueOf(taskId), today()});
    }

    public void deleteTask(long taskId) {
        ContentValues v = new ContentValues();
        v.put("active", 0);
        getWritableDatabase().update("tasks", v, "id=?", new String[]{String.valueOf(taskId)});
    }

    public int todayCount() { return scalar("SELECT COUNT(*) FROM completions WHERE day=?", new String[]{today()}); }
    public int todayPoints() { return scalar("SELECT COALESCE(SUM(points),0) FROM completions WHERE day=?", new String[]{today()}); }

    private int scalar(String sql, String[] args) {
        try (Cursor c = getReadableDatabase().rawQuery(sql,args)) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    public Cursor ranking() {
        return getReadableDatabase().rawQuery(
            "SELECT m.name,COALESCE(SUM(c.points),0),m.photo FROM members m " +
            "LEFT JOIN completions c ON c.member_id=m.id " +
            "GROUP BY m.id ORDER BY 2 DESC,m.name COLLATE NOCASE", null);
    }

    public Cursor weeklyRanking(String monday, String sunday) {
        return getReadableDatabase().rawQuery(
            "SELECT m.name,COALESCE(SUM(c.points),0) FROM members m " +
            "LEFT JOIN completions c ON c.member_id=m.id AND c.day BETWEEN ? AND ? " +
            "GROUP BY m.id ORDER BY 2 DESC,m.name COLLATE NOCASE",
            new String[]{monday,sunday});
    }

    public Cursor history() {
        return getReadableDatabase().rawQuery(
            "SELECT day,task_title,points,member_name,photo FROM completions ORDER BY id DESC LIMIT 100", null);
    }
}
