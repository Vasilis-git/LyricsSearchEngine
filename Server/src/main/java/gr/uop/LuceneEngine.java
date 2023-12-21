package gr.uop;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

public class LuceneEngine {
    private Indexer indexer;
    private Searcher searcher;
    private int MAX_RESULTS;
    private String searchField;

    public LuceneEngine(int max_res){
        MAX_RESULTS = max_res;
    }

    public void setSearchField(String value){
        searchField = value;//"title" or "body", set by client
    }
    
    public void setMaxResults(int maxRes){
        MAX_RESULTS = maxRes;
    }

    public void createIndex() throws IOException {
        indexer = new Indexer(LuceneConstants.indexDir);
        int numIndexed;
        long startTime = System.currentTimeMillis();
        numIndexed = indexer.createIndex(LuceneConstants.rawdataDir, new TextFileFilter());
        long endTime = System.currentTimeMillis();
        indexer.close();
        System.out.println(numIndexed+" File(s) indexed, time taken: " +(endTime-startTime)+" ms");
    }

    public ArrayList<SearchResult> search(String searchQuery) throws IOException, ParseException {
        searcher = new Searcher(LuceneConstants.indexDir, this);
        long startTime = System.currentTimeMillis();
        TopDocs hits = searcher.search(searchQuery);
        long endTime = System.currentTimeMillis();
        System.out.println(hits.totalHits +" documents found. Time :" +(endTime - startTime));
        ArrayList<SearchResult> ret = new ArrayList<>();
        for(ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = searcher.getDocument(scoreDoc);
            //System.out.println(scoreDoc);
            String title = doc.getField(LuceneConstants.indexTitle).stringValue();
            String body = doc.getField(LuceneConstants.indexBody).stringValue();
            ret.add(new SearchResult(title, body));
        }
        searcher.close();
        if(ret.size() == 0){//no results
            ret.add(new SearchResult("Nothing to show here", "Your search didn't yield any results."));
        }
        return ret;
        /*
        try {//delete all files under LuceneConstants.indexDir
            Files.walk(Paths.get(LuceneConstants.indexDir)).sorted(Comparator.reverseOrder()).forEach(path -> {
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

    public String getSearchField() {
        return searchField;
    }
}
