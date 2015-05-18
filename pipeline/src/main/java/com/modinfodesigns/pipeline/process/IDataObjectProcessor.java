package com.modinfodesigns.pipeline.process;

import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.IDataList;

public interface IDataObjectProcessor
{
  public IDataList processDataList( IDataList data );
    
  public void processComplete( IPropertyHolder result, boolean status );
}
