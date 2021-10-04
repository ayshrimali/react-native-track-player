package com.guichaguri.trackplayer;

import android.media.AudioManager;

public class EarpieceSpeakerState {

    private AudioManager audioManager;

    public EarpieceSpeakerState(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    public boolean usingEarpieceSpeaker() {
        return playingSound()
                && routingToEarpiece();
    }

    private boolean playingSound() {
        return audioManager.getMode() != AudioManager.MODE_NORMAL;
    }

    private boolean routingToEarpiece() {
        return !(
                audioManager.isSpeakerphoneOn()
                        || audioManager.isBluetoothScoOn()
                        || audioManager.isBluetoothA2dpOn()
                        || audioManager.isWiredHeadsetOn()
        );
    }
}
