package com.tecnocampus.LS2.protube_back.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "ProTube API",
                version = "1.0.0",
                description = "API de autenticación, perfiles, vídeos, comentarios, analíticas y funcionalidades sociales.",
                contact = @Contact(name = "Equipo ProTube", email = "soporte@example.com"),
                license = @License(name = "Proyecto educativo")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Desarrollo local"),
                @Server(url = "http://localhost", description = "Docker Compose")
        },
        security = @SecurityRequirement(name = "sessionCookie")
)
@SecurityScheme(
        name = "sessionCookie",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.COOKIE,
        paramName = "JSESSIONID",
        description = "Cookie de sesión creada por /api/auth/login o /api/auth/register"
)
public class OpenApiConfig {
}

