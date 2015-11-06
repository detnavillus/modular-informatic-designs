package com.modinfodesigns.property;

import com.modinfodesigns.property.transform.string.StringTransform;
import com.modinfodesigns.property.IComputableProperties;
import com.modinfodesigns.property.string.StringProperty;

import com.modinfodesigns.property.quantity.IntegerProperty;
import com.modinfodesigns.property.quantity.IQuantity;
import com.modinfodesigns.property.quantity.ScalarQuantity;
import com.modinfodesigns.property.quantity.QuantityOperationException;
import com.modinfodesigns.utils.DOMMethods;

import com.modinfodesigns.property.transform.xml.XMLDataObjectParser;
import com.modinfodesigns.property.transform.json.JSONParserTransform;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyList implements IProperty, IPropertySet, IComputableProperties
{
  private transient static final Logger LOG = LoggerFactory.getLogger( PropertyList.class );
    
  public static final String DELIMITED_VALUES = "DELIMITEDVALUES=";
    
  private String name;
    
  private String propertyDelimiter = "|";
  private String nameValueDelimiter = "=";
    
  private boolean uniqueOnly = false;
    
  protected ArrayList<IProperty> propList = new ArrayList<IProperty>( );
    
  public PropertyList( ) { }
    
  public PropertyList( String name )
  {
    this.name = name;
  }
    
  public PropertyList( String name, List<IProperty> props )
  {
    this.name = name;
    this.propList.addAll( props );
  }
    
  public void setUniqueOnly( String uniqueOnly )
  {
    this.uniqueOnly = uniqueOnly.equalsIgnoreCase( "true" );
    LOG.debug( "UniqueOnly = " + uniqueOnly );
  }
    
  public void setUniqueOnly( boolean uniqueOnly )
  {
    this.uniqueOnly = uniqueOnly;
  }
    
  public void addProperty( IProperty property )
  {
    LOG.debug( "addProperty: " + property );
        
    if (property == null || property.getValue() == null)
    {
      LOG.debug( "Can't add null property!" );
      return;
    }
        
    if (property instanceof DataObject && ((DataObject) property).getProperty( "type" ) != null )
    {
      IProperty innerProp = null;
      String type = ((DataObject) property).getProperty( "type" ).getValue( );
      String propName = (property.getName( ) == null && ((DataObject) property).getProperty( "name" ) != null)
                      ? ((DataObject) property).getProperty( "name" ).getValue( )
                      : property.getName( );
            
      if (type.equals( "String"))
      {
        innerProp = new StringProperty(   );
      }
      else if (type.equals( "Integer" ))
      {
        innerProp = new IntegerProperty(  );
      }
      else
      {
        try
        {
          innerProp = (IProperty)Class.forName( type ).newInstance( );
        }
        catch ( Exception e )
        {
          LOG.debug( "Could not create inner Property: '" + type + "' got exception: "+ e );
        }
      }
            
      if (innerProp != null && !(innerProp instanceof DataObject ))
      {
        String value = ((DataObject) property).getProperty( "value" ).getValue( );
        try
        {
          innerProp.setValue( value,  null );
        }
        catch ( PropertyValidationException pve )
        {
                   
        }
          
        if (propName != null) innerProp.setName( propName );
        propList.add( innerProp );
      }
      else
      {
        propList.add( property );
      }
    }
    else if (!uniqueOnly || containsProperty(property) == false)
    {
      LOG.debug( "addProperty " + property.getName() + ": " + property.getValue() );
      propList.add( property );
    }
  }
    
  public void setProperty( IProperty property )
  {
    removeProperty( property.getName() );
    addProperty( property );
  }
    
    
  public boolean containsProperty( IProperty property )
  {
    if (property == null || propList == null)
    {
      return false;
    }

    for (Iterator<IProperty> propIt = getProperties(); propIt.hasNext(); )
    {
      IProperty aProp = propIt.next();

      if ( aProp != null && aProp.getName( ).equals( property.getName( )) && aProp.getClass( ).equals( property.getClass( ))
        && aProp.getValue( ).equals( property.getValue( ) ))
      {
        return true;
      }
    }
        
    return false;
  }
    
  public void removeProperty( IProperty prop )
  {
    // if property is a dataObject - remove from propList based on ObjectID
    if (prop instanceof DataObject) {
      int ndx = -1;
      DataObject remProp = (DataObject)prop;
      for (int i = 0; i < propList.size( ); i++) {
        IProperty chProp = propList.get( i );
        if (chProp instanceof DataObject && ((DataObject)chProp).getID().equals( remProp.getID( ) ) )
        {
          ndx = i;
          break;
        }
      }
          
      if (ndx >= 0)
      {
        propList.remove( ndx );
      }
    }
    else
    {
      removeProperty( prop.getName( ) );
    }
  }
    
  public void removeProperty( String propName )
  {
    if (propList == null) return;
        
    LOG.debug( "removeProperty: " + propName );
    int propNdx = -1;
    for (int i = 0; i < propList.size(); i++)
    {
      IProperty prop = propList.get( i );
      if (prop.getName().equals( propName ))
      {
        propNdx = i;
        break;
      }
    }
        
    if (propNdx >= 0)
    {
      removeProperty( propNdx );
    }
  }
    
  public void removeProperty( int index )
  {
    if (propList != null && index >= 0 && index < propList.size() )
    {
      propList.remove( index );
    }
  }
    
  public Iterator<IProperty> getProperties( )
  {
    return (propList != null) ? propList.iterator( ) : null;
  }
    
  public IProperty getProperty( int index )
  {
    return propList.get( index );
  }
    
  public IProperty getProperty( String name )
  {
    return getProperty( name, true );
  }
    
  public IProperty getProperty( String name, boolean caseSensitive )
  {
    if (propList == null) return null;
    for (Iterator<IProperty> propIt = getProperties(); propIt.hasNext(); )
    {
      IProperty prop = propIt.next();
        
      if (prop.getName() != null)
      {
        if (caseSensitive && prop.getName().equals( name )) return prop;
        else if (prop.getName().equalsIgnoreCase( name )) return prop;
      }
    }
        
    return null;
  }
    
  @Override
  public String getName()
  {
    return name;
  }
    
  public void setName( String name )
  {
    this.name = name;
  }

  @Override
  public String getType()
  {
    return getClass().getCanonicalName( );
  }

  @Override
  public String getValue()
  {
    return getValue( IProperty.XML_FORMAT, null );
  }

  @Override
  public String getValue( String format )
  {
    return getValue( format, null );
  }
    
  public String getValue( Set<String> dobjs )
  {
    return getValue( (String)null, dobjs );
  }
    
  public String getValue( String format, Set<String> dobjs )
  {
    LOG.debug( "getValue( " + format + " )" );
        
    if (format == null || format.equals( IProperty.XML_FORMAT ) || format.equals( IProperty.XML_FORMAT_CDATA ))
    {
      StringBuilder sbr = new StringBuilder( );
      sbr.append( "<PropertyList" );
      if (name != null && name.trim().length() > 0)
      {
        sbr.append( " name=\"" ).append( StringTransform.escapeXML( this.name )).append( "\"" );
      }
      sbr.append( ">" );
      for (int i = 0, isz = propList.size( ); i < isz; i++)
      {
        IProperty prop = propList.get( i );
        sbr.append( getValue( prop, format, dobjs ));
      }
      sbr.append( "</PropertyList>" );
      return sbr.toString( );
    }
    else if (format.equals( IProperty.JSON_FORMAT ))
    {
      StringBuilder sbr = new StringBuilder( );
      if (name != null)
      {
        sbr.append( "\"" ).append( name ).append( "\":" );
      }
      sbr.append( getValue( IProperty.JSON_VALUE, dobjs ) );
      return sbr.toString( );
    }
    else if (format.equals( IProperty.JSON_VALUE ))
    {
      StringBuilder sbr = new StringBuilder( );
      sbr.append( "[" );
      for (int i = 0, isz = propList.size( ); i < isz; i++)
      {
        IProperty prop = propList.get( i );
        if (prop instanceof DataObject)
        {
          sbr.append( getValue( prop, IProperty.JSON_FORMAT, dobjs ));
        }
        else
        {
          sbr.append( getValue( prop, IProperty.JSON_VALUE, dobjs ));
        }
        if (i < (isz - 1)) sbr.append( "," );
      }
      sbr.append( "]" );
      return sbr.toString( );
    }
        
    else if (format.equals( IProperty.DELIMITED_FORMAT ))
    {
      StringBuilder sbr = new StringBuilder( );
      for (int i = 0, isz = propList.size( ); i < isz; i++)
      {
        IProperty prop = propList.get( i );
        sbr.append( prop.getName() ).append( nameValueDelimiter).append( getValue( prop, dobjs ));
        if (i < (isz - 1)) sbr.append( propertyDelimiter );
      }
      return sbr.toString( );
    }
        
    else if (format.startsWith( "DELIMITER=" ))
    {
      String delimiter = new String( format.substring( "DELIMITER=".length() ));
      StringBuilder sbr = new StringBuilder( );
      for (int i = 0, isz = propList.size( ); i < isz; i++)
      {
        IProperty prop = propList.get( i );
        sbr.append( prop.getName() ).append( nameValueDelimiter ).append( getValue( prop, dobjs ));
        if (i < (isz - 1)) sbr.append( delimiter );
      }
        
      return sbr.toString( );
    }
        
    else if (format.startsWith( DELIMITED_VALUES ))
    {
      String delimiter = new String( format.substring( DELIMITED_VALUES.length() ));
      StringBuilder sbr = new StringBuilder( );
      for (int i = 0, isz = propList.size( ); i < isz; i++)
      {
        IProperty prop = propList.get( i );
        sbr.append( getValue( prop, dobjs));
        if (i < (isz - 1)) sbr.append( delimiter );
      }
        
      return sbr.toString( );
    }
        
    return null;
  }
    
  String getValue( IProperty prop, String format, Set<String> dobjs )
  {
    if (format == null) return getValue( prop, dobjs );
        
    if (prop instanceof DataObject)
    {
      DataObject dob = (DataObject)prop;
      return  (dobjs != null) ? dob.getValue( format, dobjs ) : dob.getValue( format );
    }
    else if (prop instanceof PropertyList )
    {
      PropertyList pl = (PropertyList)prop;
      return pl.getValue( format, dobjs );
    }
    else
    {
      return prop.getValue( format );
    }
  }

    
  String getValue( IProperty prop, Set<String> dobjs )
  {
    if (prop instanceof DataObject)
    {
      DataObject dob = (DataObject)prop;
      return  (dobjs != null) ? dob.getValue( ) : dob.getValue( dobjs );
    }
    else if (prop instanceof PropertyList )
    {
      PropertyList pl = (PropertyList)prop;
      return pl.getValue( dobjs );
    }
    else
    {
      return prop.getValue(  );
    }
  }

    
  public int size( )
  {
    return propList.size( );
  }
    
  public String[] getValues( )
  {
    String[] vals = new String[ propList.size( ) ];
    for (int i = 0; i < propList.size(); i++)
    {
      IProperty prop = propList.get( i );
      vals[i] = prop.getValue( );
    }
    
    return vals;
  }

  @Override
  public String getDefaultFormat( )
  {
    return IProperty.XML_FORMAT;
  }
    
  @Override
  public void setValue( String value, String format ) throws PropertyValidationException
  {
    LOG.debug( "setValue( " + format + " )" );
        
    if (format == null || format.equals( IProperty.XML_FORMAT ))
    {
      Document doc = DOMMethods.getDocument( new StringReader( value ) );
      Element documentElement = doc.getDocumentElement( );
      String name = documentElement.getAttribute( "name" );
      if (name != null)
      {
        LOG.debug( "Setting Name to: " + name );
        setName( name );
      }
            
      NodeList propList = documentElement.getElementsByTagName( "Property" );
      if (propList != null && propList.getLength() > 0)
      {
        for (int i = 0; i < propList.getLength(); i++)
        {
          Element propEl = (Element)propList.item( i );
          IProperty prop = new XMLDataObjectParser( ).createProperty( propEl );
          addProperty( prop );
        }
      }
    }
    else if ( format.equals(IProperty.JSON_FORMAT ) )
    {
      // should have a [name]:[ list of props ]
      if ( value.indexOf( ":[" ) > 0 )
      {
        IProperty myProps = new JSONParserTransform( ).createProperty( value );
        setName( myProps.getName( ) );
        if (myProps instanceof PropertyList)
        {
          Iterator<IProperty> propIt = ((PropertyList)myProps).getProperties( );
          while ( propIt != null && propIt.hasNext() )
          {
            IProperty prop = propIt.next( );
            addProperty( prop );
          }
        }
      }
    }
  }
    
  @Override
  public IProperty copy( )
  {
    PropertyList pl = new PropertyList( this.name );
    pl.setUniqueOnly( this.uniqueOnly );
        
    for (int i = 0; i < propList.size( ); i++)
    {
      IProperty prop = propList.get( i );
      IProperty propCopy = prop.copy( );
      pl.addProperty( propCopy );
    }
        
    return pl;
  }

  @Override
  public Object getValueObject()
  {
    ArrayList<Object> objList = new ArrayList<Object>( );
    if (propList != null)
    {
      for (int i = 0; i < propList.size( ); i++)
      {
        IProperty prop = propList.get( i );
        objList.add( prop.getValueObject() );
      }
    }
        
    return objList;
  }

  @Override
  public IPropertySet union( IPropertySet another )
                            throws PropertyTypeException
  {
    if (!(another instanceof PropertyList))
    {
      throw new PropertyTypeException( "Not a PropertyList!" );
    }
        
    PropertyList union = (PropertyList)copy( );
    PropertyList anotherLst = (PropertyList)another;
        
    for (Iterator<IProperty> propIt = anotherLst.getProperties(); propIt.hasNext(); )
    {
      IProperty anotherProp = propIt.next( );
      if (!containsProperty( anotherProp ))
      {
        union.addProperty( anotherProp );
      }
    }
        
    return union;
  }

  @Override
  public IPropertySet intersection( IPropertySet another )
                                    throws PropertyTypeException
  {
    if (!(another instanceof PropertyList))
    {
      throw new PropertyTypeException( "Not a PropertyList!" );
    }
        
    PropertyList intersection = new PropertyList( );
    intersection.setName( name );
    PropertyList anotherLst = (PropertyList)another;
        
    for (Iterator<IProperty> propIt = anotherLst.getProperties(); propIt.hasNext(); )
    {
      IProperty anotherProp = propIt.next( );
      if (containsProperty( anotherProp ))
      {
        intersection.addProperty( anotherProp );
      }
    }
        
    return intersection;
  }

  @Override
  public boolean contains( IPropertySet another )
                           throws PropertyTypeException
  {
    if (!(another instanceof PropertyList))
    {
      throw new PropertyTypeException( "Not a PropertyList!" );
    }

    PropertyList anotherLst = (PropertyList)another;
    for (Iterator<IProperty> propIt = anotherLst.getProperties(); propIt.hasNext(); )
    {
      IProperty anotherProp = propIt.next( );
      if (!containsProperty( anotherProp ))
      {
        return false;
      }
    }
    
    return true;
  }

  @Override
  public boolean contains( IProperty another )
                           throws PropertyTypeException
  {
    if (another instanceof PropertyList)
    {
      return contains( (IPropertySet)another );
    }
        
    return containsProperty( another );
  }

  @Override
  public boolean intersects( IPropertySet another )
                              throws PropertyTypeException
  {
    if (!(another instanceof PropertyList))
    {
      throw new PropertyTypeException( "Not a PropertyList!" );
    }

    PropertyList anotherLst = (PropertyList)another;
    for (Iterator<IProperty> propIt = anotherLst.getProperties(); propIt.hasNext(); )
    {
      IProperty anotherProp = propIt.next( );
      if (containsProperty( anotherProp ))
      {
        return true;
      }
    }
        
    return false;
  }
    

  @Override
  public List<String> getIntrinsicProperties()
  {
    ArrayList<String> props = new ArrayList<String>( );
    props.add( "Count" );
    if (canComputeAverage( ))
    {
      props.add( "Average" );
      props.add( "Minimum" );
      props.add( "Maximum" );
    }
        
    return props;
  }
    

  @Override
  public IProperty getIntrinsicProperty( String name )
  {
    if (name == null) return null;
        
    if (name.equals( "Count" ))
    {
      return new IntegerProperty( "Count", propList.size() );
    }
    else if (name.equals( "Average" ))
    {
      return getAverage( );
    }
    else if (name.equals( "Minimum" ))
    {
      return getMinimum( );
    }
    else if (name.equals( "Maximum" ))
    {
      return getMaximum( );
    }
        
    return null;
  }

  @Override
  public Map<String, String> getComputableProperties()
  {
    return null;
  }

  @Override
  public IProperty getComputedProperty(String name, IProperty fromProp)
  {
    return null;
  }

  // Return true if all of the props are an IQuantity of the same type ...
  private boolean canComputeAverage( )
  {
    String quantityType = null;
    for (int i = 0; i < propList.size(); i++)
    {
      IProperty prop = propList.get( i );
      if (!(prop instanceof IQuantity)) return false;
        
      if (quantityType != null && quantityType.equals( prop.getType() ) == false)
      {
        return false;
      }
            
      if (quantityType == null) quantityType = prop.getType();
    }
        
    return true;
  }
    
  private IQuantity getAverage( )
  {
    IQuantity sumQuant = null;
    String quantityType = null;
    for (int i = 0; i < propList.size(); i++)
    {
      IProperty prop = propList.get( i );
      if (!(prop instanceof IQuantity)) return null;
      if (quantityType != null && !quantityType.equals( prop.getType()))
      {
        return null;
      }
            
      if (quantityType == null) quantityType = prop.getType();
        
      if (sumQuant == null)
      {
        sumQuant = (IQuantity)prop;
      }
      else
      {
        IQuantity newQuan = (IQuantity)prop;
        try
        {
          sumQuant = sumQuant.add( newQuan );
        }
        catch (QuantityOperationException qoe)
        {
          return null;
        }
      }
    }
        
    ScalarQuantity countQ = new ScalarQuantity( "Counts", (double)propList.size() );
    return (sumQuant != null && propList.size() > 0) ? sumQuant.divide( countQ ) : null;
  }
    
    
  public IQuantity getMinimum( )
  {
    IQuantity minQuant = null;
    String quantityType = null;
    for (int i = 0; i < propList.size(); i++)
    {
      IProperty prop = propList.get( i );
      if (!(prop instanceof IQuantity)) return null;
      if (quantityType != null && !quantityType.equals( prop.getType()))
      {
        return null;
      }
            
      if (quantityType == null) quantityType = prop.getType();
        
      if (minQuant == null)
      {
        minQuant = (IQuantity)prop;
      }
      else
      {
        IQuantity newQuan = (IQuantity)prop;
        if (newQuan.getQuantity() < minQuant.getQuantity() )
        {
          minQuant = newQuan;
        }
      }
    }
        
    return minQuant;
  }
    
  public IQuantity getMaximum( )
  {
    IQuantity maxQuant = null;
    String quantityType = null;
    for (int i = 0; i < propList.size(); i++)
    {
      IProperty prop = propList.get( i );
      if (!(prop instanceof IQuantity)) return null;
      if (quantityType != null && !quantityType.equals( prop.getType() ))
      {
        return null;
      }
            
      if (quantityType == null) quantityType = prop.getType();
        
      if (maxQuant == null)
      {
        maxQuant = (IQuantity)prop;
      }
      else
      {
        IQuantity newQuan = (IQuantity)prop;
        if (newQuan.getQuantity() > maxQuant.getQuantity() )
        {
          maxQuant = newQuan;
        }
      }
    }
        
    return maxQuant;
  }

  @Override
  public boolean isMultiValue()
  {
    return true;
  }

  @Override
  public String[] getValues(String format)
  {
    String[] values = new String[ propList.size( ) ];
    for (int i = 0, isz = propList.size(); i < isz; i++)
    {
      IProperty prop = propList.get( i );
      values[i] = prop.getValue( format );
    }
    return values;
  }

}
