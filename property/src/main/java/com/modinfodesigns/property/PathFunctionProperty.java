package com.modinfodesigns.property;

import com.modinfodesigns.property.string.StringProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathFunctionProperty extends DataObject implements IFunctionProperty
{
  private transient static final Logger LOG = LoggerFactory.getLogger( PathFunctionProperty.class );

  private String path;
  private String function;
  private IPropertyHolder propHolder;
    
  private String valType = "VALUE";
    
  @Override
  public void setPropertyHolder( IPropertyHolder propHolder )
  {
    this.propHolder = propHolder;
  }


  @Override
  public void setFunction( String function )
  {
    LOG.debug( "setFunction: " + function );
    this.function = function;
		
    // if path ends with ".type" strip .type and
    // set valType flag when getValue is called.
    if (function.endsWith( ".type" ) || function.equals( "type" ))
    {
      valType = "TYPE";
      if (function.endsWith( ".type" ))
      {
        this.path = new String( function.substring( 0, function.indexOf( ".type" )));
      }
    }
    else if (function.endsWith( ".name" ) || function.equals( "name" ))
    {
      valType = "NAME";
      if (function.endsWith( ".name" ))
      {
        this.path = new String( function.substring( 0, function.indexOf( ".name" )));
      }
    }
    else
    {
      this.path = function;
    }
  }

  @Override
  public String getFunction( )
  {
    return this.path;
  }

  @Override
  public IProperty execute( )
  {
    LOG.debug( "execute: " + propHolder + " with " + path );
		
    IProperty pathProp = (propHolder != null) ? ((path != null) ? propHolder.getProperty( path ) : propHolder) : null;
		
    if ( pathProp != null && pathProp instanceof DataObjectDelegate )
    {
      LOG.debug( "Extracting from DataObjectDelegate" );
      DataObjectDelegate delProp = (DataObjectDelegate)pathProp;
      pathProp = delProp.getDelegate( );
    }
		
    if ( pathProp != null )
    {
			
      if (valType.equals( "TYPE" ))
      {
        LOG.debug( "Returning " + pathProp.getType() );
        return new StringProperty( "TYPE", pathProp.getType() );
      }
      else if (valType.equals( "NAME" ))
      {
        LOG.debug( "Returning " + pathProp.getName() );
        return new StringProperty( "NAME", pathProp.getName() );
      }

      LOG.debug( "Returning " + pathProp.getValue() );

      return pathProp;
    }

    LOG.error( "Could not find path Prop: " + path + " in "
                + ((propHolder != null) ? propHolder.getValue( ) : null) );
    return null;
  }


  @Override
  public String getType()
  {
    IProperty proxy = execute( );
    return (proxy != null) ? proxy.getType(  ) : null;
  }

  @Override
  public String getValue()
  {
    IProperty proxy = execute( );
    if (proxy != null)
    {
      if (valType.equals( "TYPE" ))
      {
        return proxy.getType( );
      }
      else if (valType.equals( "NAME" ))
      {
        return proxy.getName( );
      }
			
      return proxy.getValue( );
    }
		
    return null;
  }

  @Override
  public String getValue(String format)
  {
    IProperty proxy = execute( );
    return (proxy != null) ? proxy.getValue( format ) : null;
  }

  @Override
  public void setValue( String value, String format)
                        throws PropertyValidationException
  {

  }

  @Override
  public String getDefaultFormat()
  {
    IProperty proxy = execute( );
    return (proxy != null) ? proxy.getDefaultFormat(  ) : null;
  }

  @Override
  public IProperty copy()
  {
    PathFunctionProperty pfp = new PathFunctionProperty( );
    pfp.name = this.name;
    pfp.valType = this.valType;
    pfp.path = this.path;
    pfp.function = this.function;
		
    return pfp;
  }

  @Override
  public Object getValueObject()
  {
    IProperty proxy = execute( );
    return (proxy != null) ? proxy.getValueObject(  ) : null;
  }

  @Override
  public IProperty getProperty( String name )
  {
    return execute( );
  }
}
