package com.czelink.dbaccess.testbed;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.czelink.infomgmt.intg.entities.InfoArticle;
import com.czelink.intg.entities.User;
import com.mongodb.Mongo;

public class MongoApp {

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

		mongoOps.insert(user);

		final String[] infoTitle = new String[] { "text1", "text2", "text3",
				"text4" };
		final List articleList = new ArrayList(infoTitle.length);
		for (int i = 0; i < infoTitle.length; i++) {
			final InfoArticle articleInfo = new InfoArticle();
			articleInfo.setAuther(user);
			articleInfo.setDate(new Date());
			articleInfo.setTitle(infoTitle[i]);
			articleList.add(articleInfo);
		}

		mongoOps.insert(articleList, InfoArticle.class);
	}
}
