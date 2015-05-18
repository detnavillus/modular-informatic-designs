package com.modinfodesigns.utils;

import junit.framework.TestCase;


public class TestFileMethods extends TestCase
{

  public void testFileRead(  ) {
    String filename = "MaryHadALittleLamb.txt";
      
    String filetxt = FileMethods.readFile( filename );
    String expected = "Mary Had A Little Lamb.";;
    assertEquals( filetxt, expected );
  }
    

}