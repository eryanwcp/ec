package com.eryansky.j2cache.cache.support.util;

import java.io.IOException;

import com.eryansky.j2cache.util.JacksonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.eryansky.j2cache.util.SerializationUtils;


public class J2CacheSerializer implements RedisSerializer<Object>{

	private static final Logger logger = LoggerFactory.getLogger(J2CacheSerializer.class);

	private static final JacksonSerializer jacksonSerializer = new JacksonSerializer();
	@Override
	public byte[] serialize(Object t) throws SerializationException {	
		try {
			return SerializationUtils.serialize(t);
//			return jacksonSerializer.serialize(t);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	@Override
	public Object deserialize(byte[] bytes) throws SerializationException {
		try {
			return SerializationUtils.deserialize(bytes);
//			return jacksonSerializer.deserialize(bytes);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

}
