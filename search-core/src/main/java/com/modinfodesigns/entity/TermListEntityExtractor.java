package com.modinfodesigns.entity;

import java.util.ArrayList;
import java.util.List;

import com.modinfodesigns.classify.InvertedIndex;

import com.modinfodesigns.utils.FileMethods;

/**
 * Uses a list of terms as Entity Extraction Source.
 * 
 * @author Ted Sullivan
 */

public class TermListEntityExtractor implements IEntityExtractor
{
  private ArrayList<String> entityTerms;
    
  private String prefixString;
    
  private String postfixString;
    
  private boolean caseSensitive = false;
    
  public void addEntityTerm( String entityTerm )
  {
    if (entityTerms == null) entityTerms = new ArrayList<String>( );
    entityTerms.add( entityTerm );
  }
    
  public void setEntityTerms( List<String> entTerms )
  {
    if (this.entityTerms != null)
    {
      this.entityTerms.clear();
    }
    else
    {
      this.entityTerms = new ArrayList<String>( );
    }
        
    this.entityTerms.addAll( entTerms );
  }
    
  public void setEntityTerms( String[] entTerms )
  {
    if (this.entityTerms != null)
    {
      this.entityTerms.clear();
    }
    else
    {
      this.entityTerms = new ArrayList<String>( );
    }
        
    for (int i = 0; i < entTerms.length; i++)
    {
      entityTerms.add( entTerms[i] );
    }
  }
    
  public void setPrefixString( String prefixString )
  {
    this.prefixString = prefixString;
  }
    
  public void setPostfixString( String postfixString )
  {
    this.postfixString = postfixString;
  }
    
  @Override
  public EntityPositionMap extractEntities( String field, String fromString )
  {
    EntityPositionMap ePositionMap = new EntityPositionMap( );
        
    // Create the InvertedIndex - check if it contains the entity term, if so, create
    // EntityMapper for the term and add it to the EntityMapperSet
    InvertedIndex invIndex = new InvertedIndex( field, fromString );
    
    for (int i = 0; i < entityTerms.size(); i++)
    {
      String term = entityTerms.get( i );
      List<Integer> phrasePos = invIndex.getPhrasePositions( field, term, caseSensitive );
      if (phrasePos != null)
      {
        PhrasePositionMap phraseMap = new PhrasePositionMap( term );
        phraseMap.setPhrasePositions( field, invIndex );
        ePositionMap.addPhrasePositionMap( phraseMap );
      }
    }
        
    if (prefixString != null && postfixString != null)
    {
      ePositionMap.setPrefixString( prefixString );
      ePositionMap.setPostfixString( postfixString );
    }
        
    return ePositionMap;
  }

  @Override
  public String getPrefixString()
  {
    return prefixString;
  }

  @Override
  public String getPostfixString()
  {
    return postfixString;
  }
    

  public void loadFromFile( String filename )
  {
    String[] termList = FileMethods.readFileLines( filename );
    if (termList != null)
    {
      setEntityTerms( termList );
    }
  }
}
