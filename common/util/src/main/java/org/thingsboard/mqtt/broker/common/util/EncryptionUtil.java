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
package org.thingsboard.mqtt.broker.common.util;

import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

/**
 * @author Valerii Sosliuk
 */
public class EncryptionUtil {

    private EncryptionUtil() {
    }

    public static String trimNewLines(String input) {
        return input.replaceAll("-----BEGIN CERTIFICATE-----", "")
                .replaceAll("-----END CERTIFICATE-----", "")
                .replaceAll("\n", "")
                .replaceAll("\r", "");
    }

    public static String getSha3Hash(String data) {
        String trimmedData = trimNewLines(data);
        byte[] dataBytes = trimmedData.getBytes();
        SHA3Digest md = new SHA3Digest(256);
        md.reset();
        md.update(dataBytes, 0, dataBytes.length);
        byte[] hashedBytes = new byte[256 / 8];
        md.doFinal(hashedBytes, 0);
        return ByteUtils.toHexString(hashedBytes);
    }
}