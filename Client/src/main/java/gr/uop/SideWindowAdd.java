package gr.uop;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;

public class SideWindowAdd extends SideWindow{
    private boolean flag;

    /**
     * opens option to add song to the engine
     * @param main the BorderPane currently in use
     * @param port the port to communicate with the server
     */
    public SideWindowAdd(BorderPane main, int port){
        super();
        Node previous = main.getCenter();
        main.setCenter(this);
        GridPane input = new GridPane();
        input.setHgap(10);
        input.setVgap(5);

        Map<String,TextField> labelFieldComb = new HashMap<>();
        labelFieldComb.put("Τίτλος τραγουδιού:", new TextField());
        labelFieldComb.put("Όνομα καλλιτέχνη/συγκροτήματος:", new TextField());
        labelFieldComb.put("url των στίχων του τραγουδιού(azlyrics.com):", new TextField());

        disableOK(true);

        Set<Entry<String,TextField>> entries = labelFieldComb.entrySet();
        Set<String> inputLabels = labelFieldComb.keySet();
        int i = 0;
        for(String l:inputLabels){
            Label toadd = new Label(l); 
            input.add(toadd, 0, i);//obj, c, r
            toadd.setFont(new Font(FONT_SIZE));
            TextField inputField = labelFieldComb.get(l);
            input.add(inputField, 1, i);
            GridPane.setHgrow(inputField, Priority.ALWAYS);
            inputField.setFont(new Font(FONT_SIZE));
            inputField.textProperty().addListener((obs, oldV, newV)->{
                if(!newV.isEmpty()){
                    setGlobal(true);//suppose every text field has text
                    entries.forEach(e -> {if(e.getValue().getText().isEmpty()){setGlobal(false);}});
                    disableOK(!flag);
                }else{
                    disableOK(true);
                }
            });
            i+=1;
        }
        getChildren().add(input);
        addButtonListToWindow();
        setAlignment(Pos.TOP_CENTER);

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        ButtonType yesType = new ButtonType("Ναί");
        ButtonType noType = new ButtonType("Όχι");
        confirm.getDialogPane().getButtonTypes().setAll(yesType, noType);

        setOKfunctionality((e)->{
            confirm.setHeaderText("Πρόκειται να προσθέσεις το τραγούδι στη συλλογή.");
                confirm.setContentText("Συνέχεια;");
                Optional<ButtonType> res = confirm.showAndWait();
                if(res.isPresent() && res.get() == yesType){
                    try(Socket clientSocket = new Socket("localhost", port);
                        ObjectOutputStream toServer = new ObjectOutputStream(clientSocket.getOutputStream());
                        ObjectInputStream fromServer = new ObjectInputStream(clientSocket.getInputStream())){
                        SongInfo toAdd = new SongInfo(labelFieldComb.get("Τίτλος τραγουδιού:").getText(), labelFieldComb.get("Όνομα καλλιτέχνη/συγκροτήματος:").getText(), labelFieldComb.get("url των στίχων του τραγουδιού(azlyrics.com):").getText());
                        toServer.writeObject(toAdd);
                        String response = (String)fromServer.readObject();
                        switch(response){
                            case "OK":{
                                Alert info = new Alert(AlertType.INFORMATION);
                                info.setHeaderText("Η εισαγωγή έγινε με επιτυχία");
                                info.show();
                                break;
                            }
                            default:{
                                Alert error = new Alert(AlertType.ERROR);
                                error.setHeaderText("Η εισαγωγή απέτυχε");
                                error.setContentText(response);
                                error.show();
                            }
                        }
                    }catch(IOException | ClassNotFoundException e1){}
                    main.setCenter(previous);
                } 
        });
        setCANCELfunctionality((l)->{
            setGlobal(true);//suppose every text field is empty
            entries.forEach(e -> {if(!e.getValue().getText().isEmpty()){setGlobal(false);}});
            if(!flag){//confirm dialog
                confirm.setHeaderText("Ακύρωση εισαγωγής;");
                Optional<ButtonType> res = confirm.showAndWait();
                if(res.isPresent() && res.get() == yesType){//close side window
                    main.setCenter(previous);
                }
            }else{
                main.setCenter(previous);
            }
        });
    }

    private void setGlobal(boolean f){
        flag = f;
    }
}
