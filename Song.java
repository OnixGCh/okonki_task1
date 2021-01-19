package okonki_task1;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

public class Song {
    private final int index;
    private String name;
    private String time;
    private Integer minutes;
    private Integer seconds;
    private final MediaPlayer mediaPlayer;

    Song(int index, File file) {
        this.index = index;
        this.name = file.getName();

        Media media = new Media(file.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public String getTime() {
        return time;
    }

    public void setMinutes(double value) {
        minutes = (int)(value / 60);
        return;
    }

    public void setSeconds(double value) {
        seconds = (int)value % 60;
        return;
    }

    public void setTime() {
        time = minutes + ":" + seconds;
        return;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}
