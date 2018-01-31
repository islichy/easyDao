package com.oracle.dao;


import com.oracle.jdbc.util.Dao;
import com.oracle.jdbc.util.Transactional;

public class BookDao {

	@Transactional
	public void save(String name,int price) {		
		Dao.executeSql("insert into book values(88,?,?)", name,price);
		System.out.println("²Ù×÷Íê³É");
		Dao.executeSql("insert into book values(87,?,?)", name,price);
	}
	
	
}
