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
package com.gs.obevo.db.api.platform;

import java.io.File;

import javax.sql.DataSource;

import com.gs.obevo.api.appdata.Change;
import com.gs.obevo.api.platform.DeployExecutionDao;
import com.gs.obevo.api.platform.DeployerAppContext;
import com.gs.obevo.api.platform.MainDeployerArgs;
import com.gs.obevo.db.api.appdata.DbEnvironment;
import com.gs.obevo.db.impl.core.checksum.DbChecksumDao;
import com.gs.obevo.dbmetadata.api.DbMetadataManager;
import org.eclipse.collections.api.list.ImmutableList;

public interface DbDeployerAppContext extends DeployerAppContext<DbEnvironment, DbDeployerAppContext> {
    DbDeployerAppContext setFailOnSetupException(boolean failOnSetupException);

    DbEnvironment getEnvironment();

    File getWorkDir();

    DbDeployerAppContext buildDbContext();

    DbDeployerAppContext buildFileContext();

    ImmutableList<Change> readChangesFromAudit();

    ImmutableList<Change> readChangesFromSource();

    ImmutableList<Change> readChangesFromSource(boolean useBaseline);

    DbMetadataManager getDbMetadataManager();

    SqlExecutor getSqlExecutor();

    DeployExecutionDao getDeployExecutionDao();

    DbChecksumDao getDbChecksumDao();

    /**
     * Data Source with a single shared connection that clients can use to access the database being deployed.
     * This should NOT be used by this internal product code. This is only here for external clients.
     */
    DataSource getDataSource();

    DbDeployerAppContext setupEnvInfra();

    DbDeployerAppContext setupEnvInfra(boolean failOnSetupException);

    DbDeployerAppContext cleanEnvironment();

    DbDeployerAppContext cleanAndDeploy();

    DbDeployerAppContext setupAndCleanAndDeploy();

    /**
     * Read in the input files and return stats. Only used for cases w/ some external integrations where a client wants
     * to read the metrics from the input source.
     */
    void readSource(MainDeployerArgs deployerArgs);
}
