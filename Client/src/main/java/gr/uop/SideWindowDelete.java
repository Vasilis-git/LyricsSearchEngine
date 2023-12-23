package gr.uop;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Optional;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

public class SideWindowDelete extends SideWindow
{
    private TableView<SongInfo> table;
    private ObservableList<SongInfo> data = FXCollections.observableArrayList();
    private TableColumn<SongInfo, String> songName, singerName, songHref;
    private Button cancelChoice=new Button("Ακύρωση επιλογής");
    private Socket clientSocket; 
    ObservableList<SongInfo> selected = null;
    
    /**
     * creates a new window to select and delete songs
     * @param main the BorderPane Node in use
     * @param port the port to communicate with the server
     * @throws IOException
     * @throws UnknownHostException
     */
    public SideWindowDelete(BorderPane main, int port) throws UnknownHostException, IOException
    {
        Node previous = main.getCenter();//change to this on close
        main.setCenter(this);
        table = new TableView<>();
        clientSocket = new Socket("localhost", port);
        /****Look****/ 
        table.setStyle("-fx-font-size: "+FONT_SIZE+"px;");
        //Columns: songName singerName songHref
        //User will select one or more lines then have options to 'Remove chosen' 'Cancel choice'
        //the window must also have a 'Cancel' button that resets everything and closes this window 
        songName = new TableColumn<>("Όνομα τραγουδιού");
        singerName = new TableColumn<>("Όνομα καλλιτέχνη");
        songHref = new TableColumn<>("link");

        songName.setStyle("-fx-alignment: CENTER");
        singerName.setStyle("-fx-alignment: CENTER");
        songHref.setStyle("-fx-alignment: CENTER");
        setOKbuttonText("Αφαίρεση επιλεγμένων");
        setCANCELbuttonText("Ακύρωση και κλείσιμο");
        disableOK(true);
        cancelChoice.setDisable(true);
        addToButtonList(cancelChoice);
        addButtonListToWindow();
        getChildren().add(table);
        table.getColumns().add(songName);
        table.getColumns().add(singerName);
        table.getColumns().add(songHref); 
        /****Look****/      
        
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);//do not allow resizing of columns and make them use all available width
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
        setCANCELfunctionality((e)->{
            try {
                clientSocket.close();
                main.setCenter(previous);
            } catch (IOException e1) { e1.printStackTrace(); }
        });
        table.selectionModelProperty().addListener((obs, oldV, newV)->{
            selected = table.getSelectionModel().getSelectedItems();
            this.disableOK(false);
            this.cancelChoice.setDisable(false);
        });
        setOKfunctionality((e)->{
            try(ObjectOutputStream toServer = new ObjectOutputStream(clientSocket.getOutputStream())){
                for(SongInfo s: selected){
                    toServer.writeObject(s);
                }toServer.writeObject(null);//indicate end of objects
                data.removeAll(selected);
                table.getSelectionModel().clearSelection();
                selected = null;
                this.disableOK(true);
                this.cancelChoice.setDisable(true);
            }catch(IOException l){
                l.printStackTrace();
            }
        });
        setCANCELfunctionality((e)->{
            if(selected != null){
                Alert confirm = new Alert(AlertType.CONFIRMATION);
                confirm.setHeaderText("Επιβεβαίωση επιλογής");
                confirm.setContentText("Έχεις επιλεγμένα στοιχεία. Σίγουρα θες να αποχωρήσεις; Δεν θα γίναι καμία αλλαγή στα δεδομένα.");
                Optional<ButtonType> response = confirm.showAndWait();
                if(response.get() == ButtonType.OK){
                    try {
                        clientSocket.close();
                        main.setCenter(previous);
                    } catch (IOException e1) { e1.printStackTrace(); }     
                }
            }
        });
        cancelChoice.setOnAction((e)->{
            if(selected != null){
                selected = null;
                table.getSelectionModel().clearSelection();
            }
        });
        /****Functionality****/
        getServerData();
    }


    private void getServerData() {
        try(ObjectInputStream fromServer = new ObjectInputStream(clientSocket.getInputStream())){
            //read all data from server
            SongInfo si = (SongInfo)fromServer.readObject();
            while(si != null){
                data.add(si);
                si = (SongInfo)fromServer.readObject();
            }
        }catch(ClassNotFoundException | IOException e){
            e.printStackTrace();
        }
    }
}
