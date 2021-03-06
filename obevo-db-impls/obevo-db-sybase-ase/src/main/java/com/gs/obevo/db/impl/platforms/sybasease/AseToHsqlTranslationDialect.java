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
package com.gs.obevo.db.impl.platforms.sybasease;

import java.sql.Connection;

import com.gs.obevo.api.platform.ChangeType;
import com.gs.obevo.db.impl.core.jdbc.JdbcHelper;
import com.gs.obevo.db.impl.core.reader.PrepareDbChange;
import com.gs.obevo.db.impl.platforms.DefaultDbTranslationDialect;
import com.gs.obevo.db.impl.platforms.sqltranslator.InMemoryTranslator;
import com.gs.obevo.db.impl.platforms.sqltranslator.SqlTranslatorConfigHelper;
import com.gs.obevo.db.impl.platforms.sqltranslator.impl.DateFormatterPostParsedSqlTranslator;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Sets;

public class AseToHsqlTranslationDialect extends DefaultDbTranslationDialect {
    @Override
    public ImmutableList<String> getInitSqls() {
        return Lists.immutable.with(
                "SET DATABASE SQL SYNTAX MSS TRUE"
                //, "SET DATABASE SQL NAMES FALSE"  // Sybase allows keywords as identifier names; we will be flexible as well
        );
    }

    @Override
    public ImmutableList<PrepareDbChange> getAdditionalTranslators() {
        SqlTranslatorConfigHelper configHelper = SqlTranslatorConfigHelper.createInMemoryDefault();
        configHelper.setNameMapper(new AseSqlTranslatorNameMapper());
        configHelper.getColumnSqlTranslators()
                .with(new AseToHsqlSqlTranslator());
        configHelper.getPostColumnSqlTranslators()
                .with(new AseToHsqlSqlTranslator());
        configHelper.getPostParsedSqlTranslators()
                .with(new AseToInMemorySqlTranslator())
                .with(new DateFormatterPostParsedSqlTranslator(AseToInMemorySqlTranslator.ACCEPTED_DATE_FORMATS));
        configHelper.getUnparsedSqlTranslators()
                .with(new AseToInMemorySqlTranslator())
                .with(new AseToHsqlDomainSqlTranslator())
                .with(new AseRenameTranslator())
        ;

        return Lists.immutable.<PrepareDbChange>with(new InMemoryTranslator(configHelper));
    }

    @Override
    public ImmutableSet<String> getDisabledChangeTypeNames() {
        return Sets.immutable.with(
                ChangeType.DEFAULT_STR,
                ChangeType.FUNCTION_STR,
                ChangeType.RULE_STR,
                ChangeType.SP_STR,
                ChangeType.TRIGGER_STR,
                ChangeType.TRIGGER_INCREMENTAL_OLD_STR
        );
    }

    @Override
    public void initSchema(JdbcHelper jdbc, Connection conn) {
        this.updateAndIgnoreException(conn, jdbc, "create domain TEXT as LONGVARCHAR");
        this.updateAndIgnoreException(conn, jdbc, "create domain SMALLDATETIME as DATETIME");
        this.updateAndIgnoreException(conn, jdbc, "create domain XML as BLOB");
        this.updateAndIgnoreException(conn, jdbc, "create domain MONEY as NUMERIC(30,2)");
    }
}
