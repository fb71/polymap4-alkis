/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.alkis.recordstore.lucene;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import org.polymap.core.runtime.Timer;

import org.polymap.alkis.recordstore.BaseRecordStore;
import org.polymap.alkis.recordstore.IRecordState;
import org.polymap.alkis.recordstore.RecordQuery;
import org.polymap.alkis.recordstore.IRecordStore;
import org.polymap.alkis.recordstore.RecordModel;
import org.polymap.alkis.recordstore.SimpleQuery;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class LuceneRecordStore
        extends BaseRecordStore
        implements IRecordStore {

    private static Log log = LogFactory.getLog( LuceneRecordStore.class );

    private static final Version    VERSION = Version.LUCENE_34;
    
    private Directory               directory;

    private Analyzer                analyzer = new WhitespaceAnalyzer( VERSION );
    
    private ExecutorService         executor = null; //Polymap.executorService();

    IndexSearcher                   searcher;

    IndexReader                     reader;
    
    ValueCoders                     valueCoders = new ValueCoders( this );

    
    /**
     * Creates a new store for the given filesystem directory. 
     * 
     * @param indexDir The directory to hold the store files.
     * @param clean
     * @throws IOException
     */
    public LuceneRecordStore( File indexDir, boolean clean ) 
    throws IOException {
        if (!indexDir.exists()) {
            indexDir.mkdirs();
        }
        
        directory = FSDirectory.open( indexDir );
        log.info( "    Files in directry: " + Arrays.asList( directory.listAll() ) );
        open( clean );
    }


    /**
     * Creates a new in-memory store.
     * 
     * @throws IOException
     */
    public LuceneRecordStore() 
    throws IOException {
        directory = new RAMDirectory();
        log.info( "    RAMDirectory: " + Arrays.asList( directory.listAll() ) );
        open( true );
    }

    
    protected void open( boolean clean ) 
    throws IOException {
        // create or clear index
        if (directory.listAll().length == 0 || clean) {
            IndexWriterConfig config = new IndexWriterConfig( VERSION, analyzer )
                    .setOpenMode( OpenMode.CREATE );
            IndexWriter iwriter = new IndexWriter( directory, config );
            iwriter.close();
            log.info( "    Index created." );
        }

        reader = IndexReader.open( directory, true );
        searcher = new IndexSearcher( reader, executor );
    }
    
    
    public void close() {
        try {
            searcher.close();
            searcher = null;
            reader.close();
            reader = null;
            directory.close();
            directory = null;
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }


    public IRecordState newRecord() {
        assert reader != null : "Store is closed.";
        return new LuceneRecordState( this, null, new Document() );
    }


    public IRecordState get( Object id ) 
    throws Exception {
        assert reader != null : "Store is closed.";
        assert id instanceof Integer : "Given record identifier is not an Integer: " + id;
        
        Document doc = reader.document( (Integer)id );
        return new LuceneRecordState( this, (Integer)id, doc );
    }

    
    public ResultSet find( RecordQuery query )
    throws Exception {
        if (query instanceof SimpleQuery) {
            return new SimpleQueryResultSet( this, (SimpleQuery)query );
        }
        else {
            throw new IllegalArgumentException( "Unsupported query type: " + query.getClass() );
        }
    }

    
    public Updater prepareUpdate() {
        return new LuceneUpdater();
    }
    
    
    /*
     * 
     */
    class LuceneUpdater
            implements Updater {

        private IndexWriter         writer;


        LuceneUpdater() {
            try {
                IndexWriterConfig config = new IndexWriterConfig( VERSION, analyzer )
                        .setOpenMode( OpenMode.APPEND );
                writer = new IndexWriter( directory, config );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }

        
        public void store( IRecordState record ) throws Exception {
            // add
            if (record.id() == null) {
                writer.addDocument( ((LuceneRecordState)record).getDocument() );
            }
            // update
            else {
                Term idTerm = new Term( LuceneRecordState.ID_FIELD, (String)record.id() );
                writer.updateDocument( idTerm, ((LuceneRecordState)record).getDocument() );
            }
        }

        
        public void remove( IRecordState record ) throws Exception {
            assert record.id() != null : "Record is not stored.";

            Term idTerm = new Term( LuceneRecordState.ID_FIELD, (String)record.id() );
            writer.deleteDocuments( idTerm );
        }

        
        public void apply() {
            assert writer != null : "Updater is closed.";
            Timer timer = new Timer();
            try {
                writer.commit();
                writer.close();
                writer = null;
                
                searcher.close();
                reader = reader.reopen();
                searcher = new IndexSearcher( reader, executor );
                log.info( "COMMIT: " + timer.elapsedTime() + "ms" );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }

        
        public void discard() {
            assert writer != null : "Updater is closed.";
            try {
                writer.rollback();
                writer = null;
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
        
    }
    
    
    // test ***********************************************
    
    /*
     * 
     */
    public static class TestRecord
            extends RecordModel {
        
        public static final TestRecord  TYPE = type( TestRecord.class );
        
        public TestRecord( IRecordState state ) {
            super( state );
        }
        
        public Property<String>         type = new Property<String>( "type" );
        
        public Property<String>         count = new Property<String>( "count" );
        
        public Property<String>         payload = new Property<String>( "payload" );
        
    }
    

    public static void main( String[] args ) throws Exception {
        
        
        LuceneRecordStore store = new LuceneRecordStore( new File( "/tmp", "LuceneRecordStore" ), false );
//        LuceneRecordStore store = new LuceneRecordStore();
//        
//        Updater updater = store.prepareUpdate();
//        try {
//            for (int i=0; i<100000; i++) {
//                TestRecord record = new TestRecord( store.newRecord() );
//                record.payload.put( "LuceneRecordStore store = new LuceneRecordStore( new File LuceneRecordStore store = new LuceneRecordStore( new File LuceneRecordStore store = new LuceneRecordStore( new File" );
//                record.type.put( "2" );
//                record.count.put( String.valueOf( i ) );
//                updater.store( record.state() );
//            }
//            
//            updater.apply();
//        }
//        catch (Exception e) {
//            updater.discard();
//        }
        
        Timer timer = new Timer();
        String dummy = null;
        
        for (int i=0; i<100000; i++) {
            RecordQuery query = new SimpleQuery()
                    .eq( TestRecord.TYPE.count.name(), String.valueOf( i ) )
                    .eq( TestRecord.TYPE.type.name(), "2" )
                    .setMaxResults( 1 );
            
            ResultSet result = store.find( query );

            if (result.count() == 0) {
                throw new RuntimeException( "Query: " + query );
            }
            
            TestRecord record = new TestRecord( result.get( 0 ) );
            dummy = record.type.get();
            
//            for (IRecordState state : result) {
//                TestRecord record = new TestRecord( state );
//                dummy = record.count.get();
//                log.debug( "Found: type=" + record.type.get() );
//            }
        }
        log.info( "Time: " + timer.elapsedTime() + "ms -- " + dummy );
        
        store.close();
    }
    
}
