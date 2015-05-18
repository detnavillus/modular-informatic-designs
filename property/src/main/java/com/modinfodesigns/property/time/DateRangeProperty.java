package com.modinfodesigns.property.time;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.IPropertySet;
import com.modinfodesigns.property.BooleanProperty;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.PropertyTypeException;
import com.modinfodesigns.property.PropertyValidationException;
import com.modinfodesigns.property.transform.string.StringTransform;
import com.modinfodesigns.property.IComputableProperties;

import com.modinfodesigns.property.quantity.Duration;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Expresses a Date Range
 * 
 * Can be created with a human readable string:
 * <pre>
 *   last 2 to 3 days
 *   next 4 months
 *   3 to 5 days ago
 *   4 to 6 days from now
 *   greater than 3 days from now
 *   greater than 1 week ago
 * </pre>
 * 
 * @author Ted Sullivan
 */

public class DateRangeProperty extends DataObject implements IProperty, IPropertyHolder, IPropertySet, IComputableProperties
{
  private transient static final Logger LOG = LoggerFactory.getLogger( DateRangeProperty.class );

  private static ArrayList<String> intrinsics;
  private static HashMap<String,String> computables;
	
  private String name;
	
  private DateProperty from;
  private DateProperty to;
	
  private ArrayList<IProperty> fromToList = new ArrayList<IProperty>( );
  private ArrayList<String> nameList = new ArrayList<String>( );
	
  private String delimiter = " - ";
	
  private String dateFormat;  // underlying format for date ...
	
	
  private static final String BEGINNING_OF_TIME = "01/01/1901";
  private static final String END_OF_TIME       = "01/01/3001";

  private static final long MS_PER_DAY = 24 * 60 * 60 * 1000;
    
  public static final String DATE_RANGE_FORMAT = "DATE_RANGE_FORMAT";
    
  static
  {
    intrinsics = new ArrayList<String>( );
    intrinsics.add( "Duration" );
    intrinsics.add( "StartTime" );
    intrinsics.add( "EndTime" );
    	
    computables = new HashMap<String,String>( );
    computables.put( "Contains",   "com.modinfodesigns.property.time.DateProperty" );
    computables.put( "Intersects", "com.modinfodesigns.property.time.DateRangeProperty" );
    	
    // etc...
    	
  }
	
  public DateRangeProperty( )
  {
    nameList.add( "from" );
    nameList.add( "to" );
  }
	
  public DateRangeProperty( String dateRange )
  {
    this( );
		
    // parse the dates from the string - e.g. "2 to 3 days from now" or "last 4 months"
    try
    {
      setValue( dateRange, "DATE_RANGE_FORMAT" );
    }
    catch ( PropertyValidationException pve )
    {
      LOG.error( "Could not create DateRangeProperty: " + pve.getMessage( ) );
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


  @Override
  public String getType()
  {
    return "com.modinfodesigns.time.DateRangeProperty";
  }
	
  public void setDateFormat( String dateFormat )
  {
    this.dateFormat = dateFormat;
  }

  @Override
  public String getValue()
  {
    if (from == null || to == null) return null;
    if (dateFormat != null)
    {
      return new String( from.getValue( dateFormat ) + delimiter + to.getValue( dateFormat ) );
    }
    else
    {
      return new String( from.getValue( ) + delimiter + to.getValue(  ) );
    }
  }

  @Override
  public String getValue( String format )
  {
    if (from == null || to == null) return null;
		
    if (format.equals( IProperty.XML_FORMAT))
    {
      return "<Property type=\"com.modinfodesigns.time.DateRangeProperty\"><Name>"
           + StringTransform.escapeXML( this.name ) + "</Name><Value>" + StringTransform.escapeXML( getValue( ) ) + "</Value></Property>";
    }
    else if (format != null && format.equals( IProperty.JSON_FORMAT))
    {
      return "\"" + this.name + "\":\"" + getValue( ) + "\"";
    }
		
    return getValue( );
  }

  @Override
  public void setValue(String value, String format) throws PropertyValidationException
  {
    if (format != null && format.equals( DATE_RANGE_FORMAT ))
    {
      parseDateRangeString( value );
    }
    else if (value.indexOf( delimiter ) > 0)
    {
      String fromStr = new String( value.substring(0, value.indexOf( delimiter ))).trim( );
      this.from = new DateProperty( );
      this.from.setValue( fromStr, format );
    	   
      String toStr = new String( value.substring( value.indexOf( delimiter ) + delimiter.length())).trim( );
      this.to = new DateProperty( );
      this.to.setValue( toStr, format );
    }
  }
	
  private void parseDateRangeString( String formattedRange )
  {
    if (formattedRange == null)
    {
      return;
    }

    if (formattedRange.indexOf( "last" ) == 0 && formattedRange.indexOf( "day" ) > 0)
    {
      // last n days pattern...
      setLast( formattedRange, "day" );
    }
    else if (formattedRange.indexOf( "last" ) == 0 && formattedRange.indexOf( "week" ) > 0)
    {
      // last n weeks pattern...
      setLast( formattedRange, "week" );
    }
    else if (formattedRange.indexOf( "last" ) == 0 && formattedRange.indexOf( "month" ) > 0)
    {
      // last n months pattern...
      setLast( formattedRange, "month" );
    }
    else if (formattedRange.indexOf( "last" ) == 0 && formattedRange.indexOf( "year" ) > 0)
    {
      // last n years pattern...
      setLast( formattedRange, "year" );
    }
    else if (formattedRange.indexOf( "days ago" ) > 0)
    {
      if (formattedRange.indexOf( "more than" ) >= 0 || formattedRange.indexOf( "greater than" ) >= 0)
      {
        setEarlierThan( formattedRange, "days" );
      }
      else
      {
        // n to m days ago
        setPeriod( formattedRange, "days", true );
      }
    }
    else if (formattedRange.indexOf( "weeks ago" ) > 0)
    {
      if (formattedRange.indexOf( "more than" ) >= 0 || formattedRange.indexOf( "greater than" ) >= 0)
      {
        setEarlierThan( formattedRange, "weeks" );
      }
      else
      {
        // n to m weeks ago
        setPeriod( formattedRange, "weeks", true );
      }
    }
    else if (formattedRange.indexOf( "months ago" ) > 0)
    {
      if (formattedRange.indexOf( "more than" ) >= 0 || formattedRange.indexOf( "greater than" ) >= 0)
      {
        setEarlierThan( formattedRange, "months" );
      }
      else
      {
        // n to m months ago
        setPeriod( formattedRange, "months", true );
      }
    }
    else if (formattedRange.indexOf( "years ago" ) > 0)
    {
      if (formattedRange.indexOf( "more than" ) >= 0 || formattedRange.indexOf( "greater than" ) >= 0)
      {
        setEarlierThan( formattedRange, "years" );
      }
      else
      {
        // n to m years ago
        setPeriod( formattedRange, "years", true );
      }
    }
    else if (formattedRange.indexOf( "next" ) == 0 && formattedRange.indexOf( "day" ) > 0)
    {
      // next n days pattern...
      setNext( formattedRange, "day" );
    }
    else if (formattedRange.indexOf( "next" ) == 0 && formattedRange.indexOf( "week" ) > 0)
    {
      // next n weeks pattern...
      setNext( formattedRange, "week" );
    }
    else if (formattedRange.indexOf( "next" ) == 0 && formattedRange.indexOf( "month" ) > 0)
    {
      // next n months pattern...
      setNext( formattedRange, "month" );
    }
    else if (formattedRange.indexOf( "next" ) == 0 && formattedRange.indexOf( "year" ) > 0)
    {
      // next n years pattern...
      setNext( formattedRange, "year" );
    }
    else if (formattedRange.indexOf( "days from now" ) > 0)
    {
      if (formattedRange.indexOf( "more than" ) >= 0 || formattedRange.indexOf( "greater than" ) >= 0)
      {
        setLaterThan( formattedRange, "days" );
      }
      else
      {
        // n to m days from now
        setPeriod( formattedRange, "days", false );
      }
    }
    else if (formattedRange.indexOf( "weeks from now" ) > 0)
    {
      if (formattedRange.indexOf( "more than" ) >= 0 || formattedRange.indexOf( "greater than" ) >= 0)
      {
        setLaterThan( formattedRange, "weeks" );
      }
      else
      {
        // n to m weeks from now
        setPeriod( formattedRange, "weeks", false );
      }
    }
    else if (formattedRange.indexOf( "months from now" ) > 0)
    {
      if (formattedRange.indexOf( "more than" ) >= 0 || formattedRange.indexOf( "greater than" ) >= 0)
      {
        setLaterThan( formattedRange, "months" );
      }
      else
      {
        // n to m months from now
        setPeriod( formattedRange, "months", false );
      }
    }
    else if (formattedRange.indexOf( "years from now" ) > 0)
    {
      if (formattedRange.indexOf( "more than" ) >= 0 || formattedRange.indexOf( "greater than" ) >= 0)
      {
        setLaterThan( formattedRange, "years" );
      }
      else
      {
        // n to m years from now
        setPeriod( formattedRange, "years", false );
      }
    }
  }
	
  private void setLast( String formattedString, String interval )
  {
    int totalDays = getDays( formattedString, interval );

    Date now = new Date( );
    Date then = new Date( );
    then.setTime( (then.getTime() - (totalDays * MS_PER_DAY)) );

    this.from = new DateProperty( "from", then );
    this.to = new DateProperty( "to", now );
  }
	
  private void setNext( String formattedString, String interval )
  {
    int totalDays = getDays( formattedString, interval );
      
    Date now = new Date( );
    Date then = new Date( );
    then.setTime( (then.getTime() + (totalDays * MS_PER_DAY)) );
      
    this.from = new DateProperty( "from", now );
    this.to   = new DateProperty( "to", then );
  }
	
  private int getDays( String formattedString, String interval )
  {
    int nd = getInterval( interval );

    int ni = 1;
    try
    {
      String numStr = formattedString.substring( 5, formattedString.indexOf( interval )).trim( );
      if (numStr.length() > 0) ni = Integer.parseInt( numStr );
    }
    catch (Exception e )
    {
      ni = 1;
    }

    return  (nd * ni);
  }
	
  private int getInterval( String interval )
  {
    int nd = 1;
    if (interval.equals( "week" ))       nd = 7;
    else if (interval.equals( "month" )) nd = 30;  // Not calendar math here!
    else if (interval.equals( "year" ))  nd = 365;
        
    return nd;
  }
	
  public void setLaterThan( String formattedString, String interval )
  {
    this.to = new DateProperty( "to", END_OF_TIME, "MM/dd/yyyy" );

    // figure out how many intervals "from now"
    String intervals = null;

    if ( formattedString.indexOf( "more than" ) >= 0)
    {
      intervals = formattedString.substring( "more than ".length(), formattedString.indexOf( interval ) );
    }
    else if ( formattedString.indexOf( "greater than" ) >= 0)
    {
      intervals = formattedString.substring( "greater than ".length(), formattedString.indexOf( interval ) );
    }

    if (intervals != null)
    {
      int ni = Integer.parseInt( intervals.trim( ) );
      int nd = getInterval( interval );
    
      Date fromD = new Date( );
      fromD.setTime( (fromD.getTime( ) + ((nd * ni) * MS_PER_DAY)) );
      from = new DateProperty( "from", fromD );
    }
  }
	
  public void setEarlierThan( String formattedString, String interval )
  {
    from = new DateProperty( "from", BEGINNING_OF_TIME, "MM/dd/yyyy" );

    // figure out how many intervals "ago"
    String intervals = null;

    if (formattedString.indexOf( "more than" ) >= 0)
    {
      intervals = formattedString.substring( "more than ".length(), formattedString.indexOf( interval ) );
    }
    else if (formattedString.indexOf( "greater than" ) >= 0)
    {
      intervals = formattedString.substring( "greater than ".length(), formattedString.indexOf( interval ) );
    }

    if (intervals != null)
    {
      int ni = Integer.parseInt( intervals.trim( ) );

      int nd = getInterval( interval );
        
      Date toD = new Date( );
      toD.setTime( (toD.getTime( ) - ((nd * ni) * MS_PER_DAY)) );
      to = new DateProperty( "to", toD );
    }
  }
	
  public void setPeriod( String formattedString, String interval, boolean past )
  {
    int nd = getInterval( interval );

    int n1 = 0;
    int n2 = 1;
    try
    {
      String num1 = formattedString.substring( 0, formattedString.indexOf( "to" )).trim( );
      n1 = Integer.parseInt( num1 );
        
      String num2 = formattedString.substring( formattedString.indexOf( "to ") + 3, formattedString.indexOf( interval )).trim( );
      n2 = Integer.parseInt( num2 );
    }
    catch (Exception e)
    {
      LOG.error( "Error parsing number of " + interval + ": " + e );
    }

    long msec1 = (nd * n1) * MS_PER_DAY;
    long msec2 = (nd * n2) * MS_PER_DAY;

    if ( msec1 > msec2 )
    {
      long temp = msec1;
      msec1 = msec2;
      msec2 = temp;
    }
    
    if ( past )
    {
      Date fromD = new Date( );
      fromD.setTime( (fromD.getTime() - msec2) );
        
      Date toD = new Date( );
      toD.setTime( (toD.getTime( ) - msec1 ));

      from = new DateProperty( "from", fromD );
      to   = new DateProperty( "to", toD );
    }
    else
    {
      Date fromD = new Date( );
      fromD.setTime( (fromD.getTime() + msec1) );
        
      Date toD = new Date( );
      toD.setTime( (toD.getTime() + msec2 ) );

      from = new DateProperty( "from", fromD );
      to   = new DateProperty( "to", toD );
    }
  }

  @Override
  public IProperty copy()
  {
    DateRangeProperty drp = new DateRangeProperty( );
    drp.name = this.name;
    drp.ID   = this.ID;
    drp.from = this.from;
    drp.to   = this.to;
    drp.dateFormat = this.dateFormat;
    
    return drp;
  }
	
	
  /**
   * Returns the Property of a given name.  If the name is '/' delimited, gets a nested property
   */
  public IProperty getProperty( String name )
  {
    if (name.equalsIgnoreCase( "from" ) || name.equalsIgnoreCase( "begin" ))
    {
      return from;
    }
    else if (name.equalsIgnoreCase( "to" ) || name.equalsIgnoreCase( "end" ))
    {
      return to;
    }
    	
    return null;
  }



  public Iterator<IProperty> getProperties( )
  {
    fromToList.clear();
    if (from != null) fromToList.add( from );
    if (to != null) fromToList.add( to );
    	
    return fromToList.iterator( );
  }
    
    
  /**
   * @return iterator on all IProperty names.
   */
  public Iterator<String>getPropertyNames( )
  {
    return nameList.iterator( );
  }


  public void addProperty( IProperty property )
  {
    setProperty( property );
  }


  /**
   * Sets a property to a singular value
   */
  public void setProperty( IProperty property )
  {
    if (property != null && property.getName().equalsIgnoreCase( "from" ) && property instanceof DateProperty )
    {
      this.from = (DateProperty)property;
    }
    else if (property != null && property.getName().equalsIgnoreCase( "begin" ) && property instanceof DateProperty)
    {
      this.from = (DateProperty)property;
    }
    else if (property != null && property.getName().equalsIgnoreCase( "to" ) && property instanceof DateProperty )
    {
      this.to = (DateProperty)property;
    }
    else if (property != null && property.getName().equalsIgnoreCase( "end" ) && property instanceof DateProperty )
    {
      this.to = (DateProperty)property;
    }
  }
  
  public boolean contains( Date date )
  {
    // Implement me!!!
    return false;
  }

  @Override
  public void removeProperty( String propName )
  {
    // nothing to remove so don't implement this method
  }

  @Override
  public IPropertySet union( IPropertySet another )
                             throws PropertyTypeException
  {
    // return a schedule with the union - merge any date ranges that overlap...
    if (another instanceof Schedule)
    {
      Schedule unionScheule = new Schedule( );
        
      return unionScheule;
    }
    else if (another instanceof DateRangeProperty )
    {
      Schedule unionScheule = new Schedule( );
        
      return unionScheule;
    }
    return null;
  }

  @Override
  public IPropertySet intersection( IPropertySet another )
                                    throws PropertyTypeException
  {
    if (another instanceof Schedule)
    {
      Schedule unionScheule = new Schedule( );
    
      return unionScheule;
    }
    else if (another instanceof DateRangeProperty )
    {
      Schedule unionScheule = new Schedule( );
    
      return unionScheule;
    }
		
    return null;
  }

  @Override
  public boolean contains( IPropertySet another )
                           throws PropertyTypeException
  {
    if (another instanceof DateRangeProperty)
    {
			
    }
    return false;
  }

  @Override
  public boolean intersects( IPropertySet another)
                 throws PropertyTypeException
  {
    if (contains( another ) || another.contains( (IPropertySet)this )) return true;
    	
    if (another instanceof DateRangeProperty )
    {
      DateRangeProperty anotherRange = (DateRangeProperty)another;
    }
    return false;
  }

  @Override
  public boolean contains( IProperty another )
                            throws PropertyTypeException
  {
    if (another instanceof DateProperty)
    {
      DateProperty dP = (DateProperty)another;
      Date dpDate = dP.getDate( );
      if (dpDate == null) return false;
        
      if (dpDate.compareTo( from.getDate()) == 0 || dpDate.compareTo( to.getDate() ) == 0) return true;
			
      return (dpDate.after( from.getDate() ) && dpDate.before( to.getDate() ));
    }
    else if (another instanceof DateRangeProperty)
    {
      // true if entire range is within this one ...
    }
    return false;
  }

  @Override
  public List<String> getIntrinsicProperties()
  {
    return intrinsics;
  }

  @Override
  public IProperty getIntrinsicProperty(String name)
  {
    if (name == null) return null;
		
    if (name.equals( "Duration" ))
    {
      if (from != null && to != null)
      {
        Date toDate = (Date)to.getValueObject( );
        Date fromDate = (Date)from.getValueObject( );
          
        long msecDur = toDate.getTime( ) - fromDate.getTime( );
        double seconds = msecDur / 1000.0;
          
        return new Duration( "Duration", seconds );
      }
    }
    else if (name.equals( "StartTime" ))
    {
      return (from != null) ? from.copy() : null;
    }
    else if (name.equals( "EndTime" ))
    {
      return (to != null) ? to.copy() : null;
    }
		
    return null;
  }

  @Override
  public Map<String, String> getComputableProperties()
  {
    return computables;
  }

  // Return duration properties from DateProperty to StartTime of
  @Override
  public IProperty getComputedProperty( String name, IProperty fromProp)
  {
    if (name == null || fromProp == null) return null;
		
    if (name.equals( "Contains" ) && fromProp instanceof DateProperty )
    {
      DateProperty fromDate = (DateProperty)fromProp;
      boolean contains = (fromDate.getDate( ) != null && this.contains(fromDate.getDate( )));
      return new BooleanProperty( "Contains", contains );
    }
		
    return null;
  }

}
