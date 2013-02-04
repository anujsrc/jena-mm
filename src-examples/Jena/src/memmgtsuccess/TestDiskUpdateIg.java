package memmgtsuccess;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.VCARD;

import edu.utdallas.paged.rdf.model.ExtendedModelFactory;
import edu.utdallas.paged.shared.impl.ExtendedJenaParameters;

public class TestDiskUpdateIg
{
	private static CreateModel cm = new CreateModel();
	private static int noOfAuthors = 0;
	private static int noOfPapers = 0;
	private static long startTime = 0L, endTime = 0L;

	public static void main(String[] args)
	{
		noOfAuthors = Integer.parseInt(args[0]);
		noOfPapers = Integer.parseInt(args[1]);
		System.out.println("no of authors = " + noOfAuthors + " no Of Papers = " + noOfPapers);

		ExtendedJenaParameters.initialThreshold = 10000;
		Model model = ExtendedModelFactory.createVirtMemModel();

		//create an in-memory model
		startTime = System.currentTimeMillis();
		cm.createInMemModel(model, noOfAuthors, noOfPapers);
		endTime = System.currentTimeMillis();
		System.out.println("total time for creating model = " + ((endTime - startTime)/1000L) + " seconds.");

		//list all statements in a model
		long count = 0;
		com.hp.hpl.jena.rdf.model.StmtIterator iter2 = null;
		
		// For disk
		//list a particular statement in a model
		startTime = System.currentTimeMillis();
		iter2 = model.listStatements(model.getResource("http://somewhere/johnsmith/-1"), VCARD.FN, "John Smith-1");
		count = 0;
		Statement stmt = null;                
		while(iter2.hasNext())
		{ count++;  stmt = iter2.nextStatement(); }
		iter2.close();
		endTime = System.currentTimeMillis();
		System.out.println("before changing object, search object = John Smith-1, count = " + count + ", time = " + (endTime-startTime) + " milliseconds.");

		startTime = System.currentTimeMillis();
		stmt.changeObject("John Smith-10502");
		count = 0;
		iter2 = model.listStatements(model.getResource("http://somewhere/johnsmith/-1"), VCARD.FN, "John Smith-1");
		while(iter2.hasNext())
		{ count++; stmt = iter2.nextStatement(); }
		iter2.close();
		endTime = System.currentTimeMillis();
		System.out.println("after changing object, search object = John Smith-1, count = " + count + ", time = " + (endTime-startTime) + " milliseconds.");

		startTime = System.currentTimeMillis();
		count = 0;
		iter2 = model.listStatements(model.getResource("http://somewhere/johnsmith/-1"), VCARD.FN, "John Smith-10502");
		while(iter2.hasNext())
		{ count++; stmt = iter2.nextStatement(); }
		iter2.close();
		endTime = System.currentTimeMillis();
		System.out.println("after changing object, search object = John Smith-10502, count from disk = " + count + ", time = " + (endTime-startTime) + " milliseconds.");
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