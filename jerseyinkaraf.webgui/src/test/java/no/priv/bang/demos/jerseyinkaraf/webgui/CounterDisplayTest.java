/*
 * Copyright 2018-2024 Steinar Bang
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
package no.priv.bang.demos.jerseyinkaraf.webgui;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import com.mockrunner.mock.web.MockHttpServletResponse;

import no.priv.bang.osgi.service.mocks.logservice.MockLogService;

class CounterDisplayTest {

    @Test
    void testDoGet() throws ServletException, IOException {
        var logservice = new MockLogService();
        var request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("http://localhost:8181/jerseyinkaraf/");
        when(request.getPathInfo()).thenReturn("/");
        var response = new MockHttpServletResponse();

        var servlet = new CounterDisplay();
        servlet.setLogservice(logservice);

        servlet.service(request, response);

        // Verify that the response from the servlet is as expected
        var expectedServletOutput = new SoftAssertions();
        expectedServletOutput.assertThat(response.getContentType()).isEqualTo("text/html");
        expectedServletOutput.assertThat(response.getStatus()).isEqualTo(200);
        expectedServletOutput.assertThat(response.getOutputStreamBinaryContent()).isNotEmpty();
        expectedServletOutput.assertAll();
    }

    @Test
    void testDoGetWithError() throws ServletException, IOException {
        var logservice = new MockLogService();
        var request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("http://localhost:8181/jerseyinkaraf/");
        when(request.getPathInfo()).thenReturn("/");
        var response = spy(new MockHttpServletResponse());
        var outputstream = mock(ServletOutputStream.class);
        doThrow(IOException.class).when(outputstream).write(anyInt());
        when(response.getOutputStream()).thenReturn(outputstream);

        var servlet = new CounterDisplay();
        servlet.setLogservice(logservice);

        servlet.service(request, response);

        // Verify that the response from the servlet is as expected
        var expectedServletOutput = new SoftAssertions();
        expectedServletOutput.assertThat(response.getStatus()).isEqualTo(500);
        expectedServletOutput.assertThat(response.getOutputStreamBinaryContent()).isEmpty();
        expectedServletOutput.assertAll();
    }

    @Test
    void testDoGetWithNotFound() throws ServletException, IOException {
        var logservice = new MockLogService();
        var request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("http://localhost:8181/jerseyinkaraf/");
        when(request.getPathInfo()).thenReturn("/notafileinservlet");
        var response = spy(new MockHttpServletResponse());
        var outputstream = mock(ServletOutputStream.class);
        doThrow(IOException.class).when(outputstream).write(anyInt());
        when(response.getOutputStream()).thenReturn(outputstream);

        var servlet = new CounterDisplay();
        servlet.setLogservice(logservice);

        servlet.service(request, response);

        // Verify that the response from the servlet is as expected
        var expectedServletOutput = new SoftAssertions();
        expectedServletOutput.assertThat(response.getErrorCode()).isEqualTo(404);
        expectedServletOutput.assertThat(response.getOutputStreamBinaryContent()).isEmpty();
        expectedServletOutput.assertAll();
    }

    @Test
    void testDoGetWithRedirect() throws ServletException, IOException {
        var logservice = new MockLogService();
        var request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("http://localhost:8181/jerseyinkaraf");
        var response = spy(new MockHttpServletResponse());
        var outputstream = mock(ServletOutputStream.class);
        doThrow(IOException.class).when(outputstream).write(anyInt());
        when(response.getOutputStream()).thenReturn(outputstream);

        var servlet = new CounterDisplay();
        servlet.setLogservice(logservice);

        servlet.service(request, response);

        // Verify that the response from the servlet is as expected
        var expectedServletOutput = new SoftAssertions();
        expectedServletOutput.assertThat(response.getStatus()).isEqualTo(302);
        expectedServletOutput.assertThat(response.getOutputStreamBinaryContent()).isEmpty();
        expectedServletOutput.assertAll();
    }

    @Test
    void testGuessContentTypeFromResourceName() {
        var servlet = new CounterDisplay();
        assertEquals("text/html", servlet.guessContentTypeFromResourceName("index.html"));
        assertEquals("text/javascript", servlet.guessContentTypeFromResourceName("bundle.js"));
        assertEquals("text/css", servlet.guessContentTypeFromResourceName("index.css"));
    }
}
