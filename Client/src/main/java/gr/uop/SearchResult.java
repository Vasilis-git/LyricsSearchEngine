package gr.uop;

import java.io.Serializable;

public class SearchResult implements Serializable{
    private String title, content;

    public SearchResult(){}
    public SearchResult(String title, String content){
        this.title = title;
        this.content = content;
    }

    public String getTitle(){
        return title;
    }
    public String getContent(){
        return content;
    }
}
