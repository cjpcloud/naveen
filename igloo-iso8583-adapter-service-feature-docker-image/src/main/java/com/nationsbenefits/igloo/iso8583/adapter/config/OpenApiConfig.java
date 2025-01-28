package com.nationsbenefits.igloo.iso8583.adapter.config;

import com.nationsbenefits.igloo.iso8583.adapter.constant.ISOAdapterConstant;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * <h1>OpenApiConfig</h1>
 * This OpenApiConfig is a config class used for swagger config
 *
 * @author PwC
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 *
 */

@EnableWebMvc
@OpenAPIDefinition(info = @Info(title = ISOAdapterConstant.SWAGGER_TITLE, version = ISOAdapterConstant.SWAGGER_VERSION,
        description = ISOAdapterConstant.SWAGGER_DESCRIPTION))
@Configuration
public class OpenApiConfig {

}
