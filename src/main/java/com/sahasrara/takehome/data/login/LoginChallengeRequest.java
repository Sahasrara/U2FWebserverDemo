package com.sahasrara.takehome.data.login;

import com.sahasrara.takehome.data.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Login Challenge Request Object.
 * The server receives this from the client when the client is requesting challenges for users to start a login.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginChallengeRequest implements Request {
    private List<String> usernames;
}
