package com.fan.screenadmin.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long TOKEN_TTL_SECONDS = 12 * 60 * 60;

    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Map<String, Instant> tokens = new ConcurrentHashMap<>();

    public AuthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        ensureDefaultUser();
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null || isBlank(request.account()) || isBlank(request.password())) {
            throw new IllegalArgumentException("账号和密码不能为空");
        }

        var users = jdbcTemplate.queryForList(
                "SELECT id, username, password_hash, display_name FROM admin_user WHERE username = ?",
                request.account()
        );
        if (users.isEmpty() || !passwordEncoder.matches(request.password(), Objects.requireNonNull(users.getFirst().get("password_hash")).toString())) {
            throw new IllegalArgumentException("账号或密码不正确");
        }

        var user = users.getFirst();
        String token = newToken();
        tokens.put(token, Instant.now().plusSeconds(TOKEN_TTL_SECONDS));
        return new LoginResponse(
                token,
                Objects.requireNonNull(user.get("id")).toString(),
                Objects.requireNonNull(user.get("username")).toString(),
                Objects.requireNonNull(user.get("display_name")).toString()
        );
    }

    public boolean isValidToken(String token) {
        Instant expiry = tokens.get(token);
        if (expiry == null) {
            return false;
        }
        if (Instant.now().isAfter(expiry)) {
            tokens.remove(token);
            return false;
        }
        return true;
    }

    public void logout(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }

    private void ensureDefaultUser() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM admin_user", Integer.class);
        if (count != null && count > 0) {
            return;
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO admin_user(username, password_hash, password_salt, display_name) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, "admin");
            statement.setString(2, passwordEncoder.encode("admin123"));
            statement.setString(3, randomToken());
            statement.setString(4, "管理员");
            return statement;
        }, keyHolder);
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String randomToken() {
        return newToken().substring(0, 32);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record LoginRequest(String account, String password) {
    }

    public record LoginResponse(String token, String userId, String account, String name) {
    }
}
