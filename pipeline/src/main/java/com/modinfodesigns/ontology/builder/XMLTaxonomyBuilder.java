package com.modinfodesigns.ontology.builder;

import com.modinfodesigns.ontology.ITaxonomyNode;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.transform.xml.XMLParserTransform;

import com.modinfodesigns.utils.FileMethods;

import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class XMLTaxonomyBuilder implements ITaxonomyBuilder
{
  private transient static final Logger LOG = LoggerFactory.getLogger( XMLTaxonomyBuilder.class );
    
  private String taxonomyFile;
  private String taxonomyXML;
    
  public XMLTaxonomyBuilder( ) { }
    
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
    
  public ITaxonomyNode buildTaxonomy( Reader taxoInput )
  {
    XMLParserTransform xpt = new XMLParserTransform( );
    return buildTaxonomy( xpt.createDataObject( taxoInput ) );
  }

  @Override
  public abstract ITaxonomyNode buildTaxonomy( DataObject context );

	
  private String getTaxonomyXML( )
  {
    if (taxonomyXML != null)
    {
      return taxonomyXML;
    }
    LOG.info( "Reading " + taxonomyFile );
    return FileMethods.readFile( taxonomyFile );
  }
}
