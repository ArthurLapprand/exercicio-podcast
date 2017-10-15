package br.ufpe.cin.if710.podcast.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.DATABASE_TABLE;

public class PodcastProvider extends ContentProvider {

    PodcastDBHelper helper;

    public PodcastProvider() {
    }

    @Override
    public boolean onCreate() {
        helper = PodcastDBHelper.getInstance(getContext());
        return true;
    }

    private boolean isEpisodesUri(Uri uri) {
        return uri.getLastPathSegment().equals(DATABASE_TABLE);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (isEpisodesUri(uri)) {
            long id = helper.getWritableDatabase().insert(
                    DATABASE_TABLE, null, values
            );
            return Uri.withAppendedPath(PodcastProviderContract.EPISODE_LIST_URI, Long.toString(id));
        } else return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        if (isEpisodesUri(uri)) {
            cursor = helper.getReadableDatabase().query(
                    DATABASE_TABLE,
                    projection,
                    selection,
                    selectionArgs,
                    null, null,
                    sortOrder
            );
        }
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if (isEpisodesUri(uri)) {
            return helper.getWritableDatabase().update(
                    DATABASE_TABLE, values, selection, selectionArgs
            );
        } else return 0;
    }
}
