package com.modinfodesigns.property.transform.xml;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IntrinsicPropertyDelegate;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.string.StringListProperty;
import com.modinfodesigns.property.PropertyList;

import com.modinfodesigns.property.transform.string.IDataListRenderer;
import com.modinfodesigns.property.transform.string.IPropertyHolderRenderer;
import com.modinfodesigns.property.transform.string.StringTransform;

import com.modinfodesigns.utils.StringMethods;

import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;

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
    
  private String nameProperty = "name";
    
  private boolean noIntrinsicProperties = true;
	
  private boolean renderIDAsProperty = true;
    
  private boolean renderNameAsProperty = true;
    
  private boolean renderType = false;
    
  private HashSet<String> excludedFields;
    
  // Need a map for property name --> format
  private HashMap<String,String> propFormatMap;
    
  public void setRootTag( String rootTag )
  {
    this.rootTag = rootTag;
  }
	
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
    
  public void setNameProperty( String nameProperty )
  {
    this.nameProperty = nameProperty;
  }

	
  public void setOutputType( String outputSchema )
  {
    this.outputSchema = outputSchema;
  }
    
  public void addExcludedField( String excludedField )
  {
    if (excludedFields == null) excludedFields = new HashSet<String>( );
    excludedFields.add( excludedField );
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
    System.out.println( "renderProperty " + prop.getClass().getName( ) );
    if ( excluded( prop ))
    {
      return;
    }
      
    if (prop instanceof PropertyList)
    {
      PropertyList pl = (PropertyList)prop;
      Iterator<IProperty> propIt = pl.getProperties( );
      while( propIt.hasNext( ) )
      {
        IProperty listProp = propIt.next( );
        renderProperty( listProp, strbuilder );
      }
    }
    else if (prop instanceof StringListProperty )
    {
      StringListProperty slp = (StringListProperty)prop;
      String[] values = slp.getStringList( );
      for (int i = 0; i < values.length; i++)
      {
        StringProperty tProp = new StringProperty( prop.getName( ), values[i] );
        renderProperty( tProp, strbuilder );
      }
    }
    else if (prop instanceof DataObject)
    {
        System.out.println( "Rendering DataObject" );
        DataObject dob = (DataObject)prop;
        if ( outputSchema.equals( XMLConstants.FIELD_TYPE_ATTRIBUTES_STYLE ))
        {
            renderFieldTypeAttributes( dob, strbuilder);
        }
        else if ( outputSchema.equals( XMLConstants.TAGNAME_IS_FIELD_STYLE ))
        {
            renderTagNameIsField( dob, strbuilder );
        }
        else
        {
            renderFieldTag( dob, strbuilder );
        }
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
    System.out.println( "renderPropertyHolder" );
    StringBuilder strbuilder = new StringBuilder( );
    strbuilder.append( "<" ).append( rootTag ).append( ">" );
    strbuilder.append( "<" ).append( recordTag ).append( ">" );
      
    if ( renderNameAsProperty )
    {
      StringProperty nameProp = new StringProperty( nameProperty, propHolder.getName( ) );
      renderProperty( nameProp, strbuilder );
    }
    if ( renderIDAsProperty )
    {
      String id = propHolder.getID( );
      if ( id != null )
      {
        StringProperty idProp = new StringProperty( "id", id );
        renderProperty( idProp, strbuilder );
      }
    }
    Iterator<IProperty> propIt = propHolder.getProperties( );
    while( propIt != null && propIt.hasNext() )
    {
      IProperty prop = propIt.next();
      if (!noIntrinsicProperties || !(prop instanceof IntrinsicPropertyDelegate) )
      {
        renderProperty( prop, strbuilder );
      }
    }
    strbuilder.append( "</" ).append( recordTag ).append( ">" );
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
              .append( fixForTextValue( propValue ) )
              .append( "</" ).append( valueTag ).append( ">" );
    
    strbuilder.append( "</" ).append( fieldTag ).append( ">" );
  }
	
	
  // ====================================================================
  //   <Field name="field name" type="field type" >field value</Field>
  // ====================================================================
  private void renderFieldTypeAttributesXML( IProperty prop, StringBuilder strbuilder )
  {
    strbuilder.append( "<" ).append( fieldTag ).append( " name=\"" )
              .append( prop.getName( ) ).append( "\"" );
    if ( renderType )
    {
      strbuilder.append( " type=\"" ).append( prop.getType() ).append( "\"" );
    }
    strbuilder.append( ">" );
      
    String format = (propFormatMap != null) ? propFormatMap.get( prop.getName( ) ) : null;
    String propValue = (format != null) ? prop.getValue( format ) : prop.getValue( );
    strbuilder.append( fixForTextValue( propValue ) )
              .append( "</" ).append( fieldTag ).append( ">" );
  }
	
  private void renderTagNameIsFieldXML( IProperty prop, StringBuilder strbuilder )
  {
    strbuilder.append( "<" ).append( fixForTagName( prop.getName( ) ) );
    if ( renderType )
    {
      strbuilder.append( " type=\"" )
               .append( prop.getType() ).append( "\"" );
    }
    strbuilder.append( ">" );

    String format = (propFormatMap != null) ? propFormatMap.get( prop.getName( ) ) : null;
    String propValue = (format != null) ? prop.getValue( format ) : prop.getValue( );
    strbuilder.append( fixForTextValue( propValue ))
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
    if ( renderNameAsProperty )
    {
      StringProperty nameProp = new StringProperty( nameProperty, dobj.getName( ) );
      renderFieldTypeAttributesXML( nameProp, strbuilder );
    }
    if ( renderIDAsProperty )
    {
      String id = dobj.getID( );
      if ( id != null )
      {
        StringProperty idProp = new StringProperty( "id", id );
        renderFieldTypeAttributesXML( idProp, strbuilder );
      }
    }
      
    Iterator<String> pIt = dobj.getPropertyNames();
    while( pIt != null && pIt.hasNext())
    {
      String propName = pIt.next();
      if (!excluded( dobj.getProperty( propName ) ))
      {
        System.out.println( "rendering property: " + propName );
        IProperty prop = dobj.getProperty( propName );
        if (prop instanceof PropertyList)
        {
          PropertyList pl = (PropertyList)prop;
          Iterator<IProperty> propIt = pl.getProperties( );
          while( propIt.hasNext( ) )
          {
            IProperty listProp = propIt.next( );
            if (listProp instanceof DataObject )
            {
                renderFieldTypeDataObject( prop.getName(), (DataObject)listProp, strbuilder );
            }
            else
            {
              renderFieldTypeAttributesXML( listProp, strbuilder );
            }
          }
        }
        else if (prop instanceof StringListProperty )
        {
          StringListProperty slp = (StringListProperty)prop;
          String[] values = slp.getStringList( );
          for (int i = 0; i < values.length; i++)
          {
            StringProperty tProp = new StringProperty( prop.getName( ), values[i] );
            renderFieldTypeAttributesXML( tProp, strbuilder );
          }
        }
        // ????
        else if (prop instanceof DataObject )
        {
          renderFieldTypeDataObject( prop.getName( ), (DataObject)prop, strbuilder );
        }
        else
        {
          renderFieldTypeAttributesXML( prop, strbuilder );
        }
      }
    }
		
    strbuilder.append( "</" ).append( recordTag ).append( ">" );
  }

  private void renderFieldTypeDataObject( String propName, DataObject chObject, StringBuilder strbuilder )
  {
    Iterator<IProperty> dpIt = chObject.getProperties( );
    while( dpIt != null && dpIt.hasNext() )
    {
      IProperty chprop = dpIt.next();
      if (!noIntrinsicProperties || !(chprop instanceof IntrinsicPropertyDelegate) )
      {
        StringProperty tProp = new StringProperty( propName, chprop.getValue( ) );
        renderFieldTypeAttributesXML( tProp, strbuilder );
      }
    }
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
    
  private String fixForTextValue( String text )
  {
    String fixedString = text;
    fixedString = StringTransform.replaceSubstring( fixedString, "&", "&amp;" );
    fixedString = StringTransform.replaceSubstring( fixedString, ",',", "'" );
    return fixedString;
  }
    
  private boolean excluded( IProperty prop )
  {
    if (noIntrinsicProperties && ((prop instanceof IntrinsicPropertyDelegate) || prop.getName().indexOf( "." ) > 0) )
    {
        return true;
    }
    
    return (excludedFields != null && excludedFields.contains( prop.getName( ) ));
  }

}
