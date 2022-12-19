package jag.oasipbackend.configurations;

import java.io.IOException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.jsonwebtoken.*;
import jag.oasipbackend.services.JwtUserDetailsService;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    public String secret;

    @Value("${jwt.secret}")
    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Getter
    @Setter
    String jwtToken;

    @Setter
    @Getter
    UserDetails userDetails;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");
        String username = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            setJwtToken(requestTokenHeader.substring(7));
            try {
                JSONObject payload = null;
                if(getJwtToken()!=null){
                    payload = extractMSJwt(getJwtToken());
                }
                if(StringUtils.hasText(getJwtToken()) == true &&
                        payload.getString("iss").equals("https://login.microsoftonline.com/6f4432dc-20d2-441d-b1db-ac3380ba633d/v2.0")){
                    username = payload.getString("preferred_username");
                    if(payload.has("roles")==false){
                        setUserDetails(new User(payload.getString("preferred_username"),"", Arrays.asList(new SimpleGrantedAuthority("ROLE_guest"))));
                        setJwtToken(jwtTokenUtil.generateToken(getUserDetails()));
                    }else{
                        String role = payload.getString("roles");
                        String extract = role.replaceAll("[^a-zA-Z]+", "");
                        setUserDetails( new User(payload.getString("preferred_username"),"",Arrays.asList(new SimpleGrantedAuthority("ROLE_"+extract.toLowerCase()))));
                        setJwtToken(jwtTokenUtil.generateToken(getUserDetails()));
                    }

                }else if (StringUtils.hasText(getJwtToken()) == true && payload.getString("iss").equals("https://intproj21.sit.kmutt.ac.th/or2/")) {
                    username = payload.getString("sub");
                    setUserDetails(this.jwtUserDetailsService.loadUserByUsername(username));
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                System.out.println("JWT Token has expired");
            } catch (JSONException e) {
                System.out.println("JSON Exception" + e.getMessage());
            }
        } else {
            logger.warn("JWT Token does not begin with Bearer String");
        }

        // Once we get the token validate it.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

//            UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);

            // if token is valid configure Spring Security to manually set
            // authentication
            if (jwtTokenUtil.validateToken(getJwtToken(), getUserDetails())) {

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                String authorities = getUserDetails().getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }

    @SneakyThrows
    public JSONObject extractMSJwt(String token) {
        String[] chunks = token.split("\\.");

        JSONObject header = new JSONObject(decode(chunks[0]));
        JSONObject payload = new JSONObject(decode(chunks[1]));
        String signature = decode(chunks[2]);
        if (payload.getString("iss").equals("https://login.microsoftonline.com/6f4432dc-20d2-441d-b1db-ac3380ba633d/v2.0")) {
            System.out.println("BEFORE CONFIG");

            DecodedJWT jwt = JWT.decode(token);
            JwkProvider provider = new UrlJwkProvider(new URL("https://login.microsoftonline.com/common/discovery/keys"));
            Jwk jwk = provider.get(jwt.getKeyId());
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            algorithm.verify(jwt);

            System.out.println("AFTER CONFIG");
        }
        System.out.println("PAYLOAD : " + payload);
        return payload;
    }

    private static String decode(String encodedString) {
        return new String(Base64.getUrlDecoder().decode(encodedString));
    }

}