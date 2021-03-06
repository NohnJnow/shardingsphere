/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy.backend.text.admin;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.RuleNotExistsException;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;
import java.util.List;

/**
 * Backend handler for broadcast.
 */
@RequiredArgsConstructor
public final class BroadcastBackendHandler implements TextProtocolBackendHandler {
    
    private final DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory = DatabaseCommunicationEngineFactory.getInstance();
    
    private final String sql;
    
    private final SQLStatement sqlStatement;
    
    private final BackendConnection backendConnection;
    
    @Override
    public BackendResponse execute() throws SQLException {
        String originalSchema = backendConnection.getSchemaName();
        for (String each : ProxyContext.getInstance().getAllSchemaNames()) {
            backendConnection.setCurrentSchema(each);
            if (!ProxyContext.getInstance().getMetaData(each).isComplete()) {
                throw new RuleNotExistsException();
            }
            databaseCommunicationEngineFactory.newTextProtocolInstance(sqlStatement, sql, backendConnection).execute();
        }
        backendConnection.setCurrentSchema(originalSchema);
        return new UpdateResponse();
    }
    
    @Override
    public boolean next() {
        return false;
    }
    
    @Override
    public List<Object> getRowData() {
        return null;
    }
}
