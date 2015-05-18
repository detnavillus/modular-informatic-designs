package com.modinfodesigns.classify;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.quantity.IQuantity;
import com.modinfodesigns.property.quantity.QuantityOperationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * A IQuantity implementation that expresses a term's 'tf': ratio of Term Count (HitCount) 
 * to tokenCount.
 * 
 * @author Ted Sullivan
 */
public class TermFrequency implements IQuantity
{
  private transient static final Logger LOG = LoggerFactory.getLogger( TermFrequency.class );

  private HitCount termCount;
    
  private HitCount tokenCount;
  private String name;
    
  private String[] UNITS = {"FRACTION","PERCENT"};
    
  // formats T Count / Token Count, Fraction, Percent
    
  public TermFrequency( ) {  }
    
  public TermFrequency( HitCount termCount, HitCount tokenCount )
  {
    this.termCount = termCount;
    this.tokenCount = tokenCount;
  }
    
  public TermFrequency( int termCount, int tokenCount )
  {
    this.termCount = new HitCount( termCount );
    this.tokenCount = new HitCount( tokenCount );
  }

  @Override
  public String getName()
  {
    return this.name;
  }

  @Override
  public void setName(String name)
  {
    this.name = name;
  }


  @Override
  public String getType()
  {
    return "com.modinfodesigns.classify.TermFrequency";
  }

  @Override
  public String getValue()
  {
    return Double.toString( getQuantity( ) );
  }

  @Override
  public String getValue( String format )
  {
    return Double.toString( getQuantity( format ) );
  }

  @Override
  public void setValue(String value, String format)
  {

  }

  @Override
  public IProperty copy()
  {
    TermFrequency copy = new TermFrequency( );
    copy.termCount = (HitCount)this.termCount.copy( );
    copy.tokenCount = (HitCount)this.tokenCount.copy( );
        
    return copy;
  }

  @Override
  public double getQuantity()
  {
    return getQuantity( "FRACTION" );
  }

  @Override
  public double getQuantity(String units)
  {
    if (units == null) return getQuantity( "FRACTION" );
    double termQuantity = (termCount != null) ? termCount.getQuantity( ) : 0.0;
    double tokenQuantity = (tokenCount != null) ? tokenCount.getQuantity( ) : 0.0;
        
    if (tokenQuantity == 0.0)
    {
      LOG.error( "Token count is 0 - cannot compute TermFrequency!" );
      return 0;
    }
        
    if (termQuantity > tokenQuantity)
    {
      // ERROR CONDITION!
      LOG.error( "Term quantity cannot exceed Token Quantity!" );
      return 0;
    }
        
    if (units.equals( "FRACTION" ) )
    {
      LOG.debug( "Computing Fraction: " + termCount.getIntegerValue() + "/" + tokenCount.getIntegerValue( ) );
      return termQuantity / tokenQuantity;
    }
    else if (units.equals( "PERCENT" ))
    {
      return (termQuantity / tokenQuantity) * 100.0;
    }
        
    LOG.error( "Do not understand units: '" + units + "'" );
    return 0;
  }

  @Override
  public String[] getUnits()
  {
    return UNITS;
  }

  /**
   * Can only add IF another ISA TermFrequency AND the denominator (tokenCount) is Equal to this token count.
   *
   */
  @Override
  public IQuantity add( IQuantity another ) throws QuantityOperationException
  {
    if (another == null || termCount == null || tokenCount == null)
    {
      throw new QuantityOperationException( "Cannot add NULL value!" );
    }
        
    if (another instanceof TermFrequency)
    {
      LOG.debug( "Adding TermFrequency " );
      // add the TermCounts - the Token Counts should not change(?)
      TermFrequency anotherTF = (TermFrequency)another;
        
      if (anotherTF.tokenCount == null || anotherTF.termCount == null) throw new QuantityOperationException( "Cannot add NULL!" );
        
      if (anotherTF.tokenCount.getIntegerValue() == tokenCount.getIntegerValue())
      {
        // Term count cannot exceed token count...
        int newTermCount = Math.min((anotherTF.termCount.getIntegerValue() + termCount.getIntegerValue()), tokenCount.getIntegerValue());
        LOG.debug( "Token Counts are the same: adding term counts " +
                    anotherTF.termCount.getIntegerValue() + " + " + termCount.getIntegerValue() + " = " + newTermCount );
          
        return new TermFrequency( newTermCount, tokenCount.getIntegerValue() );
      }
    }
        
    throw new QuantityOperationException( "Cannot add " + another.getType() + ": " + another.getValue( ) );
  }

  @Override
  public IQuantity sub(IQuantity another) throws QuantityOperationException
  {
    if (another == null || termCount == null || tokenCount == null)
    {
      throw new QuantityOperationException( "Cannot subtract NULL value!" );
    }
        
    if (another instanceof TermFrequency)
    {
      // add the TermCounts - the Token Counts should not change(?)
      TermFrequency anotherTF = (TermFrequency)another;
            
      if (anotherTF.tokenCount == null || anotherTF.termCount == null) throw new QuantityOperationException( "Cannot subtract NULL!" );
            
      if (anotherTF.tokenCount.getIntegerValue() == tokenCount.getIntegerValue())
      {
        // Term count cannot be less than 0
        int newTermCount = Math.max((anotherTF.termCount.getIntegerValue() - termCount.getIntegerValue()), 0);
        return new TermFrequency( newTermCount, tokenCount.getIntegerValue() );
      }
    }
        
    throw new QuantityOperationException( "Cannot add " + another.getType() + ": " + another.getValue( ) );
  }

  @Override
  public IQuantity multiply(IQuantity another)
  {
    return null;
  }

  @Override
  public IQuantity divide(IQuantity another)
  {
    return null;
  }

  @Override
  public Object getValueObject()
  {
    return this;
  }

  @Override
  public String getDefaultFormat()
  {
    return null;
  }

  @Override
  public boolean isMultiValue()
  {
    return false;
  }

  @Override
  public String[] getValues(String format)
  {
    String[] values = new String[1];
    values[0] = getValue( format );
    return values;
  }
}
