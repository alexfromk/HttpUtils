package ua.ak.utils.model;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;


/** BaseItemView.java - [Out param -> for View]
 * универсальная модель для элементов списка 
 * @author KushnirY
 *
 */
public class BaseItemView implements Cloneable, Serializable
{
	private static final long serialVersionUID = 5815705090905396453L;
	String id="";
	Object document=null;
		
	private String valS0 = "";
	private String valS1 = "";
	private String valS2 = "";
	private String valS3 = "";
	private String valS4 = "";
	private Date valD = null;
	private Date valD1 = null;
	private Date valD2 = null;
	private long valL = 0;
	private long valL2 = 0;
	private int vali = 0;
	private int vali2 = 0;
	private double valF = 0;
	private double valF2 = 0;
	
	//позиция в таблице
	long rowNumber=0;

	public void setUnits(int vali2)
	{
		this.vali2=(vali2>0)?vali2:0;
	}

	
	public int getUnits()
	{
		return vali2;
	}
	
	
	
	/**
	 * @return the valF2
	 */
	public double getValF2()
	{
		return valF2;
	}


	/**
	 * @param valF2 the valF2 to set
	 */
	public void setValF2(double valF2)
	{
		this.valF2 = valF2;
	}


	public Date getValD1()
	{
		return valD1;
	}

	public void setValD1(Date valD1)
	{
		this.valD1 = valD1;
	}

	public String getValS4()
	{
		return valS4;
	}

	public void setValS4(String valS4)
	{
		this.valS4 = valS4;
	}

	public long getValL2()
	{
		return valL2;
	}

	public void setValL2(long valL2)
	{
		this.valL2 = valL2;
	}

	public int getVali2()
	{
		return vali2;
	}

	public void setVali2(int vali2)
	{
		this.vali2 = vali2;
	}

	public Object getDocument()
	{
		return document;
	}
	
	public void setDocument(Object document)
	{
		this.document = document;
	}
	
	public String getId()
	{
		return id.trim();
	}
	public void setId(String id)
	{
		this.id = id;
	}

	public String getValS0()
	{
		return (valS0==null)?"":valS0.trim();
	}
	public void setValS0(String valS0)
	{
		this.valS0 = valS0;
	}
	public String getValS1()
	{
		return valS1;
	}
	public void setValS1(String valS1)
	{
		this.valS1 = valS1;
	}
	public String getValS2()
	{
		return valS2;
	}
	public void setValS2(String valS2)
	{
		this.valS2 = valS2;
	}
	public String getValS3()
	{
		return valS3;
	}
	public void setValS3(String valS3)
	{
		this.valS3 = valS3;
	}
	public Date getValD()
	{
		return valD;
	}
	public void setValD(Date valD)
	{
		this.valD = valD;
	}
	public long getValL()
	{
		return valL;
	}
	public void setValL(long valL)
	{
		this.valL = valL;
	}
	public int getVali()
	{
		return vali;
	}
	public void setVali(int vali)
	{
		this.vali = vali;
	}
	public double getValF()
	{
		return valF;
	}
	public void setValF(double valF)
	{
		this.valF = valF;
	}
	public long getRowNumber()
	{
		return rowNumber;
	}
	
	public void setRowNumber(long rowNumber)
	{
		this.rowNumber = rowNumber;
	}

	public Date getValD2()
	{
		return valD2;
	}

	public void setValD2(Date valD2)
	{
		this.valD2 = valD2;
	}
	public String getRefKey()
	{
		if(this.getId()==null || this.getId().length()==0) return String.valueOf(this.getValL());
		return this.getId();
	}
	
	public String getRefValue()
	{
		return this.getValS0();
	}	
	
	public String getStructCode()
	{
		return id;
	}	
	
	public String getStructValue()
	{
		return valS0;
	}
	
	public String getStructRef()
	{
		return valS1;
	}
	public int getStructClass()
	{
		return vali2;
	}
	
	public int getStructLevel()
	{
		return id.split("#").length;
	}

	public boolean isStructChields()
	{
		return vali>0?true:false;
	}

	public long getTreeChieldRows()
	{
		return valL; 
	}
	
		
	public String getTreeCode()
	{
		return id.trim(); 
	}

	public String getTreeCodeURLEncode(String enc) throws UnsupportedEncodingException
	{
		return URLEncoder.encode(id.replaceAll("''","'"), enc); 
	}
	
	public String getTreeCodeAlias(String enc) throws UnsupportedEncodingException
	{
		return URLEncoder.encode(id.replaceAll("''","'").replaceAll("&","!!!"), enc); 
	}	
	
	public String getTreeFullValue()
	{
		return valS0; 
	}
	
	public String getTreeShortValue()
	{
		if(valS0==null) return "";
	
		String hr="/";
		if((id.length()-1)==id.lastIndexOf(".") && id.indexOf("/")==-1) hr="\\";
		String out=valS0.lastIndexOf(hr)==-1?valS0:valS0.substring(valS0.lastIndexOf(hr)+1);
		
		return out;
	}

	public String getTreeParentValue(){
		return (this.valS0!=null && valS0.lastIndexOf("/")!=-1)?valS0.substring(0,valS0.lastIndexOf("/")):this.valS0;
	}
	
	public String getTreeChildValue(){
		return (this.valS0!=null && valS0.lastIndexOf("/")!=-1)?valS0.substring(valS0.lastIndexOf("/")+1):this.valS0;
	}
	
	public int getTreeLevel()
	{
		return vali; 
	}
	
	public long getTreeCountDocuments()
	{
		return this.valL2; 
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BaseItemView other = (BaseItemView)obj;
		if (id == null)
		{
			if (other.id != null)
				return false;
		}
		else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
}
