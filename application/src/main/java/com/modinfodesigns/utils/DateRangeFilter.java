package com.modinfodesigns.utils;

import java.io.File;
import java.io.FilenameFilter;

import java.util.Date;

public class DateRangeFilter implements FilenameFilter
{
  Date fromDate;
  Date toDate;

  DateRangeFilter( Date from, Date to )
  {
    this.fromDate = from;
    this.toDate   = to;
  }
    
  @Override	public boolean accept( File dir, String name)
  {
    File theFile = new File( dir.getAbsolutePath( ) + File.separator + name );
    if (theFile.exists( ) == false) return false;

    if (fromDate == null && toDate == null) return true;

    if (fromDate != null)
    {
      long fromTime = fromDate.getTime( );
      long fileTime = theFile.lastModified( );
      if (fileTime <= fromTime) return false;
    }

    if (toDate != null)
    {
      long toTime = toDate.getTime( );
      long fileTime = theFile.lastModified( );
      if (fileTime >= toTime) return false;
    }

    return true;
  }

}
