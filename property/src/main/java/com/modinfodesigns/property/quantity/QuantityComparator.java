package com.modinfodesigns.property.quantity;

import java.util.Comparator;

public class QuantityComparator implements Comparator<IQuantity>
{
	private boolean ascending = true;
	
	public void setAscending( boolean ascending )
	{
		this.ascending = ascending;
	}

	@Override
	public int compare(IQuantity quant0, IQuantity quant1)
	{
		if (quant0 == null || quant1 == null) return 0;
		
		if (quant0.getQuantity() == quant1.getQuantity() ) return 0;
		
		int diff = (ascending) ? ((quant0.getQuantity() > quant1.getQuantity()) ? 1 : -1)
				               : ((quant1.getQuantity() > quant0.getQuantity()) ? 1 : -1);
		
		return diff;
	}

}
