package br.ufpe.cin.if710.podcast.db.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by danil on 13/12/2017.
 */
@Database(entities= {ItemFeedEntity.class}, version=1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ItemFeedDAO itemFeedDAO();
}
