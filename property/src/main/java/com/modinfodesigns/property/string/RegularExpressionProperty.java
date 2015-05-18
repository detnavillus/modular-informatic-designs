package com.modinfodesigns.property.string;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IExposeInternalProperties;
import com.modinfodesigns.property.PropertyValidationException;
import com.modinfodesigns.property.schema.PropertyDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String property that conforms to a Regular Expression 
 * 
 * @author Ted Sullivan
 */
public class RegularExpressionProperty extends StringProperty implements IExposeInternalProperties
{
  private String regularExpression;
    
  private Pattern regPat;
    
  public RegularExpressionProperty(  ) {  }
    
  public RegularExpressionProperty( String name, String regularExpression )
  {
    setName( name );
    this.regularExpression = regularExpression;
  }
    
  @Override
  public String getType()
  {
    return getClass().getCanonicalName( );
  }

  @Override
  public void setValue( String value, String format )
	 		throws PropertyValidationException
  {
    if (regularExpression == null)
    {
      throw new PropertyValidationException( "Cannot validate: regular expression is NULL!" );
    }
		
    init( );
        
    // check that the value matches the RegExpr that
    // defines the formatting of this String property
    // if it does, call super.setValue( value, format )
    // if not, throw a PropertyValidationExpression
    Matcher matcher = regPat.matcher( value );
    if (matcher.matches( ))
    {
      super.setValue( value,  format );
    }
    else
    {
      throw new PropertyValidationException( "Input: " + value + " does not match pattern  " + regularExpression );
    }
  }
	
  public void setRegularExpression( String regExpr )
  {
    this.regularExpression = regExpr;
  }

  @Override
  public String getDefaultFormat()
  {
    return regularExpression;
  }
	
  public String getRegularExpression()
  {
    return regularExpression;
  }

  @Override
  public IProperty copy()
  {
    RegularExpressionProperty copy = (RegularExpressionProperty)super.copy( );
    copy.regularExpression = this.regularExpression;
    return copy;
  }

  private void init( )
  {
    if (regPat != null) return;
    	
    synchronized ( this )
    {
      if (regPat != null) return;
      this.regPat = Pattern.compile( this.regularExpression );
    }
    	
  }

  @Override
  public List<PropertyDescriptor> getInternalProperties()
  {
    ArrayList<PropertyDescriptor> internalProps = new ArrayList<PropertyDescriptor>( );
    internalProps.addAll( super.getInternalProperties( ) );
        
    PropertyDescriptor regProp = new PropertyDescriptor( );
    regProp.setName( "RegularExpression" );
    regProp.setPropertyType( "String" );
    regProp.setDisplayName( "Regular Expression" );
    internalProps.add( regProp );
        
    return internalProps;
  }
}
