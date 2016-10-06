package com.modinfodesigns.property;

import com.modinfodesigns.property.compare.BasicPropertyComparator;
import com.modinfodesigns.property.compare.IPropertyHolderMatcher;
import com.modinfodesigns.property.quantity.*;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.schema.PropertyDescriptor;
import com.modinfodesigns.property.time.DateProperty;
import com.modinfodesigns.utils.StringMethods;
import com.modinfodesigns.security.IUserCredentials;

import java.lang.reflect.Method;

import java.util.List;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BooleanFunctionProperty extends BooleanProperty implements IFunctionProperty, IPropertyHolderMatcher
{
  private transient static final Logger LOG = LoggerFactory.getLogger( BooleanFunctionProperty.class );

  private static String[] compareOps = { "=", "==", "EQ", "<", "LT", ">", "GT", "!=", "NE", "<=", "LE", ">=", "GE", "starts-with", "ends-with", "contains", "matches" };
        
  private static String[] boolOps = { " AND ", " OR ", " AND NOT ", "&&", "||" };
    
  private static final String AND = " AND ";
  private static final String AND_NOT = " ANDNOT ";
  private static final String OR = " OR ";
    
  private static final String EQU = "EQ";
  private static final String LT  = "LT";
  private static final String GT  = "GT";
  private static final String NE  = "NE";
    
  private static final String LTE  = "LE";
  private static final String GTE  = "GE";
    
  private static final String STARTS_WITH = "starts-with";
  private static final String ENDS_WITH = "ends-with";
  private static final String CONTAINS  = "contains";
  private static final String MATCHES   = "matches";
    
  private IFunctionProperty leftSide;
  private String operator;
  private IProperty rightSide;
    
  private IPropertyHolder propHolder;
  private String booleanExpression;

  private BasicPropertyComparator basicPropComparator = new BasicPropertyComparator( BasicPropertyComparator.VALUE );
    
  @Override
  public void setPropertyHolder(IPropertyHolder propHolder)
  {
    this.propHolder = propHolder;
  }


  @Override
  public void setFunction(String function)
  {
    System.out.println( "setFunction: " + function );
    this.booleanExpression = function;
    parse( function );
  }
    
  // -----------------------------------------------------------------------------
  // basic expression is [path] [compare operator] [value]
  // compound pattern is [basic expression] [boolean operator] [basic expression]
  // -----------------------------------------------------------------------------
  private void parse( String booleanExpression )
  {
    System.out.println( "BooleanFunctionProperty Parsing: " + booleanExpression );
        
    // Create Left Side, operator and Right side
    // for compound expressions, roll current
    // params into new BooleanFunctionProperty
    String trimmed = booleanExpression.trim( );
    if (trimmed.startsWith( "(" ) && trimmed.endsWith( ")" ))
    {
      if ( trimmed.indexOf( ")" ) == trimmed.lastIndexOf( ")" ) )
      {
        trimmed = new String( trimmed.substring( 1, trimmed.length() - 1 ));
      }
    }
        
    if (trimmed.startsWith( "(" ))
    {
      System.out.println( "trimmed: " + trimmed );
      int matchingBrace = getMatchingBrace( trimmed, 0 );
      String boolOp = getNextOp( trimmed, boolOps );
      System.out.println( "boolOp = '" + boolOp + "'" );
      if ( trimmed.lastIndexOf( boolOp ) > matchingBrace )
      {
        System.out.println( "the problem case" );
        String leftExpr = trimmed.substring( 1, matchingBrace );
        String restOfIt = trimmed.substring( matchingBrace + 1 );
        System.out.println( "restOfIt is '" + restOfIt + "'" );
        restOfIt = restOfIt.substring( boolOp.length() );
        System.out.println( "Got leftExpr = " + leftExpr + ", rightExpr = " + restOfIt );
        
        BooleanFunctionProperty leftProp = new BooleanFunctionProperty( );
        leftProp.setFunction( leftExpr );
        this.leftSide = leftProp;
          
        BooleanFunctionProperty rightProp = new BooleanFunctionProperty( );
        rightProp.setFunction( restOfIt );
        operator = translate( boolOp );
        this.rightSide = rightProp;
        System.out.println( "problem case done." );
      }
      else
      {
        System.out.println( "the old case" );
        String innerExpr = trimmed.substring( 1, matchingBrace );
        String restOfIt = trimmed.substring( matchingBrace + 1 );
        System.out.println( "Got innerExpr = " + innerExpr + ", rest = " + restOfIt );
        if (restOfIt.trim().length() > 0)
        {
          BooleanFunctionProperty leftProp = new BooleanFunctionProperty( );
          leftProp.setFunction( innerExpr );
          this.leftSide = leftProp;
        
          String nextBoolOp = getNextOp( restOfIt, boolOps );
          System.out.println( "got rest bool op = '" + operator + "'" );
            
          operator = translate( nextBoolOp );
            
          restOfIt = restOfIt.substring( restOfIt.indexOf( nextBoolOp ) + nextBoolOp.length( ) );
            
          BooleanFunctionProperty rightProp = new BooleanFunctionProperty( );
          rightProp.setFunction( restOfIt );
          this.rightSide = rightProp;
        }
        else
        {
          parse( innerExpr );
        }
      }
    }
    else
    {
      String nextCompareOp = getNextOp( trimmed, compareOps );
      if (nextCompareOp != null)
      {
        System.out.println( "Got Comparator OP = " + nextCompareOp );
                
        String leftExpr = new String( trimmed.substring( 0, trimmed.indexOf( nextCompareOp )).trim( ) );
        String rightExpr = new String( trimmed.substring( trimmed.indexOf( nextCompareOp ) + nextCompareOp.length( )).trim( ) );
        
        System.out.println( "creating FunctionProperty for '" + leftExpr + "'" );
        leftSide = new PathFunctionProperty(  );
        leftSide.setFunction( leftExpr );
        operator = translate( nextCompareOp );
          
        if (rightExpr.trim().startsWith( "(" ))
        {
          String rTrimmed = rightExpr.trim( );
          int matchingBrace = getMatchingBrace( rTrimmed, 0 );
          rightExpr = rightExpr.substring( 1, matchingBrace );
        }
        System.out.println( "Got leftExpr = " + leftExpr + ", rightExpr = " + rightExpr );
          
        String nextBoolOp = getNextOp( rightExpr, boolOps );
        if (nextBoolOp != null)
        {
          System.out.println( "Got Boolean OP '" + nextBoolOp + "'" );
                    
          // -----------------------------------------------------------------------------------------------------
          // if right side contains boolean operator, get up to boolean operator as
          // this rightProperty -- determine type by inspection
          // then - create a new BooleanFunctionProperty from the stuff after the operator - set my right to this
          // copy my leftSide, operator and right side into a new BooleanFunctionProperty - set my left to this
          // set my operator to the boolean operator
          // -----------------------------------------------------------------------------------------------------
          String myRight = new String( rightExpr.substring( 0, rightExpr.indexOf( nextBoolOp )).trim( ) );
          rightSide = getProperty( myRight );
          System.out.println( "got myRight = " + myRight );
          if ( rightSide instanceof BooleanFunctionProperty ) {
            System.out.println( "right side is a Boolean Function" );
            BooleanFunctionProperty boolProp = new BooleanFunctionProperty( );
            boolProp.leftSide = (BooleanFunctionProperty)rightSide;
              
            String rightBooleanExpr = new String( rightExpr.substring( rightExpr.indexOf( nextBoolOp ) + nextBoolOp.length() ).trim( ) );
            System.out.println( "got rightBooleanExpr = " + rightBooleanExpr );
            BooleanFunctionProperty rightBoolProp = new BooleanFunctionProperty( );
            rightBoolProp.setFunction( rightBooleanExpr );
            boolProp.booleanExpression = new String( ((BooleanFunctionProperty)rightSide).getFunction( ) + " " + nextBoolOp + " " + rightBoolProp.getFunction( ) );
            boolProp.rightSide = rightBoolProp;
            boolProp.operator = translate( nextBoolOp );
              
            this.rightSide = boolProp;
          }
          else
          {
            System.out.println( "right side is a Property rightExpr = '" + rightExpr + "' leftExpr is " + leftExpr );
            if (myRight.startsWith( "\"") || myRight.startsWith( "'" ))
            {
                System.out.println( "Its a VALUE!" );
            }
            
            BooleanFunctionProperty leftBoolProp = new BooleanFunctionProperty( );
            leftBoolProp.leftSide = this.leftSide;
            leftBoolProp.operator = this.operator;
            leftBoolProp.rightSide = this.rightSide;
            leftBoolProp.booleanExpression = leftSide.getFunction( ) + " " + operator + " " + rightSide;

            /*String rightBooleanExpr = (rightExpr.indexOf( nextBoolOp ) > 0)
                                    ? rightExpr
                                    : new String( rightExpr.substring( rightExpr.indexOf( nextBoolOp ) + nextBoolOp.length() ).trim( ) ); */
            String rightBooleanExpr =new String( rightExpr.substring( rightExpr.indexOf( nextBoolOp ) + nextBoolOp.length() ).trim( ) );
            System.out.println( "got rightBooleanExpr = " + rightBooleanExpr );
            BooleanFunctionProperty rightBoolProp = new BooleanFunctionProperty( );
            rightBoolProp.setFunction( rightBooleanExpr );
            
            this.leftSide = leftBoolProp;
            this.operator = translate( nextBoolOp );
            this.rightSide = rightBoolProp;
          }
        }
        else
        {
          if (operator.equals( "contains" ) && isFunction( rightExpr )) {
            rightSide = new BooleanFunctionProperty( );
            System.out.println( "setting BooleanFunctionProperty to " + rightExpr );
            ((BooleanFunctionProperty)rightSide).setFunction( rightExpr );
          }
          else
          {
            rightSide = getProperty( rightExpr );
            System.out.println( "setting rightSide expression " + rightExpr + " to " + rightSide );
          }
        
          System.out.println( "setting rightSide expression " + rightExpr + " to " + rightSide );
        }
      }
      else
      {
        String nextBoolOp = getNextOp( booleanExpression, boolOps );
        if (nextBoolOp != null)
        {
          System.out.println( "Value OP Value pattern" );
          String leftVal = booleanExpression.substring( 0, booleanExpression.indexOf( nextBoolOp ));
          String rightVal = booleanExpression.substring( booleanExpression.indexOf( nextBoolOp ) + nextBoolOp.length( ) );
          System.out.println( "leftVal = " + leftVal + " rightVal = " + rightVal );
          this.leftSide = new PropertyWrapperFunction( new StringProperty( "value", leftVal ));
          this.operator = nextBoolOp;
          this.rightSide = new StringProperty( "value", rightVal );
        }
        else
        {
          System.out.println( "Just a Single Value!" );
          this.leftSide = new PropertyWrapperFunction( new StringProperty( "value", booleanExpression ));
          this.operator = EQU;
          this.rightSide = new StringProperty( "value", booleanExpression );
        }
      }
    }
  }
    
  private boolean isFunction( String expression )
  {
    for (int i = 0; i < compareOps.length; i++ ) {
      String padded = " " + compareOps[i] + " ";
      if (expression.indexOf( padded ) > 0) return true;
    }

    return false;
  }
    
  private String getNextOp( String booleanExpression, String[] opsArray )
  {
    LOG.debug( "getNextOp " + booleanExpression );
    int lowestIndex = -1;
    String firstOp = null;
        
    for ( int i = 0, isz = opsArray.length; i < isz; i++)
    {
      if ( booleanExpression.indexOf( opsArray[i] ) >= 0 )
      {
        LOG.debug( "Checking op " + opsArray[i] );
        int itsNdx = booleanExpression.lastIndexOf( opsArray[i] );

        // need to do it this way so that >= will replace > if it is there
        // and <= will replace < if it is there
        if (itsNdx > 0 && (lowestIndex < 0 || itsNdx <= lowestIndex ))
        {
          boolean startsWithParen = booleanExpression.startsWith( "(" );
          if ((startsWithParen && booleanExpression.indexOf( ")" ) < itsNdx ) ||
              (startsWithParen && booleanExpression.length() == (booleanExpression.indexOf( ")" ) - 1)) ||
              (!startsWithParen))
          {
            lowestIndex = itsNdx;
            firstOp = opsArray[i];
            LOG.debug( "setting OP = " + firstOp );
          }
        }
      }
    }
        
    return firstOp;
  }
    
  private String translate( String compareOp )
  {
    System.out.println( "translate " + compareOp );
    if (compareOp.equals( "=" ) || compareOp.equals( "==" )) return EQU;
    else if (compareOp.equals( "<" )) return LT;
    else if (compareOp.equals( "<=" )) return LTE;
    else if (compareOp.equals( ">" )) return GT;
    else if (compareOp.equals( ">=" )) return GTE;
    else if (compareOp.equals( "!=" )) return NE;
        
    else if (compareOp.equals( "&&" )) return AND;
    else if (compareOp.equals( "||" )) return OR;
        
    return compareOp;
  }
    
  private IProperty getProperty( String strValue )
  {
    if (isFunction( strValue ))
    {
      LOG.debug( "'" + strValue + "' IsFunction!" );
      BooleanFunctionProperty boolFunProp = new BooleanFunctionProperty( );
      boolFunProp.setFunction( strValue );
      return boolFunProp;
    }
      
    if (StringMethods.isInteger( strValue ))
    {
      return new IntegerProperty( "right", strValue );
    }
    if (strValue.indexOf( "(" ) > 0)
    {
      String propType = strValue.substring( 0, strValue.indexOf( "(" ));
      LOG.debug( "found propType " + propType );
      String itsVal = strValue.substring( strValue.indexOf( "(" ) + 1, strValue.indexOf( ")" ) );
        
      IQuantity quantity = null;
      if (propType.equals( "Area" ))          quantity = new Area( );
      else if (propType.equals( "Duration" )) quantity = new Duration( );
      else if (propType.equals( "Distance" )) quantity = new Distance( );
      else if (propType.equals( "Velocity" )) quantity = new Velocity( );
      else if (propType.equals( "Volume" ))   quantity = new Volume( );
      else if (propType.equals( "Weight" ))   quantity = new Weight( );
      if (quantity != null)
      {
        itsVal = itsVal.trim( );
        String quanVal = itsVal;
        String units = null;
        if (itsVal.indexOf( " " ) > 0)
        {
          quanVal = itsVal.substring( 0, itsVal.indexOf( " " ) ).trim( );
          units = itsVal.substring( itsVal.indexOf( " " ) + 1 ).trim( );
        }
            
        try
        {
          LOG.debug( "Setting value " + quanVal + " " + units );
          quantity.setValue( quanVal, units );
          return quantity;
        }
        catch (PropertyValidationException pve )
        {
                
        }
      }
        
      if (propType.equals( "Date" ))
      {
        LOG.debug( "Its a Date!" );
        DateProperty dateProp = new DateProperty( );
        try
        {
          dateProp.setValue( itsVal, null );
          return dateProp;
        }
        catch (PropertyValidationException pve )
        {
                
        }
      }
    }
        
    LOG.debug( "getProperty: " + strValue );

    return new StringProperty( "right", strValue );
  }

  @Override
  public String getFunction()
  {
    return this.booleanExpression;
  }

  @Override
  public IProperty execute()
  {
    LOG.debug( "execute( )" );
    boolean value = getBooleanValue( );
    return new BooleanProperty( getName( ) + "_result", value );
  }

  @Override
  public boolean equals(IUserCredentials user, IProperty property)
  {
    LOG.debug( "equals ( IProperty )" );
    	
    if (property instanceof IPropertyHolder)
    {
      return equals( user, (IPropertyHolder)property );
    }
    else if (property instanceof IExposeInternalProperties)
    {
      LOG.debug( "Creating Temporary Property Holder from IExposeInternalProperties" );
        	
      // using Reflection: create a temporary property holder
      // with the values of the internal properties
      IExposeInternalProperties internalProp = (IExposeInternalProperties)property;
      DataObject tempPropHolder = new DataObject( );
      List<PropertyDescriptor> internalProps = internalProp.getInternalProperties( );
      for (PropertyDescriptor propDesc : internalProps )
      {
        // create an IProperty based on the get[propertyname] value and put it into
        // our temporary prop holder...
        String propName = propDesc.getName( );
        String methodName = "get" + StringMethods.initialCaps( propName );
        LOG.debug( "Invoking method: '" + methodName + "' on " + internalProp );
        String response = null;
        try
        {
          Method m = internalProp.getClass().getMethod( methodName );
          if (m != null)
          {
            Object o = m.invoke( internalProp );
            response = (o != null) ? o.toString() : null;
          }
          else
          {
            LOG.debug( "No Method found for " + methodName + " in " + internalProp );
          }
        }
        catch ( Exception e )
        {
          LOG.debug( "invoke method got Exception: " + e );
        }
        		
        if (response != null)
        {
          LOG.debug( "Got Response = '" + response + "'" );
        			
          try
          {
            IProperty theProp = (IProperty)Class.forName( propDesc.getPropertyType( ) ).newInstance( );
            if (theProp instanceof EnumerationProperty)
            {
              EnumerationProperty enumProp = (EnumerationProperty)theProp;
              enumProp.setChoices( propDesc.getPropertyValues( ) );
            }
        				
            theProp.setValue( response, propDesc.getPropertyFormat() );
            theProp.setName( propName );
            tempPropHolder.setProperty( theProp );
          }
          catch ( Exception e )
          {
            LOG.debug( "Could not create Property of class: " + propDesc.getPropertyType( ) + " " + e );
          }
        }
        else
        {
          LOG.debug( "Could not get property for: " + propName );
        }
      }
        	
      return equals( user, tempPropHolder );
    }
        
    return false;
  }

  @Override
  public boolean equals(IUserCredentials user, IPropertyHolder propHolder)
  {
    System.out.println( "equals ( IPropertyHolder )" );

    IPropertyHolder temp = this.propHolder;
    this.propHolder = propHolder;
        
    boolean output = getBooleanValue( );
    this.propHolder = temp;
    return output;
  }

  @Override
  public boolean getBooleanValue( )
  {
    LOG.debug( "getBooleanValue: " + this.booleanExpression );
    	
    // evaluate the expression
    if (operator == null)
    {
      LOG.debug( "Operator is NULL - returning false" );
      return false;
    }
        
    LOG.debug( "Using Operator: '" + operator + "'" );
    if (operator.equals( AND ))
    {
      LOG.debug( "Processing AND with " + propHolder );
      // left side and right side must be BooleanFunctionProperty
      BooleanFunctionProperty leftBoolFun = (BooleanFunctionProperty)leftSide;
      leftSide.setPropertyHolder( this.propHolder );
        
      BooleanFunctionProperty rightBoolFun = (BooleanFunctionProperty)rightSide;
      rightBoolFun.setPropertyHolder( this.propHolder );

      return (leftBoolFun.getBooleanValue( ) && rightBoolFun.getBooleanValue( ) );
    }
    else if (operator.equals( OR ))
    {
      LOG.debug( "Processing OR " );
      // left side and right side must be BooleanFunctionProperty
      BooleanFunctionProperty leftBoolFun = (BooleanFunctionProperty)leftSide;
      leftBoolFun.setPropertyHolder( this.propHolder );
        
      BooleanFunctionProperty rightBoolFun = (BooleanFunctionProperty)rightSide;
      rightBoolFun.setPropertyHolder( this.propHolder );
            
      return (leftBoolFun.getBooleanValue( ) || rightBoolFun.getBooleanValue( ) );
    }
    else if (operator.equals( AND_NOT ))
    {
      LOG.debug( "Processing AND_NOT " );
      // left side and right side must be BooleanFunctionProperty
      BooleanFunctionProperty leftBoolFun = (BooleanFunctionProperty)leftSide;
      leftBoolFun.setPropertyHolder( this.propHolder );
            
      BooleanFunctionProperty rightBoolFun = (BooleanFunctionProperty)rightSide;
      rightBoolFun.setPropertyHolder( this.propHolder );
            
      return (leftBoolFun.getBooleanValue( ) && !rightBoolFun.getBooleanValue( ) );
    }
    else
    {
      LOG.debug( "Setting property holder " + ((propHolder != null) ? propHolder.getValue( ) : "") );
      leftSide.setPropertyHolder( propHolder );

      if (operator.equals( EQU ))
      {
        LOG.debug( "Testing Equals with " + leftSide );
        IProperty leftProp = leftSide.execute( );
        boolean isEqual = equals( leftProp, rightSide );
        LOG.debug( "Got Is Equal = " + isEqual );
        return isEqual;
      }
      else if (operator.equals( NE ))
      {
        IProperty leftProp = leftSide.execute( );
        return !equals( leftProp, rightSide );
      }
      else if (operator.equals( LT ) || operator.equals( GT ))
      {
        IProperty leftProp = leftSide.execute( );
        String leftPropType = leftProp.getType( );
        LOG.debug( "left Type = " + leftPropType );
        LOG.debug( "rightSide Type = " + rightSide.getType( ) );
        IProperty leftProx  = getRealProperty( leftProp );
                
        if (leftProx instanceof IQuantity && rightSide instanceof IQuantity )
        {
          LOG.debug( "comparing Quantities " );
          if (leftProx.getType().equals( rightSide.getType() ) || rightSide instanceof IntegerProperty )
          {
            IQuantity leftQuan = (IQuantity)leftProx;
            IQuantity rightQuan = (IQuantity)rightSide;
                        
            LOG.debug( "left = " + leftQuan.getQuantity( ) + " right = " + rightQuan.getQuantity( ) );
                        
            return (operator.equals( LT )) ? (leftQuan.getQuantity() < rightQuan.getQuantity( ))
                                           : (leftQuan.getQuantity() > rightQuan.getQuantity( ) );
          }
          else
          {
            LOG.debug( "Not the same type! can't compare..." );
            return false;
          }
        }
        else if (leftProp instanceof DateProperty)
        {
          if (rightSide instanceof DateProperty)
          {
            DateProperty leftDate = (DateProperty)leftProp;
            DateProperty rightDate = (DateProperty)rightSide;
                        
            return (operator.equals( LT )) ? leftDate.before( rightDate )
                                           : leftDate.after( rightDate );
          }
          else
          {
            return false;
          }
        }
        else
        {
          String leftVal = leftProp.getValue( );
          String rightVal = rightSide.getValue( );
                    
          return (operator.equals( LT) ) ? (leftVal.compareTo( rightVal ) < 0)
                                         : (leftVal.compareTo( rightVal ) > 0);
        }
      }
      else if (operator.equals( LTE ) || operator.equals( GTE ))
      {
        LOG.debug( "Testing LTE operator" );
        IProperty leftProp = leftSide.execute( );
        if (leftProp instanceof IQuantity && rightSide instanceof IQuantity )
        {
          if (leftProp.getType().equals( rightSide.getType() ))
          {
            IQuantity leftQuan = (IQuantity)leftProp;
            IQuantity rightQuan = (IQuantity)rightSide;
                        
            return (operator.equals( LTE )) ? (leftQuan.getQuantity() <= rightQuan.getQuantity( ))
                                            : (leftQuan.getQuantity() >= rightQuan.getQuantity( ) );
          }
          else
          {
            LOG.debug( "not same type - returning false " );
            return false;
          }
        }
        else if (leftProp instanceof DateProperty)
        {
          // right side needs also to be a DateProperty and
          // it should be after left date
          if (rightSide instanceof DateProperty)
          {
            DateProperty leftDate = (DateProperty)leftProp;
            DateProperty rightDate = (DateProperty)rightSide;
                        
            return (operator.equals( LTE )) ? leftDate.onOrBefore( rightDate )
                                            : leftDate.onOrAfter( rightDate );
          }
          else
          {
            return false;
          }
        }
        else
        {
          LOG.debug( "LTE: Comparing String values " );
          String leftVal = leftProp.getValue( );
          String rightVal = rightSide.getValue( );
                    
          return (operator.equals( LTE ) ) ? (leftVal.compareTo( rightVal ) <= 0)
                                           : (leftVal.compareTo( rightVal ) >= 0);
        }
      }
      else if (operator.equals( STARTS_WITH ))
      {
        LOG.debug( "evaluating starts-with" );
        String leftVal = leftSide.getValue( );
        String rightVal = rightSide.getValue( );
        
        LOG.debug( leftVal + " starts-with " + rightVal + " ?" );
                
        return leftVal.startsWith( rightVal );
      }
      else if (operator.equals( ENDS_WITH ))
      {
        String leftVal = leftSide.getValue( );
        String rightVal = rightSide.getValue( );
                
        return leftVal.endsWith( rightVal );
      }
      else if (operator.equals( CONTAINS ))
      {
        // if prop is instance of PropertyList - contains means that
        // it has a property in its list that matches
        
        // if prop is instance of IRangeProperty
        IProperty leftProp = leftSide.execute( );
        if (leftProp instanceof PropertyList)
        {
          PropertyList pl = (PropertyList)leftProp;
          System.out.println( "CONTAINS: Checking in a PropertyList " + pl.getValue( ) );
          if (rightSide instanceof BooleanFunctionProperty)
          {
            BooleanFunctionProperty rightBoolFun = (BooleanFunctionProperty)rightSide;
            return propertyListContains( pl, rightBoolFun );
          }
          else
          {
            Iterator<IProperty> propIt = pl.getProperties( );
            while ( propIt.hasNext( ) )
            {
              IProperty nested = propIt.next( );
              if (equals( nested, rightSide ))
              {
                System.out.println( "MATCHES" );
                return true;
              }
            }
            return false;
          }
        }
        else if (leftProp instanceof IRangeProperty )
        {
          // value must be in ranges
        }
        else
        {
          if (rightSide instanceof BooleanFunctionProperty )
          {
            BooleanFunctionProperty rightBoolFun = (BooleanFunctionProperty)rightSide;
            return rightBoolFun.equals( (IUserCredentials)null, leftProp );
          }
          else
          {
            String leftVal = leftProp.getValue( );
            String rightVal = rightSide.getValue( );
            return leftVal.contains( rightVal );
          }
        }
      }
      else if (operator.equals( MATCHES ))
      {
        // if its a string - then use match OR regex if it is that ...
        // if the left hand prop is a PropertyList - each property must match
        IProperty leftProp = leftSide.execute( );
        if ( leftProp instanceof PropertyList )
        {
          PropertyList pl = (PropertyList)leftProp;
          if (rightSide instanceof BooleanFunctionProperty)
          {
            BooleanFunctionProperty rightBoolFun = (BooleanFunctionProperty)rightSide;
            return propertyListMatches( pl, rightBoolFun );
          }
          else
          {
            Iterator<IProperty> propIt = pl.getProperties( );
            while ( propIt.hasNext( ) )
            {
              IProperty nested = propIt.next( );
              if (!equals( nested, rightSide ))
              {
                LOG.debug( "NOT ALL MATCH!" );
                return false;
              }
            }
            LOG.debug( "ALL MATCH!" );
            return true;
          }
        }
        else
        {
          String leftVal = leftProp.getValue( );
          String rightVal = rightSide.getValue( );
            
          return leftVal.matches( rightVal );
        }
      }
    }
    
    LOG.debug( "Not mapping to any operator - returning false" );
    return false;
  }
    
  private boolean propertyListContains( PropertyList pl, BooleanFunctionProperty boolProp )
  {
    Iterator<IProperty> propIt = pl.getProperties( );
    while ( propIt.hasNext( ) )
    {
      IProperty nested = propIt.next( );
      if ( nested instanceof PropertyList )
      {
        PropertyList nestedList = (PropertyList)nested;
        boolean contains = propertyListContains( nestedList, boolProp );
        if (contains) return true;
      }
      else
      {
        System.out.println( "testing " + nested.getValue( ) );
        if (boolProp.equals( (IUserCredentials)null, nested ))
        {
          System.out.println( "MATCHES" );
          return true;
        }
      }
    }

    System.out.println( "NONE MATCHES" );
    return false;
  }
    
  private boolean propertyListMatches( PropertyList pl, BooleanFunctionProperty boolProp )
  {
    Iterator<IProperty> propIt = pl.getProperties( );
    while ( propIt.hasNext( ) )
    {
      IProperty nested = propIt.next( );
      if ( nested instanceof PropertyList )
      {
          PropertyList nestedList = (PropertyList)nested;
          boolean matches = propertyListMatches( nestedList, boolProp );
          if (!matches) return false;
      }
      else
      {
        if (!boolProp.equals( (IUserCredentials)null, nested ))
        {
          LOG.debug( "NOT ALL MATCH!" );
          return false;
        }
      }
    }
      
    LOG.debug( "ALL MATCH!" );
    return true;
  }
    
  private boolean equals( IProperty leftProp, IProperty rightProp )
  {
    LOG.debug( "equals: " + leftProp + " = " + ((rightProp != null) ? rightProp.getValue( ) : "null") );
    if (leftProp == null || rightProp == null) return false;
    	
    if (leftProp instanceof IQuantity && rightProp instanceof IQuantity )
    {
      IQuantity leftQuan = (IQuantity)leftProp;
      IQuantity rightQuan = (IQuantity)rightProp;
      return leftQuan.getQuantity( ) == rightQuan.getQuantity( );
    }
    else if (leftProp instanceof DateProperty)
    {
      if ((rightProp instanceof DateProperty) == false) return false;
      DateProperty leftDate = (DateProperty)leftProp;
      DateProperty rightDate = (DateProperty)rightProp;
      return leftDate.equals( rightDate );
    }
    else if (rightProp.getValue( ).equals( "NOT_NULL" ))
    {
      return true;
    }
        
    LOG.debug( "Using basic comparator '" + leftProp.getValue( ) + "' = '" + rightProp.getValue( ) + "'" );
    return basicPropComparator.equals( leftProp, rightProp );
  }
    
  @Override
  public String getValue()
  {
    boolean boolVal = getBooleanValue( );
    return (boolVal) ? TRUE : FALSE;
  }
    
  private int getMatchingBrace( String expression, int startFrom )
  {
    return StringMethods.findEnd( expression, '(', ')', startFrom );
  }
    
  private IProperty getRealProperty( IProperty prop )
  {
    try
    {
      IProperty proxy = (IProperty)Class.forName( prop.getType() ).newInstance( );
      LOG.debug( "prop value = " + prop.getValue( ) );
      LOG.debug( "set value on " + proxy.getType( ) );
      proxy.setValue( prop.getValue( ), proxy.getDefaultFormat() );
      return proxy;
    }
    catch ( Exception e )
    {
            
    }
    return prop;
  }

}
