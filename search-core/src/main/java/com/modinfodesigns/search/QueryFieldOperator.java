package com.modinfodesigns.search;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataObjectBean;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.schema.PropertyDescriptor;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.quantity.IntegerProperty;

import java.util.Iterator;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describes an operator that is used to describe how a query field should be processed by 
 * an IFinder implementation.
 * 
 * fieldOperator = contains the user-selected value (e.g. ANY, ALL, EXACT, NONE,
 * NEAR, ORDERED NEAR, MIN-MAX, PREFIX, FUZZY, etc.)
 * 
 * Modifiers = optional properties describing how the field operator should be evaluated
 * (e.g. proximity distance).
 * 
 * @author Ted Sullivan
 */

public class QueryFieldOperator extends DataObjectBean
{
  private transient static final Logger LOG = LoggerFactory.getLogger( QueryFieldOperator.class );

  public static final String NAME = "QueryFieldOperator";
    
  public static final String FIELD_OPERATOR = "FieldOperator";
  public static final String MODIFIERS = "Modifiers";
    
  public static final String ANY   = "ANY";
  public static final String ALL   = "ALL";
  public static final String EXACT = "EXACT";
  public static final String NONE  = "NONE";
  public static final String NEAR  = "NEAR";
  public static final String ONEAR = "ONEAR";
  public static final String PREFIX = "PREFIX";
  public static final String FUZZY  = "FUZZY";
  public static final String RANGE = "RANGE";

  public static final String EQ = "EQ";
  public static final String LT = "LT";
  public static final String GT = "GT";
  public static final String LTE = "LTE";
  public static final String GTE = "GTE";
    
  // gt, lt,
    
  public static final String DISTANCE = "DISTANCE";

  private PropertyList modifiers;
    
  public QueryFieldOperator( )
  {
    setDataObjectSchema( "QueryFieldOperator" );
  }
    
  public QueryFieldOperator( String fieldOperator )
  {
    setDataObjectSchema( "QueryFieldOperator" );
    setFieldOperator( fieldOperator );
  }
    
  @Override
  public String getName( )
  {
    return NAME;
  }
    
  @Override
  public void setName( String name )
  {
    // No changes allowed here
  }
    
  public void setFieldOperator( String fieldOperator )
  {
    doSetProperty( new StringProperty( FIELD_OPERATOR, fieldOperator ));
  }
    
  public String getFieldOperator( )
  {
    IProperty fieldOpProp = getProperty( FIELD_OPERATOR );
    return (fieldOpProp != null) ? fieldOpProp.getValue() : null;
  }
    
  public void setDistance( int distance )
  {
    LOG.debug( "setDistance( " + distance + " )" );
    setModifier( new IntegerProperty( DISTANCE, distance ));
  }
    
  public void addModifier( IProperty modifier )
  {
    LOG.debug( "addModifier( " + modifier + " )" );
    if (modifiers == null)
    {
      modifiers = new PropertyList( MODIFIERS );
      doSetProperty( modifiers );
    }
        
    // We should only have one modifier with a given name
    if (getModifier( modifier.getName() ) != null)
    {
      modifiers.setProperty( modifier );
    }
    else
    {
      modifiers.addProperty( modifier );
    }
  }
    
  @Override
  public IProperty copy(  )
  {
    QueryFieldOperator copy = new QueryFieldOperator( );
    copy.modifiers = this.modifiers;
    copy.setFieldOperator( getFieldOperator( ) );
    	
    return copy;
  }
    
  @Override
  public void addProperty( IProperty prop )
  {
    LOG.debug( "Got prop '" + prop.getName( ) + "' " + prop  );
    if (prop != null && prop.getName( ).equals( MODIFIERS ))
    {
      LOG.debug( "Adding as Modifier!" );
      if (prop instanceof PropertyList)
      {
        this.modifiers = (PropertyList)prop;
      }
      else
      {
        addModifier( prop );
      }
    }
    else
    {
      super.addProperty( prop );
    }
  }
    
  @Override
  public void setProperty( IProperty prop )
  {
    if (prop != null && prop.getName( ).equals( MODIFIERS ))
    {
      LOG.debug( "Setting Modifier!" );
      if (prop instanceof PropertyList)
      {
        this.modifiers = (PropertyList)prop;
      }
      else
      {
        setModifier( prop );
      }
    }
    else
    {
      super.setProperty( prop );
    }
  }

  public void setModifier( IProperty modifier )
  {
    if (modifiers == null)
    {
      LOG.debug(  "Modifiers is null!" );
      addModifier( modifier );
    }
    else
    {
      LOG.debug( "calling setProperty( )" );
      modifiers.setProperty( modifier );
    }
  }
    

  public Iterator<IProperty> getModifiers( )
  {
    if (modifiers == null) return null;
    	
    if (modifiers instanceof PropertyList )
    {
      return modifiers.getProperties( );
    }
    else
    {
      ArrayList<IProperty> oneList = new ArrayList<IProperty>( );
      oneList.add( modifiers );
      return oneList.iterator( );
    }
  }
    
  public void setModifiers( PropertyList modifiers )
  {
    modifiers.setName( MODIFIERS );
    setProperty( modifiers );
  }
    
  public IProperty getModifier( String name )
  {
    LOG.debug( "getModifier( '" + name + "' )" );
    if (modifiers == null) return null;
    
    for (Iterator<IProperty> modIt = getModifiers( ); modIt.hasNext(); )
    {
      IProperty modProp = modIt.next( );
            
      if (modProp.getName().equals( name ))
      {
        LOG.debug( "Returning: " + modProp.getName( ) + " = " + modProp.getValue( ) );
        return modProp;
      }
    }
        
    return null;
  }
    
  @Override
  public IProperty getProperty( String name )
  {
    if (name != null && name.equals( MODIFIERS ))
    {
      return modifiers;
    }
    else
    {
      return super.getProperty( name );
    }
  }
    
  public boolean isRangeOperator( )
  {
    String op = getFieldOperator( );
    return (op != null && (op.equals( LT ) || op.equals( LTE ) || op.equals( GT ) || op.equals( EQ )
                        || op.equals( GTE )));
  }
    
  @Override
  public String getValue( String format )
  {
    LOG.debug( "getValue( " + format + " )" );
    	
    if (format == null || format.equals( IProperty.JSON_FORMAT ))
    {
      StringBuilder strbuilder = new StringBuilder( );
      strbuilder.append( "{" );
      strbuilder.append( "\"FieldOperator\":\"" ).append( getFieldOperator( ) ).append( "\"" );
        
      IntegerProperty distProp = (IntegerProperty)getProperty( DISTANCE );
      if (distProp != null)
      {
        strbuilder.append( "\"distance\":" ).append( distProp.getValue( ) );
      }
        
      if (modifiers != null)
      {
        strbuilder.append( ",\"Modifiers\":" );
        strbuilder.append( modifiers.getValue( IProperty.JSON_VALUE ));
      }
      strbuilder.append( "}" );
      return strbuilder.toString( );
    }
    else if (format.equals( IProperty.XML_FORMAT ))
    {
      StringBuilder strbuilder = new StringBuilder( );
      strbuilder.append( "<QueryFieldOperator>" );
        
      IProperty fieldOpProp = getProperty( FIELD_OPERATOR );
      if (fieldOpProp != null)
      {
        strbuilder.append( fieldOpProp.getValue( IProperty.XML_FORMAT ) );
      }
    		
      IntegerProperty distProp = (IntegerProperty)getProperty( DISTANCE );
      if (distProp != null)
      {
        strbuilder.append( distProp.getValue( IProperty.XML_FORMAT ) );
      }
    		
      if (modifiers != null)
      {
        strbuilder.append( modifiers.getValue( IProperty.XML_FORMAT ));
      }
        
      strbuilder.append( "</QueryFieldOperator>" );
    		
      return strbuilder.toString( );
    }
    	
    return super.getValue( format );
  }
    
    

  @Override
  public DataObjectSchema createDataObjectSchema( DataObject  context )
  {
    DataObjectSchema dos = new DataObjectSchema( );
    dos.setName( "QueryFieldOperator" );
    dos.setDataObjectType( getType( ) );
    
    PropertyDescriptor pd = new PropertyDescriptor( );
    pd.setName( FIELD_OPERATOR );
    pd.setPropertyType( "String" );
    dos.addPropertyDescriptor( pd );
    
    pd = new PropertyDescriptor( );
    pd.setName( MODIFIERS );
    pd.setPropertyType( "PropertyList" );  // or should this be a DataList for persistence?
    dos.addPropertyDescriptor( pd );
    
    return dos;
  }
}
