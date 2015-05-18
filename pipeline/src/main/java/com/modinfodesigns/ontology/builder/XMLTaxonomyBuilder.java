package com.modinfodesigns.ontology.builder;

import com.modinfodesigns.ontology.ITaxonomyNode;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.transform.xml.XMLParserTransform;

public abstract class XMLTaxonomyBuilder implements ITaxonomyBuilder
{
  private String taxonomyFile;
    
  public void setTaxonomyFile( String taxonomyFile )
  {
    this.taxonomyFile = taxonomyFile;
  }
    
  @Override
  public ITaxonomyNode buildTaxonomy()
  {
    XMLParserTransform xpt = new XMLParserTransform( );
    return buildTaxonomy( xpt.createDataObject( getTaxonomyXML( ) ) );
  }

  @Override
  public abstract ITaxonomyNode buildTaxonomy(DataObject context);

	
  private String getTaxonomyXML( )
  {
    return null;
  }
}
