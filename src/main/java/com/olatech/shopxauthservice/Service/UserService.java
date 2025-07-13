package com.olatech.shopxauthservice.Service;

import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Repository.UserRepo;
import com.olatech.shopxauthservice.exceptions.UserAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JWTService jwtService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public Users register (Users user) throws Exception {
        if (userRepo.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("email", "Email already in use");
        }
        if (userRepo.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException("username", "Username already in use");
        }
        System.out.println(user.toString());
        user.setPassword(encoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    public String verifyUser(Users user) {


                return jwtService.generateToken(user);
    }

    public Users getUserByUsername(String username) {
        return userRepo.findByUsername(username).orElse(null);
    }
    @Transactional
    public void deletUserByusername(String username) {
        userRepo.deleteByUsername(username);
    }

    public Users updateUser(Users currentUser) {
        return userRepo.save(currentUser);
    }

    public Optional<Users> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public Optional<Users> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }
}
