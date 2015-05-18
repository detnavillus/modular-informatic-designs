package com.modinfodesigns.property.transform.string;

public class ConcatenateStringTransform implements IStringTransform
{
  private String prepend = "";
  private String append  = "";
    
  @Override
  public String transformString( String inputString ) throws StringTransformException
  {
    if (inputString == null)
    {
      throw new StringTransformException( "Input String is NULL!" );
    }
    return StringTransform.assembleString( prepend, inputString, append );
  }

  @Override
  public String transformString( String sessionID, String inputString ) throws StringTransformException
  {
    if (inputString == null)
    {
      throw new StringTransformException( "Input String is NULL!" );
    }
    return StringTransform.assembleString( prepend, inputString, append );
  }
	
  public void setPrepend( String prepend )
  {
    this.prepend = prepend;
    if (this.prepend == null ) this.prepend = "";
  }
	
  public void setAppend( String append )
  {
    this.append = append;
    if (this.append == null ) this.append = "";
  }

}
