package com.grade.system.service;

import com.grade.system.dto.LoginRequest;
import com.grade.system.dto.LoginResponse;
import com.grade.system.dto.PageResponse;
import com.grade.system.entity.User;
import com.grade.system.enums.ErrorCode;
import com.grade.system.exception.ResourceNotFoundException;
import com.grade.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername() == null ? null : request.getUsername().trim();
        String password = request.getPassword() == null ? null : request.getPassword().trim();
        Optional<User> userOpt = username == null ? Optional.empty() : userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (password != null) {
                boolean passwordMatches = false;
                
                if (isBCryptHash(user.getPassword())) {
                    passwordMatches = passwordEncoder.matches(password, user.getPassword());
                } else {
                    passwordMatches = user.getPassword().equals(password);
                    if (passwordMatches) {
                        user.setPassword(passwordEncoder.encode(password));
                        userRepository.save(user);
                    }
                }
                
                if (passwordMatches) {
                    LoginResponse response = new LoginResponse();
                    response.setId(user.getId());
                    response.setUsername(user.getUsername());
                    response.setRole(user.getRole());
                    response.setName(user.getName());
                    response.setClassName(user.getClassName());
                    return response;
                }
            }
        }
        return null;
    }
    
    private boolean isBCryptHash(String password) {
        return password != null && (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public PageResponse<User> getUsersPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<User> userPage = userRepository.findAll(pageable);
        
        PageResponse<User> response = new PageResponse<>();
        response.setContent(userPage.getContent());
        response.setPageNumber(userPage.getNumber());
        response.setPageSize(userPage.getSize());
        response.setTotalElements(userPage.getTotalElements());
        response.setTotalPages(userPage.getTotalPages());
        response.setFirst(userPage.isFirst());
        response.setLast(userPage.isLast());
        return response;
    }
    
    public User createUser(User user) {
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode("123456"));
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }
    
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id).orElseThrow(() -> 
            new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        if (userDetails.getName() != null) user.setName(userDetails.getName());
        if (userDetails.getContact() != null) user.setContact(userDetails.getContact());
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        if (userDetails.getClassName() != null) {
            user.setClassName(userDetails.getClassName());
        }
        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> 
            new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
    }
}
