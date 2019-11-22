package info.metadude.android.eventfahrplan.database.sqliteopenhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.LecturesTable;
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.LecturesTable.Columns;
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.LecturesTable.Defaults;
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.LecturesTable.Values;

public class LecturesDBOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 8;

    private static final String DATABASE_NAME = "lectures";

    private static final String LECTURES_TABLE_CREATE =
            "CREATE TABLE " + LecturesTable.NAME + " (" +
                    Columns.EVENT_ID + " TEXT, " +
                    Columns.TITLE + " TEXT, " +
                    Columns.SUBTITLE + " TEXT, " +
                    Columns.DAY + " INTEGER, " +
                    Columns.ROOM + " STRING, " +
                    Columns.SLUG + " TEXT, " +
                    Columns.START + " INTEGER, " +
                    Columns.DURATION + " INTEGER, " +
                    Columns.SPEAKERS + " STRING, " +
                    Columns.TRACK + " STRING, " +
                    Columns.TYPE + " STRING, " +
                    Columns.LANG + " STRING, " +
                    Columns.ABSTRACT + " STRING, " +
                    Columns.DESCR + " STRING, " +
                    Columns.REL_START + " INTEGER, " +
                    Columns.DATE + " STRING, " +
                    Columns.LINKS + " STRING, " +
                    Columns.DATE_UTC + " INTEGER, " +
                    Columns.ROOM_IDX + " INTEGER, " +
                    Columns.REC_LICENSE + " STRING, " +
                    Columns.REC_OPTOUT + " INTEGER," +
                    Columns.URL + " TEXT," +
                    Columns.CHANGED_TITLE + " INTEGER," +
                    Columns.CHANGED_SUBTITLE + " INTEGER," +
                    Columns.CHANGED_ROOM + " INTEGER," +
                    Columns.CHANGED_DAY + " INTEGER," +
                    Columns.CHANGED_SPEAKERS + " INTEGER," +
                    Columns.CHANGED_RECORDING_OPTOUT + " INTEGER," +
                    Columns.CHANGED_LANGUAGE + " INTEGER," +
                    Columns.CHANGED_TRACK + " INTEGER," +
                    Columns.CHANGED_IS_NEW + " INTEGER," +
                    Columns.CHANGED_TIME + " INTEGER," +
                    Columns.CHANGED_DURATION + " INTEGER," +
                    Columns.CHANGED_IS_CANCELED + " INTEGER)";

    public LecturesDBOpenHelper(@NonNull Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LECTURES_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2 && newVersion >= 2) {
            db.execSQL("ALTER TABLE " + LecturesTable.NAME +
                    " ADD COLUMN " + Columns.DATE_UTC + " INTEGER DEFAULT " +
                    Defaults.DATE_UTC_DEFAULT);
        }
        if (oldVersion < 3 && newVersion >= 3) {
            db.execSQL("ALTER TABLE " + LecturesTable.NAME +
                    " ADD COLUMN " + Columns.ROOM_IDX + " INTEGER DEFAULT " +
                    Defaults.ROOM_IDX_DEFAULT);
        }
        if (oldVersion < 4 && newVersion >= 4) {
            db.execSQL("ALTER TABLE " + LecturesTable.NAME +
                    " ADD COLUMN " + Columns.REC_LICENSE + " STRING DEFAULT ''");
            db.execSQL("ALTER TABLE " + LecturesTable.NAME +
                    " ADD COLUMN " + Columns.REC_OPTOUT + " INTEGER DEFAULT " +
                    Values.REC_OPT_OUT_OFF);
        }
        if (oldVersion < 5 && newVersion >= 5) {
            db.execSQL("ALTER TABLE " + LecturesTable.NAME + " ADD COLUMN " + Columns.CHANGED_TITLE + " INTEGER DEFAULT " + 0);
            db.execSQL("ALTER TABLE " + LecturesTable.NAME + " ADD COLUMN " + Columns.CHANGED_SUBTITLE + " INTEGER DEFAULT " + 0);
            db.execSQL("ALTER TABLE " + LecturesTable.NAME + " ADD COLUMN " + Columns.CHANGED_ROOM + " INTEGER DEFAULT " + 0);
            db.execSQL("ALTER TABLE " + LecturesTable.NAME + " ADD COLUMN " + Columns.CHANGED_DAY + " INTEGER DEFAULT " + 0);
            db.execSQL("ALTER TABLE " + LecturesTable.NAME + " ADD COLUMN " + Columns.CHANGED_SPEAKERS + " INTEGER DEFAULT " + 0);
            db.execSQL("ALTER TABLE " + LecturesTable.NAME + " ADD COLUMN " + Columns.CHANGED_RECORDING_OPTOUT + " INTEGER DEFAULT " + 0);
            db.execSQL("ALTER TABLE " + LecturesTable.NAME + " ADD COLUMN " + Columns.CHANGED_LANGUAGE + " INTEGER DEFAULT " + 0);
            db.execSQL("ALTER TABLE " + LecturesTable.NAME + " ADD COLUMN " + Columns.CHANGED_TRACK + " INTEGER DEFAULT " + 0);
            db.execSQL("ALTER TABLE " + LecturesTable.NAME + " ADD COLUMN " + Columns.CHANGED_IS_NEW + " INTEGER DEFAULT " + 0);
            db.execSQL("ALTER TABLE " + LecturesTable.NAME + " ADD COLUMN " + Columns.CHANGED_TIME + " INTEGER DEFAULT " + 0);
            db.execSQL("ALTER TABLE " + LecturesTable.NAME + " ADD COLUMN " + Columns.CHANGED_DURATION + " INTEGER DEFAULT " + 0);
            db.execSQL("ALTER TABLE " + LecturesTable.NAME + " ADD COLUMN " + Columns.CHANGED_IS_CANCELED + " INTEGER DEFAULT " + 0);
        }
        if (oldVersion < 6 && newVersion >= 6) {
            db.execSQL("ALTER TABLE " + LecturesTable.NAME + " ADD COLUMN " + Columns.SLUG + " TEXT DEFAULT ''");
        }
        if (oldVersion < 7 && newVersion >= 7) {
            db.execSQL("ALTER TABLE " + LecturesTable.NAME + " ADD COLUMN " + Columns.URL + " TEXT DEFAULT ''");
        }
        if (oldVersion < 8) {
            // Clear database from 34C3.
            db.execSQL("DROP TABLE IF EXISTS " + LecturesTable.NAME);
            onCreate(db);
        }
    }
}
