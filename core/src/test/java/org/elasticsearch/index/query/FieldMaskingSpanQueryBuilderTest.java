/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.index.query;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.FieldMaskingSpanQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.junit.Test;

import java.io.IOException;

public class FieldMaskingSpanQueryBuilderTest extends BaseQueryTestCase<FieldMaskingSpanQueryBuilder> {

    @Override
    protected Query doCreateExpectedQuery(FieldMaskingSpanQueryBuilder testQueryBuilder, QueryParseContext context) throws IOException {
        String fieldInQuery = testQueryBuilder.fieldName();
        MappedFieldType fieldType = context.fieldMapper(fieldInQuery);
        if (fieldType != null) {
            fieldInQuery = fieldType.names().indexName();
        }
        SpanQuery innerQuery = (SpanQuery) testQueryBuilder.innerQuery().toQuery(context);

        return new FieldMaskingSpanQuery(innerQuery, fieldInQuery);
    }

    @Override
    protected FieldMaskingSpanQueryBuilder doCreateTestQueryBuilder() {
        String fieldName;
        if (randomBoolean()) {
            fieldName = randomFrom(mappedFieldNames);
        } else {
            fieldName = randomAsciiOfLengthBetween(1, 10);
        }
        SpanTermQueryBuilder innerQuery = new SpanTermQueryBuilderTest().createTestQueryBuilder();
        return new FieldMaskingSpanQueryBuilder(innerQuery, fieldName);
    }

    @Test
    public void testValidate() {
        String fieldName;
        SpanQueryBuilder spanQueryBuilder;
        int totalExpectedErrors = 0;
        if (randomBoolean()) {
            fieldName = "fieldName";
        } else {
            fieldName = "";
            totalExpectedErrors++;
        }
        if (randomBoolean()) {
            spanQueryBuilder = new SpanTermQueryBuilder("", "test");
            totalExpectedErrors++;
        } else {
            spanQueryBuilder = new SpanTermQueryBuilder("name", "value");
        }
        FieldMaskingSpanQueryBuilder queryBuilder = new FieldMaskingSpanQueryBuilder(spanQueryBuilder, fieldName);
        assertValidate(queryBuilder, totalExpectedErrors);
    }

    @Test(expected=NullPointerException.class)
    public void testNullFieldName() {
        new FieldMaskingSpanQueryBuilder(new SpanTermQueryBuilder("name", "value"), null);
    }

    @Test(expected=NullPointerException.class)
    public void testNullInnerQuery() {
            new FieldMaskingSpanQueryBuilder(null, "");
    }
}