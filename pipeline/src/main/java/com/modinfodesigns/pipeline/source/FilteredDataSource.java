package com.modinfodesigns.pipeline.source;

import com.modinfodesigns.app.ApplicationManager;

import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.DataObject;

import com.modinfodesigns.property.transform.IPropertyHolderTransform;
import com.modinfodesigns.property.transform.PropertyTransformException;

import com.modinfodesigns.security.IUserCredentials;

import java.util.Iterator;

/**
 * DataObjectSource with a Property Holder Transform
 * 
 * @author Ted Sullivan
 */

public class FilteredDataSource extends BaseDataObjectSource
{
  private String dataSourceName;
	
  private IDataObjectSource dataSource;
    
  private IPropertyHolderTransform propXForm;
    
  private String propTransformName;
    
  public FilteredDataSource(  ) {  }
    
  public FilteredDataSource( IDataObjectSource dataSource, IPropertyHolderTransform propertyTransform )
  {
    this.dataSource = dataSource;
    this.propXForm = propertyTransform;
  }
    
  public void setDataSourceRef( String dataSourceName )
  {
    this.dataSourceName = dataSourceName;
  }
    
  public void setDataSource( IDataObjectSource dataSource)
  {
    this.dataSource = dataSource;
  }
    
  public void setDataTransformRef( String propTransformName )
  {
    this.propTransformName = propTransformName;
  }

  public void setDataTransform( IPropertyHolderTransform propTransform )
  {
    this.propXForm = propTransform;
  }
    
  @Override
  public void run()
  {
    IDataObjectSource proxy = getDataSource( );
    proxy.run( );
  }

  @Override
  public void run( IUserCredentials withUser )
  {
    IDataObjectSource proxy = getDataSource( );
    proxy.run( withUser );
  }
	
  @Override
  protected boolean doProcessDataList( IDataList data )
  {
    for (Iterator<DataObject> dit = data.getData( ); dit.hasNext(); )
    {
      DataObject dobj = dit.next( );
      try
      {
        IPropertyHolderTransform propTransform = getPropertyHolderTransform( );
				
        if (propTransform != null)
        {
          propTransform.transformPropertyHolder( dobj );
        }
      }
      catch ( PropertyTransformException pte )
      {
        
      }
    }
		
    return super.doProcessDataList( data );
  }

  private IDataObjectSource getDataSource( )
  {
    if (dataSource != null) return dataSource;
		
    ApplicationManager appMan = ApplicationManager.getInstance( );
    return (IDataObjectSource)appMan.getApplicationObject( dataSourceName, "DataSource" );
  }
	
  private IPropertyHolderTransform getPropertyHolderTransform( )
  {
    if (propXForm != null) return propXForm;
		
    ApplicationManager appMan = ApplicationManager.getInstance( );
    return (IPropertyHolderTransform)appMan.getApplicationObject( propTransformName, "DataTransform" );
  }
}
