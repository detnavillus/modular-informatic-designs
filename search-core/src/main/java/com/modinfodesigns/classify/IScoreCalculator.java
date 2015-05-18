package com.modinfodesigns.classify;

import com.modinfodesigns.property.quantity.IQuantity;

public interface IScoreCalculator
{
  public IQuantity computeScore( MatchStatistics matchStats );
}
