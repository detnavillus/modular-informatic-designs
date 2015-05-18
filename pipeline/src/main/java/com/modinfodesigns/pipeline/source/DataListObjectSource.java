package com.modinfodesigns.pipeline.source;

import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.security.IUserCredentials;

public class DataListObjectSource extends BaseDataObjectSource
{
  private IDataList dataList;
    
  public DataListObjectSource( ) {  }
    
  public DataListObjectSource( IDataList dataList )
  {
    this.dataList = dataList;
  }
    
  public void setDataList( IDataList dataList )
  {
    this.dataList = dataList;
  }
    
    
  @Override
  public void run()
  {
    doProcessDataList( dataList );
  }

  @Override
  public void run( IUserCredentials withUser )
  {
    doProcessDataList( dataList );
  }

}
