package ua.ak.utils.base;

/***********************************************************************
 * Module: CustomRuntimeException.java 
 * message 2
 * Author: Alex 
 * Purpose: Генератор исключений CustomRuntimeException
 ***********************************************************************/

public class CustomRuntimeException extends RuntimeException
{		
	private static final long serialVersionUID = 7898060696417447766L;
	
	String extMess = "";

	/**
	 * Расширенный конструктор для создания ошибки
	 * 
	 * @param mess - сообщение
	 * @param parentException - исключение родителя
	 **/
	public CustomRuntimeException(String mess, Exception parentException)
	{
		this(mess);

		if (parentException != null)
		{
			if (!(parentException instanceof CustomRuntimeException))
			{
				this.extMess += " parent Exception: "
						+ parentException.getClass().getName();
			}
			if (parentException.getMessage() != null
					&& parentException.getMessage().length() > 0)
				this.extMess += " (Parent message: "
						+ parentException.getMessage() 
						+ (parentException.getCause()!=null?" cause: "+parentException.getCause().getClass().getName()+" - "+parentException.getCause().getMessage():"")
						+ ")";
			this.setStackTrace(parentException.getStackTrace());
		}
	}

	/**
	 * Основной конструктор для создания ошибки
	 * 
	 * @param mess  - сообщение
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

