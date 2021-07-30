package com.insta.hms.diagnosticmodule.prescribetest;

public class DiagnoFactory {
  private DiagnoFactory() {
    
  }
	private static DiagnoBOIF bo=null;
	private static DiagnoDAOIF dao=null;

	public static DiagnoBOIF getDiagnoBO(){
		if(bo==null){
			bo=new DiagnoBOImpl();
		}
		return bo;
	}

	public static DiagnoDAOIF getDiagnoDAO(){
		if(dao==null){
			dao=new DiagnoDAOImpl();
		}
		return dao;
	}

}
