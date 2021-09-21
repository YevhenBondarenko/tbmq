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
package org.thingsboard.mqtt.broker.service.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thingsboard.mqtt.broker.exception.AuthenticationException;
import org.thingsboard.mqtt.broker.service.auth.providers.AuthContext;
import org.thingsboard.mqtt.broker.service.auth.providers.AuthResponse;
import org.thingsboard.mqtt.broker.service.auth.providers.MqttClientAuthProvider;
import org.thingsboard.mqtt.broker.service.auth.providers.MqttClientAuthProviderManager;
import org.thingsboard.mqtt.broker.service.security.authorization.AuthorizationRule;

import java.util.List;

@Slf4j
@Service
public class DefaultAuthenticationService implements AuthenticationService {

    private final List<MqttClientAuthProvider> authProviders;

    public DefaultAuthenticationService(MqttClientAuthProviderManager authProviderManager) {
        this.authProviders = authProviderManager.getActiveAuthProviders();
    }

    @Override
    public List<AuthorizationRule> authenticate(AuthContext authContext) throws AuthenticationException {
        log.trace("[{}] Authenticating client", authContext.getClientId());
        AuthResponse authResponse = tryAuthenticateClient(authContext);
        if (authResponse == null) {
            throw new AuthenticationException("Failed to authenticate client");
        } else {
            return authResponse.getAuthorizationRules();
        }
    }

    private AuthResponse tryAuthenticateClient(AuthContext authContext) throws AuthenticationException {
        if (authProviders.isEmpty()) {
            return new AuthResponse(true, null);
        }
        try {
            for (MqttClientAuthProvider authProvider : authProviders) {
                AuthResponse authResponse = authProvider.authorize(authContext);
                if (authResponse.isSuccess()) {
                    return authResponse;
                }
            }
        } catch (Exception e) {
            log.warn("[{}] Failed to authenticate client. Exception - {}, reason - {}.", authContext.getClientId(), e.getClass().getSimpleName(), e.getMessage());
            throw new AuthenticationException("Exception on client authentication");
        }
        return null;
    }
}
