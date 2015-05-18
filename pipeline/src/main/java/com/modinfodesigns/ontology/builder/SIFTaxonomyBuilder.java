package com.modinfodesigns.ontology.builder;

import com.modinfodesigns.ontology.ITaxonomyNode;
import com.modinfodesigns.ontology.TaxonomyNode;
import com.modinfodesigns.ontology.TaxonomyRootNode;
import com.modinfodesigns.property.DataObject;

public class SIFTaxonomyBuilder extends XMLTaxonomyBuilder
{
  public static final String TREE_NODE = "TreeNode";

  @Override
  public ITaxonomyNode buildTaxonomy( DataObject context )
  {
    TaxonomyRootNode taxoRoot = new TaxonomyRootNode( );
    // use XPath syntax to get at the name, ID of taxoRoot
		
    addProperties( taxoRoot, context );
    addChildNodes( taxoRoot, context );
    return taxoRoot;
  }
	
  private void addProperties( TaxonomyRootNode taxoRoot, DataObject context )
  {
    	
  }

  private void addChildNodes( TaxonomyNode parent, DataObject context )
  {
    	
  }
}
