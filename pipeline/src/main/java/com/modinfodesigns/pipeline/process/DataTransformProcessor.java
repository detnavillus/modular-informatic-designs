package com.modinfodesigns.pipeline.process;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.transform.IPropertyHolderTransform;
import com.modinfodesigns.property.transform.PropertyTransformException;

import com.modinfodesigns.search.IResultListFilter;
import com.modinfodesigns.search.IResultList;

import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs one or more Property Transform operations
 * 
 * @author Ted Sullivan
 */

public class DataTransformProcessor implements IDataObjectProcessor, IResultListFilter
{
  private transient static final Logger LOG = LoggerFactory.getLogger( DataTransformProcessor.class );

  private ArrayList<IPropertyHolderTransform> transformList = new ArrayList<IPropertyHolderTransform>( );
	
  private String name;
	
  public void setName( String name )
  {
    this.name = name;
  }
	
  public String getName( )
  {
    return this.name;
  }
	
  public void addDataTransform( IPropertyHolderTransform transform )
  {
    LOG.debug( "Adding Data Transform : " + transform );
    transformList.add( transform );
  }

  @Override
  public IDataList processDataList( IDataList data )
  {
    LOG.debug( "processDataList( ) ..." );
		
    for (Iterator<DataObject> dit = data.getData( ); dit.hasNext(); )
    {
      DataObject dobj = dit.next();
      for (int i = 0; i < transformList.size( ); i++)
      {
        IPropertyHolderTransform transform = transformList.get( i );
        try
        {
          transform.transformPropertyHolder( dobj );
        }
        catch (PropertyTransformException pte )
        {
          LOG.error( "IPropertyHolderTransform " + transform + " threw PropertyTransformException " + pte );
        }
      }
    }
		
    return data;
  }

  @Override
  public void processComplete( IPropertyHolder result, boolean status)
  {

  }

  @Override
  public IResultList processResultList(IResultList data)
  {
    processDataList( data );
    return data;
  }

}
