package com.sahasrara.takehome.data.register;

import com.sahasrara.takehome.data.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Registration Complete Request Object.
 * The server receives this when the client has wishes us to verify U2F signatures and store new user credentials.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterCompleteRequest implements Request {
    // Username -> SignatureData
    private Map<String, SignatureData> registerResponses;
    private String loginGroupId;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignatureData {
        private String password;
        // Normally this would just be a RegisterResponse object, but RegisterResponse.fromJson does a special length
        // check so we'll use that manually.
        private String challengeSignatureJson;
    }
}
