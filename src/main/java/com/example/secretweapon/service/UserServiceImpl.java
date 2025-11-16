package com.example.secretweapon.service;

import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.repository.UserRepositoryImpl;

import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService{

    private UserRepositoryImpl userRepository;

    UserServiceImpl(UserRepositoryImpl userRepository) {
        this.userRepository = userRepository;
    }



    @Override
    public User createUser(User user) {

        return null;
    }

    @Override
    public Optional<User> getUserById(long id) {
        return Optional.empty();
    }

    @Override
    public List<User> getAllUsers() {
        return List.of();
    }

    @Override
    public User updateUser(long id, User user) {
        return null;
    }

    @Override
    public void deleteUser(long id) {

    }
}
