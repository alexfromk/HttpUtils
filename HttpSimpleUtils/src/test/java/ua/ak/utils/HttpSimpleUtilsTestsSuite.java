package ua.ak.utils;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Запуск пакета тестов для crmUtilsCommon
 * @author Alex
 * Сюда добавляем юнит-тесты для пакетного запуска
 * для проверки всего crmUtilsCommon
 *
 */
@RunWith (Suite.class)
@SuiteClasses 
(
	       {
	    	   ua.ak.utils.util.RestUtilsTest.class
	        }
)
public class HttpSimpleUtilsTestsSuite
{
   // empty class, no properties, no methods, nothing!!
}
