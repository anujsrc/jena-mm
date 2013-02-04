package edu.utdallas.paged.mem;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.Triple.Field;
import com.hp.hpl.jena.mem.NodeToTriplesMapBase;

import edu.utdallas.paged.mem.PagedHashedBunchMap;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.NiceIterator;
import com.hp.hpl.jena.util.iterator.NullIterator;

/**
 * @see com.hp.hpl.jena.mem.NodeToTriplesMapBase
 * @author vaibhav
 */
public abstract class PagedNodeToTriplesMapBase extends NodeToTriplesMapBase
{
	/** The map from nodes to Bunch(Triple) **/
	public PagedHashedBunchMap bunchMap = new PagedHashedBunchMap();

	/** The size of the bunch map **/
	public int size = 0;
	
	/** Constructor **/
	public PagedNodeToTriplesMapBase( Field indexField, Field f2, Field f3 )
	{ super( indexField, f2, f3 ); }

	/**        
	 * The nodes which appear in the index position of the stored triples; useful for eg listSubjects().
	 */
	@SuppressWarnings("unchecked")
	public final Iterator getDomain()
	{ return bunchMap.keyIterator(); }

	public final Object getIndexingField( Triple t )
	{ return indexField.getField( t ).getIndexingValue(); }

	/** 
	 * @see com.hp.hpl.jena.mem.NodeToTriplesMapBase#clear()
	 */
	public void clear()
	{ bunchMap.clear(); size = 0; }

	/** 
	 * @see com.hp.hpl.jena.mem.NodeToTriplesMapBase#size()
	 */
	public int size()
	{ return size; }

	/** 
	 * @see com.hp.hpl.jena.mem.NodeToTriplesMapBase#removedOneViaIterator()
	 */
	public void removedOneViaIterator()
	{ size -= 1; /* System.err.println( ">> rOVI: size := " + size ); */ }

	/** 
	 * @see com.hp.hpl.jena.mem.NodeToTriplesMapBase#isEmpty()
	 */
	public boolean isEmpty()
	{ return size == 0; }

	/** 
	 * @see com.hp.hpl.jena.mem.NodeToTriplesMapBase#iterator(Node, Node, Node)
	 */
	@SuppressWarnings("unchecked")
	public abstract ExtendedIterator iterator( Node index, Node n2, Node n3 );

	/** 
	 * @see com.hp.hpl.jena.mem.NodeToTriplesMapBase#iteratorForIndexed(Object)
	 */
	@SuppressWarnings("unchecked")
	public abstract Iterator iteratorForIndexed( Object y );

	/** 
	 * @see com.hp.hpl.jena.mem.NodeToTriplesMapBase#iterateAll()
	 */
	@SuppressWarnings("unchecked")
	public ExtendedIterator iterateAll()
	{
		final Iterator nodes = getDomain();
		// System.err.println( "*>> NTM:iterateAll: nodes = " + IteratorCollection.iteratorToList( domain() ) );
		return new NiceIterator() 
		{
			private Iterator current = NullIterator.instance();
			private NotifyMe emptier = new NotifyMe();

			// private Object cn = "(none)";

			public Object next()
			{
				if (hasNext() == false) noElements( "NodeToTriples iterator" );
				return current.next();
			}

			class NotifyMe implements PagedHashCommon.NotifyEmpty
			{
				public void emptied()
				{ 
					// System.err.println( ">> exhausted iterator for " + cn ); 
					nodes.remove();
				}
			}

			public boolean hasNext()
			{
				while (true)
				{
					if (current.hasNext()) return true;
					if (nodes.hasNext() == false) return false;
					Object next = nodes.next();
					// cn = next;
					// System.err.println( ">----> NTM:iterateAll:hasNext: node " + next );
					current = iterator( next, emptier );
				}
			}

			public void remove()
			{ current.remove(); }
		};
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