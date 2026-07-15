package dev.vorstu.config;

import dev.vorstu.dto.RegistrationEmailMessage;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@ConditionalOnProperty(name = "email.dispatch.mode", havingValue = "kafka", matchIfMissing = true)
public class EmailDispatchConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${email.kafka.topic}")
    private String topic;

    @Value("${email.kafka.retry-topic}")
    private String retryTopic;

    @Bean
    public ProducerFactory<String, RegistrationEmailMessage> registrationEmailProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new DefaultKafkaProducerFactory<>(config,
                new StringSerializer(),
                new JacksonJsonSerializer<RegistrationEmailMessage>().noTypeInfo());
    }

    @Bean
    public KafkaTemplate<String, RegistrationEmailMessage> registrationEmailKafkaTemplate() {
        return new KafkaTemplate<>(registrationEmailProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, RegistrationEmailMessage> registrationEmailConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(config,
                new StringDeserializer(),
                new JacksonJsonDeserializer<>(RegistrationEmailMessage.class)
                        .trustedPackages("dev.vorstu.dto")
                        .ignoreTypeHeaders());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RegistrationEmailMessage>
    registrationEmailKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, RegistrationEmailMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(registrationEmailConsumerFactory());
        return factory;
    }

    @Bean
    public NewTopic registrationEmailsTopic() {
        return TopicBuilder.name(topic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic registrationEmailsRetryTopic() {
        return TopicBuilder.name(retryTopic).partitions(1).replicas(1).build();
    }
}
