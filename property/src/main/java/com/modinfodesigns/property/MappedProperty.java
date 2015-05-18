package com.modinfodesigns.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enables an alias to be established for an IProperty
 * 
 * @author Ted Sullivan
 */

public class MappedProperty implements IFunctionProperty
{
  private transient static final Logger LOG = LoggerFactory.getLogger( MappedProperty.class );

  private String name;                  // name of this property - becomes the alias
                                          // for the mapped property
  private String mappedName;
  private IPropertyHolder propHolder;

  private String useFormat;
    
  public MappedProperty(  ) {  }
    
  public MappedProperty( String name, String mappedName, IPropertyHolder propHolder )
  {
    setName( name );
    this.mappedName = mappedName;
    this.propHolder = propHolder;
    propHolder.setProperty( this );
  }
    
  public static void create( String name, String mappedName, IPropertyHolder propHolder )
  {
    if (canMap( name, mappedName, propHolder))
    {
      new MappedProperty( name, mappedName, propHolder );
    }
    else
    {
      LOG.error( "Trying to create an infinite loop!" );
    }
  }
    
  public static boolean canMap( String name, String mappedName, IPropertyHolder propHolder )
  {
    // if mappedName is a MappedProperty that points to name - don't do it!
    IProperty toBeMappedProp = propHolder.getProperty( mappedName );

    if (toBeMappedProp != null && toBeMappedProp instanceof MappedProperty )
    {
      String mapName = ((MappedProperty)toBeMappedProp).getMappedTarget( );
      // LOG.debug( mapName + " mapped to " + name );
      if (mapName.equals( name ))
      {
        return false;
      }
    }
    	
    return true;
  }
    
    
  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public void setName( String name )
  {
    this.name = name;
  }

  @Override
  public String getType( )
  {
    IProperty mappedProp = getMappedProperty( );
    return (mappedProp != null) ? mappedProp.getType( ) : getClass( ).getCanonicalName( );
  }

  @Override
  public String getValue( )
  {
    IProperty mappedProp = getMappedProperty( );
    if (mappedProp != null)
    {
      return (useFormat != null) ? mappedProp.getValue( useFormat ) : mappedProp.getValue( );
    }
    	
    return null;
  }

  @Override
  public String getValue( String format )
  {
    IProperty mappedProp = getMappedProperty( );
    return (mappedProp != null) ? mappedProp.getValue( format ) : null;
  }

  @Override
  public void setValue( String value, String format ) throws PropertyValidationException
  {
    IProperty mappedProp = getMappedProperty( );
    if (mappedProp != null)
    {
      mappedProp.setValue( value, format );
    }
  }

  @Override
  public String getDefaultFormat(  )
  {
    if (useFormat != null) return useFormat;
    	
    IProperty mappedProp = getMappedProperty( );
    return (mappedProp != null) ? mappedProp.getDefaultFormat( ) : null;
  }
    
  private IProperty getMappedProperty( )
  {
    return (propHolder != null && mappedName != null) ? propHolder.getProperty( mappedName ) : null;
  }

  @Override
  public IProperty copy(  )
  {
    MappedProperty copy = new MappedProperty(  );
    copy.name       = this.name;
    copy.mappedName = this.mappedName;
    copy.propHolder = this.propHolder;
    copy.useFormat  = this.useFormat;
    
    return copy;
  }

  @Override
  public Object getValueObject(  )
  {
    LOG.debug( "getValueObject( ) useFormat = " + useFormat );
    	
    IProperty mappedProp = getMappedProperty( );
    	
    if (mappedProp != null)
    {
      return (useFormat != null) ? mappedProp.getValue( useFormat ) : mappedProp.getValueObject( );
    }
    	
    return null;
  }

  @Override
  public void setPropertyHolder( IPropertyHolder propHolder )
  {
    this.propHolder = propHolder;
  }


  // format is name|mappedName
  @Override
  public void setFunction( String function )
  {
    if (function == null) return;
    	
    if (function.indexOf( "," ) > 0)
    {
      this.mappedName = new String( function.substring( 0, function.indexOf( "," ) ));
      this.useFormat = new String( function.substring( function.indexOf( "," ) + 1 ));
    }
    else
    {
      this.mappedName = function;
    }
  }

  @Override
  public String getFunction(  )
  {
    return mappedName;
  }
    
  public void setUseFormat( String useFormat )
  {
    this.useFormat = useFormat;
  }
    
  public void setMappedName( String mappedName )
  {
    this.mappedName = mappedName;
  }
    
  public String getMappedTarget( )
  {
    IProperty targetProp = propHolder.getProperty( mappedName );
    if (targetProp != null && targetProp instanceof MappedProperty )
    {
      MappedProperty mappedTarget = (MappedProperty)targetProp;
      return mappedTarget.getMappedTarget( );
    }
    
    return mappedName;
  }

  @Override
  public IProperty execute()
  {
    return getMappedProperty( );
  }

  @Override
  public boolean isMultiValue()
  {
    IProperty mappedProp = execute( );
    return (mappedProp != null) ? mappedProp.isMultiValue(  ) : false;
  }

  @Override
  public String[] getValues(String format)
  {
    IProperty mappedProp = execute( );
    return (mappedProp != null) ? mappedProp.getValues( format ) : null;
  }

}
