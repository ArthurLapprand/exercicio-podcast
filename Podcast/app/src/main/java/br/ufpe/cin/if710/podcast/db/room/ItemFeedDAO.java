package br.ufpe.cin.if710.podcast.db.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Insert;
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

    @Query("SELECT * FROM episodes")
    public List<ItemFeedEntity> getAllEpisodes();

    @Query("SELECT * FROM episodes WHERE downloadLink = :itemDownloadLink")
    public ItemFeedEntity getEpisodeFromDownloadLink(String itemDownloadLink);

    @Query("SELECT * FROM episodes WHERE downloadUri = :itemFileUri")
    public ItemFeedEntity getEpisodeFromFileURI(String itemFileUri);

    @Update()
    public void updateIteemFeed(ItemFeedEntity item);

    @Insert
    public void insertAll(ItemFeedEntity... items);

    @Delete
    public void delete(ItemFeedEntity item);


}
