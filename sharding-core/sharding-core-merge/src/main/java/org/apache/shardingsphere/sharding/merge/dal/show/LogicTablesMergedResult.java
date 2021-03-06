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

package org.apache.shardingsphere.sharding.merge.dal.show;

import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.executor.QueryResult;
import org.apache.shardingsphere.underlying.merge.result.impl.memory.MemoryMergedResult;
import org.apache.shardingsphere.underlying.merge.result.impl.memory.MemoryQueryResultRow;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Logic tables merged result.
 */
public class LogicTablesMergedResult extends MemoryMergedResult<ShardingRule> {
    
    public LogicTablesMergedResult(final ShardingRule shardingRule, 
                                   final SQLStatementContext sqlStatementContext, final RelationMetas relationMetas, final List<QueryResult> queryResults) throws SQLException {
        super(shardingRule, relationMetas, sqlStatementContext, queryResults);
    }
    
    @Override
    protected final List<MemoryQueryResultRow> init(final ShardingRule shardingRule, final RelationMetas relationMetas, 
                                                    final SQLStatementContext sqlStatementContext, final List<QueryResult> queryResults) throws SQLException {
        List<MemoryQueryResultRow> result = new LinkedList<>();
        Set<String> tableNames = new HashSet<>();
        for (QueryResult each : queryResults) {
            while (each.next()) {
                MemoryQueryResultRow memoryResultSetRow = new MemoryQueryResultRow(each);
                String actualTableName = memoryResultSetRow.getCell(1).toString();
                Optional<TableRule> tableRule = shardingRule.findTableRuleByActualTable(actualTableName);
                if (!tableRule.isPresent()) {
                    if (shardingRule.getTableRules().isEmpty() || relationMetas.containsTable(actualTableName) && tableNames.add(actualTableName)) {
                        result.add(memoryResultSetRow);
                    }
                } else if (tableNames.add(tableRule.get().getLogicTable())) {
                    memoryResultSetRow.setCell(1, tableRule.get().getLogicTable());
                    setCellValue(memoryResultSetRow, tableRule.get().getLogicTable(), actualTableName);
                    result.add(memoryResultSetRow);
                }
            }
        }
        return result;
    }
    
    protected void setCellValue(final MemoryQueryResultRow memoryResultSetRow, final String logicTableName, final String actualTableName) {
    }
}
