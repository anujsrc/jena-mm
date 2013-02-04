package edu.utdallas.paged.mem.util.iterator;

import java.util.Iterator;

import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

import edu.utdallas.paged.mem.disk.PagedGraphTripleStoreDiskBase;
import edu.utdallas.paged.mem.util.iterator.PagedIterator;
import edu.utdallas.paged.sdb.graph.PagedGraphQueryHandlerSDB.BindingQueryPlanSDB;

/**
 * A class that provides a level of indirection to the underlying PagedIterator
 * @author vaibhav
 */
@SuppressWarnings("unchecked")
public class PagedWrappedIterator extends WrappedIterator
{
	/** constructor using the base iterator and a lucene instance */
	public PagedWrappedIterator( Iterator base, PagedGraphTripleStoreDiskBase fBase, boolean removeDenied)
	{ super(new PagedIterator(base, fBase), removeDenied); }
	
	/** constructor using the base iterator and a sdb query plan instance */
	public PagedWrappedIterator( Iterator base, BindingQueryPlanSDB i, boolean isSDB, boolean removeDenied)
	{ super(new PagedIterator(base, i, isSDB), removeDenied); }	
	
	/** create a no remove iterator using the given iterator and lucene instance */
    public static PagedWrappedIterator createNoRemove( Iterator it, PagedGraphTripleStoreDiskBase fBase )
    { return new PagedWrappedIterator(it, fBase, true); }

    /** create an iterator using the given iterator and the sdb query plan instance */
    public static ExtendedIterator create( Iterator it, BindingQueryPlanSDB qPlan, boolean isSDB )
    { return it instanceof ExtendedIterator ? (ExtendedIterator) it : new PagedWrappedIterator( it, qPlan, isSDB, false ); }
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