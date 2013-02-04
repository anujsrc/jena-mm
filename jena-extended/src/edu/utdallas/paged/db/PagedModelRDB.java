package edu.utdallas.paged.db;

import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.enhanced.BuiltinPersonalities;
import com.hp.hpl.jena.enhanced.GraphPersonality;
import com.hp.hpl.jena.enhanced.Personality;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * The RDB Model implementation for this extension
 * @author vaibhav
 */
public class PagedModelRDB extends ModelCom implements Model
{
	/** The underlying graph **/
	protected PagedGraphRDB m_graphRDB = null;
	
	/**
	 * Constructor
	 * @param base - the underlying graph
	 */
	public PagedModelRDB(Graph base) { super(base);	}
	
	/**
	 * Constructor
	 * @param base - the underlying graph
	 * @param personality - the personality of this model
	 */
	@SuppressWarnings("unchecked")
	public PagedModelRDB( Graph base, Personality personality) { super(base, personality); }
	
	/**
	 * Constructor
	 * @param model - the personality for a graph
	 * @param graph - the underlying graph
	 */
    public PagedModelRDB( GraphPersonality model, PagedGraphRDB graph ) { super(graph, model); m_graphRDB = graph; }
    
    /**
     * Constructor
     * @param graph - the underlying graph
     */
    public PagedModelRDB( PagedGraphRDB graph ) { super( graph ); m_graphRDB = graph; }
    
    /**
     * Method that opens the default extended RDB model
     * @param dbcon - the database connection
     * @return an extended RDB model
     * @throws RDFRDBException
     */
    public static PagedModelRDB open(IDBConnection dbcon) throws RDFRDBException 
    { return open(dbcon, null); }

    /**
     * Method that opens an extended RDB model given its name
     * @param dbcon - the database connection
     * @param name - the model name
     * @return an extended RDB model
     * @throws RDFRDBException
     */
    public static PagedModelRDB open(IDBConnection dbcon, String name) throws RDFRDBException 
    {
        PagedGraphRDB graph = new PagedGraphRDB(dbcon, name, null, PagedGraphRDB.OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS ,false);
		return new PagedModelRDB(graph, BuiltinPersonalities.model);
    }
    
    /**
     * Method that creates a new default extended RDB model
     * @param dbcon - the database connection
     * @return an extended RDB model
     * @throws RDFRDBException
     */
    public static PagedModelRDB createModel(IDBConnection dbcon) throws RDFRDBException 
    { return createModel(dbcon, null, getDefaultModelProperties(dbcon)); }
    
    /**
     * Method that creates a new extended RDB model based on a connection and properties
     * @param dbcon - the database connection
     * @param modelProperties - the properties for the new model supplied as a model
     * @return a new extended RDB model
     * @throws RDFRDBException
     */
    public static PagedModelRDB createModel(IDBConnection dbcon, Model modelProperties) throws RDFRDBException 
    { return createModel(dbcon, null, modelProperties); }
    
    /**
     * Constructor
     * @param dbcon - the database connection
     * @throws RDFRDBException
     */
    public PagedModelRDB( IDBConnection dbcon) throws RDFRDBException 
    { this(new PagedGraphRDB(dbcon, null, null, !dbcon.containsDefaultModel()), BuiltinPersonalities.model); }
    
    /**
     * Constructor
     * @param dbcon - the database connection
     * @param modelID - the identifier for the model
     * @throws RDFRDBException
     */
    public PagedModelRDB( IDBConnection dbcon, String modelID) throws RDFRDBException 
    { this(new PagedGraphRDB(dbcon, modelID, null, !dbcon.containsDefaultModel()), BuiltinPersonalities.model); }
    
    /**
     * Method that returns an extended RDB model given a connection and model name
     * @param dbcon - the database connection
     * @param name - the name for the model
     * @return an extended RDB model
     * @throws RDFRDBException
     */
	public static PagedModelRDB createModel(IDBConnection dbcon, String name) throws RDFRDBException 
    { return createModel(dbcon, name, getDefaultModelProperties(dbcon)); }
    
	/**
	 * Method that returns a model containing default properties based on the database connection
	 * @param dbcon - the database connection
	 * @return a model containing default properties
	 */
	public static Model getDefaultModelProperties( IDBConnection dbcon ) 
	{ return dbcon.getDefaultModelProperties(); }
	
	/**
	 * Method that creates an extended RDB model based on a connection, model name and properties
	 * @param dbcon - the database connection
	 * @param name - the model name
	 * @param modelProperties - the properties for the model
	 * @return an extended RDB model
	 * @throws RDFRDBException
	 */
    public static PagedModelRDB createModel(IDBConnection dbcon, String name, Model modelProperties) throws RDFRDBException 
    {	
        PagedGraphRDB graph;
    	if( modelProperties != null )
    		graph = new PagedGraphRDB(dbcon, name, modelProperties.getGraph(), GraphRDB.OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS ,true);
    	else
        	graph = new PagedGraphRDB(dbcon, name, null, GraphRDB.OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS, true);
        return new PagedModelRDB(graph, BuiltinPersonalities.model);
    }
    
    /**
     * Method that creates an extended RDB model based on a connection, layout type and database type
     * @param dbcon - the database connection
     * @param layoutType - the layout of the database 
     * @param databaseType - the database type
     * @return an extended RDB model
     * @throws RDFRDBException
     */
    public static PagedModelRDB create(IDBConnection dbcon, String layoutType, String databaseType) throws RDFRDBException 
    {
        dbcon.setDatabaseType(databaseType);
        return createModel(dbcon, null, getDefaultModelProperties(dbcon));
    }
    
    /**
     * Method that creates an extended RDB model based on a connection and database type
     * @param dbcon - the database connection
     * @param databaseType - the database type
     * @return an extended RDB model
     * @throws RDFRDBException
     */
    public static PagedModelRDB create(IDBConnection dbcon, String databaseType) throws RDFRDBException 
    {
        dbcon.setDatabaseType(databaseType);
        return createModel(dbcon, null, getDefaultModelProperties(dbcon));
    }
    
    /**
     * Method that returns the properties for an extended RDB model 
     * @return a model that contains properties of the extended RDB model
     */
	@SuppressWarnings("unchecked")
	public Model getModelProperties() 
	{
		Model m = ModelFactory.createDefaultModel();
        Graph g = m.getGraph();
		ExtendedIterator it = m_graphRDB.getPropertyTriples();
		while (it.hasNext()) g.add( (Triple) it.next());
		return m;
	}

	/**
	 * Method that returns an iterator of all the model names in the database
	 * @param dbcon - the database connection
	 * @return an iterator with all the models in the database
	 * @throws RDFRDBException
	 */
    @SuppressWarnings("unchecked")
	public static ExtendedIterator listModels(IDBConnection dbcon) throws RDFRDBException 
    { return dbcon.getAllModelNames(); }

    /**
     * Method that closes the extended RDB model
     */
    public void close() { m_graphRDB.close(); }
    
    /**
     * Method that removes the extended RDB model
     * @throws RDFRDBException
     */
    public void remove() throws RDFRDBException { m_graphRDB.remove(); }
    
    /**
     * Method that returns the database connection object
     * @return - the database connection object
     */
	public IDBConnection getConnection() { return m_graphRDB.getConnection(); }
	
	/**
	 * Method that clears the current extended RDB model
	 * @throws RDFRDBException
	 */
    public void clear() throws RDFRDBException { remove(); }
    
    /**
     * Method that deletes a model based on a connection and model name
     * @param dbcon - the database connection
     * @param name - the model name
     * @throws RDFRDBException
     */
	public static void deleteModel(IDBConnection dbcon, String name) throws RDFRDBException 
	{
		PagedModelRDB modelToDelete = PagedModelRDB.open(dbcon, name);
		modelToDelete.remove();
	}
	
	/**
	 * Method that returns all statements in the current extended RDB model as a memory model
	 * @return a model with statements from the current extended RDB model
	 */
	public Model loadAll()  
	{
		Model m = ModelFactory.createDefaultModel();
		for (StmtIterator i = this.listStatements(); i.hasNext(); ) {
			m.add( i.nextStatement() );
		}
		return m;
	}
	
	/** Getter method for checking duplicate triples **/
	public boolean getDoDuplicateCheck() { return m_graphRDB.getDoDuplicateCheck(); }
	
	/** Setter method for checking duplicate triples **/	
	public void setDoDuplicateCheck(boolean bool) { m_graphRDB.setDoDuplicateCheck(bool); }
	
	/** Setter method for checking fast path queries **/
	public void setDoFastpath ( boolean val ) {	m_graphRDB.setDoFastpath(val); }

	/** Getter method for checking fast path queries **/
	public boolean getDoFastpath () { return m_graphRDB.getDoFastpath(); }

	/** Setter method for querying only asserted triples **/
	public void setQueryOnlyAsserted ( boolean opt ) { m_graphRDB.setQueryOnlyAsserted(opt); }

	/** Getter method for querying only asserted triples **/
	public boolean getQueryOnlyAsserted() {	return m_graphRDB.getQueryOnlyAsserted(); }
	
	/** Setter method for querying only reified triples **/
	public void setQueryOnlyReified ( boolean opt ) { m_graphRDB.setQueryOnlyReified(opt); }

	/** Getter method for querying only reified triples **/
	public boolean getQueryOnlyReified() { return m_graphRDB.getQueryOnlyReified(); }

	/** Setter method for querying full reified triples **/
	public void setQueryFullReified ( boolean opt ) { m_graphRDB.setQueryFullReified(opt); }

	/** Getter method for querying full reified triples **/
	public boolean getQueryFullReified() { return m_graphRDB.getQueryFullReified(); }
	
	/** Setter method for implicitly doing joins **/
	public void setDoImplicitJoin ( boolean val ) {	m_graphRDB.setDoImplicitJoin(val); }
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