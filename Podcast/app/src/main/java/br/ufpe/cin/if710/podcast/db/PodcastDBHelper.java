package br.ufpe.cin.if710.podcast.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.*;


class PodcastDBHelper extends SQLiteOpenHelper {



    private static final int DB_VERSION = 1;

    private PodcastDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    private static PodcastDBHelper db;

    static PodcastDBHelper getInstance(Context c) {
        if (db == null) {
            db = new PodcastDBHelper(c.getApplicationContext());
        }
        return db;
    }

    final private static String CREATE_CMD =
            "CREATE TABLE " + DATABASE_TABLE + " (" + _ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + EPISODE_TITLE + " TEXT NOT NULL, "
                    + EPISODE_DATE + " TEXT NOT NULL, "
                    + EPISODE_LINK + " TEXT NOT NULL, "
                    + EPISODE_DESC + " TEXT NOT NULL, "
                    + EPISODE_DOWNLOAD_LINK + " TEXT NOT NULL, "
                    + EPISODE_FILE_URI + " TEXT, "
                    + EPISODE_TIMESTAMP + " INTEGER, "
                    + EPISODE_DOWNLOAD_STATE + " INTEGER)";


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_CMD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        throw new RuntimeException("inutilizado");
    }

}
