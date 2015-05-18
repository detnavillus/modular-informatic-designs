package com.modinfodesigns.property;

import com.modinfodesigns.utils.StringMethods;

import com.modinfodesigns.property.string.StringProperty;

import java.util.LinkedHashMap;
import java.util.Collection;
import java.util.Iterator;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements an Enumeration of properties - the value of the EnumerationProperty
 * must be one of the set of property choices.  The setValue method takes the name of the
 * selected property to set or a delimited list of choice names if the property is multi-value.
 * 
 * @author Ted Sullivan
 */
public class EnumerationProperty implements IProperty
{
  private transient static final Logger LOG = LoggerFactory.getLogger( EnumerationProperty.class );

  protected LinkedHashMap<String,IProperty> choices;
    
  // Either a StringProperty, PropertyList or DataList depending on whether
  // the enumeration property is multi-value and the type of choice properties that
  // are added to it.
  private IProperty selected;
    
  private String name;
    
  private boolean multiValue = false;
    
  private boolean useDataList = false;
    
    
  public EnumerationProperty(  ) {  }
    
  public EnumerationProperty( String name )
  {
    this.name = name;
  }
    
  public EnumerationProperty( String name, boolean multiValue )
  {
    this.name = name;
    this.multiValue = multiValue;
  }
    
  @Override
  public String getName()
  {
    return this.name;
  }

  @Override
  public void setName( String name )
  {
    this.name = name;
  }
    
  public void setMultiValue( boolean multiValue )
  {
    this.multiValue = multiValue;
  }
    
  @Override
  public boolean isMultiValue(  )
  {
    return this.multiValue;
  }

  @Override
  public String getType()
  {
    return getClass().getCanonicalName( );
  }

  @Override
  public String getValue( )
  {
    return (selected != null) ? selected.getValue( ) : null;
  }

  @Override
  public String getValue( String format )
  {
    return (selected != null) ? selected.getValue( format ) : null;
  }

  @Override
  public void setValue( String value, String format ) throws PropertyValidationException
  {
    if (choices == null)
    {
      LOG.debug( "setValue called: choices is null!" );
      throw new PropertyValidationException( "Choices is NULL!" );
    }
    	
    if (format != null && format.startsWith( "DELIMITER=" ) && multiValue )
    {
      String delimiter = new String( format.substring( "DELIMITER=".length() ) );
      String[] values = StringMethods.getStringArray( value, delimiter );
      if (values != null)
      {
        setSelected( values );
      }
    }
    else
    {
      // set the selected property based on the value == property name
      for (IProperty prop : choices.values())
      {
        if (prop.getValue( ).equals( value ))
        {
          selected = prop;
          break;
        }
        else if (prop.getName( ).equals( value ))
        {
          selected = prop;
          break;
        }
      }
    }
        
    if (selected == null)
    {
      throw new PropertyValidationException( "Can't set value = '" + value + "' Not in Choice List!" );
    }
  }

  @Override
  public String getDefaultFormat()
  {
    return (selected != null) ? selected.getDefaultFormat() : null;
  }

  @Override
  public IProperty copy()
  {
    EnumerationProperty copy = new EnumerationProperty( );
    if (this.choices != null)
    {
      copy.setChoices( this.choices.values() );
    }
        
    if (this.selected != null)
    {
      try
      {
        if (multiValue)
        {
          copy.setValue( this.selected.getValue( "DELIMITER=|" ), "DELIMITER=|" );
        }
        else
        {
          copy.setValue( this.selected.getValue( ), null );
        }
      }
      catch (PropertyValidationException pve )
      {
        		
      }
    }
        
    copy.name = this.name;
    copy.multiValue = this.multiValue;
        
    return copy;
  }

  @Override
  public Object getValueObject()
  {
    return (selected != null) ? selected.getValueObject() : null;
  }
    
  public int size( )
  {
    int size = 0;
    if (choices != null)
    {
      for (Iterator<IProperty> propIt = choices.values().iterator(); propIt.hasNext(); )
      {
        IProperty prop = propIt.next( );
        if (prop != null && prop.getName() != null && prop.getName().trim().length() > 0)
        {
          ++size;
        }
      }
    }
    	
    return size;
  }
    
  public Iterator<IProperty> getChoices( )
  {
    return (choices != null) ? choices.values().iterator() : null;
  }
    
  public PropertyList getChoiceList( )
  {
    if (choices == null) return null;
    
    PropertyList pList = new PropertyList( );
    for (Iterator<IProperty> propIt = choices.values().iterator(); propIt.hasNext(); )
    {
      pList.addProperty( propIt.next() );
    }
        
    return pList;
  }
    
  public List<String> getChoiceNames( )
  {
    if (choices == null) return null;
    ArrayList<String> names = new ArrayList<String>( );
    for ( String name : choices.keySet() )
    {
      names.add( name );
    }
        
    return names;
  }
    
  public void setChoices( Collection<IProperty> choices )
  {
    if (choices == null) return;
        
    if (this.choices != null) this.choices.clear();
        
    for (IProperty choice : choices )
    {
      addChoice( choice );
    }
  }
    
  public void setChoices( PropertyList choices )
  {
    if (this.choices != null) this.choices.clear();
        
    Iterator<IProperty> propIt = choices.getProperties( );
    while (propIt != null && propIt.hasNext( ) )
    {
      addChoice( propIt.next( ) );
    }
  }
    
  public void setChoices( String[] choices )
  {
    if (this.choices != null) this.choices.clear();
        
    for (int i = 0; i < choices.length; i++)
    {
      addChoice( new StringProperty( choices[i], choices[i] ) );
    }
  }
    
  public void setChoices( DataList choices )
  {
    if (choices == null) return;
        
    if (this.choices != null) this.choices.clear();
        
    useDataList = true;
        
    Iterator<DataObject> dobjIt = choices.getData( );
    while (dobjIt != null && dobjIt.hasNext( ) )
    {
      addChoice( dobjIt.next( ) );
    }
  }
    
  public void addChoice( IProperty choice )
  {
    if (choices == null) choices = new LinkedHashMap<String,IProperty>( );
    choices.put( choice.getName(), choice );
    
    if (choice instanceof DataObject)
    {
      // want to make the selected thingy a DataList
      useDataList = true;
    }
  }

  public void setSelected( String[] values )
  {
    if (choices == null)
    {
      LOG.debug( "Choices is NULL - cannot set selected values." );
      return;
    }
    	
    if (!useDataList)
    {
      PropertyList pList = new PropertyList( );
      for (int i = 0, isz = values.length; i < isz; i++)
      {
        IProperty prop = choices.get( values[i] );
        if (prop != null)
        {
          pList.addProperty( prop );
        }
      }
        
      selected = pList;
    }
    else
    {
      DataList selList = new DataList( );
      for (int i = 0, isz = values.length; i < isz; i++)
      {
        DataObject dobj = (DataObject)choices.get( values[i] );
        selList.addDataObject( dobj );
      }
    		
      selected = selList;
    }
  }
    
  public boolean isSelected( String value )
  {
    if (selected == null) return false;
    	
    if (selected instanceof PropertyList)
    {
      PropertyList selList = (PropertyList)selected;
      for (Iterator<IProperty> selIt = selList.getProperties( ); selIt.hasNext(); )
      {
        IProperty selProp = selIt.next( );
        if (selProp.getValue().equals( value ))
        {
          return true;
        }
      }
    		
      return false;
    }
    else if (selected instanceof DataList)
    {
      DataList dList = (DataList)selected;
      for (Iterator<DataObject> dobjIt = dList.getData(); dobjIt.hasNext( ); )
      {
        DataObject dobj = dobjIt.next( );
        if (dobj.getName().equals( value ))
        {
          return true;
        }
      }
    		
      return false;
    }
    	
    return (selected.getValue().equals( value ));
  }

  @Override
  public String[] getValues(String format)
  {
    if (this.selected == null) return null;
		
    if (selected instanceof PropertyList)
    {
      PropertyList selList = (PropertyList)selected;
      return selList.getValues( format );
    }
    else
    {
      String[] values = new String[1];
      values[0] = selected.getValue( format );
      return values;
    }
  }
	
  public void setSelected( IProperty selected )
  {
    this.selected = selected;
  }
	
  public IProperty getSelected( )
  {
    return selected;
  }
	
}
