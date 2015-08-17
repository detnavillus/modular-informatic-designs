package com.modinfodesigns.ontology.builder;

import com.modinfodesigns.ontology.ITaxonomyNode;
import com.modinfodesigns.ontology.TaxonomyNode;
import com.modinfodesigns.property.DataObject;

public class OWLTaxonomyBuilder extends XMLTaxonomyBuilder
{

  @Override
  public ITaxonomyNode buildTaxonomy( DataObject context )
  {
    TaxonomyNode rootNode = new TaxonomyNode( );
      
    // get all sub docs named "Class"
    // doc may have a sub doc named: "subClassOf" - if so, find this in the Taxonomy and add the sub doc to it
      
    // NamedIndividual - instances of classes - look for rdf:type subtag to find parent node
    // get attributes as children nodes - 
    
      
    return rootNode;
  }

}
