/*
 * Copyright 2018 Steinar Bang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */
package no.priv.bang.demos.whiteboardwebapi.webapi;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.Matchers;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.priv.bang.demos.whiteboardwebapi.webapi.Count;
import no.priv.bang.demos.whiteboardwebapi.webapi.CounterServiceServlet;
import no.priv.bang.demos.whiteboardwebapi.webapi.mocks.MockHttpServletResponse;
import no.priv.bang.demos.whiteboardwebapi.webapi.mocks.MockLogService;

public class CounterServiceServletTest {
    static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testDoGet() throws ServletException, IOException {
        MockLogService logservice = new MockLogService();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("http://localhost:8181/hello");
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);

        CounterServiceServlet servlet = new CounterServiceServlet();
        servlet.setLogservice(logservice);

        servlet.service(request, response);

        assertEquals("application/json", response.getContentType());
        assertEquals(200, response.getStatus());
        ByteArrayOutputStream responseBody = response.getOutput();
        assertThat(response.getOutput().size()).isGreaterThan(0);
        Count counter = mapper.readValue(responseBody.toByteArray(), Count.class);
        assertEquals(0, counter.getCount());
    }

    @Test
    public void testDoGetAfterCounterIncrement() throws ServletException, IOException {
        MockLogService logservice = new MockLogService();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("http://localhost:8181/hello");
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);

        CounterServiceServlet servlet = new CounterServiceServlet();
        servlet.setLogservice(logservice);

        // Increment the counter twice
        HttpServletRequest postToIncrementCounter = mock(HttpServletRequest.class);
        when(postToIncrementCounter.getMethod()).thenReturn("POST");
        when(postToIncrementCounter.getRequestURI()).thenReturn("http://localhost:8181/hello");
        HttpServletResponse postResponse = mock(HttpServletResponse.class);
        when(postResponse.getWriter()).thenReturn(mock(PrintWriter.class));
        servlet.service(postToIncrementCounter, postResponse);
        servlet.service(postToIncrementCounter, postResponse);

        servlet.service(request, response);

        assertEquals("application/json", response.getContentType());
        assertEquals(200, response.getStatus());
        ByteArrayOutputStream responseBody = response.getOutput();
        assertThat(response.getOutput().size()).isGreaterThan(0);
        Count counter = mapper.readValue(responseBody.toByteArray(), Count.class);
        assertEquals(2, counter.getCount());
    }

    @Test
    public void testDoGetWithError() throws ServletException, IOException {
        MockLogService logservice = new MockLogService();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("http://localhost:8181/hello");
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);
        PrintWriter writer = mock(PrintWriter.class);
        doThrow(IOException.class).when(writer).write(Matchers.<char[]>any(), anyInt(), anyInt());
        when(response.getWriter()).thenReturn(writer);

        CounterServiceServlet servlet = new CounterServiceServlet();
        servlet.setLogservice(logservice);

        servlet.service(request, response);

        assertEquals(500, response.getStatus());
        assertEquals(0, response.getOutput().size());
    }
}
