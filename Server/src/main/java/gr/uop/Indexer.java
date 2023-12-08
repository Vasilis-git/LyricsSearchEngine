package gr.uop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {
    private IndexWriter writer;

    public Indexer(String indexdir) throws IOException {
         //this directory will contain the indexes
        Path indexPath = Paths.get(indexdir);
        Files.createDirectories(indexPath);
        Directory indexDirectory = FSDirectory.open(indexPath);
        //create the indexer
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        writer = new IndexWriter(indexDirectory, config);
    }

    public int createIndex(String datadir, TextFileFilter textFileFilter) throws IOException {
        //get all files in the data directory
        File[] files = new File(datadir).listFiles();
        for (File file : files) {
            if(!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && textFileFilter.accept(file)){
                indexFile(file);
            }
        }
        return writer.numRamDocs();
    }

    private void indexFile(File file) throws IOException {
        System.out.println("Indexing "+file.getCanonicalPath());
        Document document = getDocument(file);
        writer.addDocument(document);
    }

    private Document getDocument(File file) throws IOException {
        Document document = new Document();
        //index file contents
        BufferedReader br = new BufferedReader(new FileReader(file));
        String currentLine = br.readLine().toString();
        Field contentField = new Field(LuceneConstants.CONTENTS, currentLine, TextField.TYPE_STORED);
        //index file name
        Field fileNameField = new Field(LuceneConstants.FILE_NAME, file.getName(), StringField.TYPE_STORED);
        //index file path
        Field filePathField = new Field(LuceneConstants.FILE_PATH, file.getCanonicalPath(), StringField.TYPE_STORED);
        document.add(contentField);
        document.add(fileNameField);
        document.add(filePathField);
        br.close();
        return document;
    }

    public void close() throws IOException {
        writer.close();
    }

}
