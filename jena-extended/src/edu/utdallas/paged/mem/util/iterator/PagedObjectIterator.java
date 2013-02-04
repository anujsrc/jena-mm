package edu.utdallas.paged.mem.util.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.iterator.NiceIterator;

import edu.utdallas.paged.mem.disk.PagedGraphTripleStoreDiskBase;

/**
 * An iterator used to iterate over specific objects
 * @author vaibhav
 */
@SuppressWarnings("unchecked")
public abstract class PagedObjectIterator extends NiceIterator
{
	/**
	 * The lucene search instance
	 */
	public PagedGraphTripleStoreDiskBase f = null;
	
	/**
	 * Constructor
	 * @param domain - the iterator returned from an in-memory search
	 * @param pge - the lucene search instance
	 */
	public PagedObjectIterator( Iterator domain, PagedGraphTripleStoreDiskBase pge )
	{ this.domain = domain; this.f = pge; }

	/**
	 * Abstract method that looks for the specific object 
	 * @param y - the object we are looking for
	 * @return a null iterator or an iterator of triples that match the object we are looking for
	 */
	protected abstract Iterator iteratorFor( Object y );

	/**
	 * the iterator returned from the in-memory search
	 */
	public Iterator domain;

	/**
	 * the set of all object nodes that we have seen
	 */
	public Set seen = CollectionFactory.createHashedSet();

	/**
	 * the list of all pending nodes
	 */
	public List pending = new ArrayList();

	/**
	 * Method to check if the iterator has any more values
	 */
	public boolean hasNext()
	{
		boolean hasValue = false;
		while (pending.isEmpty() && domain.hasNext()) refillPending();
		if(!pending.isEmpty()) { hasValue = true; }
		else
		{
            try
            {
            	if(f.readLine || PagedIterator.isSpecificSearch) return false;
            	domain = f.find();    //try to return an iterator based on the triples read from the lucene index if any
                if(domain == null) hasValue = false;  //if no triples exist return false
                else
                {
                    if(domain.hasNext()) hasValue = true; //else true
                    while(domain.hasNext()) 
                    {
                    	Object y = domain.next();
                    	if(y instanceof Node)
                    		pending.add( y );
                    }
                }
            }
            catch(Exception e){hasValue = false;}   
		}
		return hasValue;
	}

	/**
	 * Method to return the next value in this iterator
	 * @return the next object in this iterator
	 */
	public Object next()
	{
		if (!hasNext()) throw new NoSuchElementException
		( "FasterTripleStore listObjects next()" );
		return pending.remove( pending.size() - 1 );
	}

	/**
	 * Refill the pending array list
	 */
	protected void refillPending()
	{
		Object y = domain.next();
		if (y instanceof Node)
			pending.add( y );
		else
		{
			Iterator z = iteratorFor( y );
			while (z.hasNext())
			{
				Node object = ((Triple) z.next()).getObject();
				if (seen.add( object )) pending.add( object );
			}
		}
	}

	/**
	 * not a supported operation for this iterator
	 */
	public void remove()
	{ throw new UnsupportedOperationException( "listObjects remove()" ); }
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