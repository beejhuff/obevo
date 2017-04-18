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
package com.gs.obevo.impl.command;

import com.gs.obevo.api.appdata.Change;
import com.gs.obevo.impl.ExecuteChangeCommand;

public abstract class AbstractExecuteChangeCommand extends AuditOnlyChangeCommand implements ExecuteChangeCommand {
    private boolean drop;

    protected AbstractExecuteChangeCommand(Change artifact) {
        super(artifact);
    }

    /**
     * Indicates if this command should be treated as a drop for the change sorting logic, which will cause it
     * to get sorted in the reverse order.
     */
    @Override
    public final boolean isDrop() {
        return this.drop;
    }

    /**
     * See {@link this#isDrop()}.
     */
    @Override
    public final ExecuteChangeCommand withDrop(boolean drop) {
        this.drop = drop;
        return this;
    }

}
