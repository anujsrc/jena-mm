package edu.utdallas.paged.sdb;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.graph.GraphSDB;
import com.hp.hpl.jena.sdb.sql.SDBConnection;

import edu.utdallas.paged.sdb.graph.PagedGraphSDB;
import edu.utdallas.paged.sdb.store.ExtendedStoreFactory;

/**
 * @see com.hp.hpl.jena.sdb.SDBFactory
 * @author vaibhav
 */
public class ExtendedSDBFactory extends SDBFactory
{
	/**
	 * @see com.hp.hpl.jena.sdb.SDBFactory#connectStore(StoreDesc)
	 */
    public static Store connectStore(StoreDesc desc) 
    { return ExtendedStoreFactory.create(desc) ; }
    
	/**
	 * @see com.hp.hpl.jena.sdb.SDBFactory#connectStore(SDBConnection, StoreDesc)
	 */
    public static Store connectStore(SDBConnection sdbConnection, StoreDesc desc) 
    { return ExtendedStoreFactory.create(desc, sdbConnection) ; }

	/**
	 * @see com.hp.hpl.jena.sdb.SDBFactory#connectStore(String)
	 */
    public static Store connectStore(String configFile) 
    { return ExtendedStoreFactory.create(configFile) ; }
    
	/**
	 * @see com.hp.hpl.jena.sdb.SDBFactory#connectDefaultGraph(String)
	 */
    public static Graph connectDefaultGraph(String configFile)
    { return connectDefaultGraph(ExtendedStoreFactory.create(configFile)) ; }

	/**
	 * @see com.hp.hpl.jena.sdb.SDBFactory#connectDefaultGraph(StoreDesc)
	 */
    public static Graph connectDefaultGraph(StoreDesc desc)
    { return connectDefaultGraph(ExtendedStoreFactory.create(desc)) ; }

	/**
	 * @see com.hp.hpl.jena.sdb.SDBFactory#connectDefaultGraph(Store)
	 */
    public static Graph connectDefaultGraph(Store store)
    { return new PagedGraphSDB(store) ; }

	/**
	 * @see com.hp.hpl.jena.sdb.SDBFactory#connectNamedGraph(String, String)
	 */
    public static Graph connectNamedGraph(String configFile, String iri)
    { return connectNamedGraph(ExtendedStoreFactory.create(configFile), iri) ; }

	/**
	 * @see com.hp.hpl.jena.sdb.SDBFactory#connectNamedGraph(StoreDesc, String)
	 */
    public static Graph connectNamedGraph(StoreDesc desc, String iri)
    { return connectNamedGraph(ExtendedStoreFactory.create(desc), iri) ; }

	/**
	 * @see com.hp.hpl.jena.sdb.SDBFactory#connectNamedGraph(Store, String)
	 */
    public static Graph connectNamedGraph(Store store, String iri)
    { return new PagedGraphSDB(store, iri) ; }

	/**
	 * @see com.hp.hpl.jena.sdb.SDBFactory#connectNamedGraph(String, Node)
	 */
    public static Graph connectNamedGraph(String configFile, Node node)
    { return connectNamedGraph(ExtendedStoreFactory.create(configFile), node) ; }
    
	/**
	 * @see com.hp.hpl.jena.sdb.SDBFactory#connectNamedGraph(StoreDesc, Node)
	 */
    public static Graph connectNamedGraph(StoreDesc desc, Node node)
    { return connectNamedGraph(ExtendedStoreFactory.create(desc), node) ; }
    
	/**
	 * @see com.hp.hpl.jena.sdb.SDBFactory#connectNamedGraph(Store, Node)
	 */
    public static Graph connectNamedGraph(Store store, Node node)
    { return new GraphSDB(store, node) ; }
    
	/**
	 * @see com.hp.hpl.jena.sdb.SDBFactory#connectDefaultModel(String)
	 */
    public static Model connectPagedDefaultModel(String configFile)
    { return connectDefaultModel(ExtendedStoreFactory.create(configFile)) ; }

	/**
	 * @see com.hp.hpl.jena.sdb.SDBFactory#connectDefaultModel(StoreDesc)
	 */
    public static Model connectPagedDefaultModel(StoreDesc desc)
    { return connectDefaultModel(ExtendedStoreFactory.create(desc)) ; }
    
	/**
	 * @see com.hp.hpl.jena.sdb.SDBFactory#connectDefaultModel(Store)
	 */
    public static Model connectPagedDefaultModel(Store store)
    { return createModelSDB(store) ; }
    
	/**
	 * @see com.hp.hpl.jena.sdb.SDBFactory#connectNamedModel(StoreDesc, String)
	 */
    public static Model connectPagedNamedModel(StoreDesc desc, String iri)
    { return connectNamedModel(ExtendedStoreFactory.create(desc), iri) ; }

	/**
	 * @see com.hp.hpl.jena.sdb.SDBFactory#connectNamedModel(Store, String)
	 */
    public static Model connectPagedNamedModel(Store store, String iri)
    { return createModelSDB(store, iri) ; }

	/**
	 * @see com.hp.hpl.jena.sdb.SDBFactory#connectStore(StoreDesc)
	 */
    public static Model connectPagedNamedModel(String configFile, String iri)
    { return connectNamedModel(ExtendedStoreFactory.create(configFile), iri) ; }

	/**
	 * @see com.hp.hpl.jena.sdb.SDBFactory#connectNamedModel(StoreDesc, Resource)
	 */
    public static Model connectPagedNamedModel(StoreDesc desc, Resource resource)
    { return connectNamedModel(ExtendedStoreFactory.create(desc), resource) ; }

	/**
	 * @see com.hp.hpl.jena.sdb.SDBFactory#connectNamedModel(Store, Resource)
	 */
    public static Model connectPagedNamedModel(Store store, Resource resource)
    { return createModelSDB(store, resource) ; }

	/**
	 * @see com.hp.hpl.jena.sdb.SDBFactory#connectNamedModel(String, Resource)
	 */
    public static Model connectPagedNamedModel(String configFile, Resource resource)
    { return connectNamedModel(ExtendedStoreFactory.create(configFile), resource) ; }

    /**
     * Method that creates a model given the corresponding Store object
     * @param store - the store object
     * @return a model for the given store
     */
    private static Model createModelSDB(Store store)
    { return ModelFactory.createModelForGraph(new PagedGraphSDB(store)) ; }

    /**
     * Method that creates a model given the corresponding Store object and iri
     * @param store - the store object
     * @param iri - the iri for the model
     * @return a model for the given store and iri
     */
    private static Model createModelSDB(Store store, String iri)
    { return ModelFactory.createModelForGraph(new PagedGraphSDB(store, iri)) ; }

    /**
     * Method that creates a model given the corresponding Store object and resource
     * @param store - the store object
     * @param resource - the resource node
     * @return a model for the given store and resource
     */
    private static Model createModelSDB(Store store, Resource resource)
    { return ModelFactory.createModelForGraph(new PagedGraphSDB(store, resource.asNode())) ; }
    
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