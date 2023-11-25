package gr.uop;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;

public class SideWindowSettings extends SideWindow{
    private static int numberOfResults = 0;//0 means all there are

    public SideWindowSettings(Node previous, BorderPane main){
        super();
        GridPane input = new GridPane();
        input.setHgap(10);
        input.setVgap(5);

        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(5));
        setSpacing(5);

        Label prompt = new Label("Show");
        Label promptContinued = new Label("top results");
        TextField number = new TextField();
        number.setMaxWidth(40);
        prompt.setFont(new Font(FONT_SIZE));
        promptContinued.setFont(new Font(FONT_SIZE));
        number.setFont(new Font(FONT_SIZE));
        number.setText(""+numberOfResults);
        input.add(prompt, 0, 1);
        input.add(number, 1, 1);
        input.add(promptContinued, 2, 1);
        getChildren().add(input);
        addButtonListToWindow();

        setOKfunctionality((e)->{
            String text = number.getText();
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
                Alert NaN = new Alert(AlertType.ERROR);
                NaN.setContentText("Δεν δόθηκε αριθμός.");
                NaN.showAndWait();
            }
            main.setTop(previous);
        });
        setCANCELfunctionality((e)->{
            main.setTop(previous);
        });
    }
    public static int getNumOfResultsToShow(){
        return numberOfResults;
    }
}
