package gr.uop;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
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
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

public class SearchEngineWindow extends BorderPane{
    private final TextField searchField;
    private final Button searchButton;
    private final ScrollPane resultsArea;
    private VBox resultsAreaContent;
    private SideWindowSettings settingsWindow;

    public SearchEngineWindow(){
        super();
        searchField = new TextField();
        searchField.setPromptText("Αναζήτηση στίχων, καλλιτεχνών...");
        searchField.setMaxWidth(Double.MAX_VALUE);
        searchField.setFont(new Font(32));
        searchButton = new Button();
        ImageView searchImage = new ImageView(new Image(getClass().getResourceAsStream("images/icons8-search-48.png")));
        searchButton.setGraphic(searchImage);
        searchButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        resultsArea = new ScrollPane();
        resultsArea.setStyle("-fx-background-color:transparent;");//hide borders
        resultsArea.setFitToWidth(true);//always fit content to ScrollPane's width
        resultsAreaContent = new VBox(5);
        resultsAreaContent.setPadding(new Insets(0, 5, 0, 0));//space between scoll bar and content
        resultsArea.setContent(resultsAreaContent);
        resultsArea.setPadding(new Insets(10, 0, 0,0));

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

        settingsWindow = new SideWindowSettings(this.getTop(), this);


        /***functionality***/
        //load port constants
        Path filePath = Paths.get("shared.txt");
        final int QUERY_PORT, DATA_INPUT_PORT, DATA_DEL_PORT, DATA_DEL_PORT_2;
        int qp = 0,dip = 0, ddp = 0, ddp2 = 0;
        try (Scanner port_constants = new Scanner(filePath)) {
            qp = Integer.parseInt(port_constants.next());
            dip = Integer.parseInt(port_constants.next());
            ddp = Integer.parseInt(port_constants.next());
            ddp2 = Integer.parseInt(port_constants.next());
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        QUERY_PORT = qp;
        DATA_INPUT_PORT = dip;
        DATA_DEL_PORT = ddp;
        DATA_DEL_PORT_2 = ddp2;

        add.setOnAction((e)->{ new SideWindowAdd(this, DATA_INPUT_PORT); });
        delete.setOnAction((e)->{ 
            try(Socket clientSocket = new Socket("localhost", DATA_DEL_PORT)) {
                new SideWindowDelete(this, clientSocket, DATA_DEL_PORT_2);
            } catch (IOException e1) {
                e1.printStackTrace();
            } 
        });
        settings.setOnAction((e)->{ this.setTop(settingsWindow); });

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
        
        searchField.textProperty().addListener((obs, oldV, newV)->{
            //remove all previous results whenever the field changes
            resultsAreaContent.getChildren().removeAll(resultsAreaContent.getChildren());
        });
        /*******************/
    }
    private void handleSearch(int port){
        resultsAreaContent.getChildren().removeAll(resultsAreaContent.getChildren());//remove everything from previous search
        try (Socket clientSocket = new Socket("localhost", port);
             PrintWriter toServer = new PrintWriter(clientSocket.getOutputStream(), true);
			 ObjectInputStream fromServer = new ObjectInputStream(clientSocket.getInputStream())) {
            
            if(searchField.getText() != null && searchField.getText().isBlank() == false){
                String toSend = searchField.getText();
                toServer.println(toSend);
                toServer.println(SideWindowSettings.getNumOfResultsToShow());
                toServer.println(SideWindowSettings.getSearchField());
                ArrayList<SearchResult> results = new ArrayList<>();
                SearchResult s = (SearchResult)fromServer.readObject();
				while(s != null){
                    results.add(s);
                    s = (SearchResult)fromServer.readObject();
				}
                for(SearchResult si :results){
                    TitledPane p = createResultPane(si, toSend);
                    resultsAreaContent.getChildren().add(p);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private TitledPane createResultPane(SearchResult si, String toSend) {
        /*styling*/
        Text contentText = new Text(si.getContent());
        contentText.setFont(new Font(17));

        /*TextFlow TextParts = new TextFlow();
        String contentString = contentText.getText();
        StringTokenizer tokenizer = new StringTokenizer(contentString, " ");
        while(tokenizer.hasMoreTokens()){
            String toCheck = tokenizer.nextToken();
            StringTokenizer tokenizer2 = new StringTokenizer(toSend, " ");
            while(tokenizer2.hasMoreTokens()){//make all occurences bold
                String fromSearchField = tokenizer2.nextToken();
                Text part = new Text();
                part.setStyle("-fx-font-weight: bold");
                part.setFont(new Font(17));
                if(toCheck.equalsIgnoreCase(fromSearchField)){
                    part.setText(toCheck+" ");
                    TextParts.getChildren().add(part);
                    break;
                }else if(toCheck.contains(fromSearchField)){
                    int indexOfSearch = toCheck.indexOf(fromSearchField);
                    Text previous = null;
                    if(indexOfSearch != 0){previous = new Text(toCheck.substring(0, indexOfSearch));} 
                    part.setText(toCheck.substring(indexOfSearch, indexOfSearch+fromSearchField.length()));
                    Text next = new Text(toCheck.substring(indexOfSearch+fromSearchField.length(), toCheck.length()));
                    next.setText(next.getText()+" ");
                    if(previous != null){ previous.setFont(new Font(17));}
                    next.setFont(new Font(17));
                    if(previous != null){ TextParts.getChildren().addAll(previous, part, next);
                    }else{TextParts.getChildren().addAll(part, next);}
                    break;
                }
            }
        }*/
    
        TextFlow TextParts = new TextFlow(contentText);
        TextParts.setPadding(new Insets(2, 0, 5, 15));
        TitledPane p = new TitledPane(si.getTitle(), TextParts);
        TextParts.setTextAlignment(TextAlignment.LEFT);
        p.setTextFill(Paint.valueOf(Color.BLUE.toString()));//title only
        p.setUnderline(true);//title only
        p.setCollapsible(true);
        p.setFont(new Font(20));
        /*********/
        return p;
    }
}
