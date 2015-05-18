package com.modinfodesigns.property;

import com.modinfodesigns.property.string.StringProperty;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Function Property that enables a selection of a value DataObject multi-value property ( the 'selection property')
 * to create a target property that represents the current selection. Creates an EnumerationProperty of the Data Objects' 
 * multi-value property and manages the state of the selection for the client DataObject.
 * 
 * @author Ted Sullivan
 */

public class SelectionProperty extends EnumerationProperty implements IFunctionProperty
{
  private transient static final Logger LOG = LoggerFactory.getLogger( SelectionProperty.class );

  private IPropertyHolder propHolder;
  private String selectionProperty;
  private String targetProperty;
    
  private String delimiter = "=";
  private String function;
    
  private boolean executed = false;
    
  @Override
  public void setPropertyHolder(IPropertyHolder propHolder)
  {
    LOG.debug( "setPropertyHolder: " + propHolder );
    this.propHolder = propHolder;
    executed = false;
  }


  /**
   * Function format: [targetProperty]=[selectionProperty]
   */
  @Override
  public void setFunction( String function )
  {
    LOG.debug( "setFunction( '" + function + "'" );
		
    if (function != null && function.indexOf( delimiter ) > 0 )
    {
      this.targetProperty = new String( function.substring(0, function.indexOf(delimiter))).trim( );
      this.selectionProperty = new String( function.substring( function.indexOf(delimiter) + delimiter.length() )).trim( );
    }
		
    LOG.debug( "got targetProperty: " + targetProperty );
    LOG.debug( "got selectionProperty: " + selectionProperty );
		
    this.function = function;
    executed = false;
  }

  @Override
  public String getFunction()
  {
    return  this.function;
  }

  @Override
  public IProperty execute()
  {
    LOG.debug( "execute( )" );
    if (executed) return this;
    // create the choice list from the selected Function...

    IProperty selProp = propHolder.getProperty( selectionProperty );
    this.setMultiValue( selProp.isMultiValue( ) );
        
    if (selProp != null)
    {
      LOG.debug( "Have Dependent Property now: " + selProp );
      if (selProp instanceof PropertyList )
      {
        setChoices( (PropertyList)selProp );
      }
      else if (selProp instanceof DataList)
      {
        LOG.debug( "Creating Enumeration of Data Objects ..." );
        if (choices != null) choices.clear( );
        for (Iterator<DataObject> dObjIt = ((DataList)selProp).getData( ); dObjIt.hasNext( ); )
        {
          DataObject dobj = dObjIt.next( );
          addChoice( new StringProperty( dobj.getName( ), dobj.getName( ) ) );
        }
      }
      else if (selProp.getValues( selProp.getDefaultFormat() ) != null )
      {
        LOG.debug( "Is MultiValue - setting up EnumerationProperty " );
        String[] dependentValues = selProp.getValues( selProp.getDefaultFormat() );
        if (dependentValues != null && dependentValues.length > 0)
        {
          if (choices != null) choices.clear( );
          for (int i = 0, isz = dependentValues.length; i < isz; i++)
          {
            StringProperty choiceProp = new StringProperty( dependentValues[i], dependentValues[i] );
            addChoice( choiceProp );
          }
        }
      }
    }
        
    executed = true;
    return this;
  }
	
  @Override
  public int size( )
  {
    execute( );
    return super.size( );
  }
	
  @Override
  public Iterator<IProperty> getChoices( )
  {
    execute( );
    return super.getChoices( );
  }
    
  public PropertyList getChoiceList( )
  {
    execute( );
    return super.getChoiceList( );
  }
    
  public List<String> getChoiceNames( )
  {
    execute( );
    return super.getChoiceNames( );
  }

  @Override
  public void setValue( String value, String format ) throws PropertyValidationException
  {
    // force re-execute to get the latest and greatest ...
    executed = false;
    execute( );
    	
    super.setValue( value, format );
    	
    if (getSelected( ) != null)
    {
      IProperty selected = getSelected( );
      IProperty targetProp = selected.copy( );
      targetProp.setName( targetProperty );
      propHolder.setProperty( targetProp );
    }
  }
    
    
  @Override
  public void setSelected( String[] values )
  {
    executed = false;
    execute( );
    
    super.setSelected( values );
    	
    if (getSelected( ) != null)
    {
      IProperty selected = getSelected( );
      IProperty targetProp = selected.copy( );
      targetProp.setName( targetProperty );
      propHolder.setProperty( targetProp );
    }
  }
    
  @Override
  public IProperty copy()
  {
    SelectionProperty copy = new SelectionProperty( );
    copy.setFunction( this.function );
    copy.setPropertyHolder( this.propHolder );
    copy.setName( this.getName( ) );
        
    return copy;
  }
    
  public String getTargetProperty( )
  {
    return this.targetProperty;
  }
    
  @Override
  public boolean isSelected( String value )
  {
    LOG.debug( "isSelected: " + value );
    boolean superSel = super.isSelected( value );
    if (superSel) return true;
    	
    IProperty targetProp = (propHolder != null) ? propHolder.getProperty( targetProperty ) : null;
    if (targetProp != null) LOG.debug( "TargetProp = " + targetProp.getValue( ) );
    return (targetProp != null && targetProp.getValue( ).equals( value ));
  }
    
  @Override
  public IProperty getSelected( )
  {
    IProperty superSel = super.getSelected( );
    if (superSel != null) return superSel;
    return (propHolder != null) ? propHolder.getProperty( targetProperty ) : null;
  }
}
