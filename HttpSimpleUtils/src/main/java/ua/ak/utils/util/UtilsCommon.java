package ua.ak.utils.util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import ua.ak.utils.base.CustomRuntimeException;


/**
 * утилиты общего назначения
 * 
 * @author alexk
 *
 */
public class UtilsCommon
{
	public static String fetchCauseExceptionMessage(Exception e)
	{
		if (e != null && e.getCause() != null)
		{
			return e.getCause().getClass() + " - " + e.getCause().getMessage();
		}
		return "";
	}

	/**
	 * Выдать сообщение об ошибке+сообщение причины
	 * 
	 * @param e
	 * @return
	 */
	public static String fetchExceptionMessageWithCause(Exception e)
	{
		String mainMessage = (e != null && e.getMessage() != null) ? e.getMessage() : "";
		String causeMessage = fetchCauseExceptionMessage(e);
		return mainMessage + ((mainMessage.length() > 0 && causeMessage.length() > 0) ? " " : "") + causeMessage;
	}

	/**
	 * Создание ExceptionLZCommon по др. исключению
	 * 
	 * @param e
	 * @param codeExceptionLZCommon
	 *            - код нового Exception
	 * @param methodTitle
	 *            - название сервиса/метода генерирующего ExceptionLZCommon
	 * @return ExceptionLZCommon
	 */
	public static CustomRuntimeException generateExceptionLZCommon(Exception e, int codeExceptionLZCommon, String methodTitle)
	{
		CustomRuntimeException excE3 = null;
		if (e instanceof CustomRuntimeException)
			excE3 = (CustomRuntimeException) e;
		else
		{
			excE3 = new CustomRuntimeException(methodTitle + " : " + e.getClass().getName() + " - " + e.getMessage(), e);
			if (e.getCause() != null)
				excE3.initCause(e.getCause());
		}
		return excE3;
	}

	/**
	 * Подчитать проперти из файла
	 * 
	 * @param fileName
	 *            Файл пропертей
	 * @return
	 * @throws ExceptionLZCommon
	 */
	public static Properties readProperties(String fileName) throws CustomRuntimeException
	{
		Properties properties = new Properties();
		try
		{
			properties.load(UtilsCommon.class.getClassLoader().getResourceAsStream(fileName));
		}
		catch (Exception e)
		{
			throw new CustomRuntimeException( "Ошибка чтения пропертийного файла [" + fileName + "]", e);
		}
		return properties;
	}

	/**
	 * Конвертит джавовский timestamp к формату даты майкрософт (с плавающей точкой)
	 * 
	 * @param timestamp Дата в целочисленном формате 
	 * @return
	 */
	public static Double convertDateToMicrosoftTimestamp(Long timestamp)
	{
		if (timestamp == null)
			return 0.0;
		final double DAY_MILLIS = 1000.0 * 24.0 * 60.0 * 60.0;
		Calendar cal = Calendar.getInstance();
		cal.set(1899, 11, 30, 1, 59, 59);

		Long msStartDate = cal.getTimeInMillis();

		return (timestamp - msStartDate) / DAY_MILLIS;
	}

	/**
	 * Конвертит дату майкрософт (с плавающей точкой) в джавовский timestamp
	 * 
	 * @param microsoftTimestamp Дата в формате с плавающей точкой
	 * @return
	 */
	public static Long convertMicrosoftTimestampToDate(Double microsoftTimestamp)
	{
		if (microsoftTimestamp == null)
			return 0L;
		final double DAY_MILLIS = 1000.0 * 24.0 * 60.0 * 60.0;
		Calendar cal = Calendar.getInstance();
		cal.set(1899, 11, 30, 1, 59, 59);
		Long startDate = cal.getTimeInMillis();

		Long miliseconds = new Double(microsoftTimestamp * DAY_MILLIS).longValue();

		return ((startDate + miliseconds) / 1000 + 1) * 1000;
	}

	/**
	 * Преобразовывает код ответственного по контрактам, к виду, как он хранится в Neo4J
	 * @param lbpResponsibleUnid
	 * @return
	 */
	public static String toNeo4jResponsibleUnid(String lbpResponsibleUnid)
	{
		if (StringUtils.isBlank(lbpResponsibleUnid))
			return lbpResponsibleUnid;
		return lbpResponsibleUnid.replace("0x", "");
	}
	
	/**
	 * Преобразовывает код ответственного по контрактам, к виду, как он хранится в ЛБП
	 * @param lbpResponsibleUnid
	 * @return
	 */
	public static String toLbpResponsibleUnid(String neo4jResponsibleUnid)
	{
		if (StringUtils.isNotBlank(neo4jResponsibleUnid) && !neo4jResponsibleUnid.startsWith("0x"))
			neo4jResponsibleUnid = "0x" + neo4jResponsibleUnid; 
		return neo4jResponsibleUnid;
	}
	
	/**
	 * Нормализация логина
	 * @param login - логина
	 * @return login - логина
	 */
	public static String fixLogin(String login)
	{
		if (StringUtils.isBlank(login))
		{
			return "";
		}
		return login.toLowerCase().trim();
	}
	
	public static Date getNow()
	{
		Calendar gc1=new GregorianCalendar();
		return gc1.getTime();
	}
	
	public static final int  strToInt(String s)
	{
		return strToInt(s,0);
	}

	/**
	 * strToInt
	 * @param s
	 * @param defaultVal
	 * @return
	 */
	public static final int  strToInt(String s,int defaultVal)
	{
		try
		{
			return Integer.parseInt(s);
		}
		catch (Exception ex)
		{
			return defaultVal;
		}
	}
	
	/**
	 * strToLong
	 * @param s
	 * @param defaultVal
	 * @return
	 */
	public static final long  strToLong(String s,long defaultVal)
	{
		try
		{
			return Long.parseLong(s);
		}
		catch (Exception ex)
		{
			return defaultVal;
		}

	}
	/**
	 * Получить часть строки
	 * @param s 
	 * @param regexp - рег. выражение для разделения частей строки
	 * @param num - номер части, с 0 
	 * @return
	 * всегда !=null
	 */
	public static final String getStrPart(String s,String regexp,int nom)
	{
		if(s==null || regexp==null || regexp.length()==0)
			return "";
		String[] arr=s.split(regexp);
		if(arr!=null && nom<arr.length)
			return arr[nom];
		else
			return "";
	}
	
	/**
	 * Флаги для конвертации strToDate()
	 */
	/** Есть составляющая - дата */
	public final static int fDATE=0x1;	
	/** Есть составляющая - время */
	public final static int fTIME=0x2;	
	/** дата yyyy.MM.dd иначе dd.MM.yyyy */
	public final static int fDATE_YYYY_MM_DD=0x4;	
	/** В случае неудачи возвращать дату по умолчанию, а не null */
	public final static int fNotNull=0x8;	

	/*Конвертация String-->Date
	 * Вход:
	 * sDate - Строковое представление даты
	 * до milliseconds
	 * например,
	 * 30.12.2005 15:55:33
	 * 30.12.2005 15:55:33.90 
	 * 2005.12.31 15:55:33
	 * 30.12.2005T15:55:33Z
	 * 30.12.2005T15:55:33.44Z
	 * 30.12.2005
	 * 15:55:33
	 * flags - формат вх.строки
	 * =0 - конвертируем как есть
	 * бит0=1 Есть составляющая - дата (fDATE)
	 * бит1=1 Есть составляющая - время (fTIME)
	 * бит2=1 дата yyyy.MM.dd иначе dd.MM.yyyy (fDATE_YYYY_MM_DD)
	 * бит3=1 В случае неудачи возвращать дату по умолчанию, а не null(fNotNull)
	 * 
	 * Выход: == Date
	 * 		==null(флаг fNotNull=0) нельзя конвертнуть в строку
	 *       == дата по умолчанию 1.2.1980 00:00:00 (флаг fNotNull!=0) 
	 * 
	 */
	public final static Date strToDate(String sDate,int flags)
	{
		String regexpDelimiterDate="[/\\-\\.]";
		String regexpDelimiterTime="\\:";
		String regexpDelimiterMillis="\\.";
		String regexpDelimiterParts="[\\sT]+";
		Date dtResult=null;
		String sDate1=sDate.replace("Z","");
		try
		{
			if((flags==0) || (flags==fNotNull))
			{
				//Конвертируем как есть
				DateFormat df = DateFormat.getDateInstance();
				dtResult=df.parse(sDate1);
			}
			else
			{
				if(sDate==null || sDate.length()==0)
					return null;
				int iYear=1980;
				int iMonth=1;//февраль
				int iDay=1;
				int iHour=0;
				int iMin=0;
				int iSec=0;
				int iMillis=0;
				int iPosY=2,iPosD=0;
				String sPartDate=getStrPart(sDate1,regexpDelimiterParts,0);
				String sPartTime=getStrPart(sDate1,regexpDelimiterParts,1);

				if((flags & fDATE)!=0)
				{
					if((flags & fDATE_YYYY_MM_DD)!=0)
					{
						iPosY=0;
						iPosD=2;
					}
					iYear=strToInt(getStrPart(sPartDate,regexpDelimiterDate,iPosY));
					iMonth=strToInt(getStrPart(sPartDate,regexpDelimiterDate,1));
					iDay=strToInt(getStrPart(sPartDate,regexpDelimiterDate,iPosD));
				}
				else if(sPartTime.length()==0)
				{
						sPartTime="00:00:00";//Нету первой части
				}

				if((flags & fTIME)!=0)
				{
					if(sPartTime.length()!=0)
					{
						iHour=strToInt(getStrPart(sPartTime,regexpDelimiterTime,0));		
						iMin=strToInt(getStrPart(sPartTime,regexpDelimiterTime,1));
						String secondPart = getStrPart(sPartTime,regexpDelimiterTime,2);						
						iSec=strToInt(getStrPart(secondPart,regexpDelimiterMillis,0));
						iMillis=strToInt(getStrPart(secondPart,regexpDelimiterMillis,1));
					}
					else
					{
						//Будут нулевыми iHour=0,iMin=0,iSec=0
					}					
				}
				//Установка даты
				GregorianCalendar cc1=new GregorianCalendar(iYear,(iMonth-1),iDay,iHour,iMin,iSec);//0-январь
				cc1.set(Calendar.MILLISECOND,iMillis);
				dtResult=cc1.getTime();
			}
		}
		catch (Exception e)
		{
			dtResult=null;
			if((flags & fNotNull)!=0)
			{
				//
				GregorianCalendar cc1=new GregorianCalendar(1980,1,1,0,0,0);
				dtResult=cc1.getTime();			
			}

		}
		return dtResult;	   
	}

	
	/*
	 * Преобразование в строку классов типа String,Date,Integer,Long,Double...
	 * и прочих
	 *  sFormat- шаблон
	 *  для Date см.DateUtils.Format
	 *  для numeric "#,##0.0#;(#,##0.0#)".
	 *  для прочих НЕ имеет значения sFormat,так как вызывается метод toString()
	 */
	public static final String  format(Object my_val,String sFormat)
	{ 
		String rez="";
		DecimalFormat df=null;
		if(my_val==null)
			return rez;
		if(my_val instanceof String)
		{
			rez=(String)my_val;
		}
		else
		if(my_val instanceof java.lang.Number)
		{
			df=(sFormat.length()==0)? new DecimalFormat() : new DecimalFormat(sFormat);
			if(my_val.getClass().getName().equalsIgnoreCase("java.lang.Integer"))
			rez=df.format((long)((Integer)my_val).intValue());
			else
				if(my_val.getClass().getName().equalsIgnoreCase("java.lang.Long"))
					rez=df.format((long)((Long)my_val).longValue());
				else
					if(my_val.getClass().getName().equalsIgnoreCase("java.lang.Double"))
						rez=df.format((double)((Double)my_val).doubleValue());			
		}
		else
			if(my_val instanceof java.util.Date)
			{
				rez=new SimpleDateFormat(sFormat).format((Date)my_val);
			}
			else
				rez=my_val.toString();
		return rez;
	}

	 /** 
	    * A.K. 23.10.2008
	    * Приведение даты, простой вариант
	    * @param dateForAdjust
	    * @param day
	    * @param hour
	    * @param min
	    * @param sec
	    * @return
	    */
	   public static Date adjust(Date dateForAdjust,int year,int month,int day,int hour,int min,int sec)
	   {
		   Date result=null;
		   if(dateForAdjust==null)
			   return result;
		   GregorianCalendar gc1=new GregorianCalendar();
		   gc1.setTime(dateForAdjust);
		   if(year!=0)
			   gc1.add(GregorianCalendar.YEAR,year);
		   if(month!=0)
			   gc1.add(GregorianCalendar.MONTH,month);
		   if(day!=0)
			   gc1.add(GregorianCalendar.DAY_OF_MONTH,day);
		   if(hour!=0)
			   gc1.add(GregorianCalendar.HOUR_OF_DAY,hour);
		   if(min!=0)
			   gc1.add(GregorianCalendar.MINUTE,min);
		   if(sec!=0)
			   gc1.add(GregorianCalendar.SECOND,sec);	   
		   result=gc1.getTime();
		   
		   return result;
	   }
	   
}
