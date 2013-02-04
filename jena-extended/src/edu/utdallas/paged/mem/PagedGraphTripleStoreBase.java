package edu.utdallas.paged.mem;

import java.io.File;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.TripleStore;

import edu.utdallas.paged.db.PagedDBConnection;
import edu.utdallas.paged.db.PagedModelRDB;
import edu.utdallas.paged.db.RDBConstants;
import edu.utdallas.paged.mem.PagedNodeToTriplesMapBase;
import edu.utdallas.paged.mem.PagedStoreTripleIterator;
import edu.utdallas.paged.mem.algorithm.CacheAlgorithmBase;
import edu.utdallas.paged.mem.cache.CacheBase;
import edu.utdallas.paged.mem.cache.CacheSEfficient;
import edu.utdallas.paged.mem.disk.PagedGraphTripleStoreDiskBase;
import edu.utdallas.paged.mem.disk.PagedGraphTripleStoreDiskGeneric;
import edu.utdallas.paged.mem.disk.PagedGraphTripleStoreDiskReasoning;
import edu.utdallas.paged.mem.disk.PagedGraphTripleStoreDiskSpecific;
import edu.utdallas.paged.mem.disk.PagedGraphTripleStoreDiskUpdate;
import edu.utdallas.paged.mem.graph.impl.PagedGraph;
import edu.utdallas.paged.mem.util.iterator.PagedIterator;
import edu.utdallas.paged.mem.util.iterator.PagedObjectIterator;
import edu.utdallas.paged.mem.util.iterator.PagedWrappedIterator;
import edu.utdallas.paged.rdf.model.ExtendedModelFactory;
import edu.utdallas.paged.shared.impl.ExtendedJenaParameters;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.shared.impl.JenaParameters;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * The base class for the triple store that provides methods for adding, 
 * removing, and querying triples as well as functions on the triple store itself
 * @author vaibhav
 */

public abstract class PagedGraphTripleStoreBase implements TripleStore
{
	/**
	 * The parent graph for which this triple store is constructed
	 */
	public final Graph parent;
	
	/**
	 * The data structures that maintain three copies of every triple,
	 * indexed respectively by subject, predicate and object
	 */
	public PagedNodeToTriplesMapBase subjects;
	public PagedNodeToTriplesMapBase predicates;
	public PagedNodeToTriplesMapBase objects;
	
	/**
	 * This variable determines if this is a unified triple store - ( extended in-memory + RDB )
	 */
	public static boolean isUnifiedModel = false;
	
	/**
	 * The buffer used by this triple store
	 */
	public CacheBase cache;

	/** The RDB Model */
	private Model model = null;

	/** 
	 *  The writeThreshold that determines after how many triples are added, the cache algorithm comes into picture,
	 *  The initialThreshold is used to set the write threshold based on the memory provided to the program,
	 *  The totalTriples gives the number of triples that have been added so far,
	 *  The unifiedThreshold is used with the unified model to determine when the cahce algorithm is used
	 */
	public static long writeThreshold = 0L, initialThreshold = 3000000L, totalTriples = 0L, unifiedThreshold = 0L;
	
	/**
	 * This variable determines if we have finished reading the knowledge base in the base model,
	 * triples in the knowledge base are not added to the cache and hence cannot be persisted to lucene
	 */
	public static boolean readKnowledgeBase = false;
	
	/**
	 * Constructor
	 * @param parent - the parent graph for this triple store
	 * @param subjects - the data structure that maintains triples indexed by subject
	 * @param predicates - the data structure that maintains triples indexed by predicate
	 * @param objects - the data structure that maintains triples indexed by object
	 */
	protected PagedGraphTripleStoreBase( Graph parent, PagedNodeToTriplesMapBase subjects, PagedNodeToTriplesMapBase predicates, PagedNodeToTriplesMapBase objects )
	{ 
		JenaParameters.disableBNodeUIDGeneration = true;
		this.parent = parent; this.subjects = subjects; this.predicates = predicates; this.objects = objects; this.cache = getCache(); 
		if( ExtendedJenaParameters.initialThreshold > 0 ) PagedGraphTripleStoreBase.writeThreshold = (long) Math.ceil( ( Runtime.getRuntime().totalMemory() / ( 1024*1024*1024*1.0 ) ) * ExtendedJenaParameters.initialThreshold ); 
		else PagedGraphTripleStoreBase.writeThreshold = (long) Math.ceil( ( Runtime.getRuntime().totalMemory() / ( 1024*1024*1024*1.0 ) ) * PagedGraphTripleStoreBase.initialThreshold );
		PagedGraphTripleStoreBase.unifiedThreshold = PagedGraphTripleStoreBase.writeThreshold;
	}

	/**
	 * Constructor
	 * @param parent - the parent graph for this triple store
	 * @param subjects - the data structure that maintains triples indexed by subject
	 * @param predicates - the data structure that maintains triples indexed by predicate
	 * @param objects - the data structure that maintains triples indexed by object
	 */
	protected PagedGraphTripleStoreBase( PagedGraph parent, PagedNodeToTriplesMapBase subjects, PagedNodeToTriplesMapBase predicates, PagedNodeToTriplesMapBase objects )
	{ 
		JenaParameters.disableBNodeUIDGeneration = true;
		this.parent = parent; this.subjects = subjects; this.predicates = predicates; this.objects = objects;	this.cache = getCache(); 
		if( ExtendedJenaParameters.initialThreshold > 0 ) PagedGraphTripleStoreBase.writeThreshold = (long) Math.ceil( ( Runtime.getRuntime().totalMemory() / ( 1024*1024*1024*1.0 ) ) * ExtendedJenaParameters.initialThreshold ); 
		else PagedGraphTripleStoreBase.writeThreshold = (long) Math.ceil( ( Runtime.getRuntime().totalMemory() / ( 1024*1024*1024*1.0 ) ) * PagedGraphTripleStoreBase.initialThreshold );
		PagedGraphTripleStoreBase.unifiedThreshold = PagedGraphTripleStoreBase.writeThreshold;
	}   

	/**
    	Destroy this triple store - discard the indexes. 
	 */
	public void close()
	{ 
		subjects = predicates = objects = null;
		//Explicitly delete the lucene indexes too !!
		if( cache.isFileCreated )
		{ cache.subF.delete(); cache.predF.delete(); cache.objF.delete(); }
	}

	/** Add a triple to this triple store. */
	public void add( Triple t )
	{
		PagedGraphTripleStoreBase.totalTriples++;	
		if( PagedGraphTripleStoreBase.totalTriples % 1000000 == 0 ) System.out.println("=== Triples so far === " + PagedGraphTripleStoreBase.totalTriples);
		
		//Check if we have specified that we want to create a unified model
		//else use the extended in-memory model
		if( ExtendedJenaParameters.useUnifiedModel )
		{
			//Unless we need to begin writing the subject index to lucene keep adding triples to the extended in-memoy model
			//else create a RDB model and start a thread to add triples from the extended in-memory model to the RDB model
			if( ( cache instanceof CacheSEfficient ) && ( subjects.size <= ( 2.8 * PagedGraphTripleStoreBase.unifiedThreshold ) ) )
			{
				if( ( cache instanceof CacheSEfficient ) && ( ( subjects.size + objects.size ) >= ( 2.9 * PagedGraphTripleStoreBase.unifiedThreshold ) ) ) { System.out.println("before, sub = " + subjects.size + " pred = " + predicates.size + " obj = " + objects.size); cache.writeObject = true; cache.writeToDisk(subjects, predicates, objects); System.out.println("after, sub = " + subjects.size + " pred = " + predicates.size + " obj = " + objects.size); }
				else if( ( subjects.size + predicates.size + objects.size ) >= ( 3 * PagedGraphTripleStoreBase.unifiedThreshold ) ) { System.out.println("before, sub = " + subjects.size + " pred = " + predicates.size + " obj = " + objects.size ); cache.writeToDisk(subjects, predicates, objects ); System.out.println("after, sub = " + subjects.size + " pred = " + predicates.size + " obj = " + objects.size);}
				if(subjects.add(t))
				{
					cache.updateCache(subjects, predicates, objects, getAlgorithm(), getAlgorithm(), getAlgorithm(), t );
					predicates.add( t );
					objects.add( t );
				}
			}
			else
			{
				if( !PagedGraphTripleStoreBase.isUnifiedModel )
				{
					PagedGraphTripleStoreBase.isUnifiedModel = true;
					model = getRDBModel();
					model.add(model.asStatement(t));

					//start a thread to add the existing triples to the rdb model in the background
					MemToRDBThread thread = new MemToRDBThread( subjects, model );
					thread.start();				
				}
				else
					model.add(model.asStatement(t));
			}
		}
		else
		{
			if( ( cache instanceof CacheSEfficient ) && ( subjects.size >= ( 2.8 * PagedGraphTripleStoreBase.writeThreshold ) ) ) { cache.writeSubject = true; cache.writeToDisk(subjects, predicates, objects); }
			else if( ( cache instanceof CacheSEfficient ) && ( ( subjects.size + objects.size ) >= ( 2.9 * PagedGraphTripleStoreBase.writeThreshold ) ) ) { cache.writeObject = true; cache.writeToDisk(subjects, predicates, objects); }
			else if( ( ( subjects.size + predicates.size + objects.size ) >= ( 3 * PagedGraphTripleStoreBase.writeThreshold ) ) ) { cache.writeToDisk(subjects, predicates, objects ); }
			if(subjects.add(t))
			{
				if( PagedGraphTripleStoreBase.readKnowledgeBase )
					cache.updateCache(subjects, predicates, objects, getAlgorithm(), getAlgorithm(), getAlgorithm(), t );
				predicates.add( t );
				objects.add( t );
			}
		}
	}

	/**
	 *  Get the database model the first time when we want to switch
	 */
	private Model getRDBModel()
	{
		PagedModelRDB model = null;
		try
		{
			//Check for the database driver
			Class.forName(RDBConstants.M_DBDRIVER_CLASS);
			
			//Create a database connection
			PagedDBConnection conn = new PagedDBConnection( RDBConstants.M_DB_URL, RDBConstants.M_DB_USER, RDBConstants.M_DB_PASSWD, RDBConstants.M_DB );
			
			//Create a model maker
			ModelMaker maker = ExtendedModelFactory.createPagedModelRDBMaker(conn) ;
			
			//Check if the model already exists
			if( maker.hasModel("memToRDB") ) maker.removeModel("memToRDB");
			
			//Create a new model
			model = (PagedModelRDB)maker.createModel("memToRDB");
			
			//Disable duplicate checking
			model.setDoDuplicateCheck(false);
		}
		catch(Exception e){e.printStackTrace();}
		
		//Return the model
		return model;
	}

	/**
     	Remove a triple from this triple store.
	 */
	public void delete( Triple t )
	{
		if( cache.isFileCreated )
		{
			if( subjects.remove( t ) )
			{
				predicates.remove( t );
				objects.remove( t ); 
			}
			else
			{
				//first get the node into memory 
				PagedGraphTripleStoreDiskBase pge = new PagedGraphTripleStoreDiskUpdate(t, this);
				pge.find();
				//then delete the triple we want to
				if (subjects.remove( t ))
				{
					predicates.remove( t );
					objects.remove( t ); 
				}		
			}
		}
		else
		{
			//first get the node into memory 
			PagedGraphTripleStoreDiskBase pge = new PagedGraphTripleStoreDiskUpdate(t, this);
			pge.find();
			//then delete the triple we want to
			if (subjects.remove( t ))
			{
				predicates.remove( t );
				objects.remove( t ); 
			}		
		}
	}
	
	/** We use the template pattern to specify the cache we want to use */
	public abstract CacheBase getCache();

	/** We use the template pattern to specify the memory management algorithm we want to use */
	public abstract CacheAlgorithmBase getAlgorithm() ;

	/**
     	Answer the size (number of triples) of this triple store.
	 */
	public int size()
	{ return subjects.size(); }

	/**
     	Answer true iff this triple store is empty.
	 */
	public boolean isEmpty()
	{ return subjects.isEmpty(); }

	/** 
	 * Method to list all the subjects in this store, we first find subjects in memory followed by subjects in 
	 * the lucene index
	 */
	@SuppressWarnings("unchecked")
	public ExtendedIterator listSubjects()
	{ 
		PagedIterator.isAllNodeSearch = true;
		PagedGraphTripleStoreDiskBase specific = new PagedGraphTripleStoreDiskSpecific( Triple.ANY, this, true, false, false );
		return PagedWrappedIterator.createNoRemove( subjects.getDomain(), specific );
	}

	/** 
	 * Method to list all the predicates in this store, we first find predicates in memory followed by predicates in 
	 * the lucene index
	 */
	@SuppressWarnings("unchecked")
	public ExtendedIterator listPredicates()
	{ 
		PagedIterator.isAllNodeSearch = true;
		PagedGraphTripleStoreDiskBase specific = new PagedGraphTripleStoreDiskSpecific( Triple.ANY, this, false, true, false ); 	
		return PagedWrappedIterator.createNoRemove( predicates.getDomain(), specific );
	}

	/** 
	 * Method to list all the objects in this store, we first find objects in memory followed by objects in 
	 * the lucene index
	 */
	@SuppressWarnings("unchecked")
	public ExtendedIterator listObjects()
	{
		PagedIterator.isAllNodeSearch = true;
		PagedGraphTripleStoreDiskBase specific = new PagedGraphTripleStoreDiskSpecific( Triple.ANY, this, false, false, true );			
		return new PagedObjectIterator( objects.getDomain(), specific )
		{
			protected Iterator iteratorFor( Object y )
			{ return objects.iteratorForIndexed( y ); }
		};
	}

	/**
     	Answer true iff this triple store contains the (concrete) triple <code>t</code>.
	 */
	public boolean contains( Triple t )
	{ 
		if (subjects.containsBySameValueAs( t )) return true;
		else
		{ return ( new PagedGraphTripleStoreDiskGeneric( t, (File)cache.getFiles()[0], (File)cache.getFiles()[1], (File)cache.getFiles()[2] ).find() != null) ; }
	}

	/** Check if the subjects contains the triple <code>t</code> */
	public boolean containsByEquality( Triple t )
	{ return subjects.contains( t ); }

	/** Clear all the indexes and the buffer */
	public void clear()
	{ subjects.clear(); predicates.clear(); objects.clear(); cache = null; }

	/**
	 * Method to find a triple pattern <code>tm</code> in this store
	 * @param tm - the triple pattern we are looking for
	 * @return iterator that is either null or contains the particular set of triples that match the given triple pattern
	 */
	@SuppressWarnings("unchecked")
	public ExtendedIterator find( TripleMatch tm )
	{		
		Triple t = tm.asTriple(); Node pm = t.getPredicate(); Node om = t.getObject(); Node sm = t.getSubject();
		PagedGraphTripleStoreDiskBase general = null;
		
		//If this is an ontology model use the the reasoning version of the class to query the lucene indexes
		//else use a generic method to query the lucene indexes
		if( PagedGraphTripleStoreBase.readKnowledgeBase )
		{
			if( cache != null && ( cache.isFileCreated || cache.isSubFileCreated ) && cache.getFiles()[0] != null && cache.getFiles()[1] != null && cache.getFiles()[2] != null ) general = new PagedGraphTripleStoreDiskReasoning( tm, (File)cache.getFiles()[0], (File)cache.getFiles()[1], (File)cache.getFiles()[2] );
			else if( cache != null && cache.isObjFileCreated ) general = new PagedGraphTripleStoreDiskReasoning( tm, null, (File)cache.getFiles()[1], (File)cache.getFiles()[2] );
			else if( cache != null && cache.isPredFileCreated ) general = new PagedGraphTripleStoreDiskReasoning( tm, null, (File)cache.getFiles()[1], null );
		}
		else
		{
			if( cache != null && ( cache.isFileCreated || cache.isSubFileCreated ) && cache.getFiles()[0] != null && cache.getFiles()[1] != null && cache.getFiles()[2] != null ) general = new PagedGraphTripleStoreDiskGeneric( tm, (File)cache.getFiles()[0], (File)cache.getFiles()[1], (File)cache.getFiles()[2] );
			else if( cache != null && cache.isObjFileCreated ) general = new PagedGraphTripleStoreDiskGeneric( tm, null, (File)cache.getFiles()[1], (File)cache.getFiles()[2] );
			else if( cache != null && cache.isPredFileCreated ) general = new PagedGraphTripleStoreDiskGeneric( tm, null, (File)cache.getFiles()[1], null );
		}
		
		if (sm.isConcrete())
			return new PagedStoreTripleIterator( this.parent, subjects.iterator( sm, pm, om ), subjects, predicates, objects, general );
		else if (om.isConcrete())
			return new PagedStoreTripleIterator( this.parent, objects.iterator( om, sm, pm ), objects, subjects, predicates, general );
		else if (pm.isConcrete())
			return new PagedStoreTripleIterator( this.parent, predicates.iterator( pm, om, sm ), predicates, subjects, objects, general );
		else
			return new PagedStoreTripleIterator( this.parent, subjects.iterateAll(), subjects, predicates, objects, general );
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