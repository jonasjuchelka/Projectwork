package de.tum.cit.aet.valleyday.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

// Kommentar
/**
 * This enum is used to manage the music tracks in the game.
 * Currently, only one track is used, but this could be extended to include multiple tracks.
 * Using an enum for this purpose is a good practice, as it allows for easy management of the music tracks
 * and prevents the same track from being loaded into memory multiple times.
 * See the assets/audio folder for the actual music files.
 * Feel free to add your own music tracks and use them in the game!
 */
public enum MusicTrack {
    
    BACKGROUND("background.mp3", 0.2f);
    
    /** The music file owned by this variant. */
    private final Music music;
    /** Whether the track is currently muted. */
    private boolean muted = false;
    
    MusicTrack(String fileName, float volume) {
        this.music = Gdx.audio.newMusic(Gdx.files.internal("audio/" + fileName));
        this.music.setLooping(true);
        this.music.setVolume(volume);
    }
    
    /**
     * Play this music track if it is not muted.
     * This will not stop other music from playing - if you add more tracks, you will have to handle that yourself.
     */
    public void play() {
        if (!muted) {
            this.music.play();
        }
    }

    /** Pause playback regardless of mute state. */
    public void pause() {
        this.music.pause();
    }

    /** Resume playback if not muted. */
    public void resume() {
        if (!muted && !music.isPlaying()) {
            this.music.play();
        }
    }

    /** Toggle mute and immediately apply the new state. */
    public void toggleMute() {
        setMuted(!muted);
    }

    /** Set the mute state; pauses when muted, resumes when unmuted. */
    public void setMuted(boolean muted) {
        this.muted = muted;
        if (muted) {
            music.pause();
        } else {
            music.play();
        }
    }

    public boolean isMuted() {
        return muted;
    }
}
