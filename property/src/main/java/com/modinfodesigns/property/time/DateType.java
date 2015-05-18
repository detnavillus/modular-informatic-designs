package com.modinfodesigns.property.time;

import com.modinfodesigns.app.INamedObject;
import com.modinfodesigns.property.BinaryProperty;
import com.modinfodesigns.property.IFunctionProperty;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class DateType implements INamedObject
{
  public static final String FIRST_OF_MONTH = "FirstOfMonth";
  public static final String LAST_OF_MONTH = "LastOfMonth";

  private String name;
	
  private boolean isFutureDate;
	
  // Does this date represent a one-time event or a recurring event (such as Tax Day)?
  // if isRecurring is true - isFutureDate may not be relevant.
  private boolean isRecurring;
	
  private String format;
	
  private String locale;
	
  // ==============================================================================
  // specifies what the DateProperty's beforeAfter intrinsic property will express
  // e.g. Active|Expired, Living|Dead, Pending|Active
  // ==============================================================================
  private BinaryProperty beforeAfterProp;
	
  // ==============================================================================
  // specifies a label for before or until duration
  // ==============================================================================
  private String durationLabel;
	
  // ==============================================================================
  // Fill In Semantics: specify what to fill in. e.g. lastDayOfMonth, endOfDay,
  // firstDayOfMonth, nextYear (this year if before day/month, next year if not), thisYear
  //
  // Used for implicit date parts such a ValidThroughMonthYear expiration date type.
  // - in this case the day would be the last day of the month.
  // For an Application Date - the year may be implied if the applications are accepted
  // every year. Year of the application date would be the next valid year. This DateType
  // could also have an intrinsic property - Application Year.
  // ==============================================================================
  private String implicitDay;   // first of week, day name of this week, etc.
                                // last day of month, first day of month
    
  private String implicitMonth;
    
  // ==============================================================================
  // Implicit years can be important for Recurring dates.
  // Tax day (April 15) is the deadline for the previous tax year.
  // Admissions applications may be for the next year (if the date is near the
  // end of the year or the current year - if near the beginning).
  // ==============================================================================
  private String implicitYear;  // previous year, this year, next year
    
  // List of Intrinsic Properties - e.g. 'Application Year' or 'Publication Year'
  // DateProperty can use these to apply generate dynamic Intrinsic Properties
  // by applying the function property to itself.
  // Name should be the name of the property that the function property
  // will add to the DateProperty
  private HashMap<String,IFunctionProperty> intrinsicFunctionProperties;


  @Override
  public void setName(String name)
  {
    this.name = name;
  }

  @Override
  public String getName()
  {
    return this.name;
  }
	
  public void setIsFutureDate( boolean isFutureDate )
  {
    this.isFutureDate = isFutureDate;
  }
	
  public void setIsFutureDate( String isFutureDate )
  {
    this.isFutureDate = (isFutureDate != null && isFutureDate.equalsIgnoreCase( "true" ));
  }
	
  public boolean isFutureDate( )
  {
    return this.isFutureDate;
  }
	
  public void setIsRecurring( boolean isRecurring )
  {
    this.isRecurring = isRecurring;
  }
	
  public void setIsRecurring( String isRecurring )
  {
    this.isRecurring = (isRecurring != null && isRecurring.equalsIgnoreCase( "true" ));
  }
	
  public boolean isRecurring( )
  {
    return this.isRecurring;
  }
	
  public void setDurationLabel( String durationLabel )
  {
    this.durationLabel = durationLabel;
  }
	
  public String getDurationLabel( )
  {
    if (this.durationLabel != null) return this.durationLabel;
     
    return (isFutureDate) ? "Until" : "Since";
  }
	
  public void setBinaryProperty( BinaryProperty beforeAfterProp )
  {
    this.beforeAfterProp = beforeAfterProp;
  }

  public BinaryProperty getBeforeAfterProp( )
  {
    return this.beforeAfterProp;
  }
	
  public void setFormat( String format )
  {
    this.format = format;
  }
	
  public String getFormat( )
  {
    return this.format;
  }
	
  public void setImplicitDay( String implicitDay )
  {
    this.implicitDay = implicitDay;
  }
	
  public String getImplicitDay( )
  {
    return this.implicitDay;
  }
	
  public void addFunctionProperty( IFunctionProperty functionProp )
  {
    if (functionProp == null) return;
		
    if (intrinsicFunctionProperties == null) intrinsicFunctionProperties = new HashMap<String,IFunctionProperty>( );
    intrinsicFunctionProperties.put( functionProp.getName(), functionProp );
  }
	
  public List<String> getFunctionPropertyNames( )
  {
    ArrayList<String> functionNames = new ArrayList<String>( );
    if (intrinsicFunctionProperties != null)
    {
      for (String name : intrinsicFunctionProperties.keySet() )
      {
        functionNames.add( name );
      }
    }
		
    return functionNames;
  }
	
  public IFunctionProperty getFunctionProperty( String name )
  {
    return (intrinsicFunctionProperties != null) ? intrinsicFunctionProperties.get( name ) : null;
  }
}
