package gr.uop;

import java.util.Optional;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;

public class SideWindowSettings extends SideWindow{
    private static int numberOfResults = 0;//0 means all there are
    private static TextField number;
    private static String searchField;
    private boolean numberTextChanged = false, FieldChoiceChange = false, firstSelectedField = true;

    public SideWindowSettings(Node previous, BorderPane main){
        /***Look***/
        super();
        GridPane input = new GridPane();
        input.setHgap(10);
        input.setVgap(5);
        input.setAlignment(Pos.TOP_CENTER);

        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(5));
        setSpacing(5);

        Label prompt = new Label("Show");
        Label promptContinued = new Label("top results");
        number = new TextField();
        number.maxWidth(3);
        prompt.setFont(new Font(FONT_SIZE));
        promptContinued.setFont(new Font(FONT_SIZE));
        number.setFont(new Font(FONT_SIZE));
        number.setText(""+numberOfResults);
        number.setAlignment(Pos.TOP_CENTER);
        number.getStyleClass().add("-fx-text-alignment: center;");
        input.add(prompt, 0, 1);
        input.add(number, 1, 1);
        input.add(promptContinued, 2, 1);
        GridPane.setVgrow(number, Priority.ALWAYS);

        Label searchLabel = new Label("Search in:");
        searchLabel.setFont(new Font(FONT_SIZE));
        input.add(searchLabel, 0, 2);
        RadioButton titleButton = new RadioButton("title");//must be same as indexTitle and indexBody in LuceneConstants, server-side
        RadioButton bodyButton = new RadioButton("body");
        titleButton.setFont(new Font(FONT_SIZE));
        bodyButton.setFont(new Font(FONT_SIZE));
        ToggleGroup tg = new ToggleGroup();
        titleButton.setToggleGroup(tg);
        bodyButton.setToggleGroup(tg);

        input.add(titleButton, 1, 2);
        input.add(bodyButton, 1, 3);


        getChildren().add(input);
        addButtonListToWindow();
        setSeparatorToBottom();
        /***Look***/


        /********Functionality********/

        tg.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){

            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if(!firstSelectedField){FieldChoiceChange = true;}
                firstSelectedField = false;
                if(newValue == titleButton){
                    searchField = titleButton.getText();
                }else{
                    searchField = bodyButton.getText();
                }
            }

        });

        //first time setup
        number.setText(null);
        number.setPromptText("all");
        titleButton.setSelected(true);
        searchField = titleButton.getText();

        number.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().equals(KeyCode.ENTER)){
                    handleNumberInput(previous, main);
                }
            }
        });
        
        number.textProperty().addListener((obs, oldV, newV)->{
            numberTextChanged = true;
        });

        setOKfunctionality((e)->{
            Alert confirm =  new Alert(AlertType.CONFIRMATION);
            confirm.setHeaderText("Συνέχεια με τις τρέχων ρυθμίσεις;");
            Optional<ButtonType> choice = confirm.showAndWait();
            if(choice.get() == ButtonType.OK){
                main.setTop(previous);
                if(numberTextChanged){
                    handleNumberInput(previous, main);
                    numberTextChanged = false;
                }
            }    
        });
        setCANCELfunctionality((e)->{
            if(numberTextChanged){
                number.setText(""+numberOfResults);
                if(numberOfResults == 0){
                    number.setText(null);
                    number.setPromptText("all");
                }
                numberTextChanged = false;
            }
            main.setTop(previous);
            if(FieldChoiceChange){
                if(bodyButton.isSelected() == true){
                    titleButton.setSelected(true);
                }else{
                    bodyButton.setSelected(true);
                }
                FieldChoiceChange = false;
            }
        });
        /********Functionality********/
    }

    private void handleNumberInput(Node previous, BorderPane main) {
        String text = number.getText();
        String prompt = number.getPromptText();
        try{
            int n = Integer.parseInt(text);
            if(n < 0){
                Alert negative = new Alert(AlertType.ERROR);
                negative.setContentText("Μή έγκυρο νούμερο, πρέπει να έιναι θετικό ή 0 (0 για να δείχνει όλα τα αποτελέσματα).");
                negative.showAndWait();
            }else{
                numberOfResults = n;
                Alert info = new Alert(AlertType.INFORMATION);
                info.setHeaderText("Επιτυχής αλλαγή");
                if(n > 0){info.setContentText("Θα βλέπεις μόνο τα "+n+" top αποτελέσματα απο την επόμενη αναζήτηση.");
                number.maxWidth(number.getText().length());}
                else{
                    info.setContentText("Θα βλέπεις όλα τα αποτελέσματα.");
                    number.setText(null);
                    number.setPromptText("all");
                    number.maxWidth(number.getPromptText().length());
                }
                info.showAndWait();
            }
        }catch(NumberFormatException nfe){
            if((prompt.equalsIgnoreCase("all") && text == null) || text.equalsIgnoreCase("all")){
                Alert info = new Alert(AlertType.INFORMATION);
                info.setHeaderText("Επιτυχής αλλαγή");
                info.setContentText("Θα βλέπεις όλα τα αποτελέσματα.");
                info.showAndWait();
                number.setText(null);
                number.maxWidth(number.getPromptText().length());
            }else{
                Alert NaN = new Alert(AlertType.ERROR);
                NaN.setContentText("Δεν δόθηκε αριθμός.");
                number.setText(""+numberOfResults);
                if(numberOfResults == 0){
                    number.setText(null);
                    number.setPromptText("all");
                }
                NaN.showAndWait();
            }
        }
        main.setTop(previous);
    }

    public static int getNumOfResultsToShow(){
        return numberOfResults;
    }

    public static String getSearchField() {
        return searchField;
    }
}
