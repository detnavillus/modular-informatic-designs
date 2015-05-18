package com.modinfodesigns.property.schema;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataObjectBean;
import com.modinfodesigns.property.IComputableProperties;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.string.StringListProperty;

import com.modinfodesigns.property.quantity.IQuantity;

import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents a Property Type
 * 
 * Properties - Short Name
 *            - Class Name
 *            - Supported Formats
 *            If property ISA IComputableProperties
 *                - Intrinsic properties (StringListProperty)
 *                - Computable Properties (PropertyList of ...)
 *            - is Function Property
 *            - is Composite Property
 *            - Component Properties
 *            
 *            
 * @author Ted Sullivan
 */
public class PropertyClassDescriptor extends DataObjectBean
{
  public static final String CLASS_NAME = "ClassName";
  public static final String SHORT_NAME = "ShortName";
  public static final String FORMATS    = "Formats";
  public static final String DEFAULT_FORMAT = "DefaultFormat";
  public static final String INTRINSIC_PROPS = "IntrinsicProperties";
  public static final String COMPUTABLE_PROPS = "ComputableProperties";
	
  public PropertyClassDescriptor(  )
  {
		
  }
	
  public PropertyClassDescriptor( IProperty property )
  {
    setSchemaProps( property );
  }
	
  public PropertyClassDescriptor( String name, IProperty property )
  {
    setName( name );
    setSchemaProps( property );
  }
	
  private void setSchemaProps( IProperty property )
  {
    String className = property.getType( );
    if (className != null)
    {
      setShortName( new String( className.substring( className.lastIndexOf( "." ) + 1 )) );
      setClassName( className );
    }
		
    if ( property instanceof IQuantity )
    {
      IQuantity quan = (IQuantity)property;
      setPropertyFormats( quan.getUnits( ) );
    }

    
    if ( property.getDefaultFormat( ) != null )
    {
      setDefaultFormat( property.getDefaultFormat( ) );
    }
		
    if ( property instanceof IComputableProperties )
    {
      IComputableProperties computableProps = (IComputableProperties)property;
        
      List<String> intrinsicProps = computableProps.getIntrinsicProperties( );
      if (intrinsicProps != null)
      {
        setIntrinsicProperties( intrinsicProps );
      }
			
      Map<String,String> computablePropMap = computableProps.getComputableProperties( );
      if (computablePropMap != null)
      {
        setComputableProperties( computablePropMap );
      }
    }
  }
	
  public PropertyClassDescriptor( String propertyClass )
  {
    try
    {
      IProperty prop = (IProperty)Class.forName( propertyClass ).newInstance( );
      setSchemaProps( prop );
    }
    catch ( Exception e )
    {
			
    }
  }
	
  public void setClassName( String className )
  {
    doSetProperty( new StringProperty( CLASS_NAME, className ));
  }
	
  public String getClassName( )
  {
    IProperty classNameProp = getProperty( CLASS_NAME );
    return (classNameProp != null) ? classNameProp.getValue( ) : null;
  }

  public void setShortName( String shortName )
  {
    doSetProperty( new StringProperty( SHORT_NAME, shortName ));
  }
	
  public String getShortName( )
  {
    IProperty shortNameProp = getProperty( SHORT_NAME );
    return ( shortNameProp != null) ? shortNameProp.getValue( ) : null;
  }
	
  public void setPropertyFormats( String[] formats )
  {
    doSetProperty( new StringListProperty( FORMATS, formats ));
  }
	
  public String[] getPropertyFormats(  )
  {
    StringListProperty slp = (StringListProperty)getProperty( FORMATS );
    return (slp != null) ? slp.getStringList() : null;
  }
	
  public void setDefaultFormat( String defaultFormat )
  {
    doSetProperty( new StringProperty( DEFAULT_FORMAT, defaultFormat ));
  }
	
  public String getDefaultFormat(  )
  {
    IProperty formProp = getProperty( DEFAULT_FORMAT );
    return (formProp != null) ? formProp.getValue( ) : null;
  }
	
  public void setIntrinsicProperties( List<String> intrinsicProps )
  {
    StringListProperty intrinsicProp = new StringListProperty( INTRINSIC_PROPS );
    intrinsicProp.addStrings( intrinsicProps.iterator( ) );
    doSetProperty( intrinsicProp );
  }
	
  public String[] getIntrinsicProperties( )
  {
    StringListProperty intrinsicProps = (StringListProperty)getProperty( INTRINSIC_PROPS );
    return (intrinsicProps != null) ? intrinsicProps.getStringList() : null;
  }
	
  public void setComputableProperties( Map<String,String> computablePropMap )
  {
    DataObject compProps = new DataObject( COMPUTABLE_PROPS );
    doSetProperty( compProps );
    Iterator<String> propNames = computablePropMap.keySet().iterator( );
    while ( propNames != null && propNames.hasNext() )
    {
      String prop = propNames.next( );
      String propClass = computablePropMap.get( prop );
      compProps.setProperty( new StringProperty( prop, propClass ));
    }
  }
	
	
  public Map<String,String> getComputableProperties(  )
  {
    DataObject compProps = (DataObject)getProperty( COMPUTABLE_PROPS );
    if (compProps != null)
    {
      HashMap<String,String> compPropMap = new HashMap<String,String>( );
      Iterator<IProperty> props = compProps.getProperties( );
      while (props != null && props.hasNext() )
      {
        IProperty prop = props.next( );
        compPropMap.put( prop.getName(), prop.getValue() );
      }
      return compPropMap;
    }
		
    return null;
  }
	
  @Override
  public DataObjectSchema createDataObjectSchema(DataObject context)
  {
    DataObjectSchema dos = new DataObjectSchema( );
    dos.setName( "PropertyTypeSchema" );
        
    PropertyDescriptor pd = new PropertyDescriptor( );
    pd.setName( CLASS_NAME );
    pd.setDisplayName( "Class Name" );
    pd.setPropertyType( "String" );
    dos.addPropertyDescriptor( pd );
    
    pd = new PropertyDescriptor( );
    pd.setName( SHORT_NAME );
    pd.setDisplayName( "Short Name" );
    pd.setPropertyType( "String" );
    dos.addPropertyDescriptor( pd );
    
    pd = new PropertyDescriptor( );
    pd.setName( FORMATS );
    pd.setDisplayName( "Formats" );
    pd.setPropertyType( "StringListProperty" );
    dos.addPropertyDescriptor( pd );
        
    return dos;
  }

}
