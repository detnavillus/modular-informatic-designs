package com.modinfodesigns.entity;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;

import com.modinfodesigns.property.string.StringListProperty;
import com.modinfodesigns.property.transform.IPropertyHolderTransform;
import com.modinfodesigns.property.transform.IPropertyTransformListener;
import com.modinfodesigns.property.transform.PropertyTransformException;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.quantity.IntegerRangeProperty;
import com.modinfodesigns.property.quantity.LongRangeProperty;

import java.util.Set;
import java.util.List;

/**
 * Uses an IEntityExtractor to extract a set of terms/phrases from another property.
 * Creates a StringListProperty containing the set of extracted terms/phrases
 * 
 * @author Ted Sullivan
 */

public class EntityExtractorTransform implements IPropertyHolderTransform
{
  private IEntityExtractor entityExtractor;

  private String inputProperty;
  private String outputPropertyName;
    
  private String wordPositionsProperty;
  private String characterPositionsProperty;
    
  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    if (input == null) return null;
    	
    if ( entityExtractor == null )
    {
      throw new PropertyTransformException( "EntityExtractor is NULL!" );
    }
        
    String propValue = input.getValue( );
        
    EntityPositionMap entities = entityExtractor.extractEntities( "text", propValue );
    Set<String> entityPhrases = entities.getMappedPhrases( true );
    if (entityPhrases != null && entityPhrases.size() > 0)
    {
      return new StringListProperty( outputPropertyName, entityPhrases.iterator()  );
    }
        
    return input;
  }

  @Override
  public IPropertyHolder transformPropertyHolder(IPropertyHolder input) throws PropertyTransformException
  {
    if (inputProperty == null) return input;
        
    if (entityExtractor == null)
    {
      throw new PropertyTransformException( "EntityExtractor is NULL!" );
    }
        
    IProperty sourceProperty = input.getProperty( inputProperty );
    if (sourceProperty != null)
    {
      String sourceValue = sourceProperty.getValue( );
      EntityPositionMap entities = entityExtractor.extractEntities( inputProperty, sourceValue );
        
      if (outputPropertyName != null)
      {
        Set<String> entityPhrases = entities.getMappedPhrases( true );
        if (entityPhrases != null && entityPhrases.size() > 0)
        {
          input.addProperty( new StringListProperty( outputPropertyName, entityPhrases.iterator() ) );
        }
      }
            
      if ( wordPositionsProperty != null)
      {
        List<IntegerRangeProperty> ranges = entities.getWordPositions( );
        if (ranges != null && ranges.size() > 0)
        {
          PropertyList pList = new PropertyList(  );
          pList.setName( wordPositionsProperty );
          for (int i = 0; i < ranges.size(); i++)
          {
            pList.addProperty( ranges.get( i ) );
          }
                    
          input.addProperty( pList );
        }
      }
            
      if ( characterPositionsProperty != null)
      {
        List<LongRangeProperty> ranges = entities.getCharacterPositions( );
        if (ranges != null && ranges.size() > 0)
        {
          PropertyList pList = new PropertyList(  );
          pList.setName( characterPositionsProperty );
          for (int i = 0; i < ranges.size(); i++)
          {
            pList.addProperty( ranges.get( i ) );
          }
                    
          input.addProperty( pList );
        }
      }
    }
        
    return input;
  }
    
  @Override
  public void startTransform( IProperty input, IPropertyTransformListener transformListener)
                              throws PropertyTransformException
  {
    // not sure ...
  }
    
  public void setEntityExtractor( IEntityExtractor entityExtractor )
  {
    this.entityExtractor = entityExtractor;
  }

  public void setInputProperty( String inputProperty )
  {
    this.inputProperty = inputProperty;
  }
    
  public void setOutputProperty( String outputPropertyName )
  {
    this.outputPropertyName = outputPropertyName;
  }
    
  public void setWordPositionsProperty( String wordPositionsProperty )
  {
    this.wordPositionsProperty = wordPositionsProperty;
  }
    
  public void setCharacterPositionsProperty( String characterPositionsProperty )
  {
    this.characterPositionsProperty = characterPositionsProperty;
  }

}
