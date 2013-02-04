package edu.utdallas.paged.query;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry;
import com.hp.hpl.jena.sparql.engine.QueryExecutionBase;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.Context;

import edu.utdallas.paged.db.PagedDBConnection;
import edu.utdallas.paged.db.RDBConstants;
import edu.utdallas.paged.mem.PagedGraphTripleStoreBase;
import edu.utdallas.paged.rdf.model.ExtendedModelFactory;
import edu.utdallas.paged.shared.impl.ExtendedJenaParameters;

/**
 * A factory class that returns query execution plans given the query and the model,
 * we use this class primarily to switch between the extended in-memory model and the
 * RDB model when we are using the unified model
 * @author vaibhav
 */
public class PagedQueryExecutionFactory 
{
	/**
	 * Blank constructor so that no one can create instances of this class
	 */
	private PagedQueryExecutionFactory() {}

	/**
	 * Method to open a model
	 * @return the rdb model
	 */
	private static Model openModel()
	{
		Model model = null;
		try
		{
			//Check for the driver class
			Class.forName(RDBConstants.M_DBDRIVER_CLASS);
			
			//Create a database connection
			PagedDBConnection conn = new PagedDBConnection( RDBConstants.M_DB_URL, RDBConstants.M_DB_USER, RDBConstants.M_DB_PASSWD, RDBConstants.M_DB );
			
			//Create the model maker
			ModelMaker maker = ExtendedModelFactory.createPagedModelRDBMaker(conn) ;
			
			//Open the model
			model = maker.openModel("memToRDB");
		}
		catch(Exception e){e.printStackTrace();}
		return model;
	}
	
	/**
	 * Method that returns a query execution plan
	 * @param query - the SPARQL query
	 * @param model - the model we want to query
	 * @return - A query execution plan
	 */
    static public QueryExecution create(Query query, Model model)
    {
        checkArg(query) ;
        checkArg(model) ;
        
        //check if unified model is created, if is use that one else the default
        if( ExtendedJenaParameters.useUnifiedModel && PagedGraphTripleStoreBase.isUnifiedModel )
        { model = openModel(); }

        return make(query, new DatasetImpl(model)) ;
    }

    /**
     * Method that returns a query execution plan
     * @param queryStr - the SPARQL query as a string
     * @param model - the model we want to query
     * @return - A query execution plan
     */
    static public QueryExecution create(String queryStr, Model model)
    {
        checkArg(queryStr) ;
        checkArg(model) ;

        //check if unified model is created, if is use that one else the default
        if( ExtendedJenaParameters.useUnifiedModel && PagedGraphTripleStoreBase.isUnifiedModel )
        { model = openModel(); }

        return create(makeQuery(queryStr), model) ;
    }

    /**
     * Method to convert the SPARQL query from a String to a Query object
     * @param queryStr - A SPARQL query as a string
     * @return - A Query object
     */
    static private Query makeQuery(String queryStr)
    {
        return QueryFactory.create(queryStr) ;
    }

    /**
     * Method to create a query execution plan given the SPARQL query as a Query object
     * @param query - the SPARQL query as a Query object
     * @return - A query execution plan
     */
    @SuppressWarnings("unused")
	static private QueryExecution make(Query query)
    { return make(query, null) ; }

    /**
     * Method to create a query execution plan given the SPARQL query and the dataset implementation
     * @param query - the SPARQL query as a Query object
     * @param dataset - a dataset implementation of the model
     * @return - A query execution plan
     */
    static private QueryExecution make(Query query, Dataset dataset)
    { return make(query, dataset, null) ; }

    /**
     * Method to generate a query execution plan
     * @param query - the SPARQL query as a Query object
     * @param dataset - the dataset implementation of this model
     * @param context - the context
     * @return - A query execution plan
     */
    static private QueryExecution make(Query query, Dataset dataset, Context context)
    {
        query.validate() ;
        if ( context == null )
            context = ARQ.getContext().copy();
        DatasetGraph dsg = null ;
        if ( dataset != null )
            dsg = dataset.asDatasetGraph() ;
        QueryEngineFactory f = findFactory(query, dsg, context);
        if ( f == null )
        {
            ALog.warn(QueryExecutionFactory.class, "Failed to find a QueryEngineFactory for query: "+query) ;
            return null ;
        }
        return new QueryExecutionBase(query, dataset, context, f) ;
    }
    
    /**
     * Method to get a query engine factory
     * @param query - the SPARQL query as a Query object
     * @param dataset - the dataset implementation of the model
     * @param context - the context
     * @return the engine factory to use
     */
    static private QueryEngineFactory findFactory(Query query, DatasetGraph dataset, Context context)
    {
        return QueryEngineRegistry.get().find(query, dataset, context);
    }
    
    /**
     * Method to check if an object is null
     * @param obj - the object which we want to check
     * @param msg - an error message
     */
    static private void checkNotNull(Object obj, String msg)
    {
        if ( obj == null )
            throw new IllegalArgumentException(msg) ;
    }
    
    /**
     * Method to check if the model is null
     * @param model - the model we want to check
     */
    static private void checkArg(Model model)
    { checkNotNull(model, "Model is a null pointer") ; }

    /**
     * Method to check if the query is null
     * @param queryStr - the SPARQL query as a string
     */
    static private void checkArg(String queryStr)
    { checkNotNull(queryStr, "Query string is null") ; }

    /**
     * Method to check if the query is null
     * @param queryStr - the SPARQL query as a Query object
     */
    static private void checkArg(Query query)
    { checkNotNull(query, "Query is null") ; }
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