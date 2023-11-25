package gr.uop;


import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.scene.text.Font;

public class SearchEngineWindow extends BorderPane{
    private final TextField searchField;
    private final Button searchButton;
    private final ScrollPane resultsArea;

    public SearchEngineWindow(){
        super();
        searchField = new TextField();
        searchField.setPromptText("Αναζήτηση στίχων, καλλιτεχνών...");
        searchField.setMaxWidth(Double.MAX_VALUE);
        searchField.setFont(new Font(32));
        searchButton = new Button();
        ImageView searchImage = new ImageView(new Image(getClass().getResourceAsStream("images/icons8-search-48.png")));
        searchButton.setGraphic(searchImage);
        searchButton.setMaxSize(48, 48);

        resultsArea = new ScrollPane();
        resultsArea.setStyle("-fx-background-color:transparent;");//hide borders
        resultsArea.setFitToWidth(true);//always fit content to ScrollPane's width
        VBox resultsAreaContent = new VBox(5);
        resultsAreaContent.setPadding(new Insets(0, 5, 0, 0));//space between scoll bar and content
        resultsArea.setContent(resultsAreaContent);
        resultsArea.setPadding(new Insets(10, 0, 0,0));


        /*results example*/
        int resultCount = 100;
        int showCount;
        TitledPane results[] = new TitledPane[resultCount];
        if(SideWindowSettings.getNumOfResultsToShow() == 0){
            showCount = resultCount;
        }else{showCount = SideWindowSettings.getNumOfResultsToShow();}
        for (int i = 0; i < showCount; i++) {
            if(resultCount < i){break;}
            Label info = new Label("Result "+i+" info");
            info.setFont(new Font(17));
            results[i] = new TitledPane("Result "+i, info);
            results[i].setMaxWidth(Double.MAX_VALUE);
            results[i].setExpanded(false);
            results[i].setFont(new Font(20));
            resultsAreaContent.getChildren().add(results[i]);
        }
        /****************/

        HBox searchLine = new HBox(5);
        searchLine.getChildren().addAll(searchField, searchButton);
        HBox.setHgrow(searchButton, Priority.NEVER);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchLine.setMaxWidth(Double.MAX_VALUE);


        HBox buttonList = new HBox(5);
        Button add = new Button("Προσθήκη");
        add.setFont(new Font(14));
        add.setMaxWidth(Double.MAX_VALUE);
        Button delete = new Button("Διαγραφή");
        delete.setFont(new Font(14));
        delete.setMaxWidth(Double.MAX_VALUE);
        buttonList.getChildren().addAll(add, delete);
        HBox.setHgrow(delete, Priority.ALWAYS);
        HBox.setHgrow(add, Priority.ALWAYS);


        Button settings = new Button();
        settings.setMaxSize(32, 32);
        ImageView settingsImage = new ImageView(new Image(getClass().getResourceAsStream("images/icons8-settings-32.png")));
        settings.setGraphic(settingsImage);
        Label lFalse = new Label("F.A.L.S.E.");
        Label explanation = new Label("Finally! Another Lyrics Search Engine!");
        lFalse.setFont(new Font(56));
        explanation.setFont(new Font(14));


        VBox contentBox = new VBox(5);
        contentBox.getChildren().add(lFalse);
        contentBox.getChildren().add(explanation);
        contentBox.getChildren().add(searchLine);
        contentBox.getChildren().add(buttonList);
        contentBox.getChildren().add(resultsArea);    
        contentBox.setAlignment(Pos.CENTER);    

        Label imgSrc = new Label("Button icons from icons8.com");
        Separator line = new Separator();
        VBox bottom = new VBox(0);
        bottom.getChildren().addAll(line, imgSrc);
        bottom.setPadding(new Insets(10, 0, 0, 0));
        bottom.setAlignment(Pos.BASELINE_CENTER);

        this.setCenter(contentBox);
        this.setTop(settings);
        this.setBottom(bottom);
        this.setPadding(new Insets(5));


        /***functionality***/
        //load port constants
        Path filePath = Paths.get("/home/vasilis/Έγγραφα/VS Code/LyricsSearchEngine/shared.txt");
        final int QUERY_PORT, DATA_INPUT_PORT, FILE_PORT;
        int qp = 0,dip = 0,fp = 0;
        try (Scanner port_constants = new Scanner(filePath)) {
            qp = Integer.parseInt(port_constants.next());
            dip = Integer.parseInt(port_constants.next());
            fp = Integer.parseInt(port_constants.next());

        } catch (NumberFormatException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(0);
        }
        QUERY_PORT = qp;
        DATA_INPUT_PORT = dip;
        FILE_PORT = fp;
        //if qp, dip and fp don't change, it means an exception was thrown and the program will stop

        add.setOnAction((e)->{
            SideWindowAdd addSong = new SideWindowAdd(this.getCenter(), this, DATA_INPUT_PORT);
            this.setCenter(addSong);
        });
        settings.setOnAction((e)->{
            SideWindowSettings settingsWindow = new SideWindowSettings(this.getTop(), this);
            this.setTop(settingsWindow);
        });

        searchField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                if(e.getCode().equals(KeyCode.ENTER)){//make connection with server and send query
                    handleSearch(QUERY_PORT);
                }
            }
        });
        searchButton.setOnAction((e)->{
            handleSearch(QUERY_PORT);
        });
        /*******************/
    }
    private void handleSearch(int port){
        try (Socket clientSocket = new Socket("localhost", port)) {
            OutputStream outstream = clientSocket.getOutputStream();
            PrintWriter out = new PrintWriter(outstream);
            out.print(searchField.getText());
            out.flush();
            out.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
}
