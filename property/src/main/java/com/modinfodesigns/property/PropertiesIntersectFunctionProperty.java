package com.modinfodesigns.property;

import com.modinfodesigns.property.compare.IPropertyHolderMatcher;
import com.modinfodesigns.property.string.StringListProperty;

import com.modinfodesigns.security.IUserCredentials;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesIntersectFunctionProperty extends BooleanProperty implements IFunctionProperty, IPropertyHolderMatcher
{
  private transient static final Logger LOG = LoggerFactory.getLogger( PropertiesIntersectFunctionProperty.class );
    
  public static final String INTERSECTS = "INTERSECTS";
  public static final String NOT_INTERSECTS = "NOT INTERSECTS";
    
  private String setFunction;
    private String operator;  // INTERSECTS or NOT_INTERSECTS
    
  private IPropertyHolder propHolder;

  private ArrayList<String> setOneProps;
  private ArrayList<String> setTwoProps;
    
  public void setPropertyHolder( IPropertyHolder propHolder )
  {
    this.propHolder = propHolder;
  }
    

  // List of properties field, field, field INTERSECTS|NOT INTERSECTS field, field, field
  public void setFunction( String function )
  {
    System.out.println( "setFunction: " + function );
    this.setFunction = function;
    parse( function );
  }
    
  private void parse( String setExpression )
  {
    // look for comma separated strings separated by either INTERSECTS or NOT INTERSECTS
    if (setExpression.indexOf( NOT_INTERSECTS ) > 0 )
    {
      operator = NOT_INTERSECTS;
      String leftSide = setExpression.substring( 0, setExpression.indexOf( NOT_INTERSECTS )).trim( );
      String rightSide = setExpression.substring( setExpression.indexOf( NOT_INTERSECTS ) + NOT_INTERSECTS.length() ).trim( );
      createFieldProps( leftSide, rightSide );
    }
    else if (setExpression.indexOf( INTERSECTS ) > 0 )
    {
      operator = INTERSECTS;
      String leftSide = setExpression.substring( 0, setExpression.indexOf( INTERSECTS )).trim( );
      String rightSide = setExpression.substring( setExpression.indexOf( INTERSECTS ) + INTERSECTS.length() ).trim( );
      createFieldProps( leftSide, rightSide );
    }
  }
    
  private void createFieldProps( String leftSide, String rightSide )
  {
    String[] fields = leftSide.split( "," );
    setOneProps = new ArrayList<String>( );
    for (int i = 0; i < fields.length; i++)
    {
      System.out.println( "adding left prop " + fields[i].trim( ) );
      setOneProps.add( fields[i].trim( ) );
    }
    fields = rightSide.split( "," );
    setTwoProps = new ArrayList<String>( );
    for (int i = 0; i < fields.length; i++)
    {
      System.out.println( "adding right prop '" + fields[i].trim( ) + "'" );
      setTwoProps.add( fields[i].trim( ) );
    }
  }
    
  @Override
  public boolean getBooleanValue( )
  {
    System.out.println( "getBooleanValue( )" );
    boolean setsIntersect = false;
      
    HashSet<String> setOneVals = new HashSet<String>( );
    for (String setOneProp : setOneProps )
    {
      IProperty prop = propHolder.getProperty( setOneProp );
      if (prop != null)
      {
        if (prop instanceof PropertyList)
        {
          PropertyList pl = (PropertyList)prop;
          Iterator<IProperty> props = pl.getProperties( );
          while (props.hasNext( ) )
          {
            IProperty pr = props.next( );
            System.out.println( "adding left value: " + pr.getValue( ) );
            setOneVals.add( pr.getValue( ) );
          }
        }
        else if (prop instanceof StringListProperty )
        {
          String[] vals = ((StringListProperty)prop).getStringList( );
          for (int i = 0; i < vals.length; i++)
          {
            System.out.println( "adding left value: " + vals[i] );
            setOneVals.add( vals[i] );
          }
        }
        else
        {
          System.out.println( "adding left value: " + prop.getValue( ) );
          setOneVals.add( prop.getValue( ) );
        }
      }
      else
      {
        System.out.println( "No Property Value for '" + setOneProp + "'" );
      }
    }
      
    for (String setTwoProp : setTwoProps )
    {
      IProperty prop = propHolder.getProperty( setTwoProp );
      if (prop != null)
      {
        if (prop instanceof PropertyList )
        {
          PropertyList pl = (PropertyList)prop;
          Iterator<IProperty> props = pl.getProperties( );
          while (props.hasNext( ) )
          {
            IProperty pr = props.next();
            System.out.println( "checking right value: " + pr.getValue( ) );
            if (setOneVals.contains( pr.getValue( )))
            {
              setsIntersect = true;
              break;
            }
          }
        }
        else if (prop instanceof StringListProperty )
        {
          String[] vals = ((StringListProperty)prop).getStringList( );
          for (int i = 0; i < vals.length; i++)
          {
            System.out.println( "checking right value: " + vals[i] );
            if (setOneVals.contains( vals[i] ) )
            {
              setsIntersect = true;
              break;
            }
          }
        }
        else
        {
          String pVal = prop.getValue( );
          System.out.println( "checking right value: " + pVal );
          if (setOneVals.contains( pVal ))
          {
            setsIntersect = true;
            break;
          }
        }
      }
      else
      {
        System.out.println( "No Property Value for '" + setTwoProp + "'" );
      }
        
      if (setsIntersect ) break;
    }
      
    // see if set one values intersect set two values
      
    boolean answer = (operator != null && operator.equals( INTERSECTS )) ? setsIntersect : !setsIntersect;
    System.out.println( "returning " + answer );
    return answer;
  }

  public String getFunction(  )
  {
    return this.setFunction;
  }
        
  public IProperty execute(  )
  {
    LOG.debug( "execute( )" );
    boolean value = getBooleanValue( );
    return new BooleanProperty( getName( ) + "_result", value );
  }
    
  @Override
  public boolean equals( IUserCredentials user, IProperty property )
  {
    return (property instanceof IPropertyHolder ) ? equals( user, (IPropertyHolder)property) : false;
  }
    
  @Override
  public boolean equals( IUserCredentials user, IPropertyHolder propHolder )
  {
    System.out.println( "equals..." );
    this.propHolder = propHolder;
    return getBooleanValue( );
  }
}
