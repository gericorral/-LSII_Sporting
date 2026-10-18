package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import com.tecnocampus.LS2.protube_back.auth.domain.model.User;

import java.lang.reflect.Constructor;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class UserSchemaInitializerTest {
    @Test
    void createsUsersTableIfItDoesNotExist() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        UserSchemaInitializer initializer = new UserSchemaInitializer(jdbcTemplate);

        initializer.initialize();

        verify(jdbcTemplate).execute(contains("CREATE TABLE IF NOT EXISTS users"));
    }

    @Test
    void supportsJpaProtectedConstructor() throws Exception {
        Constructor<User> constructor = User.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertNotNull(constructor.newInstance());
    }
}
