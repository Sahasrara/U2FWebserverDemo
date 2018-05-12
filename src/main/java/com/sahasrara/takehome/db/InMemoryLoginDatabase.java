package com.sahasrara.takehome.db;

import com.sahasrara.takehome.data.User;

import javax.ws.rs.BadRequestException;
import java.util.*;

/**
 * In-memory implementation of LoginDatabase.
 */
public class InMemoryLoginDatabase implements LoginDatabase {
    private final Map<String, User> database;

    public InMemoryLoginDatabase() {
        this.database = new HashMap<>();
    }

    @Override
    public synchronized Optional<User> fetchUser(String username) {
        return Optional.ofNullable(database.get(username));
    }

    @Override
    public synchronized List<User> fetchUsers(Collection<String> usernames) {
        List<User> users = new LinkedList<>();
        for (String username : usernames) {
            if (database.containsKey(username)) {
                users.add(database.get(username));
            }
        }
        return users;
    }

    @Override
    public synchronized List<User> fetchAllUsersInGroup(String loginGroupId) {
        List<User> users = new LinkedList<>();
        // being lazy again not normalization, indexing, etc.
        for (Map.Entry<String, User> userEntry : database.entrySet()) {
            if (userEntry.getValue().getLoginGroupId().equals(loginGroupId))
            users.add(userEntry.getValue());
        }
        return users;
    }

    @Override
    public synchronized void saveUsers(Collection<User> users) {
        for (User user : users) {
            if (database.containsKey(user.getUsername())) {
                throw new BadRequestException("Tried to register the same user twice");
            }
        }
        for (User user : users) {
            database.put(user.getUsername(), user);
        }
    }

    @Override
    public synchronized void updateUsers(Collection<User> users) {
        for (User user : users) {
            database.put(user.getUsername(), user);
        }
    }
}
