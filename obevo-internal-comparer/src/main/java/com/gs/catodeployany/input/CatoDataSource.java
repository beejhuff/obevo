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
package com.gs.catodeployany.input;

import java.util.Iterator;

import com.gs.catodeployany.data.CatoDataObject;
import com.gs.catodeployany.util.CatoConfiguration;

public interface CatoDataSource extends Iterator<CatoDataObject> {

    String getName();

    String getShortName();

    void open();

    void close();

    void addDerivedField(CatoDerivedField field);

    void setTypeConverter(CatoTypeConverter converter);

    void setSorted(boolean sorted);

    boolean isSorted();

    void setCatoConfiguration(CatoConfiguration configuration);
}
