package com.lexia.backend.email.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Thymeleaf configuration for email templates.
 * Configures a dedicated TemplateEngine with email-specific MessageSource
 * to properly resolve i18n messages in email templates.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Configuration
public class EmailThymeleafConfig {

    /**
     * Creates a template resolver for email templates.
     *
     * @return configured SpringResourceTemplateResolver
     */
    @Bean
    public SpringResourceTemplateResolver emailTemplateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setPrefix("classpath:/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false); // Set to true in production
        templateResolver.setOrder(1);
        templateResolver.setCheckExistence(true);
        return templateResolver;
    }

    /**
     * Creates a SpringTemplateEngine for email templates with proper MessageSource.
     * This bean uses the emailMessageSource for i18n resolution in email templates.
     *
     * @param templateResolver the email template resolver
     * @param emailMessageSource the message source for email i18n
     * @return configured SpringTemplateEngine
     */
    @Bean(name = "emailTemplateEngine")
    public SpringTemplateEngine emailTemplateEngine(
            SpringResourceTemplateResolver emailTemplateResolver,
            @Qualifier("emailMessageSource") MessageSource emailMessageSource) {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(emailTemplateResolver);
        templateEngine.setTemplateEngineMessageSource(emailMessageSource);
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
    }
}
