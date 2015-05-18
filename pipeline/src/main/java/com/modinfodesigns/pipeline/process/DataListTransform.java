package com.modinfodesigns.pipeline.process;

import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.IPropertyHolder;

import com.modinfodesigns.property.transform.IPropertyHolderTransform;
import com.modinfodesigns.property.transform.PropertyTransformException;

import com.modinfodesigns.search.IResultListFilter;
import com.modinfodesigns.search.IResultList;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies a Property Transform to the DataList object itself. 
 * 
 * To process each DataObject in a data list, use the DataTransformProcessor.
 * 
 * @author Ted Sullivan
 *
 */

public class DataListTransform implements IDataObjectProcessor, IResultListFilter
{
  private transient static final Logger LOG = LoggerFactory.getLogger( DataListTransform.class );

  private ArrayList<IPropertyHolderTransform> propTransforms;
    
  public void addDataTransform( IPropertyHolderTransform propTransform )
  {
    if (propTransforms == null) propTransforms = new ArrayList<IPropertyHolderTransform>( );
    propTransforms.add( propTransform );
  }
    
  @Override
  public IDataList processDataList( IDataList data )
  {
    LOG.debug( "processDataList( ): " );
    try
    {
      if (propTransforms != null)
      {
        for (int i = 0; i < propTransforms.size(); i++)
        {
          IPropertyHolderTransform propTransform = propTransforms.get( i );
          propTransform.transformPropertyHolder( data );
        }
      }
      else
      {
        LOG.error( "No Property Transforms!" );
      }
    }
    catch ( PropertyTransformException pte )
    {
      LOG.error( "Got PropertyTransformException " + pte );
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
