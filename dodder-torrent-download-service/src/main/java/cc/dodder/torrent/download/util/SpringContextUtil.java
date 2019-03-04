package cc.dodder.torrent.download.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/***
 * Spring 容器上下文工具类，用于在线程中获取 Bean
 *
 * @author Mr.Xu
 * @date 2019-02-22 10:47
 **/
@Component
public class SpringContextUtil implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringContextUtil.applicationContext = applicationContext;
	}

	/**
	* 根据 Bean name 获取 Bean
	*
	* @param name
	* @return java.lang.Object
	*/
	public static Object getBean(String name) {
		return applicationContext.getBean(name);
	}

	/**
	* 根据 Class 获取 Bean
	*
	* @param clazz
	* @return java.lang.Object
	*/
	public static Object getBean(Class clazz) {
		return applicationContext.getBean(clazz);
	}
}
