package com.eryansky.j2cache.cache.support.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import com.eryansky.j2cache.cluster.ClusterPolicy;
import com.eryansky.j2cache.Command;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * spring redis 订阅消息监听
 * @author zhangsaizz
 *
 */
public class SpringRedisMessageListener implements MessageListener{

	private static final Logger logger = LoggerFactory.getLogger(SpringRedisMessageListener.class);

	private final int localCommandId;

	private final ClusterPolicy clusterPolicy;
	
	private final String channel;

	private final RedisSerializer<?> redisSerializer;

	SpringRedisMessageListener(ClusterPolicy clusterPolicy, String channel,int localCommandId,RedisSerializer<?> redisSerializer){
		this.clusterPolicy = clusterPolicy;
		this.channel = channel;
		this.localCommandId = localCommandId;
		this.redisSerializer = redisSerializer;
	}

	private boolean isLocalCommand(Command cmd) {
		return cmd.getSrc() == localCommandId;
	}

	@Override
	public void onMessage(Message message, byte[] pattern) {
		byte[] messageChannel = message.getChannel();
		byte[] messageBody = message.getBody();
		if (messageChannel == null || messageBody == null) {
			return;
		}
        try {
            Command cmd = (Command) redisSerializer.deserialize(messageBody);
			clusterPolicy.handleCommand(cmd);
        } catch (Exception e) {
        	logger.error("Failed to handle received msg", e);
        }
	}

}
