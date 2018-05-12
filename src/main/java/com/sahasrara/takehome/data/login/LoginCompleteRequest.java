package com.sahasrara.takehome.data.login;

import com.sahasrara.takehome.data.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Login Complete Request Object.
 * The server receives this when the client has wishes us to verify U2F signatures and verify user credentials.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginCompleteRequest implements Request {
    // Username -> SignatureData
    private Map<String, SignatureData> loginResponses;
    private String loginGroupId;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignatureData {
        private String password;
        // Normally this would just be a SignResponse object, but SignResponse.fromJson does a special length
        // check so we'll use that manually.
        private String challengeSignatureJson;
    }
}
