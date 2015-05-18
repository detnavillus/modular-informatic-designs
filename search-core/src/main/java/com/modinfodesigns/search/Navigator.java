package com.modinfodesigns.search;

import java.util.ArrayList;
import java.util.List;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;

public class Navigator extends DataObject implements INavigator
{
  private ArrayList<INavigatorField> navFields;
    
  public Navigator( ) {  }
    
  public Navigator( String name )
  {
    setName( name );
  }


  @Override
  public String getType()
  {
    return getClass().getCanonicalName();
  }

  @Override
  public String getValue( )
  {
    return getValue( IProperty.JSON_FORMAT );
  }
	
  @Override
  public String getValue( String format )
  {
    if (format.equals( IProperty.JSON_FORMAT ))
    {
      StringBuilder strbuilder = new StringBuilder( );
      strbuilder.append( "{ fields:[" );
      if (navFields != null)
      {
        for (int i = 0; i < navFields.size(); i++)
        {
          strbuilder.append( navFields.get( i ).getValue( format ) );
        }
      }
      strbuilder.append( "]}" );
      return strbuilder.toString();
    }
    else if (format.equals( IProperty.XML_FORMAT))
    {
      StringBuilder strbuilder = new StringBuilder( );
      strbuilder.append( "<Navigator name=\"" ).append( getName() ).append( "\">" );
      if (navFields != null)
      {
        for (int i = 0; i < navFields.size(); i++)
        {
          strbuilder.append( navFields.get( i ).getValue( format ) );
        }
      }
			
      strbuilder.append( "</Navigator>" );
      return strbuilder.toString();
    }
		
    return super.getValue( format );
  }

  @Override
  public String getDefaultFormat()
  {
    return null;
  }

  @Override
  public IProperty copy()
  {
    Navigator copy = new Navigator( this.name );
		
    return copy;
  }

  @Override
  public Object getValueObject()
  {
    return navFields;
  }
	
  @Override
  public void addNavigatorField( INavigatorField navField )
  {
    if (navFields == null) navFields = new ArrayList<INavigatorField>( );
    navFields.add( navField );
  }

  @Override
  public INavigatorField getNavigatorField( String navFieldName )
  {
    if (navFields == null) return null;
    for (int i = 0; i < navFields.size(); i++)
    {
      INavigatorField navField = navFields.get( i );
      if (navField.getName().equals( navFieldName )) return navField;
    }
		
    return null;
  }
	
	
  @Override
  public List<String> getNavigatorFields()
  {
    if (navFields == null) return null;
		
    ArrayList<String> navFieldNames = new ArrayList<String>( );
    for (int i = 0; i < navFields.size(); i++)
    {
      INavigatorField navField = navFields.get( i );
      navFieldNames.add( navField.getName() );
    }
		
    return navFieldNames;
  }

}
