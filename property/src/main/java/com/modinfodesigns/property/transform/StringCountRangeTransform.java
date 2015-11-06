package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataObjectDelegate;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.string.StringCountProperty;
import com.modinfodesigns.property.string.StringProperty;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

public class StringCountRangeTransform implements IPropertyHolderTransform
{
  private String stringCountPropertyName;
  private String rangePropertyPrefix;
  private String rangePropertySuffix = "";
    
  public void setStringCountPropertyName( String stringCountPropertyName )
  {
    this.stringCountPropertyName = stringCountPropertyName;
  }
    
  public void setRangePropertyPrefix( String rangePropertyPrefix )
  {
    this.rangePropertyPrefix = rangePropertyPrefix;
  }
    
  public void setRangePropertySuffix( String rangePropertySuffix )
  {
    this.rangePropertySuffix = rangePropertySuffix;
  }
    
  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    if (input instanceof IPropertyHolder )
    {
      return transformPropertyHolder( (IPropertyHolder)input );
    }

    return input;
  }
    
  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input )
                         throws PropertyTransformException
  {

    IProperty stringCountProp = input.getProperty( stringCountPropertyName );
    if (stringCountProp != null && stringCountProp instanceof StringCountProperty ) {
      StringCountProperty scp = (StringCountProperty)stringCountProp;
      System.out.println( input.getName( ) + " transforming " + scp.getName( ) );
        
      // get the maximum count and the number of counts - calculate mean, std dev.
      double mean = scp.getMeanCount( );
      double std_dev = scp.getStandardDeviation( );
      std_dev = Math.sqrt( std_dev );
      int maxCount = scp.getMaximumCount( );
        
      System.out.println( "mean: " + mean + " std_dev " + std_dev + " max " + maxCount );
        
      ArrayList<String> firstGroup = new ArrayList<String>( );
      ArrayList<String> secondGroup = new ArrayList<String>( );
      ArrayList<String> thirdGroup = new ArrayList<String>( );
            
      String[] strings = scp.getStringList( );
      for (int i = 0; i < strings.length; i++)
      {
        int count = scp.getCount( strings[i] );
        if (count >= (int)(mean + std_dev))
        {
          System.out.println( "Significant relation: " + strings[i] + " (" + Integer.toString( count ) + ")" );
          firstGroup.add( strings[i] );
        }
        else if (count <= (int)(mean - std_dev))
        {
          thirdGroup.add( strings[i] );
        }
        else
        {
          secondGroup.add( strings[i] );
        }
      }
             
      int outputG = 0;
      if (firstGroup.size() > 0)  addProperties( input, firstGroup, ++outputG );
      if (secondGroup.size() > 0) addProperties( input, secondGroup, ++outputG );
      if (thirdGroup.size() > 0)  addProperties( input, thirdGroup, outputG );
    }

    return input;
  }
    
  private void addProperties( IPropertyHolder input, List<String> strings, int groupNum )
  {
    String fieldName = rangePropertyPrefix + Integer.toString( groupNum ) + rangePropertySuffix;
    for( String string : strings )
    {
      System.out.println( "add Range Property " + input.getName() + " " + fieldName + " = " + string );
      input.addProperty( new StringProperty( fieldName, string ));
    }
  }
    
  @Override
  public void startTransform( IProperty input,
                              IPropertyTransformListener transformListener)
                              throws PropertyTransformException
  {
        
  }
    
}