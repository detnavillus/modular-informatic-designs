package com.modinfodesigns.search;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataObjectBean;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.BooleanProperty;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.schema.PropertyDescriptor;
import com.modinfodesigns.property.string.StringProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describes a Query Field. At its basic level, a Query Field is a name=value pair.
 * 
 * A Query Field can also include a QueryFieldOperator that describes how the terms in the
 * query field's value are to be interpreted by an IFinder.  (e.g. proximity operators or accrue
 *  (min occurs | max occurs) operators.
 * 
 * If the QueryField is part of a Range Query, an associated QueryField may be added as an
 * Association link. If the Range is one-sided, the IsMinimum property will be set to a non-null value.
 * 
 * @author Ted Sullivan
 */

public class QueryField extends DataObjectBean
{
  private transient static final Logger LOG = LoggerFactory.getLogger( QueryField.class );

  public static final String FIELD_NAME = "FieldName";
  public static final String FIELD_VALUE = "FieldValue";
  public static final String FIELD_TYPE  = "FieldType";
	
  public static final String RANGE_FIELD = "RangeField";   // Other end of a Range Query ...
  public static final String IS_MINIMUM = "IsMinimum";
  public static final String RANGE_INCLUSIVE = "RangeInclusive";
	
  public static final String ASSOCIATED_RANGE_VALUE = "AssociatedRangeValue";
	
  // name == field name
  // Field Value
  // QueryFieldOperator
	
  // multi-value operator - depends on the property of the field ...
	
  /*
  @Override
  public String getName( )
  {
    return (this.name != null) ? this.name : getFieldName( );
  }
  */
	
  public QueryField( )
  {
    setDataObjectSchema( "QueryField" );
  }
	
  public QueryField( String fieldName, String fieldValue )
  {
    setDataObjectSchema( "QueryField" );

    setFieldName( fieldName );
    setFieldValue( fieldValue );
  }
	
  public QueryField( IProperty prop )
  {
    setDataObjectSchema( "QueryField" );

    if (prop != null)
    {
      LOG.debug( "constructing with " + prop.getName( ) + ": " + prop.getValue( )  + " type " + prop.getClass( ) );
      setFieldName( prop.getName( ) );
      setFieldValue( prop.getValue( ) );
    }
  }
	
  public void setFieldName( String fieldName )
  {
    LOG.debug( "setFieldName( '" + fieldName + "'" );
    doSetProperty( new StringProperty( FIELD_NAME, fieldName ));
  }
	
  public String getFieldName( )
  {
    IProperty fieldNameProp = getProperty( FIELD_NAME );
    return (fieldNameProp != null) ? fieldNameProp.getValue() : null;
  }
	
  public void setFieldValue( IProperty fieldValue )
  {
    fieldValue.setName( FIELD_VALUE );
    doSetProperty( fieldValue );
    setFieldType( fieldValue.getType( ) );
  }
	
  public void setFieldValue( String fieldValue )
  {
    doSetProperty( new StringProperty( FIELD_VALUE, fieldValue ));
    setFieldType( "String" );
  }
	
	
  /**
   * Convenience method to return the String value of the QueryField.  The super
   * class method getProperty( QueryField.FIELD_VALUE ) can be used for more complex query value
   * types such as Boosted query terms.
   *
   * @return
   */
  public String getFieldValue( )
  {
    IProperty fieldValueProp = getProperty( FIELD_VALUE );
    return (fieldValueProp != null) ? fieldValueProp.getValue() : null;
  }
	
  /**
   * Set the fieldType
  */
	
  public void setFieldType( String fieldType )
  {
    doSetProperty( new StringProperty( FIELD_TYPE, fieldType ));
  }
	
  public String getFieldType( )
  {
    IProperty fieldTypeProp = getProperty( FIELD_TYPE );
    return (fieldTypeProp != null) ? fieldTypeProp.getValue() : null;
  }
	
	
  public void setQueryFieldOperator( QueryFieldOperator qFieldOperator )
  {
    LOG.debug( "Set QueryFieldOperator: " + qFieldOperator + " " + qFieldOperator.getName( ) );
    doSetProperty( qFieldOperator );
  }
	
  public QueryFieldOperator getQueryFieldOperator(  )
  {
    return (QueryFieldOperator)getProperty( QueryFieldOperator.NAME );
  }
	
	
  /**
   * Check if this QueryField is part of a Range Query.
   *
   * @return  true if this QueryField has an associated QueryField for the maximum range OR
   *          if this is a one-ended range and the isRangeMinimum property is set.
   */
  public boolean isRangeQuery( )
  {
    return (getAssociatedRangeField() != null || getProperty( IS_MINIMUM ) != null);
  }
	
  /**
   * Sets the associated request property for a Range Query
   *
   * This will be set by the QueryFormatter if the originating QueryFormField has
   * an associated range field.
   *
   * @param rangeField
   */
  public void setAssociatedRangeField( IProperty associatedRangeProp )
  {
    associatedRangeProp.setName( ASSOCIATED_RANGE_VALUE );
    doSetProperty( associatedRangeProp );
  }
	
  public IProperty getAssociatedRangeField(  )
  {
    return getProperty( ASSOCIATED_RANGE_VALUE );
  }

  /**
   * Determines whether the Range is to be Inclusive or Exclusive. If Inclusive, the range end points
   * are part of the query values that can hit.
   *
   * @param isRangeInclusive
   */
  public void setIsRangeInclusive( boolean isRangeInclusive )
  {
    doSetProperty( new BooleanProperty( RANGE_INCLUSIVE, isRangeInclusive ));
  }
    
  public boolean isRangeInclusive(  )
  {
    IProperty rangeInclusiveProp = getProperty(  RANGE_INCLUSIVE );
    if (rangeInclusiveProp == null || !(rangeInclusiveProp instanceof BooleanProperty)) return false;
		
    BooleanProperty boolProp = (BooleanProperty)rangeInclusiveProp;
    return boolProp.getBooleanValue( );
  }
	
  @Override
  public String getValue( String format )
  {
		
    if (format == null || format.equals( IProperty.JSON_FORMAT ))
    {
      StringBuilder strbuilder = new StringBuilder( );
      strbuilder.append( "{" );
      strbuilder.append( "\"FieldName\":\"" ).append( getFieldName( ) ).append( "\"" )
                .append( ",\"FieldValue\":\"" ).append( getFieldValue( ) ).append( "\"" );
			
      QueryFieldOperator queryFieldOp = getQueryFieldOperator( );
      if (queryFieldOp != null)
      {
        strbuilder.append( ",\"queryFieldOperator\":" ).append( queryFieldOp.getValue( IProperty.JSON_FORMAT ));
      }
			 
      strbuilder.append( "}" );
      return strbuilder.toString( );
    }
    else if (format.equals( IProperty.XML_FORMAT ))
    {
      StringBuilder strbuilder = new StringBuilder( );
      strbuilder.append( "<QueryField" );
      if (getName( ) != null)
      {
        strbuilder.append( " \"name=\"" ).append( getName( ) ).append( "\"" );
      }
			
      String fieldName = getFieldName( );
      if (fieldName == null) fieldName = "";
      strbuilder.append( " fieldName=\"" ).append( fieldName ).append( "\"" );
			
      String fieldValue = getFieldValue( );
      if (fieldValue == null) fieldValue = "";
      strbuilder.append( " fieldValue=\"" ).append( fieldValue ).append( "\"" );
      strbuilder.append( ">" );
        
      QueryFieldOperator queryFieldOp = getQueryFieldOperator( );
      if (queryFieldOp != null)
      {
        queryFieldOp.getValue( IProperty.XML_FORMAT );
      }
			
      strbuilder.append( "</QueryField>" );
      return strbuilder.toString( );
    }
		
    return super.getValue( format );
  }

  @Override
  public DataObjectSchema createDataObjectSchema( DataObject context )
  {
    DataObjectSchema dos = new DataObjectSchema( );
    dos.setName( "com.modinfodesigns.search.QueryField" );
    dos.setDataObjectType( getType( ) );
		
    PropertyDescriptor pd = new PropertyDescriptor( );
    pd.setName( FIELD_NAME );
    pd.setPropertyType( "String" );
    dos.addPropertyDescriptor( pd );
		
    pd = new PropertyDescriptor( );
    pd.setName( FIELD_VALUE );
    pd.setPropertyType( "String" );
    dos.addPropertyDescriptor( pd );
    
    pd = new PropertyDescriptor( );
    pd.setName( FIELD_TYPE );
    pd.setPropertyType( "String" );
    dos.addPropertyDescriptor( pd );
    
    pd = new PropertyDescriptor( );
    pd.setName( QueryFieldOperator.NAME );
    pd.setIsDataObject( true );
    pd.setDataObjectSchema( QueryFieldOperator.NAME );
    dos.addPropertyDescriptor( pd );
		
    return dos;
  }
}
