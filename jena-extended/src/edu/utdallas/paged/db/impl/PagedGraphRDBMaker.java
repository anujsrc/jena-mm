package edu.utdallas.paged.db.impl;

import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.impl.GraphRDBMaker;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.shared.ReificationStyle;

import edu.utdallas.paged.db.PagedGraphRDB;

/**
 * @see com.hp.hpl.jena.db.impl.GraphRDBMaker
 * @author vaibhav
 */
public class PagedGraphRDBMaker extends GraphRDBMaker
{
	/** Constructor **/
	public PagedGraphRDBMaker(IDBConnection c, ReificationStyle style) { super(c, style); }

	/**
	 * @see com.hp.hpl.jena.db.impl.GraphRDBMaker#createGraph(String, boolean)
	 */
	protected Graph consGraph( String name, boolean fresh )
	{        
		Graph p = c.getDefaultModelProperties().getGraph();
		return new PagedGraphRDB( c, name, (fresh ? p : null), GraphRDB.styleRDB(this.getReificationStyle()), fresh );
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