package com.modinfodesigns.utils;

import java.io.IOException;
import java.io.InputStream;

public class FileResourceLoader
{
  public static InputStream getResourceAsStream( String filename ) throws IOException {
    return FileResourceLoader.class.getClassLoader().getResourceAsStream( filename );
  }
    
  public static boolean resourceExists( String filename ) throws IOException {
    return FileResourceLoader.class.getClassLoader().getResourceAsStream( filename ) != null;
  }

}