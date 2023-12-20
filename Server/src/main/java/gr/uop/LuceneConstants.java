package gr.uop;

public interface LuceneConstants {
    public final String[] albumsRawHeaders = {"i", "id", "singer_name", "name", "type", "year"};
    public final String[] songsRawHeaders = {"i", "song_id", "singer_name", "song_name", "song_href"};
    public final String[] lyricsRawHeaders = {"i", "link", "artist", "song_name", "lyrics"};

    public final String songsRawFilename = "songs.csv";
    public final String albumsRawFilename= "albums.csv";
    public final String lyricsRawFilename= "lyrics.csv";   
    public final String indexDir =        "Server/src/main/java/gr/uop/indexDir";
    public final String rawdataDir =      "Server/src/main/java/gr/uop/rawdataDir";
    public final String websiteLink = "https://www.azlyrics.com";
    
    public final String singerNameField = "singer_name";
    public final String artistNameField = "artist";
    public final String songNameField = "song_name";
    public final String HrefFieldName = "song_href";
    public final String linkFieldName = "link";
    public final String albumNameField = "name";    
    public final String albumTypeField = "type";
    public final String albumYearField = "year";
    public final String lyricsFieldName = "lyrics";    

}
