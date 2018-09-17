/*-
 * #%L
 * Elastic APM Java agent
 * %%
 * Copyright (C) 2018 Elastic and contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.example.stacktrace;

import co.elastic.apm.impl.ElasticApmTracer;
import co.elastic.apm.impl.error.ErrorCapture;
import co.elastic.apm.impl.stacktrace.StacktraceConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ErrorCaptureTest {

    private StacktraceConfiguration stacktraceConfiguration;
    private ElasticApmTracer tracer;

    @BeforeEach
    void setUp() {
        tracer = mock(ElasticApmTracer.class);
        stacktraceConfiguration = spy(StacktraceConfiguration.class);
        when(tracer.getConfig(StacktraceConfiguration.class)).thenReturn(stacktraceConfiguration);
    }

    @Test
    void testCulpritApplicationPackagesNotConfigured() {
        final ErrorCapture errorCapture = new ErrorCapture(tracer);
        errorCapture.setException(new Exception());
        assertThat(errorCapture.getCulprit()).isNull();
    }

    @Test
    void testCulprit() {
        when(stacktraceConfiguration.getApplicationPackages()).thenReturn(List.of("org.example.stacktrace"));
        final ErrorCapture errorCapture = new ErrorCapture(tracer);
        final Exception nestedException = new Exception();
        final Exception topLevelException = new Exception(nestedException);
        topLevelException.printStackTrace();
        errorCapture.setException(topLevelException);
        assertThat(errorCapture.getCulprit()).startsWith("org.example.stacktrace.ErrorCaptureTest.testCulprit(ErrorCaptureTest.java:");
        assertThat(errorCapture.getCulprit()).endsWith(":" + nestedException.getStackTrace()[0].getLineNumber() + ")");
    }

}
