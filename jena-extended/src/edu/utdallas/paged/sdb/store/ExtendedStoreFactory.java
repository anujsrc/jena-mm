package edu.utdallas.paged.sdb.store;

import static com.hp.hpl.jena.sdb.store.DatabaseType.DB2;
import static com.hp.hpl.jena.sdb.store.DatabaseType.Derby;
import static com.hp.hpl.jena.sdb.store.DatabaseType.H2;
import static com.hp.hpl.jena.sdb.store.DatabaseType.HSQLDB;
import static com.hp.hpl.jena.sdb.store.DatabaseType.MySQL;
import static com.hp.hpl.jena.sdb.store.DatabaseType.Oracle;
import static com.hp.hpl.jena.sdb.store.DatabaseType.PostgreSQL;
import static com.hp.hpl.jena.sdb.store.DatabaseType.SQLServer;
import static com.hp.hpl.jena.sdb.store.LayoutType.LayoutSimple;
import static com.hp.hpl.jena.sdb.store.LayoutType.LayoutTripleNodesHash;
import static com.hp.hpl.jena.sdb.store.LayoutType.LayoutTripleNodesIndex;
import static java.lang.String.format;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.layout1.StoreSimpleDB2;
import com.hp.hpl.jena.sdb.layout1.StoreSimpleDerby;
import com.hp.hpl.jena.sdb.layout1.StoreSimpleH2;
import com.hp.hpl.jena.sdb.layout1.StoreSimpleHSQL;
import com.hp.hpl.jena.sdb.layout1.StoreSimpleMySQL;
import com.hp.hpl.jena.sdb.layout1.StoreSimpleOracle;
import com.hp.hpl.jena.sdb.layout1.StoreSimplePGSQL;
import com.hp.hpl.jena.sdb.layout1.StoreSimpleSQLServer;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashDB2;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashDerby;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashH2;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashHSQL;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashOracle;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashPGSQL;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashSQLServer;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexDB2;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexDerby;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexH2;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexHSQL;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexOracle;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexPGSQL;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexSQLServer;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnectionFactory;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.sdb.store.StoreFactory;
import com.hp.hpl.jena.sdb.store.StoreMaker;
import com.hp.hpl.jena.sdb.util.Pair;

import edu.utdallas.paged.sdb.layout2.hash.StorePagedTriplesNodesHashMySQL;
import edu.utdallas.paged.sdb.layout2.index.StorePagedTriplesNodesIndexMySQL;

/**
 * @see com.hp.hpl.jena.sdb.store.StoreFactory
 * @author vaibhav
 */
public class ExtendedStoreFactory extends StoreFactory
{
    private static Logger log = LoggerFactory.getLogger(ExtendedStoreFactory.class) ;
    
    static { SDB.init() ; } 

    /**
     * @see com.hp.hpl.jena.sdb.store.StoreFactory#create(String)
     */
    public static Store create(String filename)
    { return create(StoreDesc.read(filename), null) ; }

    /**
     * @see com.hp.hpl.jena.sdb.store.StoreFactory#create(StoreDesc, SDBConnection)
     */
    public static Store create(StoreDesc desc, SDBConnection sdb)
    {
        Store store = _create(sdb, desc) ;
        return store ;
    }
    
    private static Store _create(SDBConnection sdb, StoreDesc desc)
    {
        if ( sdb == null && desc.connDesc == null )
            desc.connDesc = SDBConnectionDesc.none() ;

        if ( sdb == null && desc.connDesc.getType() == null && desc.getDbType() != null )
            desc.connDesc.setType(desc.getDbType().getName()) ;
        
        if ( sdb == null && desc.connDesc != null)
            sdb = SDBConnectionFactory.create(desc.connDesc) ;
        
        DatabaseType dbType = desc.getDbType() ;
        LayoutType layoutType = desc.getLayout() ;
        
        return _create(desc, sdb, dbType, layoutType) ;
    }
    
    private static Store _create(StoreDesc desc, SDBConnection sdb, DatabaseType dbType, LayoutType layoutType)
    {
        StoreMaker f = registry.get(dbType, layoutType) ;
        if ( f == null )
        {
            log.warn(format("No factory for (%s, %s)", dbType.getName(), layoutType.getName())) ;
            return null ;
        }
        
        return f.create(sdb, desc) ;
    }
    
    /**
     * @see com.hp.hpl.jena.sdb.store.StoreFactory#register(DatabaseType, LayoutType, StoreMaker)
     */
    public static void register(DatabaseType dbType, LayoutType layoutType, StoreMaker factory)
    {
        registry.put(dbType, layoutType, factory) ;
    }
    
    private static class Registry extends MapK2<DatabaseType, LayoutType, StoreMaker> {}
    private static Registry registry = new Registry() ;

    static { setRegistry() ; checkRegistry() ; }
    
    static private void setRegistry()
    {
        // registry.clear() ;
        // -- Hash layout
        
        register(Derby, LayoutTripleNodesHash, 
            new StoreMaker(){
                public Store create(SDBConnection conn, StoreDesc desc)
                { return new StoreTriplesNodesHashDerby(conn, desc) ; } }) ;
        
        register(HSQLDB, LayoutTripleNodesHash, 
                 new StoreMaker(){
                     public Store create(SDBConnection conn, StoreDesc desc)
                     { return new StoreTriplesNodesHashHSQL(conn, desc) ; }} ) ;
        
        /* H2 contribution from Martin HEIN (m#)/March 2008 */
        register(H2, LayoutTripleNodesHash, 
                 new StoreMaker(){
                     public Store create(SDBConnection conn, StoreDesc desc)
                     { return new StoreTriplesNodesHashH2(conn, desc) ; }} ) ;
        
        register(MySQL, LayoutTripleNodesHash,
                 new StoreMaker() {
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StorePagedTriplesNodesHashMySQL(conn, desc, desc.engineType) ; } }) ;

        register(PostgreSQL, LayoutTripleNodesHash,
                 new StoreMaker() {
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesHashPGSQL(conn, desc) ; } }) ;

        register(SQLServer, LayoutTripleNodesHash,
                 new StoreMaker() {
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesHashSQLServer(conn, desc) ; } }) ;

        register(Oracle, LayoutTripleNodesHash,
                 new StoreMaker() {
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesHashOracle(conn, desc) ; } }) ;

        register(DB2, LayoutTripleNodesHash,
                 new StoreMaker() {
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesHashDB2(conn, desc) ; } }) ;

        // -- Index layout
        
        register(Derby, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesIndexDerby(conn, desc) ; }
                    }) ;
        
        register(HSQLDB, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesIndexHSQL(conn, desc) ; } }) ;
        
        register(H2, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesIndexH2(conn, desc) ; } }) ;
        
        register(MySQL, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StorePagedTriplesNodesIndexMySQL(conn, desc, desc.engineType) ; } }) ;

        register(PostgreSQL, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesIndexPGSQL(conn, desc) ; } }) ;

        register(SQLServer, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesIndexSQLServer(conn, desc) ; } }) ;

        register(Oracle, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesIndexOracle(conn, desc) ; } }) ;
        
        register(DB2, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesIndexDB2(conn, desc) ; } }) ;
        
        // -- Simple layout
        
        register(Derby, LayoutSimple,
                 new StoreMaker() {
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreSimpleDerby(conn, desc) ; }
                    }) ;
        
        register(HSQLDB, LayoutSimple,
                 new StoreMaker() {
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreSimpleHSQL(conn, desc) ; } }) ;
        
        register(H2, LayoutSimple,
                 new StoreMaker() {
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreSimpleH2(conn, desc) ; } }) ;
        
        register(MySQL, LayoutSimple,
                 new StoreMaker() {
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreSimpleMySQL(conn, desc, desc.engineType) ; } }) ;

        register(PostgreSQL, LayoutSimple,
                 new StoreMaker() {
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreSimplePGSQL(conn, desc) ; } }) ;

        register(SQLServer, LayoutSimple,
                 new StoreMaker() {
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreSimpleSQLServer(conn, desc) ; } }) ;

        register(Oracle, LayoutSimple,
                 new StoreMaker() {
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreSimpleOracle(conn, desc) ; } }) ;

        register(DB2, LayoutSimple,
                 new StoreMaker() {
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreSimpleDB2(conn, desc) ; } }) ;
    }
    
    static private void checkRegistry()
    {
        DatabaseType[] dbTypes = {Derby, HSQLDB, H2, MySQL, PostgreSQL, SQLServer, Oracle} ;
        LayoutType[] layoutTypes = {LayoutTripleNodesHash, LayoutTripleNodesIndex, LayoutSimple} ;
        
        Set <StoreMaker> seen = new HashSet<StoreMaker>() ;
        
        for ( DatabaseType k1 : dbTypes )
            for ( LayoutType k2 : layoutTypes )
            {
                if ( ! registry.containsKey(k1, k2) )
                    log.warn(format("Missing store maker: (%s, %s)", k1.getName(), k2.getName())) ;
                StoreMaker x = registry.get(k1, k2) ;
                if ( seen.contains(x) )
                    log.warn(format("Duplicate store maker: (%s, %s)", k1.getName(), k2.getName())) ;
                seen.add(x) ;
            }
    }

    
    // Convenience.
    static class MapK2<K1, K2, V>
    {
        private Map <Pair<K1, K2>, V> map = null ;
        
        public MapK2() { map = new HashMap<Pair<K1, K2>, V>() ; }
        public MapK2(Map <Pair<K1, K2>, V> map) { this.map = map ; }
        
        public V get(K1 key1, K2 key2) { return map.get(new Pair<K1, K2>(key1, key2)) ; }
        public void put(K1 key1, K2 key2, V value) { map.put(new Pair<K1, K2>(key1, key2), value) ; }
        public boolean containsKey(K1 key1, K2 key2) { return map.containsKey(new Pair<K1, K2>(key1, key2)) ; }
        public boolean containsValue(V value) { return map.containsValue(value) ; }
        public int size() { return map.size() ; }
        public boolean isEmpty() { return map.isEmpty() ; }
        public void clear() { map.clear() ; }
    }
}
/** Copyright (c) 2008-2010, The University of Texas at Dallas
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*     * Redistributions of source code must retain the above copyright
*       notice, this list of conditions and the following disclaimer.
*     * Redistributions in binary form must reproduce the above copyright
*       notice, this list of conditions and the following disclaimer in the
*       documentation and/or other materials provided with the distribution.
*     * Neither the name of the The University of Texas at Dallas nor the
*       names of its contributors may be used to endorse or promote products
*       derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY The University of Texas at Dallas ''AS IS'' AND ANY
* EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL The University of Texas at Dallas BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/