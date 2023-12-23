package gr.uop;

import java.io.Serializable;

public class SongInfo implements Serializable{//class for adding songs in the set
    private String songTitle, nameOfSingerOrBand, lyricsLink;

    public SongInfo(String songTitle, String nameOfSingerOrBand, String lyricsLink){
        this.songTitle = songTitle;       
        this.nameOfSingerOrBand = nameOfSingerOrBand;   
        this.lyricsLink = lyricsLink;
    }

    public String getSongTitle(){
        return songTitle;
    }
    public String getNameOfArtist(){
        return nameOfSingerOrBand;
    }       
    public String getlyricslink(){
        return lyricsLink;
    }    
    @Override
    public String toString(){
        return songTitle+" "+nameOfSingerOrBand+" "+lyricsLink;
    }

    public SearchResult toSearchResult() {
        return new SearchResult(songTitle, nameOfSingerOrBand+", "+lyricsLink);
    }
}
