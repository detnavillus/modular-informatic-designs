package com.modinfodesigns.utils;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UniqueIDGenerator
{
  private transient static final Logger LOG = LoggerFactory.getLogger( UniqueIDGenerator.class );
    
  private static String hexServerIP;

  private static Random seeder = new Random( System.currentTimeMillis( ) );
    
  public static String generateUniqueID( )
  {
    StringBuilder strbuilder = new StringBuilder(16);
    if (hexServerIP == null)
    {
      java.net.InetAddress localAddress = null;
      try
      {
        localAddress = java.net.InetAddress.getLocalHost();
      }
      catch (java.net.UnknownHostException uhe)
      {
      }
      if (localAddress != null)
      {
        byte serverIP[] = localAddress.getAddress();
        hexServerIP = formatAsHex(getIntFrom(serverIP), 8);
      }
      else
      {
        hexServerIP = "127.0.0.1";
      }
    }

    strbuilder.append(hexServerIP);

    long time     = System.currentTimeMillis();
    int lowTime   = (int)time & 0xFFFFFFFF;
    int node      = seeder.nextInt();

    StringBuilder uniqueID = new StringBuilder(32);
    uniqueID.append(formatAsHex(lowTime, 8));
    uniqueID.append(strbuilder.toString());
    uniqueID.append(formatAsHex(node, 8));

    LOG.debug(  "Got unique ID = " + uniqueID.toString( ) );
    return uniqueID.toString();
  }
    
  private static int getIntFrom( byte bytes[] )
  {
    int a = 0;
    int b = 24;
    for (int c = 0; b >= 0; c++)
    {
      int d = bytes[c] & 0xff;
      a += d << b;
      b -= 8;
    }

    return a;
  }

  private static String formatAsHex(int i, int j)
  {
    String s = Integer.toHexString( i );
    return padHexString(s, j) + s;
  }

  private static String padHexString(String str, int ndx)
  {
    StringBuilder strbuilder = new StringBuilder();
    if (str.length() < ndx)
    {
      for (int j = 0; j < ndx - str.length(); j++)
      {
        strbuilder.append('0');
      }
    }
    return strbuilder.toString();
  }

}
