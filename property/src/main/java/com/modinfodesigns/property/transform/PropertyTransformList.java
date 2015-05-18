package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.DataObject;

import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies one or more property transforms to a DataObject or to all objects (recursively) in an IDataList.
 * 
 * @author Ted Sullivan
 */
public class PropertyTransformList implements IPropertyHolderTransform
{
  private transient static final Logger LOG = LoggerFactory.getLogger( PropertyTransformList.class );

  private ArrayList<IPropertyHolderTransform> propTransforms;
    
  public void addDataTransform( IPropertyHolderTransform propTransform )
  {
    if (propTransforms == null) propTransforms = new ArrayList<IPropertyHolderTransform>( );
    propTransforms.add( propTransform );
  }
    
  @Override
  public IProperty transform(IProperty input) throws PropertyTransformException
  {
    return input;
  }



  @Override
  public IPropertyHolder transformPropertyHolder(IPropertyHolder input) throws PropertyTransformException
  {
    LOG.debug( "transformPropertyHolder( ) ..." );
    if (input instanceof IDataList)
    {
      IDataList dList = (IDataList)input;
      LOG.debug( "Processing " + dList.size() + " objects." );
      for (Iterator<DataObject> dit = dList.getData(); dit.hasNext(); )
      {
        DataObject dobj = dit.next();
        transformPropertyHolder( dobj );
      }
    }

    doTransform( input );
		
    return input;
  }
	
  private void doTransform( IPropertyHolder propHolder ) throws PropertyTransformException
  {
    if (propTransforms == null) return;
		
    for (Iterator<IPropertyHolderTransform> transformIt = propTransforms.iterator(); transformIt.hasNext(); )
    {
      IPropertyHolderTransform propTransform = transformIt.next();
      propTransform.transformPropertyHolder( propHolder );
    }
  }

  @Override
  public void startTransform( IProperty input, IPropertyTransformListener transformListener)
                              throws PropertyTransformException
  {

  }

}
