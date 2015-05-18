package com.modinfodesigns.classify;

import com.modinfodesigns.property.quantity.IQuantity;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexMatcherComparator implements Comparator<IIndexMatcher>
{
  private transient static final Logger LOG = LoggerFactory.getLogger( IndexMatcherComparator.class );

  private IScoreCalculator scoreCalculator;
	
  private InvertedIndex invIndex;
	
  private boolean ascending = true;
	
  // Quantity Units - should be something that enables comparisons to return distinct integer values.
  // e.g. if Quantity is always 0.0 - 1.0 Integer comparisons will always be equal
  private String quantityUnits;
	
  // Can also be used to ensure that we get Integer comparisons for values that are 0 to 1
  private Double scoreMultiplier;

	
  public IndexMatcherComparator( InvertedIndex invIndex, IScoreCalculator scoreCalculator,
			                       boolean ascending, String quantityUnits, Double scoreMultiplier )
  {
    this.scoreCalculator = scoreCalculator;
    this.invIndex = invIndex;
    this.ascending = ascending;
    this.quantityUnits = quantityUnits;
    this.scoreMultiplier = scoreMultiplier;
  }
	

  @Override
  public int compare(IIndexMatcher matcher_0, IIndexMatcher matcher_1)
  {
    if (matcher_0 == null || matcher_1 == null)
    {
      LOG.error( "compare( ) called with a NULL pointer!" );
      return 0;
    }
		
    try
    {
      MatchStatistics matchStats_0 = matcher_0.getMatchStatistics( this.invIndex );
      MatchStatistics matchStats_1 = matcher_1.getMatchStatistics( this.invIndex );
        
      if (matchStats_0 == null || matchStats_1 == null)
      {
        LOG.error( "getMatchStatistics( ) returned NULL!" );
        return 0;
      }
			
      IQuantity score_0 = null;
      IQuantity score_1 = null;
				
      if (scoreCalculator != null)
      {
        score_0 = scoreCalculator.computeScore( matchStats_0 );
        score_1 = scoreCalculator.computeScore( matchStats_1 );
      }
      else
      {
        score_0 = matchStats_0.getScore( );
        score_1 = matchStats_1.getScore( );
      }
			
      double multiplier = (scoreMultiplier != null) ? scoreMultiplier.doubleValue() : 1.0;
			
      int score0 = (quantityUnits != null) ? (int)(score_0.getQuantity( quantityUnits ) * multiplier) : (int)(score_0.getQuantity( ) * multiplier);
      int score1 = (quantityUnits != null) ? (int)(score_1.getQuantity( quantityUnits ) * multiplier) : (int)(score_1.getQuantity( ) * multiplier);
			
      return (ascending) ? (score0 - score1 ) : (score1 - score0 );
    }
    catch (Exception e )
    {
      LOG.error( "compare( ) got Exception: " + e );
    }

    return 0;
  }

}
