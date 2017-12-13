package br.ufpe.cin.if710.podcast.db.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.UUID;

import br.ufpe.cin.if710.podcast.domain.NewItemFeed;

import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_DATE;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_DESC;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_DOWNLOAD_LINK;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_DOWNLOAD_STATE;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_FILE_URI;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_LINK;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_TIMESTAMP;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_TITLE;

/**
 * Created by danil on 13/12/2017.
 */
@Entity(tableName = "episodes")
public class ItemFeedEntity {

    @PrimaryKey()
    private UUID id;

    @ColumnInfo(name=EPISODE_TITLE)
    private String episodeTitle;

    @ColumnInfo(name=EPISODE_DATE)
    private String episodeDate;

    @ColumnInfo(name=EPISODE_DESC)
    private String episodeDesc;

    @ColumnInfo(name=EPISODE_DOWNLOAD_LINK)
    private String episodeDownloadLink;

    @ColumnInfo(name=EPISODE_LINK)
    private String episodeLink;

    @ColumnInfo(name=EPISODE_TIMESTAMP)
    private int episodeTimestamp;

    @ColumnInfo(name=EPISODE_FILE_URI)
    private String episodeFileUri;

    @ColumnInfo(name=EPISODE_DOWNLOAD_STATE)
    private int episodeDownloadState;

    public ItemFeedEntity(){

    }

    public ItemFeedEntity(NewItemFeed item){
        this.id = UUID.randomUUID();
        this.episodeDate = item.getPubDate();
        this.episodeDesc = item.getDescription();
        this.episodeDownloadLink = item.getDownloadLink();
        this.episodeFileUri = item.getDownloadUri();
        this.episodeDownloadState = item.getDownloadState();
        this.episodeTimestamp = 0;
        this.episodeTitle = item.getTitle();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEpisodeTitle() {
        return episodeTitle;
    }

    public void setEpisodeTitle(String episodeTitle) {
        this.episodeTitle = episodeTitle;
    }

    public String getEpisodeDate() {
        return episodeDate;
    }

    public void setEpisodeDate(String episodeDate) {
        this.episodeDate = episodeDate;
    }

    public String getEpisodeDesc() {
        return episodeDesc;
    }

    public void setEpisodeDesc(String episodeDesc) {
        this.episodeDesc = episodeDesc;
    }

    public String getEpisodeDownloadLink() {
        return episodeDownloadLink;
    }

    public void setEpisodeDownloadLink(String episodeDownloadLink) {
        this.episodeDownloadLink = episodeDownloadLink;
    }

    public String getEpisodeLink() {
        return episodeLink;
    }

    public void setEpisodeLink(String episodeLink) {
        this.episodeLink = episodeLink;
    }

    public int getEpisodeTimestamp() {
        return episodeTimestamp;
    }

    public void setEpisodeTimestamp(int episodeTimestamp) {
        this.episodeTimestamp = episodeTimestamp;
    }

    public String getEpisodeFileUri() {
        return episodeFileUri;
    }

    public void setEpisodeFileUri(String episodeFileUri) {
        this.episodeFileUri = episodeFileUri;
    }

    public int getEpisodeDownloadState() {
        return episodeDownloadState;
    }

    public void setEpisodeDownloadState(int episodeDownloadState) {
        this.episodeDownloadState = episodeDownloadState;
    }
}
