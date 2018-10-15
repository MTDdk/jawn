package net.javapla.jawn.core.templates;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.configuration.DeploymentInfo;
import net.javapla.jawn.core.templates.TemplateEngine.TemplateRenderEngine;

public class ContentTemplateLoaderTest {
	
	
	@Test
	public void handleLayoutEndings() {
		
		@SuppressWarnings("unchecked")
		TemplateRenderEngine<String> treMock = mock(TemplateEngine.TemplateRenderEngine.class);
		
		when(treMock.getSuffixOfTemplatingEngine()).thenReturn(".st");
		assertEquals("test.html", new ContentTemplateLoader<>(mock(DeploymentInfo.class), treMock).getLayoutNameForResult(new Result(200).layout("test.st")));
		when(treMock.getSuffixOfTemplatingEngine()).thenReturn("st");
		assertEquals("test.html", new ContentTemplateLoader<>(mock(DeploymentInfo.class), treMock).getLayoutNameForResult(new Result(200).layout("test.st")));
		when(treMock.getSuffixOfTemplatingEngine()).thenReturn(".test");
		assertEquals("test.html", new ContentTemplateLoader<>(mock(DeploymentInfo.class), treMock).getLayoutNameForResult(new Result(200).layout("test.test")));
		when(treMock.getSuffixOfTemplatingEngine()).thenReturn(".st");
		assertEquals("test.html", new ContentTemplateLoader<>(mock(DeploymentInfo.class), treMock).getLayoutNameForResult(new Result(200).layout("test.html.st")));

	}

}
