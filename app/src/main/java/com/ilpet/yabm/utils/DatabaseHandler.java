package com.ilpet.yabm.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.ilpet.yabm.R;
import com.ilpet.yabm.classes.Bookmark;
import com.ilpet.yabm.classes.Category;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "yabm";
    private static final String TABLE_BOOKMARK = "bookmark";
    private static final String TABLE_CATEGORY = "category";
    private static final String TABLE_PASSWORD = "password";
    private static final String BOOKMARK_ID = "bookmark_id";
    private static final String PASSWORD_ID = "password_id";
    private static final String CATEGORY_ID = "category_id";
    private static final String KEY_BOOKMARK_LINK = "link";
    private static final String KEY_BOOKMARK_TITLE = "title";
    private static final String KEY_BOOKMARK_DESCRIPTION = "description";
    private static final String KEY_BOOKMARK_IMAGE = "image";
    private static final String KEY_BOOKMARK_CATEGORY = "category";
    private static final String KEY_BOOKMARK_REMINDER = "reminder";
    private static final String KEY_BOOKMARK_PREVIOUS_CATEGORY = "prev_category";
    private static final String KEY_BOOKMARK_TYPE = "type";
    private static final String KEY_CATEGORY_TITLE = "title";
    private static final String KEY_CATEGORY_IMAGE = "image";
    private static final String KEY_DATE = "date";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_USER_PASSWORD = "user_password";
    private static DatabaseHandler instance;
    private final Context context;
    private static final String CREATE_TABLE_CATEGORY = "CREATE TABLE " + TABLE_CATEGORY
            + "(" + CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + KEY_CATEGORY_TITLE +
            " TEXT ," + KEY_DATE + " TEXT ," + KEY_CATEGORY_IMAGE + " BLOB ," + KEY_PASSWORD +
            " INTEGER)";
    private static final String CREATE_TABLE_BOOKMARK = "CREATE TABLE " + TABLE_BOOKMARK + "(" +
            BOOKMARK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_BOOKMARK_LINK + " TEXT," +
            KEY_BOOKMARK_TITLE + " TEXT," + KEY_BOOKMARK_DESCRIPTION + " TEXT," +
            KEY_BOOKMARK_IMAGE + " TEXT," + KEY_BOOKMARK_REMINDER + " LONG," +
            KEY_BOOKMARK_PREVIOUS_CATEGORY + " TEXT," + KEY_BOOKMARK_TYPE + " TEXT," + KEY_DATE +
            " TEXT," + KEY_BOOKMARK_CATEGORY + " INTEGER ," +
            " FOREIGN KEY (" + KEY_BOOKMARK_CATEGORY + ")" + " REFERENCES " + TABLE_CATEGORY +
            "(" + CATEGORY_ID + "));";
    private static final String CREATE_TABLE_PASSWORD = "CREATE TABLE " + TABLE_PASSWORD
            + "(" + PASSWORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + KEY_USER_PASSWORD + " TEXT)";

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
        db.execSQL(CREATE_TABLE_PASSWORD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKMARK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PASSWORD);
        onCreate(db);
    }

    public boolean addBookmark(Bookmark bookmark) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("Select  * from " + TABLE_BOOKMARK + " where "
                + KEY_BOOKMARK_LINK + " = ?", new String[]{bookmark.getLink()});

        if (cursor.moveToFirst()) {
            return false;
        } else {
            ContentValues values = new ContentValues();
            values.put(KEY_BOOKMARK_LINK, bookmark.getLink());
            values.put(KEY_BOOKMARK_TITLE, bookmark.getTitle());
            values.put(KEY_BOOKMARK_DESCRIPTION, bookmark.getDescription());
            values.put(KEY_BOOKMARK_IMAGE, bookmark.getImage());
            values.put(KEY_BOOKMARK_CATEGORY, bookmark.getCategory());
            values.put(KEY_BOOKMARK_REMINDER, bookmark.getReminder());
            values.put(KEY_BOOKMARK_TYPE, String.valueOf(bookmark.getType()));
            values.put(KEY_DATE, bookmark.getDate());
            db.insert(TABLE_BOOKMARK, null, values);
            cursor.close();
            return true;
        }
    }

    public String addCategory(Category category) {
        String categoryId = null;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("Select  * from " + TABLE_CATEGORY + " where "
                + KEY_CATEGORY_TITLE + " = ?", new String[]{category.getCategoryTitle()});

        if (!cursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put(KEY_CATEGORY_TITLE, category.getCategoryTitle());
            Drawable image = category.getCategoryImage();
            values.put(KEY_CATEGORY_IMAGE, drawableToByteArray(image));
            values.put(KEY_DATE, category.getDate());
            values.put(KEY_PASSWORD, category.getPasswordProtectionValue());
            categoryId = String.valueOf(db.insert(TABLE_CATEGORY, null, values));
            cursor.close();
        }
        return categoryId;
    }

    public void addToArchive(String bookmarkId, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select " + CATEGORY_ID + " from " + TABLE_CATEGORY + " where "
                + KEY_CATEGORY_TITLE + " = ?", new String[]{context.getString(R.string.archived_bookmarks)});

        if (cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_ID));
            ContentValues values = new ContentValues();
            values.put(KEY_BOOKMARK_CATEGORY, id);
            values.put(KEY_BOOKMARK_PREVIOUS_CATEGORY, category);
            db.update(TABLE_BOOKMARK, values, BOOKMARK_ID + " = ?",
                    new String[]{bookmarkId});
        } else {
            return;
        }
        cursor.close();
    }

    public void removeFromArchive(String bookmarkId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select " + KEY_BOOKMARK_PREVIOUS_CATEGORY + " from "
                + TABLE_BOOKMARK + " where " + BOOKMARK_ID + " = ?", new String[]{bookmarkId});

        if (cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKMARK_PREVIOUS_CATEGORY));
            ContentValues values = new ContentValues();
            values.put(KEY_BOOKMARK_CATEGORY, id);
            values.put(KEY_BOOKMARK_PREVIOUS_CATEGORY, (byte[]) null);
            db.update(TABLE_BOOKMARK, values, BOOKMARK_ID + " = ?",
                    new String[]{bookmarkId});
        } else {
            return;
        }
        cursor.close();
    }

    public ArrayList<Category> getCategories(String orderBy, String orderType) {
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<Category> categories = new ArrayList<>();
        Cursor cursor;
        if (orderBy != null && orderType != null) {
            cursor = db.rawQuery("Select  * from " + TABLE_CATEGORY + " where "
                            + KEY_CATEGORY_TITLE + " != ?" + " order by " + orderBy + " " + orderType,
                    new String[]{context.getString(R.string.archived_bookmarks)});
        } else {
            cursor = db.rawQuery("Select  * from " + TABLE_CATEGORY + " where "
                    + KEY_CATEGORY_TITLE + " != ?", new String[]{context.getString(R.string.archived_bookmarks)});
        }

        if (cursor.moveToFirst()) {
            do {
                Category category = new Category();
                category.setCategoryId(cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_ID)));
                category.setCategoryTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_TITLE)));
                category.setDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE)));
                byte[] drawableByteArray = cursor.getBlob(cursor.getColumnIndexOrThrow(KEY_CATEGORY_IMAGE));
                category.setCategoryImage(byteArrayToDrawable(drawableByteArray));
                int protection = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PASSWORD));
                category.setCategoryProtection(Category.CategoryProtection.castFromInt(protection));
                categories.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    public ArrayList<Category> getAllCategories(String orderBy, String orderType) {
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<Category> categories = new ArrayList<>();
        Cursor cursor;

        if (orderBy != null && orderType != null) {
            cursor = db.rawQuery("Select  * from " + TABLE_CATEGORY + " order by " + orderBy
                    + " " + orderType, null);
        } else {
            cursor = db.rawQuery("Select  * from " + TABLE_CATEGORY, null);
        }

        if (cursor.moveToFirst()) {
            do {
                Category category = new Category();
                category.setCategoryId(cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_ID)));
                category.setCategoryTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_TITLE)));
                category.setDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE)));
                byte[] drawableByteArray = cursor.getBlob(cursor.getColumnIndexOrThrow(KEY_CATEGORY_IMAGE));
                category.setCategoryImage(byteArrayToDrawable(drawableByteArray));
                int protection = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PASSWORD));
                category.setCategoryProtection(Category.CategoryProtection.castFromInt(protection));
                categories.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    public ArrayList<Bookmark> getBookmarks(String orderBy, String orderType) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Bookmark> bookmarks = new ArrayList<>();

        Cursor c = db.rawQuery("Select " + CATEGORY_ID + " from " + TABLE_CATEGORY +
                        " where " + KEY_CATEGORY_TITLE + " = ?",
                new String[]{context.getString(R.string.archived_bookmarks)});

        int lock = Category.CategoryProtection.LOCK.getValue();
        Cursor cur = db.rawQuery("Select " + CATEGORY_ID + " from " + TABLE_CATEGORY +
                        " where " + KEY_PASSWORD + " = ?",
                new String[]{String.valueOf(lock)});

        ArrayList<String> categoriesLocked = new ArrayList<>();
        if (cur.moveToFirst()) {
            do {
                categoriesLocked.add(cur.getString(cur.getColumnIndexOrThrow(CATEGORY_ID)));
            } while (cur.moveToNext());
        }
        if (c.moveToFirst()) {
            categoriesLocked.add(c.getString(c.getColumnIndexOrThrow(CATEGORY_ID)));
            Cursor cursor;
            String query;
            if (orderBy != null && orderType != null) {
                query = "SELECT * FROM " + TABLE_BOOKMARK + " WHERE " + KEY_BOOKMARK_CATEGORY +
                        " NOT IN (" + makePlaceholders(categoriesLocked.size()) + ")" +
                        " order by " + orderBy + " " + orderType;

            } else {
                query = "SELECT * FROM " + TABLE_BOOKMARK + " WHERE " + KEY_BOOKMARK_CATEGORY +
                        " NOT IN (" + makePlaceholders(categoriesLocked.size()) + ")";
            }
            cursor = db.rawQuery(query, categoriesLocked.toArray(new String[0]));

            if (cursor.moveToFirst()) {
                do {
                    Bookmark bookmark = new Bookmark();
                    bookmark.setId(cursor.getString(cursor.getColumnIndexOrThrow(BOOKMARK_ID)));
                    bookmark.setLink(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKMARK_LINK)));
                    bookmark.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKMARK_TITLE)));
                    bookmark.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKMARK_DESCRIPTION)));
                    bookmark.setImage(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKMARK_IMAGE)));
                    bookmark.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKMARK_CATEGORY)));
                    bookmark.setReminder(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_BOOKMARK_REMINDER)));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKMARK_TYPE));
                    bookmark.setType(Bookmark.ItemType.valueOf(type));
                    bookmark.setDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE)));
                    bookmarks.add(bookmark);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        c.close();
        cur.close();
        return bookmarks;
    }

    public String getCategoryId(String categoryName) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select " + CATEGORY_ID + " from " + TABLE_CATEGORY +
                        " where " + KEY_CATEGORY_TITLE + " = ?",
                new String[]{categoryName});

        if (cursor.moveToFirst()) {
            String categoryId = cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_ID));
            cursor.close();
            return categoryId;
        } else {
            return null;
        }
    }

    public Category getCategoryById(String categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select *" + " from " + TABLE_CATEGORY +
                        " where " + CATEGORY_ID + " = ?",
                new String[]{categoryId});

        if (cursor.moveToFirst()) {
            Category category = new Category();
            category.setCategoryId(cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_ID)));
            category.setCategoryTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_TITLE)));
            category.setDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE)));
            byte[] drawableByteArray = cursor.getBlob(cursor.getColumnIndexOrThrow(KEY_CATEGORY_IMAGE));
            category.setCategoryImage(byteArrayToDrawable(drawableByteArray));
            int protection = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PASSWORD));
            category.setCategoryProtection(Category.CategoryProtection.castFromInt(protection));
            cursor.close();
            return category;
        } else {
            return null;
        }
    }

    public boolean getCategoryProtectionByTitle(String categoryTitle) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select " + KEY_PASSWORD + " from " + TABLE_CATEGORY +
                        " where " + KEY_CATEGORY_TITLE + " = ?",
                new String[]{categoryTitle});

        if (cursor.moveToFirst()) {
            int protection = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PASSWORD));
            if (Category.CategoryProtection.castFromInt(protection).equals(Category.CategoryProtection.LOCK)) {
                cursor.close();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public ArrayList<Bookmark> getBookmarksByCategory(String category, String orderBy, String orderType) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Bookmark> bookmarks = new ArrayList<>();

        Cursor c = db.rawQuery("Select " + CATEGORY_ID + " from " + TABLE_CATEGORY +
                " where " + KEY_CATEGORY_TITLE + " = ?", new String[]{category});

        if (c.moveToFirst()) {
            Cursor cursor;
            String id = c.getString(c.getColumnIndexOrThrow(CATEGORY_ID));
            if (orderBy != null && orderType != null) {
                cursor = db.rawQuery("Select  * from " + TABLE_BOOKMARK + " where "
                                + KEY_BOOKMARK_CATEGORY + " = ?" + "order by " + orderBy + " " + orderType,
                        new String[]{id});
            } else {
                cursor = db.rawQuery("Select  * from " + TABLE_BOOKMARK + " where "
                        + KEY_BOOKMARK_CATEGORY + " = ?", new String[]{id});
            }

            if (cursor.moveToFirst()) {
                do {
                    Bookmark bookmark = new Bookmark();
                    bookmark.setId(cursor.getString(cursor.getColumnIndexOrThrow(BOOKMARK_ID)));
                    bookmark.setLink(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKMARK_LINK)));
                    bookmark.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKMARK_TITLE)));
                    bookmark.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKMARK_DESCRIPTION)));
                    bookmark.setImage(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKMARK_IMAGE)));
                    bookmark.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKMARK_CATEGORY)));
                    bookmark.setReminder(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_BOOKMARK_REMINDER)));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKMARK_TYPE));
                    bookmark.setType(Bookmark.ItemType.valueOf(type));
                    bookmark.setDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE)));
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

        ArrayList<Bookmark> bookmarks = getBookmarksByCategory(category.getCategoryTitle(),
                null, null);

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
        values.put(KEY_CATEGORY_TITLE, category.getCategoryTitle());
        Drawable image = category.getCategoryImage();
        values.put(KEY_CATEGORY_IMAGE, drawableToByteArray(image));
        values.put(KEY_DATE, category.getDate());
        values.put(KEY_PASSWORD, category.getPasswordProtectionValue());
        int result = db.update(TABLE_CATEGORY, values, CATEGORY_ID + " = ?",
                new String[]{category.getCategoryId()});

        return result == 1;
    }

    public boolean updateBookmark(Bookmark bookmark) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_BOOKMARK_LINK, bookmark.getLink());
        values.put(KEY_BOOKMARK_TITLE, bookmark.getTitle());
        values.put(KEY_BOOKMARK_DESCRIPTION, bookmark.getDescription());
        values.put(KEY_BOOKMARK_IMAGE, bookmark.getImage());
        values.put(KEY_BOOKMARK_CATEGORY, bookmark.getCategory());
        values.put(KEY_BOOKMARK_REMINDER, bookmark.getReminder());
        values.put(KEY_BOOKMARK_TYPE, String.valueOf(bookmark.getType()));
        values.put(KEY_DATE, bookmark.getDate());

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
                TABLE_BOOKMARK, KEY_BOOKMARK_CATEGORY, KEY_BOOKMARK_PREVIOUS_CATEGORY, BOOKMARK_ID,
                args));
    }

    public void deleteCategories(ArrayList<Category> categories) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] whereArgs = new String[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            whereArgs[i] = categories.get(i).getCategoryId();
        }

        String args = TextUtils.join(", ", whereArgs);
        db.execSQL(String.format("DELETE FROM " + TABLE_CATEGORY + " WHERE " + CATEGORY_ID +
                " IN (%s);", args));

        db.execSQL(String.format("DELETE FROM " + TABLE_BOOKMARK + " WHERE " + KEY_BOOKMARK_CATEGORY +
                " IN (%s);", args));
    }

    public boolean updateBookmarkCategory(ArrayList<Bookmark> bookmarks, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select " + CATEGORY_ID + " from " + TABLE_CATEGORY + " where "
                + KEY_CATEGORY_TITLE + " = ?", new String[]{category});

        if (cursor.moveToFirst()) {
            String[] whereArgs = new String[bookmarks.size()];
            for (int i = 0; i < bookmarks.size(); i++) {
                whereArgs[i] = bookmarks.get(i).getId();
            }

            String args = TextUtils.join(", ", whereArgs);
            String categoryId = cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_ID));
            db.execSQL(String.format("UPDATE %s SET %s =" + categoryId + ", %s = " + null +
                            " WHERE %s IN (%s);",
                    TABLE_BOOKMARK, KEY_BOOKMARK_CATEGORY, KEY_BOOKMARK_PREVIOUS_CATEGORY,
                    BOOKMARK_ID, args));
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public boolean insertPassword(String password) {
        PasswordManager passwordManager = PasswordManager.getInstance();
        String hashPassword = passwordManager.hashPassword(password);

        if (hashPassword == null) {
            return false;
        } else {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_USER_PASSWORD, hashPassword);
            db.insert(TABLE_PASSWORD, null, values);
            return true;
        }
    }

    public String getPassword() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select " + KEY_USER_PASSWORD + " from " + TABLE_PASSWORD,
                null);
        if (cursor.moveToFirst()) {
            String password = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_PASSWORD));
            cursor.close();
            return password;
        } else {
            cursor.close();
            return null;
        }
    }

    public boolean updatePassword(String newPassword) {
        PasswordManager passwordManager = PasswordManager.getInstance();
        String hashPassword = passwordManager.hashPassword(newPassword);

        if (hashPassword == null) {
            return false;
        } else {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_USER_PASSWORD, hashPassword);
            return db.update(TABLE_PASSWORD, values, null, null) > 0;
        }
    }

    private String makePlaceholders(int len) {
        if (len < 1) {
            throw new RuntimeException("No placeholders");
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }

    private byte[] drawableToByteArray(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private Drawable byteArrayToDrawable(byte[] image) {
        if (image != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            return new BitmapDrawable(context.getResources(), bitmap);
        } else {
            return null;
        }
    }

    public ArrayList<Bookmark> getAllBookmarks() {
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<Bookmark> bookmarks = new ArrayList<>();
        Cursor cursor = db.rawQuery("Select  * from " + TABLE_BOOKMARK,
                new String[]{});

        if (cursor.moveToFirst()) {
            do {
                Bookmark bookmark = new Bookmark();
                bookmark.setId(cursor.getString(cursor.getColumnIndexOrThrow(BOOKMARK_ID)));
                bookmark.setLink(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKMARK_LINK)));
                bookmark.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKMARK_TITLE)));
                bookmark.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKMARK_DESCRIPTION)));
                bookmark.setImage(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKMARK_IMAGE)));
                bookmark.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKMARK_CATEGORY)));
                bookmark.setReminder(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_BOOKMARK_REMINDER)));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOOKMARK_TYPE));
                bookmark.setType(Bookmark.ItemType.valueOf(type));
                bookmark.setDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE)));
                bookmarks.add(bookmark);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookmarks;
    }
}
