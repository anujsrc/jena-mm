package edu.utdallas.paged.rdf.model;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.impl.OntModelImpl;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactoryBase;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.impl.ModelMakerImpl;
import com.hp.hpl.jena.shared.ReificationStyle;

import edu.utdallas.paged.db.PagedDBConnection;
import edu.utdallas.paged.db.PagedGraphRDB;
import edu.utdallas.paged.db.impl.PagedGraphRDBMaker;
import edu.utdallas.paged.db.PagedModelRDB;
import edu.utdallas.paged.graph.ExtendedFactory;
import edu.utdallas.paged.mem.PagedGraphTripleStoreBase;
import edu.utdallas.paged.rdf.model.impl.ExtendedModelCom;

/**
 * Factory class that allows to create the various models in this extension
 * @author vaibhav
 */
public class ExtendedModelFactory extends ModelFactoryBase
{
	/**
    The standard reification style; quadlets contribute to reified statements,
    and are visible to listStatements().
	 */
	public static final ReificationStyle Standard = ReificationStyle.Standard;

	/**
    	The convenient reification style; quadlets contribute to reified statements,
    	but are invisible to listStatements().
	 */
	public static final ReificationStyle Convenient = ReificationStyle.Convenient;

	/**
    	The minimal reification style; quadlets do not contribute to reified statements,
    	and are visible to listStatements().
	 */
	public static final ReificationStyle Minimal = ReificationStyle.Minimal;

	/**
	 *  Answer a new memory and file based model
	 *  @return a model
	 */
	public static Model createVirtMemModel()
	{ 
		PagedGraphTripleStoreBase.readKnowledgeBase = true; 
		return createVirtMemModel( Standard ) ;    
	}

	/**
	 *  Answer a new memory and file based model using the given reification style
	 *  @param style - the reification style to use
	 *  @return a model
	 */
	public static Model createVirtMemModel( ReificationStyle style )
	{ 
		PagedGraphTripleStoreBase.readKnowledgeBase = true; 
		return new ExtendedModelCom( ExtendedFactory.createPagedGraphMem( style ) ) ; 
	}
	
	/**
	 *  Answer a new memory and file based model for reading a knowledge base
	 *  @return a model
	 */
	public static Model createOntVirtMemModel()
	{ 
		return createOntVirtMemModel( Standard ) ;    
	}

	/**
	 *  Answer a new memory and file based model using the given reification style for reading a knowledge base
	 *  @param style - the reificiation style to use
	 *  @return a model
	 */
	public static Model createOntVirtMemModel( ReificationStyle style )
	{ 
		return new ExtendedModelCom( ExtendedFactory.createPagedGraphMem( style ) ) ; 
	}	

	/**
    	Answer a ModelMaker that accesses database-backed Models on
    	the database at the other end of the connection c with the usual
    	Standard reification style.

    	@param c a connection to the database holding the models
    	@return a ModelMaker whose Models are held in the database at c
	 */
	public static ModelMaker createPagedModelRDBMaker( IDBConnection c )
	{ 
		PagedGraphTripleStoreBase.readKnowledgeBase = true;
		return createPagedModelRDBMaker( c, Standard ); 
	}

	/**
    	Answer a ModelMaker that accesses database-backed Models on
    	the database at the other end of the connection c with the given
    	reification style.

    	@param c a connection to the database holding the models
    	@param style the desired reification style
    	@return a ModelMaker whose Models are held in the database at c
	 */
	public static ModelMaker createPagedModelRDBMaker
	( IDBConnection c, ReificationStyle style )
	{ 
		PagedGraphTripleStoreBase.readKnowledgeBase = true;
		return new PagedModelRDBMaker( new PagedGraphRDBMaker( c, style ) ); 
	}

	static class PagedModelRDBMaker extends ModelMakerImpl implements ModelMaker
	{
		public PagedModelRDBMaker( PagedGraphRDBMaker gm ) { super( gm ); }

		public Model makeModel( Graph graphRDB )
		{ return new PagedModelRDB( (PagedGraphRDB) graphRDB ); }
	}

	/**
    	Answer a plain IDBConnection to a database with the given URL, with
    	the given user having the given password. For more complex ways of
    	forming a connection, see the DBConnection documentation.

    	@param url the URL of the database
    	@param user the user name to use to access the database
    	@param password the password to use. WARNING: open text.
    	@param dbType the databate type: currently, "Oracle" or "MySQL".
    	@return the connection
    	@exception quite possibly
	 */
	public static IDBConnection createSimpleRDBConnection
	( String url, String user, String password, String dbType )
	{ return new PagedDBConnection( url, user, password, dbType ); }

	/**
    	Answer a plain IDBConnection to a database, with the arguments implicitly
    	supplied by system properties:
		<p>
    		The database URL - jena.db.url
    		<br>The user - jena.db.user, or fails back to "test"
    		<br>The password - jena.db.password, or fails back to ""
    		<br>The db type - jena.db.type, or guessed from the URL
	 */
	public static IDBConnection createSimpleRDBConnection()
	{
		return createSimpleRDBConnection
		( guessDBURL(), guessDBUser(), guessDBPassword(), guessDBType() );
	}

	/**
	 * Answer an ontology model
	 * @param spec - the specification we want to use
	 * @param base - the base model in which we've read the knowledge base
	 * @return an ontology model
	 */
    public static OntModel createOntologyModel( OntModelSpec spec, Model base ) 
    {
    	PagedGraphTripleStoreBase.readKnowledgeBase = true;
        return new OntModelImpl( spec, base );
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