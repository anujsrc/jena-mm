package memmgtfail;

import java.util.Random;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC_10;
import com.hp.hpl.jena.vocabulary.VCARD;

public class CreateModelReified
{
	public CreateModelReified(){}

	public void createInMemModel(Model model, int noOfAuthors, int noOfPapers)
	{
		Resource johnSmith;
		Resource jSmith_ppr;

		//Create resources with authors
		for(int i=1; i<=noOfAuthors; i++)
		{
    		johnSmith = model.createResource("http://somewhere/johnsmith/-"+i);
    		
   			model.createReifiedStatement("http://johnSmith/FN"+i, model.createStatement(johnSmith.addProperty(VCARD.FN, i+"John Smith"), model.createProperty("Probability-FN"), "0.99"));
       		model.createReifiedStatement("http://johnSmith/Country"+i, model.createStatement(johnSmith.addProperty(VCARD.Country, i+"United States of America"), model.createProperty("Probability-Country"), "0.3"));
       		model.createReifiedStatement("http://johnSmith/Given"+i, model.createStatement(johnSmith.addProperty(VCARD.Given, i+"John"), model.createProperty("Probability-Given"), "0.3"));
       		model.createReifiedStatement("http://johnSmith/Family"+i, model.createStatement(johnSmith.addProperty(VCARD.Family, i+"Smith"), model.createProperty("Probability-Family"), "0.3"));
       		model.createReifiedStatement("http://johnSmith/Bday"+i, model.createStatement(johnSmith.addProperty(VCARD.BDAY, i+"1-Jan-1950"), model.createProperty("Probability-BDAY"), "0.99"));
       		model.createReifiedStatement("http://johnSmith/Uid"+i, model.createStatement(johnSmith.addProperty(VCARD.UID, i+"U"), model.createProperty("Probability-Uid"), "0.3"));
       		model.createReifiedStatement("http://johnSmith/Email"+i, model.createStatement(johnSmith.addProperty(VCARD.EMAIL, i+"johnsmith@utd.edu"), model.createProperty("Probability-Email"), "0.3"));
       		model.createReifiedStatement("http://johnSmith/Suffix"+i, model.createStatement(johnSmith.addProperty(VCARD.Suffix, i+"JR."), model.createProperty("Probability-Suffix"), "0.3"));
       		model.createReifiedStatement("http://johnSmith/Prefix"+i, model.createStatement(johnSmith.addProperty(VCARD.Prefix, i+"Dr."), model.createProperty("Probability-Prefix"), "0.3"));
       		model.createReifiedStatement("http://johnSmith/Pobox"+i, model.createStatement(johnSmith.addProperty(VCARD.Pobox, i+"10"), model.createProperty("Probability-Pobox"), "0.3"));
       		model.createReifiedStatement("http://johnSmith/Nickname"+i, model.createStatement(johnSmith.addProperty(VCARD.NICKNAME, i+"Johnny"), model.createProperty("Probability-Nickname"), "0.3"));
       		model.createReifiedStatement("http://johnSmith/Role"+i, model.createStatement(johnSmith.addProperty(VCARD.ROLE, i+"Sys Developer"), model.createProperty("Probability-Role"), "0.3"));
       		model.createReifiedStatement("http://johnSmith/Tel"+i, model.createStatement(johnSmith.addProperty(VCARD.TEL, i+"214-220-2232"), model.createProperty("Probability-Tel"), "0.3"));
       		model.createReifiedStatement("http://johnSmith/Locality"+i, model.createStatement(johnSmith.addProperty(VCARD.Locality, i+"Inglewood"), model.createProperty("Probability-Locality"), "0.3"));
       		model.createReifiedStatement("http://johnSmith/Adr"+i, model.createStatement(johnSmith.addProperty(VCARD.ADR, i+"2312 Preston Rd, Dallas, Tx, 75272"), model.createProperty("Probability-Adr"), "0.3"));
        	model.createReifiedStatement("http://johnSmith/Street"+i, model.createStatement(johnSmith.addProperty(VCARD.Street, i+"Preston Rd"), model.createProperty("Probability-Street"), "0.3"));
        	model.createReifiedStatement("http://johnSmith/Orgname"+i, model.createStatement(johnSmith.addProperty(VCARD.Orgname, i+"UT Dallas"), model.createProperty("Probability-Orgname"), "0.3"));
        	model.createReifiedStatement("http://johnSmith/Region"+i, model.createStatement(johnSmith.addProperty(VCARD.Region, i+"East Dallas"), model.createProperty("Probability-Region"), "0.3"));
        	model.createReifiedStatement("http://johnSmith/Pcode"+i, model.createStatement(johnSmith.addProperty(VCARD.Pcode, i+"75272"), model.createProperty("Probability-Pcode"), "0.3"));
        	model.createReifiedStatement("http://johnSmith/Group"+i, model.createStatement(johnSmith.addProperty(VCARD.GROUP, i+"Developer"), model.createProperty("Probability-Developer"), "0.3"));
        	model.createReifiedStatement("http://johnSmith/Orgunit"+i, model.createStatement(johnSmith.addProperty(VCARD.Orgunit, i+"UT Dallas - Developer"), model.createProperty("Probability-Orgunit"), "0.3"));
        	model.createReifiedStatement("http://johnSmith/Key"+i, model.createStatement(johnSmith.addProperty(VCARD.KEY, i+"112-343-3434"), model.createProperty("Probability-Key"), "0.3"));
        	model.createReifiedStatement("http://johnSmith/Prodid"+i, model.createStatement(johnSmith.addProperty(VCARD.PRODID, i+"1"), model.createProperty("Probability-Prodid"), "0.3"));
        	model.createReifiedStatement("http://johnSmith/Label"+i, model.createStatement(johnSmith.addProperty(VCARD.LABEL, i+"Key member"), model.createProperty("Probability-Label"), "0.3"));
        	model.createReifiedStatement("http://johnSmith/Note"+i, model.createStatement(johnSmith.addProperty(VCARD.NOTE, i+"Important contributor"), model.createProperty("Probability-Note"), "0.3"));
		}
		System.out.println("finished adding authors");
		
		Random numGen = new Random();
		
		for(int j=1; j<=noOfPapers; j++)
		{
			int rno1 = numGen.nextInt(noOfAuthors)+1;
			int rno2 = numGen.nextInt(noOfAuthors)+1;
			String date = (numGen.nextInt(12)+1)+"-"+(numGen.nextInt(28)+1)+"-"+(numGen.nextInt(38)+1970);
			jSmith_ppr	= model.createResource("http://somewhere/johnsmith/ppr-"+j);
			
			model.createReifiedStatement("http://johnSmith/ppr"+j+"/creator", model.createStatement(jSmith_ppr.addProperty(DC_10.creator, model.getResource("http://somewhere/johnsmith/-"+rno1)), model.createProperty("Probability-creator"), "0.3"));
			model.createReifiedStatement("http://johnSmith/ppr"+j+"/contributor", model.createStatement(jSmith_ppr.addProperty(DC_10.contributor, model.getResource("http://somewhere/johnsmith/-"+rno2)), model.createProperty("Probability-contributor"), "0.3"));
			model.createReifiedStatement("http://johnSmith/ppr"+j+"/date", model.createStatement(jSmith_ppr.addProperty(DC_10.date, date), model.createProperty("Probability-date"), "0.3"));
			model.createReifiedStatement("http://johnSmith/ppr"+j+"/description", model.createStatement(jSmith_ppr.addProperty(DC_10.description, j+"Sem Web 1"), model.createProperty("Probability-description"), "0.3"));
			model.createReifiedStatement("http://johnSmith/ppr"+j+"/format", model.createStatement(jSmith_ppr.addProperty(DC_10.format, j+"PDF"), model.createProperty("Probability-format"), "0.3"));
			model.createReifiedStatement("http://johnSmith/ppr"+j+"/identifier", model.createStatement(jSmith_ppr.addProperty(DC_10.identifier, "ppr"+j), model.createProperty("Probability-identifier"), "0.3"));
			model.createReifiedStatement("http://johnSmith/ppr"+j+"/language", model.createStatement(jSmith_ppr.addProperty(DC_10.language, j+"english"), model.createProperty("Probability-language"), "0.3"));
			model.createReifiedStatement("http://johnSmith/ppr"+j+"/publisher", model.createStatement(jSmith_ppr.addProperty(DC_10.publisher, j+"IEEE Computer Society"), model.createProperty("Probability-publisher"), "0.3"));
			model.createReifiedStatement("http://johnSmith/ppr"+j+"/relation", model.createStatement(jSmith_ppr.addProperty(DC_10.relation, j+""), model.createProperty("Probability-relation"), "0.3"));
			model.createReifiedStatement("http://johnSmith/ppr"+j+"/rights", model.createStatement(jSmith_ppr.addProperty(DC_10.rights, j+"Free to copy for class purposes"), model.createProperty("Probability-rights"), "0.3"));
			model.createReifiedStatement("http://johnSmith/ppr"+j+"/source", model.createStatement(jSmith_ppr.addProperty(DC_10.source, j+""), model.createProperty("Probability-source"), "0.3"));
			model.createReifiedStatement("http://johnSmith/ppr"+j+"/subject", model.createStatement(jSmith_ppr.addProperty(DC_10.subject, j+"Semantic Web"), model.createProperty("Probability-subject"), "0.3"));
			model.createReifiedStatement("http://johnSmith/ppr"+j+"/title", model.createStatement(jSmith_ppr.addProperty(DC_10.title, j+"Introductory topics to semantic web"), model.createProperty("Probability-title"), "0.3"));
			model.createReifiedStatement("http://johnSmith/ppr"+j+"/type", model.createStatement(jSmith_ppr.addProperty(DC_10.type, j+"Computer Science/Semantic Web"), model.createProperty("Probability-type"), "0.3"));
		}
		System.out.println("finished adding papers");
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