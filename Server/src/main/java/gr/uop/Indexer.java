package gr.uop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVFormat.Builder;
import org.apache.commons.csv.CSVRecord;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
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
        IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer());
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
        Document document = getDocument(file);
        writer.addDocument(document);
    }

    private Document getDocument(File file) throws IOException {
        Document doc = new Document();
        Builder csvInFileBuilder = CSVFormat.DEFAULT.builder();
        csvInFileBuilder.setSkipHeaderRecord(true);
        Reader in = new FileReader(file);
        Iterable<CSVRecord> records;
        CSVFormat inCSVFormat;
        String singerName, link;

        switch(file.getName()){
            case LuceneConstants.albumsRawFilename:{
                csvInFileBuilder.setHeader(LuceneConstants.albumsRawHeaders);
                inCSVFormat = csvInFileBuilder.build();
                records = inCSVFormat.parse(in);
                for(CSVRecord r: records){
                    singerName = configureArtistName(r.get(LuceneConstants.singerNameField));
                    doc.add(new TextField(LuceneConstants.singerNameField, singerName, Field.Store.YES));
                    doc.add(new TextField(LuceneConstants.albumNameField, r.get(LuceneConstants.albumNameField), Field.Store.YES));
                    doc.add(new TextField(LuceneConstants.albumTypeField, r.get(LuceneConstants.albumTypeField), Field.Store.YES));
                    doc.add(new TextField(LuceneConstants.albumYearField, r.get(LuceneConstants.albumYearField), Field.Store.YES));
                }
                break;
            }
            case LuceneConstants.lyricsRawFilename:{
                csvInFileBuilder.setHeader(LuceneConstants.lyricsRawHeaders);
                inCSVFormat = csvInFileBuilder.build();
                records = inCSVFormat.parse(in);
                for(CSVRecord r: records){
                    link = configureLink(r.get(LuceneConstants.linkFieldName));
                    doc.add(new TextField(LuceneConstants.HrefFieldName, link, Field.Store.YES));
                    doc.add(new TextField(LuceneConstants.artistNameField, r.get(LuceneConstants.artistNameField), Field.Store.YES));
                    doc.add(new TextField(LuceneConstants.songNameField, r.get(LuceneConstants.songNameField), Field.Store.YES));
                    doc.add(new TextField(LuceneConstants.lyricsFieldName, r.get(LuceneConstants.lyricsFieldName), Field.Store.YES));
                }
                break;
            }
            default:{//songs.csv
                csvInFileBuilder.setHeader(LuceneConstants.songsRawHeaders);
                inCSVFormat = csvInFileBuilder.build();
                records = inCSVFormat.parse(in);
                for(CSVRecord r: records){
                    singerName = configureArtistName(r.get(LuceneConstants.singerNameField));
                    doc.add(new TextField(LuceneConstants.singerNameField, singerName, Field.Store.YES));
                    doc.add(new TextField(LuceneConstants.songNameField, r.get(LuceneConstants.songNameField), Field.Store.YES));
                    link = configureLink(r.get(LuceneConstants.HrefFieldName));
                    doc.add(new TextField(LuceneConstants.HrefFieldName, r.get(LuceneConstants.HrefFieldName), Field.Store.YES));
                }
            }
        }
        return doc;
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

}
