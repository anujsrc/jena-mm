package edu.utdallas.paged.mem;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * MemToRDBThread provides a separate thread to copy triples from the 
 * extended in-memory model to the RDB model when the size of the graph
 * gets very large
 * 
 * @author vaibhav
 */

public class MemToRDBThread extends Thread
{
	/**
	 * The "subjects" structure from the extended in-memory model
	 */
	public static PagedNodeToTriplesMapBase subjects;
	
	/**
	 * An instance of the RDB model
	 */
	private Model model;
	
	/**
	 * Constructor
	 * @param sub - The subject data structure from the extended in-memory model
	 * @param rdbModel - The RDB model instance to use
	 */
	public MemToRDBThread( PagedNodeToTriplesMapBase sub, Model rdbModel )
	{	
		MemToRDBThread.subjects = sub; this.model = rdbModel;
	}
	
	/**
	 * Thread to copy the triples to the RDB model
	 */
	@SuppressWarnings("unchecked")
	public void run()
	{
		ExtendedIterator iter = subjects.iterateAll();
		while( iter.hasNext() )
		{
			model.add( model.asStatement((Triple)iter.next()) );
		}
		MemToRDBThread.subjects.clear();
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