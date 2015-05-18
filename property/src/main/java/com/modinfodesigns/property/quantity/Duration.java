package com.modinfodesigns.property.quantity;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.PropertyValidationException;

import java.util.Date;

public class Duration implements IQuantity 
{
  public static String[] VALID_UNITS = { "seconds", "milliseconds", "microseconds", "nanoseconds",
                                         "minutes", "hours", "days", "weeks", "years", "years days" };

  public static final String NANOSECONDS = "nanoseconds";
  public static final String MICROSECONDS = "microseconds";
  public static final String MILLISECONDS = "milliseconds";
  public static final String SECONDS = "seconds";
  public static final String MINUTES = "minutes";
  public static final String HOURS   = "hours";
  public static final String DAYS    = "days";
  public static final String WEEKS   = "weeks";
  public static final String YEARS   = "years";
    
  private static double SECONDS_PER_MINUTE = 60.0;
  private static double SECONDS_PER_HOUR   = SECONDS_PER_MINUTE * 60.0;
  private static double SECONDS_PER_DAY    = SECONDS_PER_HOUR * 24;
  private static double SECONDS_PER_WEEK   = SECONDS_PER_DAY * 7;
  private static double SECONDS_PER_YEAR   = SECONDS_PER_DAY * 365.24;
    
  private double seconds;
  private String name;
    
  public Duration(  ) {  }
    
  public Duration( String name, double seconds )
  {
    this.name = name;
    this.seconds = seconds;
  }
    
  public Duration( String name, Date from, Date to )
  {
    this.name = name;
    // compute duration from Dates ...
    long fromTime = from.getTime( );
    long toTime = to.getTime( );
    	
    this.seconds = (double)(toTime - fromTime) / 1000.0;
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
    return getClass().getCanonicalName();
  }

  @Override
  public String getValue()
  {
    return Double.toString( seconds );
  }

  @Override
  public String getValue(String format)
  {
    if (format == null) return getValue( );
		
    if (format.equals( "years days" ))
    {
      int years = (int)(seconds / SECONDS_PER_YEAR);
      int days =  (int)( ((double)seconds - ((double)years * SECONDS_PER_YEAR )) / SECONDS_PER_DAY);
        
      return Integer.toString( years ) + " years " + Integer.toString( days ) + ((days != 1) ?  " days" : " day");
    }
    else if (format.equals( "years" ))
    {
      int years = (int)(seconds / SECONDS_PER_YEAR);
      return Integer.toString( years );
    }
		
    return Double.toString( getQuantity( format ) );
  }

  @Override
  public void setValue(String value, String format) throws PropertyValidationException
  {
    try
    {
      double dVal  = Double.parseDouble( value );
      if (format == null || format.equals( "seconds" ))
      {
        this.seconds = dVal;
      }
      else if (format.equals( "minutes" ))
      {
        this.seconds = dVal * SECONDS_PER_MINUTE;
      }
      else if (format.equals( "hours" ))
      {
        this.seconds = dVal * SECONDS_PER_HOUR;
      }
      else if (format.equals( "days" ))
      {
        this.seconds = dVal * SECONDS_PER_DAY;
      }
      else if (format.equals( "years" ))
      {
        this.seconds = dVal * SECONDS_PER_YEAR;
      }
      else if (format.equals( "miliseconds" ))
      {
        this.seconds = dVal / 1000.0;
      }
    }
    catch ( NumberFormatException nfe )
    {
      throw new PropertyValidationException( "can't parse: " + value );
    }
  }

  @Override
  public String getDefaultFormat()
  {
    return "seconds";
  }

  @Override
  public IProperty copy()
  {
    Duration dur = new Duration( );
    dur.seconds = seconds;
    dur.name = name;
    
    return dur;
  }

  @Override
  public Object getValueObject( )
  {
    return new Double( seconds );
  }

  @Override
  public double getQuantity()
  {
    return seconds;
  }

  @Override
  public double getQuantity(String units)
  {
    if (units == null) return seconds;
		
    if (units.equals( NANOSECONDS ))
    {
      return seconds * 1000000000.0;
    }
    else if (units.equals( MICROSECONDS ))
    {
      return seconds * 1000000.0;
    }
    else if (units.equals( MILLISECONDS ))
    {
      return seconds * 1000.0;
    }
    else if (units.equals( SECONDS ))
    {
      return seconds;
    }
    else if (units.equals( MINUTES ))
    {
      return seconds / SECONDS_PER_MINUTE;
    }
    else if (units.equals( HOURS ))
    {
      return seconds / SECONDS_PER_HOUR;
    }
    else if (units.equals( DAYS ))
    {
      return seconds / SECONDS_PER_DAY;
    }
    else if (units.equals( WEEKS ))
    {
      return seconds / SECONDS_PER_WEEK;
    }
    else if (units.equals( YEARS ))
    {
      return seconds / SECONDS_PER_YEAR;
    }
		
    return seconds;
  }

  @Override
  public String[] getUnits()
  {
    return VALID_UNITS;
  }

  @Override
  public IQuantity add( IQuantity another )
                        throws QuantityOperationException
  {
    if (another instanceof Duration)
    {
      Duration anotherDur = (Duration)another;
      return new Duration( "Sum", anotherDur.seconds + this.seconds );
    }
    else
    {
      throw new QuantityOperationException( "Duration: Cannot add " + another );
    }
  }

  @Override
  public IQuantity sub( IQuantity another )
                        throws QuantityOperationException
  {
    if (another instanceof Duration)
    {
      Duration anotherDur = (Duration)another;
      return new Duration( "Sum", this.seconds - anotherDur.seconds );
    }
    else
    {
      throw new QuantityOperationException( "Duration: Cannot add " + another );
    }
  }

  @Override
  public IQuantity multiply( IQuantity another )
 {
    if (another instanceof Velocity )
    {
      Velocity anotherVel = (Velocity)another;
        
      return new Distance( "dist", anotherVel.getQuantity() * this.seconds );
    }
    // else if its a ratio quantity and denominator is a Duration ...
    // return numerator * the ratio of this/denominator duration
    else if (another instanceof RatioQuantity)
    {
      RatioQuantity anotherRat = (RatioQuantity)another;
      return anotherRat.multiply( this );
    }
    else if (another instanceof Duration)
    {
      Duration anotherDur = (Duration)another;
      // This is not correct - should be new Duration with value of seconds * seconds
      Duration powDur = new Duration( "pow", this.seconds * anotherDur.seconds );
      return new PowerQuantity( "pow", powDur, 2 );
    }
    else
    {
      return new ProductQuantity( this, another );
    }
  }

  @Override
  public IQuantity divide( IQuantity another )
  {
    if (another instanceof Duration)
    {
      Duration anotherDur = (Duration)another;
      double ratio = this.seconds / anotherDur.seconds;
      return new ScalarQuantity( "div", ratio );
    }
		
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

}
