package gr.uop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVFormat.Builder;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class LuceneEngine {
    private static final String indexDir =        "Server/src/main/java/gr/uop/indexDir";
    private static final String rawdataDir =      "Server/src/main/java/gr/uop/rawdataDir";
    private static final String modifiedDataDir = "Server/src/main/java/gr/uop/modifiedDataDir";
    public static final String resultsFile =      "Server/src/main/java/gr/uop/results.obj";
    public static final String songsFilePath  = modifiedDataDir+"/songsM.csv";
    public static final String albumsFilePath = modifiedDataDir+"/albumsM.csv";
    public static final String lyricsFilePath = modifiedDataDir+"/lyricsM.csv";
    private static final String websiteLink = "https://www.azlyrics.com";
    private Indexer indexer;
    private Searcher searcher;
    private final int MAX_RESULTS;
    private TextFileFilter filter = new TextFileFilter();

    public LuceneEngine(int max_res){
        MAX_RESULTS = max_res;
    }

    public void createIndex() throws IOException {
        indexer = new Indexer(indexDir);
        int numIndexed;
        long startTime = System.currentTimeMillis();
        createModifiedData();
        numIndexed = indexer.createIndex(modifiedDataDir, new TextFileFilter());
        long endTime = System.currentTimeMillis();
        indexer.close();
        System.out.println(numIndexed+" File(s) indexed, time taken: " +(endTime-startTime)+" ms");
    }

    private void createModifiedData() throws IOException {
        Path modifiedDataPath = Paths.get(modifiedDataDir);
        File dir = new File(modifiedDataDir);
        if(dir.isDirectory() && dir.exists()){System.out.println("preprocessesed data exists");return;}
        Files.createDirectories(modifiedDataPath);
        File[] files = new File(rawdataDir).listFiles();
        for (File file : files) {
            if(!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file))
            {
                final String iNameField = "i";//first column, added manually
                final String singerNameField = "singer_name";
                final String songNameField = "song_name";
                final String songHrefField = "song_href";
                Reader in = new FileReader(file);

                if(file.getName().equalsIgnoreCase("songs.csv")){//preprocessing for songs
                    File modifiedCSV = new File(songsFilePath);
                    modifiedCSV.createNewFile();
                    BufferedWriter out = new BufferedWriter(new FileWriter(modifiedCSV));
                    final String[] inputHeaders = {iNameField,"song_id",singerNameField,songNameField,songHrefField};
                    final String[] outputHeaders = {singerNameField,songNameField, songHrefField};

                    Builder csvInFileBuilder = CSVFormat.DEFAULT.builder();
                    csvInFileBuilder.setHeader(inputHeaders);
                    csvInFileBuilder.setSkipHeaderRecord(true);
                    CSVFormat songCsvFormat = csvInFileBuilder.build();

                    Builder csvOutFileBuilder = CSVFormat.DEFAULT.builder();
                    csvOutFileBuilder.setHeader(outputHeaders);
                    CSVFormat outCsvFormat = csvOutFileBuilder.build();
                    CSVPrinter csvPr = new CSVPrinter(out, outCsvFormat);

                    Iterable<CSVRecord> records = songCsvFormat.parse(in);
                    for(CSVRecord r: records){
                        String singerName = r.get(singerNameField);
                        String songName = r.get(songNameField);
                        String songHref = r.get(songHrefField);
                        singerName = configureArtistName(singerNameField, singerName);
                        songHref = configureLink(songHrefField, songHref);
                        csvPr.printRecord(singerName, songName, songHref);
                    }csvPr.close();

                }else if(file.getName().equalsIgnoreCase("albums.csv")){//preprocessing for albums
                    File modifiedCSV = new File(albumsFilePath);
                    modifiedCSV.createNewFile();
                    BufferedWriter out = new BufferedWriter(new FileWriter(modifiedCSV));
                    final String nameField = "name", typeField="type", yearField = "year";
                    final String[] inputHeaders = {iNameField,"id",singerNameField, nameField, typeField, yearField};
                    final String[] outputHeaders = {singerNameField, nameField, typeField, yearField};

                    Builder csvInFileBuilder = CSVFormat.DEFAULT.builder();
                    csvInFileBuilder.setHeader(inputHeaders);
                    csvInFileBuilder.setSkipHeaderRecord(true);
                    CSVFormat albumCsvFormat = csvInFileBuilder.build();

                    Builder csvOutFileBuilder = CSVFormat.DEFAULT.builder();
                    csvOutFileBuilder.setHeader(outputHeaders);
                    CSVFormat outCsvFormat = csvOutFileBuilder.build();
                    CSVPrinter csvPr = new CSVPrinter(out, outCsvFormat);

                    Iterable<CSVRecord> records = albumCsvFormat.parse(in);
                    for(CSVRecord r: records){
                        String singerName = r.get(singerNameField);
                        String albumName = r.get(nameField);
                        String albumType = r.get(typeField);
                        String albumYear = r.get(yearField);
                        singerName = configureArtistName(singerNameField, singerName);
                        csvPr.printRecord(singerName, albumName, albumType, albumYear);
                    }csvPr.close();

                }else{//preprocessing for lyrics
                    File modifiedCSV = new File(lyricsFilePath);
                    modifiedCSV.createNewFile();
                    BufferedWriter out = new BufferedWriter(new FileWriter(modifiedCSV));
                    final String songLyricsField = "lyrics";
                    final String[] inputHeaders = {iNameField,songHrefField,singerNameField,songNameField,songLyricsField};
                    final String[] outputHeaders = {songHrefField,singerNameField,songNameField,songLyricsField};

                    Builder csvInFileBuilder = CSVFormat.DEFAULT.builder();
                    csvInFileBuilder.setHeader(inputHeaders);
                    csvInFileBuilder.setSkipHeaderRecord(true);
                    CSVFormat lyricsCsvFormat = csvInFileBuilder.build();

                    Builder csvOutFileBuilder = CSVFormat.DEFAULT.builder();
                    csvOutFileBuilder.setHeader(outputHeaders);
                    CSVFormat outCsvFormat = csvOutFileBuilder.build();
                    CSVPrinter csvPr = new CSVPrinter(out, outCsvFormat);

                    Iterable<CSVRecord> records = lyricsCsvFormat.parse(in);
                    for(CSVRecord r: records){
                        String songHref = r.get(songHrefField);
                        String singerName = r.get(singerNameField);
                        String songName = r.get(songNameField);
                        String lyrics = r.get(songLyricsField);
                        singerName = configureArtistName(singerNameField, singerName);
                        songHref = configureLink(songHrefField, songHref);
                        csvPr.printRecord(songHref, singerName, songName, lyrics);
                    }csvPr.close();
                }
            }
        }
    }

    private String configureArtistName(String singerNameField, String artist) {
        if(!artist.equalsIgnoreCase(singerNameField)){
            artist = artist.substring(0, artist.indexOf(" Lyrics"));
        }
        return artist;
    }

    private String configureLink(String linkFieldName, String link) {
        if(!link.equalsIgnoreCase(linkFieldName)){
            link = link.substring(2);
            link = websiteLink+link;
        }
        return link;
    }

    public void search(String searchQuery) throws IOException, ParseException {
        searcher = new Searcher(indexDir, this);
        long startTime = System.currentTimeMillis();
        TopDocs hits = searcher.search(searchQuery);
        long endTime = System.currentTimeMillis();
        System.out.println(hits.totalHits +" documents found. Time :" +(endTime - startTime));
        for(ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = searcher.getDocument(scoreDoc);
            System.out.println(scoreDoc);
            System.out.println("File: " +doc.get(LuceneConstants.FILE_PATH));
        }
        searcher.close();
        /*
        try {//delete all files under indexDir
            Files.walk(Paths.get(indexDir)).sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public int getMaxResultsCount() {
        return MAX_RESULTS;
    }
}
