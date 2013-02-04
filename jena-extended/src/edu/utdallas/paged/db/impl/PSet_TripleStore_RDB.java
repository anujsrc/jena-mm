package edu.utdallas.paged.db.impl;

import java.sql.PreparedStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.db.impl.DBIDInt;
import com.hp.hpl.jena.db.impl.IDBID;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.TripleMatchIterator;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * @see com.hp.hpl.jena.db.impl.PSet_TripleStore_RDB
 * @author vaibhav
 */
public class PSet_TripleStore_RDB extends com.hp.hpl.jena.db.impl.PSet_TripleStore_RDB
{
	/** Logger **/
	Logger logger = LoggerFactory.getLogger( PSet_TripleStore_RDB.class );

	/** Constructor **/
	public PSet_TripleStore_RDB() { super(); }
	
	/**
	 * @see com.hp.hpl.jena.db.impl.PSet_TripleStore_RDB#statementTableContains(IDBID, Triple)
	 */
	@SuppressWarnings("unchecked")
	public boolean statementTableContains(IDBID graphID, Triple t) 
	{
	   ExtendedIterator it = find( t,  graphID );
	   boolean res = it.hasNext();
	   it.close();
	   return res;
	}
	
	/**
	 * @see com.hp.hpl.jena.db.impl.PSet_TripleStore_RDB#find(TripleMatch, IDBID)
	 */
	@SuppressWarnings("unchecked")
	public ExtendedIterator find(TripleMatch t, IDBID graphID) 
	{
		String astName = getTblName();
		Node subj_node = t.getMatchSubject();
		Node pred_node = t.getMatchPredicate();
		Node obj_node = t.getMatchObject();
		int gid = ((DBIDInt) graphID).getIntID();
		boolean notFound = false;
		int hack = 0;

		PagedResultSetTripleIterator result =
			new PagedResultSetTripleIterator(this, graphID);

		PreparedStatement ps = null;

		String subj = null;
		String pred = null;
		String obj = null;
		String op = null;
		if ((astName.substring(0, 6)).equalsIgnoreCase("jena_g") && !(astName.substring(0, 10)).equalsIgnoreCase("jena_graph"))
			op = "selectStatementWithLimit";
		else
			op = "selectStatement";
		String qual = "";
		int args = 1;
		if ( hack != 0 ) { subj_node = pred_node = obj_node = null; }
		if (subj_node != null) 
		{
			subj = m_driver.nodeToRDBString(subj_node, false);
			if (subj == null)
				notFound = true;
			else
				qual += "S";
		}
		if (pred_node != null) 
		{
			pred = m_driver.nodeToRDBString(pred_node, false);
			if (pred == null)
				notFound = true;
			else
				qual += "P";
		}
		if (obj_node != null) 
		{
			obj = m_driver.nodeToRDBString(obj_node, false);
			if (obj == null)
				notFound = true;
			else
				qual += "O";
		}
		if (notFound == false)
		try 
		{
				op += qual;
				ps = ((PagedSQLCache)m_sql).getPreparedSQLStatement(op, getTblName());
				if (obj != null)
					ps.setString(args++, obj);
				if (subj != null)
					ps.setString(args++, subj);
				if (pred != null)
					ps.setString(args++, pred);

				ps.setInt(args++, gid);
				PagedResultSetTripleIterator returnResult = (PagedResultSetTripleIterator) ((PagedSQLCache)m_sql).executeSQL(ps, op, result);
				if ( returnResult != null ) { result = returnResult; }
		} catch (Exception e) {
			notFound = true;
			logger.debug( "find encountered exception: args=" + args + " err: ",  e);
			throw new JenaException("Exception during database access", e);    // Rethrow in case there is a recovery option
		}

		if ( notFound ) result.close();
		return (new TripleMatchIterator(t.asTriple(), result));
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