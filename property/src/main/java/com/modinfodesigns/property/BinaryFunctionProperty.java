package com.modinfodesigns.property;

import com.modinfodesigns.property.schema.PropertyDescriptor;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinaryFunctionProperty extends BooleanFunctionProperty implements IExposeInternalProperties
{
  private transient static final Logger LOG = LoggerFactory.getLogger( BinaryFunctionProperty.class );

  private String primaryChoice;    // Choice for a truth value of the boolean function
  private String secondaryChoice;  // Choice for the false value of the boolean function
	
  @Override
  public IProperty execute()
  {
    LOG.debug( "execute( )" );
    boolean tf = getBooleanValue( );

    BinaryProperty binaryProp = new BinaryProperty( primaryChoice, secondaryChoice );
    binaryProp.setName( getName( ) );
    try
    {
      binaryProp.setValue( ((tf) ? primaryChoice : secondaryChoice), null );
    }
    catch (Exception e ) {  }
        
    return binaryProp;
  }
    
  public void setPrimaryChoice( String primaryChoice )
  {
    this.primaryChoice = primaryChoice;
  }
    
  public void setSecondaryChoice( String secondaryChoice )
  {
    this.secondaryChoice = secondaryChoice;
  }

  @Override
  public List<PropertyDescriptor> getInternalProperties()
  {
    ArrayList<PropertyDescriptor> internalProps = new ArrayList<PropertyDescriptor>( );
    PropertyDescriptor affirmDesc = new PropertyDescriptor( );
    affirmDesc.setName( "PrimaryChoice" );
    affirmDesc.setPropertyType( "String" );
    affirmDesc.setDisplayName( "Primary Choice" );
    internalProps.add( affirmDesc );
    
    PropertyDescriptor altDesc = new PropertyDescriptor( );
    altDesc.setName( "SecondaryChoice" );
    altDesc.setPropertyType( "String" );
    altDesc.setDisplayName( "Secondary Choice" );
    internalProps.add( altDesc );
    
    return internalProps;
  }
}
