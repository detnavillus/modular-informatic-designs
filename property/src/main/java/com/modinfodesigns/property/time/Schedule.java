package com.modinfodesigns.property.time;

import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.IPropertySet;
import com.modinfodesigns.property.PropertyTypeException;

import java.util.ArrayList;

/**
 * Models a Schedule as one or more Date Ranges.
 * Supports methods to check for intersection, union, containment, etc.
 * 
 * Methods for addition and subtraction of Schedules
 * 
 * Can be created with a human readable string:
 * <pre>
 *   every tuesday from 4 PM to 5 PM
 *   every M,W,F from Noon to 1 PM
 *   every day from 12:00 PM to 12:15 PM
 * </pre>
 * 
 * @author Ted Sullivan
 */

public class Schedule extends DataList implements IPropertyHolder, IPropertySet
{
  // Merge this into DataList ....
  // one or more DateRangeProperty
  private ArrayList<DateRangeProperty> dateRanges;
	
	
  public void addDateRange( DateRangeProperty dateRange )
  {
    if (dateRanges == null) dateRanges = new ArrayList<DateRangeProperty>( );
    dateRanges.add( dateRange );
  }

  @Override
  public IPropertySet union( IPropertySet another )
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
    else
    {
      return null;
    }
  }

  @Override
  public IPropertySet intersection( IPropertySet another )
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
  {
    if (dateRanges == null) return false;
		
    if (another instanceof DateRangeProperty)
    {
        
    }
    else if (another instanceof Schedule)
    {
        
    }
		
    return false;
  }

  @Override
  public boolean intersects( IPropertySet another )
  {
    if (dateRanges == null) return false;
		
    if (another instanceof DateRangeProperty)
    {
        
    }
    else if (another instanceof Schedule)
    {
			
    }
    return false;
  }

  @Override
  public boolean contains( IProperty another )
                           throws PropertyTypeException
  {
    if (dateRanges == null) return false;
		
    if (another instanceof DateProperty || another instanceof DateRangeProperty)
    {
      for (DateRangeProperty drp : dateRanges )
      {
        if (drp.contains( another ))
        {
          return true;
        }
      }
    }
		
    return false;
  }
	
}
