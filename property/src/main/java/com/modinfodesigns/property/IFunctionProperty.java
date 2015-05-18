package com.modinfodesigns.property;

/**
 * Special class of IProperty that can execute a Function on an IPropertyHolder. When added to
 * an IPropertyHolder (e.g. a DataObject), it gets a handle on the IPropertyHolder from which it 
 * can get the object's other property values that it needs to compute its function or effect 
 * a change in the IPropertyHolder.
 * 
 * @author Ted Sullivan
 */

public interface IFunctionProperty extends IProperty
{
  // used for getValue( format ) to return the function itself
	
  public static final String FUNCTION = "Function";
	
  /**
   * Sets the IPropertyHolder context for this function.
   *
   * @param propHolder
   */
  public void setPropertyHolder( IPropertyHolder propHolder );
    
    
  /**
   * Sets the function that will be applied by this Function Property (implementation
   * dependent).
   *
   * @param function
   */
  public void setFunction( String function );
    
  /**
   * Returns the function that will be applied by this Function Property (implementation
   * dependent).
   *
   * @param function
   */
  public String getFunction(  );
    
  public IProperty execute(  );
}
