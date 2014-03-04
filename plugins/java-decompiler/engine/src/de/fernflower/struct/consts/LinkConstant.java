/*
 *    Fernflower - The Analytical Java Decompiler
 *    http://www.reversed-java.com
 *
 *    (C) 2008 - 2010, Stiver
 *
 *    This software is NEITHER public domain NOR free software 
 *    as per GNU License. See license.txt for more details.
 *
 *    This software is distributed WITHOUT ANY WARRANTY; without 
 *    even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 *    A PARTICULAR PURPOSE. 
 */

package de.fernflower.struct.consts;

import java.io.DataOutputStream;
import java.io.IOException;

/*
 *   NameAndType, FieldRef, MethodRef, InterfaceMethodref
 *   InvokeDynamic, MethodHandle
 */

public class LinkConstant extends PooledConstant {

	// *****************************************************************************
	// public fields
	// *****************************************************************************
	
	public int index1, index2;
	
	public String classname;
	
	public String elementname;

	public String descriptor;
	
	public int paramCount = 0;
	
	public boolean isVoid = false;;
	
	public boolean returnCategory2 = false;
	
	 	
	// *****************************************************************************
	// constructors
	// *****************************************************************************

	public LinkConstant(int type, String classname, String elementname, String descriptor) {
		this.type = type;
		this.classname = classname;
		this.elementname = elementname;
		this.descriptor = descriptor;
		
		initConstant();
	}	
	
	public LinkConstant(int type, int index1, int index2) {
		this.type = type;
		this.index1 = index1;
		this.index2 = index2;
	}	
	

	// *****************************************************************************
	// public methods
	// *****************************************************************************
	
	public void resolveConstant(ConstantPool pool) {

		if(type == CONSTANT_NameAndType) {
			elementname = pool.getPrimitiveConstant(index1).getString();
			descriptor = pool.getPrimitiveConstant(index2).getString();
		} else if(type == CONSTANT_MethodHandle) {
			LinkConstant ref_info = pool.getLinkConstant(index2);
			
			classname = ref_info.classname;
			elementname = ref_info.elementname;
			descriptor = ref_info.descriptor;
			
		} else {
			if(type != CONSTANT_InvokeDynamic) {
				classname = pool.getPrimitiveConstant(index1).getString();
			}
			
			LinkConstant nametype = pool.getLinkConstant(index2);
			elementname = nametype.elementname;
			descriptor = nametype.descriptor;
		}
		
		initConstant();
	}

	public void writeToStream(DataOutputStream out) throws IOException {
		out.writeByte(type);
		if(type == CONSTANT_MethodHandle) {
			out.writeByte(index1);
		} else {
			out.writeShort(index1);
		}
		out.writeShort(index2);
	}
	
	
	public boolean equals(Object obj) {
		
		if(obj == null || !(obj instanceof LinkConstant)) {
			return false;
		}
		
		LinkConstant cn = (LinkConstant)obj;
		
		if(this.type == cn.type && 
				this.elementname.equals(cn.elementname) && 
				this.descriptor.equals(cn.descriptor)) {
			
			if(this.type == CONSTANT_NameAndType) {
				return this.classname.equals(cn.classname);  
			} else {
				return true;
			}
		}
		
		return false;
	}
	
	// *****************************************************************************
	// private methods
	// *****************************************************************************
	
	private void initConstant() {
		
		if(type == CONSTANT_Methodref || type == CONSTANT_InterfaceMethodref || type == CONSTANT_InvokeDynamic || type == CONSTANT_MethodHandle) {
			resolveDescriptor(descriptor);
		} else if(type == CONSTANT_Fieldref) {
			returnCategory2 = ("D".equals(descriptor) || "J".equals(descriptor));
		}
		
	}
	
	private void resolveDescriptor(String descr){
		
		String[] arr = descr.split("[()]"); 
		String par = arr[1];
		
		int index = 0, counter = 0;
		int len = par.length(); 
		
		while(index<len) {

			char c = par.charAt(index);
			if(c == 'L') {
				index = par.indexOf(";", index);
			} else if (c == '[') {
				index++;
				continue;
			}
			
			counter++; 
			index++;
		}
		
		paramCount = counter;
		isVoid = "V".equals(arr[2]);
		returnCategory2 = ("D".equals(arr[2]) || "J".equals(arr[2]));
	}
	
}

