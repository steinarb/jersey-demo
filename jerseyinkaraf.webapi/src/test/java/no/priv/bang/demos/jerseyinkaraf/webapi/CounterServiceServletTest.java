/*
 * Copyright 2018-2021 Steinar Bang
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
package no.priv.bang.demos.jerseyinkaraf.webapi;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.InternalServerErrorException;

import org.glassfish.jersey.server.ServerProperties;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockrunner.mock.web.MockHttpServletResponse;

import no.priv.bang.demos.jerseyinkaraf.servicedef.Counter;
import no.priv.bang.demos.jerseyinkaraf.servicedef.beans.Count;
import no.priv.bang.demos.jerseyinkaraf.services.CounterService;
import no.priv.bang.demos.jerseyinkaraf.webapi.CounterServiceServlet;
import no.priv.bang.osgi.service.mocks.logservice.MockLogService;

class CounterServiceServletTest {
    static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testDoGet() throws ServletException, IOException {
        MockLogService logservice = new MockLogService();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8181/jerseyinkaraf/api/counter"));
        when(request.getRequestURI()).thenReturn("/jerseyinkaraf/api/counter");
        when(request.getContextPath()).thenReturn("");
        when(request.getServletPath()).thenReturn("/jerseyinkaraf/api");
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        MockHttpServletResponse response = new MockHttpServletResponse();

        CounterServiceServlet servlet = new CounterServiceServlet();
        servlet.setLogService(logservice);
        Counter counterService = mock(Counter.class);
        when(counterService.currentValue()).thenReturn(new Count());
        servlet.setCounter(counterService);

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        servlet.service(request, response);

        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());
        byte[] responseBody = response.getOutputStreamBinaryContent();
        assertThat(responseBody).isNotEmpty();
        Count counter = mapper.readValue(responseBody, Count.class);
        assertEquals(0, counter.getCount());
    }

    @Test
    void testDoGetAfterCounterIncrement() throws ServletException, IOException {
        MockLogService logservice = new MockLogService();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8181/jerseyinkaraf/api/counter"));
        when(request.getRequestURI()).thenReturn("/jerseyinkaraf/api/counter");
        when(request.getContextPath()).thenReturn("");
        when(request.getServletPath()).thenReturn("/jerseyinkaraf/api");
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        MockHttpServletResponse response = new MockHttpServletResponse();

        CounterServiceServlet servlet = new CounterServiceServlet();
        servlet.setLogService(logservice);
        Counter counterService = new CounterService();
        servlet.setCounter(counterService);

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        // Increment the counter twice
        HttpServletRequest postToIncrementCounter = mock(HttpServletRequest.class);
        when(postToIncrementCounter.getMethod()).thenReturn("POST");
        when(postToIncrementCounter.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8181/jerseyinkaraf/api/counter"));
        when(postToIncrementCounter.getRequestURI()).thenReturn("/jerseyinkaraf/api/counter");
        when(postToIncrementCounter.getContextPath()).thenReturn("");
        when(postToIncrementCounter.getServletPath()).thenReturn("/jerseyinkaraf/api");
        when(postToIncrementCounter.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        HttpServletResponse postResponse = mock(HttpServletResponse.class);
        when(postResponse.getWriter()).thenReturn(mock(PrintWriter.class));
        servlet.service(postToIncrementCounter, postResponse);
        servlet.service(postToIncrementCounter, postResponse);

        servlet.service(request, response);

        assertEquals("application/json", response.getContentType());
        assertEquals(200, response.getStatus());
        byte[] responseBody = response.getOutputStreamBinaryContent();
        assertThat(responseBody).isNotEmpty();
        Count counter = mapper.readValue(responseBody, Count.class);
        assertEquals(2, counter.getCount());
    }

    @Test
    void testDoGetWithError() throws ServletException, IOException {
        MockLogService logservice = new MockLogService();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8181/jerseyinkaraf/api/counter"));
        when(request.getRequestURI()).thenReturn("/jerseyinkaraf/api/counter");
        when(request.getContextPath()).thenReturn("");
        when(request.getServletPath()).thenReturn("/jerseyinkaraf/api");
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        MockHttpServletResponse response = new MockHttpServletResponse();

        CounterServiceServlet servlet = new CounterServiceServlet();
        servlet.setLogService(logservice);
        // Create a mock Counter service that causes the internal server error
        Counter counterService = mock(Counter.class);
        InternalServerErrorException exception = new InternalServerErrorException();
        when(counterService.currentValue()).thenThrow(exception);
        servlet.setCounter(counterService);

        // When the servlet is activated it will be plugged into the http whiteboard and configured
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        servlet.service(request, response);

        assertEquals(500, response.getStatus());
        assertEquals(0, response.getOutputStreamBinaryContent().length);
    }

    private ServletConfig createServletConfigWithApplicationAndPackagenameForJerseyResources() {
        ServletConfig config = mock(ServletConfig.class);
        when(config.getInitParameterNames()).thenReturn(Collections.enumeration(Arrays.asList(ServerProperties.PROVIDER_PACKAGES)));
        when(config.getInitParameter(ServerProperties.PROVIDER_PACKAGES)).thenReturn("no.priv.bang.demos.jerseyinkaraf.webapi.resources");
        ServletContext servletContext = mock(ServletContext.class);
        when(config.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttributeNames()).thenReturn(Collections.emptyEnumeration());
        return config;
    }
}
