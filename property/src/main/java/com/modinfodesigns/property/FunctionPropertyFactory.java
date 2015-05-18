package com.modinfodesigns.property;

import com.modinfodesigns.property.quantity.function.ComputedQuantity;
import com.modinfodesigns.property.quantity.function.AverageProperty;
import com.modinfodesigns.property.quantity.function.SumProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FunctionPropertyFactory
{
  private transient static final Logger LOG = LoggerFactory.getLogger( FunctionPropertyFactory.class );

  public static final String CALCULATOR_FUNCTION = "calc(";
  public static final String BOOLEAN_FUNCTION = "bool(";
  public static final String TEMPLATE_FUNCTION = "template(";
  public static final String MAPPED_FUNCTION = "map(";

  public static final String AVERAGE_FUNCTION = "average(";
  public static final String SUM_FUNCTION     = "sum(";
	

  public static IFunctionProperty createFunctionProperty( String function )
  {
    LOG.debug(  "createFunctionProperty ..." );
    	
    if (function.indexOf( "(" ) > 0 && function.indexOf( ")" ) > 0)
    {
      String functionStr = new String( function.substring( function.indexOf( "(" ) + 1, function.lastIndexOf( ")" )));

      if (function.startsWith( CALCULATOR_FUNCTION ))
      {
        ComputedQuantity exprCalc = new ComputedQuantity( );
        exprCalc.setFunction( functionStr );
        return exprCalc;
      }
      else if (function.startsWith( BOOLEAN_FUNCTION ))
      {
        BooleanFunctionProperty bfp = new BooleanFunctionProperty( );
        bfp.setFunction( functionStr );
        return bfp;
      }
      else if (function.startsWith( TEMPLATE_FUNCTION ))
      {
        TemplateFunctionProperty tfp = new TemplateFunctionProperty( );
        tfp.setFunction( functionStr );
        return tfp;
      }
      else if (function.startsWith( MAPPED_FUNCTION ))
      {
        LOG.debug( "creating mapped function '" + functionStr + "'" );
        MappedProperty mp = new MappedProperty( );
        mp.setFunction( functionStr );
        return mp;
      }
      else if (function.startsWith( AVERAGE_FUNCTION ))
      {
        AverageProperty ap = new AverageProperty( );
        ap.setFunction( functionStr );
        return ap;
      }
      else if (function.startsWith( SUM_FUNCTION ))
      {
        SumProperty sp = new SumProperty( );
        sp.setFunction( functionStr );
        return sp;
      }
    }
    	
    return null;
  }
}
