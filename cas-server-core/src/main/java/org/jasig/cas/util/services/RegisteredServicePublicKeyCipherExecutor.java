/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.util.services;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.util.CompressionUtils;
import org.jasig.cas.util.cipher.CipherExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.security.PublicKey;

/**
 * Default cipher implementation based on public keys.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class RegisteredServicePublicKeyCipherExecutor implements CipherExecutor {
    private static final String UTF8_ENCODING = "UTF-8";

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServicePublicKeyCipherExecutor.class);

    private final RegisteredService registeredService;

    /**
     * Instantiates a new Default cipher executor.
     *
     * @param registeredService the registered service
     */
    public RegisteredServicePublicKeyCipherExecutor(final RegisteredService registeredService) {
        this.registeredService = registeredService;
    }

    /**
     * Encrypt using the given cipher, and encode the data in base 64.
     *
     * @param data the data
     * @return the encoded piece of data in base64
     */
    public String encode(final String data) {
        try {
            final Cipher cipher = initializeCipherBasedOnServicePublicKey();
            if (cipher != null) {
                final byte[] cipherData = cipher.doFinal(data.getBytes(UTF8_ENCODING));
                return CompressionUtils.encodeBase64(cipherData);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Initialize cipher based on service public key.
     *
     * @return the false if no public key is found
     * or if cipher cannot be initialized, etc.
     */
    private Cipher initializeCipherBasedOnServicePublicKey() {
        try {
            if (this.registeredService.getPublicKey() == null) {
                LOGGER.debug("No public key is defined for service [{}]. No encoding will take place.",
                        this.registeredService);
                return null;
            }
            final PublicKey publicKey = this.registeredService.getPublicKey().createInstance();
            if (publicKey == null) {
                LOGGER.debug("No public key instance created for service [{}]. No encoding will take place.",
                        this.registeredService);
                return null;
            }
            LOGGER.debug("Using public key [{}] to initialize the cipher", this.registeredService.getPublicKey());

            final Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            LOGGER.debug("Initialized cipher in encrypt-mode via the public key algorithm [{}]",
                    publicKey.getAlgorithm());
            return cipher;
        } catch (final Exception e) {
            LOGGER.warn("Cipher could not be initialized for service [{}]. Error "
                    + e.getMessage(), registeredService);
        }
        return null;
    }
}
