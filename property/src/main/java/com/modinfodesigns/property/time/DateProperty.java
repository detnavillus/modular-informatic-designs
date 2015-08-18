package com.modinfodesigns.property.time;

import com.modinfodesigns.app.ApplicationManager;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IComputableProperties;
import com.modinfodesigns.property.IFunctionProperty;
import com.modinfodesigns.property.IExposeInternalProperties;
import com.modinfodesigns.property.BooleanProperty;
import com.modinfodesigns.property.BinaryProperty;
import com.modinfodesigns.property.PropertyValidationException;
import com.modinfodesigns.property.quantity.Duration;
import com.modinfodesigns.property.schema.PropertyDescriptor;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.transform.string.StringTransform;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import java.text.SimpleDateFormat;
import java.text.ParsePosition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateProperty extends StringProperty implements IExposeInternalProperties, IComputableProperties
{
  private transient static final Logger LOG = LoggerFactory.getLogger( DateProperty.class );

  public static final String DEFAULT_FORMAT = "MM/dd/yyyy";
	
  private static ArrayList<String> intrinsics;
	
  private static HashMap<String,String> computables;
	
  private static String[] commonFormats = { "MM/dd/yyyy", "MM/dd/yyyy hh:mm:ss",
                                            "yyyy/MM/dd", "MMM dd, yyyy", "MMMM dd, yyyy, yyyy/MM/dd hh:mm:ss" };
	
  private String name;
	
  private Date date;
	
  private DateType dateType;
	
  private String dateFormat = DEFAULT_FORMAT;
	
  static
  {
    intrinsics = new ArrayList<String>( );
    intrinsics.add( "Duration" );  // if past date
    	
    computables = new HashMap<String,String>( );
    computables.put( "Interval", "com.modinfodesigns.property.time.DateProperty" );
    computables.put( "Before", "com.modinfodesigns.property.time.DateProperty" );
    computables.put( "Within", "com.modinfodesigns.property.time.DateRangeProperty" );
  }
	
  public DateProperty(  )
  {
    this.date = new Date( );
    dateFormat = getDefaultFormat( );
  }
	
  public DateProperty( String name )
  {
    this.name = name;
    this.date = new Date( );
    dateFormat = getDefaultFormat( );
  }
	
  public DateProperty( String name, Date date )
  {
    this.name = name;
    this.date = date;
    dateFormat = getDefaultFormat();
  }
	
  public DateProperty( String name, String value, String dateFormat )
  {
    this.name = name;
    try
    {
      setValue( value, dateFormat );
    }
    catch (PropertyValidationException pve )
    {
      LOG.error( "Could not construct DateProperty: " + pve.getMessage( ) );
    }
  }

  @Override
  public String getName()
  {
    return this.name;
  }

  @Override
  public void setName(String name)
  {
    this.name = name;
  }
	
  public Date getDate(  )
  {
    return this.date;
  }
	
  public void setDateFormat( String dateFormat )
  {
    this.dateFormat = dateFormat;
  }

  @Override
  public String getType()
  {
    return getClass().getCanonicalName( );
  }

  @Override
  public String getValue()
  {
    LOG.debug( "getValue( ): " + this.date );
		
    if (date == null)
    {
      LOG.debug( "Returning empty string ..." );
      return "";
    }
		
    return (dateFormat != null) ? getValue( dateFormat ) : date.toString( );
  }

  @Override
  public String getValue( String format )
  {
    LOG.debug( "getValue( " + format + " )" );
		
    if (format != null && format.equals(IProperty.XML_FORMAT))
    {
      return "<Property type=\"com.modinfodesigns.time.DateProperty\"><Name>" + StringTransform.escapeXML( this.name )
           + "</Name><Value>" + StringTransform.escapeXML( getValue( ) ) + "</Value></Property>";
    }
    else if (format != null && format.equals( IProperty.JSON_FORMAT))
    {
      return "\"" + this.name + "\":\"" + getValue( ) + "\"";
    }
    else if (format != null && format.equals( IProperty.JSON_VALUE))
    {
      return getValue( );
    }
		
    if (this.date == null)
    {
      LOG.error( "getValue FAILED - date is null!" );
      return "";
    }
		
    if (dateFormat == null) dateFormat = format;
    
    try
    {
      SimpleDateFormat sdf = new SimpleDateFormat( format );
      return sdf.format( this.date );
    }
    catch ( Exception e )
    {
      LOG.error( "getValue FAILED for format '" + format + "' Exception: " + e );
    }
		
    return null;
  }

  @Override
  public void setValue(String value, String format) throws PropertyValidationException
  {
    if (format != null && dateFormat == null)
    {
      dateFormat = format;
    }
    else if (format == null)
    {
      format = dateFormat;
    }
		
    if (format == null && dateType != null)
    {
      format = dateType.getFormat( );
    }
		
    // fix format based on implicit days or years
    // for example if format is MM/yyyy and implicit day is first of month
    // value = MM/01/yyyy etc...
    value = fixDateValue( value, format );
		
    if (value != null && value.equalsIgnoreCase( "NOW" ))
    {
      this.date = new Date( );
    }
    else
    {
      try
      {
        SimpleDateFormat sdf = new SimpleDateFormat( format );
        this.date = sdf.parse( value, new ParsePosition(0) );
      }
      catch ( Exception e )
      {
        throw new PropertyValidationException( "setValue FAILED for format '" + format + "' Exception: " + e.getMessage( ) );
      }
    }
  }
	
  // Do some surgery on the supplied value if there are implicit pieces
  // required by the DateType
  private String fixDateValue( String value, String format )
  {
    if (dateType != null)
    {
      if (dateType.getImplicitDay( ) != null)
      {
          
      }
    }
    return value;
  }

  @Override
  public IProperty copy()
  {
    DateProperty dp = new DateProperty( );
    dp.date = this.date;
    dp.name = this.name;
    dp.dateFormat = this.dateFormat;
    dp.dateType = this.dateType;
		
    return dp;
  }

  @Override
  public Object getValueObject()
  {
    return date;
  }
	
	
  public void setDefaultFormat( String defaultFormat)
  {
    this.dateFormat = defaultFormat;
  }

  @Override
  public String getDefaultFormat()
  {
    return dateFormat;
  }
	
  @Override
  public List<String> getIntrinsicProperties()
  {
    if (dateType != null)
    {
      ArrayList<String> intrinsics = new ArrayList<String>( );
      intrinsics.add( "DateType" );
      intrinsics.add( "IsFutureDate" );
      intrinsics.add( dateType.getDurationLabel( ) );
      if (dateType.getBeforeAfterProp( ) != null) intrinsics.add( "BeforeAfterProperty" );
			
      intrinsics.addAll( dateType.getFunctionPropertyNames( ) );
			
      return intrinsics;
    }
    else
    {
      return intrinsics;
    }
  }

  @Override
  public IProperty getIntrinsicProperty( String name )
  {
    if (dateType != null)
    {
      if (name.equals( "DateType" ))
      {
        return new StringProperty( "DateType", dateType.getName( ) );
      }
      else if (name.equals( "IsFutureDate" ))
      {
        // This property automatically reverts to false IF
        // this.date is not null and this.date is in the past
        boolean isFutureDate = dateType.isFutureDate( );
        if (isFutureDate && (this.date != null && this.date.getTime( ) < new Date( ).getTime( ) ))
        {
          isFutureDate = false;
        }
        return new BooleanProperty( "IsFutureDate", isFutureDate );
      }
      else if (name.equals( "BeforeAfterProperty" ))
      {
        BinaryProperty binaryProp = dateType.getBeforeAfterProp( );
        try
        {
          if (this.date == null) binaryProp.setValue( binaryProp.getFirstChoice( ), null );
          else
          {
            boolean isAfter = (this.date.after( new Date( )));
            binaryProp.setValue( ((isAfter) ? binaryProp.getSecondChoice( ) : binaryProp.getFirstChoice( ) ), null );
          }
        }
        catch ( Exception e )
        {
             
        }
        return binaryProp;
      }
      else if (name.equals( dateType.getDurationLabel( ) ))
      {
        if (this.date != null)
        {
          // depending on whether the date type is future or not, calculate
          // a duration from now ...
          if (dateType.isFutureDate( ) )
          {
            return new Duration( dateType.getDurationLabel( ), new Date( ), this.date );
          }
          else
          {
            return new Duration( dateType.getDurationLabel( ), this.date, new Date( ) );
          }
        }
      }
      else if ( dateType.getFunctionProperty( name ) != null )
      {
        IFunctionProperty functionProp = dateType.getFunctionProperty( name );
        DataObject dobj = new DataObject( );
        dobj.setProperty( this.copy( ) );
          
        functionProp.setPropertyHolder( dobj );
        IProperty outputProp = functionProp.execute( );
        if (outputProp != null)
        {
          IProperty copy = outputProp.copy( );
          copy.setName( name );
          return copy;
        }
      }
    }
    else
    {
      if ( name.equals( "Duration" ) )
      {
        return new Duration( "Duration", this.date, new Date( ) );
      }
    }
		
    return null;
  }
	

	
  @Override
  public Map<String, String> getComputableProperties()
  {
    return computables;
  }
	
  @Override
  public IProperty getComputedProperty( String name, IProperty toProp )
  {
    if (name == null || toProp == null || this.date == null) return null;
    
    if (name.equals( "Interval" ) && toProp instanceof DateProperty )
    {
      DateProperty toDate = (DateProperty)toProp;
      if (this.date == null || toDate.date == null) return null;
    
      if (toDate.date.getTime() > this.date.getTime( ))
      {
        return new Duration( "Interval", this.date, toDate.date );
      }
      else
      {
        return new Duration( "Interval", toDate.date, this.date );
      }
    }
    else if ( name.equals( "Within" ) && toProp instanceof DateRangeProperty )
    {
      DateRangeProperty toRange = (DateRangeProperty)toProp;
      boolean contains = toRange.contains( this.date );
      return new BooleanProperty( "Within", contains );
    }
        
    return null;
  }
	
  public boolean before( DateProperty anotherDate )
  {
    if (this.date == null || anotherDate.date == null) return false;
    
    return this.date.getTime( ) < anotherDate.date.getTime( );
  }
	
  public boolean onOrBefore( DateProperty anotherDate )
  {
    if (this.date == null || anotherDate.date == null) return false;
    
    return this.date.getTime( ) <= anotherDate.date.getTime( );
  }

  public boolean after( DateProperty anotherDate )
  {
    if (this.date == null || anotherDate.date == null) return false;
    
    return this.date.getTime( ) > anotherDate.date.getTime( );
  }

  public boolean onOrAfter( DateProperty anotherDate )
  {
    if (this.date == null || anotherDate.date == null) return false;
		
    return this.date.getTime( ) >= anotherDate.date.getTime( );
  }
	
  public boolean equals( DateProperty anotherDate )
  {
    if (this.date == null || anotherDate.date == null) return false;

    return this.date.getTime( ) == anotherDate.date.getTime( );
  }

	
  public void setDateType( String dateTypeName )
  {
    LOG.debug( "setDateType( '" + dateTypeName + "' )" );
    	
    ApplicationManager appMan = ApplicationManager.getInstance( );
    this.dateType = (DateType)appMan.getApplicationObject( dateTypeName, "DateType" );
    if (this.dateType != null && this.dateType.getFormat() != null)
    {
      setDefaultFormat( dateType.getFormat( ) );
    }
  }
    
  public String getDateType( )
  {
    return (this.dateType != null) ? this.dateType.getName( ) : null;
  }
    
  public String getIsFutureDate(  )
  {
    return ( isFutureDate( ) ) ? "true" : "false";
  }
    
  public boolean isFutureDate( )
  {
    if (dateType != null)
    {
      boolean isFutureDate = dateType.isFutureDate( );
      if (isFutureDate && (this.date != null && this.date.getTime( ) < new Date( ).getTime( ) ))
      {
        isFutureDate = false;
      }
        
      return isFutureDate;
    }
    	
    return false;
  }
	
  @Override
  public List<PropertyDescriptor> getInternalProperties( )
  {
    ArrayList<PropertyDescriptor> internalProps = new ArrayList<PropertyDescriptor>( );
		
    LOG.debug( "Getting Date Types ..." );
    // prop descriptor for DateType -
    // create a list of the DateTypes available for use
    // make this one an EnumerationProperty (read only)
    PropertyDescriptor dateTypeDesc = new PropertyDescriptor( );
    dateTypeDesc.setName( "DateType" );
    dateTypeDesc.setPropertyType( "EnumerationProperty" );
    dateTypeDesc.setDisplayName( "Date Type" );
    
    ApplicationManager appMan = ApplicationManager.getInstance( );
    List<String> dateTypes = appMan.getApplicationObjectNames( "DateType" );
    if (dateTypes != null)
    {
      String[] dateTypesList = new String[ dateTypes.size( ) ];
      dateTypes.toArray( dateTypesList );
      dateTypeDesc.setPropertyValues( dateTypesList );
      if (dateType != null)
      {
        dateTypeDesc.setDefaultValue( dateType.getName( ) );
      }
      else
      {
        dateTypeDesc.setDefaultValue( dateTypesList[0] );
      }
    }
		
    internalProps.add( dateTypeDesc );
		
    return internalProps;
  }

}
