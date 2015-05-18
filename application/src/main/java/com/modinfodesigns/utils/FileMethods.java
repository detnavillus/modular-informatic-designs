package com.modinfodesigns.utils;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Date;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileMethods
{
  private transient static final Logger LOG = LoggerFactory.getLogger( FileMethods.class );
	
  public static boolean fileExists( String fileName )
  {
    File file = new File( resolveRelativePath( fileName ) );
    return (file != null && file.exists( ));
  }

  public static String readFile( String pFileName )
  {
    return readFile( resolveRelativePath( pFileName ), null );
  }


  public static String readFile( String pFileName, String charSet )
  {
    String fileName = resolveRelativePath( pFileName );

    StringBuffer strbuf = new StringBuffer( 1000 );

    try
    {
      InputStream fis = (fileName.startsWith( "/" )) ? new FileInputStream( fileName ) : FileResourceLoader.getResourceAsStream( fileName );
      BufferedReader br = (charSet == null)
                        ? new BufferedReader( new InputStreamReader( fis ) )
                        : new BufferedReader( new InputStreamReader( fis, charSet ) );

      String line = null;
      while ((line = br.readLine()) != null)
      {
        strbuf.append( line ).append( " \r\n" );
      }

      br.close( );
      fis.close( );
    }
    catch( Exception e )
    {
      LOG.error( "COULD NOT READ: " + fileName + " exception was: " + e );
    }

    return strbuf.toString( ).trim( );
  }

  public static String readFile( String pFileName, long offset, int nChars )
  {
    String fileName = resolveRelativePath( pFileName );

    try
    {
      FileInputStream fis = new FileInputStream( fileName );
      fis.skip( offset );
        
      byte[] charBuf = new byte[ nChars ];
      fis.read( charBuf, 0, nChars );
        
      fis.close( );

      return new String( charBuf );
    }
    catch ( Exception e )
    {
        
    }

    return null;
  }

  public static String[] readFileLines( String pFileName )
  {
    return readFileLines( resolveRelativePath( pFileName ), (String)null );
  }

  public static String[] readFileLines( String pFileName, String charSet )
  {
    return readFileLines( resolveRelativePath( pFileName ), charSet, -1L, -1L);
  }

  public static String[] readFileLines( String pFileName, String charSet, long startAt, long maxLines )
  {
    String fileName = resolveRelativePath( pFileName );

    ArrayList<String> lines = new ArrayList<String>(  );
    long linenum = 0L;
      
    try
    {
      FileInputStream fis = new FileInputStream( fileName );
      BufferedReader br = (charSet == null)
                        ? new BufferedReader( new InputStreamReader( fis ) )
                        : new BufferedReader( new InputStreamReader( fis, charSet ) );
      String line = null;
      while ((line = br.readLine()) != null)
      {
        if (startAt == -1L || (startAt >= 0 && linenum++ >= startAt ))
        {
          lines.add( line );
        }

        if (maxLines > 0 && linenum == maxLines)
        {
          break;
        }
      }

      br.close( );
      fis.close( );
    }
    catch( Exception e )
    {
      LOG.error( "COULD NOT READ: " + fileName );
    }

    String[] lineArray = new String[ lines.size( ) ];
    lines.toArray( lineArray );
    return lineArray;
  }
    
  public static String[] getFileList( String pDirectoryPath, boolean fullPath )
  {
    String directoryPath = resolveRelativePath( pDirectoryPath );
    	
    File dirFile = new File( directoryPath );
    return getFileList( dirFile, fullPath );
  }

  public static String[] getFileList( String directoryPath, boolean fullPath, boolean recurseSubdirectories )
  {
    File dirFile = new File( resolveRelativePath( directoryPath ) );
    return getFileList( dirFile, fullPath, recurseSubdirectories, (FilenameFilter)null, (Date)null );
  }

  public static String[] getFileList( File dirFile, boolean fullPath )
  {
    return getFileList( dirFile, fullPath, false, (FilenameFilter)null, (Date)null );
  }


  public static String[] getFileList( File dirFile, boolean fullPath, boolean recurseSubdirectories )
  {
    return getFileList( dirFile, fullPath, recurseSubdirectories, (FilenameFilter)null, (Date)null );
  }

  public static String[] getFileList( File dirFile, boolean fullPath, boolean recurseSubdirectories, Date from, Date to )
  {
    if (from != null && to != null)
    {
      DateRangeFilter dateRangeFilter = new DateRangeFilter( from, to );
      return getFileList( dirFile, fullPath, recurseSubdirectories, dateRangeFilter, null );
    }
    else
    {
      return getFileList( dirFile, fullPath, recurseSubdirectories, (FilenameFilter)null, (Date)null );
    }
  }

  public static String[] getFileList( File dirFile, boolean fullPath, boolean recurseSubdirectories, FilenameFilter fnameFilter )
  {
    return getFileList( dirFile, fullPath, recurseSubdirectories, fnameFilter, null );
  }


  public static String[] getFileList( File dirFile, boolean fullPath, boolean recurseSubdirectories,
                                      FilenameFilter fnameFilter, Date sinceDate )
  {
    return getFileList( dirFile, fullPath, recurseSubdirectories, fnameFilter, sinceDate, true );
  }

  public static String[] getFileList( File dirFile, boolean fullPath, boolean recurseSubdirectories,
                                      FilenameFilter fnameFilter, Date sinceDate, boolean sortList )
  {

    String dirPath = (fullPath) ? (dirFile.getAbsolutePath( ) + File.separator) : "";
    ArrayList<String> fileNameArray = new ArrayList<String>( );

    try
    {
      if (dirFile.exists( ) && dirFile.isDirectory( ))
      {
        String[] files = dirFile.list( );
        for (int i = 0; i < files.length; i++)
        {
          File aFile = new File( dirFile.getAbsolutePath( ) + File.separator + files[i] );
          if (recurseSubdirectories)
          {
            if (aFile.exists( ) && aFile.isDirectory( ))
            {
              LOG.debug( "Checking subdirectory... " + dirFile.getAbsolutePath( ) + File.separator + files[i] );

              String[] subFileList = getFileList( aFile, fullPath, true, fnameFilter );

              for (int j = 0; j < subFileList.length; j++)
              {
                LOG.debug( "Checking file " + subFileList[j] );
                File subFile = new File( subFileList[j] );

                if (subFile.exists( ) && (fnameFilter == null || fnameFilter.accept( subFile, subFileList[j] ) ))
                {
                  fileNameArray.add( subFileList[j] );
                }
              }
            }
            else if (aFile.exists( ) && (fnameFilter == null || fnameFilter.accept( aFile, aFile.getName( )) ))
            {
              if (sinceDate == null || fileModifiedSince( aFile, sinceDate ))
              {
                fileNameArray.add( dirPath + files[i] );
              }
            }
          }

          else if (aFile.exists( ) && (fnameFilter == null || fnameFilter.accept( aFile, aFile.getName( )) ))
          {
            if (sinceDate == null || fileModifiedSince( aFile, sinceDate ))
            {
              fileNameArray.add( dirPath + files[i] );
            }
          }
        }
      }
      else if (dirFile.exists( ))
      {
        fileNameArray.add( dirFile.getAbsolutePath( ) );
      }
    }
    catch (Exception e )
    {
      LOG.debug( "FileUtils.getFileNameList( ) got Exception: " + e );
    }

    if (sortList)
    {
      Collections.sort( fileNameArray );
    }

    String[] fileList = new String[ fileNameArray.size( ) ];
    fileNameArray.toArray( fileList );
    return fileList;
  }
    
  public static boolean fileModifiedSince( File aFile, Date aDate )
  {
    return (aFile != null && aDate != null) ? (aFile.lastModified() > aDate.getTime()) : false;
  }
    
  public static void addToFile( String pFileName, String content )
  {
    String fileName = resolveRelativePath( pFileName );
      
    try
    {
      fileName = fileName.replace( '\\', '/' );

      PrintWriter pr = new PrintWriter( new FileWriter( fileName, true ) );
        
      pr.println( content );
      pr.close( );
    }
    catch (Exception e )
    {
      LOG.error( "addLine write to '" + fileName + "' failed: " + e );
    }
  }
    
  public static void writeFile( String pFileName, String content )
  {
    writeFile( resolveRelativePath( pFileName ), content, false );
  }

  public static void writeFile( String fileName, String content, boolean createDirectory )
  {
    try
    {
      fileName = resolveRelativePath( fileName ).replace( '\\', '/' );
        
      if (fileName.indexOf( "/" ) > 0)
      {
        String dirName =  fileName.substring( 0,  fileName.lastIndexOf( "/" ));
        File dirFile = new File( dirName );
        if (!dirFile.exists( ))
        {
          if (createDirectory)
          {
            dirFile.mkdir( );
          }
          else
          {
            return;
          }
        }
      }

      PrintWriter pr = new PrintWriter( new FileWriter( fileName ) );

      pr.println( content );
      pr.close( );
    }
    catch (Exception e )
    {
      LOG.error( "writeFile: COULD NOT Write " + fileName );
    }
  }
    
  public static String resolveRelativePath( String inputPath )
  {
    String path = inputPath;
    	
    if (path.startsWith( "./" ))
    {
      File file = new File( "." );
      String basePath = file.getAbsolutePath( );
      basePath = basePath.replace( "\\", "/" );
      basePath = basePath.substring( 0, basePath.lastIndexOf( "/" ));
      System.out.println( "returns " + basePath + path.substring( 1 ) );
      return basePath + path.substring( 1 );
    }
    
    return inputPath;
  }
}
