package jag.oasipbackend.services;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {


    private Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id, 16, 16);

    public PasswordService() {
    }

    public String securePassword(String password){
//        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id, 32, 64);
//        argon2.hash(10, 65536, 1, password.toCharArray());
        try{
             return argon2.hash(22, 65536, 1, password.toCharArray());
        }finally {
            argon2.wipeArray(password.toCharArray());
        }
    }

//    public boolean validatePassword(String hash, String password){
//        return argon2.verify(hash, password.toCharArray());
//    }

//    private Argon2 getArgon2Instance() {
//        Argon2Factory.Argon2Types type = Argon2Factory.Argon2Types.ARGON2d;
//        if(passwordConfig.getType() == 1){
//            type = Argon2Factory.Argon2Types.ARGON2i;
//        }else if(passwordConfig.getType() == 2){
//            type = Argon2Factory.Argon2Types.ARGON2id;
//        }
//        return Argon2Factory.create(type, passwordConfig.getSaltLength(), passwordConfig.getHashLength());
//    }
}
