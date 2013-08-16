package com.czelink.dbaccess.testbed;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.czelink.infomgmt.intg.entities.InfoArticle;
import com.czelink.common.intg.entities.User;
import com.mongodb.Mongo;

public class MongoAppInsert {

	/**
	 * @param args
	 * @throws UnknownHostException
	 */
	public static void main(String[] args) throws UnknownHostException {
		final UserCredentials userCredentials = new UserCredentials("Lambert",
				"123");
		final MongoDbFactory mongoDbFactory = new SimpleMongoDbFactory(
				new Mongo(), "test", userCredentials);

		final MongoOperations mongoOps = new MongoTemplate(mongoDbFactory);

		final User user = new User();
		user.setGender(true);
		user.setCompany("HSDC");
		user.setJobTitle("Software Engineer");
		user.setMailAddress("jason.lambert.89@gmail.com");
		user.setUsername("Jason_Lambert");
		user.setPassword("123");
		user.setRegisterDate(new Date());

//		final Properties props = new Properties();
//		final InputStream inputStream = MongoAppInsert.class.getClassLoader()
//				.getResourceAsStream("title.properties");
//		try {
//			props.load(inputStream);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		System.out.println(props.getProperty("text1"));

		final String[] infoTitle = new String[] { "第三届财智远见论坛",
				"特别关注：财政部规范企...", "我国将加快房产税改革试...",
				"七大战略新兴产业规划详解" };
		final List articleList = new ArrayList(infoTitle.length);
		for (int i = 0; i < infoTitle.length; i++) {
			final InfoArticle articleInfo = new InfoArticle();
			articleInfo.setAuther(user);
			articleInfo.setDate(new Date());
			articleInfo.setTitle(infoTitle[i]);
			articleList.add(articleInfo);
		}

		mongoOps.insert(user);
		mongoOps.insert(articleList, InfoArticle.class);
	}
}
