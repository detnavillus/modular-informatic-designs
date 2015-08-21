package com.modinfodesigns.property.quantity.function;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;

import com.modinfodesigns.property.quantity.IQuantity;
import com.modinfodesigns.property.quantity.QuantityOperationException;
import com.modinfodesigns.property.transform.IPropertyHolderTransform;
import com.modinfodesigns.property.transform.IPropertyTransformListener;
import com.modinfodesigns.property.transform.PropertyTransformException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes an algebraic expression where the variable names are property names and computes
 * a value from the IQuantity properties contained in an IPropertyHolder
 *
 * @author Ted Sullivan
 */

public class ExpressionCalculator implements IPropertyHolderTransform
{
  private transient static final Logger LOG = LoggerFactory.getLogger( ExpressionCalculator.class );

  private String expression;
	
  private String outputProperty;
	
  public ExpressionCalculator(  ) {  }
	
  public ExpressionCalculator( String expression )
  {
    this.expression = expression;
  }
	
  public void setExpression( String expression )
  {
    this.expression = expression;
  }
	
  public void setOutputProperty( String outputProperty )
  {
    this.outputProperty = outputProperty;
  }
	

  public IPropertyHolder transformPropertyHolder( IPropertyHolder input )
            throws PropertyTransformException
  {
    try
    {
      IQuantity result = calculate( this.expression, input );
      if (outputProperty != null)
      {
        result.setName( outputProperty );
      }
    	
      input.addProperty( result );
    }
    catch ( QuantityOperationException qoe )
    {
      throw new PropertyTransformException( "Got Operation Exception: " + qoe.getMessage( ) );
    }
    	
    return input;
  }
    
    
  /**
   *  Calculates result of an algebraic expression in which the variables are propertyNames
   *  of the property holder.  Can handle nested expressions using '( )'.
   */
  public static IQuantity calculate( String expression, IPropertyHolder pHolder ) throws QuantityOperationException
  {
    if (expression == null) return null;
    	
    IQuantity accumulator = null;
    IQuantity operand = null;
    StringBuffer opBuffer = new StringBuffer( );

    char lastOperator = ' ';

    String expr = expression.trim();
    if ( expr.charAt(expr.length()-1) != '=')
    {
      expr += "=";
    }

    LOG.debug( "   Calculating: " + expr );

    for (int i = 0; i < expr.length( ); i++)
    {
      char ch = expr.charAt(i);
      if ( ch == '(' )
      {
        int lastBrace = findNestedBrace( expr, i );
        String nest = expr.substring(i+1, lastBrace );
        operand = calculate( nest, pHolder );

        // jump past inner calculation
        i = lastBrace;
      }
      else if ( ch == '+' || ch == '-' ||
                ch == '*' || ch == '/')
      {
        // construct operand from string buffer
        // get the IQuantity specified by operand from pHolder

        IProperty opP = pHolder.getProperty( opBuffer.toString( ).trim( ) );
        if (opP != null && opP instanceof IQuantity)
        {
          operand = (IQuantity)opP;
        }
                
        if (lastOperator != ' ')
        {
          try
          {
            accumulator = doFunction( accumulator, operand, lastOperator );
          }
          catch (QuantityOperationException qoe )
          {
                		
          }
        }
        else
        {
          accumulator = operand;
        }

        lastOperator = ch;

        // clear string buffer.
        opBuffer.delete( 0, opBuffer.length()-1 );
      }
      else if ( ch == '=' )
      {
        IProperty opP = pHolder.getProperty( opBuffer.toString( ).trim( ) );
        if (opP != null && opP instanceof IQuantity)
        {
          operand = (IQuantity)opP;
        }
        if (lastOperator == ' ' )
        {
          return operand;
        }

        try
        {
          accumulator = doFunction( accumulator, operand, lastOperator );
        }
        catch (QuantityOperationException qoe )
        {
                	
        }
                
        LOG.debug( "   Returning: " + accumulator );
        return accumulator;
      }
      else
      {
        opBuffer.append( ch );
      }
    }

    return accumulator;
  }


  /* ------------------------------------------------------
   *		doFunction
   * ------------------------------------------------------ */
  static IQuantity doFunction( IQuantity accumulator, IQuantity operand, char operator )
                    throws QuantityOperationException
  {
    LOG.debug( "       doFunction(): " + accumulator + " " +
                operator + " " + operand );
    switch ( operator )
    {
      case '+':
        accumulator = accumulator.add( operand );
        break;
      case '-':
        accumulator = accumulator.sub( operand );
        break;
      case '*':
        accumulator = accumulator.multiply( operand );
        break;
      case '/':
        accumulator = accumulator.divide( operand );
        break;
      case ' ':
        accumulator = operand;
        break;
    }

    return accumulator;
  }


  /* ------------------------------------------------------
   *		findNestedBrace
   * ------------------------------------------------------ */
  static int findNestedBrace( String s, int offset )
  {
    int i = offset + 1;
    for ( int nestCount = 1; nestCount > 0 && i < s.length(); ++i )
    {
      if (s.charAt( i ) == '(' )    ++nestCount;
      else if (s.charAt(i) == ')' ) --nestCount;
    }

    return i-1;
  }
    
    
  @Override
  public IProperty transform( IProperty input )
                              throws PropertyTransformException
  {
    try
    {
      if ( input instanceof IPropertyHolder )
      {
        return calculate( expression, (IPropertyHolder)input );
      }
    }
    catch ( QuantityOperationException qoe )
    {
      throw new PropertyTransformException( "Got QuantityOperationException: " + qoe.getMessage( ) );
    }
		
    return null;
  }
	
	
  @Override
  public void startTransform( IProperty input,
                              IPropertyTransformListener transformListener)
			                    throws PropertyTransformException 
  {
		
  }

}
