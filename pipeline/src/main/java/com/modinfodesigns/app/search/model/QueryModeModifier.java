package com.modinfodesigns.app.search.model;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.string.StringProperty;

/**
 * Represents a modifier parameter for a Query Mode. For example the proximity
 * operators may specify a term distance within which two or more terms are 
 * considered to be 'near' each other.
 * 
 * @author Ted Sullivan
 */

public class QueryModeModifier extends QueryFieldDefinition
{
	public static final String MODIFIER_TYPE = "ModifierType";
	public static final String DEFAULT_VALUE = "DefaultValue";
	
    public void setModifierType( String modifierType )
    {
		doSetProperty( new StringProperty( MODIFIER_TYPE, modifierType ) );
    }
    
    public String getModifierType(  )
    {
		IProperty modProp = getProperty( MODIFIER_TYPE );
		return (modProp != null) ? modProp.getValue( ) : null;
    }
    
    public void setDefaultValue( String defaultValue )
    {
    	doSetProperty( new StringProperty( DEFAULT_VALUE, defaultValue ));
    }
    
    public String getDefaultValue(  )
    {
		IProperty defValProp = getProperty( DEFAULT_VALUE );
		return (defValProp != null) ? defValProp.getValue( ) : null;
    }
}
