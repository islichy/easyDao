package com.oracle.dao;

import java.sql.SQLException;

import com.oracle.jdbc.util.ServiceFactory;

public class Test {

	public static void main(String[] args) throws SQLException {		
		BookDao dao=ServiceFactory.getObject(BookDao.class);		
		dao.save("good", 78);
	}

}
