package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.compare.IPropertyMatcher;

import com.modinfodesigns.security.IUserCredentials;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs a conditional transform of an IProperty based on a set of Property Comparator/Property Transform beans. The first 
 * ComparatorTransformBean that matches the property will provide its Property Transform to do the transformation task for the
 * Conditional transform.
 * 
 * @author Ted Sullivan
 */

public class ConditionalPropertyTransform extends BasePropertyTransform implements IPropertyTransform
{
  private transient static final Logger LOG = LoggerFactory.getLogger( ConditionalPropertyTransform.class );

  private List<ConditionalTransformComparator> comparatorTransforms;
	
  private String mode = "ALL_MATCHING";  // ALL_MATCHING | FIRST_ONLY
	
  public void addComparatorTransform( ConditionalTransformComparator comparatorTransform )
  {
    LOG.debug( "addComparatorTransform" );
    comparatorTransforms.add( comparatorTransform );
  }
	
  public void setMode( String mode )
  {
    this.mode = mode;
  }

  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    LOG.debug( "transform" );
		
    if (comparatorTransforms == null) throw new PropertyTransformException( "No Property Transform!" );
		
    IUserCredentials user = getUserCredentials( );
    for (int i = 0; i < comparatorTransforms.size(); i++ )
    {
      ConditionalTransformComparator ct = comparatorTransforms.get( i );
      IPropertyMatcher propMatcher = ct.getPropertyMatcher( );

      LOG.debug( "Testing PropertyMatcher " + propMatcher );
      if (propMatcher != null && propMatcher.equals( user, input ) )
      {
        List<IPropertyTransform> pts = ct.getPropertyTransforms( );
        if (pts != null)
        {
          for (int t = 0; t < pts.size(); t++)
          {
            IPropertyTransform pt = pts.get( t );
            input = pt.transform( input );
          }
          if (mode.equals( "FIRST_ONLY" ))
          {
            return input;
          }
        }
      }
      else
      {
        LOG.error( "PropertyMatcher was null!" );
		    	
      }
    }
		
    return input;
  }
	
  protected IUserCredentials getUserCredentials( )
  {
    // use SessionManager ...
    return null;
  }
	
  public void setComparatorTransforms( List<ConditionalTransformComparator> compTransforms )
  {
    this.comparatorTransforms = compTransforms;
  }

}
