package com.sahasrara.takehome.data.login;

import com.sahasrara.takehome.data.Response;
import com.yubico.u2f.data.messages.SignRequestData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Login Challenge Response Object.
 * The server sends this to the client after the client has requested challenges for users during a login.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginChallengeResponse implements Response {
    private String loginGroupId;
    private List<ChallengeData> loginResponses;

    @Getter
    @AllArgsConstructor
    public static class ChallengeData {
        private String username;
        private SignRequestData signRequestData;
    }
}
