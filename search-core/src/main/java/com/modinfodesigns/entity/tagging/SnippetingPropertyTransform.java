package com.modinfodesigns.entity.tagging;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.quantity.LongRangeProperty;
import com.modinfodesigns.property.transform.IPropertyHolderTransform;
import com.modinfodesigns.property.transform.IPropertyTransformListener;
import com.modinfodesigns.property.transform.PropertyTransformException;
import com.modinfodesigns.property.string.StringProperty;

import com.modinfodesigns.entity.IEntityExtractor;
import com.modinfodesigns.entity.EntityPositionMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a set of snippets around entities extracted by one or more EntityExtractors 
 * from an input field. Uses an once the Entity Extractors have identified their text segments,
 * snippets or excerpts are computed in which nearby entities are merged until the distance between
 * adjacent entities exceeds some threshold. 
 * 
 * The entities identified by each IEntityExtractor are then tagged using an EntityTaggerStringTransform.
 * 
 * @author Ted Sullivan
 */

public class SnippetingPropertyTransform implements IPropertyHolderTransform
{
  private transient static final Logger LOG = LoggerFactory.getLogger( SnippetingPropertyTransform.class );

  // Name of property to apply the snippeting operation
  private String inputProperty;
    
  // Name of snippet Property
  private String outputProperty;
    
  private HashMap<String,IEntityExtractor> entityExtractors;

  private long clusterDistance = 100;
    
  private int padding = 50;
    
  private int maxSnippets = 0;  // if > 0 only create x snippets
    
  public void setInputProperty( String inputProperty )
  {
    this.inputProperty = inputProperty;
  }
    
  public void setOutputProperty( String outputProperty )
  {
    this.outputProperty = outputProperty;
  }
    
  public void setPadding( int padding )
  {
    this.padding = padding;
  }
    
  public void setPadding( String padding )
  {
    this.padding = Integer.parseInt( padding );
  }
    
  public void addEntityExtractor( IEntityExtractor entityExtractor )
  {
    if (entityExtractor == null) return;
    
    LOG.debug( "Adding Entity Extractor: " + entityExtractor );
    	
    if (entityExtractors == null) entityExtractors = new HashMap<String,IEntityExtractor>( );
    String extractorName = Integer.toString( entityExtractors.size( ) );
    entityExtractors.put( extractorName, entityExtractor );
  }
    
  public void addEntityExtractors( List<IEntityExtractor> entityExtractorList )
  {
    if (entityExtractorList == null) return;
    for (int i = 0; i < entityExtractorList.size(); i++)
    {
      addEntityExtractor( entityExtractorList.get( i ) );
    }
  }
    
  public void setEntityExtractors( Map<String,IEntityExtractor> extractorMap )
  {
    if (extractorMap == null) return;
    
    this.entityExtractors = new HashMap<String,IEntityExtractor>( );
    for (Iterator<String> nameIt = extractorMap.keySet().iterator(); nameIt.hasNext( ); )
    {
      String extractorName = nameIt.next( );
      IEntityExtractor entityExtractor = extractorMap.get( extractorName );
      entityExtractors.put( extractorName, entityExtractor );
    }
  }
    
  public void setClusterDistance( long clusterDistance )
  {
    this.clusterDistance = clusterDistance;
  }
    
  public void setClusterDistance( String clusterDistance )
  {
    this.clusterDistance = Long.parseLong( clusterDistance );
  }
    
  public void setMaxSnippets( int maxSnippets )
  {
    this.maxSnippets = maxSnippets;
  }
    
  public void setMaxSnippets( String maxSnippets )
  {
    this.maxSnippets = Integer.parseInt( maxSnippets );
  }

  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    if ( input == null )
    {
      throw new PropertyTransformException( "Property is NULL!" );
    }
        
    String inputString = input.getValue( );
    return getSnippets( inputString );
  }


  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input )
                                                  throws PropertyTransformException
  {
    if (inputProperty == null)
    {
      throw new PropertyTransformException( "Configuration ERROR: No input property specified!" );
    }
        
    if (input instanceof IDataList)
    {
      IDataList inputLst = (IDataList)input;
      Iterator<DataObject> resIt = inputLst.getData( );
      while (resIt != null && resIt.hasNext( ) )
      {
        DataObject res = resIt.next( );
        transformPropertyHolder( res );
      }
    }
    else
    {
      IProperty inputProp = input.getProperty( inputProperty );
      if (inputProp != null)
      {
        PropertyList outputProp = getSnippets( inputProp.getValue( ) );
        if (outputProp != null && outputProperty != null)
        {
          outputProp.setName( outputProperty );
          LOG.debug( "Adding outputProp " + outputProperty );
          input.setProperty( outputProp );
        }
      }
    }
        
    return input;
  }
    
    
  public PropertyList getSnippets( String inputString )
  {
    // =====================================================================
    // Temporary map of IEntityExtractor name --> EntityMapperSet
    // =====================================================================
    return getSnippets( inputString, new HashMap<String,EntityPositionMap>( ), true );
  }
    
    
  /**
   * Creates a set of Snippets from the set of IEntityExtractors that this Snippeting Transform
   * has. Can be used to get the set of snippets from a String along with the set of Entities that
   * were extracted to create the snippets.
   *
   * @param inputString
   * @param entityMappers        Gets populated with the output of the EntityExtractors
   * @return
   */
  public PropertyList getSnippets( String inputString, Map<String,EntityPositionMap> entityMappers, boolean extractFirst )
  {
    if (extractFirst)
    {
      LOG.debug( "extractFirst ..." );
      // =====================================================================
      // Create a list of charPositions named by the Entity Extractor
      // =====================================================================
      for (Iterator<String> extractIt = entityExtractors.keySet().iterator(); extractIt.hasNext(); )
      {
        String extractorName = extractIt.next();

        IEntityExtractor extractor = entityExtractors.get( extractorName );
        EntityPositionMap ePositionMap = extractor.extractEntities( "text", inputString );
        if (ePositionMap != null)
        {
          entityMappers.put( extractorName, ePositionMap );
        }
        else
        {
          LOG.debug( "Got no ePositionMap for " + extractorName );
        }
      }
    }
        
    LOG.debug( "Getting E Mapper Set ..." + entityMappers );
    ArrayList<LongRangeProperty> charPositionList = new ArrayList<LongRangeProperty>( );
    for (Iterator<String> eMapIt = entityMappers.keySet().iterator(); eMapIt.hasNext(); )
    {
      String eMapperName = eMapIt.next( );
      LOG.debug( "checking mapper: " + eMapperName );
        	
      EntityPositionMap ePositionMap = entityMappers.get( eMapperName );
        	
      if (ePositionMap != null)
      {
        // ==========================================================================
        // rename a copy of each LongRangeProperty with the extractorName so that we
        // can keep track of which character range came from which Entity Extractor
        // and so that we can sort them easily.
        // ==========================================================================
        List<LongRangeProperty> charRanges = ePositionMap.getCharacterPositions( );
        if (charRanges != null && charRanges.size() > 0)
        {
          for (int i = 0; i < charRanges.size( ); i++)
          {
            LongRangeProperty charRange = charRanges.get( i );
            LongRangeProperty charCopy = (LongRangeProperty)charRange.copy( );
            charCopy.setName( eMapperName );
            charPositionList.add( charCopy );
          }
        }
      }
    }

    LOG.debug( "Getting Snippets now ..." );
    if (charPositionList != null && charPositionList.size() > 0)
    {
      LOG.debug( "sorting ..." );
      // =======================================================
      // First: sort the charPositionList by minimum position
      // =======================================================
      Collections.sort( charPositionList );
            
            
      LOG.debug( "Clustering ..." );
      // ===============================================================================================
      // next cluster the ranges based on the distance between adjacent character ranges. If adjacent
      // character ranges are farther apart than some minimum distance, start a new cluster
      // ===============================================================================================
      ArrayList<ArrayList<LongRangeProperty>> clusters = new ArrayList<ArrayList<LongRangeProperty>>( );
        
      ArrayList<LongRangeProperty> curCluster = new ArrayList<LongRangeProperty>( );
      LOG.debug( "adding " + charPositionList );
      curCluster.add( charPositionList.get( 0 ) );
      clusters.add( curCluster );
            
      for (int i = 0; i < charPositionList.size() - 1; i++ )
      {
        LongRangeProperty range_1 = charPositionList.get( i );
        LongRangeProperty range_2 = charPositionList.get( i + 1 );
                
        LOG.debug( "Char Positions 1: " + range_1.getValue( ) );
        LOG.debug( "Char Positions 2: " + range_2.getValue( ) );
            
        if (range_1 != null && range_2 != null)
        {
          long distance = range_1.distance( range_2 );
          if (distance > clusterDistance )
          {
            // ================================================
            // We have hit a boundary, so start a new cluster:
            // ================================================
            curCluster = new ArrayList<LongRangeProperty>( );
            clusters.add( curCluster );
          }
                
          curCluster.add( range_2 );
        }
      }
            
      // ================================================================================================
      // Now, for each cluster, compute the union of the ranges ...
      // ================================================================================================
      ArrayList<LongRangeProperty> clusterRanges = new ArrayList<LongRangeProperty>( );
      for (int i = 0; i < clusters.size( ); i++)
      {
        LongRangeProperty union = null;
        ArrayList<LongRangeProperty> clusterList = clusters.get( i );
        for (int j = 0; j < clusterList.size( ); j++)
        {
          LongRangeProperty lrp = clusterList.get( j );
          if (union == null)
          {
            union = lrp;
          }
          else
          {
            union = union.union( lrp );
          }
        }
        if (union == null)
        {
          union = new LongRangeProperty( );
        }
                
        clusterRanges.add( union );
      }
            
      LOG.debug( "Creating snippet list " );
      PropertyList pList = new PropertyList( );
      pList.setName( outputProperty );
            
      // add some padding(?), compute the offsets of the contained ranges relative to the cluster ...
      int maxClusters = (maxSnippets > 0) ? Math.min( clusters.size(), maxSnippets) : clusters.size( );
      for (int i = 0; i < maxClusters; i++)
      {
        ArrayList<LongRangeProperty> clusterList = clusters.get( i );
        LongRangeProperty union = clusterRanges.get( i );
                
        long minimum = Math.max( union.getMinimum(), 0L );
        long offset = minimum;
                
        long maximum = Math.min( union.getMaximum( ), inputString.length() );
                
        String snippet = new String( inputString.substring( (int)minimum, (int)maximum ));
          
        // ======================================================================
        // Collect the EntityMapperSets from the cluster ranges using the name
        // mapping set extractorName mapping set up earlier
        // ======================================================================
        ArrayList<EntityPositionMap> entityMapperList = new ArrayList<EntityPositionMap>( );
        for (int j = 0; j < clusterList.size(); j++)
        {
          LongRangeProperty phraseRange = clusterList.get( j );
                    
          EntityPositionMap eMapperSet = entityMappers.get( phraseRange.getName( ) );
          entityMapperList.add( eMapperSet );
        }
                
        // =======================================================================
        // Transform the string with the prefix/postfix tags.  Need to specify -offset to
        // account for the relative position of the snippet's start point within the inputString
        // (from which the character positions are computed).
        // =======================================================================
        snippet = EntityTaggerStringTransform.transformString( entityMapperList, snippet, -offset );
        LOG.debug( "Add padding to '" + snippet + "'" );
                
        // Add the padding before and after the first and last entities.
        int beforeStart = Math.max( ((int)offset - padding), 0 );
        if (beforeStart > 0 && inputString.indexOf( " " ) > beforeStart ) beforeStart = inputString.indexOf( " ", beforeStart );
                
        if (beforeStart >= 0)
        {
          String before = new String( inputString.substring( beforeStart, (int)offset ));
                
          int afterEnd = Math.min( ((int)union.getMaximum() + padding), inputString.length() );
          String after = new String( inputString.substring( (int)union.getMaximum(), afterEnd ));
          after = (after.indexOf( " " ) > 0) ? new String( after.substring( 0, after.lastIndexOf( " " ))) : after.trim( );
          snippet = before + snippet + after;
        }
                
        StringProperty snippetProp = new StringProperty( "snippet", snippet.trim( ) );
                
        LOG.debug( "Adding snippet " + snippet );
        pList.addProperty( snippetProp );
      }
            
      LOG.debug( "Done snippeting!" );
      return pList;
    }
        
    LOG.debug( "returning null!" );
    return null;
  }

    
  @Override
  public void startTransform( IProperty input, IPropertyTransformListener transformListener)
                            throws PropertyTransformException
  {

  }
}
