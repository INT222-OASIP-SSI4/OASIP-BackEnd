package jag.oasipbackend.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse implements Serializable {

//    private static final long serialVersionUID = -8091879091924046844L;
//    private final String jwttoken;
      private String jwttoken;

//    public JwtResponse(String jwttoken) {
//        this.jwttoken = jwttoken;
//    }
//
//    public String getToken() {
//        return this.jwttoken;
//    }
}