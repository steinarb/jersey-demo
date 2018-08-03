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
package no.priv.bang.demos.jerseyinkaraf.webapi;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.WebConfig;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.osgi.service.log.LogService;

import no.priv.bang.demos.jerseyinkaraf.servicedef.Counter;
import no.priv.bang.osgi.service.adapters.logservice.LogServiceAdapter;

@Component(
    service=Servlet.class,
    property={"alias=/jerseyinkaraf/api",
              HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_INIT_PARAM_PREFIX+ServerProperties.PROVIDER_PACKAGES+"=no.priv.bang.demos.jerseyinkaraf.webapi.resources"
    } )
public class CounterServiceServlet extends ServletContainer {
    private static final long serialVersionUID = 1L;
    private Counter counter = null; // NOSONAR This is an injected service, in practice a constant
    private final LogServiceAdapter logservice = new LogServiceAdapter();

    @Reference
    public void setCounter(Counter counter) {
        this.counter = counter;
    }

    @Reference
    public void setLogservice(LogService logservice) {
        this.logservice.setLogService(logservice);
    }

    @Override
    protected void init(WebConfig webConfig) throws ServletException {
        super.init(webConfig);
        ResourceConfig copyOfExistingConfig = new ResourceConfig(getConfiguration());
        copyOfExistingConfig.register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(logservice).to(LogService.class);
                    bind(counter).to(Counter.class);
                }
            });
        reload(copyOfExistingConfig);
    }

}
