package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.transform.string.IPropertyHolderRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms a Property Holder using a transform template. The template is a string that contains
 * placeholders that the transform replaces with the PropertyHolder's property values. The transform
 * uses this template to create an output String from the IPropertyHolder's property set.
 * 
 * {PROPERTY_NAME} tags are replaced with the value of the property name.  (Braces can be set to other than '{' and '}' if necessary )
 * 
 * {PROPERTY_NAME|FORMAT} tags are replaced with the value of the property in the specified format.
 * 
 * @author Ted Sullivan
 */
public class PropertyTemplateTransform extends BasePropertyTransform implements IPropertyHolderTransform, IPropertyHolderRenderer
{
  private transient static final Logger LOG = LoggerFactory.getLogger( PropertyTemplateTransform.class );

  // Special constants for IPropertyHolder and IProperty method properties:
  public static final String NAME = "_NAME_";
  public static final String TYPE = "_TYPE_";
  public static final String ID   = "_ID_";
  public static final String FORMAT = "_FORMAT_";
  public static final String IS_ROOT = "_IS_ROOT_";
  public static final String PROXY = "_PROXY_";
	
  private String transformTemplate;
  private String outputProperty;
    
  private char leftBrace  = '{';
  private char rightBrace = '}';
    
  public static final String childKeyDelimiter = ";";
    
    
  public PropertyTemplateTransform( ) {  }
    
  public PropertyTemplateTransform( String transformTemplate, String outputProperty )
  {
    this.transformTemplate = transformTemplate;
    this.outputProperty = outputProperty;
  }
    
  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input ) throws PropertyTransformException
  {
    String outputVal = transformTemplate( input );
    input.addProperty( new StringProperty( outputProperty, outputVal ) );
    return input;
  }

  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    DataObject dob = new DataObject( );
    dob.setProperty( input );
    String outputVal = transformTemplate( dob );
		
    return new StringProperty( outputProperty, outputVal );
  }
	
  public String transformTemplate( IPropertyHolder propHolder )
  {
    if (propHolder == null) return null;
		
    LOG.debug( "transform Template from: " + propHolder );
		
    StringBuilder rBuilder = new StringBuilder( );

    int start = transformTemplate.indexOf( leftBrace );
    int end   = findEnd( transformTemplate, leftBrace, rightBrace, start );
    int last = 0;
    while ( start >= 0 && end >= 0 )
    {
      rBuilder.append( new String( transformTemplate.substring( last, start ) ) );
      String key = new String( transformTemplate.substring( start + 1, end ) );
      LOG.debug( "Got key = '" + key + "'" );
      String val = null;
      String format = null;
            
      if (key.indexOf( "|" ) > 0)
      {
        format = new String( key.substring( key.indexOf( "|" ) + 1 ) );
        key = new String( key.substring( 0, key.indexOf( "|" )));
      }
            
      IProperty prop = propHolder.getProperty( key );
      if (prop != null)
      {
        if (key.equals( NAME ))
        {
          val = prop.getName( );
        }
        else if (key.equals( TYPE ))
        {
          val = prop.getType();
        }
        else if (key.equals( FORMAT ))
        {
          val = prop.getDefaultFormat();
        }
        else if (key.equals( PROXY ))
        {
          if (propHolder.getProperty( PROXY ) != null )
          {
            LOG.debug( "Setting PROXY key: " + prop.getValue( ) );
            val = prop.getValue( );
          }
          else
          {
            val = "";
          }
        }
        else
        {
          if (propHolder.getProperty( PROXY ) != null )
          {
            LOG.debug( "object is proxied - not setting values." );
            val = "";
          }
          else
          {
            val = (format != null) ? prop.getValue( format ) : prop.getValue( );
          }
        }
      }
      else if (key.equals( NAME ))
      {
        val = propHolder.getName( );
      }
      else if (key.equals( TYPE ))
      {
        val = propHolder.getType();
      }
      else if (key.equals( ID ) )
      {
        val = propHolder.getID( );
      }
      else if (key.equals( IS_ROOT ))
      {
        LOG.debug( propHolder + " Checking isRootObject( ) " + propHolder.isRootObject( ) );
        val = (propHolder.isRootObject() ) ? "T" : "F";
      }
            
      LOG.debug( "val = " + val );
            
      if (val == null) val = "";
            
      rBuilder.append( val );
        
      last = end + 1;
      start = transformTemplate.indexOf( leftBrace, last );
      end   = findEnd( transformTemplate, leftBrace, rightBrace, start );
    }
        
    if (last < transformTemplate.length() )
    {
      rBuilder.append(new String( transformTemplate.substring( last, transformTemplate.length() ) ) );
    }
        
    return rBuilder.toString();
  }
	
  private int findEnd( String str, char startCh, char endCh, int start )
  {
    int i = start + 1;
    for ( int nestCount = 1; nestCount > 0 && i < str.length(); ++i )
    {
      if      (str.charAt( i ) == startCh ) ++nestCount;
      else if (str.charAt( i ) == endCh )   --nestCount;
    }

    return i-1;
  }
    
  // Java Bean Methods
	
  public void setTransformTemplate( String transformTemplate )
  {
    this.transformTemplate = transformTemplate;
  }
	
  public void setOutputProperty( String outputProperty )
  {
    this.outputProperty = outputProperty;
  }
	
  public void setLeftBrace( char leftBrace )
  {
    this.leftBrace = leftBrace;
  }
	
  public void setRightBrace( char rightBrace )
  {
    this.rightBrace = rightBrace;
  }

  @Override
  public String renderProperty(IProperty property)
  {
    return null;
  }

  @Override
  public String renderPropertyHolder( IPropertyHolder propHolder )
  {
    return transformTemplate( propHolder );
  }

}
