package com.example.ciscarpool.interfaces;

import com.example.ciscarpool.classes.User;
import java.util.ArrayList;

/**
 * {@link LeaderboardCallback} is an interface to wait for Firebase Firestore response for the
 * {@link com.example.ciscarpool.fragments.LeaderboardFragment}
 *
 * @author Eric Wu
 * @version 1.0
 * **/
public interface LeaderboardCallback {
    void leaderboardCallback(ArrayList<User> result);
}
