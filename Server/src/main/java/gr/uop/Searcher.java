package gr.uop;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

public class Searcher {
    IndexSearcher indexSearcher;
    Directory indexDirectory;
    IndexReader indexReader;
    QueryParser queryParser;
    Query query;

    public Searcher(String indexdir) {
    }

    public TopDocs search(String searchQuery) {
        return null;
    }

    public Document getDocument(ScoreDoc scoreDoc) {
        return null;
    }

    public void close() {
    }

}
