package gr.uop;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Callback;

public class SideWindowDelete extends SideWindow
{
    private TableView<SongInfo> table;
    private ObservableList<SongInfo> data = FXCollections.observableArrayList();
    private TableColumn<SongInfo, String> songName, singerName, songHref;
    private Button cancelChoice=new Button("Ακύρωση επιλογής");
    private Socket clientSocket; 
    ObservableList<SongInfo> selected = null;
    private TextField search = new TextField();
    private int port2;
    
    /**
     * creates a new window to select and delete songs
     * @param main the BorderPane Node in use
     * @param port the port to communicate with the server
     * @throws IOException
     * @throws UnknownHostException
     */
    public SideWindowDelete(BorderPane main, Socket clientSocket, int port2) throws UnknownHostException, IOException
    {
        Node previous = main.getCenter();//change to this on close
        table = new TableView<>();
        this.port2 = port2;
        this.clientSocket = clientSocket;
        //Columns: songName singerName songHref
        //User will select one or more lines then have options to 'Remove chosen' 'Cancel choice'
        //the window must also have a 'Cancel' button that resets everything and closes this window 
        songName = new TableColumn<>("Όνομα τραγουδιού");
        singerName = new TableColumn<>("Όνομα καλλιτέχνη");
        songHref = new TableColumn<>("link");


        /****Look****/ 
        main.setCenter(this);
        search.setFont(new Font(FONT_SIZE));
        search.setMaxWidth(Double.MAX_VALUE);
        search.setPromptText("Αναζήτηση ονόματος τραγουδιού...");
        Node settings = main.getTop();
        main.setTop(search);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setStyle("-fx-font-size: "+FONT_SIZE+"px;");
        songName.setStyle("-fx-alignment: CENTER");
        singerName.setStyle("-fx-alignment: CENTER");
        songHref.setStyle("-fx-alignment: CENTER");
        songName.setMinWidth(songName.getText().length());
        songHref.setMinWidth(songHref.getText().length());
        singerName.setMinWidth(singerName.getText().length());
        setOKbuttonText("Αφαίρεση επιλεγμένων");
        setCANCELbuttonText("Kλείσιμο");
        disableOK(true);
        cancelChoice.setDisable(true);
        cancelChoice.setFont(new Font(FONT_SIZE));
        addToButtonList(cancelChoice);
        getChildren().add(table);
        addButtonListToWindow();
        table.getColumns().add(songName);
        table.getColumns().add(singerName);
        table.getColumns().add(songHref); 
        /****Look****/
              
        
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);//make columns use all available width
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);//allow multiple selections

        /****Functionality****/ 
        songName.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SongInfo,String>,ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<SongInfo, String> param) {
                return new ReadOnlyObjectWrapper<String>(param.getValue().getSongTitle());
            }   
        });
        singerName.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SongInfo,String>,ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<SongInfo, String> param) {
                return new ReadOnlyObjectWrapper<String>(param.getValue().getNameOfArtist());
            }     
        });
        songHref.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SongInfo,String>,ObservableValue<String>>(){
            @Override
            public ObservableValue<String> call(CellDataFeatures<SongInfo, String> param) {
                return new ReadOnlyObjectWrapper<String>(param.getValue().getlyricslink());
            }     
        });       
        table.setItems(data);
        search.textProperty().addListener((obs, oldV, newV)->{
            table.getItems().stream()
                            .filter(item -> item.getSongTitle().toLowerCase().contains(newV.toLowerCase()))
                            .findFirst()
                            .ifPresent(item -> {
                                table.getSelectionModel().clearSelection();
                                table.getSelectionModel().select(item);
                                table.scrollTo(item);
                            });
        });
        search.setOnKeyPressed(new EventHandler<KeyEvent>(){
            @Override
            public void handle(KeyEvent event) {
                table.getItems().stream()
                                .filter(item -> item.getSongTitle().toLowerCase().equalsIgnoreCase(search.getText().toLowerCase()))
                                .findFirst()
                                .ifPresent(item -> {
                                    table.getSelectionModel().clearSelection();
                                    table.getSelectionModel().select(item);
                                    table.scrollTo(item);
                                });
            }    
        });
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV)->{
            Platform.runLater(()->{
                selected = table.getSelectionModel().getSelectedItems();
                if(!table.getSelectionModel().getSelectedItems().isEmpty()){
                    this.disableOK(false);
                    this.cancelChoice.setDisable(false);
                }else{
                    this.disableOK(true);
                    this.cancelChoice.setDisable(true);
                }
            });
        });
        setOKfunctionality((e)->{
            try(Socket newCliSocket = new Socket("localhost", port2);
                ObjectOutputStream toServer = new ObjectOutputStream(newCliSocket.getOutputStream())){
                    toServer.writeObject("OK");
                for(SongInfo s: selected){
                    toServer.writeObject(s);
                }toServer.writeObject(null);//indicate end of objects
                Platform.runLater(()->{
                    data.removeAll(selected);
                    table.getSelectionModel().clearSelection();
                    selected = null;
                });
            }catch(IOException l){
                l.printStackTrace();
            }
        });
        setCANCELfunctionality((e)->{
            confirmClose(main, previous, settings);
        });
        cancelChoice.setOnAction((e)->{
            if(!table.getSelectionModel().getSelectedItems().isEmpty()){
                selected = null;
                Platform.runLater(()->{
                    table.getSelectionModel().clearSelection();
                });
            }
        });
        /****Functionality****/
        getServerData();
    }

    public void confirmClose(BorderPane main, Node previous, Node settings){
        try {
            Platform.runLater(()->{
                if(!table.getSelectionModel().getSelectedItems().isEmpty()){
                    Alert confirm = new Alert(AlertType.CONFIRMATION);
                    confirm.setHeaderText("Επιβεβαίωση επιλογής");
                    confirm.setContentText("Έχεις επιλεγμένα στοιχεία. Σίγουρα θες να αποχωρήσεις; Δεν θα γίνει καμία αλλαγή στα δεδομένα.");
                    Optional<ButtonType> response = confirm.showAndWait();
                    if(response.get() == ButtonType.OK){
                        main.setCenter(previous);
                        main.setTop(settings);
                        connectAndExit();
                    }
                }else{ main.setCenter(previous); main.setTop(settings); connectAndExit();}
            });
        } catch (Exception e1) { e1.printStackTrace(); } 
    }

    private void connectAndExit() {
        try(Socket newCliSocket = new Socket("localhost", port2);
            ObjectOutputStream toServer = new ObjectOutputStream(newCliSocket.getOutputStream())){
            toServer.writeObject("EXIT");
        }catch(IOException e){ e.printStackTrace(); }
    }

    private void getServerData() {
        try(ObjectInputStream fromServer = new ObjectInputStream(clientSocket.getInputStream())){//socket will automatically close here
            //read all data from server
            
            do{
                final SongInfo si = (SongInfo)fromServer.readObject();
                if(si == null){break;}
                Platform.runLater(()->{data.add(si);});
            }while(true);
            
        }catch(ClassNotFoundException | IOException e){
            e.printStackTrace();
        }
    }
}
