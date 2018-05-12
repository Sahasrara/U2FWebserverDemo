package com.sahasrara.takehome.data.register;

import com.sahasrara.takehome.data.Response;
import com.yubico.u2f.attestation.Attestation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Registration Complete Response Object.
 * The server sends this to the client when we have verified signatures sent to us and stored new user credentials.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterCompleteResponse implements Response {
    private List<Attestation> attestation;
}
