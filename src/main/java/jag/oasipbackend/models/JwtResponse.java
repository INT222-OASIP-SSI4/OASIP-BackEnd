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

      private String message;
      private String jwttoken;
      private String refreshToken;

      public JwtResponse(String jwttoken) {
            this.jwttoken = jwttoken;
      }
}