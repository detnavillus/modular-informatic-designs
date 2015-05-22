package com.modinfodesigns.utils;

import java.util.List;

import junit.framework.TestCase;

public class TestPermutationMethods extends TestCase
{
  private boolean debugConsole = true;
    
  public void testGetArray( )
  {
    int[] array = PermutationMethods.getArray( 1, 4 );
    int[] result = { 1, 2, 3, 4 };
      
    arraysEqual( array, result );
      
    if (debugConsole)
    {
      printArray( array );
      System.out.println( "" );
      printArray( result );
      System.out.println( "" );
    }
  }
    
  public void testGetArrays( )
  {
    List<int[]> arrays = PermutationMethods.getArrays( 1, 4 );
    // result should be [1],[2],[3],[4]
    assertEquals( arrays.size(), 4 );
      
    for (int i = 0; i < 4; i++ )
    {
      int[] expected = new int[1];
      expected[0] = i+1;
      int[] got = arrays.get( i );
      arraysEqual( got, expected );
    }
      
    if (debugConsole)
    {
      printArrays( arrays );
      System.out.println( "" );
    }
  }
    
  public void testGetPermutations( )
  {
    List<int[]> arrays = PermutationMethods.getPermutations( 1, 4, 2 );
    // result should be: [1],[2],[3],[4],[1,2],[1,3],[1,4],[2,3],[2,4],[3,4]
    assertEquals( arrays.size(), 10 );
      
    int[] test = arrays.get( 6 );
    int[] expected4 = {1,4};
    arraysEqual( test, expected4 );
      
    test = arrays.get( 9 );
    int[] expected9 = {3,4};
    arraysEqual( test, expected9 );
      
    if (debugConsole)
    {
      printArrays( arrays );
      System.out.println( "" );
    }
      
    arrays = PermutationMethods.getPermutations( 1, 4, 4 );
    // result should be: [1],[2],[3],[4],[1,2],[1,3],[1,4],[2,3],[2,4],[3,4],[1,2,3],[1,2,4],[1,3,4],[2,3,4],[1,2,3,4]
    assertEquals( arrays.size(), 15 );
      
    test = arrays.get( 10 );
    int[] expected10 = {1,2,3};
    arraysEqual( test, expected10 );
      
    if (debugConsole)
    {
      printArrays( arrays );
      System.out.println( "" );
    }
  }
    
  private void arraysEqual( int[] arr1, int[] arr2 )
  {
    assertEquals( arr1.length, arr2.length );
      
    for( int i = 0; i < arr1.length; i++)
    {
      assertEquals( arr1[i], arr2[i] );
    }
  }
    
  private void printArray( int[] array )
  {
    System.out.print( "[" );
    for (int i = 0; i < array.length; i++ )
    {
      if (i > 0) System.out.print( "," );
      System.out.print( array[i] );
    }
    System.out.print( "]" );
  }
    
  private void printArrays( List<int[]> arrays )
  {
    for (int i = 0; i < arrays.size(); i++ )
    {
      if (i > 0) System.out.print( "," );
      printArray( arrays.get( i ) );
    }
  }
}