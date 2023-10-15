package com.danielaassis.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.danielaassis.todolist.user.IUserRepository;
import com.danielaassis.todolist.user.UserModel;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.BCrypt.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String serveletPath = request.getServletPath();

        if(serveletPath.startsWith("/tasks/")) {
            // Autenticação
            String authorization = request.getHeader("Authorization");
            authorization = authorization.substring("Basic".length()).trim();
            byte[] authDecoded = Base64.getDecoder().decode(authorization);
            String authString = new String(authDecoded);
            String [] credentials = authString.split(":");
            String user = credentials[0];
            String password = credentials[1];

            // Validação usuário
            UserModel userModel = this.userRepository.findByUserName(user);
            if(userModel == null) {
                response.sendError(401);
            } else {
                // Validação senha
                Result passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), userModel.getPassword());
                if(passwordVerify.verified) {
                    request.setAttribute("idUser", userModel.getId());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401);
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }

    }
}
