package gr.uop;

import javafx.scene.control.Alert;

public class ConnectionError extends Alert {
    
    public ConnectionError(){
        super(AlertType.ERROR);
        setHeaderText("Πρόβλημα σύνδεσης.");
        setContentText("Αδύνατη η σύνδεση στο διακομιστή, δοκιμάστε ξανά αργότερα.");
    }
}
