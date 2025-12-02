package org.example.fronted.observer;

import org.example.fronted.models.User;

public interface SessionObserver {
    void onUserLoggedIn(User user);
    void onUserLoggedOut();
    void onSessionUpdated(User user);
}