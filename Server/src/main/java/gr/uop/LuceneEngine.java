package gr.uop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

public class LuceneEngine {
    private static final String indexDir =        "Server/src/main/java/gr/uop/indexDir";
    private static final String rawdataDir =      "Server/src/main/java/gr/uop/rawdataDir";
    private static final String modifiedDataDir = "Server/src/main/java/gr/uop/modifiedDataDir";
    public static final String resultsFile =      "Server/src/main/java/gr/uop/results.obj";
    public static final String songsFile  = modifiedDataDir+"/songsM.csv";
    public static final String albumsFile = modifiedDataDir+"/albumsM.csv";
    public static final String lyricsFile = modifiedDataDir+"/lyricsM.csv";
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
        PrintWriter pr;
        Scanner scan;
        Path modifiedDataPath = Paths.get(modifiedDataDir);
        Files.createDirectories(modifiedDataPath);
        File[] files = new File(rawdataDir).listFiles();
        for (File file : files) {
            if(!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file)){
                if(file.getName().equalsIgnoreCase("songs.csv")){//preprocessing for songs
                    File songs = new File(songsFile);
                    songs.createNewFile();
                    pr = new PrintWriter(songs);
                    scan = new Scanner(file);
                    int count = 0;
                    while(scan.hasNext()){
                        String line = scan.nextLine();
                        StringTokenizer tok = new StringTokenizer(line, ",");
                        if(count != 0){
                            tok.nextToken();
                        }
                        tok.nextToken();
                        String singerName = tok.nextToken();
                        String songName = tok.nextToken();
                        String songHref = tok.nextToken();
                        if(!singerName.equalsIgnoreCase("singer_name")){
                            singerName = singerName.substring(0, singerName.indexOf(" Lyrics"));
                        }
                        if(!songHref.equalsIgnoreCase("song_href")){
                            songHref = songHref.substring(2);
                            songHref = "https://www.azlyrics.com"+songHref;
                        }
                        pr.println(singerName+","+songName+","+songHref);
                        count += 1;
                    }

                }else if(file.getName().equalsIgnoreCase("albums.csv")){//preprocessing for albums

                }else{//preprocessing for lyrics

                }
            }
        }
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
        }
    }

    public int getMaxResultsCount() {
        return MAX_RESULTS;
    }
}
