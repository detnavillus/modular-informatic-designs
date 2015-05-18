package com.modinfodesigns.property.transform.string;

import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.DataObject;

import com.modinfodesigns.property.DataObjectTemplate;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataListTemplateRenderer implements IDataListRenderer 
{
  private transient static final Logger LOG = LoggerFactory.getLogger( DataListTemplateRenderer.class );

  private String header;
  private String dataTemplate;
  private String footer;
    
  public void setHeader( String header )
  {
    this.header = header;
  }
    
  public void setDataTemplate( String dataTemplate )
  {
    this.dataTemplate = dataTemplate;
  }
    
  public void setFooter( String footer )
  {
    this.footer = footer;
  }
    
    
  @Override
  public String renderDataList( IDataList dList )
  {
    LOG.debug( "renderDataList( )..." );
		
    StringBuilder sbr = new StringBuilder( );
		
    if (header != null)
    {
      sbr.append( header );
    }
		
    Iterator<DataObject> dobjIt = dList.getData( );
    while ( dobjIt != null && dobjIt.hasNext( ) )
    {
      DataObjectTemplate dot = new DataObjectTemplate( dataTemplate,  dobjIt.next() );
      sbr.append( dot.getValue( ) );
    }
		
    if (footer != null)
    {
      sbr.append( footer );
    }
		
    return sbr.toString( );
  }

}
