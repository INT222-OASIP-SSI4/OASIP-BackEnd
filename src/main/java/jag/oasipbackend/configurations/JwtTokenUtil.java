package jag.oasipbackend.configurations;

import io.jsonwebtoken.*;

import jag.oasipbackend.entities.User;
import jag.oasipbackend.enums.RoleType;
import jag.oasipbackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = -2550185165626007488L;

    public static final long JWT_TOKEN_VALIDITY = 1800;

    public static final long JWT_REFRESHTOKEN_VALIDITY = 86400;

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    UserRepository userRepository;


    //retrieve username from jwt token
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    //retrieve expiration date from jwt token
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    //for retrieveing any information from token we will need the secret key
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    //check if the token has expired
    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    //generate token for user
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        final String authorities = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
        Optional<User> user = userRepository.findByUserEmail(userDetails.getUsername());
        claims.put("Roles", authorities);
        claims.put("UserName",user.get().getUserName());
        return doGenerateToken(claims, userDetails.getUsername());
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return refreshToken(claims, userDetails.getUsername());
    }

    public String refreshToken(Map<String, Object> claims, String subject){
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_REFRESHTOKEN_VALIDITY * 1000))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    //while creating the token -
    //1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
    //2. Sign the JWT using the HS512 algorithm and secret key.
    //3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
    //   compaction of the JWT to a URL-safe string
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setIssuer("https://intproj21.sit.kmutt.ac.th/or2/").setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000)) //set expire กำหนดอายุ token หน่วยมิลลิ
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    //validate token
    public Boolean validateTokenFromMs(String token) {
        try {
            final String username = getUsernameFromToken(token);
            Jws<Claims> claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("INVALID_CREDENTIALS", ex);
        } catch (ExpiredJwtException ex) {
            throw ex;
        }
    }

    //validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String doGenerateAccessToken(Map<String, Object> claims, String email, String role, String name, long time) {
        if(time == 1){
            time = JWT_TOKEN_VALIDITY;
        } else {
            time = JWT_REFRESHTOKEN_VALIDITY;
        }

        claims.put("name", name);
        claims.put("role", role);
        System.out.println(claims);
        return Jwts.builder().setClaims(claims).setSubject(email).setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000)) // 5 ชั่วโมง
                .setExpiration(new Date(System.currentTimeMillis() + time)) // 30 นาที or 1 day
                .signWith(SignatureAlgorithm.HS512, secret).compact();

    }

    public List<SimpleGrantedAuthority> getRolesFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();

        List<SimpleGrantedAuthority> roles = null;

        String role = claims.get("roles", String.class);

        if (role != null) {
            if (role.equals(RoleType.admin.name())) {
                roles = Arrays.asList(new SimpleGrantedAuthority(RoleType.admin.name()));
            } else if (role.equals(RoleType.student.name())) {
                roles = Arrays.asList(new SimpleGrantedAuthority(RoleType.student.name()));
            } else if (role.equals(RoleType.lecturer.name())) {
                roles = Arrays.asList(new SimpleGrantedAuthority(RoleType.lecturer.name()));
            } else if (role.equals("Guest")) {
                roles = Arrays.asList(new SimpleGrantedAuthority("Guest"));
            }

        }
        return roles;

    }

}