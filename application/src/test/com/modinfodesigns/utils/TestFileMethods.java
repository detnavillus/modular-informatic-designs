package com.modinfodesigns.utils;

import junit.framework.TestCase;

import java.io.File;


public class TestFileMethods extends TestCase
{

  public void testFileRead(  ) {
    String filename = "MaryHadALittleLamb.txt";
      
    String filetxt = FileMethods.readFile( filename );
    String expected = "Mary Had A Little Lamb.";;
    assertEquals( filetxt, expected );
  }
    
  public void testResolveRelativePath( ) {
    String currentPath = FileMethods.resolveRelativePath( "./application/src/test/resources" );
    File currFile = new File( currentPath );
    assertTrue( currFile.exists( ) );
    assertTrue( currFile.isDirectory( ) );
  }

}