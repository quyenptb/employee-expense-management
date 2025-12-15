package com.example.secretweapon.service;

import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.payload.request.UserUpdateRequest;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface UserService {


    //Read
    Optional<User> getUserById(long id);

    //Read
    List<User> getAllUsers();

    User getUserByEmailAddress(String email);



    //Update
    User updateUser(UserUpdateRequest userUpdate);

    //Delete
    void deleteUser(long id);


}
