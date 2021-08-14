import com.github.zkingboos.youtubedl.YoutubeDL;
import com.github.zkingboos.youtubedl.YoutubeRequest;
import com.github.zkingboos.youtubedl.exception.YoutubeDLException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;


public class Controller implements Initializable {


    @FXML
    private Button btnSkipPrevious,btnSkipNext,btnRandom,btnReplay;
    @FXML
    private ProgressBar prgrsSong;
    @FXML
    private ToggleButton tglStartStop;
    @FXML
    private Slider sldrVolume;
    @FXML
    private Label lblSong;
    @FXML
    private ListView lvSongs;
    @FXML
    private TextField tfSong;

    private Timer timer;

    private TimerTask task;

    private boolean playing;

    private int number;

    private double x,y,current,end;

    private MediaPlayer mediaPlayer;

    private Media media;

    private ObservableList<String> musicList;

    private File[] musicFiles;

    private ArrayList<File> musics;

    private File downloads;

    private Alert alert;

    private Random random;


    @Override
    public void initialize(URL location, ResourceBundle resources) {


        lvSongs.setStyle("-fx-background-color:  rgb(56,58,89);" + "-fx-text-fill: #0091ff;");

        downloads = new File(System.getProperty("user.home")+"/MusicPlayerDownloads");

        alert=new Alert(Alert.AlertType.INFORMATION);
        DialogPane dialogPane=alert.getDialogPane();
        dialogPane.setMinHeight(Region.USE_PREF_SIZE);
        dialogPane.getStylesheets().add(getClass().getResource("musicPlayerUIStyle.css").toExternalForm());
        dialogPane.getStyleClass().add("alert");


        if(downloads.isDirectory() && downloads.listFiles().length!=0) {

            musicList = FXCollections.observableArrayList((Arrays.asList(downloads.list())));
            lvSongs.setItems(musicList);

            lvSongs.getSelectionModel().selectFirst();
            lblSong.setText(lvSongs.getSelectionModel().getSelectedItem().toString());

            musics = new ArrayList<File>();
            musicFiles = downloads.listFiles();

            if (musicFiles != null) {

                for (File file : musicFiles) {
                    musics.add(file);
                }
            }

            number = lvSongs.getSelectionModel().getSelectedIndex();
            media = new Media(musics.get(number).toURI().toString());
            mediaPlayer = new MediaPlayer(media);


            lvSongs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

                mediaPlayer.stop();
                lblSong.setText((String) newValue);

                number = lvSongs.getSelectionModel().getSelectedIndex();
                media = new Media(musics.get(number).toURI().toString());
                mediaPlayer = new MediaPlayer(media);

                tglStartStop.setSelected(true);
                tmrBegin();
                mediaPlayer.setVolume(sldrVolume.getValue() * 0.01);
                mediaPlayer.play();

            });


            sldrVolume.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                    mediaPlayer.setVolume(sldrVolume.getValue() * 0.01);

                }
            });
        }
        else{

            downloads.delete();

        }

    }

    public void downloadBtnClicked(ActionEvent actionEvent) throws YoutubeDLException, IOException {


        if(!tfSong.getText().isEmpty()) {

            if (!downloads.isDirectory()) {

                Boolean folderCheck = downloads.mkdirs();
                String result = (folderCheck == true) ? "Downloads folder created successfully" : "Error:Couldn't create downloads folder";
                System.out.println(result);

            }

            YoutubeDL.setExecutablePath("cmd.exe", "/c", "youtube-dl");
            String videoUrl = "ytsearch1:" + tfSong.getText();
            YoutubeRequest request = YoutubeDL.search(videoUrl, downloads.getPath()).extractAudio().audioFormat("mp3").output("%(title)s.%(ext)s");
            request.build();

            if(mediaPlayer !=null){
                mediaPlayer.stop();
            }


            Parent tableViewParent = FXMLLoader.load(getClass().getResource("musicPlayer.fxml"));
            Scene tableViewScene = new Scene(tableViewParent);
            tableViewScene.getStylesheets().add(getClass().getResource("musicPlayerUIStyle.css").toExternalForm());
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(tableViewScene);
            stage.show();

        }
        else{

            alert.setHeaderText(alert.getTitle());
            alert.setContentText("Please write artist and song name");
            alert.showAndWait();
        }

    }



    public void titleBarDragged(MouseEvent mouseEvent){

        Stage stage=(Stage)((Node) mouseEvent.getSource()).getScene().getWindow();
        stage.setX(mouseEvent.getScreenX()-x);
        stage.setY(mouseEvent.getScreenY()-y);

    }
    public void  titleBarPressed(MouseEvent mouseEvent){

        x=mouseEvent.getSceneX();
        y=mouseEvent.getSceneY();

    }
    public void closeButtonClicked(MouseEvent mouseEvent) throws IOException {

        if(mediaPlayer!=null) {
            mediaPlayer.dispose();
        }

        Platform.exit();
        System.exit(0);

    }

    public void tglStartStopClicked(ActionEvent actionEvent){

      if(!lvSongs.getItems().isEmpty()) {

          if (tglStartStop.isSelected()) {
              tmrBegin();
              mediaPlayer.setVolume(sldrVolume.getValue() * 0.01);
              mediaPlayer.play();
          } else {
              tmrCancel();
              mediaPlayer.pause();

          }
      }
      else{

          tglStartStop.setSelected(false);

          alert.setContentText("There isn't any music to play");
          alert.showAndWait();

      }

    }

    public void btnReplayClicked(ActionEvent actionEvent){

        if(!lvSongs.getItems().isEmpty()) {

            prgrsSong.setProgress(0);
            mediaPlayer.seek(Duration.seconds(0));
            mediaPlayer.play();
            tglStartStop.setSelected(true);
        }
    }

    public void btnSkipNextClicked(ActionEvent actionEvent){


        if(!lvSongs.getItems().isEmpty() && lvSongs.getItems().size()!=1) {

            mediaPlayer.stop();

            if (number < musics.size() - 1) {
                number++;
                lvSongs.getSelectionModel().selectNext();
            } else {
                number = 0;
                lvSongs.getSelectionModel().selectFirst();
            }


            if(playing){
                tmrCancel();
            }

            mediaPlayer.stop();
            lblSong.setText(musicFiles[number].getName());

            media = new Media(musics.get(number).toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            tglStartStop.setSelected(true);
            mediaPlayer.setVolume(sldrVolume.getValue() * 0.01);
            mediaPlayer.play();

        }
        else if(lvSongs.getItems().size()==1){

            prgrsSong.setProgress(0);
            mediaPlayer.seek(Duration.seconds(0));
            mediaPlayer.play();
            tglStartStop.setSelected(true);
        }

    }

    public void btnSkipPreviousClicked(ActionEvent actionEvent){

        if(!lvSongs.getItems().isEmpty() && lvSongs.getItems().size()!=1) {

            mediaPlayer.stop();

            if (number > 0) {
                number--;
                lvSongs.getSelectionModel().selectPrevious();
            } else {
                number = musics.size() - 1;
                lvSongs.getSelectionModel().selectLast();
            }


            if(playing){
                tmrCancel();
            }


            lblSong.setText(musicFiles[number].getName());

            media = new Media(musics.get(number).toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setVolume(sldrVolume.getValue() * 0.01);
            tglStartStop.setSelected(true);
            mediaPlayer.play();
        }
        else if(lvSongs.getItems().size()==1){

            prgrsSong.setProgress(0);
            mediaPlayer.seek(Duration.seconds(0));
            mediaPlayer.play();
            tglStartStop.setSelected(true);
        }

    }

    public void btnRandomClicked(ActionEvent actionEvent){

        if(!lvSongs.getItems().isEmpty() && lvSongs.getItems().size()!=1) {

            mediaPlayer.stop();

            random=new Random();
            number=random.nextInt(musics.size());
            lvSongs.getSelectionModel().select(number);


            if(playing){
                tmrCancel();
            }

            lblSong.setText(musicFiles[number].getName());

            media = new Media(musics.get(number).toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setVolume(sldrVolume.getValue() * 0.01);
            tglStartStop.setSelected(true);
            mediaPlayer.play();
        }
        else if(lvSongs.getItems().size()==1){

            prgrsSong.setProgress(0);
            mediaPlayer.seek(Duration.seconds(0));
            mediaPlayer.play();
            tglStartStop.setSelected(true);
        }

    }

    public void btnInfoClicked(ActionEvent actionEvent){

        alert.setHeaderText("Path Info");
        alert.setContentText("Location of downloaded songs:  "+downloads.getPath());
        alert.showAndWait();

    }

    public void tmrBegin(){

        timer=new Timer();
        task=new TimerTask() {
            @Override
            public void run() {

                playing=true;
                current=mediaPlayer.getCurrentTime().toSeconds();
                end=media.getDuration().toSeconds();
                prgrsSong.setProgress(current/end);

                if(current/end==1){

                    tmrCancel();

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {

                            if(number<musics.size()-1){
                                number++;
                                lvSongs.getSelectionModel().selectNext();
                            }
                            else {
                                number=0;
                                lvSongs.getSelectionModel().selectFirst();
                            }


                            lblSong.setText(musicFiles[number].getName());

                            media=new Media(musics.get(number).toURI().toString());
                            mediaPlayer=new MediaPlayer(media);

                            tglStartStop.setSelected(true);
                            mediaPlayer.setVolume(sldrVolume.getValue()*0.01);
                            mediaPlayer.play();

                            tmrBegin();

                        }


                    });

                }
            }
        };

        timer.scheduleAtFixedRate(task,0,1000);

    }

    public void tmrCancel(){

        playing=false;
        timer.cancel();

    }

}
