package edu.utdallas.paged.db;

import java.sql.Connection;
import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.db.impl.DBPropGraph;
import com.hp.hpl.jena.db.impl.IRDBDriver;
import com.hp.hpl.jena.db.impl.SpecializedGraph;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.utdallas.paged.rdf.model.ExtendedModelFactory;

/**
 * A class that implements the database connection for this extension
 * @author vaibhav
 */
public class PagedDBConnection extends DBConnection implements IDBConnection
{
	/**
	 * Constructor
	 * @param url - the database url
	 * @param user - the database user
	 * @param password - the database password
	 */
    public PagedDBConnection(String url, String user, String password) { super( url, user, password, null); }
    
    /**
	 * Constructor
	 * @param url - the database url
	 * @param user - the database user
	 * @param password - the database password
     * @param databaseType - the database type
     */
    public PagedDBConnection(String url, String user, String password, String databaseType) 
    { super( url, user, password, databaseType ); }

    /**
     * Constructor
     * @param connection - the database connection
     * @param databaseType - the database type
     */
    public PagedDBConnection(Connection connection, String databaseType) { super( connection, databaseType ); }

    /**
     * Method that determines if the database has a default model
     * @return true, if the database contains a default model, false otherwise
     */
	public boolean containsDefaultModel() throws RDFRDBException 
	{ return containsModel(PagedGraphRDB.DEFAULT); }

	/**
	 * Method that returns a database driver based on the type of the database
	 * @return a driver instance
	 */
	public IRDBDriver getDriver() throws RDFRDBException 
	{
		try 
		{
			if (m_connection == null) { getConnection(); }
			if (m_driver == null) 
			{
				if (m_databaseType == null) { throw new RDFRDBException("Error - attempt to call DBConnection.getDriver before setting the database type"); }
				m_driver = (IRDBDriver) (Class.forName("edu.utdallas.paged.db.Driver_Paged" + m_databaseType).newInstance());
				m_driver.setConnection( this );
			} 
		} 
		catch (Exception e) { throw new RDFRDBException("Failure to instantiate Paged DB Driver:"+ m_databaseType+ " "+ e.toString(), e); }
		return m_driver;
	}
	
	/**
	 * Method that returns the properties of the default model
	 * @return a model containing the properties
	 */
	public Model getDefaultModelProperties() throws RDFRDBException 
	{
		if (m_driver == null)
			m_driver = getDriver();
		DBPropGraph defaultProps = m_driver.getDefaultModelProperties();
		Model resultModel = ExtendedModelFactory.createVirtMemModel();
		copySpecializedGraphToModel( m_driver.getSystemSpecializedGraph(true),
			                         resultModel,
			                         Triple.createMatch(defaultProps.getNode(), null, null));
		return resultModel;
	}
	
	/**
	 * Method that copies statements from a specialized graph to a model based on a triple filter
	 * @param fromGraph - the specialized graph
	 * @param toModel - the model to be copied to
	 * @param filter - a triple filter based on which statements are added to the new model
	 * @throws RDFRDBException
	 */
	@SuppressWarnings("unchecked")
	static void copySpecializedGraphToModel( SpecializedGraph fromGraph, Model toModel, TripleMatch filter) throws RDFRDBException 
	{
		Graph toGraph = toModel.getGraph();
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		ExtendedIterator it = fromGraph.find( filter, complete);
		while(it.hasNext())
			toGraph.add((Triple)(it.next())); 
		it.close();
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