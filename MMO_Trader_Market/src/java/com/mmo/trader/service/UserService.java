package com.mmo.trader.service;

import com.mmo.trader.dao.UserDAO;
import com.mmo.trader.model.User;

/**
 * Business operations around user accounts and authentication.
 */
public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public User authenticate(String username, String password) {
        return userDAO.findByCredentials(username, password);
    }
}
