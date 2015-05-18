package com.modinfodesigns.classify.subject;

import com.modinfodesigns.classify.IIndexMatcher;
import com.modinfodesigns.classify.MatchStatistics;

public interface ISubjectMatcher extends IIndexMatcher
{
  public String getSubject( MatchStatistics matchStats );
}
