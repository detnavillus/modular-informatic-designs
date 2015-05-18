package com.modinfodesigns.entity;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.PropertyValidationException;

import com.modinfodesigns.property.quantity.IntegerProperty;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.transform.IPropertyHolderTransform;
import com.modinfodesigns.property.transform.IPropertyTransformListener;
import com.modinfodesigns.property.transform.PropertyTransformException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms a PropertyHolder by comparing two versions of a Person's name, differing in 
 * data (e.g. First Name vs First Initial only). Computes a score based on how much confidence
 * should be given to the name match.
 * 
 * (put formula here).
 * 
 * @author Ted Sullivan
 */

public class PersonNameMatcher implements IPropertyHolderTransform
{
  private transient static final Logger LOG = LoggerFactory.getLogger( PersonNameMatcher.class );

  private String firstNameResultField;
  private String firstNameMatchField;

  private String lastNameResultField;
  private String lastNameMatchField;

  private String middleNameResultField;
  private String middleNameMatchField;

  private String firstInitialResultField;
  private String firstInitialMatchField;

  private String middleInitialResultField;
  private String middleInitialMatchField;

  private String suffixResultField;
  private String suffixMatchField;

  private String matchScoreField;
    
    
    
  public void setFirstNameResultField( String firstNameResultField )
  {
    this.firstNameResultField = firstNameResultField;
  }
    
  public void setFirstNameMatchField( String firstNameMatchField )
  {
    this.firstNameMatchField = firstNameMatchField;
  }

  public void setLastNameResultField( String lastNameResultField )
  {
    this.lastNameResultField = lastNameResultField;
  }
    
  public void setLastNameMatchField( String lastNameMatchField )
  {
    this.lastNameMatchField = lastNameMatchField;
  }

  public void setMiddleNameResultField( String middleNameResultField )
  {
    this.middleNameResultField = middleNameResultField;
  }
    
  public void setMiddleNameMatchField( String middleNameMatchField )
  {
    this.middleNameMatchField = middleNameMatchField;
  }

  public void setFirstInitialResultField( String firstInitialResultField )
  {
    this.firstInitialResultField = firstInitialResultField;
  }
    
  public void setFirstInitialMatchField( String firstInitialMatchField )
  {
    this.firstInitialMatchField = firstInitialMatchField;
  }

  public void setMiddleInitialResultField( String middleInitialResultField )
  {
    this.middleInitialResultField = middleInitialResultField;
  }
    
  public void setMiddleInitialMatchField( String middleInitialMatchField )
  {
    this.middleInitialMatchField = middleInitialMatchField;
  }

  public void setSuffixResultField( String suffixResultField )
  {
    this.suffixResultField = suffixResultField;
  }
    
  public void setSuffixMatchField( String suffixMatchField )
  {
    this.suffixMatchField = suffixMatchField;
  }

  public void setMatchScoreField( String matchScoreField )
  {
    this.matchScoreField = matchScoreField;
  }

  @Override
  public IProperty transform(IProperty input) throws PropertyTransformException
  {
    if (input instanceof IPropertyHolder)
    {
      IPropertyHolder propHolder = (IPropertyHolder)input;
      return transformPropertyHolder( propHolder );
    }
    return null;
  }

  @Override
  public void startTransform(IProperty input, IPropertyTransformListener transformListener)
            throws PropertyTransformException
  {

  }

  @Override
  public IPropertyHolder transformPropertyHolder(IPropertyHolder input) throws PropertyTransformException
  {
    LOG.debug( "tranform: " + input.getValue( ) );
    if (!lastNamesMatch( input ) )
    {
      return input; // no chance
    }

    try
    {
      adjustFirstMiddleNames( input );
    }
    catch ( PropertyValidationException pve )
    {
        	
    }

    boolean hasFirstNameMatch = false;
    boolean hasMiddleNameMatch = false;

    if (firstNameResultField != null)
    {
      if ( input.getProperty( firstNameResultField ) != null )
      {
        if ( input.getProperty( firstNameMatchField ) != null && !firstNamesMatch( input ) )
        {
          LOG.debug( "No match - first names are different." );
          return input;  // first names are different
        }
        else if ( input.getProperty( firstNameMatchField ) != null)
        {
          LOG.debug( "First Names match!" );
          hasFirstNameMatch = true;
        }
      }
    }
        
    if (middleNameResultField != null)
    {
      if ( input.getProperty( middleNameResultField ) != null )
      {
        if ( input.getProperty( middleNameMatchField ) != null && !middleNamesMatch( input ))
        {
          LOG.debug( "No match - middle names are different." );
          return input;  // middle names are different
        }
        else if ( input.getProperty( middleNameMatchField ) != null)
        {
          hasMiddleNameMatch = true;
        }
      }
    }

    if (hasFirstNameMatch && hasMiddleNameMatch )
    {
      LOG.debug( "Very Good match - First Name, Middle Name" );
      input.addProperty( new IntegerProperty( matchScoreField, 100 ) );   // Best possible match
      return input;
    }

    if (hasFirstNameMatch && !hasMiddleNameMatch && middleInitialResultField != null)
    {
      if ( input.getProperty( middleInitialResultField ) != null && middleInitialsMatch( input ) )
      {
        LOG.debug( "Good match - First Name, Middle Initial" );
        input.addProperty( new IntegerProperty( matchScoreField, 80 ) );  // Good match - First Name, Middle Initial
        return input;
      }
      else if ( input.getProperty( middleInitialMatchField ) != null)
      {
        LOG.debug( "No match - middle initials are different." );
        return input;  // Doesn't match - middile initial is different
      }
      else
      {
        LOG.debug( "Pretty Good - First Names match - no middle initial" );
        input.addProperty( new IntegerProperty( matchScoreField, 60 ) );
        return input;
      }
    }

    if (!hasFirstNameMatch && firstInitialResultField != null)
    {
      if ( input.getProperty( firstInitialResultField ) != null && firstInitialsMatch( input ) )
      {
        LOG.debug( "First Initials Match." );

        if (hasMiddleNameMatch)
        {
          LOG.debug( "Pretty Good - First Initial and Middle Name match." );
          input.addProperty( new IntegerProperty( matchScoreField, 60 ) );  // Pretty Good - First Initial and Middle Name match
          return input;
        }
        else if ( input.getProperty( middleInitialResultField ) != null && middleInitialsMatch( input ) )
        {
          LOG.debug( "OK - First Initial, Middle Initial match." );
          input.addProperty( new IntegerProperty( matchScoreField, 40 ) );  // OK - First Initial, Middle Initial match
          return input;
        }
        else if ( input.getProperty( middleInitialMatchField ) == null )
        {
          LOG.debug( "Barely OK - just have last name, first initial." );
          input.addProperty( new IntegerProperty( matchScoreField, 20 ) );  // Barely OK - just have last name, first initial
          return input;
        }
      }
      else if ( input.getProperty( middleInitialMatchField ) != null )
      {
        LOG.debug( "No match - first initials are different." );
        return input;  // No match - first initials are different
      }
    }

    LOG.debug( "Very poor match but still some hope - All we have is last name, all other data is missing." );
    input.addProperty( new IntegerProperty( matchScoreField, 5 ) );  // Very poor match but still some hope - All we have is last name, all other data is missing

    return input;
  }

    
  private void adjustFirstMiddleNames( IPropertyHolder input ) throws PropertyValidationException
  {
    if ( firstNameResultField != null && firstInitialResultField != null )
    {
      IProperty firstNameProp = input.getProperty( firstNameResultField );
      if (firstNameProp != null && firstNameProp.getValue().endsWith( "." ))
      {
        String firstNameVal = firstNameProp.getValue( );
                
        firstNameProp.setValue( firstNameVal.substring( 0, firstNameVal.indexOf( "." ) ), null );
      }

      if (firstNameProp != null && firstNameProp.getValue().trim().length() == 1)
      {
        LOG.debug( "Changing First Name Result'" + firstNameProp + "' to First Initial" );
        input.removeProperty( firstNameResultField );
        input.setProperty( new StringProperty( firstInitialResultField, firstNameProp.getValue( ).trim( ) ) );
      }
    }

    if ( firstNameMatchField != null && firstInitialMatchField != null )
    {
      IProperty firstNameMatchProp = input.getProperty( firstNameMatchField );
      if (firstNameMatchProp != null && firstNameMatchProp.getValue().endsWith( "." ))
      {
        String firstNameMatchVal = firstNameMatchProp.getValue( );
                
        firstNameMatchProp.setValue( new String( firstNameMatchVal.substring( 0, firstNameMatchVal.indexOf( "." ) ) ), null );
      }

      if (firstNameMatchProp != null && firstNameMatchProp.getValue().trim().length() == 1)
      {
        LOG.debug( "Changing First Name Match '" + firstNameMatchProp + "' to First Initial: " + firstNameMatchProp.getValue().trim( ) );
        input.removeProperty( firstNameMatchField );
        input.setProperty( new StringProperty( firstInitialMatchField, firstNameMatchProp.getValue().trim( ) ) );
      }
    }

    if ( middleNameResultField != null && middleInitialResultField != null )
    {
      IProperty middleNameProp = input.getProperty( middleNameResultField );
      if ( middleNameProp != null && middleNameProp.getValue().endsWith( "." ))
      {
        String middleNamePropVal = middleNameProp.getValue( );
        middleNameProp.setValue(  new String( middleNamePropVal.substring( 0, middleNamePropVal.indexOf( "." ) ) ), null);
      }
        
      if ( middleNameProp != null && middleNameProp.getValue().trim().length() == 1)
      {
        input.removeProperty( middleNameResultField );
        input.setProperty( new StringProperty( middleInitialResultField, middleNameProp.getValue().trim( ) ) );
      }
    }

    if ( middleNameMatchField != null && middleInitialMatchField != null )
    {
      IProperty middleNameMatchProp = input.getProperty( middleNameMatchField );
      if ( middleNameMatchProp != null && middleNameMatchProp.getValue().endsWith( "." ))
      {
        String middleNameMatchVal = middleNameMatchProp.getValue( );
        middleNameMatchProp.setValue(  new String( middleNameMatchVal.substring( 0, middleNameMatchVal.indexOf( "." ) ) ), null );
      }

      if ( middleNameMatchProp != null && middleNameMatchProp.getValue().trim().length() == 1)
      {
        input.removeProperty( middleNameMatchField );
        input.setProperty( new StringProperty( middleInitialMatchField, middleNameMatchProp.getValue().trim( ) ) );
      }
    }
  }
    
  private boolean lastNamesMatch( IPropertyHolder input )
  {
    return ( lastNameResultField != null && input.getProperty( lastNameResultField ) != null
           && lastNameMatchField != null && input.getProperty( lastNameMatchField ) != null
           && input.getProperty( lastNameResultField ).getValue( ).equals( input.getProperty( lastNameMatchField ).getValue( ) ) )

          ? true : false;
  }
    
  private boolean firstNamesMatch( IPropertyHolder input )
  {
    return ( firstNameResultField != null && input.getProperty( firstNameResultField ) != null
          && firstNameMatchField != null && input.getProperty( firstNameMatchField ) != null
          && input.getProperty( firstNameResultField ).getValue().equals( input.getProperty( firstNameMatchField ).getValue() ) )

          ? true : false;
  }

  private boolean middleNamesMatch( IPropertyHolder input )
  {
    return ( middleNameResultField != null && input.getProperty( middleNameResultField ) != null
          && middleNameMatchField != null && input.getProperty( middleNameMatchField ) != null
          && input.getProperty( middleNameResultField ).getValue( ).equals( input.getProperty( middleNameMatchField ).getValue( ) ) )

          ? true : false;
  }

  private boolean firstInitialsMatch( IPropertyHolder input )
  {
    LOG.debug( "firstInitialsMatch ? '" + input.getProperty(  firstInitialResultField ).getValue( ) + "' == '" + input.getProperty(  firstInitialMatchField ).getValue  ( ) + "'" );
    return ( firstInitialResultField != null && input.getProperty( firstInitialResultField ) != null
          && firstInitialMatchField != null && input.getProperty( firstInitialMatchField ) != null
          && input.getProperty( firstInitialResultField ).getValue( ).equals( input.getProperty( firstInitialMatchField ).getValue( ) ) )

           ? true : false;
  }


  private boolean middleInitialsMatch( IPropertyHolder input )
  {
    return ( middleInitialResultField != null && input.getProperty( middleInitialResultField ) != null
          && middleInitialMatchField != null && input.getProperty( middleInitialMatchField ) != null
          && input.getProperty( middleInitialResultField ).getValue( ).equals( input.getProperty( middleInitialMatchField ).getValue( ) ) )

          ? true : false;
  }
}
