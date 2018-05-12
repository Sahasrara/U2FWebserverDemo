package com.sahasrara.takehome.data;

import com.yubico.u2f.data.DeviceRegistration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User data object.
 * NOTE: I didn't bother to salt/hash the password since this is a demo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String username;
    private String password;
    private DeviceRegistration u2fDeviceData;
    private String loginGroupId;
}
