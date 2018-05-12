package com.sahasrara.takehome;

import com.sahasrara.takehome.cache.RequestCache;
import com.sahasrara.takehome.data.User;
import com.sahasrara.takehome.data.login.LoginChallengeRequest;
import com.sahasrara.takehome.data.login.LoginChallengeResponse;
import com.sahasrara.takehome.data.login.LoginCompleteRequest;
import com.sahasrara.takehome.data.login.LoginCompleteResponse;
import com.sahasrara.takehome.data.register.RegisterChallengeRequest;
import com.sahasrara.takehome.data.register.RegisterChallengeResponse;
import com.sahasrara.takehome.data.register.RegisterCompleteRequest;
import com.sahasrara.takehome.data.register.RegisterCompleteResponse;
import com.sahasrara.takehome.db.LoginDatabase;
import com.sahasrara.takehome.view.LoginView;
import com.sahasrara.takehome.view.RegisterView;
import com.google.common.collect.ImmutableList;
import com.yubico.u2f.U2F;
import com.yubico.u2f.attestation.Attestation;
import com.yubico.u2f.attestation.MetadataService;
import com.yubico.u2f.data.DeviceRegistration;
import com.yubico.u2f.data.messages.RegisterRequestData;
import com.yubico.u2f.data.messages.RegisterResponse;
import com.yubico.u2f.data.messages.SignRequestData;
import com.yubico.u2f.data.messages.SignResponse;
import com.yubico.u2f.exceptions.U2fAuthenticationException;
import com.yubico.u2f.exceptions.U2fBadConfigurationException;
import io.dropwizard.views.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import java.security.cert.CertificateException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for Resource.
 * NOTE: I'd probably have more tests for the other classes, but again DEMO :)
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceTest {
    private static final String PASSWORD = "password";
    private static final String LOGIN_GROUP_ID = UUID.randomUUID().toString();
    private static final List<String> USERNAMES = ImmutableList.<String>builder()
            .add("eric")
            .add("alex")
            .add("kevin")
            .build();
    private static final List<String> NON_UNIQUE_USERNAMES = ImmutableList.<String>builder()
            .add("eric")
            .add("eric")
            .build();
    private static final String REGISTER_RESPONSE_JSON = "{\"registrationData\":\"BQSMnJgPtgvGHGbcx7lLI5m3xU5Llegu2Jn" +
            "6TnSvB7t-bXxjqEHp8aMTnXGvKfMJsCWQoLDojFZiFJnI-1GLuQZRQOQlaZo1x2RAnpmCeiMdzQyd9UvImQLkYE62pk9IhX7_vb843Nu" +
            "2blFyzrwPRR9mVGoVPG3MOxv64ZpU3xxxrtEwggE1MIHcoAMCAQICCwCRkIVctXdDo-gMMAoGCCqGSM49BAMCMBUxEzARBgNVBAMTClU" +
            "yRiBJc3N1ZXIwGhcLMDAwMTAxMDAwMFoXCzAwMDEwMTAwMDBaMBUxEzARBgNVBAMTClUyRiBEZXZpY2UwWTATBgcqhkjOPQIBBggqhkj" +
            "OPQMBBwNCAASOu-MLt369HM9Jceb4JoZEIB15Jy9NuE0_E-mBiXaQCAMSrEiPBoAQT99ZPl_1JRnFVUx_hjCxB3V735q17QTIoxcwFTA" +
            "TBgsrBgEEAYLlHAIBAQQEAwIFIDAKBggqhkjOPQQDAgNIADBFAiEAwaOmji8WpyFGJwV_YrtyjJ4D56G6YtBGUk5FbSwvP3MCIAtfeOU" +
            "RqhgSn28jbZITIn2StOZ-31PoFt-wXZ3IuQ_eMEQCICji8TLHmbt7vQr_EjBpmTsQUWGmBerXaS_ykElbLBJ6AiB1rIIKV8oivuebfzg" +
            "z2scO6xL0GCGSTKPadai_eRyUdg\",\"version\":\"U2F_V2\",\"challenge\":\"06l-3vf6RHwifD_P2BQbDP9JPEwQed2N52Q" +
            "sCCgd8Kk\",\"appId\":\"https://localhost:8443\",\"clientData\":\"eyJ0eXAiOiJuYXZpZ2F0b3IuaWQuZmluaXNoRW5" +
            "yb2xsbWVudCIsImNoYWxsZW5nZSI6IjA2bC0zdmY2Ukh3aWZEX1AyQlFiRFA5SlBFd1FlZDJONTJRc0NDZ2Q4S2siLCJvcmlnaW4iOiJ" +
            "odHRwczovL2xvY2FsaG9zdDo4NDQzIiwiY2lkX3B1YmtleSI6InVudXNlZCJ9\"}";
    public static final String SIGN_RESPONSE_JSON = "{\"keyHandle\":\"5CVpmjXHZECemYJ6Ix3NDJ31S8iZAuRgTramT0iFfv-9vzj" +
            "c27ZuUXLOvA9FH2ZUahU8bcw7G_rhmlTfHHGu0Q\",\"clientData\":\"eyJ0eXAiOiJuYXZpZ2F0b3IuaWQuZ2V0QXNzZXJ0aW9uI" +
            "iwiY2hhbGxlbmdlIjoicURtV25GWVMxWkJSX01iQjdSZGpraWFOZl9jWnBaM1hubXFRYkVPb09VRSIsIm9yaWdpbiI6Imh0dHBzOi8vb" +
            "G9jYWxob3N0Ojg0NDMiLCJjaWRfcHVia2V5IjoidW51c2VkIn0\",\"signatureData\":\"AQAAAC8wRQIgcY42gVWhFQ31TZHo_fE" +
            "T5W_iJXaloZn9UxKaf9kmnikCIQDSIyMQrQfv8rWlOs_Ue0RnXSphvrLdllRZgBpGzfhBNQ\"}";

    @Mock
    private RequestCache requestCache;
    @Mock
    private LoginDatabase loginDatabase;
    @Mock
    private U2F u2f;
    @Mock
    private MetadataService metadataService;
    @Mock
    private RegisterRequestData registerRequestData;
    @Mock
    private SignRequestData signRequestData;
    @Mock
    private DeviceRegistration deviceRegistration;
    @Mock
    private Attestation attestation;

    private Resource resource;

    @Before
    public void setup() {
        this.resource = new Resource(requestCache, loginDatabase, u2f, metadataService);
    }

    @Test
    public void getRegister() {
        // Test
        View view = resource.getRegister();

        // Verify
        assertTrue(view instanceof RegisterView);
    }

    @Test
    public void getLogin() {
        // Test
        View view = resource.getLogin();

        // Verify
        assertTrue(view instanceof LoginView);
    }

    @Test
    public void getRegisterChallenge_succeed() throws Exception {
        // Setup
        RegisterChallengeRequest registerChallengeRequest = new RegisterChallengeRequest(USERNAMES);
        when(u2f.startRegistration(anyString(), any())).thenReturn(registerRequestData);

        // Test
        RegisterChallengeResponse registerChallengeResponse = resource.getRegisterChallenge(registerChallengeRequest);

        // Verify
        ArgumentCaptor<String> appId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List> registrations = ArgumentCaptor.forClass(List.class);
        verify(u2f, times(USERNAMES.size())).startRegistration(appId.capture(), registrations.capture());
        assertEquals(Resource.APP_ID, appId.getValue());
        assertEquals(Collections.EMPTY_LIST, registrations.getValue());
        verify(requestCache).putRequest(anyString(), any());
        assertNotNull(registerChallengeResponse);
        assertNotNull(registerChallengeResponse.getLoginGroupId());
        assertNotNull(registerChallengeResponse.getRegisterResponses());
        assertEquals(USERNAMES.size(), registerChallengeResponse.getRegisterResponses().size());
    }

    @Test(expected = BadRequestException.class)
    public void getRegisterChallenge_nonUniqueUsersFail() {
        // Setup
        RegisterChallengeRequest registerChallengeRequest = new RegisterChallengeRequest(NON_UNIQUE_USERNAMES);

        // Test
        resource.getRegisterChallenge(registerChallengeRequest);
    }

    @Test(expected = BadRequestException.class)
    public void getRegisterChallenge_nullRequest() {
        // Test
        resource.getRegisterChallenge(null);
    }

    @Test(expected = BadRequestException.class)
    public void getRegisterChallenge_emptyUsersFail() {
        // Setup
        RegisterChallengeRequest registerChallengeRequest = new RegisterChallengeRequest();

        // Test
        resource.getRegisterChallenge(registerChallengeRequest);
    }

    @Test(expected = InternalServerErrorException.class)
    public void getRegisterChallenge_challengeFail() throws Exception {
        // Setup
        RegisterChallengeRequest registerChallengeRequest = new RegisterChallengeRequest(USERNAMES);
        when(u2f.startRegistration(anyString(), any())).thenThrow(U2fBadConfigurationException.class);

        // Test
        resource.getRegisterChallenge(registerChallengeRequest);
    }

    @Test
    public void completeRegisterChallenge_succeed() throws Exception {
        // Setup
        Map<String, RegisterRequestData> usernameToRequestData = new HashMap<>();
        for (String username : USERNAMES) {
            usernameToRequestData.put(username, mock(RegisterRequestData.class));
        }
        RequestCache.ChallengeData<RegisterRequestData> originalCacheRequest
                = new RequestCache.ChallengeData<>(usernameToRequestData);
        Optional<RequestCache.ChallengeData<RegisterRequestData>> challengeDataOpt = Optional.of(originalCacheRequest);
        when(requestCache.<RegisterRequestData>evictRequest(LOGIN_GROUP_ID)).thenReturn(challengeDataOpt);

        Map<String, RegisterCompleteRequest.SignatureData> registerResponses = new HashMap<>();
        for (String username : USERNAMES) {
            registerResponses.put(
                    username, new RegisterCompleteRequest.SignatureData(PASSWORD, REGISTER_RESPONSE_JSON));
        }
        RegisterCompleteRequest registerCompleteRequest
                = new RegisterCompleteRequest(registerResponses, LOGIN_GROUP_ID);

        when(u2f.finishRegistration(any(), any())).thenReturn(deviceRegistration);
        when(metadataService.getAttestation(any())).thenReturn(attestation);

        // Test
        RegisterCompleteResponse registerCompleteResponse = resource.completeRegisterChallenge(registerCompleteRequest);

        // Verify
        assertNotNull(registerCompleteResponse.getAttestation());
        assertEquals(USERNAMES.size(), registerCompleteResponse.getAttestation().size());
        ArgumentCaptor<List> usersCaptor = ArgumentCaptor.forClass(List.class);
        verify(loginDatabase).saveUsers(usersCaptor.capture());
        List<User> usersList = (List<User>) usersCaptor.getValue();
        assertEquals(USERNAMES.size(), usersList.size());
        for (User user : usersList) {
            verify(u2f).finishRegistration(
                    usernameToRequestData.get(user.getUsername()),
                    RegisterResponse.fromJson(REGISTER_RESPONSE_JSON));
        }
    }

    @Test(expected = BadRequestException.class)
    public void completeRegisterChallenge_nullRequest() {
        // Test
        resource.completeRegisterChallenge(null);
    }

    @Test(expected = BadRequestException.class)
    public void completeRegisterChallenge_verifyFail() throws Exception {
        // Setup
        Map<String, RegisterRequestData> usernameToRequestData = new HashMap<>();
        for (String username : USERNAMES) {
            usernameToRequestData.put(username, mock(RegisterRequestData.class));
        }
        RequestCache.ChallengeData<RegisterRequestData> originalCacheRequest
                = new RequestCache.ChallengeData<>(usernameToRequestData);
        Optional<RequestCache.ChallengeData<RegisterRequestData>> challengeDataOpt = Optional.of(originalCacheRequest);
        when(requestCache.<RegisterRequestData>evictRequest(LOGIN_GROUP_ID)).thenReturn(challengeDataOpt);

        Map<String, RegisterCompleteRequest.SignatureData> registerResponses = new HashMap<>();
        for (String username : USERNAMES) {
            registerResponses.put(
                    username, new RegisterCompleteRequest.SignatureData(PASSWORD, REGISTER_RESPONSE_JSON));
        }
        RegisterCompleteRequest registerCompleteRequest
                = new RegisterCompleteRequest(registerResponses, LOGIN_GROUP_ID);

        when(u2f.finishRegistration(any(), any())).thenThrow(CertificateException.class);

        // Test
       resource.completeRegisterChallenge(registerCompleteRequest);
    }

    @Test
    public void getLoginChallenge_succeed() throws Exception {
        // Setup
        when(loginDatabase.fetchUser(anyString())).thenAnswer(invocationOnMock -> Optional.of(
                new User(invocationOnMock.getArgument(0), PASSWORD, deviceRegistration, LOGIN_GROUP_ID)));
        List<User> users = new LinkedList<>();
        for (String username : USERNAMES) {
            users.add(new User(username, PASSWORD, deviceRegistration, LOGIN_GROUP_ID));
        }
        when(loginDatabase.fetchAllUsersInGroup(LOGIN_GROUP_ID)).thenReturn(users);
        when(u2f.startSignature(anyString(), any())).thenReturn(signRequestData);
        LoginChallengeRequest loginChallengeRequest = new LoginChallengeRequest(USERNAMES);

        // Test
        LoginChallengeResponse loginChallengeResponse = resource.getLoginChallenge(loginChallengeRequest);

        // Verify
        assertNotNull(loginChallengeResponse);
        assertEquals(LOGIN_GROUP_ID, loginChallengeResponse.getLoginGroupId());
        assertNotNull(loginChallengeResponse.getLoginResponses());
        assertEquals(USERNAMES.size(), loginChallengeResponse.getLoginResponses().size());
        ArgumentCaptor<List> registrationListCaptor = ArgumentCaptor.forClass(List.class);
        verify(u2f, times(USERNAMES.size())).startSignature(anyString(), registrationListCaptor.capture());
        assertEquals(USERNAMES.size(), registrationListCaptor.getAllValues().size());
        for (List<DeviceRegistration> registrationsSingleton : registrationListCaptor.getAllValues()) {
            assertNotNull(registrationsSingleton);
            assertEquals(1, registrationsSingleton.size()); // Only checking first user
            assertEquals(deviceRegistration, registrationsSingleton.get(0));
        }
    }

    @Test(expected = BadRequestException.class)
    public void getLoginChallenge_nullRequest() {
        // Test
        resource.getLoginChallenge(null);
    }

    @Test(expected = BadRequestException.class)
    public void getLoginChallenge_nonUniqueUsersFail() {
        // Setup
        LoginChallengeRequest loginChallengeRequest = new LoginChallengeRequest(NON_UNIQUE_USERNAMES);

        // Test
        resource.getLoginChallenge(loginChallengeRequest);
    }

    @Test(expected = BadRequestException.class)
    public void getLoginChallenge_emptyUsersFail() {
        // Setup
        LoginChallengeRequest loginChallengeRequest = new LoginChallengeRequest();

        // Test
        resource.getLoginChallenge(loginChallengeRequest);
    }

    @Test(expected = InternalServerErrorException.class)
    public void getLoginChallenge_challengeFail() throws Exception {
        // Setup
        when(loginDatabase.fetchUser(anyString())).thenAnswer(invocationOnMock -> Optional.of(
                new User(invocationOnMock.getArgument(0), PASSWORD, deviceRegistration, LOGIN_GROUP_ID)));
        List<User> users = new LinkedList<>();
        for (String username : USERNAMES) {
            users.add(new User(username, PASSWORD, deviceRegistration, LOGIN_GROUP_ID));
        }
        when(loginDatabase.fetchAllUsersInGroup(LOGIN_GROUP_ID)).thenReturn(users);
        when(u2f.startSignature(anyString(), any())).thenThrow(U2fBadConfigurationException.class);
        LoginChallengeRequest loginChallengeRequest = new LoginChallengeRequest(USERNAMES);

        // Test
        resource.getLoginChallenge(loginChallengeRequest);
    }

    @Test
    public void completeLoginChallenge_succeed() throws Exception {
        // Setup
        Map<String, SignRequestData> usernameToRequestData = new HashMap<>();
        for (String username : USERNAMES) {
            usernameToRequestData.put(username, mock(SignRequestData.class));
        }
        RequestCache.ChallengeData<SignRequestData> originalCacheRequest
                = new RequestCache.ChallengeData<>(usernameToRequestData);
        Optional<RequestCache.ChallengeData<SignRequestData>> challengeDataOpt = Optional.of(originalCacheRequest);
        when(requestCache.<SignRequestData>evictRequest(LOGIN_GROUP_ID)).thenReturn(challengeDataOpt);

        Map<String, LoginCompleteRequest.SignatureData> registerResponses = new HashMap<>();
        for (String username : USERNAMES) {
            registerResponses.put(username, new LoginCompleteRequest.SignatureData(PASSWORD, SIGN_RESPONSE_JSON));
        }

        when(loginDatabase.fetchUser(anyString())).thenAnswer(invocationOnMock -> Optional.of(
                new User(invocationOnMock.getArgument(0), PASSWORD, deviceRegistration, LOGIN_GROUP_ID)));

        when(u2f.finishSignature(any(), any(), any())).thenReturn(deviceRegistration);
        LoginCompleteRequest loginCompleteRequest
                = new LoginCompleteRequest(registerResponses, LOGIN_GROUP_ID);

        // Test
        LoginCompleteResponse loginCompleteResponse = resource.completeLoginChallenge(loginCompleteRequest);

        // Verify
        assertTrue(loginCompleteResponse.isSuccess());
        ArgumentCaptor<List> usersCaptor = ArgumentCaptor.forClass(List.class);
        verify(loginDatabase).updateUsers(usersCaptor.capture());
        List<User> usersList = (List<User>) usersCaptor.getValue();
        assertEquals(USERNAMES.size(), usersList.size());
        for (User user : usersList) {
            verify(u2f).finishSignature(
                    usernameToRequestData.get(user.getUsername()),
                    SignResponse.fromJson(SIGN_RESPONSE_JSON),
                    ImmutableList.of(deviceRegistration));
        }
    }

    @Test(expected = BadRequestException.class)
    public void completeLoginChallenge_nullRequest() {
        // Test
        resource.completeLoginChallenge(null);
    }

    @Test(expected = BadRequestException.class)
    public void completeLoginChallenge_verifyFail() throws Exception {
        // Setup
        Map<String, SignRequestData> usernameToRequestData = new HashMap<>();
        for (String username : USERNAMES) {
            usernameToRequestData.put(username, mock(SignRequestData.class));
        }
        RequestCache.ChallengeData<SignRequestData> originalCacheRequest
                = new RequestCache.ChallengeData<>(usernameToRequestData);
        Optional<RequestCache.ChallengeData<SignRequestData>> challengeDataOpt = Optional.of(originalCacheRequest);
        when(requestCache.<SignRequestData>evictRequest(LOGIN_GROUP_ID)).thenReturn(challengeDataOpt);

        Map<String, LoginCompleteRequest.SignatureData> registerResponses = new HashMap<>();
        for (String username : USERNAMES) {
            registerResponses.put(username, new LoginCompleteRequest.SignatureData(PASSWORD, SIGN_RESPONSE_JSON));
        }

        when(loginDatabase.fetchUser(anyString())).thenAnswer(invocationOnMock -> Optional.of(
                new User(invocationOnMock.getArgument(0), PASSWORD, deviceRegistration, LOGIN_GROUP_ID)));

        when(u2f.finishSignature(any(), any(), any())).thenThrow(U2fAuthenticationException.class);
        LoginCompleteRequest loginCompleteRequest
                = new LoginCompleteRequest(registerResponses, LOGIN_GROUP_ID);

        // Test
        resource.completeLoginChallenge(loginCompleteRequest);
    }
}
