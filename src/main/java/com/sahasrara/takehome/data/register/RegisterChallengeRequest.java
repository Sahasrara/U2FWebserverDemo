package com.sahasrara.takehome.data.register;

import com.sahasrara.takehome.data.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Registration Challenge Request Object.
 * The server receives this from the client when the client is requesting challenges for users to start a registration.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterChallengeRequest implements Request {
    private List<String> usernames;
}
