package com.modinfodesigns.property;

public class PropertyValidationException extends Exception 
{
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public PropertyValidationException(  )
  {
    super( );
  }
	
  public PropertyValidationException( String message )
  {
    super( message );
  }

}
