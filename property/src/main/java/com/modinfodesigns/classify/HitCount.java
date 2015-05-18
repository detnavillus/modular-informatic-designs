package com.modinfodesigns.classify;

import com.modinfodesigns.property.quantity.IntegerProperty;
import com.modinfodesigns.property.IProperty;

public class HitCount extends IntegerProperty
{
  public HitCount( ) {  }
	
  public HitCount( int value )
  {
    this.value = value;
  }

  public void increment( )
  {
    this.value += 1;
  }
	
  @Override
  public IProperty copy( )
  {
    HitCount copy = new HitCount( );
    copy.value = this.value;
		
    return copy;
  }
}
