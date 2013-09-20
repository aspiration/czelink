package com.czelink.dbaccess;

import org.springframework.data.redis.core.RedisOperations;

public interface RedisOperationsAware {

	public void setRedisOperations(
			final RedisOperations<Object, Object> redisOperations);
}
