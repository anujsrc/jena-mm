package edu.utdallas.paged.mem.faster;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.ExpressionSet;
import com.hp.hpl.jena.graph.query.Mapping;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.graph.query.Stage;
import com.hp.hpl.jena.mem.faster.FasterPatternStage;
import com.hp.hpl.jena.mem.faster.GraphMemFaster;

import edu.utdallas.paged.mem.PagedGraphMemBaseQueryHandler;

/**
 * A class that creates a query handler for the given model
 * @author vaibhav
 */
@SuppressWarnings("unchecked")
public class PagedGraphMemFasterQueryHandler extends PagedGraphMemBaseQueryHandler implements QueryHandler
{
	/**
	 * Constructor
	 * @param graph - the base graph
	 */
	public PagedGraphMemFasterQueryHandler( GraphMemFaster graph ) 
	{ super( graph ); }

	/**
	 * Method to create a pattern stage
	 * @param map - the mapping we use
	 * @param constraints - the set of constraints
	 * @param t - the set of triples
	 */
	public Stage patternStage( Mapping map, ExpressionSet constraints, Triple [] t )
	{ return new FasterPatternStage( graph, map, constraints, t ); }
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