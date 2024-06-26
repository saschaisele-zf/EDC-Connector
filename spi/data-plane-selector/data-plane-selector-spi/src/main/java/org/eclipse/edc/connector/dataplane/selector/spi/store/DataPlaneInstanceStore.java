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

package org.eclipse.edc.connector.dataplane.selector.spi.store;

import org.eclipse.edc.connector.dataplane.selector.spi.instance.DataPlaneInstance;
import org.eclipse.edc.spi.persistence.StateEntityStore;
import org.eclipse.edc.spi.result.StoreResult;

import java.util.stream.Stream;

/**
 * Holds all {@link DataPlaneInstance} objects that are known to the DPF selector.
 * The collection of {@link DataPlaneInstance} objects is mutable at runtime, so implementations must take that into account.
 */
public interface DataPlaneInstanceStore extends StateEntityStore<DataPlaneInstance> {

    String DATA_PLANE_INSTANCE_EXISTS = "Data Plane Instance with ID %s already exists";
    String DATA_PLANE_INSTANCE_NOT_FOUND = "Data Plane Instance with ID %s not found";

    /**
     * Stores the {@link DataPlaneInstance} if a data plane instance with the same ID doesn't exist.
     *
     * @param instance The {@link DataPlaneInstance} to store.
     *
     * @return {@link StoreResult#success()} if the data plane instance was stored, {@link StoreResult#alreadyExists(String)} if a data
     *         plane instance with the same ID already exists
     * @deprecated please use {@link #save(Object)}
     */
    @Deprecated(since = "0.7.0")
    default StoreResult<Void> create(DataPlaneInstance instance) {
        save(instance);
        return StoreResult.success();
    }

    /**
     * Updates the {@link DataPlaneInstance} if a data plane instance with the same ID already exists.
     *
     * @param instance The {@link DataPlaneInstance} to update.
     *
     * @return {@link StoreResult#success()} if the data plane instance was updated, {@link StoreResult#notFound(String)} if a data
     *         plane instance with the same ID was not found
     * @deprecated please use {@link #save(Object)}
     */
    @Deprecated(since = "0.7.0")
    default StoreResult<Void> update(DataPlaneInstance instance) {
        save(instance);
        return StoreResult.success();
    }

    /**
     * Delete a data plane instance by its id.
     *
     * @param instanceId the data plane instance id.
     * @return the deleted data plane instance.
     */
    StoreResult<DataPlaneInstance> deleteById(String instanceId);

    Stream<DataPlaneInstance> getAll();

}
