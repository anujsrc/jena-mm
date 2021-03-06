package sp2b.mem;

import java.io.FileInputStream;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.utdallas.paged.query.PagedQueryExecutionFactory;
import edu.utdallas.paged.rdf.model.ExtendedModelFactory;
import edu.utdallas.paged.shared.impl.ExtendedJenaParameters;

public class Sp2bBenchQ1 
{
	public static void main(String[] args)
	{
		ExtendedJenaParameters.initialThreshold = 7500;
		Model model = ExtendedModelFactory.createVirtMemModel();
		try
		{
			long startTime = System.currentTimeMillis(), endTime = 0L;
			model.read(new FileInputStream(args[0]), null, "N3");
			endTime = System.currentTimeMillis();
			System.out.println("time to read the model = " + (endTime-startTime)/1000 + " seconds.");
			startTime = System.currentTimeMillis();
			String queryString = 
			" PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			" PREFIX dc:      <http://purl.org/dc/elements/1.1/> " +
			" PREFIX dcterms: <http://purl.org/dc/terms/> " +
			" PREFIX bench:   <http://localhost/vocabulary/bench/> " +
			" PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#> " +
			" SELECT ?yr " +
			" WHERE { " +
			" ?journal rdf:type bench:Journal . " +
			" ?journal dc:title \"Journal 1 (1940)\"^^xsd:string . " +
			" ?journal dcterms:issued ?yr " +
			" } ";
			
			QueryExecution qexec = PagedQueryExecutionFactory.create(queryString, model);
			ResultSet rs = qexec.execSelect();
			while( rs.hasNext() )
			{
				QuerySolution rb = rs.nextSolution();
				RDFNode x = rb.getLiteral("?yr"); 
				System.out.println("year of publication is: " + x.toString());
			}
			qexec.close();
			endTime = System.currentTimeMillis();
			System.out.println("time to query = " + (endTime-startTime) + " milliseconds.");
		}
		catch(Exception e) { e.printStackTrace(); }
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