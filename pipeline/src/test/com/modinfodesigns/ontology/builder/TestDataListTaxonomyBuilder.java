package com.modinfodesigns.ontology.builder;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.ontology.ITaxonomyNode;

import junit.framework.TestCase;

public class TestDataListTaxonomyBuilder extends TestCase
{
    
  public void testDataListTaxonomyBuilder( )
  {
    DataListTaxonomyBuilder dltb = new DataListTaxonomyBuilder( );
    dltb.setParentIDProperty( "parentID" );
    dltb.setDataSource( new MockDataObjectSource( ) );
		
    ITaxonomyNode taxo = dltb.buildTaxonomy( );
    System.out.println( taxo.getValue( IProperty.XML_FORMAT ) );
  }

}
