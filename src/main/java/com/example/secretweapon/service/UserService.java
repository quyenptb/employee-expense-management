package com.example.secretweapon.service;

import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.repository.UserRepositoryImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface UserService {

    //Create
    User createUser(User user);

    //Read
    Optional<User> getUserById(long id);

    //Read
    List<User> getAllUsers();

    //Update
    User updateUser(long id, User user);

    //Delete
    void deleteUser(long id);


}
