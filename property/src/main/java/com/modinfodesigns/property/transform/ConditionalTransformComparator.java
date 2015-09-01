package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.compare.IPropertyMatcher;
import com.modinfodesigns.property.compare.IPropertyHolderMatcher;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Wrapper class used to do conditional transforms. Contains a set of transforms and a Matcher object.
 *  Used by both ConditionalPropertyTransform and ConditionalDataTransform.  If the matcher matches the
 *  property or target object the associated transform objects are used to modify the property or target object
 *  respectively.
 *  
 * @author Ted Sullivan
 */

public class ConditionalTransformComparator
{
  private transient static final Logger LOG = LoggerFactory.getLogger( ConditionalTransformComparator.class );

  private IPropertyMatcher propMatcher;
	
  private ArrayList<IPropertyTransform> propTransforms;
  private ArrayList<IPropertyTransform> notMatchedTransforms;
	
  private ArrayList<IPropertyHolderTransform> propHolderTransforms;
  private ArrayList<IPropertyHolderTransform> notMatchedDataTransforms;
	
  private IPropertyHolderMatcher propHolderMatcher;

		
  public void addPropertyTransform( IPropertyTransform propTransform )
  {
    if (propTransforms == null) propTransforms = new ArrayList<IPropertyTransform>( );
    this.propTransforms.add( propTransform );
  }
    
  public void addNotMatchedTransform( IPropertyTransform notMatchedTransform )
  {
    if (notMatchedTransforms == null) notMatchedTransforms = new ArrayList<IPropertyTransform>( );
    notMatchedTransforms.add( notMatchedTransform );
  }
		
  public List<IPropertyTransform> getPropertyTransforms( )
  {
    return this.propTransforms;
  }

  public List<IPropertyTransform> getNotMatchedTransforms( )
  {
    return this.notMatchedTransforms;
  }
	
  public void addDataTransform( IPropertyHolderTransform propTransform )
  {
    if (propHolderTransforms == null) propHolderTransforms = new ArrayList<IPropertyHolderTransform>( );
    this.propHolderTransforms.add( propTransform );
  }
    
  public void addNotMatchedDataTransform( IPropertyHolderTransform notMatchedDataTransform )
  {
      System.out.println( "Adding not matched data transform " + notMatchedDataTransform );
    if (notMatchedDataTransforms == null) notMatchedDataTransforms = new ArrayList<IPropertyHolderTransform>( );
    notMatchedDataTransforms.add( notMatchedDataTransform );
  }
		
  public List<IPropertyHolderTransform> getDataTransforms( )
  {
    return this.propHolderTransforms;
  }

  public List<IPropertyHolderTransform> getNotMatchedDataTransforms( )
  {
    
    return this.notMatchedDataTransforms;
  }
		
  public void setPropertyMatcher( IPropertyMatcher propertyMatcher )
  {
    this.propMatcher = propertyMatcher;
  }
		
  public IPropertyMatcher getPropertyMatcher( )
  {
    return this.propMatcher;
  }
	
  public void setDataMatcher( IPropertyHolderMatcher propertyHolderMatcher )
  {
    LOG.debug( "setDataMatcher: " + propertyHolderMatcher );
    this.propHolderMatcher = propertyHolderMatcher;
  }
		
  public IPropertyHolderMatcher getDataMatcher( )
  {
    return this.propHolderMatcher;
  }

}
