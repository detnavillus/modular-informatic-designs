package com.modinfodesigns.utils;

import java.util.List;
import java.util.ArrayList;

public class PermutationMethods
{
  // pairs   1,4  [1,2],[1,3],[1,4],[2,3],[2,4],[3,4]
  // triples 1,4  [1,2,3],[1,2,4],[1,3,4],[2,3,4]
  // triples 1,5  [1,2,3],[1,2,4],[1,2,5],[1,3,4],[1,3,5],[1,4,5],[2,3,4],[2,3,5],[2,4,5],[3,4,5]
  public static List<int[]> getPermutations( int start, int end, int arraySize )
  {
    ArrayList<int[]> permutations = new ArrayList<int[]>( );
    List<int[]> singles = getArrays( start, end );
    permutations.addAll( singles );
      
    List<int[]> lastArrays = singles;
    for ( int i = 2; i <= arraySize; i++ )
    {
      List<int[]> nextArrays = getArrays( lastArrays, end );
      permutations.addAll( nextArrays );
      lastArrays = nextArrays;
    }
      
    return permutations;
  }
  
  // for [1],[2],[3],[4], end = 4
  // returns [1,2],[1,3],[1,4],[2,3],[2,4],[3,4]
  public static List<int[]> getArrays( List<int[]> inputs, int end ) {
    ArrayList<int[]> arrays = new ArrayList<int[]>( );
    for (int[] stArr : inputs)
    {
      if (stArr[stArr.length-1] < end )
      {
        List<int[]> newArrays = getArrays( stArr, end );
        arrays.addAll( newArrays );
      }
    }
      
    return arrays;
  }
    
  // for arr=[1] and end = 4
  // returns [1,2],[1,3],[1,4]
  public static List<int[]> getArrays( int[] arr, int end )
  {
    ArrayList<int[]> arrays = new ArrayList<int[]>( );
    int last = arr[ arr.length - 1 ];

    for (int lastVal = last + 1; lastVal <= end; lastVal++ )
    {
      int[] newArray = new int[ arr.length + 1 ];
      System.arraycopy( arr, 0, newArray, 0, arr.length );
      newArray[ arr.length ] = lastVal;
      arrays.add( newArray );
    }
      
    return arrays;
  }

  // for 1,4 returns [1],[2],[3],[4]
  public static List<int[]> getArrays( int start, int end )
  {
    ArrayList<int[]> arrays = new ArrayList<int[]>( );
    for (int i = start; i <= end; i++ )
    {
      int[] arr = new int[1];
      arr[0] = i;
      arrays.add( arr );
    }
      
    return arrays;
  }
    
  // for 1,4 returns [1,2,3,4]
  public static int[] getArray( int start, int end ) {
    int[] array = new int[ (end - start) + 1 ];
    for (int i = 0; i <= (end - start); i++) {
      array[i] = start + i;
    }
    return array;
  }
}