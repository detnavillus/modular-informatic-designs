package com.modinfodesigns.ontology.builder;

import com.modinfodesigns.ontology.ITaxonomyNode;
import com.modinfodesigns.property.DataObject;

public class OWLTaxonomyBuilder implements ITaxonomyBuilder
{
  @Override
  public ITaxonomyNode buildTaxonomy( )
  {
    return buildTaxonomy( null );
  }

  @Override
  public ITaxonomyNode buildTaxonomy( DataObject context )
  {
    return null;
  }

}
