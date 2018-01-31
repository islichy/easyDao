package com.oracle.jdbc.util;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;


/**
 * 获得代理对象，内部使用cglib来实现的，支持事务及自动打开关闭连接
 * @author Administrator
 *
 */
public class ServiceFactory implements MethodInterceptor{
	Enhancer e=new Enhancer();  //用来创建代理对象

	@Override
	public Object intercept(Object proxy, Method method, Object[] args, MethodProxy arg3) throws Throwable {
		Object obj=null;
		
		System.out.println(method.getName());
		if(method.isAnnotationPresent(Transactional.class)){
			System.out.println("transactional");
			try{
				Dao.begin();
				obj=arg3.invokeSuper(proxy, args);				
				Dao.commit();		
			}catch(Exception e){
				e.printStackTrace();
				Dao.rollback();				
			}finally{				
				Dao.close();
			}	
		}else{
			try{
				obj=arg3.invokeSuper(proxy, args);
			}finally{
				Dao.close();
			}	
		}
		return obj;
	}
	
	//生成代理对象；
	@SuppressWarnings("unchecked")
	public <T> T getProxy(Class<T> clazz){
		T obj=null;
		try {
			obj = clazz.newInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		e.setSuperclass(obj.getClass()); //父类是谁
		e.setCallback(this); //设置回调，当调用代理对象的方法时，则会调用this的intercepter方法
		return (T) e.create();  //返回代理对象
	}
	
	
	public static <T> T getObject(Class<T> clazz){
		return new ServiceFactory().getProxy(clazz);
	}
	

}
