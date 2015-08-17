package com.modinfodesigns.test.ontology.builder;

import com.modinfodesigns.ontology.ITaxonomyNode;

import java.io.StringReader;

import junit.framework.TestCase;

public class TestRDFOWLBuilder extends TestCase
{
  public void testMusicExample( )
  {
    RDFOWLTaxonomyBuilder rdfOwlBuilder = new RDFOWLTaxonomyBuilder( );
    ITaxonomyNode onto = rdfOwlBuilder.buildTaxonomy( new StringReader( music_exampld ) );
      
    System.out.println( "Got ontology: " + onto.toString( ) );
  }

}

private static final String music_example = "<rdf:RDF xmlns=\"http://www.semanticweb.org/tedsullivan/ontologies/2015/5/music-10#\""
                                          + "xml:base=\"http://www.semanticweb.org/tedsullivan/ontologies/2015/5/music-10\""
                                          + "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
                                          + "xmlns:owl=\"http://www.w3.org/2002/07/owl#\""
                                          + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\""
                                          + "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\""
                                          + "xmlns:music-10=\"http://www.semanticweb.org/tedsullivan/ontologies/2015/5/music-10#\">"
                                          + "<owl:Ontology rdf:about=\"http://www.semanticweb.org/tedsullivan/ontologies/2015/5/music-10\"/>"

                                          + "<owl:Class rdf:about=\"&amp;music-10;Performers\">"
                                          + "<rdfs:label>Musician</rdfs:label>"
                                          + "</owl:Class>"

                                          + "<owl:Class rdf:about=\"&amp;music-10;Band\">"
                                          + "<rdfs:label>Band</rdfs:label>"
                                          + "<rdfs:subClassOf rdf:resource=\"&amp;music-10;Performers\"/>"
                                          + "</owl:Class>"

                                          + "<owl:Class rdf:about=\"&amp;music-10;Singer\">"
                                          + "<synonym>Vocalist</synonym>"
                                          + "<rdfs:subClassOf rdf:resource=\"&amp;music-10;Performers\"/>"
                                          + "</owl:Class>"

                                          + "<owl:NamedIndividual rdf:about=\"&amp;music-10;Paul_Butterfield\">"
                                          + "<rdf:type rdf:resource=\"&amp;music-10;Singer\"/>"
                                          + "<rdfs:label>Paul Butterfield</rdfs:label>"
                                          + "<memberOfGroup rdf:resource=\"&amp;music-10;The_Paul_Butterfield_Blues_Band\"/>"
                                          + "</owl:NamedIndividual>"

                                          + "<owl:NamedIndividual rdf:about=\"&amp;music-10;The_Paul_Butterfield_Blues_Band\">"
                                          + "<rdf:type rdf:resource=\"&amp;music-10;Band\"/>"
                                          + "<rdfs:label>The Paul Butterfield Blues Band</rdfs:label>"
                                          + "</owl:NamedIndividual>"

                                          + "</rdf:RDF>";