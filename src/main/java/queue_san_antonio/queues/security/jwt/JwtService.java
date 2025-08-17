package queue_san_antonio.queues.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import queue_san_antonio.queues.models.Empleado;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret:bXVuaWNpcGFsaWRhZF9zYW5fYW50b25pb19zZWNyZXRfa2V5XzIwMjRfcXVldWVfc3lzdGVt}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // 24 horas en milisegundos
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration:604800000}") // 7 días en milisegundos
    private long refreshExpiration;

    //Extrae el username del token JWT
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    //Extrae un claim específico del token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    //Extrae el ID del empleado del token
    public Long extractEmpleadoId(String token) {
        return extractClaim(token, claims -> claims.get("empleadoId", Long.class));
    }

    //Extrae el rol del empleado del token
    public String extractRol(String token) {
        return extractClaim(token, claims -> claims.get("rol", String.class));
    }

    //Extrae el ID del sector del empleado del token
    public Long extractSectorId(String token) {
        return extractClaim(token, claims -> claims.get("sectorId", Long.class));
    }

    //Genera un token JWT para un empleado
    public String generateToken(Empleado empleado) {
        return generateToken(new HashMap<>(), empleado);
    }

    //Genera un token JWT con claims adicionales
    public String generateToken(Map<String, Object> extraClaims, Empleado empleado) {
        return buildToken(extraClaims, empleado, jwtExpiration);
    }

    //Genera un token de refresh
    public String generateRefreshToken(Empleado empleado) {
        return buildToken(new HashMap<>(), empleado, refreshExpiration);
    }

    //Construye el token JWT
    private String buildToken(Map<String, Object> extraClaims, Empleado empleado, long expiration) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .claims(extraClaims)
                .subject(empleado.getUsername())
                .claim("empleadoId", empleado.getId())
                .claim("rol", empleado.getRol().name())
                .claim("sectorId", empleado.getSector() != null ? empleado.getSector().getId() : null)
                .claim("nombreCompleto", empleado.getNombreCompleto())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiration))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    //Verifica si el token es válido para el usuario
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage());
            return false;
        }
    }

    //Verifica si el token ha expirado
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    //Extrae la fecha de expiración del token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    //Extrae todos los claims del token
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("Token JWT expirado: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("Token JWT no soportado: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("Token JWT malformado: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.error("Firma del token JWT inválida: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("Token JWT vacío o nulo: {}", e.getMessage());
            throw e;
        }
    }

    //Obtiene la clave de firma para el JWT
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //Valida solo la estructura del token sin verificar expiración
    public boolean isTokenStructureValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // Token expirado pero estructuralmente válido
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //Obtiene el tiempo restante de vida del token en minutos
    public long getTokenRemainingMinutes(String token) {
        try {
            Date expiration = extractExpiration(token);
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remaining / (1000 * 60)); // Convertir a minutos
        } catch (Exception e) {
            return 0;
        }
    }
}