package com.movieflix.auth.services;

import com.movieflix.auth.entities.RefreshToken;
import com.movieflix.auth.entities.User;
import com.movieflix.auth.repositories.RefreshTokenRepository;
import com.movieflix.auth.repositories.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
@Service
public class RefreshTokenService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository) {

        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // create or replace refresh token
    public RefreshToken createRefreshToken(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email : " + email));

        RefreshToken refreshToken = user.getRefreshToken();

        // check both null and expired
        if (refreshToken == null ||
                refreshToken.getExpirationTime().isBefore(Instant.now())) {

            if (refreshToken != null) {
                refreshTokenRepository.delete(refreshToken);
            }

            refreshToken = RefreshToken.builder()
                    .refreshToken(UUID.randomUUID().toString())
                    .expirationTime(
                            Instant.now().plus(Duration.ofDays(7))
                    )
                    .user(user)
                    .build();

            refreshTokenRepository.save(refreshToken);
        }

        return refreshToken;
    }

    //  validate refresh token
    public RefreshToken verifyRefreshToken(String refreshTokenValue) {

        RefreshToken refreshToken =
                refreshTokenRepository.findByRefreshToken(refreshTokenValue)
                        .orElseThrow(() ->
                                new RuntimeException("Refresh token not found"));

        if (refreshToken.getExpirationTime().isBefore(Instant.now())) {

            refreshTokenRepository.delete(refreshToken);

            throw new RuntimeException("Refresh token expired");
        }

        return refreshToken;
    }
}
