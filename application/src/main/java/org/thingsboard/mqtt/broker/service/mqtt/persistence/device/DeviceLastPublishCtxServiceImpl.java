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
package org.thingsboard.mqtt.broker.service.mqtt.persistence.device;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thingsboard.mqtt.broker.queue.provider.DevicePublishCtxQueueFactory;
import org.thingsboard.mqtt.broker.service.mqtt.persistence.BaseLastPublishCtxService;
import org.thingsboard.mqtt.broker.service.mqtt.persistence.LastPublishCtxService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceLastPublishCtxServiceImpl implements DeviceLastPublishCtxService {
    private LastPublishCtxService lastPublishCtxService;

    @Value("${queue.device-publish-ctx.poll-interval}")
    private long pollDuration;
    @Value("${queue.device-publish-ctx.graceful-shutdown-timout:100}")
    private long gracefulShutdownTimeout;
    @Value("${queue.device-publish-ctx.client-threads}")
    private int clientThreadsCount;

    private final DevicePublishCtxQueueFactory devicePublishCtxQueueFactory;

    @PostConstruct
    public void init() {
        this.lastPublishCtxService = BaseLastPublishCtxService.builder()
                .name("persisted-device-last-publish-ctx")
                .clientThreadsCount(clientThreadsCount)
                .pollDuration(pollDuration)
                .gracefulShutdownTimeout(gracefulShutdownTimeout)
                .queueFactory(devicePublishCtxQueueFactory)
                .build();

        log.info("Start loading persisted LastPublishCtx for DEVICE clients.");
        lastPublishCtxService.loadPersistedCtx();
    }

    @PreDestroy
    public void destroy() {
        log.trace("Executing destroy.");
        if (lastPublishCtxService != null) {
            lastPublishCtxService.destroy();
        }
    }

    @Override
    public int getNextPacketId(String clientId) {
        log.trace("[{}] Getting next packet id.", clientId);
        return lastPublishCtxService.getNextPacketId(clientId);
    }

    @Override
    public void saveLastPublishCtx(String clientId, int packetId) {
        log.trace("[{}] Saving packet id [{}]", clientId, packetId);
        lastPublishCtxService.saveLastPublishCtx(clientId, packetId);
    }

    @Override
    public void clearContext(String clientId) {
        log.trace("[{}] Clearing last publish ctx.", clientId);
        lastPublishCtxService.clearContext(clientId);
    }
}
