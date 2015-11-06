package com.modinfodesigns.property.quantity;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertySet;
import com.modinfodesigns.property.PropertyTypeException;
import com.modinfodesigns.property.IComputableProperties;
import com.modinfodesigns.property.transform.string.StringTransform;

/**
 * Represents an Integer Range, pairing a minimum and a maximum value.
 *
 * @author Ted Sullivan
 */

public class FloatRangeProperty implements IProperty, IRangeProperty, IPropertySet, IComputableProperties, Comparable<FloatRangeProperty>
{
    private static ArrayList<String> intrinsics;
    private String name;
    
    private double minimum;
    private double maximum;
    
    private String delimiter = " - ";
    
    static
    {
        intrinsics = new ArrayList<String>( );
        intrinsics.add( "Minimum" );
        intrinsics.add( "Maximum" );
    }
    
    public FloatRangeProperty(  )
    {
    	
    }
    
    public FloatRangeProperty( String name, double minimum, double maximum )
    {
        this.name = name;
        this.minimum = (minimum <= maximum) ? minimum : maximum;
        this.maximum = (minimum <= maximum) ? maximum : minimum;
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
        return this.getClass().getCanonicalName( );
    }
    
    @Override
    public String getValue()
    {
        return getValue( "{MIN} - {MAX}" );
    }
    
    @Override
    // format can be a {MIN} {MAX} type of format string
    // can be a delimiter
    public String getValue( String format )
    {
        if (format != null && format.equals( IProperty.JSON_FORMAT) )
        {
            return new String( "\"" + getName() + "\":" + getValue( IProperty.JSON_VALUE ) );
        }
        else if (format != null && format.equals( IProperty.JSON_VALUE ))
        {
            return new String( "\"" + Double.toString( minimum ) + delimiter + Double.toString( maximum ) + "\"" );
        }
        else if (format != null && format.equals( IProperty.XML_FORMAT ))
        {
            return "<Property type=\"com.modinfodesigns.property.quantity.FloatRangeProperty\"><Name>"
            + StringTransform.escapeXML( this.name ) + "</Name><Minimum>"
            + Double.toString( minimum ) + "</Minimum><Maximum>"
            + Double.toString( maximum ) + "</Maximum></Property>";
        }
        
        return new String( Double.toString( minimum ) + delimiter + Double.toString( maximum ) );
    }
    
    @Override
    public void setValue(String value, String format)
    {
        if (format == null)
        {
            
        }
        if (format.equals(IProperty.DELIMITED_FORMAT))
        {
            
        }
        else if (format.startsWith( "delimiter=" ))
        {
			
        }
    }
    
    @Override
    public IProperty copy()
    {
        return  new FloatRangeProperty( this.name, this.minimum, this.maximum );
    }
	
    public void setMinimum( double minimum )
    {
        this.minimum = minimum;
    }
	
    public double getMinimum( )
    {
        return this.minimum;
    }
	
    public void setMaximum( double maximum )
    {
        this.maximum = maximum;
    }
	
    public double getMaximum(  )
    {
        return this.maximum;
    }
	
    public boolean overlaps( FloatRangeProperty another )
    {
        if ( contains(another) || another.contains( this )) return true;
		
        return ((another.maximum >= this.minimum && another.maximum <= this.maximum) ||
                (another.minimum >= this.minimum && another.minimum <= this.minimum) ||
                (another.minimum == this.maximum) || (another.maximum == this.minimum));
    }
	
    public boolean contains( FloatRangeProperty another )
    {
        return (another.minimum >= this.minimum && another.maximum <= this.maximum);
    }
	
    public boolean contains( LongRangeProperty another )
    {
        return ((double)another.getMinimum( ) >= this.minimum && (double)another.getMaximum( ) <= this.maximum);
    }
	
    public boolean contains( double value )
    {
        return (value >= this.minimum && value <= this.maximum);
    }
	
    public FloatRangeProperty intersection( FloatRangeProperty another )
    {
        if (contains(another))
        {
            return (FloatRangeProperty)another.copy( );
        }
        else if (another.contains( this ))
        {
            return (FloatRangeProperty)copy( );
        }
        else if (overlaps( another ))
        {
            if (contains( another.minimum ))
            {
                return new FloatRangeProperty( "intersect", another.minimum, this.maximum );
            }
            else
            {
                return new FloatRangeProperty( "intersect", this.minimum, another.maximum );
            }
        }
		
        return null;
    }
	
    public FloatRangeProperty union( FloatRangeProperty another )
    {
        return new FloatRangeProperty( "union", Math.min( this.minimum, another.minimum), Math.max( this.maximum, another.maximum ));
    }
    
    
    @Override
    public int compareTo( FloatRangeProperty irp)
    {
        return (int)(minimum - irp.minimum);
    }
	
    public double distance( FloatRangeProperty another )
    {
        double distance = -1;
		
        if (another.maximum == minimum || (maximum == another.minimum))
        {
            distance = 0;
        }
        else if (another.contains( this ) || this.contains( another ))
        {
            distance = 0;
        }
        else if (another.overlaps( this ))
        {
            distance = 0;
        }
        else if (another.minimum >= maximum)
        {
            distance = another.minimum - maximum;
        }
        else if (minimum >= another.maximum)
        {
            distance = minimum - another.maximum;
        }
		
        return distance;
    }
    
    @Override
    public Object getValueObject()
    {
        return this;
    }
    
    @Override
    public String getDefaultFormat()
    {
        return "delimiter= - ";
    }
    
    @Override
    public IPropertySet union( IPropertySet another )
    throws PropertyTypeException
    {
        if (!(another instanceof FloatRangeProperty) || another == null)
        {
            throw new PropertyTypeException( "Not a FloatRangeProperty!" );
        }
		
        return union( (FloatRangeProperty)another );
    }
    
    @Override
    public IPropertySet intersection( IPropertySet another )
    throws PropertyTypeException
    {
        if (!(another instanceof FloatRangeProperty) || another == null)
        {
            throw new PropertyTypeException( "Not a FloatRangeProperty!" );
        }
		
        return intersection( (FloatRangeProperty)another );
    }
    
	
    @Override
    public boolean contains(IPropertySet another)
    throws PropertyTypeException {
        if (!(another instanceof FloatRangeProperty) || another == null)
        {
            throw new PropertyTypeException( "Not a FloatRangeProperty!" );
        }
        
        return contains( (FloatRangeProperty)another );
    }
    
	
    @Override
    public boolean contains( IProperty another )
    throws PropertyTypeException
    {
        if ( (!(another instanceof FloatRangeProperty) && !(another instanceof IQuantity))
            || another == null)
        {
            throw new PropertyTypeException( "Not a FloarRangeProperty or IntegerProperty!" );
        }
		
        if (another instanceof FloatRangeProperty)
        {
            return contains( (FloatRangeProperty)another );
        }
        else
        {
            return contains( ((IQuantity)another).getQuantity( ) );
        }
    }
    
    @Override
    public boolean intersects( IPropertySet another )
    throws PropertyTypeException
    {
        if (!(another instanceof FloatRangeProperty) || another == null)
        {
            throw new PropertyTypeException( "Not a FloatRangeProperty!" );
        }
		
        return overlaps( (FloatRangeProperty)another );
    }
    
    @Override
    public List<String> getIntrinsicProperties()
    {
        return intrinsics;
    }
    
    @Override
    public IProperty getIntrinsicProperty(String name)
    {
        if (name.equals( "Minimum" ))
        {
            
        }
        else if (name.equals( "Maximum" ))
        {
            
        }
        return null;
    }
    
    @Override
    public Map<String, String> getComputableProperties()
    {
        return null;
    }
	
    @Override
    public IProperty getComputedProperty( String name, IProperty fromProp)
    {
        // can be things like shifted etc...
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
