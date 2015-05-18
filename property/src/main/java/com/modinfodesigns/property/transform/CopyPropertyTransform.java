package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopyPropertyTransform extends BasePropertyTransform implements IPropertyHolderTransform
{
  private transient static final Logger LOG = LoggerFactory.getLogger( CopyPropertyTransform.class );
	
  private String copyFrom;
	
  private String copyTo;
	
  private boolean isAdd = false;
	
  ArrayList<IPropertyTransform> propTransforms = null;
	
  public void setCopyFrom( String copyFrom )
  {
    LOG.debug( "Setting copyFrom = " + copyFrom );
    this.copyFrom = copyFrom;
  }
	
  public void setInputProperty( String input )
  {
    LOG.debug( "Setting copyFrom = " + copyFrom );
    this.copyFrom = input;
  }
	
  public void setFrom( String from )
  {
    this.copyFrom = from;
  }
	
  public void setCopyTo( String copyTo )
  {
    LOG.debug( "Setting copyTo = " + copyTo );
    this.copyTo = copyTo;
  }
	
  public void setOutputProperty( String output )
  {
    LOG.debug( "Setting copyTo = " + copyTo );
    this.copyTo = output;
  }
	
  public void setTo( String to )
  {
    this.copyTo = to;
  }
	
  public void setIsAdd( boolean isAdd )
  {
    this.isAdd = isAdd;
  }
	
  public void addPropertyTransform( IPropertyTransform propertyTransform )
  {
    LOG.debug( "addPropertyTransform: " + propertyTransform );
		
    if (propTransforms == null)
    {
      propTransforms = new ArrayList<IPropertyTransform>( );
    }
    
    propTransforms.add( propertyTransform );
  }
	
	
  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    if ( propTransforms != null )
    {
      for (int i = 0; i < propTransforms.size(); i++)
      {
        IPropertyTransform propTransform = propTransforms.get( i );
        input = propTransform.transform( input );
      }
    }
		
    return input;
  }
	

  @Override
  public IPropertyHolder transformPropertyHolder(IPropertyHolder input) throws PropertyTransformException
  {
    LOG.debug( "transformPropertyHolder ..." );
    if (copyFrom == null || copyTo == null)
    {
      LOG.error( "from or to is NULL" + copyFrom + "," + copyTo );
      return input;
    }
		
    IProperty fromProp = input.getProperty( copyFrom );
    if ( fromProp != null )
    {
      LOG.debug( "fromProp = " + fromProp.getValue() );
      IProperty toProp = fromProp.copy( );
			
      if (propTransforms != null)
      {
        for (int i = 0; i < propTransforms.size(); i++)
        {
          IPropertyTransform propTransform = propTransforms.get( i );
          toProp = propTransform.transform( toProp );
        }
      }
			
      toProp.setName( copyTo );
      if (isAdd)
      {
        LOG.debug( "Adding property: " + toProp.getName() + " = " + toProp.getValue( ) );
        input.addProperty( toProp );
      }
      else
      {
        LOG.debug( "Setting property: " + toProp.getName() + " = " + toProp.getValue( ) );
        input.setProperty( toProp );
      }
    }
    else
    {
      LOG.error( copyFrom + " is NULL!" );
    }
		
    return input;
  }

}
