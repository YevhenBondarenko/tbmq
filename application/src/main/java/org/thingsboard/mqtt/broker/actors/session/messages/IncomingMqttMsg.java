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
package org.thingsboard.mqtt.broker.actors.session.messages;

import io.netty.handler.codec.mqtt.MqttMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.mqtt.broker.actors.TbActorId;
import org.thingsboard.mqtt.broker.actors.msg.MsgType;
import org.thingsboard.mqtt.broker.actors.msg.TbActorMsg;

import java.util.UUID;

@Slf4j
@Getter
public class IncomingMqttMsg extends SessionDependentMsg {
    // TODO: think if it's really worth it or we can create a DTO for transfering and release messages in SessionHandler
    // message needs to be released at some point of a time
    private final MqttMessage msg;

    public IncomingMqttMsg(UUID sessionId, MqttMessage msg) {
        super(sessionId);
        this.msg = msg;
    }

    @Override
    public MsgType getMsgType() {
        return MsgType.INCOMING_MQTT_MSG;
    }
}
