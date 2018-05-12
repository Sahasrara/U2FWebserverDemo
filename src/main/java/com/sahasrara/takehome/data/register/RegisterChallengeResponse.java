package com.sahasrara.takehome.data.register;

import com.sahasrara.takehome.data.Response;
import com.yubico.u2f.data.messages.RegisterRequestData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Registration Challenge Response Object.
 * The server sends this to the client after the client has requested challenges for users.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterChallengeResponse implements Response {
    private String loginGroupId;
    private List<ChallengeData> registerResponses;

    @Getter
    @AllArgsConstructor
    public static class ChallengeData {
        private String username;
        private RegisterRequestData registerRequestData;
    }
}
