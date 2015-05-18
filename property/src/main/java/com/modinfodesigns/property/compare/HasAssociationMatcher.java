package com.modinfodesigns.property.compare;

import com.modinfodesigns.property.IAssociation;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.security.IUserCredentials;

import java.util.List;

/**
 * Matches an IPropertyHolder if it has an Association with an object that
 * matches the criteria specified by a delegate IPropertyHolderMatcher. (can do
 * nested Association matching if this delegate is also a HasAssociationMatcher).
 * 
 * @author Ted Sullivan
 */

public class HasAssociationMatcher implements IPropertyHolderMatcher
{
  private String associationName;
	
  private IPropertyHolderMatcher targetMatcher;
	
  public void setAssociationName( String associationName )
  {
    this.associationName = associationName;
  }
	
  public void setTargetMatcher( IPropertyHolderMatcher targetMatcher )
  {
    this.targetMatcher = targetMatcher;
  }

  @Override
  public boolean equals(IUserCredentials user, IProperty property)
  {
    if (property instanceof IPropertyHolder)
    {
      IPropertyHolder pHolder = (IPropertyHolder)property;
      return equals( user, pHolder );
    }
    return false;
  }

  @Override
  public boolean equals(IUserCredentials user, IPropertyHolder propHolder)
  {
    List<IAssociation> assocs = propHolder.getAssociations( associationName );
    if (assocs == null || assocs.size() == 0) return false;
		
    if (targetMatcher == null) return true;
		
    for( int i = 0; i < assocs.size(); i++)
    {
      IAssociation assoc = assocs.get( i );
      IPropertyHolder target = assoc.getAssociationTarget( );
      if (targetMatcher.equals( user, target ))
      {
        return true;
      }
    }
		
    return false;
  }

}
