package br.ufpe.cin.if710.podcast.db.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_DOWNLOAD_STATE;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_FILE_URI;

/**
 * Created by danil on 13/12/2017.
 */
@Dao
public interface ItemFeedDAO {

    @Query("SELECT * from episodes")
    public List<ItemFeedEntity> getAllEpisodes();

    @Update()
    public void updateIteemFeed();

    

}
