package com.modinfodesigns.property.transform;

import java.util.ArrayList;
import java.util.List;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.compare.IPropertyHolderMatcher;
import com.modinfodesigns.security.IUserCredentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs a conditional transform of an IPropertyHolder (DataObject) based on a set of Property Comparator/Property Transform beans. The first 
 * ConditionalTransformComparator that matches the data object will provide its set of PropertyHolder Transforms to do the transformation task for the
 * Conditional transform.
 * 
 * @author Ted Sullivan
 */
public class ConditionalDataTransform extends ConditionalPropertyTransform implements IPropertyHolderTransform
{
  private transient static final Logger LOG = LoggerFactory.getLogger( ConditionalDataTransform.class );

  private ArrayList<ConditionalTransformComparator> comparatorTransforms;
	
  private String mode = "ALL_MATCHING";  // ALL_MATCHING | FIRST_ONLY
	
  public void addConditionalTransformComparator( ConditionalTransformComparator comparatorTransform )
  {
    LOG.debug( "addConditionalTransformComparator " + comparatorTransform );
    if (comparatorTransforms == null) comparatorTransforms = new ArrayList<ConditionalTransformComparator>( );
    comparatorTransforms.add( comparatorTransform );
  }
	
  public void setMode( String mode )
  {
    this.mode = mode;
  }


  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input )
         throws PropertyTransformException
  {
    if (comparatorTransforms == null) throw new PropertyTransformException( "No Property Transform!" );
		
    IUserCredentials user = getUserCredentials( );
    for (int i = 0; i < comparatorTransforms.size(); i++ )
    {
      ConditionalTransformComparator ct = comparatorTransforms.get( i );
      IPropertyHolderMatcher propHolderMatcher = ct.getDataMatcher( );

      LOG.debug( "Testing with " + propHolderMatcher );
      if (propHolderMatcher != null && propHolderMatcher.equals( user, input ) )
      {
        List<IPropertyHolderTransform> pts = ct.getDataTransforms( );
        if (pts != null)
        {
          for (int t = 0; t < pts.size(); t++)
          {
            IPropertyHolderTransform pt = pts.get( t );
            input = pt.transformPropertyHolder( input );
          }
			        
          if (mode.equals( "FIRST_ONLY" ))
          {
            return input;
          }
        }
        else
        {
          LOG.error( "ConditionalDataTransform not have an IPropertyHolderTransform!" );
        }
      }
      else
      {
        LOG.debug( "propertyHolderMatcher " + propHolderMatcher + " does NOT match!"  );
      }
    }
		
    return input;
  }

  @Override
  public void startTransform(IProperty input, IPropertyTransformListener transformListener)
        throws PropertyTransformException
  {

  }

}
