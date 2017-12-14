package br.ufpe.cin.if710.podcast.domain;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import br.ufpe.cin.if710.podcast.db.room.ItemFeedEntity;

/**
 * Created by Lapp on 14/12/2017.
 */

public class LiveItemFeed extends ViewModel {
    private MutableLiveData<List<ItemFeedEntity>> feedLiveData;

    public MutableLiveData<List<ItemFeedEntity>> getFeedLiveData() {
        if (feedLiveData == null) feedLiveData = new MutableLiveData<>();
        return feedLiveData;
    }
}
