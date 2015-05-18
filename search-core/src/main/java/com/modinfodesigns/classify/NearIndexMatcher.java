package com.modinfodesigns.classify;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.quantity.IntegerRangeProperty;
import com.modinfodesigns.property.quantity.LongRangeProperty;
import com.modinfodesigns.property.quantity.IntegerProperty;
import com.modinfodesigns.property.transform.string.StringTransform;

import com.modinfodesigns.search.QueryTree;
import com.modinfodesigns.security.IUserCredentials;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NearIndexMatcher extends DataObject implements IIndexMatcher
{
  private transient static final Logger LOG = LoggerFactory.getLogger( NearIndexMatcher.class );

  private boolean ordered = false;
    
  private ArrayList<Integer> minDistances = new ArrayList<Integer>( );   // Near Distance
    
  private ArrayList<IIndexMatcher> childMatchers = new ArrayList<IIndexMatcher>( );
    
  private HashSet<String> termSet;
  private HashSet<String> phraseSet;
    
  private String addMatchPhrasesMode;
  private String addMatchPhrasesProperty;
    
  public NearIndexMatcher( )
  {
    minDistances.add( new Integer( 5 ) );
  }
    
  public NearIndexMatcher( boolean ordered )
  {
    this( );
    this.ordered = ordered;
  }
    
  public NearIndexMatcher( QueryTree qTree )
  {
    this( );
    initialize( qTree );
  }
    
  @Override
  public String getType( )
  {
    return "com.modinfodesigns.classify.NearIndexMatcher";
  }
    
  public void initialize( QueryTree qTree )
  {
    // if operator == ONEAR then we are an ordered near...
    if (qTree.getOperator().equalsIgnoreCase( "ONEAR") || qTree.getOperator( ).equalsIgnoreCase( "NEAR" ))
    {
      this.ordered = qTree.getOperator().equalsIgnoreCase( "ONEAR" );
 
      // get the child QueryTree objects ...
      List<QueryTree> children = qTree.getSubTrees( );
      if (children != null && children.size() > 0)
      {
        LOG.debug( "Have " + children.size( ) + " subtrees" );
        for( QueryTree child : children )
        {
          if (child != null)
          {
            IIndexMatcher chMatcher = child.getIndexMatcher( );
            if (chMatcher != null)
            {
              LOG.debug( "adding " + chMatcher );
              childMatchers.add( chMatcher );
            }
          }
        }
      }
    }
    else
    {
      String queryMode = qTree.getMultiTermOperator( );
      if (queryMode != null && queryMode.equalsIgnoreCase( "NEAR" ) || queryMode.equalsIgnoreCase( "ONEAR" ))
      {
        this.ordered = queryMode.equalsIgnoreCase( "ONEAR" );
            	
        String[] terms = StringTransform.getStringArray( qTree.getQueryText( ), " ,.:;-" );
        for (int i = 0, isz = terms.length; i < isz; i++)
        {
          addIndexMatcher( new TermIndexMatcher( terms[i] ));
        }
      }
    }
        
    PropertyList mods = qTree.getQueryModifiers( );
    if (mods != null)
    {
      IProperty dist = mods.getProperty( "DISTANCE", false );
      if (dist != null && dist instanceof IntegerProperty)
      {
        minDistances.add( (Integer)((IntegerProperty)dist).getValueObject( ) );
      }
      else if (dist != null)
      {
        try
        {
          int minDist = Integer.parseInt( dist.getValue( ) );
          minDistances.add( new Integer( minDist ) );
        }
        catch ( NumberFormatException nfe )
        {
          // Not an Integer!!
        }
      }
    }
  }
    
  public void addIndexMatcher( IIndexMatcher matcher )
  {
    childMatchers.add( matcher );
  }
    
  @Override
  public boolean equals( IUserCredentials user, IProperty property )
  {
    if (property == null || !(property instanceof InvertedIndex))
    {
      return false;
    }
    try
    {
      MatchStatistics matchStats = getMatchStatistics( (InvertedIndex)property );
      return matchStats.matches( );
    }
    catch (IndexMatcherException ime )
    {
      return false;
    }
  }

  @Override
  // MatchStatistics must be "pruned" to only include the IntegerRange and CharacterRange for
  // the matched sets ...
  public MatchStatistics getMatchStatistics( InvertedIndex invIndex ) throws IndexMatcherException
  {
    MatchStatistics matchStats = new MatchStatistics( );
    matchStats.setMatches( false );
        
    if (childMatchers.size() < 2)
    {
      if (childMatchers.size() == 1)
      {
        // return true - child is near itself!
        matchStats.setMatches( true );
        matchStats.setIndexMatcher( this );
      }
            
      return matchStats;
    }
        
    MatchStatistics mergedStats = (ordered)
                                ? getOrderedMatchStatistics( invIndex )
                                : getUnorderedMatchStatistics( invIndex );

    if (mergedStats != null && mergedStats.matches() )
    {
      for (IIndexMatcher childMatcher : childMatchers)
      {
        MatchStatistics chStats = childMatcher.getMatchStatistics( invIndex );
        chStats.setIndexMatcher( childMatcher );
        mergedStats.addMatchStatistics( chStats );
      }
    }
        
    MatchStatistics outputStats = (mergedStats != null) ? mergedStats : matchStats;
    if (outputStats.matches())
    {
      outputStats.setIndexMatcher( this );
    }
        
    LOG.debug( "returning " + outputStats );
    return outputStats;
  }
    
  private MatchStatistics getOrderedMatchStatistics( InvertedIndex invIndex ) throws IndexMatcherException
  {
    LOG.debug( "\r\n\r\ngetting ordered MatchStatistics" );
        
    IIndexMatcher child_0 = childMatchers.get( 0 );
    MatchStatistics child_0_stats = child_0.getMatchStatistics( invIndex );
        
    IIndexMatcher child_1 = childMatchers.get( 1 );
    MatchStatistics child_1_stats = child_1.getMatchStatistics( invIndex );
    Integer minDistance = minDistances.get( new Integer( 0 ) );
    MatchStatistics mergedStats = mergeMatchStatistics( invIndex, child_0_stats, child_1_stats, minDistance );

    if (mergedStats.matches( ))
    {
      for (int i = 2; i < childMatchers.size(); i++)
      {
        IIndexMatcher chMatcher = childMatchers.get( i );
        MatchStatistics chStats = chMatcher.getMatchStatistics( invIndex );
        Integer minDist = minDistances.get( new Integer( i - 1 ) );
                
        LOG.debug( "calling mergeMatchStatistics " + i );
        mergedStats = mergeMatchStatistics( invIndex, mergedStats, chStats, minDist );
        if (!mergedStats.matches( ))
        {
          break;
        }
      }
    }
        
    return mergedStats;
  }
    
    
  private MatchStatistics getUnorderedMatchStatistics( InvertedIndex invIndex ) throws IndexMatcherException
  {
    IIndexMatcher child_0 = childMatchers.get( 0 );
    if (child_0 == null) return null;
        
    MatchStatistics mergedStats = child_0.getMatchStatistics( invIndex );
        
    HashSet<Integer> others = new HashSet<Integer>( );
    for (int i = 1; i < childMatchers.size(); i++)
    {
      others.add( new Integer( i ) );
    }
        
    return mergeMatchStatisticsPermutations( invIndex, mergedStats, others );
  }
    
  private MatchStatistics mergeMatchStatisticsPermutations( InvertedIndex invIndex, MatchStatistics mergedStats, HashSet<Integer> others ) throws
    IndexMatcherException
  {
    LOG.debug( "permutations of " + others.size() );
        
    if (others.size() == 0) return mergedStats;
        
    for (Iterator<Integer> otherIt = others.iterator(); otherIt.hasNext(); )
    {
      Integer otherInt = otherIt.next();
      IIndexMatcher other_child = childMatchers.get( otherInt.intValue() );
      MatchStatistics newlyMerged = mergeMatchStatistics( invIndex, mergedStats, other_child.getMatchStatistics( invIndex ), null );
      if (newlyMerged != null && newlyMerged.matches( ))
      {
        HashSet<Integer> reducedSet = new HashSet<Integer>( );
        reducedSet.addAll( others );
        reducedSet.remove( otherInt );

        MatchStatistics nextMerge = mergeMatchStatisticsPermutations( invIndex, newlyMerged, reducedSet );
        if (nextMerge != null && nextMerge.matches())
        {
          return nextMerge;
        }
      }
    }
        
    return null;
  }
    
  private MatchStatistics mergeMatchStatistics( InvertedIndex invIndex, MatchStatistics stats_1, MatchStatistics stats_2, Integer currDist )
  {
    LOG.debug( "mergeMatchStatistics " );
        
    MatchStatistics mergedStats = new MatchStatistics( );
    mergedStats.setMatches( false );
    Set<String> fields = invIndex.getFields( );
        
    for (Iterator<String> fieldIt = fields.iterator(); fieldIt.hasNext(); )
    {
      String field = fieldIt.next();
      LOG.debug( field );
            
      ArrayList<IntegerRangeProperty> child_0_ranges = stats_1.getTextSpans( field );
      ArrayList<IntegerRangeProperty> child_1_ranges = stats_2.getTextSpans( field );
            
      ArrayList<IntegerRangeProperty> nearRanges = getNearRanges( child_0_ranges, child_1_ranges, currDist );
      nearRanges = mergeRanges( nearRanges );
      if (nearRanges != null && nearRanges.size() > 0)
      {
        mergedStats.setMatches( true );
        LOG.debug( "setMatches( true ) " );
                
        for (int i = 0; i < nearRanges.size(); i++)
        {
          IntegerRangeProperty textSpan = nearRanges.get( i );

          Long minCharPos = invIndex.getCharacterPosition( field, textSpan.getMinimum( ) );
          Long maxCharPos = invIndex.getCharacterPosition( field, textSpan.getMaximum( ) );
          maxCharPos = invIndex.getCharacterPositionEnd( field, maxCharPos.longValue() );
                    
          LongRangeProperty lrp = new LongRangeProperty( field, minCharPos.longValue( ), maxCharPos.longValue( ) );
          String fieldStr = invIndex.getFieldString( field );
          int maxCharPosInt = Math.min( maxCharPos.intValue(), fieldStr.length() );
          String spanString = new String( fieldStr.substring( minCharPos.intValue(), maxCharPosInt ) );

          LOG.debug( "Adding phrase: '" + spanString + "'" );
          mergedStats.addPhrase( field, spanString, textSpan, lrp );
          mergedStats.addMatchRange( lrp );
        }
      }
      else
      {
        LOG.debug( "no near ranges for " + field );
      }
    }
        
    LOG.debug( "Returning ... "  + mergedStats.matches( ) );
    return mergedStats;
  }
    

  private ArrayList<IntegerRangeProperty> getNearRanges( ArrayList<IntegerRangeProperty> range_1, ArrayList<IntegerRangeProperty> range_2, Integer currDist )
  {
    ArrayList<IntegerRangeProperty> nearRanges = new ArrayList<IntegerRangeProperty>( );
    if (range_1 == null || range_2 == null)
    {
      LOG.error( "range was NULL!" );
      return nearRanges;
    }
        
    int minimumDist = 5;
    if (currDist != null)
    {
      minimumDist = currDist.intValue();
    }
    else if (minDistances != null && minDistances.get( 0 ) != null)
    {
      minimumDist = minDistances.get( 0 ).intValue( );
    }
        
    // for every IntegerRangeProperty in range_1, check if there is an IntegerRangeProperty in range_2 that is near enough,
    // if so, create a new IntegerRangeProperty that is the union of range_1 and range_2
    for (int i = 0, isz = range_1.size( ); i < isz; i++)
    {
      IntegerRangeProperty irp_1 = range_1.get( i );
      for (int j = 0, jsz = range_2.size( ); j < jsz; j++)
      {
        IntegerRangeProperty irp_2 = range_2.get( j );
                
        int distance = -1;
                
        if (irp_1.getMaximum() == irp_2.getMinimum() || (!ordered && irp_2.getMaximum() == irp_1.getMinimum()))
        {
          distance = 0;
        }
        else if (irp_1.contains( irp_2 ) || irp_2.contains( irp_1))
        {
          distance = 0;
        }
        else if (irp_1.overlaps( irp_2 ))
        {
          distance = (ordered) ? (irp_2.getMinimum() - irp_1.getMinimum()) : Math.abs( irp_2.getMinimum( ) - irp_1.getMinimum( ));
        }
        else if (!ordered && (irp_1.getMinimum( ) >= irp_2.getMaximum( )))
        {
          distance = irp_1.getMinimum() - irp_2.getMaximum( );
        }
        else if (irp_2.getMinimum() >= irp_1.getMaximum( ))
        {
          distance = irp_2.getMinimum() - irp_1.getMaximum( );
        }
                
        LOG.debug( "Distance between " + irp_1.getValue() + " and " + irp_2.getValue( ) + " = " + distance + " minDist = " + minimumDist );
        if (distance > 0 && distance <= minimumDist )
        {
          LOG.debug( " adding range " );
          nearRanges.add( irp_1.union( irp_2 ) );
        }
      }
    }
        
    LOG.debug( "returning " + nearRanges.size() + " ranges." );
    return nearRanges;
  }
    
  private ArrayList<IntegerRangeProperty> mergeRanges( ArrayList<IntegerRangeProperty> ranges )
  {
    ArrayList<IntegerRangeProperty> merged = new ArrayList<IntegerRangeProperty>( );
    	
    for (int i = 0, rsz = ranges.size( ); i < rsz; i++)
    {
      IntegerRangeProperty range_i = ranges.get( i );
      for (int j = i+1; j < rsz; j++ )
      {
        IntegerRangeProperty range_j = ranges.get( j );
        LOG.debug( "checking " + range_i.getMinimum() + " to " + range_i.getMaximum( ) + " with " + range_j.getMinimum( ) + " to " + range_j.getMaximum(  ) );
        if (range_i.overlaps( range_j ))
        {
          LOG.debug( "merging " );
          range_i = range_i.union( range_j );
        }
        else
        {
          LOG.debug( "no overlap!" );
          if (j == (rsz - 1) ) merged.add( range_j );
        }
      }
    		
      merged.add( range_i );
    }
    
    return merged;
  }
    
  public Set<String> getMatchTerms(  )
  {
    if (termSet != null) return termSet;
        
    termSet = new HashSet<String>( );
    for (IIndexMatcher chMatcher : childMatchers)
    {
      Set<String> childSet = chMatcher.getMatchTerms( );
      termSet.addAll( childSet );
    }
        
    return termSet;
  }
    
  public Set<String> getMatchPhrases( )
  {
    if (phraseSet != null) return phraseSet;
        
    phraseSet = new HashSet<String>( );
    for (IIndexMatcher chMatcher : childMatchers)
    {
      Set<String> childSet = chMatcher.getMatchPhrases( );
      phraseSet.addAll( childSet );
    }
        
    return phraseSet;
  }
    
  public void addMatchProperties( IPropertyHolder input, MatchStatistics matchStats )
  {
    MatchStatistics.addMatchProperties( input, matchStats, this );
  }
    
    
  public void setOrdered( boolean ordered )
  {
    this.ordered = ordered;
  }
    
  public void addMinimumDistance( int minDistance )
  {
    this.minDistances.add( new Integer(minDistance ));
  }
    
  public void setMinimumDistance( int minDistance )
  {
    this.minDistances.clear();
    this.minDistances.add( new Integer( minDistance ) );
  }
    
  public void addMatcher( IIndexMatcher childMatcher )
  {
    childMatchers.add( childMatcher );
  }

  public IIndexMatcher getChildMatcher( int index )
  {
    return childMatchers.get( index );
  }

  @Override
  public void setAddMatchPhrasesMode(String addMatchPhrasesMode)
  {
    this.addMatchPhrasesMode = addMatchPhrasesMode;
  }

  @Override
  public void setAddMatchPhrasesProperty(String addMatchPhrasesProperty)
  {
    this.addMatchPhrasesProperty = addMatchPhrasesProperty;
  }

  @Override
  public String getAddMatchPhrasesMode()
  {
    return this.addMatchPhrasesMode;
  }

  @Override
  public String getAddMatchPhrasesProperty()
  {
    return this.addMatchPhrasesProperty;
  }
}
