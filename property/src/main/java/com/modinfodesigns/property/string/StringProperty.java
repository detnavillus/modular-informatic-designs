package com.modinfodesigns.property.string;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IComputableProperties;
import com.modinfodesigns.property.IExposeInternalProperties;
import com.modinfodesigns.property.PropertyValidationException;
import com.modinfodesigns.property.schema.PropertyDescriptor;
import com.modinfodesigns.property.transform.string.StringTransform;

import com.modinfodesigns.property.quantity.IntegerProperty;

import com.modinfodesigns.utils.DOMMethods;
import com.modinfodesigns.utils.StringMethods;

import java.io.StringReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringProperty implements IProperty, IComputableProperties, IExposeInternalProperties
{
  private transient static final Logger LOG = LoggerFactory.getLogger( StringProperty.class );

  private static ArrayList<String> computables;
	
  private String name;
  private String value;
    
  protected String defaultFormat;
    
  protected int maximumLength = 0;
    
  static
  {
    computables = new ArrayList<String>( );
    computables.add( "Length" );
    computables.add( "MaximumLength" );
  }

  public StringProperty(  )
  {
    this.name = "";
    this.value = "";
  }
    
  public StringProperty( String name, String value )
  {
    this.name = name;
    this.value = value;
  }
    
  @Override
  public String getName()
  {
    return this.name;
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
    return (this.value != null) ? this.value : "";
  }

  @Override
  public String getValue( String format )
  {
    if (this.value == null) return "";
		
    if (format == null) return this.value;
		
    if (format.equals(IProperty.JSON_FORMAT))
    {
      if (this.value.startsWith( "\"" ) && this.value.endsWith( "\"" ))
      {
        return "\"" + this.name + "\":" + this.value;
      }

      return "\"" + this.name + "\":\"" + this.value + "\"";
    }
    else if (format.equals( IProperty.JSON_VALUE ))
    {
      if (this.value.startsWith( "\"" ))
      {
        return this.value;
      }
      return "\"" + this.value + "\"";
    }
    else if (format.equals( IProperty.XML_FORMAT ))
    {
      return "<Property type=\"com.modinfodesigns.property.string.StringProperty\"><Name>"
            + StringTransform.escapeXML( this.name ) + "</Name><Value>" + StringTransform.escapeXML( this.value ) + "</Value></Property>";
    }
    else if (format.equals(IProperty.XML_FORMAT_CDATA))
    {
      return "<Property type=\"com.modinfodesigns.property.string.StringProperty\"><Name><![CDATA[" + this.name
           + "]]></Name><Value><![CDATA[" + this.value + "]]></Value></Property>";
    }
    else if ( format.equals( IProperty.HTML_FORM_FORMAT ))
    {
      // create an input field
      return "<input type=\"text\" name=\"" + this.name + "\" value=\"" + this.value + "\">";
    }
		
    return this.value;
  }
	
  public void setDefaultFormat( String defaultFormat )
  {
    this.defaultFormat = defaultFormat;
  }
	
  @Override
  public String getDefaultFormat()
  {
    return this.defaultFormat;
  }
	
  @Override
  public void setValue( String value, String format ) throws PropertyValidationException
  {
    // TO BE IMPLEMENTED!
    // Detect if JSON or XML - if so, get the name and value
    // if format is null or empty
    
    if (format == null || format.trim().length() == 0)
    {
      this.value = value;
        
      if (maximumLength > 0 && value.length() > maximumLength)
      {
        throw new PropertyValidationException( "String length exceeds maximum for this property." );
      }
    }
    else if (format.equals( IProperty.JSON_VALUE ))
    {
      this.value = StringMethods.stripEnclosingQuotes( value );
    }
    else if (format.equals( IProperty.JSON_FORMAT ))
    {
      String[] nameVals = value.split( ":" );
      this.name = StringMethods.stripEnclosingQuotes(nameVals[0]);
      this.value = StringMethods.stripEnclosingQuotes(nameVals[1]);
    }
    else if (format.equals( IProperty.XML_FORMAT ))
    {
      LOG.debug( "StringProperty setValue( XML ) NOT IMPLEMENTED YET!" );
      if (value.startsWith( "<" ) && value.endsWith( ">" ))
      {
        // TO DO: Create DOM object, get what's in the Value tag ...
        Document doc = DOMMethods.getDocument( new StringReader( value ) );
        if (doc != null && doc.getDocumentElement() != null)
        {
          Element documentElement = doc.getDocumentElement( );
	            
          // Check that type is ok?
          NodeList valList = documentElement.getElementsByTagName( "Value" );
          if (valList != null && valList.getLength() > 0)
          {
            Element valEl = (Element)valList.item( 0 );
            this.value = DOMMethods.getText( valEl );
          }
        }
      }
      else
      {
        this.value = value;
      }
    }
    else
    {
      throw new PropertyValidationException( "Format: " + format + " not supported." );
    }
  }
	
  public void setMaximumLength( int maximumLength )
  {
    this.maximumLength = maximumLength;
  }
	
  public void setMaximumLength( String maximumLength )
  {
		
  }
	
  public int getMaximumLength( )
  {
    return this.maximumLength;
  }
	
  @Override
  public IProperty copy( )
  {
    StringProperty strProp = new StringProperty( );
    strProp.setName( this.name );
    try
    {
      strProp.setValue( this.value, null );
    }
    catch ( PropertyValidationException pve ) { }
		
    return strProp;
  }

  @Override
  public Object getValueObject()
  {
    return (value != null) ? new String( value ) : null;
  }

  // Can compute the string length ...
  @Override
  public List<String> getIntrinsicProperties()
  {
    return computables;
  }

  @Override
  public IProperty getIntrinsicProperty(String name)
  {
    if ( name.equals( "Length" ) )
    {
      int len = (value != null) ? value.length() : 0;
      return new IntegerProperty( name, len );
    }
		
    return null;
  }

  @Override
  public Map<String, String> getComputableProperties()
  {
    return null;
  }
	
  @Override
  public IProperty getComputedProperty( String name, IProperty fromProp )
  {
    // Lehvenstein distances and things are possible here ...
    return null;
  }

  @Override
  public boolean isMultiValue()
  {
    return false;
  }

  @Override
  public String[] getValues(String format)
  {
    String[] values = new String[1];
    values[0] = getValue( format );
    return values;
  }

  @Override
  public List<PropertyDescriptor> getInternalProperties()
  {
    ArrayList<PropertyDescriptor> internalProps = new ArrayList<PropertyDescriptor>( );
        
    PropertyDescriptor maxLengthProp = new PropertyDescriptor( );
    maxLengthProp.setName( "MaximumLength" );
    maxLengthProp.setPropertyType( "Integer" );
    maxLengthProp.setDisplayName( "Maximum Length" );
    internalProps.add( maxLengthProp );
    
    return internalProps;
  }

}
