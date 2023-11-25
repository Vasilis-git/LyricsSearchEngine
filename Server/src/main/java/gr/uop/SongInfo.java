package gr.uop;

import java.io.Serializable;


public class SongInfo implements Serializable{
    private String songTitle, nameOfSingerOrBand, albumName, albumType, albumReleaseDate, lyricsLink;

    public SongInfo(String songTitle, String nameOfSingerOrBand, String albumName, String albumType, String albumReleaseDate, String lyricsLink){
        this.songTitle = songTitle;       
        this.nameOfSingerOrBand = nameOfSingerOrBand;   
        this.albumName = albumName;   
        this.albumType = albumType;   
        this.albumReleaseDate = albumReleaseDate;   
        this.lyricsLink = lyricsLink;
    }

    public String getSongTitle(){
        return songTitle;
    }
    public String getNameOfArtist(){
        return nameOfSingerOrBand;
    }    
    public String getalbumName(){
        return albumName;
    }
    public String getalbumType(){
        return albumType;
    }    
    public String getalbumReleaseDate(){
        return albumReleaseDate;
    }
    public String getlyricslink(){
        return lyricsLink;
    }    
    @Override
    public String toString(){
        return songTitle+" "+nameOfSingerOrBand;
    }
}
