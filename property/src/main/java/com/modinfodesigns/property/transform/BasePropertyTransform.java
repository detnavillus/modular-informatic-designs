package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.IProperty;

public abstract class BasePropertyTransform implements IPropertyTransform
{
  public abstract IProperty transform(IProperty input) throws PropertyTransformException;

  @Override
  public void startTransform(IProperty input, IPropertyTransformListener listener ) throws PropertyTransformException
  {
    // Create a TransformRunner in a new Thread,
    TransformRunner tr = new TransformRunner( input, listener, this );
    Thread thread = new Thread( tr );
    thread.start( );
  }
	
  private class TransformRunner implements Runnable
  {
    private IProperty inputProperty;
    private IPropertyTransform propTransform;
    private IPropertyTransformListener ptListener;
		
    public TransformRunner( IProperty inputProperty, IPropertyTransformListener listener, IPropertyTransform propTransform )
    {
        this.inputProperty = inputProperty;
        this.propTransform = propTransform;
        this.ptListener = listener;
    }
	    
    @Override
    public void run()
    {
        IProperty outputProperty = null;
        String status = null;
					
        try
        {
            outputProperty = propTransform.transform( inputProperty );
            status = "COMPLETED";
        }
        catch ( PropertyTransformException pte )
        {
            status = "FAILED";
        }
					
        ptListener.transformComplete( outputProperty, status );

    }
  }
}
