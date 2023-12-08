package gr.uop;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Searcher {
    private IndexSearcher indexSearcher;
    private Directory indexDirectory;
    private IndexReader indexReader;
    private QueryParser queryParser;
    private Query query;
    private LuceneEngine parent;

    public Searcher(String indexdir, LuceneEngine luceneEngine) throws IOException {
        this.parent = luceneEngine;
        Path indexPath = Paths.get(indexdir);
        indexDirectory = FSDirectory.open(indexPath);
        indexReader = DirectoryReader.open(indexDirectory);
        indexSearcher = new IndexSearcher(indexReader);
        queryParser = new QueryParser(LuceneConstants.CONTENTS, new StandardAnalyzer());
    }

    public TopDocs search(String searchQuery) throws IOException {
        try {
            query = queryParser.parse(searchQuery);
            System.out.println("query: "+ query.toString());
            int show = 0;
            if(parent.getMaxResultsCount() == 0){
                show = IndexSearcher.getMaxClauseCount();
            }else{
                show = parent.getMaxResultsCount();
            }
            return indexSearcher.search(query, show);
        } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Document getDocument(ScoreDoc scoreDoc) throws IOException {
        return indexSearcher.storedFields().document(scoreDoc.doc);
    }

    public void close() throws IOException {
        indexReader.close();
        indexDirectory.close();
    }

}
