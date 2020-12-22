/**
 * Copyright © 2016-2020 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.mqtt.broker.sevice.integration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.jodah.concurrentunit.Waiter;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.thingsboard.mqtt.broker.ThingsboardMQTTBrokerApplication;
import org.thingsboard.mqtt.broker.queue.kafka.settings.PublishMsgKafkaSettings;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Import(AbstractPubSubIntegrationTest.KafkaTestContainersConfiguration.class)
@ComponentScan({"org.thingsboard.mqtt.broker"})
@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = ThingsboardMQTTBrokerApplication.class)
public abstract class AbstractPubSubIntegrationTest {
    static final int SUBSCRIBERS_COUNT = 10;
    static final int PUBLISHERS_COUNT = 5;
    static final int PUBLISH_MSGS_COUNT = 100;

    @ClassRule
    public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"));

    void initPubSubTest() throws Throwable {
        Waiter subscribersWaiter = new Waiter();
        CountDownLatch connectingSubscribers = new CountDownLatch(SUBSCRIBERS_COUNT);
        ExecutorService executor = Executors.newFixedThreadPool(PUBLISHERS_COUNT);
        for (int i = 0; i < SUBSCRIBERS_COUNT; i++) {
            int finalI = i;
            executor.execute(() -> {
                initSubscriber(subscribersWaiter, finalI);
                connectingSubscribers.countDown();
            });
        }

        connectingSubscribers.await(2, TimeUnit.SECONDS);

        CountDownLatch processingPublishers = new CountDownLatch(PUBLISHERS_COUNT);
        for (int i = 0; i < PUBLISHERS_COUNT; i++) {
            int finalI = i;
            executor.execute(() -> {
                initPublisher(subscribersWaiter, finalI);
                processingPublishers.countDown();
            });
        }

        processingPublishers.await(2, TimeUnit.SECONDS);

        subscribersWaiter.await(1, TimeUnit.SECONDS, SUBSCRIBERS_COUNT);
    }

    abstract void initSubscriber(Waiter waiter, int subscriberId);

    abstract void initPublisher(Waiter waiter, int publisherId);

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    static class TestPublishMsg {
        int publisherId;
        int sequenceId;
        boolean isLast;
    }


    @TestConfiguration
    static class KafkaTestContainersConfiguration {
        @Bean
        ReplaceKafkaPropertiesBeanPostProcessor beanPostProcessor() {
            return new ReplaceKafkaPropertiesBeanPostProcessor();
        }
    }

    static class ReplaceKafkaPropertiesBeanPostProcessor implements BeanPostProcessor {
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof PublishMsgKafkaSettings) {
                PublishMsgKafkaSettings kafkaSettings = (PublishMsgKafkaSettings) bean;
                kafkaSettings.setServers(kafka.getBootstrapServers());
            }
            return bean;
        }
    }

}
