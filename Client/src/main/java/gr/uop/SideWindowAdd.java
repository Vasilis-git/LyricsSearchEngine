package gr.uop;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.io.ObjectOutputStream;
import java.io.IOException;
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
import javafx.scene.text.Font;

public class SideWindowAdd extends SideWindow{
    private boolean flag;

    public SideWindowAdd(Node previous, BorderPane main, int port){
        super();
        GridPane input = new GridPane();
        input.setHgap(10);
        input.setVgap(5);

        Map<String,TextField> labelFieldComb = new HashMap<>();
        labelFieldComb.put("Τίτλος τραγουδιού:", new TextField());
        labelFieldComb.put("Όνομα καλλιτέχνη/συγκροτήματος:", new TextField());
        labelFieldComb.put("Άλμπουμ:", new TextField());
        labelFieldComb.put("Τύπος άλμπουμ:", new TextField());
        labelFieldComb.put("Ημερομηνία κυκλοφορίας του άλμπουμ:", new TextField());
        labelFieldComb.put("url των στίχων του τραγουδιού(azlyrics.com):", new TextField());

        Button csv = new Button("csv");
        addToButtonList(csv);
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
            inputField.setMaxWidth(Double.MAX_VALUE);
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
                    SongInfo toAdd = new SongInfo(labelFieldComb.get("Τίτλος τραγουδιού:").getText(), labelFieldComb.get("Όνομα καλλιτέχνη/συγκροτήματος:").getText(), labelFieldComb.get("Άλμπουμ:").getText(), labelFieldComb.get("Τύπος άλμπουμ:").getText(), labelFieldComb.get("Ημερομηνία κυκλοφορίας του άλμπουμ:").getText(), labelFieldComb.get("url των στίχων του τραγουδιού(azlyrics.com):").getText());
                    try (Socket clientSocket = new Socket("localhost", port)) {
                        ObjectOutputStream ous = new ObjectOutputStream(clientSocket.getOutputStream());
                        ous.writeObject(toAdd);
                        ous.close();
                    }catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    main.setCenter(previous);
                    Alert info = new Alert(AlertType.INFORMATION);
                    info.setHeaderText("Η εισαγωγή έγινε με επιτυχία");
                    info.show();
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
