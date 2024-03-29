/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet.view.script;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.testfixture.servlet.MockHttpServletRequest;
import org.springframework.web.testfixture.servlet.MockHttpServletResponse;
import org.springframework.web.testfixture.servlet.MockServletContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.condition.JRE.JAVA_21;
import static org.mockito.Mockito.mock;

/**
 * Tests for Kotlin script templates running on Kotlin JSR-223 support.
 *
 * @author Sebastien Deleuze
 * @author Sam Brannen
 */
@DisabledForJreRange(min = JAVA_21, disabledReason = "Kotlin doesn't support Java 21+ yet")
class KotlinScriptTemplateTests {

	private WebApplicationContext webAppContext = mock();

	private ServletContext servletContext = new MockServletContext();


	@BeforeEach
	void setup() {
		this.servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, this.webAppContext);
	}

	@Test
	void renderTemplateWithFrenchLocale() throws Exception {
		Map<String, Object> model = new HashMap<>();
		model.put("foo", "Foo");
		String url = "org/springframework/web/servlet/view/script/kotlin/template.kts";
		MockHttpServletResponse response = render(url, model, Locale.FRENCH, ScriptTemplatingConfiguration.class);
		assertThat(response.getContentAsString()).isEqualTo("<html><body>\n<p>Bonjour Foo</p>\n</body></html>");
	}

	@Test
	void renderTemplateWithEnglishLocale() throws Exception {
		Map<String, Object> model = new HashMap<>();
		model.put("foo", "Foo");
		String url = "org/springframework/web/servlet/view/script/kotlin/template.kts";
		MockHttpServletResponse response = render(url, model, Locale.ENGLISH, ScriptTemplatingConfiguration.class);
		assertThat(response.getContentAsString()).isEqualTo("<html><body>\n<p>Hello Foo</p>\n</body></html>");
	}

	@Test
	void renderTemplateWithoutRenderFunction() throws Exception {
		Map<String, Object> model = new HashMap<>();
		model.put("header", "<html><body>");
		model.put("hello", "Hello");
		model.put("foo", "Foo");
		model.put("footer", "</body></html>");
		MockHttpServletResponse response = render("org/springframework/web/servlet/view/script/kotlin/eval.kts",
				model, Locale.ENGLISH, ScriptTemplatingConfigurationWithoutRenderFunction.class);
		assertThat(response.getContentAsString()).isEqualTo("<html><body>\n<p>Hello Foo</p>\n</body></html>");
	}


	private static MockHttpServletResponse render(String viewUrl, Map<String, Object> model,
			Locale locale, Class<?> configuration) throws Exception {

		ScriptTemplateView view = createViewWithUrl(viewUrl, configuration);
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addPreferredLocale(locale);
		view.renderMergedOutputModel(model, request, response);
		return response;
	}

	private static ScriptTemplateView createViewWithUrl(String viewUrl, Class<?> configuration) throws Exception {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(configuration);

		ScriptTemplateView view = new ScriptTemplateView();
		view.setApplicationContext(ctx);
		view.setUrl(viewUrl);
		view.afterPropertiesSet();
		return view;
	}


	@Configuration
	static class ScriptTemplatingConfiguration {

		@Bean
		ScriptTemplateConfigurer kotlinScriptConfigurer() {
			ScriptTemplateConfigurer configurer = new ScriptTemplateConfigurer();
			configurer.setEngineName("kotlin");
			configurer.setScripts("org/springframework/web/servlet/view/script/kotlin/render.kts");
			configurer.setRenderFunction("render");
			return configurer;
		}

		@Bean
		ResourceBundleMessageSource messageSource() {
			ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
			messageSource.setBasename("org/springframework/web/servlet/view/script/messages");
			return messageSource;
		}
	}

	@Configuration
	static class ScriptTemplatingConfigurationWithoutRenderFunction {

		@Bean
		ScriptTemplateConfigurer kotlinScriptConfigurer() {
			return new ScriptTemplateConfigurer("kotlin");
		}
	}

}
