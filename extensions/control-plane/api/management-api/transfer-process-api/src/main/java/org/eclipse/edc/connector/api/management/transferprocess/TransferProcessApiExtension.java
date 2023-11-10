/*
 *  Copyright (c) 2020 - 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.edc.connector.api.management.transferprocess;

import jakarta.json.Json;
import org.eclipse.edc.connector.api.management.configuration.ManagementApiConfiguration;
import org.eclipse.edc.connector.api.management.configuration.transform.ManagementApiTypeTransformerRegistry;
import org.eclipse.edc.connector.api.management.transferprocess.transform.JsonObjectFromTransferProcessTransformer;
import org.eclipse.edc.connector.api.management.transferprocess.transform.JsonObjectFromTransferStateTransformer;
import org.eclipse.edc.connector.api.management.transferprocess.transform.JsonObjectToTerminateTransferTransformer;
import org.eclipse.edc.connector.api.management.transferprocess.transform.JsonObjectToTransferRequestTransformer;
import org.eclipse.edc.connector.api.management.transferprocess.validation.TerminateTransferValidator;
import org.eclipse.edc.connector.api.management.transferprocess.validation.TransferRequestValidator;
import org.eclipse.edc.connector.spi.transferprocess.TransferProcessService;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.validator.spi.JsonObjectValidatorRegistry;
import org.eclipse.edc.web.spi.WebService;

import static java.util.Collections.emptyMap;
import static org.eclipse.edc.connector.api.management.transferprocess.model.TerminateTransfer.TERMINATE_TRANSFER_TYPE;
import static org.eclipse.edc.connector.transfer.spi.types.TransferRequest.TRANSFER_REQUEST_TYPE;

@Extension(value = TransferProcessApiExtension.NAME)
public class TransferProcessApiExtension implements ServiceExtension {

    public static final String NAME = "Management API: Transfer Process";

    @Inject
    private WebService webService;

    @Inject
    private ManagementApiConfiguration configuration;

    @Inject
    private ManagementApiTypeTransformerRegistry transformerRegistry;

    @Inject
    private TransferProcessService service;

    @Inject
    private JsonObjectValidatorRegistry validatorRegistry;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var builderFactory = Json.createBuilderFactory(emptyMap());
        transformerRegistry.register(new JsonObjectFromTransferProcessTransformer(builderFactory));
        transformerRegistry.register(new JsonObjectFromTransferStateTransformer(builderFactory));

        transformerRegistry.register(new JsonObjectToTerminateTransferTransformer());
        transformerRegistry.register(new JsonObjectToTransferRequestTransformer(context.getMonitor()));

        validatorRegistry.register(TRANSFER_REQUEST_TYPE, TransferRequestValidator.instance());
        validatorRegistry.register(TERMINATE_TRANSFER_TYPE, TerminateTransferValidator.instance());

        var newController = new TransferProcessApiController(context.getMonitor(), service, transformerRegistry, validatorRegistry);
        webService.registerResource(configuration.getContextAlias(), newController);
    }
}
