package cn.ncuhome.model;

import java.io.Serializable;

public class Contact implements Serializable {
	private static final long serialVersionUID = 1001L;
	private String Dep_ID;
	private String Dep_Name;
	private String Emp_ID;
	private String Emp_Name;
	private String Emp_Cellphone;

	public Contact() {
	}

	public String getDep_ID() {
		return Dep_ID;
	}

	public void setDep_ID(String dep_ID) {
		Dep_ID = dep_ID;
	}

	public String getDep_Name() {
		return Dep_Name;
	}

	public void setDep_Name(String dep_Name) {
		Dep_Name = dep_Name;
	}

	public String getEmp_ID() {
		return Emp_ID;
	}

	public void setEmp_ID(String emp_ID) {
		Emp_ID = emp_ID;
	}

	public String getEmp_Name() {
		return Emp_Name;
	}

	public void setEmp_Name(String emp_Name) {
		Emp_Name = emp_Name;
	}

	public String getEmp_Cellphone() {
		return Emp_Cellphone;
	}

	public void setEmp_Cellphone(String emp_Cellphone) {
		Emp_Cellphone = emp_Cellphone;
	}

	@Override
	public String toString() {
		return "Contact [" + (Dep_ID != null ? "Dep_ID=" + Dep_ID + ", " : "") + (Dep_Name != null ? "Dep_Name=" + Dep_Name + ", " : "") + (Emp_ID != null ? "Emp_ID=" + Emp_ID + ", " : "") + (Emp_Name != null ? "Emp_Name=" + Emp_Name + ", " : "") + (Emp_Cellphone != null ? "Emp_Cellphone=" + Emp_Cellphone : "") + "]";
	}
}
