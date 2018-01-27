package productions.darthplagueis.asynccolordownloader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;

/**
 * Created by oleg on 1/24/18.
 */

public class ColorDataBase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "colors.db";
    private static final String TABLE_NAME = "color_table";
    private static final String NAME_COLUMN = "color_names";
    private static final String VALUE_COLUMN = "color_values";
    private static final int SCHEMA_VERSION = 1;

    private static ColorDataBase instance;

    public static synchronized ColorDataBase getInstance(Context context) {
        if (instance == null) {
            instance = new ColorDataBase(context.getApplicationContext());
        }
        return instance;
    }

    private ColorDataBase(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(String.format("CREATE TABLE %s (%s STRING PRIMARY KEY, %s STRING);", TABLE_NAME, NAME_COLUMN, VALUE_COLUMN));
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    public boolean insertColor(String name, String value) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(NAME_COLUMN, name);
        contentValues.put(VALUE_COLUMN, value);
        sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        return true;
    }

    public int numberOfRows() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(sqLiteDatabase, TABLE_NAME);
        return numRows;
    }

    public HashMap<String, String> getColorMap() {
        HashMap<String, String> colorsValuesMap = new HashMap<>();
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM " + TABLE_NAME + ";", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    colorsValuesMap.put(cursor.getString(cursor.getColumnIndex(NAME_COLUMN)),
                            cursor.getString(cursor.getColumnIndex(VALUE_COLUMN)));
                } while (cursor.moveToNext());
            }
        }
        return colorsValuesMap;
    }
}
