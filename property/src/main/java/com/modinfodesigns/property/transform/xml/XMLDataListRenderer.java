package com.modinfodesigns.property.transform.xml;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.DataObject;

import com.modinfodesigns.property.transform.string.IDataListRenderer;
import com.modinfodesigns.property.transform.string.IPropertyHolderRenderer;
import com.modinfodesigns.property.transform.string.StringTransform;

import com.modinfodesigns.utils.StringMethods;

import java.util.Iterator;
import java.util.HashMap;

/**
 * Renders a DataList as an XML String.
 * 
 * @author Ted Sullivan
 */

public class XMLDataListRenderer implements IPropertyHolderRenderer, IDataListRenderer
{
  private String outputSchema = XMLConstants.FIELD_VALUE_TAG_STYLE;
    
  private String rootTag      = "ResultSet";
  private String recordTag    = "Result";
  private String fieldTag     = "Field";
  private String fieldNameTag = "Name";
  private String valueTag     = "Value";
  private String typeTag      = "Type";
	
  // Need a map for property name --> format
  private HashMap<String,String> propFormatMap;
	
  public void setRecordTag( String recordTag )
  {
    this.recordTag = recordTag;
  }
	
  public void setFieldTag( String fieldTag )
  {
    this.fieldTag = fieldTag;
  }
	
  public void setFieldNameTag( String fieldNameTag )
  {
    this.fieldNameTag = fieldNameTag;
  }
	
  public void setValueTag( String valueTag )
  {
    this.valueTag = valueTag;
  }
	
  public void setTypeTag( String typeTag )
  {
    this.typeTag = typeTag;
  }
	
  public void setOutputType( String outputSchema )
  {
    this.outputSchema = outputSchema;
  }
    
  @Override
  public String renderProperty( IProperty property )
  {
    StringBuilder strbuilder = new StringBuilder( );
    renderProperty( property, strbuilder );
    return strbuilder.toString();
  }
	
  public void renderProperty( IProperty prop, StringBuilder strbuilder)
  {
    if (prop instanceof IPropertyHolder )
    {
      strbuilder.append( renderPropertyHolder( (IPropertyHolder)prop ) );
    }
    else
    {
      if ( outputSchema.equals( XMLConstants.FIELD_TYPE_ATTRIBUTES_STYLE ))
      {
        renderFieldTypeAttributesXML( prop, strbuilder);
      }
      else if ( outputSchema.equals( XMLConstants.TAGNAME_IS_FIELD_STYLE ))
      {
        renderTagNameIsFieldXML( prop, strbuilder );
      }
      else
      {
        renderFieldValueXML( prop, strbuilder );
      }
    }
  }

	
  @Override
  public String renderPropertyHolder( IPropertyHolder propHolder )
  {
    if (propHolder instanceof IDataList)
    {
      return renderDataList( (IDataList)propHolder );
    }

    StringBuilder strbuilder = new StringBuilder( );
    strbuilder.append( "<" ).append( rootTag ).append( ">" );
    Iterator<IProperty> propIt = propHolder.getProperties( );
    while( propIt != null && propIt.hasNext() )
    {
      IProperty prop = propIt.next();
      renderProperty( prop, strbuilder );
    }

    strbuilder.append( "</").append( rootTag ).append( ">" );
    return strbuilder.toString( );
  }
	
  // ============================================================
  // Renders:
  //
  //   <Field>
  //     <Name>fieldname</Name>
  //     <Value>field value</Value><Type>field type</Type>
  //   </Field>
  // ============================================================
  private void renderFieldValueXML( IProperty prop, StringBuilder strbuilder )
  {
    strbuilder.append( "<" ).append( fieldTag ).append( ">" );
		
    strbuilder.append( "<" ).append( fieldNameTag ).append( ">" )
              .append( prop.getName( ) )
              .append( "</" ).append( fieldNameTag ).append( ">" );
    
    String format = (propFormatMap != null) ? propFormatMap.get( prop.getName( ) ) : null;
    String propValue = (format != null) ? prop.getValue( format ) : prop.getValue( );
    strbuilder.append( "<" ).append( valueTag ).append( ">" )
              .append( propValue )
              .append( "</" ).append( valueTag ).append( ">" );
    
    strbuilder.append( "</" ).append( fieldTag ).append( ">" );
  }
	
	
  // ====================================================================
  //   <Field name="field name" type="field type" >field value</Field>
  // ====================================================================
  private void renderFieldTypeAttributesXML( IProperty prop, StringBuilder strbuilder )
  {
    strbuilder.append( "<" ).append( fieldTag ).append( " name=\"" )
              .append( prop.getName( ) ).append( " type=\"" )
              .append( prop.getType() ).append( ">" );
      
    String format = (propFormatMap != null) ? propFormatMap.get( prop.getName( ) ) : null;
    String propValue = (format != null) ? prop.getValue( format ) : prop.getValue( );
    strbuilder.append( propValue )
              .append( "</" ).append( fieldTag ).append( ">" );
  }
	
  private void renderTagNameIsFieldXML( IProperty prop, StringBuilder strbuilder )
  {
    strbuilder.append( "<" ).append( fixForTagName( prop.getName( ) ) ).append( " type=\"" )
              .append( prop.getType() ).append( ">" );

    String format = (propFormatMap != null) ? propFormatMap.get( prop.getName( ) ) : null;
    String propValue = (format != null) ? prop.getValue( format ) : prop.getValue( );
    strbuilder.append( propValue )
              .append( "</" ).append( fixForTagName( prop.getName( ) ) ).append( ">" );
  }
	
  @Override
  public String renderDataList( IDataList dList )
  {
    StringBuilder strbuilder = new StringBuilder( );
    strbuilder.append( "<" ).append( rootTag ).append( ">" );
    Iterator<DataObject> dIt = dList.getData( );
    while (dIt != null && dIt.hasNext() )
    {
      DataObject dobj = dIt.next();
        
      if ( outputSchema.equals( XMLConstants.FIELD_TYPE_ATTRIBUTES_STYLE ))
      {
        renderFieldTypeAttributes( dobj, strbuilder );
      }
      else if ( outputSchema.equals( XMLConstants.TAGNAME_IS_FIELD_STYLE ))
      {
        renderTagNameIsField( dobj, strbuilder );
      }
      else
      {
        renderFieldTag( dobj, strbuilder );
      }
    }

    strbuilder.append( "</").append( rootTag ).append( ">" );
    return strbuilder.toString( );
  }
	
	
  // ====================================================================
  // <Result>
  //   <Field name="field name" type="field type" >field value</Field>
  // </Result>
  // ====================================================================
  private void renderFieldTypeAttributes( DataObject dobj, StringBuilder strbuilder )
  {
    strbuilder.append( "<" ).append( recordTag ).append( ">" );
    Iterator<String> pIt = dobj.getPropertyNames();
    while( pIt != null && pIt.hasNext())
    {
      String propName = pIt.next();
      IProperty prop = dobj.getProperty( propName );
      renderFieldTypeAttributesXML( prop, strbuilder );
    }
		
    strbuilder.append( "</" ).append( recordTag ).append( ">" );
  }

	
  // ==============================================================
  // Render Field Tag Value Tag Style
  // <Result>
  //   <Field>
  //     <Name>fieldname</Name>
  //     <Value>field value</Value><Type>field type</Type>
  //   </Field>
  // </Result>
  // ==============================================================
  private void renderFieldTag( DataObject dobj, StringBuilder strbuilder )
  {
    strbuilder.append( "<" ).append( recordTag ).append( ">" );
		
    Iterator<String> pIt = dobj.getPropertyNames();
    while( pIt != null && pIt.hasNext())
    {
      String propName = pIt.next();
      IProperty prop = dobj.getProperty( propName );
      renderFieldValueXML( prop, strbuilder );
    }
		
    strbuilder.append( "</" ).append( recordTag ).append( ">" );
  }

	
  // ===============================================================
  // Render TagName is FieldName style
  // <Result>
  //   <FieldName type="field type" >field value</FieldName>
  // </Result>
  // ===============================================================
  private void renderTagNameIsField( DataObject dobj, StringBuilder strbuilder )
  {
    strbuilder.append( "<" ).append( recordTag ).append( ">" );
    Iterator<String> pIt = dobj.getPropertyNames();
    while( pIt != null && pIt.hasNext())
    {
      String propName = pIt.next();
      IProperty prop = dobj.getProperty( propName );
      renderTagNameIsFieldXML( prop, strbuilder );
    }
		
    strbuilder.append( "</" ).append( recordTag ).append( ">" );
  }
	
  // ===============================================================
  // Makes the property name compliant as an XML Tag Name
  //   can contain letters, numbers, and other characters
  //   cannot start with a number or punctuation character
  //   cannot start with the letters xml (or XML, or Xml, etc)
  //   cannot contain spaces
  // ===============================================================
  private String fixForTagName( String propName )
  {
    String pName = propName;
    // probably should use RegularExpression
    if (StringMethods.startsWithLetter( propName ) == false )
    {
      pName = new String( propName.substring( 1 ) );
    }
		
    pName = StringTransform.replaceSubstring( pName, " ", "_"  );
    
    return pName;
  }

}