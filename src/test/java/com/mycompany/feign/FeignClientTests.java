/*
 * Copyright 2013-2016 the original author or authors.
 *
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
 */

package com.mycompany.feign;

import com.mycompany.myapp.MicroserviceApp;
import com.mycompany.myapp.config.SecurityBeanOverrideConfiguration;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.netflix.ribbon.StaticServerList;
import org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {MicroserviceApp.class, FeignClientTests.Application.class,
    SecurityBeanOverrideConfiguration.class}, webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class FeignClientTests {

    @Autowired
    private LocalApiClient localApiClient;

    @FeignClient(name = "local-api")
    protected interface LocalApiClient {

        @RequestMapping(method = RequestMethod.GET, path = "/headers")
        Map<String, String> getHeaders();

    }

    @Configuration
    @EnableAutoConfiguration
    @RestController
    @EnableFeignClients(clients = { LocalApiClient.class})
    @RibbonClient(name = "local-api", configuration = LocalRibbonClientConfiguration.class)
    protected static class Application {

        @RequestMapping(method = RequestMethod.GET, path = "/headers")
        public Map<String, String> getHeaders(HttpServletRequest request) {
            Map<String, String> headers = new HashMap<>();
            Enumeration headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String key = (String) headerNames.nextElement();
                String value = request.getHeader(key);
                headers.put(key, value);
            }
            return headers;
        }

        @Configuration
        public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

            @Override
            public void configure(WebSecurity web) throws Exception {
                web.ignoring().antMatchers("/**");
            }
        }
    }

    @Test
    public void testHeaderContent() {
        Map<String, String> headers = this.localApiClient.getHeaders();
        assertNotNull("headers was null", headers);
        assertTrue("headers didn't contain Authorization header",
            headers.containsKey(OAuth2FeignRequestInterceptor.AUTHORIZATION));
    }

    // Load balancer with fixed server list for "local" pointing to localhost
    @Configuration
    public static class LocalRibbonClientConfiguration {

        @Value("${local.server.port}")
        private int port = 0;

        @Bean
        public ServerList<Server> ribbonServerList() {
            return new StaticServerList<>(new Server("localhost", this.port));
        }

    }
}
