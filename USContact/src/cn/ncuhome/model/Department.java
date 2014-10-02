package cn.ncuhome.model;

import java.io.Serializable;

public class Department implements Serializable {
	private static final long serialVersionUID = 1002L;
	private String Dep_ID;
	private String Dep_Name;

	public Department() {
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

	@Override
	public String toString() {
		return "Department [" + (Dep_ID != null ? "Dep_ID=" + Dep_ID + ", " : "") + (Dep_Name != null ? "Dep_Name=" + Dep_Name : "") + "]";
	}
}
