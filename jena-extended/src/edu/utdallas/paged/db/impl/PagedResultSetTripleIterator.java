package edu.utdallas.paged.db.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.db.impl.IDBID;
import edu.utdallas.paged.db.impl.PagedSQLCache;
import edu.utdallas.paged.mem.PagedGraphTripleStoreBase;

import com.hp.hpl.jena.db.impl.ResultSetTripleIterator;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.mem.faster.GraphMemFaster;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * @see com.hp.hpl.jena.db.impl.ResultSetTripleIterator
 * @author vaibhav
 */
public class PagedResultSetTripleIterator extends ResultSetTripleIterator
{
	/** Logger **/
	Logger logger = LoggerFactory.getLogger( PagedResultSetTripleIterator.class );
	
	/** the initial chunk size **/
	protected final int INITIAL_SIZE = 1000000;
	
	/** the default chunk size **/
	protected final int DEFAULT_CHUNKSIZE;
	
	/** the chunk size for the current object **/
	protected int m_iChunkSize;

	/** the current row being processed **/
	protected int m_iCurrentRowIndex;
	
	/** the starting index for a given chunk **/
	protected int m_iStartingIndexParameterIndex;
	
	/** the parameter index for a given chunk size **/
	protected int m_iChunkSizeParameterIndex;

	/** true if the limit operator is used, false otherwise **/
	protected boolean m_bHasLimit;
	
	/** the graph used in this iterator **/
	protected Graph m_PagedGraph;
	
	/** the iterator of triples **/
	@SuppressWarnings("unchecked")
	protected ExtendedIterator m_ExtendedIterator;
	
	/** the current triple index **/
	protected int m_iCurrentTripleIndex;

	/**
	 * Constructor
	 * @param set_TripleStore_RDB - the underlying triple store
	 * @param graphID - the graph identifier
	 */
	public PagedResultSetTripleIterator( PSet_TripleStore_RDB set_TripleStore_RDB, IDBID graphID ) 
	{ 
		super( set_TripleStore_RDB, graphID );
		this.DEFAULT_CHUNKSIZE = (int) Math.ceil( ( Runtime.getRuntime().totalMemory() / ( 1024*1024*1024*1.0 ) ) * this.INITIAL_SIZE );
		this.m_iChunkSize = DEFAULT_CHUNKSIZE;
	}

	/**
	 * Method to setup parameters needed
	 * @param sourceStatement - the given query
	 * @param opname - the operation to use
	 * @param cache - the underlying cache
	 * @throws SQLException
	 */
	protected void setParameters( PreparedStatement sourceStatement, String opname, PagedSQLCache cache)
	throws SQLException
	{
		m_sqlCache = cache;
		m_statement = sourceStatement;
		m_opname = opname;
		m_iChunkSizeParameterIndex = m_statement.getParameterMetaData().getParameterCount();
		m_bHasLimit = m_statement.toString().toLowerCase().contains("limit");
		m_resultSet = null;
		m_finished = false;
		m_prefetched = false;
		m_row = null;
		m_statementClean = true;
		// Properties added in this class
		m_iCurrentRowIndex = 0;
		m_iStartingIndexParameterIndex =  m_iChunkSizeParameterIndex - 1;
		m_iCurrentTripleIndex = -1;		
		m_PagedGraph = new GraphMemFaster(ReificationStyle.Standard);
		PagedGraphTripleStoreBase.writeThreshold = this.DEFAULT_CHUNKSIZE;
	}

	/**
	 * Method that executes the given query
	 * @return true, if successful, false otherwise
	 */
	public boolean execute() 
	{
		try {
			if (m_bHasLimit) {
				return batchExecute();
			}
			else if (m_statement.execute()) {
				m_resultSet = m_statement.getResultSet();
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.warn("Problem in memory efficient iterator over db result set, op = " + m_opname, e);
			throw new JenaException(e);
		}
	}

	/**
	 * Method that executes the query as a set of batches
	 * @return true, if the execution was successful, false, otherwise
	 * @throws SQLException
	 */
	protected boolean batchExecute() throws SQLException 
	{
		// Fetch the chunks
		while(fetchNextChunk());
		m_ExtendedIterator = m_PagedGraph.find(new Triple(Node.ANY, Node.ANY, Node.ANY));		 
		if (m_ExtendedIterator.hasNext())
			return true;
		return false;
	}

	/**
	 * More forward one row. Sets the m_finished flag if there is no more to fetch
	 * @see com.hp.hpl.jena.db.impl.ResultSetTripleIterator#moveForward()
	 */
	protected void moveForward() 
	{
		try {
			if (!m_bHasLimit) {
				if (m_resultSet == null) {
					if (m_statement.execute())
						m_resultSet = m_statement.getResultSet();
					else
						m_finished = true;
				}
				super.moveForward();
				return;
			}
			if (m_finished)
				return;
			if (m_ExtendedIterator.hasNext()) 
			{
				m_triple = (Triple) m_ExtendedIterator.next();
				m_prefetched = true;
			}
			else {
				m_finished = true;
			}
		} catch(Exception e) {
			logger.warn("Problem in memory efficient iterator over db result set, op = " + m_opname, e);
			throw new JenaException(e);
		}
	}

	/**
	 * @see com.hp.hpl.jena.db.impl.ResultSetTripleIterator#hasNext()
	 */
	public boolean hasNext() { return super.hasNext(); }

	/**
	 * Fetches the next chunk of data.
	 * @return true if more fetches may be needed
	 */
	protected boolean fetchNextChunk() throws SQLException 
	{
		m_statement.setInt(m_iStartingIndexParameterIndex, m_iCurrentRowIndex);
		m_statement.setInt(m_iChunkSizeParameterIndex, m_iChunkSize);
		m_iCurrentRowIndex += m_iChunkSize;
		m_statement.execute();
		ResultSet rs = m_statement.getResultSet();
		
		int iRowsExtracted = 0;
		Triple t;
		while(rs.next()) {
			iRowsExtracted++;
			t = extractTriple(rs);
			if (null != t)
				m_PagedGraph.add(t);
		}
		rs.close();
		return (iRowsExtracted == m_iChunkSize);
	}

	/**
	 * @return The extracted triple. It may be null if extraction fails.
	 */
	protected Triple extractTriple(ResultSet rs) throws SQLException 
	{
		String subj = rs.getString(1);
		String pred = rs.getString(2);
		String obj = rs.getString(3);

		if ( m_isReif ) {
			m_stmtURI = m_pset.driver().RDBStringToNode(rs.getString(4));
			m_hasType = rs.getString(5).equals("T");
		}

		Triple t = null;

		try {
			t = m_pset.extractTripleFromRowData(subj, pred, obj);
		} catch (RDFRDBException e) {
			logger.debug("Extracting triple from row encountered exception: ", e);
		}
		return t;
	}

	/**
	 * @return the chunk size
	 */
	public int getChunkSize() {	return m_iChunkSize; }

	/**
	 * @param size the chunk size to set
	 */
	public void setChunkSize(int size) { m_iChunkSize = (size > 0) ? size : DEFAULT_CHUNKSIZE; }
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