package edu.utdallas.paged.mem.util;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Literal;

import com.hp.hpl.jena.rdf.model.impl.NTripleWriter;

/**
 * A class that is used to convert a Triple stored in memory into a N-Triple that is stored in the Lucene indices
 * @author vaibhav
 */
public class PagedNTripleWriter extends NTripleWriter
{
	/** A static variable that determines the position of a Node in the subject Lucene index **/
	public static long sid = 10L;

	/** A static variable that determines the position of a Node in the predicate Lucene index **/
	public static long pid = 10L;
	
	/** A static variable that determines the position of a Node in the object Lucene index **/
	public static long oid = 10L;
	
	/**
	 * Acceptable characters in URI's
	 */
	private static boolean okURIChars[] = new boolean[128];
	static {
				for (int i = 32; i < 127; i++)
					okURIChars[i] = true;
					okURIChars['<'] = false;
					okURIChars['>'] = false;
					okURIChars['\\'] = false;
		   }

	/**
	 * Method that writes a URI and its associated triples to a Lucene index
	 * @param f - the directory for the Lucene indices
	 * @param uri - the given URI
	 * @param triples - the triples associated with a URI
	 * @param writer - the Lucene index writer
	 * @param isSubjectIndex - true if this a subject index, false otherwise
	 * @param isPredicateIndex - true if this a predicate index, false otherwise
	 * @param isObjectIndex - true if this an object index, false otherwise
	 * @return true, if the Lucene index was updated, false otherwise
	 */
	public boolean writeTriple(File f, String uri, String triples, IndexWriter writer, boolean isSubjectIndex, boolean isPredicateIndex, boolean isObjectIndex)
	{
		try 
		{
			Document doc = new Document();
			if(isSubjectIndex) doc.add(new Field("id", sid++ + "", Field.Store.YES, Field.Index.UN_TOKENIZED));
			if(isPredicateIndex) doc.add(new Field("id", pid++ + "", Field.Store.YES, Field.Index.UN_TOKENIZED));
			if(isObjectIndex) doc.add(new Field("id", oid++ + "", Field.Store.YES, Field.Index.UN_TOKENIZED));
		    doc.add(new Field("uri", uri, Field.Store.YES, Field.Index.UN_TOKENIZED));
		    doc.add(new Field("triples", triples, Field.Store.YES, Field.Index.UN_TOKENIZED));
		    writer.addDocument(doc);
		} 
		catch (IOException e) { e.printStackTrace();  return false;}
		return true;
	}
	
	/**
	 * Method that updates the Lucene index for a URI by adding triples for that URI
	 * @param f - the directory for the Lucene indices
	 * @param reader - the Lucene index reader
	 * @param searcher - the Lucene index searcher
	 * @param uri - the given URI
	 * @param triples - the triples associated with a URI
	 * @param writer - the Lucene index writer
	 * @param isSubjectIndex - true if this a subject index, false otherwise
	 * @param isPredicateIndex - true if this a predicate index, false otherwise
	 * @param isObjectIndex - true if this an object index, false otherwise
	 * @return true, if the index was updated, false otherwise
	 * @throws IOException
	 */
	public boolean updateIndex( File f, IndexReader reader, Searcher searcher, String uri, String triples, IndexWriter writer, boolean isSubjectIndex, boolean isPredicateIndex, boolean isObjectIndex ) throws IOException
	{
		String tripleStr = "", id = null;
		try
		{
			BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE); Analyzer analyzer = new KeywordAnalyzer();
			QueryParser parser = new QueryParser("uri", analyzer); Query query = parser.parse(uri);
			Hits hits = searcher.search(query);
			for(int i=0; i<hits.length(); i++)
			{ Document doc = hits.doc(i); id = doc.get("id"); tripleStr = doc.get("triples"); }
			if( id != null )
			{
				Term identifier = new Term("id", id);
				Document d = new Document();
				d.add(new Field("id", id, Field.Store.YES, Field.Index.UN_TOKENIZED));
				d.add(new Field("uri", uri, Field.Store.YES, Field.Index.UN_TOKENIZED));
				d.add(new Field("triples", tripleStr.concat(triples), Field.Store.YES, Field.Index.UN_TOKENIZED));
				writer.updateDocument(identifier, d);
			}
			else
			{ writeTriple( f, uri, triples, writer, isSubjectIndex, isPredicateIndex, isObjectIndex ); }
		}
		catch(Exception e) { e.printStackTrace(); return false; }
		return true;
	}
	
	/**
	 * Method that converts a Triple into its N-Triple representation
	 * @param t - the Triple t
	 * @return a N-Triple representation of the Triple as a string
	 */
	public static String writeNTriple(Triple t)
	{
		String str = "";
		str = writeResource(t.getSubject(), str);
		str += " ";
		str = writeResource(t.getPredicate(), str);
		str += " ";
		str = writeNode(t.getObject(), str);
		str += (" .\n");
		return str;
	}

	/**
	 * Method that converts the given resource node into its string representation 
	 * @param r - the node
	 * @param writer - a string that holds the N-Triple representation
	 * @return a string representation of the given node
	 */
	private static String writeResource(Node r, String writer)
	{
		if (r.isBlank())
			writer += anonName(r.getBlankNodeId());
		else 
		{
				String uri;
				uri = "<";
				uri += writeURIString(r.getURI());
				uri += ">";
				writer += uri;
		}
		return writer;
	}

	/**
	 * Method that converts the given literal node into its string representation 
	 * @param r - the node
	 * @param writer - a string that holds the N-Triple representation
	 * @return a string representation of the given node
	 */
	private static String writeNode(Node n, String writer)
	{
		if (n instanceof Node_Literal)
			writer = writeNodeLiteral((Node) n, writer);
		else if (n.isLiteral())
			writer = writeLiteral((Literal) n, writer);
		else
			writer = writeResource((Node) n, writer);
		return writer;
	}

	/**
	 * Method that converts the given literal to its string representation
	 * @param l - the given literal 
	 * @param writer - a string that holds the N-Triple representation
	 * @return a string representation of the literal
	 */
	private static String writeLiteral(Literal l, String writer) 
	{
		String s = l.toString();
		String literal = "";
		literal += "" + '"';
		literal += writeString(s, writer);
		literal += "" + '"';
		String lang = l.getLanguage();
		if (lang != null && !lang.equals(""))
			literal += "@" + lang;
		String dt = l.getDatatypeURI();
		if (dt != null && !dt.equals(""))
			literal += "^^<" + dt + ">";
		writer += literal;
		return writer;	
	}

	/**
	 * Method that converts the given literal node to its string representation
	 * @param l - the given literal node
	 * @param writer - a string that holds the N-Triple representation
	 * @return a string representation of the literal node
	 */
	private static String writeNodeLiteral(Node l, String writer) 
	{
		String literal = "";
		literal += "" + '"';
		literal += l.getLiteralLexicalForm();
		literal += "" + '"';
		String lang = l.getLiteralLanguage();
		if (lang != null && !lang.equals(""))
			literal += "@" + lang;
		String dt = l.getLiteralDatatypeURI();
		if (dt != null && !dt.equals(""))
			literal += "^^<" + dt + ">";
		writer += literal;
		return writer;
	}

	/**
	 * Method that converts the string URI to its N-Triple representation 
	 * @param s - the string representing the URI
	 * @return a string representation of the URI with appropriate markup
	 */
	private static String writeURIString(String s) 
	{
		String uri = "";
		for (int i = 0; i < s.length(); i++) 
		{
			char c = s.charAt(i);
			if (c < okURIChars.length && okURIChars[c])
				uri += "" + c;
			else 
			{
				String hexstr = Integer.toHexString(c).toUpperCase();
				int pad = 4 - hexstr.length();
				uri += "\\u";
				for (; pad > 0; pad--)
					uri += "0";
				uri += hexstr;
			}
		}
		return uri;
	}

	/**
	 * Method that converts a literal string into its appropriate N-Triple representation
	 * @param s - the literal string
	 * @param writer - a string that holds the N-Triple representation
	 * @return an appropriate representation of the given literal string
	 */
	public static String writeString(String s, String writer) 
	{
		String uri = "";
		for (int i = 0; i < s.length(); i++) 
		{
			char c = s.charAt(i);
			if (c == '\\' || c == '"') 
			{
					uri += "" + '\\';
					uri += "" + c;
			} 
			else 
				if (c == '\n')
					uri += "\\n";
			 	else 
			 		if (c == '\r')
			 			uri += "\\r";
			 		else 
			 			if (c == '\t')
			 				uri += "\\t";
			 			else 
			 				if (c >= 32 && c < 127)
			 					uri += "" + c;
			 				else 
			 				{
			 					String hexstr = Integer.toHexString(c).toUpperCase();
			 					int pad = 4 - hexstr.length();
			 					uri += "\\u";
			 					for (; pad > 0; pad--)
			 						uri += "0";
			 					uri += hexstr;
			 				}
		}
		return uri;
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