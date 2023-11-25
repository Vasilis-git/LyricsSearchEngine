package gr.uop;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public abstract class SideWindow extends VBox{
    private HBox buttonList;
    private Button OK, CANCEL;
    public final static int FONT_SIZE = 14;

    public SideWindow(){
        buttonList = new HBox(5);
        setPadding(new Insets(7));
        OK = new Button("OK");
        OK.setFont(new Font(FONT_SIZE));
        CANCEL = new Button("CANCEL");
        CANCEL.setFont(new Font(FONT_SIZE));
        buttonList.getChildren().addAll(OK, CANCEL);
    }
    public void setOKbuttonText(String t){
        OK.setText(t);
    }
    public void setCANCELbuttonText(String t){
        CANCEL.setText(t);
    }
    public void addToButtonList(Button b){
        buttonList.getChildren().add(b);
    }
    public void addButtonListToWindow(){
        VBox bottom = new VBox();
        bottom.setPadding(new Insets(10, 0, 0, 0));
        Separator line = new Separator();
        bottom.getChildren().addAll(line, buttonList);
        this.getChildren().add(bottom);
        buttonList.setAlignment(Pos.BASELINE_CENTER);
    }
    /**
     * disable OK button
     * @param f true to disable, false to enable
     */
    public void disableOK(boolean f){
        OK.setDisable(f);
    }
    public void setOKfunctionality(EventHandler<ActionEvent> e){
        OK.setOnAction(e);
    }
    public void setCANCELfunctionality(EventHandler<ActionEvent> e){
        CANCEL.setOnAction(e);
    }
}
