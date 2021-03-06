package com.fenxiang.rabbit.consume;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;

@Component
public class RabbitSender {
	//自动注入RabbitTemplate模板类
	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Value("${spring.rabbitmq.listener.goods-push.exchange.name}")
	private String goodsExchangeName;
	@Value("${spring.rabbitmq.listener.goods-push.key}")
	private String goodsKey;

	//回调函数: confirm确认
	final RabbitTemplate.ConfirmCallback confirmCallback = new RabbitTemplate.ConfirmCallback() {
		@Override
		public void confirm(CorrelationData correlationData, boolean ack, String cause) {
			System.err.println("correlationData: " + correlationData);
			System.err.println("ack: " + ack);
			if(!ack){
				//可以进行日志记录、异常处理、补偿处理等
				System.err.println("异常处理....");
			}else {
				//更新数据库，可靠性投递机制
			}
		}
	};
	
	//回调函数: return返回
	final RabbitTemplate.ReturnCallback returnCallback = new RabbitTemplate.ReturnCallback() {
		@Override
		public void returnedMessage(org.springframework.amqp.core.Message message, int replyCode, String replyText,
				String exchange, String routingKey) {
			System.err.println("return exchange: " + exchange + ", routingKey: " 
				+ routingKey + ", replyCode: " + replyCode + ", replyText: " + replyText);
		}
	};
	
	//发送消息方法调用: 构建Message消息
	public void send(Object message, Map<String, Object> properties) throws Exception {
		MessageHeaders mhs = new MessageHeaders(properties);
		Message msg = MessageBuilder.createMessage(message, mhs);
		rabbitTemplate.setConfirmCallback(confirmCallback);
		rabbitTemplate.setReturnCallback(returnCallback);
		//id + 时间戳 全局唯一  用于ack保证唯一一条消息,这边做测试写死一个。但是在做补偿策略的时候，必须保证这是全局唯一的消息
		CorrelationData correlationData = new CorrelationData(new Random().nextInt(10000000) +"");
		rabbitTemplate.convertAndSend(goodsExchangeName, goodsKey, msg, correlationData);
	}
	
}
