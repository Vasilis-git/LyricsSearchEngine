package gr.uop;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;

public class SideWindowSettings extends SideWindow{
    private static int numberOfResults = 0;//0 means all there are
    private static TextField number;

    public SideWindowSettings(Node previous, BorderPane main){
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
        number.setMaxWidth(40);
        prompt.setFont(new Font(FONT_SIZE));
        promptContinued.setFont(new Font(FONT_SIZE));
        number.setFont(new Font(FONT_SIZE));
        number.setText(""+numberOfResults);
        number.setAlignment(Pos.TOP_CENTER);
        input.add(prompt, 0, 1);
        input.add(number, 1, 1);
        input.add(promptContinued, 2, 1);
        getChildren().add(input);
        addButtonListToWindow();
        setSeparatorToBottom();

        //first time setup
        number.setText(null);
        number.setPromptText("all");

        number.textProperty().addListener((obs,oldV, newV)->{
            if(number.getText().equalsIgnoreCase("0")){
                number.setText("");
                number.setPromptText("all");
            }
        });

        number.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().equals(KeyCode.ENTER)){
                    handleNumberInput(previous, main);
                }
            }
        });
        setOKfunctionality((e)->{
            handleNumberInput(previous, main);
        });
        setCANCELfunctionality((e)->{
            number.setText(""+numberOfResults);
            main.setTop(previous);
        });
    }
    private void handleNumberInput(Node previous, BorderPane main) {
        String text = number.getText();
        String textPrompt = number.getPromptText();
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
                if(n > 0){info.setContentText("Θα βλέπεις μόνο τα "+n+"top αποτελέσματα.");}
                else{info.setContentText("Θα βλέπεις όλα τα αποτελέσματα.");}
            }
        }catch(NumberFormatException nfe){
            if(!text.equalsIgnoreCase("all") || (!textPrompt.equalsIgnoreCase("all") && (text.isEmpty() || text.equalsIgnoreCase("all")))){
                Alert NaN = new Alert(AlertType.ERROR);
                NaN.setContentText("Δεν δόθηκε αριθμός.");
                number.setText(""+numberOfResults);
                NaN.showAndWait();
            }
        }
        main.setTop(previous);
    }
    /**
     * add more actiton when the TextField changes content
     * @param listener the listener to add
     */
    public static void addAction(ChangeListener<? super String> listener){
        number.textProperty().addListener(listener);
    }

    public static int getNumOfResultsToShow(){
        return numberOfResults;
    }
}
