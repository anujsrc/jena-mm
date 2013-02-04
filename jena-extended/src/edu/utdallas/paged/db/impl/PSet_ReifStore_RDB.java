package edu.utdallas.paged.db.impl;

import java.sql.PreparedStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.db.impl.IDBID;
import com.hp.hpl.jena.db.impl.ResultSetReifIterator;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Map1;

/**
 * @see com.hp.hpl.jena.db.impl.PSet_ReifStore_RDB
 * @author vaibhav
 */
public class PSet_ReifStore_RDB extends com.hp.hpl.jena.db.impl.PSet_ReifStore_RDB
{
	/** Logger **/
	Logger logger = LoggerFactory.getLogger( PSet_ReifStore_RDB.class );

	/** Constructor **/
	public PSet_ReifStore_RDB() { super(); }

	/**
	 * @see com.hp.hpl.jena.db.impl.PSet_ReifStore_RDB#findReifStmt(Node, boolean, IDBID, boolean)
	 */
	public ResultSetReifIterator findReifStmt( Node stmtURI, boolean hasType, IDBID graphID, boolean getTriples) 
	{
		if(!(m_driver.nodeToRDBString(stmtURI, false)).contains("_:")) return super.findReifStmt(stmtURI, hasType, graphID, getTriples); 
		String astName = getTblName();
		String gid = graphID._getID().toString();
		ResultSetReifIterator result = new ResultSetReifIterator(this, getTriples, graphID);

		PreparedStatement ps = null;

		int args = 1;
		String stmtStr;
		boolean findAll = (stmtURI == null) || stmtURI.equals(Node.ANY);
		boolean notFound = false;

		if ( findAll ) 
			stmtStr = hasType ? "selectReifiedT" :  "selectReified";
		else
			stmtStr = hasType ? "selectReifiedNT" : "selectReifiedN";
			
		try {
			ps = ((PagedSQLCache)m_sql).getPreparedSQLStatement(stmtStr, astName);

			if (!findAll) {
				String stmt_uri = m_driver.nodeToRDBString(stmtURI, false);
				if ( stmt_uri == null ) notFound = true;
				else 
					if((stmt_uri.substring(4, 6)).equalsIgnoreCase("_:")) ps.setString(args++, stmt_uri.substring(0, 4) + stmt_uri.substring(6, stmt_uri.length()));
					else ps.setString(args++, stmt_uri);
			}
			if (hasType)
				ps.setString(args++, "T");

			ps.setString(args++, gid);

		} catch (Exception e) {
			notFound = true;
			logger.warn( "Getting prepared statement for " + stmtStr + " Caught exception ", e);
			throw new JenaException("Exception during database access", e);    // Rethrow in case there is a recovery option
		}

		if ( notFound )
			result.close();
		else {
			try {
				((PagedSQLCache)m_sql).executeSQL(ps, stmtStr, result);
			} catch (Exception e) {
				logger.debug( "find encountered exception ", e);
				throw new JenaException("Exception during database access", e);    // Rethrow in case there is a recovery option
			}
		}
		return result;
	}

	/**
	 * @see com.hp.hpl.jena.db.impl.PSet_ReifStore_RDB#findReifNodes(Node, IDBID)
	 */
	@SuppressWarnings("unchecked")
	public ExtendedIterator findReifNodes(Node stmtURI, IDBID graphID) 
	{
		String astName = getTblName();
		String gid = graphID._getID().toString();
		PagedResultSetReifIterator result = new PagedResultSetReifIterator(this, CACHE_PREPARED_STATEMENTS, graphID);
		int argc = 1;
		PreparedStatement ps = null;
		boolean notFound = false;

		String stmtStr = null;
		
		if( stmtURI == null )
		{
			if ((astName.substring(0, 6)).equalsIgnoreCase("jena_g") && (astName.substring(9, 14)).equalsIgnoreCase("_reif"))
				stmtStr = "selectReifNodeWithLimit";
			else
				stmtStr = "selectReifNode";
		}
		else
		{
			if ((astName.substring(0, 6)).equalsIgnoreCase("jena_g") && (astName.substring(9, 14)).equalsIgnoreCase("_reif"))
				stmtStr = "selectReifNodeWithLimitN";
			else
				stmtStr = "selectReifNodeN";			
		}
		
		try {
			ps = ((PagedSQLCache)m_sql).getPreparedSQLStatement(stmtStr, astName);

			if (stmtURI != null) {
				String stmt_uri = m_driver.nodeToRDBString(stmtURI,false);
				if ( stmtURI == null ) notFound = true;
				else ps.setString(argc++, stmt_uri);
			}

			ps.setString(argc, gid);

		} catch (Exception e) {
			notFound = true;
			logger.warn( "Getting prepared statement for " + stmtStr + " Caught exception ", e);
			throw new JenaException("Exception during database access", e);    // Rethrow in case there is a recovery option
		}

		if ( notFound )
			result.close();
		else try {
			((PagedSQLCache)m_sql).executeSQL(ps, stmtStr, result);
		} catch (Exception e) {
			logger.debug("find encountered exception ", e);
			throw new JenaException("Exception during database access", e);    // Rethrow in case there is a recovery option
		}
		return result;
	}
	
	/**
	 * @see com.hp.hpl.jena.db.impl.PSet_ReifStore_RDB#findReifStmtURIByTriple(Triple, IDBID)
	 */
	@SuppressWarnings("unchecked")
	public ExtendedIterator findReifStmtURIByTriple(Triple t, IDBID my_GID) 
	{
		String astName = getTblName();
		String stmtStr = null;
		int argc = 1;
		PreparedStatement ps = null;
		PagedResultSetReifIterator result = new PagedResultSetReifIterator(this, CACHE_PREPARED_STATEMENTS, my_GID);
		boolean notFound = false;

		if ((astName.substring(0, 6)).equalsIgnoreCase("jena_g") && (astName.substring(9, 14)).equalsIgnoreCase("_reif"))
			stmtStr = "selectReifNodeWithLimit";
		else
			stmtStr = "selectReifNode";
		stmtStr += (t == null) ? "T" : "SPOT";

		try {
			ps = ((PagedSQLCache)m_sql).getPreparedSQLStatement(stmtStr, astName);
			ps.clearParameters();

			if (t != null) {
				String argStr;
				argStr = m_driver.nodeToRDBString(t.getSubject(),false);
				if ( argStr == null ) notFound = true;
				else ps.setString(argc++, argStr);
				argStr = m_driver.nodeToRDBString(t.getPredicate(),false);
				if ( argStr == null ) notFound = true;
				else ps.setString(argc++, argStr);
				argStr = m_driver.nodeToRDBString(t.getObject(),false);
				if ( argStr == null ) notFound = true;
				else ps.setString(argc++, argStr);
			}

				ps.setString(argc, my_GID._getID().toString());
		} catch (Exception e) {
			notFound = true;
			logger.warn( "Getting prepared statement for " + stmtStr + " Caught exception ",  e);
                        throw new JenaException("Exception during database access", e);    // Rethrow in case there is a recovery option
		}

		// find on object field
		if ( notFound )
			result.close();
		else {
		try {
			((PagedSQLCache)m_sql).executeSQL(ps, stmtStr, result);
		} catch (Exception e) {
			logger.debug("find encountered exception ", e);
            throw new JenaException("Exception during database access", e);    // Rethrow in case there is a recovery option
		}
		}
		return result.mapWith(new MapResultSetToNode());
	}
	
	@SuppressWarnings("unchecked")
	private class MapResultSetToNode implements Map1 
	{
		/* (non-Javadoc)
		 * @see com.hp.hpl.jena.util.iterator.Map1#map1(java.lang.Object)
		 */
		public Object map1(Object o) {
			Triple l = (Triple) o;
			Node r = l.getMatchSubject();
			return r;
		}	
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