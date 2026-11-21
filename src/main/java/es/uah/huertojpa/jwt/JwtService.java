package es.uah.huertojpa.jwt;


import es.uah.huertojpa.persona.dominio.entidades.Persona;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

import java.security.Key;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    /** S1: la clave ya no está en el código. Base64 de al menos 32 bytes (HS256). */
    private final Key key;
    private final long expiracionMs;

    public JwtService(@Value("${jwt.secret:}") String secretoBase64,
                      @Value("${jwt.expiration-hours:24}") long horasExpiracion) {
        this.key = Keys.hmacShaKeyFor(bytesClave(secretoBase64));
        this.expiracionMs = 1000L * 60 * 60 * horasExpiracion;
    }

    private static byte[] bytesClave(String secretoBase64) {
        if (secretoBase64 == null || secretoBase64.isBlank()) {
            // Arranque sin JWT_SECRET (desarrollo): clave aleatoria. Los tokens dejan de valer al reiniciar.
            log.warn("jwt.secret (JWT_SECRET) no configurado: se usa una clave aleatoria temporal");
            byte[] aleatoria = new byte[32];
            new SecureRandom().nextBytes(aleatoria);
            return aleatoria;
        }
        byte[] bytes = Decoders.BASE64.decode(secretoBase64.trim());
        if (bytes.length < 32) {
            throw new IllegalStateException("jwt.secret debe tener al menos 32 bytes (genera uno con: openssl rand -base64 48)");
        }
        return bytes;
    }


    public String getToken(UserDetails persona){
        return getToken(new HashMap<>(),persona);
    }

    public String getToken(Map<String, Object> claims, UserDetails persona) {
        return Jwts.builder().setClaims(claims)
                .setSubject(persona.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+expiracionMs))
                .signWith(SignatureAlgorithm.HS256,getkey()) .compact();
    }

    public Key getkey() {
        return key;
    }
    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username=getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername())&& !isTokenExpired(token)
                && !emitidoAntesDelCambioDeContrasena(token, userDetails));
    }

    /**
     * S8/S9: un token emitido antes del último cambio de contraseña (propio o restablecida por el admin) o del último
     * «cerrar sesión en todos los equipos» ya no vale: quien tuviera la sesión abierta la pierde. {@code iat} va en
     * segundos, así que se compara a segundos: un token del mismo segundo que el cambio se acepta (es el que se
     * devuelve en la propia respuesta del cambio).
     */
    private boolean emitidoAntesDelCambioDeContrasena(String token, UserDetails userDetails) {
        if (!(userDetails instanceof Persona persona) || persona.tokensValidosDesde() == null) {
            return false;
        }
        Date emitido = getClaim(token, Claims::getIssuedAt);
        if (emitido == null) {
            return true;
        }
        long desde = persona.tokensValidosDesde().atZone(ZoneId.systemDefault()).toEpochSecond();
        return emitido.getTime() / 1000 < desde;
    }

    private Claims getAllClaims(String token)
    {
        return Jwts
                .parserBuilder()
                .setSigningKey(this.getkey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T getClaim(String token, Function<Claims,T> claimsResolver)
    {
        final Claims claims=getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Date getExpiration(String token)
    {
        return getClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token)
    {
        return getExpiration(token).before(new Date());
    }

}
