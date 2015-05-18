package com.modinfodesigns.test.app.persistence.database;

import com.modinfodesigns.app.persistence.database.CommonsDataSourceFactory;

import com.modinfodesigns.app.persistence.database.SQLMethods;

import com.modinfodesigns.property.DataObjectTemplateManager;
import com.modinfodesigns.property.IProperty;

public class TestSQLTemplateManager
{

	public static void main(String[] args)
	{
        CommonsDataSourceFactory cdsf = new CommonsDataSourceFactory( );
        cdsf.setDriverClass( "[ oracle driver ]" );
        cdsf.setUsername( "SYSTEM" );
        cdsf.setPassword( "Fann13man" );
        cdsf.setUrl( " oracle url " );
        
        // Create some SQLTemplates and a SQLTemplateManager
        
	}

}
