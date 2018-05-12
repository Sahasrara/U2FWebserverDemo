package com.sahasrara.takehome.db;

import com.sahasrara.takehome.data.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Super simple database to hold user login information.
 */
public interface LoginDatabase {
    /**
     * Fetch a user from the database by username.
     * @param username username
     * @return user if present
     */
    Optional<User> fetchUser(String username);

    /**
     * Fetch a list of users from the database by username;
     * @param usernames usernames
     * @return list of users if present
     */
    List<User> fetchUsers(Collection<String> usernames);

    /**
     * Fetches all users in a given login group.
     * @param loginGroupId login group id
     * @return users in the login group id
     */
    List<User> fetchAllUsersInGroup(String loginGroupId);

    /**
     * Save users to the database.
     * @param users user objects
     */
    void saveUsers(Collection<User> users);

    /**
     * Update users in the database.
     * @param users user objects
     */
    void updateUsers(Collection<User> users);
}
