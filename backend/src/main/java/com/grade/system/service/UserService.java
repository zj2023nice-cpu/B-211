package com.grade.system.service;

import com.grade.system.dto.LoginRequest;
import com.grade.system.dto.LoginResponse;
import com.grade.system.dto.PageResponse;
import com.grade.system.dto.UserImportResult;
import com.grade.system.entity.User;
import com.grade.system.enums.ErrorCode;
import com.grade.system.exception.ResourceNotFoundException;
import com.grade.system.repository.UserRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    private static final Set<String> VALID_ROLES = new HashSet<>(Arrays.asList("ADMIN", "TEACHER", "HEAD_TEACHER", "STUDENT"));
    private static final Set<String> ROLES_REQUIRE_CLASS = new HashSet<>(Arrays.asList("STUDENT", "HEAD_TEACHER"));

    @Transactional
    public UserImportResult importUsersFromCsv(MultipartFile file) {
        UserImportResult result = new UserImportResult();
        List<User> usersToSave = new ArrayList<>();
        Set<String> processedUsernames = new HashSet<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String[]> allRows = reader.readAll();

            if (allRows.isEmpty()) {
                result.setTotal(0);
                result.setSuccessCount(0);
                result.setFailCount(0);
                result.getErrors().add(new UserImportResult.ImportError(1, "", "", "CSV文件为空"));
                return result;
            }

            String[] header = allRows.get(0);
            List<String[]> dataRows = allRows.subList(1, allRows.size());

            result.setTotal(dataRows.size());

            for (int i = 0; i < dataRows.size(); i++) {
                String[] row = dataRows.get(i);
                int rowNumber = i + 2;

                try {
                    if (row.length < 5) {
                        throw new IllegalArgumentException("行数据不完整，至少需要5列：用户名、姓名、角色、班级、联系方式");
                    }

                    String username = row[0].trim();
                    String name = row[1].trim();
                    String role = row[2].trim();
                    String className = row[3].trim();
                    String contact = row[4].trim();

                    if (username.isEmpty()) {
                        throw new IllegalArgumentException("用户名不能为空");
                    }
                    if (name.isEmpty()) {
                        throw new IllegalArgumentException("姓名不能为空");
                    }
                    if (role.isEmpty()) {
                        throw new IllegalArgumentException("角色不能为空");
                    }
                    if (!VALID_ROLES.contains(role)) {
                        throw new IllegalArgumentException("角色非法，必须是 ADMIN、TEACHER、HEAD_TEACHER、STUDENT 之一");
                    }
                    if (ROLES_REQUIRE_CLASS.contains(role) && className.isEmpty()) {
                        throw new IllegalArgumentException("角色为 " + role + " 时班级字段不能为空");
                    }

                    if (processedUsernames.contains(username)) {
                        throw new IllegalArgumentException("文件中存在重复的用户名");
                    }
                    if (userRepository.findByUsername(username).isPresent()) {
                        throw new IllegalArgumentException("用户名已存在");
                    }

                    processedUsernames.add(username);

                    User user = new User();
                    user.setUsername(username);
                    user.setName(name);
                    user.setRole(role);
                    user.setClassName(className.isEmpty() ? null : className);
                    user.setContact(contact.isEmpty() ? null : contact);
                    user.setPassword(passwordEncoder.encode("123456"));

                    usersToSave.add(user);
                    result.setSuccessCount(result.getSuccessCount() + 1);

                } catch (Exception e) {
                    result.setFailCount(result.getFailCount() + 1);
                    String usernameVal = row.length > 0 ? row[0].trim() : "";
                    String nameVal = row.length > 1 ? row[1].trim() : "";
                    result.getErrors().add(new UserImportResult.ImportError(
                        rowNumber, usernameVal, nameVal, e.getMessage()
                    ));
                }
            }

            if (!usersToSave.isEmpty()) {
                userRepository.saveAll(usersToSave);
            }

        } catch (IOException | CsvException e) {
            result.setFailCount(result.getFailCount() + 1);
            result.getErrors().add(new UserImportResult.ImportError(
                0, "", "", "文件读取失败：" + e.getMessage()
            ));
        }

        return result;
    }
}
