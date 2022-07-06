package com.dream.camera.modules.continuephoto;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;

import com.android.camera2.R;

public class ContinueMediaActionSound {
    private static final int NUM_MEDIA_SOUND_STREAMS = 1;

    private Context mContext;
    private SoundPool mSoundPool;
    private ContinueMediaActionSound.SoundState[] mSounds;

    private static final int[] SOUND_IDS = {
            R.raw.camera_shutter
    };

    private static final String TAG = "CMediaActionSound";

    public static final int CAMERA_SHUTTER         = 0;

    private static final int STATE_NOT_LOADED             = 0;
    private static final int STATE_LOADING                = 1;
    private static final int STATE_LOADING_PLAY_REQUESTED = 2;
    private static final int STATE_LOADED                 = 3;

    private class SoundState {
        public final int name;
        public int id;
        public int state;

        public SoundState(int name) {
            this.name = name;
            id = 0; // 0 is an invalid sample ID.
            state = STATE_NOT_LOADED;
        }
    }

    public ContinueMediaActionSound(Context context) {
        mContext = context;
        mSoundPool = new SoundPool.Builder()
                .setMaxStreams(NUM_MEDIA_SOUND_STREAMS)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .build();
        mSoundPool.setOnLoadCompleteListener(mLoadCompleteListener);
        mSounds = new ContinueMediaActionSound.SoundState[SOUND_IDS.length];
        for (int i = 0; i < mSounds.length; i++) {
            mSounds[i] = new ContinueMediaActionSound.SoundState(i);
        }
    }

    private int loadSound(ContinueMediaActionSound.SoundState sound) {
        for (int soundId : SOUND_IDS) {
            int id = mSoundPool.load(mContext, soundId, 1);
            if (id > 0) {
                sound.state = STATE_LOADING;
                sound.id = id;
                return id;
            }
        }

        return 0;
    }

    public void load(int soundId) {
        if (soundId < 0 || soundId >= SOUND_IDS.length) {
            throw new RuntimeException("Unknown sound requested: " + soundId);
        }
        ContinueMediaActionSound.SoundState sound = mSounds[soundId];
        synchronized (sound) {
            switch (sound.state) {
                case STATE_NOT_LOADED:
                    if (loadSound(sound) <= 0) {
                        Log.e(TAG, "load() error loading sound: " + soundId);
                    }
                    break;
                default:
                    Log.e(TAG, "load() called in wrong state: " + sound + " for sound: "+ soundId);
                    break;
            }
        }
    }

    public void play(int soundId) {
        if (soundId < 0 || soundId >= SOUND_IDS.length) {
            throw new RuntimeException("Unknown sound requested: " + soundId);
        }
        ContinueMediaActionSound.SoundState sound = mSounds[soundId];
        synchronized (sound) {
            switch (sound.state) {
                case STATE_NOT_LOADED:
                    loadSound(sound);
                    if (loadSound(sound) <= 0) {
                        Log.e(TAG, "play() error loading sound: " + soundId);
                        break;
                    }
                    // FALL THROUGH

                case STATE_LOADING:
                    sound.state = STATE_LOADING_PLAY_REQUESTED;
                    break;
                case STATE_LOADED:
                    mSoundPool.play(sound.id, 1.0f, 1.0f, 0, 0, 1.0f);
                    break;
                default:
                    Log.e(TAG, "play() called in wrong state: " + sound.state + " for sound: "+ soundId);
                    break;
            }
        }
    }

    private SoundPool.OnLoadCompleteListener mLoadCompleteListener =
            new SoundPool.OnLoadCompleteListener() {
                public void onLoadComplete(SoundPool soundPool,
                                           int sampleId, int status) {
                    for (ContinueMediaActionSound.SoundState sound : mSounds) {
                        if (sound.id != sampleId) {
                            continue;
                        }
                        int playSoundId = 0;
                        synchronized (sound) {
                            if (status != 0) {
                                sound.state = STATE_NOT_LOADED;
                                sound.id = 0;
                                Log.e(TAG, "OnLoadCompleteListener() error: " + status +
                                        " loading sound: "+ sound.name);
                                return;
                            }
                            switch (sound.state) {
                                case STATE_LOADING:
                                    sound.state = STATE_LOADED;
                                    break;
                                case STATE_LOADING_PLAY_REQUESTED:
                                    playSoundId = sound.id;
                                    sound.state = STATE_LOADED;
                                    break;
                                default:
                                    Log.e(TAG, "OnLoadCompleteListener() called in wrong state: "
                                            + sound.state + " for sound: "+ sound.name);
                                    break;
                            }
                        }
                        if (playSoundId != 0) {
                            soundPool.play(playSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
                        }
                        break;
                    }
                }
            };

    public void release() {
        if (mSoundPool != null) {
            for (ContinueMediaActionSound.SoundState sound : mSounds) {
                synchronized (sound) {
                    sound.state = STATE_NOT_LOADED;
                    sound.id = 0;
                }
            }
            mSoundPool.release();
            mSoundPool = null;
        }
    }
}
