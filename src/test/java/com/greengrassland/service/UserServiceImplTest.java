package com.greengrassland.service;

import com.greengrassland.dto.*;
import com.greengrassland.entity.User;
import com.greengrassland.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Test
    void testRegister() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("testuser");
        dto.setPassword("123456");
        dto.setNickname("TestUser");
        UserDTO result = userService.register(dto);
        assertNotNull(result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testRegisterDuplicate() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("duplicate");
        dto.setPassword("123456");
        userService.register(dto);
        assertThrows(Exception.class, () -> userService.register(dto));
    }

    @Test
    void testLogin() {
        UserRegisterDTO reg = new UserRegisterDTO();
        reg.setUsername("loginuser");
        reg.setPassword("123456");
        userService.register(reg);

        UserLoginDTO login = new UserLoginDTO();
        login.setUsername("loginuser");
        login.setPassword("123456");
        UserDTO result = userService.login(login);
        assertEquals("loginuser", result.getUsername());
    }

    @Test
    void testLoginWrongPassword() {
        UserRegisterDTO reg = new UserRegisterDTO();
        reg.setUsername("pwduser");
        reg.setPassword("123456");
        userService.register(reg);

        UserLoginDTO login = new UserLoginDTO();
        login.setUsername("pwduser");
        login.setPassword("wrong");
        assertThrows(Exception.class, () -> userService.login(login));
    }

    @Test
    void testGetUserById() {
        UserRegisterDTO reg = new UserRegisterDTO();
        reg.setUsername("getuser");
        reg.setPassword("123456");
        UserDTO created = userService.register(reg);

        UserDTO result = userService.getUserById(created.getId());
        assertEquals("getuser", result.getUsername());
    }

    @Test
    void testUpdateProfile() {
        UserRegisterDTO reg = new UserRegisterDTO();
        reg.setUsername("profileuser");
        reg.setPassword("123456");
        UserDTO created = userService.register(reg);

        UserUpdateDTO update = new UserUpdateDTO();
        update.setNickname("NewName");
        update.setBio("Hello world");
        update.setInterestTags("basketball,music");
        UserDTO result = userService.updateProfile(created.getId(), update);
        assertEquals("NewName", result.getNickname());
        assertEquals("Hello world", result.getBio());
        assertEquals("basketball,music", result.getInterestTags());
    }

    @Test
    void testUpdatePassword() {
        UserRegisterDTO reg = new UserRegisterDTO();
        reg.setUsername("chpwd");
        reg.setPassword("oldpwd");
        UserDTO created = userService.register(reg);

        UserPasswordDTO pwd = new UserPasswordDTO();
        pwd.setOldPassword("oldpwd");
        pwd.setNewPassword("newpwd");
        userService.updatePassword(created.getId(), pwd);

        UserLoginDTO login = new UserLoginDTO();
        login.setUsername("chpwd");
        login.setPassword("newpwd");
        UserDTO result = userService.login(login);
        assertNotNull(result);
    }
}
