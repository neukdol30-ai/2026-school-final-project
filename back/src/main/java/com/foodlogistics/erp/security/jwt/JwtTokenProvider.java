package com.foodlogistics.erp.security.jwt;

import com.foodlogistics.erp.security.auth.ErpUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class JwtTokenProvider {

    private static final String ISSUER = "food-logistics-erp";
    private final JwtEncoder jwtEncoder;
    private final long accessTokenValiditySeconds;

    public JwtTokenProvider(
            JwtEncoder jwtEncoder,
            @Value(
                    "${security.jwt.access-token-expiration-seconds}"
            ) long accessTokenValiditySeconds

    ) {
        this.jwtEncoder = jwtEncoder;
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
    }

    public String createAccessToken(ErpUserDetails userDetails) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(accessTokenValiditySeconds);
        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        List<String> authorities =
                userDetails.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList();

        JwtClaimsSet claims = JwtClaimsSet
                .builder()
                .issuer(ISSUER)
                .subject(userDetails.getUsername())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("appUserId", userDetails.getAppUserId())
                .claim("companyId", userDetails.getCompanyId())
                .claim("userName", userDetails.getDisplayName())
                .claim("authorities", authorities)
                .build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(
                        header,
                        claims
                ))
                .getTokenValue();
    }

    public long getAccessTokenValiditySeconds() {
        return accessTokenValiditySeconds;
    }
}

