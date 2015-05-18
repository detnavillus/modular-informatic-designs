package com.modinfodesigns.property;

/**
 * Links derivable or 'Computable' properties of object properties to their parent 
 * DataObject. This enables these properties to be 'seen' at the DataObject level.
 * 
 * Requires the following information:
 * <ul>
 *   <li> Name of the source property (must implement IComputableProperties)
 *   <li> Name of the computable property that the source property exposes
 *   <li> Name of the parameter or target Property that will be used by the sourceProperty 
 *        to do its computation
 *   <li> Optional default property to be returned if the computation cannot be
 *        completed.
 *  </ul>
 * 
 * @author Ted Sullivan
 *
 */
public class ComputablePropertyDelegate implements IFunctionProperty
{
  private String name;
  private IPropertyHolder propHolder;
  private String function;
    
  private String sourcePropName;
  private String computable;
  private String targetPropName;
  private String defaultPropName;
    
  public ComputablePropertyDelegate( ) {  }
    
  public ComputablePropertyDelegate( String name, String function, IPropertyHolder propHolder )
  {
    setName( name );
    setFunction( function );
    setPropertyHolder( propHolder );
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

  @Override
  public String getType()
  {
    IProperty computableProp = execute( );
    return (computableProp != null) ? computableProp.getType( ) : null;
  }

  @Override
  public String getValue()
  {
    IProperty computableProp = execute( );
    return (computableProp != null) ? computableProp.getValue( ) : null;
  }

  @Override
  public String getValue( String format )
  {
    IProperty computableProp = execute( );
    return (computableProp != null) ? computableProp.getValue( format ) : null;
  }

  @Override
  public void setValue(String value, String format)
                       throws PropertyValidationException
  {
    throw new PropertyValidationException( "Cannot setValue on FunctionProperty" );
  }

  @Override
  public String getDefaultFormat()
  {
    IProperty computableProp = execute( );
    return (computableProp != null) ? computableProp.getDefaultFormat( ) : null;
  }

  @Override
  public IProperty copy()
  {
    ComputablePropertyDelegate cpd = new ComputablePropertyDelegate( this.name, this.function, this.propHolder );
    return cpd;
  }

  @Override
  public Object getValueObject()
  {
    IProperty computableProp = execute( );
    return (computableProp != null) ? computableProp.getValueObject( ) : null;
  }

  @Override
  public void setPropertyHolder(IPropertyHolder propHolder)
  {
    this.propHolder = propHolder;
  }


  /**
   * Sets the function:
   * format  [sourcePropertyName].[computable]:[targetPropertyName]|[defaultPropertyName]
   */
  @Override
  public void setFunction( String function )
  {
    this.function = function;
    
    // parse the component pieces
    if (function != null && function.indexOf( ":" ) > 0)
    {
      String leftPart = new String( function.substring( 0, function.indexOf( ":" )));
      String rightPart = new String( function.substring( function.indexOf( ":" ) + 1 ));
        
      this.sourcePropName = new String( leftPart.substring( 0, leftPart.lastIndexOf( "." )));
      this.computable = new String( leftPart.substring( leftPart.lastIndexOf( "." ) + 1 ));
        
      if (rightPart.indexOf ( "|" ) > 0)
      {
        this.targetPropName = new String (rightPart.substring( 0, rightPart.indexOf( "|" )));
        this.defaultPropName = new String( rightPart.substring( rightPart.indexOf( "|" ) + 1 ));
      }
      else
      {
        this.targetPropName = rightPart;
      }
    }
  }

  @Override
  public String getFunction()
  {
    return this.function;
  }

  @Override
  public IProperty execute()
  {
    if (sourcePropName == null || propHolder == null) return null;
		
    IProperty sourceProp = propHolder.getProperty( sourcePropName );
    if (sourceProp == null || (sourceProp instanceof IComputableProperties) == false) return null;
    
    IComputableProperties computableProp = (IComputableProperties)sourceProp;
    IProperty targetProp = propHolder.getProperty( targetPropName );
    IProperty computed = (targetProp != null)
                       ? computableProp.getComputedProperty( computable, targetProp )
                       : null;
                           
    return (computed == null && defaultPropName != null)
            ? propHolder.getProperty( defaultPropName )
            : computed;
  }
	
  @Override
  public boolean isMultiValue()
  {
    IProperty computedProp = execute( );
    return (computedProp != null) ? computedProp.isMultiValue(  ) : false;
  }

  @Override
  public String[] getValues(String format)
  {
    IProperty computedProp = execute( );
    return (computedProp != null) ? computedProp.getValues( format ) : null;
  }

}
