package edu.utdallas.paged.mem.disk;

import java.io.File;
import java.util.Iterator;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;

import edu.utdallas.paged.mem.PagedGraphTripleStoreBase;

/**
 * An abstract class that defines the basic methods for querying the lucene indexes
 * @author vaibhav
 */
public abstract class PagedGraphTripleStoreDiskBase 
{
	/** The subject, predicate, and, the object Lucene index */
	File subjectFile = null, predicateFile = null, objectFile = null;
	
	/** The nodes for the triple being searched */
	Node sm = null, pm = null, om = null;

	/** The current position from where the Lucene index is being read */
	public int lineIndex = 1;
	
	/** The increment keeps track of the chunk that is being read from the lucene index, 
	 is used for a select * type of query **/
	public int increment = 0;
	
	/** boolean variable to identify whether the subject, predicate or object index is to be searched */
	boolean isSubjectSearch = false, isPredicateSearch = false, isObjectSearch = false;
	
	/** The triple pattern to be found in the lucene index */
	public TripleMatch tripleMatch = null;
	
	/** A variable to keep track of whether a triple was successfully read from the lucene index */
	public boolean readLine = false;

	/** This is an instance of the base class */
	public PagedGraphTripleStoreBase p = null;
	
	/** A variable to keep track of the indices read at a time from the lucene index **/
	public int incrementThreshold = 0;
	
	/** Constructor
	 *  @param tm - the triple pattern we are looking for
	 *  @param p - the triple store base instance 
	 */
	public PagedGraphTripleStoreDiskBase( TripleMatch tm, PagedGraphTripleStoreBase p )
	{
		this.incrementThreshold = (int) Math.ceil( Runtime.getRuntime().totalMemory() / ( 1024*1024*1024*1.0 ) ) + 2;
		this.tripleMatch = tm; this.p = p; 
	}
	
	/** 
	 * Constructor
	 * @param tm - the triple pattern we are looking for 
	 */
	public PagedGraphTripleStoreDiskBase( TripleMatch tm )
	{ 
		this.incrementThreshold = (int) Math.ceil( Runtime.getRuntime().totalMemory() / ( 1024*1024*1024*1.0 ) ) + 2;
		this.tripleMatch = tm; 
	}

	/** 
	 * Method to add the triple to the jena structure, for the update triples case
	 * @param t - the triple to add 
	 */
	public void add( Triple t )
	{
		if ( p.predicates.size >= PagedGraphTripleStoreBase.writeThreshold ) { p.cache.writeToDisk( p.subjects, p.predicates, p.objects ); }
		if ( p.subjects.add( t ) )
		{
			p.cache.updateCache( p.subjects, p.predicates, p.objects, p.getAlgorithm(), p.getAlgorithm(), p.getAlgorithm(), t );
			p.predicates.add( t );
			p.objects.add( t );
		}
	}

	/** 
	 * Method to remove punctuations
	 * @param input - the input string
	 * @return the string with punctuation removed
	 */
	public static String removePunctuation(String input)
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < input.length(); i++) 
		{
			if (input.charAt(i) == 42 || (input.charAt(i) >= 65 && input.charAt(i) <= 90) || (input.charAt(i) >= 97 && input.charAt(i) <= 122) || (input.charAt(i) >= 48 && input.charAt(i) <= 57)) 
				sb = sb.append(input.charAt(i));
		}
		return sb.toString();
	}
	
	/** 
	 *  Singleton class to generate single objects of the index reader and the searcher
	 *  to be used for searching the lucene index  
	 */
    public static class Singleton
    {
    	private static Searcher searcher = null;
    	private static IndexReader reader = null;

    	protected Singleton() {}

    	public static IndexReader getInstance(String fileString)
    	{
    		try
    		{ reader = IndexReader.open(fileString); }
    		catch (Exception e) { e.printStackTrace(); }
    		return reader;
    	}

    	public static Searcher getInstance(IndexReader reader)
    	{ return searcher = new IndexSearcher(reader); }

    	public void finalize() throws Throwable
    	{
    		try { searcher.close(); reader.close(); }
    		finally { super.finalize(); }
    	}
    }
    
    /**
     * Method that checks the lucene index for a string
     * @param searchString - the strinng we are looking for
     * @param fileString - the index we want to check
     * @return a null iterator or an iterator that contains the triples that match the triple pattern we are looking for
     */
	@SuppressWarnings("unchecked")
	public abstract Iterator read(String searchString, String fileString) ;
	
	/**
	 * Method to find a triple pattern in the lucene index
	 * @return a null iterator or an iterator that contains the triples that match the triple pattern we are looking for
	 */
	@SuppressWarnings("unchecked")
	public abstract Iterator find() ;
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