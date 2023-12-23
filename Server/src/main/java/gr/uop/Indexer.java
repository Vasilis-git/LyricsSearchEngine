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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVFormat.Builder;
import org.apache.commons.csv.CSVRecord;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
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
                    songName = r.get(LuceneConstants.songNameField);
                    String lyrics = r.get(LuceneConstants.lyricsFieldName);
                    d.add(new StringField(LuceneConstants.idField, LuceneConstants.lyricsID, Field.Store.YES));
                    d.add(new TextField(LuceneConstants.indexTitle, singerName+", "+songName, Field.Store.YES));
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
                    songName = r.get(LuceneConstants.songNameField);
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

    private String configureArtistName(String artist) {
        artist = artist.substring(0, artist.indexOf(" Lyrics"));
        return artist;
    }

    private String configureLink(String link) {
        link = link.substring(2);
        link = LuceneConstants.websiteLink+link;
        return link;
    }

    public void close() throws IOException {
        writer.close();
    }

    public ArrayList<SearchResult> getAllSongDocs() {
        try(Directory indexDirectory = FSDirectory.open(indexPath);
            IndexReader reader = DirectoryReader.open(indexDirectory);) {

            ArrayList<SearchResult> toReturn = new ArrayList<>();
            for(int i = 0; i < reader.numDocs(); i++){
                Document d = reader.storedFields().document(i);
                if(d.get(LuceneConstants.idField) != null && d.get(LuceneConstants.idField).equalsIgnoreCase(LuceneConstants.songsID)){
                    toReturn.add(new SearchResult(d.get(LuceneConstants.indexTitle), d.get(LuceneConstants.indexBody)));
                }
            }
            return toReturn;
           
        } catch (IOException e) { e.printStackTrace(); }   
        return null;
    }

    public void removeAllSongDocs(ArrayList<SearchResult> toIndex) {

    }

}
