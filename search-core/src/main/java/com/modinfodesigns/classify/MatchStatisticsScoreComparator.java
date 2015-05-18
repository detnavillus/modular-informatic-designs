package com.modinfodesigns.classify;

import java.util.Comparator;

import com.modinfodesigns.property.quantity.QuantityComparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchStatisticsScoreComparator implements Comparator<MatchStatistics>
{
  private transient static final Logger LOG = LoggerFactory.getLogger( MatchStatisticsScoreComparator.class );

  boolean ascending = true;
	
  public void setAscending( boolean ascending )
  {
    this.ascending = ascending;
  }
	
  @Override
  public int compare(MatchStatistics ms_1, MatchStatistics ms_2)
  {
    if (ms_1 == null || ms_2 == null) return 0;
		
    LOG.debug( "compare " + ms_1.getScore( ) + " with " + ms_2.getScore( ) );
		
    QuantityComparator qC = new QuantityComparator( );
    qC.setAscending( ascending );
    return qC.compare( ms_1.getScore( ), ms_2.getScore( ) );
  }

}
