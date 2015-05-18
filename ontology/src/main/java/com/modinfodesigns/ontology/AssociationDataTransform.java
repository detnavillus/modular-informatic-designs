package com.modinfodesigns.ontology;

import com.modinfodesigns.property.Association;
import com.modinfodesigns.property.AssociationException;
import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.transform.IPropertyHolderTransform;
import com.modinfodesigns.property.transform.IPropertyTransformListener;
import com.modinfodesigns.property.transform.PropertyTransformException;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * Adds association links between the input property holder and a set of 
 * Data Objects.
 * 
 * @author Ted Sullivan
 */

public class AssociationDataTransform implements IPropertyHolderTransform
{
  private String targetAssociation;
  private boolean multipleTargetAssociations = true;
    
  private String sourceAssociation;
  private boolean multipleSourceAssociations = true;
    
  private List<IPropertyHolder> targetList;
    
  public void setTargetAssociation( String targetAssociation )
  {
    this.targetAssociation = targetAssociation;
  }
    
  public void setMultipleTargetAssociations( String multiTargetAssociations )
  {
    this.multipleTargetAssociations = (multiTargetAssociations != null && multiTargetAssociations.equalsIgnoreCase( "true" ));
  }
    
  public void setSourceAssociation( String sourceAssociation )
  {
    this.sourceAssociation = sourceAssociation;
  }

  public void setMultipleSourceAssociations( String multiSourceAssociations )
  {
    this.multipleSourceAssociations = (multiSourceAssociations != null && multiSourceAssociations.equalsIgnoreCase( "true" ));
  }
    
  public void setTargetList( DataList dataList )
  {
    this.targetList = new ArrayList<IPropertyHolder>( );
    for (Iterator<DataObject> dit = dataList.getData(); dit.hasNext(); )
    {
      targetList.add( dit.next() );
    }
    	
  }

  @Override
  public IPropertyHolder transformPropertyHolder(IPropertyHolder input)
        throws PropertyTransformException
  {
    // for each target, create an Association, call addAssociation on the input
    // if sourceAssociation is not null, create an Association with source as target
    // and call addAssociation on the target.
		
    try
    {
      if (targetList != null)
      {
        Association sourceAssoc = null;
        if (sourceAssociation != null)
        {
          sourceAssoc = new Association( sourceAssociation, input.getID( ), input, multipleSourceAssociations );
        }
		    	
        for (int i = 0; i < targetList.size(); i++ )
        {
          IPropertyHolder target = targetList.get( i );
          Association targetAssoc = new Association( targetAssociation, target.getID( ), target, multipleTargetAssociations );
          input.addAssociation( targetAssoc );
            
          if (sourceAssoc != null)
          {
            target.addAssociation( sourceAssoc );
          }
        }
      }
    }
    catch ( AssociationException ae )
    {
      throw new PropertyTransformException( "Association Exception: " + ae.getMessage( ) );
    }
		
    return input;
  }
	
  @Override
  public IProperty transform(IProperty input) throws PropertyTransformException
  {
    return input;
  }

  @Override
  public void startTransform(IProperty input, IPropertyTransformListener transformListener)
        throws PropertyTransformException
  {

  }

}
