package okonki_task1;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXToggleNode;
import global_task.JsonBlueprints;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.json.JSONObject;
import org.json.XML;

public class MPlayer extends Application {
    @FXML
    private Label songName;

    @FXML
    private JFXButton play;

    @FXML
    private JFXButton next;

    @FXML
    private JFXButton prev;

    @FXML
    private JFXButton showSongText;

    @FXML
    private Label time;

    @FXML
    private JFXSlider sliderDuration;

    @FXML
    private JFXSlider sliderVolume;

    @FXML
    private TableView<Song> playlist;

    @FXML
    private JFXToggleNode mediaSelect;

    @FXML
    private JFXButton mediaSelectButton;

    @FXML
    private JFXToggleNode mediaDelete;

    @FXML
    private JFXButton mediaDeleteButton;

    @FXML
    private TableColumn<Song, Integer> indexColumn;

    @FXML
    private TableColumn<Song, String> nameColumn;

    @FXML
    private TableColumn<Song, String> timeColumn;

    private MediaPlayer mediaPlayer = null;
    private int index = -1;
    private int size = 1;
    private Gson gson = new Gson();
    private ResultsArray resultsArray = new ResultsArray();
    private FinalResult finalResult = new FinalResult();

    @FXML
    void initialize() {
        sliderVolume.setValue(15);

        indexColumn.setCellValueFactory(new PropertyValueFactory<>("index"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));

        this.setImageOnButton(mediaDeleteButton, "remove.png", 40);
        this.setImageOnButton(mediaSelectButton, "add.png", 40);
        this.setImageOnButton(prev, "prev.png", 20);
        this.setImageOnButton(next, "next.png", 20);
        this.setImageOnButton(play, "play.png", 35);

        showSongText.setOnAction(keyEvent -> {
            Song selectedItem = playlist.getSelectionModel().getSelectedItem();

            if (selectedItem == null) {
                return;
            }

            String songRefactor = playlist.getSelectionModel().getSelectedItem().getName().substring(0, playlist.getSelectionModel().getSelectedItem().getName().length()-4);
            String firstPart = "";
            String secondPart = "";
            String result = "";

            for(Integer i = 0; i < songRefactor.length(); i++)
                if(songRefactor.charAt(i) == '-') {
                    firstPart = songRefactor.substring(0, i);
                    secondPart = songRefactor.substring(i + 1);
                }

            for(Integer i = 0; i < firstPart.length(); i++)
                if(firstPart.charAt(i) == ' ') {
                    firstPart = firstPart.substring(0, i) + "%20" + firstPart.substring(i + 1);
                }

            for(Integer i = 0; i < secondPart.length(); i++)
                if(secondPart.charAt(i) == ' ') {
                    secondPart = secondPart.substring(0, i) + "%20" + secondPart.substring(i + 1);
                }

            firstPart = firstPart.substring(0, firstPart.length()-3);
            secondPart = secondPart.substring(3);
            System.out.println(firstPart);
            System.out.println(secondPart);

            try {

                result = makeAPICall("http://api.chartlyrics.com/apiv1.asmx/SearchLyric?artist=" + firstPart + "&song=" + secondPart);
                JSONObject obj = XML.toJSONObject(result);
                JsonReader jsonReader = new JsonReader(new StringReader(obj.toString()));
                resultsArray = gson.fromJson(jsonReader, ResultsArray.class);

                firstPart = resultsArray.ArrayOfSearchLyricResult.SearchLyricResult[0].LyricChecksum;
                secondPart = resultsArray.ArrayOfSearchLyricResult.SearchLyricResult[0].LyricId;

                try {
                    //result = makeAPICall("https://api.genius.com/web_pages/lookup?access_token=DEfoBnanPcbpKZEd7Iix_7vQqFi_k7yeUK-IfFdUrrYV48CwqgVl4vvX-SJYLrmF&canonical_url=http://genius.com/" + songRefactor);
                    result = makeAPICall("http://api.chartlyrics.com/apiv1.asmx/GetLyric?lyricId="+ secondPart + "&lyricCheckSum=" + firstPart);
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }

                obj = XML.toJSONObject(result);
                jsonReader = new JsonReader(new StringReader(obj.toString()));
                finalResult = gson.fromJson(jsonReader, FinalResult.class);

                Stage newStage = new Stage();
                VBox vBox = new VBox();
                Label lbl = new Label(finalResult.GetLyricResult.Lyric);
                vBox.getChildren().add(lbl);
                newStage.setScene(new Scene(vBox));
                newStage.show();

            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });

        mediaDelete.setOnAction(actionEvent -> {
            showSongText.setVisible(false);
            playlist.getItems().clear();
            mediaPlayer.stop();
            mediaPlayer = null;
            index = -1;
            mediaDelete.setSelected(false);
            time.setText("mm:ss");
            songName.setText("name");
            sliderDuration.setValue(0);
            this.setImageOnButton(play, "play.png", 35);
        });

        mediaSelect.setOnAction(actionEvent -> {
            mediaSelectClick();
            mediaSelect.setSelected(false);
            songName.requestFocus();
        });

        sliderDuration.valueProperty().addListener(durationListener);
        sliderVolume.valueProperty().addListener(volumeListener);

        TableView.TableViewSelectionModel<Song> selectionModel = playlist.getSelectionModel();
        selectionModel.selectedItemProperty().addListener(tableChangeListener);

        play.setOnAction(actionEvent -> playButtonClick());
        next.setOnAction(actionEvent -> rewindClick(1));
        prev.setOnAction(actionEvent -> rewindClick(-1));
    }

    static class StyleRowFactory<T> implements Callback<TableView<T>, TableRow<T>> {
        @Override
        public TableRow<T> call(TableView<T> tableView) {
            return new TableRow<>() {
                @Override
                protected void updateItem(T paramT, boolean b) {
                    super.updateItem(paramT, b);
                }
            };
        }
    }

    static String makeAPICall(String url) throws IOException, URISyntaxException {
        var response_content = "";

        var query = new URIBuilder(url);

        var client = HttpClients.createDefault();
        var request = new HttpGet(query.build());

        var response = client.execute(request);

        try {
            System.out.println(response.getStatusLine());
            var entity = response.getEntity();
            response_content = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
        } finally {

        }
        return response_content;
    }

    ChangeListener<Duration> currentTimeListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Duration> observableValue, Duration duration, Duration t) {

            if (mediaPlayer == null) {
                return;
            }

            if(sliderDuration.getValue() >=  mediaPlayer.getTotalDuration().toSeconds() - 1)
                rewindClick(1);

            sliderDuration.setValue(mediaPlayer.getCurrentTime().toSeconds());
            changeCurrentTime();
        }
    };

    ChangeListener<Song> tableChangeListener = (val, oldSong, newSong) -> {
        if (newSong != null) {
            showSongText.setVisible(true);
            setCurrentSong(newSong);
            index = newSong.getIndex() - 1;
            songName.setText(newSong.getName());
            time.setText(String.valueOf(newSong.getTime()));
        }
    };

    InvalidationListener durationListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable observable) {
            if (sliderDuration.isPressed() && mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(sliderDuration.getValue()));
            }
        }
    };

    InvalidationListener volumeListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable observable) {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(sliderVolume.getValue() / 100);
            }
        }
    };

    void rewindClick(int direction) {
        if (playlist.getItems().isEmpty() && this.mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
            index = -1;
            time.setText("mm:ss");
            songName.setText("name");
            sliderDuration.setValue(0);
            this.setImageOnButton(play, "play.png", 35);
        }

        if (index == -1) {
            return;
        }

        index = (index + direction) % playlist.getItems().size();
        if (index < 0) {
            index = playlist.getItems().size() - 1;
        }

        playlist.getSelectionModel().select(index);
        playlist.getFocusModel().focus(index);
    }

    void setCurrentSong(Song song) {
        if (song == null) {
            return;
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }


        mediaPlayer = song.getMediaPlayer();
        mediaPlayer.play();
        this.setImageOnButton(play, "pause.png", 20);
        sliderDuration.setMin(0);
        sliderDuration.setMax(mediaPlayer.getMedia().getDuration().toSeconds());
        sliderDuration.setValue(0);
        songName.setText(song.getName());
        mediaPlayer.setVolume(sliderVolume.getValue() / 100);

        mediaPlayer.currentTimeProperty().addListener(currentTimeListener);
        playlist.setRowFactory(new StyleRowFactory<>());
        playlist.refresh();
    }

    void changeCurrentTime() {
        int sec = ((int) mediaPlayer.getCurrentTime().toSeconds()) % 60;
        time.setText((int) mediaPlayer.getCurrentTime().toMinutes() + (sec < 10 ? ":0" : ":") + sec);
    }

    void playButtonClick() {
        if (mediaPlayer == null) {
            return;
        }

        var status = mediaPlayer.getStatus();
        if (status == MediaPlayer.Status.PAUSED) {
            mediaPlayer.play();
            this.setImageOnButton(play, "pause.png", 20);
        } else {
            mediaPlayer.pause();
            this.setImageOnButton(play, "play.png", 35);
        }
    }

    void mediaSelectClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(null);

        if (files == null) {
            return;
        }

        for (File file : files) {
            Song song = new Song(playlist.getItems().size() + 1, file);
            song.getMediaPlayer().setOnReady(() -> {
                song.setMinutes(song.getMediaPlayer().getTotalDuration().toSeconds());
                song.setSeconds(song.getMediaPlayer().getTotalDuration().toSeconds());
                song.setTime();
                refreshTime(song);
            });
            playlist.getItems().add(song);
        }

    }

    private void refreshTime(Song song){
        playlist.getItems().get(song.getIndex() - 1).setTime();
        playlist.refresh();
    }

    private void setImageOnButton(JFXButton node, String icon, int fitHeight) {
        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("icons/" + icon)));
        imageView.setFitHeight(fitHeight);
        imageView.setPreserveRatio(true);
        node.setGraphic(imageView);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("layout.fxml"));

        primaryStage.setTitle("Player");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 950, 650));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}