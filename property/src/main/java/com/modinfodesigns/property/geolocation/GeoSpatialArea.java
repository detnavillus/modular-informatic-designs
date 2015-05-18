package com.modinfodesigns.property.geolocation;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertySet;
import com.modinfodesigns.property.PropertyTypeException;
import com.modinfodesigns.property.IComputableProperties;

import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.quantity.Area;
import com.modinfodesigns.property.quantity.Distance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeoSpatialArea extends DataObject implements IPropertyHolder, IPropertySet, IComputableProperties
{
  private static ArrayList<String> computableProps;
	
  private ArrayList<GeographicLocation> perimeter;
    
  static
  {
    computableProps = new ArrayList<String>( );
    computableProps.add( "Area" );
    computableProps.add( "GeoCenter" );
    computableProps.add( "Circumference" );
  }
    
  public GeoSpatialArea( ) { }
    
  public GeoSpatialArea( String name, List<GeographicLocation> perimeterPoints )
  {
    setName( name );
    perimeter = new ArrayList<GeographicLocation>( );
    perimeter.addAll( perimeterPoints );
  }
    
    
  public Area getArea( )
  {
    return null;
  }
    
  public GeographicLocation getCenter( )
  {
    return null;
  }
    
  public Distance getCircumference( )
  {
    return null;
  }


  @Override
  public IPropertySet union(IPropertySet another) throws PropertyTypeException
  {
    if (!(another instanceof GeoSpatialArea) || another == null)
    {
      throw new PropertyTypeException( "Not a GeoSpatialLocation!"  );
    }
    return null;
  }


  @Override
  public IPropertySet intersection(IPropertySet another)  throws PropertyTypeException
  {
    if (!(another instanceof GeoSpatialArea) || another == null)
    {
      throw new PropertyTypeException( "Not a GeoSpatialLocation!"  );
    }
    return null;
  }


  @Override
  public boolean contains( IPropertySet another )
		                     throws PropertyTypeException
  {
    if (!(another instanceof GeoSpatialArea) || another == null)
    {
      throw new PropertyTypeException( "Not a GeoSpatialLocation!"  );
    }
		
    return false;
  }


  @Override
  public boolean intersects( IPropertySet another )
			                   throws PropertyTypeException
  {
    if (!(another instanceof GeoSpatialArea) || another == null)
    {
      throw new PropertyTypeException( "Not a GeoSpatialLocation!"  );
    }
		
    return false;
  }


  @Override
  public boolean contains( IProperty another )
			                throws PropertyTypeException
  {

    if ( (!(another instanceof GeoSpatialArea) && !(another instanceof GeographicLocation) )
         || another == null)
    {
      throw new PropertyTypeException( "Not a GeoSpatialLocation!"  );
    }
    
    return false;
  }


  /**
   * Returns Area and GeoCenter
   * @return
   */
  @Override
  public List<String> getIntrinsicProperties()
  {
    return computableProps;
  }


  @Override
  public IProperty getIntrinsicProperty(String name)
  {
    if (name.equals("Area" ))
    {
      return getArea( );
    }
    else if (name.equals( "GeoCenter" ))
    {
      return getCenter( );
    }
    return null;
  }

  @Override
  public Map<String, String> getComputableProperties()
  {
    return null;
  }
	
  @Override
  public IProperty getComputedProperty(String name, IProperty fromProp)
  {
    // if name = "Distance" and fromProp is a GeoLocation property ...
		
    return null;
  }

}
