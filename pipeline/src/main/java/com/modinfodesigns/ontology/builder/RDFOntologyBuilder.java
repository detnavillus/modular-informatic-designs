package com.modinfodesigns.ontology.builder;

import com.modinfodesigns.ontology.ITaxonomyNode;
import com.modinfodesigns.ontology.TaxonomyNode;
import com.modinfodesigns.property.DataObject;

import java.util.HashMap;

public class RDFOntologyBuilder extends XMLTaxonomyBuilder
{

  @Override
  public ITaxonomyNode buildTaxonomy( DataObject context )
  {
    // pass 1 set ID = rdf:about
    HashMap<String,DataObject> nodeMap = new HashMap<String,DataObject>( );
      
    TaxonomyNode rootNode = new TaxonomyNode( );
      
    // pass 1 set ID = rdf:about
      
    // pass 2: build hierarchy with rdfs:subclassof
    // ID is rdf:about
      
    //pass 2: for rdf:resource properties lookup the DataObject set it as the Proxy object for this property
    // rdf:resource
    
      
    return rootNode;
  }

}
