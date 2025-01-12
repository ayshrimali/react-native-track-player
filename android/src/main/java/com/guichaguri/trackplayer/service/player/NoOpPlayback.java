package com.guichaguri.trackplayer.service.player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.database.DatabaseProvider;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.guichaguri.trackplayer.service.MusicManager;
import com.guichaguri.trackplayer.service.Utils;
import com.guichaguri.trackplayer.service.models.Track;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Guichaguri
 */
public class NoOpPlayback extends ExoPlayback<SimpleExoPlayer> {

//    private final long cacheMaxSize;

//    private SimpleCache cache;
//    private ConcatenatingMediaSource source;
    private boolean prepared = false;
    protected int currentState = PlaybackStateCompat.STATE_NONE;

    public NoOpPlayback(Context context, MusicManager manager, SimpleExoPlayer player, long maxCacheSize,
                        boolean autoUpdateMetadata) {
        super(context, manager, player, autoUpdateMetadata);
//        this.cacheMaxSize = maxCacheSize;
    }

    @Override
    public void initialize() {
//        if(cacheMaxSize > 0) {
//            File cacheDir = new File(context.getCacheDir(), "TrackPlayer");
//            DatabaseProvider db = new ExoDatabaseProvider(context);
//            cache = new SimpleCache(cacheDir, new LeastRecentlyUsedCacheEvictor(cacheMaxSize), db);
//        } else {
//            cache = null;
//        }

        super.initialize();

        resetQueue();
    }

//    public DataSource.Factory enableCaching(DataSource.Factory ds) {
//        if(cache == null || cacheMaxSize <= 0) return ds;
//
//        return new CacheDataSourceFactory(cache, ds, CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
//    }

    private void prepare() {
        if(!prepared) {
            Log.d(Utils.LOG, "Preparing the media source...");
            // player.prepare(source, false, false);
            prepared = true;
        }

        Log.d(Utils.LOG, "queue size " + queue.size());
        if (queue.size() > 0) {
            // only one track is supported currently
            Track currentTrack = queue.get(0);
            String source = null;
            String title = currentTrack.title;
            String url = currentTrack.uri.toString();
            String artist = currentTrack.artist;
            String album = currentTrack.album;
            String date = currentTrack.date;
            String genre = currentTrack.genre;
            manager.onMetadataReceived(source, title, url, artist, album, date, genre);
        }
    }

    @Override
    public int getState() {
        return currentState;
    }

    @Override
    public void add(Track track, int index, Promise promise) {
        Log.d(Utils.LOG, "NoOpPlayer add track called");
        queue.add(index, track);
//        MediaSource trackSource = track.toMediaSource(context, this);
//        source.addMediaSource(index, trackSource, manager.getHandler(), () -> promise.resolve(index));

        promise.resolve(index);
        prepare();
    }

    @Override
    public void add(Collection<Track> tracks, int index, Promise promise) {
        Log.d(Utils.LOG, "NoOpPlayer add tracks called");
//        List<MediaSource> trackList = new ArrayList<>();

//        for(Track track : tracks) {
//            trackList.add(track.toMediaSource(context, this));
//        }

        queue.addAll(index, tracks);
        // source.addMediaSources(index, trackList, manager.getHandler(), () -> promise.resolve(index));

        promise.resolve(index);
        prepare();
    }

    @Override
    public long getBufferedPosition() {
        return 0L;
    }

    @Override
    public float getRate() {
        return 1.0f;
    }

    @Override
    public long getPosition() {
        return 0L;
    }

    @Override
    public void remove(List<Integer> indexes, Promise promise) {
        int currentIndex = 0; //player.getCurrentWindowIndex();

        // Sort the list so we can loop through sequentially
        Collections.sort(indexes);

        for(int i = indexes.size() - 1; i >= 0; i--) {
            int index = indexes.get(i);

            // Skip indexes that are the current track or are out of bounds
            if(index == currentIndex || index < 0 || index >= queue.size()) {
                // Resolve the promise when the last index is invalid
                if(i == 0) promise.resolve(null);
                continue;
            }

            queue.remove(index);

//            if(i == 0) {
//                source.removeMediaSource(index, manager.getHandler(), Utils.toRunnable(promise));
//            } else {
//                source.removeMediaSource(index);
//            }

            // Fix the window index
            if (index < lastKnownWindow) {
                lastKnownWindow--;
            }
        }
    }

    @Override
    public void removeUpcomingTracks() {
        int currentIndex = player.getCurrentWindowIndex();
        if (currentIndex == C.INDEX_UNSET) return;

        for (int i = queue.size() - 1; i > currentIndex; i--) {
            queue.remove(i);
//            source.removeMediaSource(i);
        }
    }

    @Override
    public void setRepeatMode(int repeatMode) {
        player.setRepeatMode(repeatMode);
    }

    public int getRepeatMode() {
        return player.getRepeatMode();
    }

    private void resetQueue() {
        queue.clear();

//        source = new ConcatenatingMediaSource();
//        player.prepare(source, true, true);
        prepared = false; // We set it to false as the queue is now empty

        lastKnownWindow = C.INDEX_UNSET;
        lastKnownPosition = C.POSITION_UNSET;

        manager.onReset();
    }

    @SuppressLint("WrongConstant")
    @Override
    public void play() {
        prepare();
//        super.play();
        onPlayerStateChanged(true, PlaybackStateCompat.STATE_PLAYING);
    }

    @SuppressLint("WrongConstant")
    @Override
    public void pause() {
//        super.pause();
        onPlayerStateChanged(true, PlaybackStateCompat.STATE_PAUSED);
    }

    @SuppressLint("WrongConstant")
    @Override
    public void stop() {
//        super.stop();
        onPlayerStateChanged(true, PlaybackStateCompat.STATE_STOPPED);
        prepared = false;
    }

    @SuppressLint("WrongConstant")
    public void markConnecting() {
        onPlayerStateChanged(true, PlaybackStateCompat.STATE_CONNECTING);
        prepared = false;
    }

    @SuppressLint("WrongConstant")
    public void markReady() {
        onPlayerStateChanged(true, PlaybackStateCompat.STATE_PAUSED);
        prepared = true;
    }

    public void reportError(final String error) {
        prepared = false;
        manager.onError("", error);
    }

    @Override
    public void seekTo(long time) {
        prepare();
//        super.seekTo(time);
    }

    @Override
    public void reset() {
        Integer track = getCurrentTrackIndex();
        long position = player.getCurrentPosition();

//        super.reset();
        resetQueue();

        manager.onTrackUpdate(track, position, null, null);
    }

    @Override
    public float getPlayerVolume() {
        return player.getVolume();
    }

    @Override
    public void setPlayerVolume(float volume) {
        player.setVolume(volume);
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if(playbackState == Player.STATE_ENDED) {
            prepared = false;
        }

        int state = playbackState;
        currentState = playbackState;
        if(state != previousState) {
            if(Utils.isPlaying(state) && !Utils.isPlaying(previousState)) {
                manager.onPlay();
            } else if(Utils.isPaused(state) && !Utils.isPaused(previousState)) {
                manager.onPause();
            } else if(Utils.isStopped(state) && !Utils.isStopped(previousState)) {
                manager.onStop();
            }

            manager.onStateChange(state);
            previousState = state;

            if(state == PlaybackStateCompat.STATE_STOPPED) {
                Integer previous = getCurrentTrackIndex();
                long position = getPosition();
                manager.onTrackUpdate(previous, position, null, null);
                manager.onEnd(getCurrentTrackIndex(), getPosition());
            }
        }

        // super.onPlayerStateChanged(playWhenReady, playbackState);
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        prepared = false;
        super.onPlayerError(error);
    }

    @Override
    public void destroy() {
        super.destroy();

//        if(cache != null) {
//            try {
//                cache.release();
//                cache = null;
//            } catch(Exception ex) {
//                Log.w(Utils.LOG, "Couldn't release the cache properly", ex);
//            }
//        }
    }
}
