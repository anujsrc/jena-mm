package edu.utdallas.paged.graph.test;

import java.lang.reflect.Constructor;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.graph.impl.ReifierFragmentsMap;
import com.hp.hpl.jena.graph.impl.ReifierTripleMap;
import com.hp.hpl.jena.graph.impl.SimpleReifier;
import com.hp.hpl.jena.graph.impl.SimpleReifierFragmentsMap;
import com.hp.hpl.jena.graph.impl.SimpleReifierTripleMap;
import com.hp.hpl.jena.graph.test.MetaTestGraph;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.utdallas.paged.mem.graph.impl.PagedGraph;

public class TestPagedReifier extends AbstractTestPagedReifier
{
	public TestPagedReifier( String name )
	{ super( name ); graphClass = null; style = null; }

	@SuppressWarnings("unchecked")
	protected final Class graphClass;
	protected final ReificationStyle style;

	@SuppressWarnings("unchecked")
	public TestPagedReifier( Class graphClass, String name, ReificationStyle style ) 
	{
		super( name );
		this.graphClass = graphClass;
		this.style = style;
	}

	public static TestSuite suite()
	{ 
		TestSuite result = new TestSuite();
		//result.addTest( MetaTestGraph.suite( TestReifier.class, GraphMem.class ) );
		result.addTest( MetaTestGraph.suite( TestPagedReifier.class, PagedGraph.class ) );
		return result; 
	}   

	public Graph getGraph()
	{ return getGraph( style ); }

	@SuppressWarnings("unchecked")
	public Graph getGraph( ReificationStyle style ) 
	{
		try
		{
			Constructor cons = getConstructor( graphClass, new Class[] {ReificationStyle.class} );
			if (cons != null) return (Graph) cons.newInstance( new Object[] { style } );
			Constructor cons2 = getConstructor( graphClass, new Class [] {this.getClass(), ReificationStyle.class} );
			if (cons2 != null) return (Graph) cons2.newInstance( new Object[] { this, style } );
			throw new JenaException( "no suitable graph constructor found for " + graphClass );
		}
		catch (RuntimeException e)
		{ throw e; }
		catch (Exception e)
		{ throw new JenaException( e ); }
	}        

	public void testExtendedConstructorExists()
	{
		GraphBase parent = new GraphBase() {

			@SuppressWarnings("unchecked")
			public ExtendedIterator graphBaseFind( TripleMatch m )
			{
				return null;
			}};
			ReifierTripleMap tm = new SimpleReifierTripleMap();
			ReifierFragmentsMap fm = new SimpleReifierFragmentsMap();
			@SuppressWarnings("unused")
			SimpleReifier sr = new SimpleReifier( parent, tm, fm, ReificationStyle.Minimal );
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