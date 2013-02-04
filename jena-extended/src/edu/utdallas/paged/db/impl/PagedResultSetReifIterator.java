package edu.utdallas.paged.db.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.hp.hpl.jena.db.impl.ResultSetReifIterator;
import com.hp.hpl.jena.db.impl.IDBID;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;
import com.hp.hpl.jena.mem.faster.GraphMemFaster;

import edu.utdallas.paged.db.impl.PagedSQLCache;
import edu.utdallas.paged.mem.PagedGraphTripleStoreBase;

import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @see com.hp.hpl.jena.db.impl.ResultSetReifIterator
 * @author vaibhav
 */
public class PagedResultSetReifIterator extends ResultSetReifIterator
{
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
	
	/** the current triple being processed **/
	protected Triple m_Triple;

	/**
	 * Constructor
	 * @param set_ReifStore_RDB - the reification store
	 * @param getTriples - true if triples are needed, false otherwise
	 * @param graphID - the current graph id
	 */
	public PagedResultSetReifIterator( PSet_ReifStore_RDB set_ReifStore_RDB, boolean getTriples, IDBID graphID ) 
	{ 
		super( set_ReifStore_RDB, getTriples, graphID );
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
	protected void setParameters( PreparedStatement sourceStatement, String opname, PagedSQLCache cache )
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
		m_Triple = null;		
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
	 * @see com.hp.hpl.jena.db.impl.ResultSetReifIterator#moveForward()
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
			m_iCurrentTripleIndex++;
			if (m_ExtendedIterator.hasNext()) {
				m_Triple = (Triple) m_ExtendedIterator.next();
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
	 * @see com.hp.hpl.jena.db.impl.ResultSetReifIterator#getRow()
	 */
	protected Triple getRow() { return m_Triple; }

	/**
	 *  com.hp.hpl.jena.db.impl.ResultSetReifIterator#hasNext()
	 */
	public boolean hasNext() { return super.hasNext(); }

	/**
	 * Fetches the next chunk of data.
	 * @return true if successful, otherwise false
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

		//check if resultset has only one column for statement or 5 with s, p, o, stmt, type
		m_nCols = rs.getMetaData().getColumnCount();
		if(m_nCols > 1)
		{
			while(rs.next()) 
			{
				iRowsExtracted++;
				t = extractTriple(rs);
				if (null != t)
					m_PagedGraph.add(t);
			}
		}
		else
		{
			while(rs.next()) 
			{
				iRowsExtracted++;
				String node = rs.getString(1);
				//if((node.substring(0, 4)).equalsIgnoreCase("Bv::")) { PagedNTripleReader.readAnon = true; }
				t = Triple.create(m_pset.driver().RDBStringToNode(node), NodeCreateUtils.create(PrefixMapping.Standard, "adummypredicate"), NodeCreateUtils.create(PrefixMapping.Standard, "adummyobject"));
				if(t != null)
					m_PagedGraph.add(t);
			}
		}
		rs.close();
		return (iRowsExtracted == m_iChunkSize);
	}

	/**
	 * Method that extracts a triple from the result set
	 * @param rs - the result set object
	 * @return the extracted Triple
	 * @throws SQLException
	 */
	protected Triple extractTriple(ResultSet rs) throws SQLException 
	{
		extractRow(rs);
		return getTriple();
	}
	
	/**
	 * Method that extracts a row, i.e. a Triple from the result set
	 * @param rs - the result set
	 * @throws SQLException
	 */
	protected void extractRow(ResultSet rs) throws SQLException 
	{
		String subj = rs.getString(1);
		String pred = rs.getString(2);
		String obj = rs.getString(3);

		m_stmtURI = m_pset.driver().RDBStringToNode(rs.getString(4));
		m_hasType = rs.getString(5).equals("T");

		m_fragRem = 0;
		if ( m_hasType )
			if ( (m_matchObj==null) || m_matchObj.equals(RDF.Nodes.Statement) )
				m_fragRem++;

		if ( subj == null ) {
			m_subjNode = Node.NULL;
		} else {
			m_subjNode = m_pset.driver().RDBStringToNode(subj);
			if ( (m_matchObj==null) || m_matchObj.equals(m_subjNode) )
				m_fragRem++;
		}
		if ( pred == null ) {
			m_predNode = Node.NULL;
		} else {
			m_predNode = m_pset.driver().RDBStringToNode(pred);
			if ( (m_matchObj==null) || m_matchObj.equals(m_predNode) )
				m_fragRem++;
		}
		if ( obj == null ) {
			m_objNode = Node.NULL;
		} else {
			m_objNode = m_pset.driver().RDBStringToNode(obj);
			if ( (m_matchObj==null) || m_matchObj.equals(m_objNode) )
				m_fragRem++;
		}
		if ( m_propCol > 0 ) {
			m_nextFrag = m_propCol - 1;
			m_fragCount = m_fragRem = 1;
		} else {
			m_nextFrag = 0;
			m_fragCount = m_fragRem;
		}
	}
	
	/**
	 * Return triples for the current row, which should have already been extracted.
	 */
	protected Triple getTriple() 
	{
		Triple t = null;

		if ( m_getTriples == true ) {
			if ( m_nextFrag == 0) {
				if ( !m_subjNode.equals(Node.NULL) &&
						((m_matchObj==null) || m_matchObj.equals(m_subjNode)) ) {
					t = Triple.create(m_stmtURI,RDF.Nodes.subject,m_subjNode);
					m_fragRem--;
				} else
					m_nextFrag++;
			}
			if ( m_nextFrag == 1) {
				if ( !m_predNode.equals(Node.NULL) &&
						((m_matchObj==null) || m_matchObj.equals(m_predNode)) ) {
					t = Triple.create(m_stmtURI,RDF.Nodes.predicate,m_predNode);
					m_fragRem--;
				} else
					m_nextFrag++;
			}
			if ( m_nextFrag == 2) {
				if ( !m_objNode.equals(Node.NULL) &&
						((m_matchObj==null) || m_matchObj.equals(m_objNode)) ) {
					t = Triple.create(m_stmtURI,RDF.Nodes.object,m_objNode);
					m_fragRem--;
				} else
					m_nextFrag++;
			}
			if ( m_nextFrag >= 3) {
				if ( m_hasType &&
						((m_matchObj==null) || m_matchObj.equals(RDF.Nodes.Statement)) ) {
					t = Triple.create(m_stmtURI,RDF.Nodes.type,RDF.Nodes.Statement);
					m_fragRem--;							
				} else
					throw new JenaException("Reified triple not found");
			}
			m_nextFrag++;
			if ( m_fragRem > 0 )
				m_prefetched = true;

		} else {
			t = Triple.create(m_subjNode, m_predNode, m_objNode);
		}
		return t;
	}
	
	/**
	 * @return the chunk size
	 */
	public int getChunkSize() { return m_iChunkSize; }
	
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