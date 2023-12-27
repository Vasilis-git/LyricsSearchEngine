package gr.uop;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVFormat.Builder;
import org.apache.commons.csv.CSVRecord;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {
    private IndexWriter writer;
    private Path indexPath;

    public Indexer(String indexdir) throws IOException {
         //this directory will contain the indexes
        indexPath = Paths.get(indexdir);
        Files.createDirectories(indexPath);
        Directory indexDirectory = FSDirectory.open(indexPath);
        //create the indexer
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        config.setOpenMode(OpenMode.CREATE_OR_APPEND);
        writer = new IndexWriter(indexDirectory, config);
    }

    public int createIndex(String datadir, TextFileFilter textFileFilter) throws IOException {
        if(!DirectoryReader.indexExists(FSDirectory.open(indexPath))){//create index only if it does not exist
            //get all files in the data directory
            File[] files = new File(datadir).listFiles();
            for (File file : files) {
                if(!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && textFileFilter.accept(file)){
                    indexFile(file);
                }
            }
        }
        return writer.numRamDocs();//will return 0 if index already exists, since no file was indexed
    }

    private void indexFile(File file) throws IOException {
        System.out.println("Indexing "+file.getCanonicalPath());
        Document[] documents = getDocument(file);
        for(Document d: documents){
            writer.addDocument(d);
        }
    }

    private Document[] getDocument(File file) throws IOException {
        ArrayList<Document> docs = new ArrayList<>();
        Builder csvInFileBuilder = CSVFormat.DEFAULT.builder();
        csvInFileBuilder.setSkipHeaderRecord(true);
        Reader in = new FileReader(file);
        Iterable<CSVRecord> records;
        CSVFormat inCSVFormat;
        String singerName, link, songName;

        switch(file.getName()){
            case LuceneConstants.albumsRawFilename:{
                csvInFileBuilder.setHeader(LuceneConstants.albumsRawHeaders);
                inCSVFormat = csvInFileBuilder.build();
                records = inCSVFormat.parse(in);
                for(CSVRecord r: records){
                    Document d = new Document();
                    String albumName, albumType, albumYear;
                    singerName = configureArtistName(r.get(LuceneConstants.singerNameField));
                    albumName =  r.get(LuceneConstants.albumNameField);
                    albumType =  r.get(LuceneConstants.albumTypeField);
                    albumYear = r.get(LuceneConstants.albumYearField);
                    d.add(new StringField(LuceneConstants.idField, LuceneConstants.albumsID, Field.Store.YES));
                    d.add(new TextField(LuceneConstants.indexTitle, albumName, Field.Store.YES));
                    d.add(new TextField(LuceneConstants.indexBody, singerName+", "+albumType+", "+albumYear, Field.Store.YES));
                    docs.add(d);
                }
                break;
            }
            case LuceneConstants.lyricsRawFilename:{
                csvInFileBuilder.setHeader(LuceneConstants.lyricsRawHeaders);
                inCSVFormat = csvInFileBuilder.build();
                records = inCSVFormat.parse(in);
                for(CSVRecord r: records){
                    Document d = new Document();
                    link = configureLink(r.get(LuceneConstants.linkFieldName));
                    singerName = configureArtistName(r.get(LuceneConstants.artistNameField));
                    songName = configureSongName(r.get(LuceneConstants.songNameField));
                    String lyrics = r.get(LuceneConstants.lyricsFieldName);
                    d.add(new StringField(LuceneConstants.idField, LuceneConstants.lyricsID, Field.Store.YES));
                    d.add(new TextField(LuceneConstants.indexTitle, songName+", "+singerName, Field.Store.YES));
                    d.add(new TextField(LuceneConstants.indexBody, link+"\n"+lyrics, Field.Store.YES));
                    docs.add(d);
                }
                break;
            }
            default:{//songs.csv
                csvInFileBuilder.setHeader(LuceneConstants.songsRawHeaders);
                inCSVFormat = csvInFileBuilder.build();
                records = inCSVFormat.parse(in);
                for(CSVRecord r: records){
                    Document d = new Document();
                    singerName = configureArtistName(r.get(LuceneConstants.singerNameField));
                    songName = configureSongName(r.get(LuceneConstants.songNameField));
                    link = configureLink(r.get(LuceneConstants.HrefFieldName));
                    d.add(new StringField(LuceneConstants.idField, LuceneConstants.songsID, Field.Store.YES));
                    d.add(new TextField(LuceneConstants.indexTitle, songName, Field.Store.YES));
                    d.add(new TextField(LuceneConstants.indexBody, singerName+", "+link, Field.Store.YES));
                    docs.add(d);
                }
            }
        }
        Document[] ret = new Document[docs.size()];
        Iterator<Document> it = docs.iterator();
        for(int i = 0; i < ret.length; i++){
            ret[i] = it.next();
        }
        return ret;
    }

    public void add(Document songD) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        config.setOpenMode(OpenMode.APPEND);
        writer = new IndexWriter(FSDirectory.open(indexPath), config);
        writer.addDocument(songD);
        close();
    }

    
    private String configureSongName(String songName) {
        if(songName.contains("(")){
            songName = songName.replace("(", "").replace(")", "");
        }
        return songName;
    }

    private String configureArtistName(String artist) {
        artist = artist.substring(0, artist.indexOf(" Lyrics"));
        return artist;
    }

    private String configureLink(String link) {
        link = link.substring(2);
        link = LuceneConstants.websiteLink+link;
        return link;
    }



    public ArrayList<SearchResult> getAllSongDocs() {
        try(IndexReader reader = DirectoryReader.open(FSDirectory.open(indexPath))){

            IndexSearcher searcher = new IndexSearcher(reader);
            PhraseQuery pq = new PhraseQuery.Builder().add(new Term(LuceneConstants.idField, LuceneConstants.songsID)).build();
            TopDocs hits = searcher.search(pq, Integer.MAX_VALUE);
            
            ArrayList<SearchResult> toReturn = new ArrayList<>();
            for(ScoreDoc s: hits.scoreDocs){
                Document d = searcher.storedFields().document(s.doc);
                String title = d.getField(LuceneConstants.indexTitle).stringValue();
                String body = d.getField(LuceneConstants.indexBody).stringValue();
                toReturn.add(new SearchResult(title, body));
            }
            return toReturn;
           
        } catch (IOException e) { e.printStackTrace(); }   
        return null;
    }

    public void removeAllSongDocs(ArrayList<SearchResult> toIndex) throws IOException {

        IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer());
        config.setOpenMode(OpenMode.APPEND);
        writer = new IndexWriter(FSDirectory.open(indexPath), config);
        for(SearchResult sr : toIndex){
            String songName = sr.getTitle();
            String artistName = sr.getContent().substring(0, sr.getContent().indexOf(", "));
            PhraseQuery.Builder b1 = new PhraseQuery.Builder();
            PhraseQuery.Builder b2 = new PhraseQuery.Builder();
            addToBuilder(LuceneConstants.indexTitle, songName, b1);
            addToBuilder(LuceneConstants.indexBody, artistName, b2);
            PhraseQuery songNameQuery = b1.build();
            PhraseQuery artistQuery = b2.build();
            BooleanQuery.Builder b = new BooleanQuery.Builder();
            b.add(new BooleanClause(songNameQuery, BooleanClause.Occur.MUST));//AND
            b.add(new BooleanClause(artistQuery, BooleanClause.Occur.MUST));

            //delete also the lyrics doc of the song
            PhraseQuery.Builder b3 = new PhraseQuery.Builder();
            int pos = addToBuilder(LuceneConstants.indexTitle, songName, b3);
            for(String s: artistName.toLowerCase().split(" ")){
                b3.add(new Term(LuceneConstants.indexTitle, s), pos);
                pos += 1;
            }
            BooleanQuery.Builder fin = new BooleanQuery.Builder();
            fin.add(new BooleanClause(b3.build(), BooleanClause.Occur.SHOULD));//OR
            fin.add(new BooleanClause(b.build(), BooleanClause.Occur.SHOULD));
            
            writer.deleteDocuments(fin.build());
        }
        writer.close();
    }

    private int addToBuilder(String field, String text, PhraseQuery.Builder b) {
        StringTokenizer tok = new StringTokenizer(text, " ");
        int pos = 0;
        while(tok.hasMoreTokens()){
            String part = tok.nextToken();
            part = part.toLowerCase();
            b.add(new Term(field, part), pos);
            pos += 1;
        }
        return pos;
    }

    public void close() throws IOException {
        writer.close();
    }
}
