package com.modinfodesigns.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IComputableProperties;
import com.modinfodesigns.property.string.StringProperty;

/**
 * Describes a Sort request.  Sortable field and sort direction. Nesting enables multiple
 * sort levels to be specified.
 * 
 * Subclasses of SortProperty can override the getValue( ) method to
 * format the sort output in a platform-specific manner.
 * 
 * @author Ted Sullivan
 *
 */

public class SortProperty implements IProperty, IComputableProperties
{
  private static ArrayList<String> intrinsics;
	
  public static final String SORT_FIELD = "SortField";
  public static final String SORT_DIRECTION = "SortDirection";
  public static final String SECONDARY_SORT = "SecondarySort";
	
  public static final String ASCENDING = "ASC";
  public static final String DESCENDING = "DESC";
	
  public static final String RELEVANCE = "Relevance";
	
  private String sortField;
    
  private String direction = ASCENDING; // ASC | DESC
  private String delimiter = ":";
    
  private SortProperty secondarySort;
    
  static
  {
    intrinsics = new ArrayList<String>( );
    intrinsics.add( SORT_FIELD );
    intrinsics.add( SORT_DIRECTION );
    intrinsics.add( SECONDARY_SORT );
  }
    
  @Override
  public String getName()
  {
    return this.sortField;
  }

  @Override
  public void setName(String name)
  {
    setSortField( name );
  }
	
  public void setSortField( String sortField )
  {
    this.sortField = sortField;
  }
	
  public String getSortField(  )
  {
    return this.sortField;
  }
	
  public void setSortDirection( String sortDirection )
  {
    this.direction = sortDirection;
  }
	
  public String getSortDirection(  )
  {
    return this.direction;
  }


  @Override
  public String getType()
  {
    return "com.modinfodesigns.search.SortProperty";
  }

  @Override
  public String getValue()
  {
    String thisSort = new String( sortField + ":" + direction );
    
    if (secondarySort != null)
    {
      thisSort = new String( thisSort + "|" + secondarySort.getValue() );
    }
		
    return thisSort;
  }

  @Override
  public String getValue(String format)
  {
    String thisSort = new String( sortField + ":" + direction );
		
    if (secondarySort != null)
    {
      thisSort += "|" + secondarySort.getValue( format );
    }
		
    return thisSort;
  }

  @Override
  public void setValue(String value, String format)
  {
    if (format.equals( IProperty.DELIMITED_FORMAT ) && format.indexOf( delimiter ) > 0)
    {
      sortField = format.substring( 0, format.indexOf( delimiter )).trim();
      direction = format.substring( format.indexOf( delimiter ) + 1 ).trim();
    }
    else
    {
      sortField = value;
    }
  }

  @Override
  public IProperty copy()
  {
    SortProperty copy  = new SortProperty( );
    copy.sortField = this.sortField;
    copy.direction = this.direction;
    copy.secondarySort = (secondarySort != null) ? (SortProperty)secondarySort.copy() : null;
    
    return copy;
  }
	
  public void setSecondarySort( SortProperty secondarySort )
  {
    if (this.secondarySort != null)
    {
      this.secondarySort.setSecondarySort( secondarySort );
    }
    else
    {
      this.secondarySort = secondarySort;
    }
  }
	
  public SortProperty getSecondarySort(  )
  {
    return this.secondarySort;
  }
	
  public SortProperty[] getSortProperties(  )
  {
    if (secondarySort == null)
    {
      SortProperty[] sortList = new SortProperty[1];
      sortList[0] = this;
      return sortList;
    }
    else
    {
      ArrayList<SortProperty> sortList = new ArrayList<SortProperty>( );
      sortList.add( this );
      secondarySort.addToSortList( sortList );
        
      SortProperty[] outputArray = new SortProperty[ sortList.size( ) ];
      sortList.toArray( outputArray );
      return outputArray;
    }
  }
	
  private void addToSortList( ArrayList<SortProperty> sortList )
  {
    sortList.add( this );
    if (secondarySort != null) secondarySort.addToSortList( sortList );
  }

  @Override
  public Object getValueObject()
  {
    return null;
  }

  @Override
  public String getDefaultFormat()
  {
    return null;
  }

  @Override
  public List<String> getIntrinsicProperties()
  {
    return intrinsics;
  }

  @Override
  public IProperty getIntrinsicProperty( String name )
  {
    if (name.equals( SORT_FIELD ) && sortField != null)
    {
      return new StringProperty( SORT_FIELD, sortField );
    }
    else if (name.equals( SORT_DIRECTION ) && direction != null )
    {
      return new StringProperty( SORT_DIRECTION, direction );
    }
    else if (name.equals( SECONDARY_SORT ) && direction != null )
    {
      return secondarySort;
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
