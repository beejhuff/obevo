/**
 * Copyright 2017 Goldman Sachs.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.gs.obevo.api.appdata;

import java.util.LinkedHashSet;

import com.gs.obevo.api.platform.DeployerAppContext;
import com.gs.obevo.api.platform.DeployerRuntimeException;
import com.gs.obevo.api.platform.Platform;
import com.gs.obevo.util.inputreader.Credential;
import com.gs.obevo.util.vfs.FileObject;
import com.gs.obevo.util.vfs.FileResolverStrategy;
import com.gs.obevo.util.vfs.FileRetrievalMode;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.Sets;

public class Environment<T extends Platform> {
    private String name;
    private T platform;
    private boolean cleanBuildAllowed = false;
    private ImmutableMap<String, String> tokens = Maps.immutable.empty();
    private String defaultUserId;
    private String defaultPassword;
    private Class<? extends DeployerAppContext> appContextBuilderClass;

    private FileObject coreSourcePath;
    private ListIterable<String> additionalSourceDirs;
    private RichIterable<FileObject> sourceDirs;
    private ListIterable<FileResolverStrategy> fileResolverStrategies = Lists.mutable.<FileResolverStrategy>of(FileRetrievalMode.FILE_SYSTEM, FileRetrievalMode.CLASSPATH);
    private String dbSchemaPrefix = "";
    private String dbSchemaSuffix = "";
    private ImmutableSet<Schema> allSchemas = Sets.immutable.empty();
    private ImmutableMap<String, String> schemaNameOverrides = Maps.immutable.empty();
    private boolean rollbackDetectionEnabled = true;

    public static final Function<Environment, String> TO_NAME = new Function<Environment, String>() {
        @Override
        public String valueOf(Environment env) {
            return env.getName();
        }
    };

    public void copyFieldsFrom(Environment<T> env) {
        this.name = env.name;
        this.platform = env.platform;
        this.cleanBuildAllowed = env.cleanBuildAllowed;
        this.tokens = env.tokens;
        this.defaultUserId = env.defaultUserId;
        this.defaultPassword = env.defaultPassword;
        this.appContextBuilderClass = env.appContextBuilderClass;
        this.coreSourcePath = env.coreSourcePath;
        this.additionalSourceDirs = Lists.mutable.withAll(env.additionalSourceDirs);
        this.sourceDirs = env.sourceDirs;
        this.dbSchemaPrefix = env.dbSchemaPrefix;
        this.dbSchemaSuffix = env.dbSchemaSuffix;
        this.allSchemas = env.allSchemas;
        this.schemaNameOverrides = env.schemaNameOverrides;
        this.rollbackDetectionEnabled = env.rollbackDetectionEnabled;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public T getPlatform() {
        return platform;
    }

    public void setPlatform(T platform) {
        this.platform = platform;
    }

    public boolean isCleanBuildAllowed() {
        return this.cleanBuildAllowed;
    }

    public void setCleanBuildAllowed(boolean cleanBuildAllowed) {
        this.cleanBuildAllowed = cleanBuildAllowed;
    }

    public String getDefaultUserId() {
        return this.defaultUserId;
    }

    public void setDefaultUserId(String defaultUserId) {
        this.defaultUserId = defaultUserId;
    }

    public String getDefaultPassword() {
        return this.defaultPassword;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    public String getDisplayString() {
        return "Environment [" + this.getName() + "]";
    }

    public ImmutableMap<String, String> getTokens() {
        return this.tokens;
    }

    public void setTokens(ImmutableMap<String, String> tokens) {
        this.tokens = tokens != null ? tokens : Maps.immutable.<String, String>empty();
    }

    /**
     * The main source path that was used to invoke this environment deployment. Used in case the paths are read in via
     * configuration and need to be resolved by some other executor.
     * Most other clients should just stick w/ {@link #setSourceDirs(RichIterable)}.
     */
    public void setCoreSourcePath(FileObject coreSourcePath) {
        this.coreSourcePath = coreSourcePath;
    }

    public ListIterable<String> getAdditionalSourceDirs() {
        return additionalSourceDirs;
    }

    /**
     * Paths specified in a config file. Used in case the paths need to be resolved by some other executor.
     * Most other clients should just stick w/ {@link #setSourceDirs(RichIterable)}.
     */
    public void setAdditionalSourceDirs(ListIterable<String> additionalSourceDirs) {
        this.additionalSourceDirs = additionalSourceDirs;
    }

    public RichIterable<FileObject> getSourceDirs() {
        if (this.sourceDirs == null) {
            // only keep the distinct list of files here
            LinkedHashSet<FileObject> fileObjects = new LinkedHashSet<FileObject>();
            if (coreSourcePath != null) {
                fileObjects.add(coreSourcePath);
            }
            if (additionalSourceDirs != null) {
                fileObjects.addAll(additionalSourceDirs.flatCollect(new Function<String, Iterable<FileObject>>() {
                    @Override
                    public Iterable<FileObject> valueOf(String path) {
                        MutableList<FileObject> resolvedFileObjects = Lists.mutable.empty();
                        for (FileResolverStrategy fileResolverStrategy : fileResolverStrategies) {
                            resolvedFileObjects.addAllIterable(fileResolverStrategy.resolveFileObjects(path));
                        }
                        if (resolvedFileObjects.isEmpty()) {
                            throw new IllegalArgumentException("Unable to find the given path [" + path + "] via any of the fileResolverStrategies:" + fileResolverStrategies.makeString(", "));
                        }
                        return resolvedFileObjects;
                    }
                }).toList());
            }
            this.sourceDirs = Lists.mutable.withAll(fileObjects);
        }
        return this.sourceDirs;
    }

    public void setSourceDirs(RichIterable<FileObject> sourceDirs) {
        this.sourceDirs = sourceDirs;
    }

    public ListIterable<FileResolverStrategy> getFileResolverStrategies() {
        return fileResolverStrategies;
    }

    public void setFileResolverStrategies(ListIterable<FileResolverStrategy> fileResolverStrategies) {
        this.fileResolverStrategies = fileResolverStrategies;
    }

    @Override
    public String toString() {
        return this.getDisplayString();
    }

    public DeployerAppContext getAppContextBuilder() {
        DeployerAppContext deployerAppContext;
        try {
            deployerAppContext = this.appContextBuilderClass.newInstance();
        } catch (InstantiationException e) {
            throw new DeployerRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new DeployerRuntimeException(e);
        }
        deployerAppContext.setEnvironment(this);
        if (defaultUserId != null && defaultPassword != null) {
            deployerAppContext.setCredential(new Credential(defaultUserId, defaultPassword));
        }

        return deployerAppContext;
    }

    public void setAppContextBuilderClass(Class<? extends DeployerAppContext> appContextBuilderClass) {
        this.appContextBuilderClass = appContextBuilderClass;
    }

    public ImmutableSet<String> getSchemaNames() {
        return this.getSchemas().collect(Schema.TO_NAME);
    }

    public String getPhysicalSchemaPrefixInternal(String schema) {
        Validate.isTrue(getAllSchemas().collect(Schema.TO_NAME).contains(schema),
                "Schema does not exist in the environment. Requested schema: " + schema
                        + "; available schemas: " + getSchemaNames().makeString(","));

        return ObjectUtils.defaultIfNull(this.schemaNameOverrides.get(schema), schema);
    }

    /**
     * Returns the schemas to be deployed to by the application. We do not include readOnly schemas for compatibility
     * with the rest of the code.
     */
    public ImmutableSet<Schema> getSchemas() {
        return this.allSchemas.reject(Schema.IS_READ_ONLY);
    }

    public void setSchemas(ImmutableSet<Schema> schemas) {
        this.allSchemas = schemas;
    }

    /**
     * Returns all schemas (read-write and read-only).
     */
    public ImmutableSet<Schema> getAllSchemas() {
        return allSchemas;
    }

    public String getDbSchemaPrefix() {
        return this.dbSchemaPrefix;
    }

    public void setDbSchemaPrefix(String dbSchemaPrefix) {
        // ensure this value is never null. We use it all over the place and don't check
        this.dbSchemaPrefix = (null == dbSchemaPrefix) ? "" : dbSchemaPrefix;
    }

    public String getDbSchemaSuffix() {
        return this.dbSchemaSuffix;
    }

    public void setDbSchemaSuffix(String dbSchemaSuffix) {
        // ensure this value is never null. We use it all over the place and don't check
        this.dbSchemaSuffix = (null == dbSchemaSuffix) ? "" : dbSchemaSuffix;
    }

    private final Function<String, PhysicalSchema> CONVERT_TO_PHYSICAL_SCHEMA = new Function<String, PhysicalSchema>() {
        @Override
        public PhysicalSchema valueOf(String schema) {
            // do not append the suffix from the getDeployer metadata if an override is specified
            String prefix = Environment.this.schemaNameOverrides.containsKey(schema) ? "" : getDbSchemaPrefix();
            String suffix = Environment.this.schemaNameOverrides.containsKey(schema) ? "" : getDbSchemaSuffix();
            PhysicalSchema physicalSchemaTemp = PhysicalSchema.parseFromString(getPhysicalSchemaPrefixInternal(schema));
            return new PhysicalSchema(prefix + physicalSchemaTemp.getPhysicalName() + suffix, physicalSchemaTemp.getSubschema());
        }
    };

    public PhysicalSchema getPhysicalSchema(String schema) {
        return this.CONVERT_TO_PHYSICAL_SCHEMA.valueOf(schema);
    }

    public ImmutableSet<PhysicalSchema> getPhysicalSchemas() {
        return this.getSchemas().collect(Schema.TO_NAME).collect(this.CONVERT_TO_PHYSICAL_SCHEMA);
    }

    public ImmutableSet<PhysicalSchema> getAllPhysicalSchemas() {
        return this.getAllSchemas().collect(Schema.TO_NAME).collect(this.CONVERT_TO_PHYSICAL_SCHEMA);
    }

    public void setSchemaNameOverrides(ImmutableMap<String, String> schemaNameOverrides) {
        this.schemaNameOverrides = schemaNameOverrides;
    }

    /**
     * Whether the rollback detection is enabled. Defaults to true; we have this here as a fallback in case the check
     * fails for some reason.
     */
    public boolean isRollbackDetectionEnabled() {
        return rollbackDetectionEnabled;
    }

    public void setRollbackDetectionEnabled(boolean rollbackDetectionEnabled) {
        this.rollbackDetectionEnabled = rollbackDetectionEnabled;
    }
}
