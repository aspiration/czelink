package com.czelink.dbaccess.testbed;

import java.net.UnknownHostException;
import java.util.List;

import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.czelink.infomgmt.intg.entities.InfoArticle;
import com.mongodb.Mongo;

public class MongoAppFind {

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

		final List<InfoArticle> infoArticles = mongoOps
				.findAll(InfoArticle.class);

		for (int i = 0; i < infoArticles.size(); i++) {
			System.out.println(infoArticles.get(i).getTitle());
		}
	}

}
