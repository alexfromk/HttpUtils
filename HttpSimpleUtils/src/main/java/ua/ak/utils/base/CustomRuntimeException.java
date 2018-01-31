package ua.ak.utils.base;

/***********************************************************************
 * Module: CustomRuntimeException.java 
 * Author: Alex 
 * Purpose: Custom RuntimeException 
 ***********************************************************************/

public class CustomRuntimeException extends RuntimeException
{		
	private static final long serialVersionUID = 7898060696417447766L;
	
	String extMess = "";

	/**
	 * Extended constructor
	 * 
	 * @param mess - message
	 * @param parentException - parent Exception, can be null
	 **/
	public CustomRuntimeException(String mess, Exception parentException)
	{
		this(mess);

		if (parentException != null)
		{
			if (!(parentException instanceof CustomRuntimeException))
			{
				this.extMess += " Parent Exception: ";
				this.extMess += parentException.getClass().getName();
			}
			if (parentException.getMessage() != null && parentException.getMessage().length() > 0)
				this.extMess += " (parent message: "
						+ parentException.getMessage() 
						+ (parentException.getCause()!=null?" cause: "+parentException.getCause().getClass().getName()+" - "+parentException.getCause().getMessage():"")
						+ ")";
			this.setStackTrace(parentException.getStackTrace());
		}
	}

	/**
	 * Main constructor
	 * 
	 * @param mess  - message
	 */
	public CustomRuntimeException(String mess)
	{
		super(mess);
	}

	@Override
	public String getMessage()
	{
		String result = super.getMessage() + ((getMessageExt().length() > 0)? " - " + getMessageExt() : "");
		return result;
	}

	String getMessageExt()
	{
		return extMess==null?"":extMess;
	}

}

