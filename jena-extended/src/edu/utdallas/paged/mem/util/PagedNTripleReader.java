package edu.utdallas.paged.mem.util;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.SyntaxError;

/**
 * A class that is used to convert the N-Triples stored in the Lucene indices into a Triple format
 * @author vaibhav
 */
public class PagedNTripleReader 
{
	protected static boolean inErr = false;
	protected static final int sbLength = 200;
	private static RDFErrorHandler errorHandler = new RDFDefaultErrorHandler();

	/** Constructor **/
	private PagedNTripleReader()	{}

	/** 
	 * Method to convert a N-Triple to the Triple format
	 * @param triple - the given N-Triple as a string
	 * @return the converted Triple
	 */
	public static Triple readNTriple(String triple)
	{
		Triple t = unwrappedReadRDFTriple(triple);
		return t;
	}

	/**
	 * Logger method for errors
	 * @param s - the error message
	 */
	private static void deprecated(String s) 
	{ errorHandler.warning( new SyntaxError( syntaxErrorMessage("Deprecation warning", s))); }

	/**
	 * Logger method for errors
	 * @param s - the error message
	 */
	private static void syntaxError(String s) 
	{ errorHandler.error( new SyntaxError( syntaxErrorMessage( "Syntax error", s ))); }

	/**
	 * A method to print the kind of syntax error
	 * @param sort - the kind of error
	 * @param msg - the error message
	 * @return String combining the kind of error with the error message
	 */
	private static String syntaxErrorMessage( String sort, String msg ) 
	{ return sort + ": " + msg; }

	/**
	 * Method for converting the given N-Triple into Triple format
	 * @param triple - the Triple as a string
	 * @return the converted Triple
	 */
	private static final Triple unwrappedReadRDFTriple(String triple) 
	{
		String subject;
		String predicate = null;
		String object; 
		String temp = "";
		Node blankSub = null, blankObj = null;		

		String[] tripleSplit = triple.split(" ");
		subject = readResourceForTriple(tripleSplit[0]);
		if( subject.charAt(0) == '_' && subject.charAt(1) == ':' )
			blankSub = Node.createAnon( new AnonId( subject.substring(2, subject.length())) );
		try { predicate = getURI(readResourceForTriple(tripleSplit[1])); } catch (Exception e1) { }
		for( int i = 2; i < (tripleSplit.length-1) && tripleSplit.length > 3; i++)
		{ 
			if( !( i == (tripleSplit.length-2) ) ) temp += tripleSplit[i] + " ";
			else temp += tripleSplit[i];
		}
		if(temp.equalsIgnoreCase("")) temp = tripleSplit[2];
		object = readNodeForTriple(temp);
		if( object.charAt(0) == '_' && object.charAt(1) == ':' )
			blankObj = Node.createAnon( new AnonId( object.substring(2, object.length()) ) );
		try 
		{
			Triple t = null;

			if( blankSub == null && blankObj == null )
				t = Triple.create(NodeCreateUtils.create(PrefixMapping.Standard, subject.trim()), 
						NodeCreateUtils.create(PrefixMapping.Standard, predicate.trim()), 
						NodeCreateUtils.create(PrefixMapping.Standard, object.trim()));
			else
				if( blankSub != null )
					t = Triple.create(blankSub, 
							NodeCreateUtils.create(PrefixMapping.Standard, predicate.trim()), 
							NodeCreateUtils.create(PrefixMapping.Standard, object.trim()));
				else
					if( blankObj != null )
						t = Triple.create(NodeCreateUtils.create(PrefixMapping.Standard, subject.trim()), 
								NodeCreateUtils.create(PrefixMapping.Standard, predicate.trim()), blankObj);
					else
						t = Triple.create(blankSub, NodeCreateUtils.create(PrefixMapping.Standard, predicate.trim()), blankObj);
			return t;
		} 
		catch (Exception e2) {return null;}
	}

	/**
	 * Method to get the anonymous name for the subject of a Triple
	 * @param subject - the subject of the N-Triple
	 * @return a string representation of the subject as a Triple
	 */
	private static String readName(String subject) 
	{
		StringBuffer name = new StringBuffer(sbLength);

		for(int i=2; i<subject.length(); i++)
		{
			char c = subject.charAt(i);
			if( c != ' ') name.append(c);
		}
		return name.toString();
	}

	/**
	 * Method to get the URI for the subject of a Triple
	 * @param subject - the subject of the N-Triple
	 * @return a string representation of the subject as a Triple
	 */
	private static String readURI(String subject) 
	{
		StringBuffer uri = new StringBuffer(sbLength);

		for(int i=1; i<subject.length(); i++)
		{
			char c = subject.charAt(i);
			if( c == '\\' && subject.charAt(i+1) == 'u')
				c = readUnicode4Escape(subject, i+1);
			if( c == '>')
				break;
			uri = uri.append(c);
		}
		return uri.toString();
	}

	/**
	 * Method that returns the character represented by unicode characters
	 * @param subject - the subject of the N-Triple
	 * @param i - the position from where we need to begin checking
	 * @return the char represented by the unicode characters
	 */
	private static char readUnicode4Escape(String subject, int i) 
	{
		char buf[] = new char[] { subject.charAt(i+1), subject.charAt(i+2), subject.charAt(i+3), subject.charAt(i+4) };
		try 
		{
			return (char) Integer.parseInt(new String(buf), 16);
		} 
		catch (NumberFormatException e) 
		{
			syntaxError("bad unicode escape sequence");
			return 0;
		}
	}

	/**
	 * Method that reads the language if specified
	 * @param object - the object of the N-Triple
	 * @return a string representation of the subject as a Triple
	 */
	private static String readLang(String object) 
	{
		StringBuffer lang = new StringBuffer(15);

		for(int i=5; i<object.length(); i++)
		{
			char c = object.charAt(i);
			if( c == ' ' || c == '.' || c == '^')
				break;
			lang = lang.append(c);
		}
		return lang.toString();
	}

	/**
	 * Method that gets the string representation of the URI
	 * @param n - the string whose URI is to be determined
	 * @return a string representing the URI
	 */
	public static String getURI(String n) 
	{
		Node node = NodeCreateUtils.create(PrefixMapping.Standard, n.trim());
		return node.isBlank() ? null : node.getURI();
	}

	/**
	 * Method that converts the subject of the N-Triple into the representation for a Triple
	 * @param subject - the subject of the N-Triple
	 * @return a string representation of the subject
	 */
	public static String readResourceForTriple(String subject)  
	{
		char inChar = subject.charAt(0);

		if (inChar == '_') 
		{ // anon resource
			if (!(subject.charAt(1) == (':')))
				return null;
			String name = readName(subject);
			if (name == null) 
			{
				syntaxError("expected bNode label");
				return null;
			}
			return createResourceForTriple(name);
		} 
		else 
			if (inChar == '<') 
			{ // uri
				String uri = readURI(subject);
				if (uri == null) 
				{
					inErr = true;
					return null;
				}
				inChar = subject.charAt(subject.length()-1);
				if (inChar != '>') 
				{
					syntaxError("expected '>'");
					return null;
				}
				return makeURI(uri).toString();
			} 
			else 
			{
				syntaxError("unexpected input");
				return null;
			}
	}

	/**
	 * Method that converts the given string into a resource
	 * @param name - the given name
	 * @return a string representing the resource for a given string 
	 */
	private static String createResourceForTriple(String name)
	{
		String rtString = "_:"; 
		for(int i=1; i<name.length(); )
		{
			char c = name.charAt(i);
			if(c == 'X') 
			{ if(Integer.parseInt((name.substring(i+1, i+3)), 16) == 58)
			{ rtString += ":"; i+=4; }
			else
				if(Integer.parseInt((name.substring(i+1, i+3)), 16) == 45) 
				{ rtString += "-"; i+=4; }
			}
			else
			{ rtString += c; i++; }
		}
		return rtString;
	}

	/**
	 * Method that converts a node object of a N-Triple into Triple format
	 * @param object - the object of the N-Triple
	 * @return a string representation of the N-Triple in Triple format
	 */
	public static String readNodeForTriple(String object)  
	{
		switch (object.charAt(0)) 
		{
		case '"' :	return readLiteralForTriple(object, false);
		case 'x' :	return readLiteralForTriple(object, true);
		case '<' :
		case '_' :	return readResourceForTriple(object);
		default  :	syntaxError("unexpected input");
		return null;
		}
	}

	/**
	 * Method that converts a literal object of a N-Triple into Triple format
	 * @param object - the object of the N-Triple
	 * @param wellFormed - true, iff the object is well formed, false otherwise
	 * @return a string representation of the N-Triple in Triple format 
	 */
	private static String readLiteralForTriple(String object, boolean wellFormed)  
	{
		StringBuffer lit = new StringBuffer(sbLength);

		if (wellFormed) 
		{
			deprecated("Use ^^rdf:XMLLiteral not xml\"literals\", .");

			if (!object.substring(1, 4).equalsIgnoreCase("xml"))
				return null;
		}

		if (object.charAt(0) != ('"'))
			return null;
		int i = 1;
		while (true) 
		{
			if( i == object.length() ) break;
			char inChar = object.charAt(i);
			if (inChar == '\\') 
			{
				char c = object.charAt(i+1);
				if (c == 'n') 
					inChar = '\n';
				else 
					if (c == 'r')
						inChar = '\r';
					else 
						if (c == 't')
							inChar = '\t';
						else 
							if (c == '\\' || c == '"')
								inChar = c;
							else 
								if (c == 'u')
									inChar = readUnicode4Escape(object, i+2);
				if (inErr)
					return null;
				else 
				{
					syntaxError("illegal escape sequence '" + c + "'");
					return null;
				}
			} 
			else if (inChar == '"') 
			{
				String lang;
				if ('@' == object.charAt(i)) 
				{
					lang = readLang(object);
				} 
				else if ('-' == object.charAt(i)) 
				{
					deprecated("Language tags should be introduced with @ not -.");
					lang = readLang(object);
				} 
				else 
				{
					lang = "";
				}

				if (wellFormed) 
				{
					return Node.createLiteral( lit.toString(), "", wellFormed).toString();
				} 
				else 
					if ((i+1)<object.length() && '^' == object.charAt(i+1))
					{
						String datatypeURI = null;
						if (!object.substring(i+1, i+4).equalsIgnoreCase("^^<")) 
						{
							syntaxError("ill-formed datatype");
							return null;
						}
						datatypeURI = readURI(object.substring( i+3, object.length() - 1));
						if (datatypeURI == null || object.charAt( object.length() - 1 ) != '>')
							return null;
						if ( lang.length() > 0 )
							deprecated("Language tags are not permitted on typed literals.");

						return ('"' + lit.toString() + '"' + datatypeURI);

					} else {
						return Node.createLiteral(lit.toString(), lang,false).toString();
					}
			}
			lit = lit.append(inChar);
			i++;
		}
		return lit.toString();
	}

	/**
	 * Method that creates a typed literal
	 * @param lex - the lexical value
	 * @param typeURI - the URI
	 * @return a string representation
	 */
	public static String createTypedLiteral(String lex, String typeURI)  
	{
		RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(typeURI);
		LiteralLabel ll = LiteralLabelFactory.createLiteralLabel( lex, "", dt );
		return Node.createLiteral(ll).toString();
	}

	/**
	 * Method that creates a URI given its string representation
	 * @param uri - a string representation of the URI
	 * @return a node representing the URI
	 */
	private static Node makeURI(String uri) 
	{ return uri == null ? Node.createAnon() : Node.createURI( uri ); }
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