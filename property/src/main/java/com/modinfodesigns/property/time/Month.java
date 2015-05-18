package com.modinfodesigns.property.time;

import com.modinfodesigns.property.EnumerationProperty;
import com.modinfodesigns.property.IExposeInternalProperties;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.schema.PropertyDescriptor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Month extends EnumerationProperty implements IExposeInternalProperties
{
  public static String[] ABBREV_MONTH_NAMES = {"Jan","Feb", "Mar", "Apr", "May", "Jun",
                                               "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
	
    public static String[] FULL_MONTH_NAMES =  { "January","February", "March", "April", "May", "June",
                                             "July", "August", "September", "October", "November", "December" };
	
  private String locale = "en-us";
    
  private String style;  // FULL | ABBREVIATED  == binary property
    
  public void setLocale( String locale )
  {
    this.locale = locale;
  }
    
  public void setStyle( String style )
  {
    this.style = style;
  }
    
    
  // now depending on the locale and the style, can get the enumeration list ...
  @Override
  public Iterator<IProperty> getChoices( )
  {
    return null;
  }
    
  @Override
  public PropertyList getChoiceList( )
  {
    return null;
  }

  @Override
  public List<PropertyDescriptor> getInternalProperties()
  {
    ArrayList<PropertyDescriptor> internalProps = new ArrayList<PropertyDescriptor>( );
    PropertyDescriptor localeDesc = new PropertyDescriptor( );
    localeDesc.setName( "Locale" );
    localeDesc.setPropertyType( "EnumerationProperty" );
    
    // get the locales ... that we support
        
    internalProps.add( localeDesc );
        
    PropertyDescriptor styleDesc = new PropertyDescriptor( );
    styleDesc.setName( "Style" );
    styleDesc.setPropertyType( "BinaryProperty" );
    styleDesc.setPropertyValues( "FULL|ABBREVIATED" );
    internalProps.add( styleDesc );
        
    return internalProps;
  }
	
  public static String[] getAbbreviatedMonthNames( String locale )
  {
    return ABBREV_MONTH_NAMES;
  }
	
  public static String[] getFullMonthNames( String locale )
  {
    return FULL_MONTH_NAMES;
  }
	
}
