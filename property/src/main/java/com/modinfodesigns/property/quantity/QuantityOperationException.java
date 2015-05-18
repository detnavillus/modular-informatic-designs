package com.modinfodesigns.property.quantity;

import com.modinfodesigns.property.PropertyValidationException;

public class QuantityOperationException extends PropertyValidationException
{
   private static final long serialVersionUID = 1L;
   
   public QuantityOperationException( String message )
   {
	   super(message );
   }
}
