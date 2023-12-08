package gr.uop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Comparator;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

public class LuceneEngine {
    private static final String indexDir = "src/main/java/com/gr/uop/indexDir";
    private static final String dataDir = "src/main/java/com/gr/uop/dataDir";
    private Indexer indexer;
    private Searcher searcher;
    private final int MAX_RESULTS;

    public LuceneEngine(int max_res){
        MAX_RESULTS = max_res;
    }

    public void createIndex() throws IOException {
        indexer = new Indexer(indexDir);
        int numIndexed;
        long startTime = System.currentTimeMillis();
        numIndexed = indexer.createIndex(dataDir, new TextFileFilter());
        long endTime = System.currentTimeMillis();
        indexer.close();
        System.out.println(numIndexed+" File(s) indexed, time taken: " +(endTime-startTime)+" ms");
    }

    public void search(String searchQuery) throws IOException, ParseException {
        searcher = new Searcher(indexDir, this);
        long startTime = System.currentTimeMillis();
        TopDocs hits = searcher.search(searchQuery);
        long endTime = System.currentTimeMillis();
        System.out.println(hits.totalHits +" documents found. Time :" +(endTime - startTime));
        for(ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = searcher.getDocument(scoreDoc);
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
