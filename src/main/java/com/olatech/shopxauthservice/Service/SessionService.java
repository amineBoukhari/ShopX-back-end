package com.olatech.shopxauthservice.Service;


import com.olatech.shopxauthservice.Model.Sessions;
import com.olatech.shopxauthservice.Model.Users;
import com.olatech.shopxauthservice.Repository.SessionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class SessionService {

    @Autowired
    private SessionRepo repo;

    @Autowired
    private JWTService jwtService;


    public Sessions createSession(Sessions session) {
        return repo.save(session);
    }


    public List<Sessions> getAllSessionsByUserId(Users user) {
        return repo.findByUser(user);
    }

    public List<Sessions> getAllSessions() {
        return repo.findAll();
    }

    public Sessions getSessionById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Sessions updateSession(Sessions session) {
        return repo.save(session);
    }

    public void deleteSession(Long id) {
        repo.deleteById(id);
    }
    
    /**
     * Met à jour la date de dernière activité d'une session
     * @param token Le token de la session
     * @return La session mise à jour
     */
    public Sessions updateLastActivityTime(String token) {
        Optional<Sessions> sessionOpt = repo.findByRefreshToken(token);
        if (sessionOpt.isPresent()) {
            Sessions session = sessionOpt.get();
            session.setLastActivityTime(new Date());
            return repo.save(session);
        }
        return null;
    }
    
    /**
     * Supprime toutes les sessions d'un utilisateur sauf celle spécifiée
     * @param user Utilisateur concerné
     * @param currentSessionId ID de la session à conserver
     */
    public void deleteAllSessionsExceptCurrent(Users user, Long currentSessionId) {
        List<Sessions> sessions = repo.findByUser(user);
        for (Sessions session : sessions) {
            if (!session.getId().equals(currentSessionId)) {
                repo.deleteById(session.getId());
            }
        }
    }
    
    /**
     * Trouve une session par son token
     * @param token Token de session
     * @return La session si trouvée
     */
    public Optional<Sessions> findByToken(String token) {
        String tokenId = jwtService.extractTokenId(token);
        System.out.println("Token ID: " + tokenId);
        System.out.println(repo.findByRefreshToken(tokenId).toString());
        return repo.findByRefreshToken(tokenId);
    }
}
