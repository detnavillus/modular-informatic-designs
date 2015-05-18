package com.modinfodesigns.property.quantity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class QuantityFactors
{
    private HashMap<String,Integer> types = new HashMap<String,Integer>( );
    
    public QuantityFactors(  ) {  }

    public QuantityFactors( IQuantity quan, int count )
    {
        addFactors( quan, count );
    }

    public QuantityFactors( String quantityType, int count )
    {
        types.put( quantityType, new Integer( count ) );
    }

    public QuantityFactors( QuantityFactors another )
    {
        types.putAll( another.types );
    }


    public void invert(  )
    {
        HashMap<String,Integer> newTypes = new HashMap<String,Integer>( );
        for (Iterator<String> it = types.keySet().iterator(); it.hasNext(); )
        {
            String type = it.next( );
            Integer count = (Integer)types.get( type );
            Integer newCount = new Integer( -count.intValue() );
            newTypes.put( type, newCount );
        }
        
        this.types = newTypes;
    }


    public int size( )
    {
        return types.size( );
    }

    public int getPower( String type )
    {
        Integer count = (Integer)types.get( type );
        if (count == null) return 0;
        return count.intValue( );
    }

    public String[] getQuantityTypes( boolean includeScalar )
    {
        ArrayList<String> arL = new ArrayList<String>( );
        for ( Iterator<String> it = types.keySet().iterator(); it.hasNext(); )
        {
            String qType = it.next( );
            if (includeScalar || !isScalar( qType ))
               arL.add( qType );
        }
        
        String[] qTypes = new String[ arL.size() ];
        arL.toArray( qTypes );

        return qTypes;
    }

    public boolean isScalar( String type )
    {
        return (type.equals( "com.modinfodesigns.property.quantity.ScalarQuantity" ) ||
                type.equals( "com.modinfodesigns.property.quantity.PercentQuantity" ));
    }

    public void addFactor( String quantityType )
    {
        addFactor( quantityType, 1 );
    }

    public void addFactor( String quantityType, int nTimes )
    {
        if (quantityType == null || quantityType.equals("") ||
            quantityType.equals( "com.modinfodesigns.property.quantity.ScalarQuantity" )) return;

        Integer count = (Integer)types.get( quantityType );
        if (count != null)
        {
            Integer newCount = new Integer( count.intValue() + nTimes );
            types.put( quantityType, newCount );
        }
        else
        {
            types.put( quantityType, new Integer( nTimes ) );
        }
    }

    public void addFactors( IQuantity another )
    {
        addFactors( another, 1 );
    }

    public void addFactors( IQuantity another, int count )
    {
        if (count == 0) return;

        if (another instanceof RatioQuantity)
        {
            addFactors( ((RatioQuantity)another).getNumerator( ), count );
            subtractFactors( ((RatioQuantity)another).getDenominator( ), count );
        }

        addFactors( getFactors( another ), count );
    }


    public QuantityFactors getFactors( IQuantity another )
    {
    	return null;
    }
    
    // adding factors is like multiplying
    public void addFactors( QuantityFactors another )
    {
        addFactors( another, 1 );
    }
    public void addFactors( QuantityFactors another, int nTimes )
    {
        if (nTimes == 0) return;
        for ( Iterator<String> it = another.types.keySet().iterator(); it.hasNext(); )
        {
            String type = it.next( );
            Integer count = (Integer)another.types.get( type );
            addFactors( type, (count.intValue( ) * nTimes) );
        }
    }

    public void addFactors( String type, int count )
    {
        if (count == 0 || type.equals("com.modinfodesigns.property.quantity.ScalarQuantity")) return;
        Integer myCount = (Integer)this.types.get( type );
        if (myCount == null)
        {
            this.types.put( type, new Integer(count) );
        }
        else
        {
             Integer newCount = new Integer( myCount.intValue() + count );
             if (newCount.intValue() == 0) types.remove( type );
             else this.types.put( type, newCount );
        }
    }


    public void subtractFactors( IQuantity another )
    {
        subtractFactors( another, 1 );
    }

    public void subtractFactors( IQuantity another, int count )
    {
        if (count == 0) return;

        if (another instanceof RatioQuantity)
        {
            subtractFactors( ((RatioQuantity)another).getNumerator( ), count );
            addFactors( ((RatioQuantity)another).getDenominator( ), count );
        }

        subtractFactors( getFactors( another ), count );
    }


    public void subtractFactors( QuantityFactors another )
    {
        subtractFactors( another, 1 );
    }

    public void subtractFactors( QuantityFactors another, int nTimes )
    {
        if (nTimes == 0) return;
        for ( Iterator<String> it = another.types.keySet().iterator(); it.hasNext(); )
        {
            String type = it.next( );
            Integer count = (Integer)another.types.get( type );
            subtractFactors( type, (count.intValue() * nTimes ) );
        }
    }

    public void subtractFactors( String type, int count )
    {
        if (count == 0 || type.equals( "com.modinfodesigns.property.quantity.ScalarQuantity" )) return;
        Integer myCount = (Integer)this.types.get( type );
        if (myCount != null)
        {
             Integer newCount = new Integer( myCount.intValue() - count );
             if (newCount.intValue() == 0) types.remove( type );
             else this.types.put( type, newCount );
        }
        else
        {
             Integer newCount = new Integer( -count );

             this.types.put( type, newCount );
        }
    }


    public boolean matches( IQuantity quantity )
    {
        if (quantity instanceof ScalarQuantity)
        {
            return (types.size() == 0);
        }

        return (quantity != null && matches( getFactors( quantity ) ));
    }

    public boolean equals( Object another )
    {
        if (!(another instanceof QuantityFactors)) return false;
        return matches( (QuantityFactors)another );
    }


    public int hashCode(  )
    {
        return types.hashCode( );
    }


    // --------------------------------------------------------------------------------
    // return true if another factor has all of the types in this factor
    // with the same counts.
    // --------------------------------------------------------------------------------
    public boolean matches( QuantityFactors another )
    {
        if (another == null || another.types.size() != types.size()) return false;
        for ( Iterator<String> it = this.types.keySet().iterator(); it.hasNext(); )
        {
             String type = it.next( );
             Integer itsCount = (Integer)another.types.get( type );
             if (itsCount == null) return false;   // does not have this type so NO
             Integer myCount = (Integer)types.get( type );
             if (itsCount.intValue() != myCount.intValue()) return false;   // different number
        }

        return true;
    }

    // return true if all of another's factors are in this factor
    public boolean includes( QuantityFactors another )
    {
        for (Iterator<String> it = another.types.keySet().iterator(); it.hasNext(); )
        {
             String type = it.next( );
	         if (!(type.equals("com.modinfodesigns.property.quantity.ScalarQuantity")))
             {
                 Integer itsCount = (Integer)another.types.get( type );
                 Integer myCount = (Integer)types.get( type );
                 if (myCount == null) return false;

                 // have to change this..
                 if (myCount.intValue() > 0 && itsCount.intValue() > myCount.intValue()) return false;
                 if (myCount.intValue() < 0 && itsCount.intValue() < myCount.intValue()) return false;  // different number
            }
        }
        
        return true;
    }


    public IQuantity getQuantity(  )
    {
        // go through types. multiply together types with positive counts...
        // if any have negative counts - return a RatioQuantity...

        IQuantity theQuan = null;
        for (Iterator<String> it = types.keySet().iterator(); it.hasNext(); )
        {
             String type = it.next( );
             Integer count = (Integer)types.get( type );
	         int qCount = count.intValue( );

             IQuantity nQuan = createQuantity( type );
             if (nQuan == null) continue;

             if (qCount > 1) nQuan = new PowerQuantity( "", nQuan, qCount );
             else if (qCount < 0)
             {
                 int absCnt = -qCount;
                 if (absCnt > 1) nQuan = new PowerQuantity( "", nQuan, absCnt );
                 nQuan = new RatioQuantity( new ScalarQuantity("", 1.0), nQuan );
             }

             if (theQuan == null) theQuan = nQuan;
             else
             {
                 theQuan = theQuan.multiply( nQuan );
             }
        }
        
        return theQuan;
    }

 

    public String toString(  )
    {
        if (types == null || types.size() == 0)
        {
            return "com.modinfodesigns.property.quantity.ScalarQuantity";
        }

        StringBuffer strbuf = new StringBuffer( );
        for ( Iterator<String> it = types.keySet().iterator(); it.hasNext(); )
        {
             String type = it.next( );
             strbuf.append( type );
             Integer itsCount = (Integer)types.get( type );
             if (itsCount.intValue() != 1 )
             {
                strbuf.append( " " );
                strbuf.append( itsCount );
             }
             
             if (it.hasNext( ))
                 strbuf.append( " * " );
        }
        
        return strbuf.toString( );
    }


    private IQuantity createQuantity( String fromUnits )
    {
        try
        {
            return (IQuantity)Class.forName( fromUnits ).newInstance( );
        }
        catch (Exception e ) {   }

        if ((fromUnits.indexOf("/") > 0) || (fromUnits.indexOf(" per ") > 0))
            return RatioQuantity.createRatioQuantity( fromUnits );

        else if ((fromUnits.indexOf("*") > 0))
            return ProductQuantity.createProductQuantity( fromUnits );

        try
        {
            for (int i = 0; i < quanClasses.length; i++)
            {
                IQuantity q = (IQuantity)Class.forName( quanClasses[i] ).newInstance( );

                if ( fromUnits.equals( quanClasses[i] ) || (hasFormat( q, fromUnits )))
                {
                    return q;
                }
            }
        }
        catch( Exception e )
        {

        }
        
        return null;
    }
    
    private boolean hasFormat( IQuantity quan, String unit )
    {
    	if (quan == null) return false;
    	
    	String[] itsUnits = quan.getUnits();
    	if (itsUnits == null) return false;
    	for (int i = 0; i < itsUnits.length; i++)
    	{
    		if (itsUnits[i].equals( unit )) return true;
    	}
    	
    	return false;
    }

    private static String[] quanClasses = {
	    "com.modinfodesigns.property.quantity.Area",
        "com.modinfodesigns.property.quantity.Count",
        "com.modinfodesigns.property.quantity.Currency",
        "com.modinfodesigns.property.quantity.Distance",
        "com.modinfodesigns.property.quantity.DistanceVector",
        "com.modinfodesigns.property.quantity.Duration",
        "com.modinfodesigns.property.quantity.Speed",
        "com.modinfodesigns.property.quantity.TwoDAngle",
        "com.modinfodesigns.property.quantity.Volume",
        "com.modinfodesigns.property.quantity.Weight"
    };
}
