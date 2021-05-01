package com.example.linkcontainer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static DatabaseHandler instance;

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "linkcontainer";

    // Table Names
    private static final String TABLE_BOOKMARK = "bookmark";
    private static final String TABLE_CATEGORY = "category";

    // Common column names
    private static final String BOOKMARK_ID = "bookmark_id";
    private static final String CATEGORY_ID = "category_id";

    // NOTES Table - column nmaes
    private static final String KEY_LINK = "link";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_REMINDER = "reminder";
    private static final String KEY_PREVIOUS_CATEGORY = "prev_category";


    // TAGS Table - column names
    private static final String KEY_NAME = "name";


    // Tag table create statement
    private static final String CREATE_TABLE_CATEGORY = "CREATE TABLE " + TABLE_CATEGORY
            + "(" + CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + KEY_NAME + " TEXT" + ")";

    // Table Create Statements
    // Todo table create statement
    private static final String CREATE_TABLE_BOOKMARK = "CREATE TABLE "
            + TABLE_BOOKMARK + "(" + BOOKMARK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_LINK
            + " TEXT," + KEY_TITLE + " TEXT," + KEY_DESCRIPTION + " TEXT," + KEY_IMAGE + " TEXT,"
            + KEY_REMINDER + " LONG,"  + KEY_PREVIOUS_CATEGORY + " TEXT,"
            + KEY_CATEGORY + " INTEGER ," + " FOREIGN KEY ("+KEY_CATEGORY+")" +
            " REFERENCES "+TABLE_CATEGORY+"("+CATEGORY_ID+"));";

    public static synchronized DatabaseHandler getInstance(Context context) {
        if (instance == null)
            instance = new DatabaseHandler(context.getApplicationContext());

        return instance;
    }

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
                // creating required tables
        db.execSQL(CREATE_TABLE_CATEGORY);
        db.execSQL(CREATE_TABLE_BOOKMARK);

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, "Default");
        db.insert(TABLE_CATEGORY, null, values);
        values.put(KEY_NAME, "Archiviati");
        db.insert(TABLE_CATEGORY, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKMARK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);

        // create new tables
        onCreate(db);
    }

    public boolean addBookmark(Bookmark bookmark) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("Select  * from " + TABLE_BOOKMARK + " where "
                + KEY_LINK + " = ?", new String[]{bookmark.getLink()});

        if (cursor.moveToFirst()) {
            return false;
        } else {
            ContentValues values = new ContentValues();
            values.put(KEY_LINK, bookmark.getLink());
            values.put(KEY_TITLE, bookmark.getTitle());
            values.put(KEY_DESCRIPTION, bookmark.getDescription());
            values.put(KEY_IMAGE, bookmark.getImage());
            values.put(KEY_CATEGORY, bookmark.getCategory());
            values.put(KEY_REMINDER, bookmark.getReminder());
            db.insert(TABLE_BOOKMARK, null, values);

            return true;
        }
    }

    public boolean addCategory(String category) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("Select  * from " + TABLE_CATEGORY + " where "
                + KEY_NAME + " = ?", new String[]{category});

        if (cursor.moveToFirst()) {
            return false;
        } else {
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, category);
            db.insert(TABLE_CATEGORY, null, values);

            return true;
        }

    }

    public boolean addToArchive(String bookmarkId, String category) {
        int result;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("Select " + CATEGORY_ID + " from " + TABLE_CATEGORY + " where "
                + KEY_NAME + " = ?", new String[]{"Archiviati"});

        if (c.moveToFirst()) {
            String id = c.getString(c.getColumnIndex(CATEGORY_ID));
            ContentValues values = new ContentValues();
            values.put(KEY_CATEGORY, id);
            values.put(KEY_PREVIOUS_CATEGORY, category);
            result = db.update(TABLE_BOOKMARK, values, BOOKMARK_ID + " = ?",
                    new String[] { bookmarkId });

        } else {
            return false;
        }
        return result == 1;
    }

    public boolean removeFromArchive(String bookmarkId) {
        int result;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("Select " + KEY_PREVIOUS_CATEGORY + " from " + TABLE_BOOKMARK + " where "
                + BOOKMARK_ID + " = ?", new String[]{bookmarkId});

        if (c.moveToFirst()) {
            String id = c.getString(c.getColumnIndex(KEY_PREVIOUS_CATEGORY));
            ContentValues values = new ContentValues();
            values.put(KEY_CATEGORY, id);
            values.put(KEY_PREVIOUS_CATEGORY, (byte[]) null);
            result = db.update(TABLE_BOOKMARK, values, BOOKMARK_ID + " = ?",
                    new String[] { bookmarkId });

        } else {
            return false;
        }
        return result == 1;
    }

    public ArrayList<String> getCategories() {
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<String> categories = new ArrayList<>();

        Cursor cursor = db.rawQuery("Select  * from " + TABLE_CATEGORY + " where "
                + KEY_NAME + " != ?", new String[]{"Archiviati"});

        if (cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
            } while (cursor.moveToNext());
        }

        return categories;
    }

    public ArrayList<String> getAllCategories() {
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<String> categories = new ArrayList<>();

        Cursor cursor = db.rawQuery("Select  * from " + TABLE_CATEGORY, null);

        if (cursor.moveToFirst()) {
            do {

                categories.add(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
            } while (cursor.moveToNext());
        }

        return categories;
    }

    public ArrayList<Bookmark> getAllBookmarks() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Bookmark> bookmarks = new ArrayList<>();

        Cursor c = db.rawQuery("Select " + CATEGORY_ID + " from " + TABLE_CATEGORY +
                        " where " + KEY_NAME + " = ?",
                new String[]{"Archiviati"});

        if (c.moveToFirst()) {
            String category = c.getString(c.getColumnIndex("category_id"));
            Cursor cursor = db.rawQuery("Select  * from " + TABLE_BOOKMARK + " where " + KEY_CATEGORY + " != ? ",
                    new String[]{category});

            if (cursor.moveToFirst()) {
                do {
                    Bookmark bookmark = new Bookmark();
                    bookmark.setId(cursor.getString(cursor.getColumnIndex(BOOKMARK_ID)));
                    bookmark.setLink(cursor.getString(cursor.getColumnIndex(KEY_LINK)));
                    bookmark.setTitle(cursor.getString(cursor.getColumnIndex(KEY_TITLE)));
                    bookmark.setDescription(cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)));
                    bookmark.setImage(cursor.getString(cursor.getColumnIndex(KEY_IMAGE)));
                    bookmark.setCategory(cursor.getString(cursor.getColumnIndex(KEY_CATEGORY)));
                    bookmark.setReminder(cursor.getLong(cursor.getColumnIndex(KEY_REMINDER)));
                    bookmarks.add(bookmark);
                } while (cursor.moveToNext());
            }
        }

        return bookmarks;
    }

    public String getCategoryId(String categoryName) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select " + CATEGORY_ID + " from " + TABLE_CATEGORY +
                        " where " + KEY_NAME + " = ?",
                new String[]{categoryName});

        if (cursor.moveToFirst())
            return cursor.getString(cursor.getColumnIndex(CATEGORY_ID));
        else
            return null;
    }

    public String getCategoryById(String categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select " + KEY_NAME + " from " + TABLE_CATEGORY +
                        " where " + CATEGORY_ID + " = ?",
                new String[]{categoryId});

        if (cursor.moveToFirst())
            return cursor.getString(cursor.getColumnIndex(KEY_NAME));
        else
            return null;
    }

    public ArrayList<Bookmark> getBookmarksByCategory(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Bookmark> bookmarks = new ArrayList<>();

        Cursor c = db.rawQuery("Select " + CATEGORY_ID + " from " + TABLE_CATEGORY +
                " where " + KEY_NAME + " = ?", new String[]{category});

        if (c.moveToFirst()){
            String id = c.getString(c.getColumnIndex(CATEGORY_ID));
            Cursor cursor = db.rawQuery("Select  * from " + TABLE_BOOKMARK + " where "
                    + KEY_CATEGORY + " = ?", new String[]{id});

            if (cursor.moveToFirst()) {
                do {
                    Bookmark bookmark = new Bookmark();
                    bookmark.setId(cursor.getString(cursor.getColumnIndex(BOOKMARK_ID)));
                    bookmark.setLink(cursor.getString(cursor.getColumnIndex(KEY_LINK)));
                    bookmark.setTitle(cursor.getString(cursor.getColumnIndex(KEY_TITLE)));
                    bookmark.setDescription(cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)));
                    bookmark.setImage(cursor.getString(cursor.getColumnIndex(KEY_IMAGE)));
                    bookmark.setCategory(cursor.getString(cursor.getColumnIndex(KEY_CATEGORY)));
                    bookmark.setReminder(cursor.getLong(cursor.getColumnIndex(KEY_REMINDER)));
                    bookmarks.add(bookmark);

                } while (cursor.moveToNext());
            }
        }
        return bookmarks;
    }

    public boolean deleteBookmark(String id) {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(TABLE_BOOKMARK, BOOKMARK_ID + " = ?",
                new String[]{id}) > 0;
    }

    public boolean deleteCategory(String category) {
        boolean result = false;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("Select " + CATEGORY_ID + " from " + TABLE_CATEGORY +
                " where " + KEY_NAME + " = ?", new String[]{category});

        if (cursor.moveToFirst()){
            String id = cursor.getString(cursor.getColumnIndex(CATEGORY_ID));
            result = db.delete(TABLE_CATEGORY, KEY_NAME + " = ?",
                    new String[]{category}) > 0;

            if (result) {
                db.delete(TABLE_BOOKMARK, KEY_CATEGORY + " = ?",
                        new String[]{id});
            }
        }
        return result;
    }

    public boolean updateCategory(String category, String id) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.rawQuery("Select " + KEY_NAME + " from " + TABLE_CATEGORY +
                " where " + KEY_NAME + " = ?", new String[]{category});

        if (c.moveToFirst()) {
            return false;
        } else {
            Cursor cursor = db.rawQuery(" Update " + TABLE_CATEGORY + " set " +
                            KEY_NAME + " = ? " + " where " + CATEGORY_ID + " = ? ",
                    new String[] {category, id});

            cursor.close();
            return true;
        }
    }

    public boolean updateBookmark(Bookmark bookmark) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LINK, bookmark.getLink());
        values.put(KEY_TITLE, bookmark.getTitle());
        values.put(KEY_DESCRIPTION, bookmark.getDescription());
        values.put(KEY_IMAGE, bookmark.getImage());
        values.put(KEY_CATEGORY, bookmark.getCategory());
        values.put(KEY_REMINDER, bookmark.getReminder());

        int result = db.update(TABLE_BOOKMARK, values, BOOKMARK_ID + " = ?",
                new String[] { String.valueOf(bookmark.getId()) });

        return result == 1;
    }

    public String getDbPath(Context context) {
        return context.getDatabasePath(DATABASE_NAME).toString();
    }
}
