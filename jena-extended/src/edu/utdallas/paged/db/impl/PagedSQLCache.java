package edu.utdallas.paged.db.impl;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import com.hp.hpl.jena.db.IDBConnection;

import edu.utdallas.paged.db.impl.PagedResultSetReifIterator;
import edu.utdallas.paged.db.impl.PagedResultSetTripleIterator;
import com.hp.hpl.jena.db.impl.ResultSetReifIterator;
import com.hp.hpl.jena.db.impl.ResultSetIterator;
import com.hp.hpl.jena.db.impl.SQLCache;

/**
 * @see com.hp.hpl.jena.db.impl.SQLCache
 * @author vaibhav
 */
public class PagedSQLCache extends SQLCache 
{
	/** Constructor **/
    public PagedSQLCache(String sqlFile, Properties defaultOps, IDBConnection connection, String idType) throws IOException 
    { super(sqlFile, defaultOps, connection, idType); }

    /**
     * Method that executes the given SQL statement
     */
	@SuppressWarnings("unchecked")
	protected ResultSetIterator executeSQL(PreparedStatement ps, String opname, ResultSetIterator iterator) throws SQLException 
	{
		if (!ps.toString().toLowerCase().contains("limit")) { return super.executeSQL(ps, opname, iterator); }
		if (iterator instanceof ResultSetReifIterator) 
		{
			((PagedResultSetReifIterator)iterator).setParameters(ps, opname, this);
			if (((PagedResultSetReifIterator)iterator).execute()) 
				return ((PagedResultSetReifIterator)iterator);
			else 
			{
	            returnPreparedSQLStatement(ps);
	            return null;
			}
		}
		else 
		{	
			((PagedResultSetTripleIterator)iterator).setParameters(ps, opname, this);
			if (((PagedResultSetTripleIterator)iterator).execute()) 
				return ((PagedResultSetTripleIterator)iterator);       
			else 
			{
	            returnPreparedSQLStatement(ps);
	            return null;
			}
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