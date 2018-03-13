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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.priv.bang.osgi.service.adapters.logservice.LogServiceAdapter;

@Component(service={Servlet.class}, property={"alias=/overlap/api/counter"} )
public class CounterServiceServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final LogServiceAdapter logservice = new LogServiceAdapter();
    private AtomicInteger counter = new AtomicInteger(); // NOSONAR This is just a demo
    static final ObjectMapper mapper = new ObjectMapper();

    @Reference
    public void setLogservice(LogService logservice) {
        this.logservice.setLogService(logservice);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        returnCounterAsJsonInMessageBody(counter.get(), request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int value = counter.incrementAndGet();
        returnCounterAsJsonInMessageBody(value, request, response);
    }

    private void returnCounterAsJsonInMessageBody(int value, HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setContentType("application/json");
            try(PrintWriter writer = response.getWriter()) {
                Count count = new Count(value);
                mapper.writeValue(writer, count);

                response.setStatus(200);
            }

        } catch (Exception e) {
            String message = String.format("Counter REST service caught exception during %s", request.getMethod());
            logservice.log(LogService.LOG_ERROR, message, e);
            response.setStatus(500); // Report internal server error
        }
    }

}
