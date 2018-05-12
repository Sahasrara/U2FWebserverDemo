package com.sahasrara.takehome.data.login;

import com.sahasrara.takehome.data.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Login Complete Response Object.
 * The server sends this to the client when we have verified signatures/credentials.
 * We return true only if this operation succeeded.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginCompleteResponse implements Response {
    private boolean success;
}
