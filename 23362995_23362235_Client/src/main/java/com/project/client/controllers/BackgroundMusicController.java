package com.project.client.controllers;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;

public class BackgroundMusicController {
    private static BackgroundMusicController instance;
    private MediaPlayer mediaPlayer;

    private BackgroundMusicController() {
        String musicFile = "src/main/resources/music/sunnydays.mp3";
        Media media = new Media(new File(musicFile).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
    }

    public static BackgroundMusicController getInstance() {
        if (instance == null) {
            instance = new BackgroundMusicController();
        }
        return instance;
    }

    public void play() {
        if (mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            mediaPlayer.play();
        }
    }

    public void stop() {
        mediaPlayer.stop();
    }
}

