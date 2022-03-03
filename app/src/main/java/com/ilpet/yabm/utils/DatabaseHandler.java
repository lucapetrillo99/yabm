package com.ilpet.yabm.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.ilpet.yabm.R;
import com.ilpet.yabm.classes.Bookmark;
import com.ilpet.yabm.classes.Category;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "yabm";
    private static final String TABLE_BOOKMARK = "bookmark";
    private static final String TABLE_CATEGORY = "category";
    private static final String BOOKMARK_ID = "bookmark_id";
    private static final String CATEGORY_ID = "category_id";
    private static final String KEY_LINK = "link";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_REMINDER = "reminder";
    private static final String KEY_PREVIOUS_CATEGORY = "prev_category";
    private static final String KEY_TYPE = "type";
    private static final String KEY_NAME = "name";
    private static final String CREATE_TABLE_CATEGORY = "CREATE TABLE " + TABLE_CATEGORY
            + "(" + CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + KEY_NAME + " TEXT)";
    private static final String CREATE_TABLE_BOOKMARK = "CREATE TABLE "
            + TABLE_BOOKMARK + "(" + BOOKMARK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_LINK
            + " TEXT," + KEY_TITLE + " TEXT," + KEY_DESCRIPTION + " TEXT," + KEY_IMAGE + " TEXT,"
            + KEY_REMINDER + " LONG," + KEY_PREVIOUS_CATEGORY + " TEXT," + KEY_TYPE + " TEXT,"
            + KEY_CATEGORY + " INTEGER ," + " FOREIGN KEY (" + KEY_CATEGORY + ")" +
            " REFERENCES " + TABLE_CATEGORY + "(" + CATEGORY_ID + "));";
    private static DatabaseHandler instance;
    private final Context context;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    public static synchronized DatabaseHandler getInstance(Context context) {
        if (instance == null)
            instance = new DatabaseHandler(context.getApplicationContext());
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CATEGORY);
        db.execSQL(CREATE_TABLE_BOOKMARK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKMARK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);

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
            values.put(KEY_TYPE, String.valueOf(bookmark.getType()));
            db.insert(TABLE_BOOKMARK, null, values);
            cursor.close();
            return true;
        }
    }

    public String addCategory(Category category) {
        String categoryId = null;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("Select  * from " + TABLE_CATEGORY + " where "
                + KEY_NAME + " = ?", new String[]{category.getCategoryTitle()});

        if (!cursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, category.getCategoryTitle());
            categoryId = String.valueOf(db.insert(TABLE_CATEGORY, null, values));
            cursor.close();
        }

        return categoryId;
    }

    public boolean addToArchive(String bookmarkId, String category) {
        int result;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select " + CATEGORY_ID + " from " + TABLE_CATEGORY + " where "
                + KEY_NAME + " = ?", new String[]{context.getString(R.string.archived_bookmarks)});

        if (cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_ID));
            ContentValues values = new ContentValues();
            values.put(KEY_CATEGORY, id);
            values.put(KEY_PREVIOUS_CATEGORY, category);
            result = db.update(TABLE_BOOKMARK, values, BOOKMARK_ID + " = ?",
                    new String[]{bookmarkId});

        } else {
            return false;
        }
        cursor.close();
        return result == 1;
    }

    public boolean removeFromArchive(String bookmarkId) {
        int result;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select " + KEY_PREVIOUS_CATEGORY + " from " + TABLE_BOOKMARK + " where "
                + BOOKMARK_ID + " = ?", new String[]{bookmarkId});

        if (cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PREVIOUS_CATEGORY));
            ContentValues values = new ContentValues();
            values.put(KEY_CATEGORY, id);
            values.put(KEY_PREVIOUS_CATEGORY, (byte[]) null);
            result = db.update(TABLE_BOOKMARK, values, BOOKMARK_ID + " = ?",
                    new String[]{bookmarkId});

        } else {
            return false;
        }
        cursor.close();
        return result == 1;
    }

    public ArrayList<Category> getCategories() {
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<Category> categories = new ArrayList<>();

        Cursor cursor = db.rawQuery("Select  * from " + TABLE_CATEGORY + " where "
                + KEY_NAME + " != ?", new String[]{context.getString(R.string.archived_bookmarks)});

        if (cursor.moveToFirst()) {
            do {
                Category category = new Category();
                category.setCategoryId(cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_ID)));
                category.setCategoryTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
                categories.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    public ArrayList<Category> getAllCategories() {
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<Category> categories = new ArrayList<>();

        Cursor cursor = db.rawQuery("Select  * from " + TABLE_CATEGORY, null);

        if (cursor.moveToFirst()) {
            do {
                Category category = new Category();
                category.setCategoryId(cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_ID)));
                category.setCategoryTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
                categories.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    public ArrayList<Bookmark> getAllBookmarks() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Bookmark> bookmarks = new ArrayList<>();

        Cursor c = db.rawQuery("Select " + CATEGORY_ID + " from " + TABLE_CATEGORY +
                        " where " + KEY_NAME + " = ?",
                new String[]{context.getString(R.string.archived_bookmarks)});

        if (c.moveToFirst()) {
            String category = c.getString(c.getColumnIndexOrThrow(CATEGORY_ID));
            Cursor cursor = db.rawQuery("Select  * from " + TABLE_BOOKMARK + " where " + KEY_CATEGORY + " != ? ",
                    new String[]{category});

            if (cursor.moveToFirst()) {
                do {
                    Bookmark bookmark = new Bookmark();
                    bookmark.setId(cursor.getString(cursor.getColumnIndexOrThrow(BOOKMARK_ID)));
                    bookmark.setLink(cursor.getString(cursor.getColumnIndexOrThrow(KEY_LINK)));
                    bookmark.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE)));
                    bookmark.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)));
                    bookmark.setImage(cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE)));
                    bookmark.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY)));
                    bookmark.setReminder(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_REMINDER)));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TYPE));
                    bookmark.setType(Bookmark.ItemType.valueOf(type));
                    bookmarks.add(bookmark);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        c.close();
        return bookmarks;
    }

    public String getCategoryId(String categoryName) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select " + CATEGORY_ID + " from " + TABLE_CATEGORY +
                        " where " + KEY_NAME + " = ?",
                new String[]{categoryName});

        if (cursor.moveToFirst()) {
            String categoryId = cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_ID));
            cursor.close();
            return categoryId;
        } else {
            return null;
        }
    }

    public String getCategoryById(String categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select " + KEY_NAME + " from " + TABLE_CATEGORY +
                        " where " + CATEGORY_ID + " = ?",
                new String[]{categoryId});

        if (cursor.moveToFirst()) {
            String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME));
            cursor.close();
            return categoryName;
        } else {
            return null;
        }
    }

    public ArrayList<Bookmark> getBookmarksByCategory(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Bookmark> bookmarks = new ArrayList<>();

        Cursor c = db.rawQuery("Select " + CATEGORY_ID + " from " + TABLE_CATEGORY +
                " where " + KEY_NAME + " = ?", new String[]{category});

        if (c.moveToFirst()) {
            String id = c.getString(c.getColumnIndexOrThrow(CATEGORY_ID));
            Cursor cursor = db.rawQuery("Select  * from " + TABLE_BOOKMARK + " where "
                    + KEY_CATEGORY + " = ?", new String[]{id});

            if (cursor.moveToFirst()) {
                do {
                    Bookmark bookmark = new Bookmark();
                    bookmark.setId(cursor.getString(cursor.getColumnIndexOrThrow(BOOKMARK_ID)));
                    bookmark.setLink(cursor.getString(cursor.getColumnIndexOrThrow(KEY_LINK)));
                    bookmark.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE)));
                    bookmark.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)));
                    bookmark.setImage(cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE)));
                    bookmark.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY)));
                    bookmark.setReminder(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_REMINDER)));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TYPE));
                    bookmark.setType(Bookmark.ItemType.valueOf(type));
                    bookmarks.add(bookmark);

                } while (cursor.moveToNext());
                cursor.close();
            }
        }
        c.close();
        return bookmarks;
    }

    public boolean deleteBookmark(String id) {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(TABLE_BOOKMARK, BOOKMARK_ID + " = ?",
                new String[]{id}) > 0;
    }

    public boolean deleteCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();

        ArrayList<Bookmark> bookmarks = getBookmarksByCategory(category.getCategoryTitle());

        for (Bookmark bookmark : bookmarks) {
            deleteBookmark(bookmark.getId());
        }
        return db.delete(TABLE_CATEGORY, CATEGORY_ID + " = ?",
                new String[]{category.getCategoryId()}) > 0;
    }

    public boolean updateCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CATEGORY_ID, category.getCategoryId());
        values.put(KEY_NAME, category.getCategoryTitle());
        int result = db.update(TABLE_CATEGORY, values, CATEGORY_ID + " = ?",
                new String[]{category.getCategoryId()});

        return result == 1;
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
        values.put(KEY_TYPE, String.valueOf(bookmark.getType()));

        int result = db.update(TABLE_BOOKMARK, values, BOOKMARK_ID + " = ?",
                new String[]{String.valueOf(bookmark.getId())});

        return result == 1;
    }

    public String getDbPath(Context context) {
        return context.getDatabasePath(DATABASE_NAME).toString();
    }

    public void deleteBookmarks(ArrayList<Bookmark> bookmarks) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] whereArgs = new String[bookmarks.size()];
        for (int i = 0; i < bookmarks.size(); i++) {
            whereArgs[i] = bookmarks.get(i).getId();
        }

        String args = TextUtils.join(", ", whereArgs);
        db.execSQL(String.format("DELETE FROM " + TABLE_BOOKMARK + " WHERE " + BOOKMARK_ID +
                " IN (%s);", args));
    }

    public void archiveBookmarks(ArrayList<Bookmark> bookmarks) {
        String id = bookmarks.get(0).getCategory();
        SQLiteDatabase db = this.getWritableDatabase();
        String[] whereArgs = new String[bookmarks.size()];
        for (int i = 0; i < bookmarks.size(); i++) {
            whereArgs[i] = bookmarks.get(i).getId();
        }

        String args = TextUtils.join(", ", whereArgs);

        db.execSQL(String.format("UPDATE %s SET %s =" + 2 + ", %s = " + id + " WHERE %s IN (%s);",
                TABLE_BOOKMARK, KEY_CATEGORY, KEY_PREVIOUS_CATEGORY, BOOKMARK_ID, args));
    }
}
