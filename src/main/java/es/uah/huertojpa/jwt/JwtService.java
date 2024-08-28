package es.uah.huertojpa.jwt;

import es.uah.huertojpa.persona.dominio.entidades.Persona;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Service
public class JwtService {

    public JwtService() {
    }

    private static final String SECRET_KEY ="Y29udHJhc2VuaWFhbXV5c2VndXJhY29udHJhc2VuaWFhbXV5c2VndXJhY29udHJhc2VuaWFhbXV5c2VndXJh";
    public String getToken(UserDetails persona){
        return getToken(new HashMap<>(),persona);
    }

    public String getToken(Map<String, Object> claims, UserDetails persona) {
        return Jwts.builder().setClaims(claims)
                .setSubject(persona.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+1000*60*24))
                .signWith(SignatureAlgorithm.HS256,getkey()) .compact();
    }

    public Key getkey() {
        byte[] keyBytes= Decoders.BASE64.decode(SECRET_KEY);

        return Keys.hmacShaKeyFor(keyBytes) ;

    }
    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username=getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername())&& !isTokenExpired(token));
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
