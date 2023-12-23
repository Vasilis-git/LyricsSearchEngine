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
    public SongInfo toSongInfo() {
        String songName, singerName, songHref;
        songName = title;
        singerName = content.substring(0, content.indexOf(", "));
        songHref = content.substring(singerName.length()+2);
        return new SongInfo(songName, singerName, songHref);
    }

    @Override
    public String toString(){
        return title+": "+content;
    }
}
