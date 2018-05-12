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
import com.yubico.u2f.exceptions.*;
import io.dropwizard.views.View;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.security.cert.CertificateException;
import java.util.*;

/**
 * Endpoint definition resource file.
 * NOTE: Normally I'd have this separated into an interface and implementation, but since this is just a demo, I didn't.
 *       I'd also normally be more specific with status codes.
 */
@Path("/")
@Produces(MediaType.TEXT_HTML)
@Slf4j
public class Resource {
    public static final String APP_ID = "https://localhost:8443";
    private final RequestCache requestCache;
    private final LoginDatabase loginDatabase;
    private final U2F u2f;
    private final MetadataService metadataService;

    public Resource(RequestCache requestCache, LoginDatabase loginDatabase, U2F u2f, MetadataService metadataService) {
        this.requestCache = requestCache;
        this.loginDatabase = loginDatabase;
        this.u2f = u2f;
        this.metadataService = metadataService;
    }

    // ---------------
    // Pages
    // ---------------
    @Path("register")
    @GET
    public View getRegister() {
        return new RegisterView();
    }

    @Path("login")
    @GET
    public View getLogin() {
        return new LoginView();
    }

    // ---------------
    // Register REST Endpoints
    // ---------------
    @Path("register/challenge")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public RegisterChallengeResponse getRegisterChallenge(RegisterChallengeRequest request) {
        // Verify Unique Users
        nullCheckRequest(request);
        Set<String> uniqueUsernames = verifyUsersArePresentAndUnique(request.getUsernames());

        // Generate Challenges for Each User
        String loginGroupId = UUID.randomUUID().toString();
        Map<String, RegisterRequestData> usernameToRequestData = new HashMap<>();
        List<RegisterChallengeResponse.ChallengeData> registerResponseData = new LinkedList<>();
        for (String username : uniqueUsernames) {
            // NOTE: I'm only allowing one registration per user for the purpose of demo
            try {
                // Construct Response Data
                RegisterRequestData registerRequestData = u2f.startRegistration(APP_ID, Collections.EMPTY_LIST);
                registerResponseData.add(new RegisterChallengeResponse.ChallengeData(username, registerRequestData));

                // Add Register Challenge to Map
                usernameToRequestData.put(username, registerRequestData);
            } catch (U2fBadConfigurationException e) {
                throw new InternalServerErrorException("Failed to generate user registration", e);
            }
        }
        // Store Request Information for Verification Later
        requestCache.putRequest(loginGroupId, new RequestCache.ChallengeData<>(usernameToRequestData));

        // Return RegisterChallengeResponse
        return new RegisterChallengeResponse(loginGroupId, registerResponseData);
    }

    @Path("register")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public RegisterCompleteResponse completeRegisterChallenge(RegisterCompleteRequest request) {
        // Non Null
        nullCheckRequest(request);
        List<User> newUsers = new LinkedList<>();
        List<Attestation> responseData = new LinkedList<>();

        // Verify Login Group Matches our Recollection
        RequestCache.ChallengeData<RegisterRequestData> originalChallengeData = verifyGroupFromCache(
                request.getLoginGroupId(), request.getRegisterResponses().keySet());

        // Verify All Signatures
        for (Map.Entry<String, RegisterCompleteRequest.SignatureData> registerComplete :
                request.getRegisterResponses().entrySet()) {
            String username = registerComplete.getKey();
            RegisterCompleteRequest.SignatureData signatureData = registerComplete.getValue();
            RegisterRequestData originalRegisterRequest = originalChallengeData.getUsernameToRequestData()
                    .get(username);
            try {
                // Deserialize RegisterResponse
                RegisterResponse registerResponse
                        = RegisterResponse.fromJson(signatureData.getChallengeSignatureJson());

                // Verify Challenge Signature
                DeviceRegistration registration = u2f.finishRegistration(originalRegisterRequest, registerResponse);
                responseData.add(metadataService.getAttestation(registration.getAttestationCertificate()));

                // Signature Verified, Create New User (see note on security in User)
                newUsers.add(new User(username, signatureData.getPassword(), registration, request.getLoginGroupId()));
            } catch (CertificateException | U2fBadInputException | U2fRegistrationException e) {
                throw new BadRequestException("Failed to verify signatures", e);
            }
        }

        // Store the new users
        loginDatabase.saveUsers(newUsers);

        // Return RegisterCompleteResponse
        return new RegisterCompleteResponse(responseData);
    }

    // ---------------
    // Login REST Endpoints
    // ---------------
    @Path("login/challenge")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public LoginChallengeResponse getLoginChallenge(LoginChallengeRequest request) {
        // Verify Unique Users
        nullCheckRequest(request);
        Set<String> uniqueUsernames = verifyUsersArePresentAndUnique(request.getUsernames());

        // Verify Login Group
        List<User> users = verifyGroupFromDB(uniqueUsernames);
        String loginGroupId = users.get(0).getLoginGroupId();

        // Generate Challenges for Each User
        Map<String, SignRequestData> usernameToRequestData = new HashMap<>();
        List<LoginChallengeResponse.ChallengeData> loginResponseData = new LinkedList<>();
        for (User user : users) {
            try {
                // Construct Response Data
                DeviceRegistration deviceRegistration = user.getU2fDeviceData();
                SignRequestData signRequestData = u2f.startSignature(APP_ID, ImmutableList.of(deviceRegistration));
                loginResponseData.add(
                        new LoginChallengeResponse.ChallengeData(user.getUsername(), signRequestData));

                // Add Register Challenge to Map
                usernameToRequestData.put(user.getUsername(), signRequestData);
            } catch (U2fBadConfigurationException | NoEligibleDevicesException e) {
                throw new InternalServerErrorException("Failed to generate user registration", e);
            }
        }

        // Store Request Information for Verification Later
        requestCache.putRequest(loginGroupId, new RequestCache.ChallengeData<>(usernameToRequestData));

        // Return LoginChallengeResponse
        return new LoginChallengeResponse(loginGroupId, loginResponseData);
    }

    @Path("login")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public LoginCompleteResponse completeLoginChallenge(LoginCompleteRequest request) {
        nullCheckRequest(request);
        List<User> existingUsers = new LinkedList<>(); // Login updates counter so we need to save to DB

        // Verify Login Group Matches our Recollection
        RequestCache.ChallengeData<SignRequestData> originalChallengeData = verifyGroupFromCache(
                request.getLoginGroupId(), request.getLoginResponses().keySet());

        // Verify All Signatures and Passwords
        for (Map.Entry<String, LoginCompleteRequest.SignatureData> loginComplete :
                request.getLoginResponses().entrySet()) {
            String username = loginComplete.getKey();
            LoginCompleteRequest.SignatureData signatureData = loginComplete.getValue();
            SignRequestData originalSignRequest = originalChallengeData.getUsernameToRequestData().get(username);

            try {
                // Deserialize LoginResponse
                SignResponse signResponse = SignResponse.fromJson(signatureData.getChallengeSignatureJson());

                // Fetch Original User Data and Verify User Exists and Password
                Optional<User> optUser = loginDatabase.fetchUser(username);
                if (!optUser.isPresent()) {
                    throw new BadRequestException("User not found");
                } else if (!optUser.get().getPassword().equals(signatureData.getPassword())) {
                    throw new BadRequestException("Bad login information");
                }
                User originalUser = optUser.get();

                // Verify Challenge Signature
                List<DeviceRegistration> deviceRegistrations = ImmutableList.of(originalUser.getU2fDeviceData());
                DeviceRegistration registration = u2f.finishSignature(
                        originalSignRequest, signResponse, deviceRegistrations);
                originalUser.setU2fDeviceData(registration);
                existingUsers.add(originalUser);
            } catch (U2fAuthenticationException | U2fBadInputException e) {
                throw new BadRequestException("Failed to verify signatures", e);
            }
        }

        // Update the existing users
        loginDatabase.updateUsers(existingUsers);

        // Return LoginCompleteResponse
        return new LoginCompleteResponse(true);
    }

    /**
     * Checks to see if a list of user names contains duplicates.
     * @param stringList list of user names to check for duplicates
     * @return the set of unique user names
     */
    private Set<String> verifyUsersArePresentAndUnique(List<String> stringList) {
        if (stringList == null || stringList.size() < 1) {
            throw new BadRequestException("No users in challenge request!");
        }
        Set<String> result;
        Set<String> uniqueUsers = new HashSet(stringList);
        if (uniqueUsers.size() != stringList.size()) {
            throw new BadRequestException("Duplicate user names!");
        } else {
            result = uniqueUsers;
        }
        return result;
    }

    /**
     * Verify all expected users are present.
     * @param loginGroupId original login group id
     * @param usernames user names to be verified
     * @param <T> type of challenge request
     * @return the original challenge data for this login group id
     */
    private <T> RequestCache.ChallengeData<T> verifyGroupFromCache(String loginGroupId, Set<String> usernames) {
        Optional<RequestCache.ChallengeData<T>> originalCachedRequest = requestCache.evictRequest(loginGroupId);
        if (!originalCachedRequest.isPresent()) {
            throw new BadRequestException("No challenge request for signatures supplied");
        }
        RequestCache.ChallengeData<T> originalChallengeData = originalCachedRequest.get();
        if (!originalChallengeData.getUsernameToRequestData().keySet().containsAll(usernames)) {
            // Better to find out now since the work of iterating is cheaper than verifying all these signatures
            throw new BadRequestException("Login group was tampered with");
        }
        return originalChallengeData;
    }

    /**
     * Verify all usernames exist and are from same login group.
     * @param usernames usernames
     * @return the list of users we verified to exist in the same group
     */
    private List<User> verifyGroupFromDB(Set<String> usernames) {
        // Load First User
        User firstUser = loginDatabase.fetchUser(usernames.iterator().next())
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Load All Users in Login Group
        List<User> users = loginDatabase.fetchAllUsersInGroup(firstUser.getLoginGroupId());

        // Verify All Users Present
        if (users.size() != usernames.size()) {
            throw new BadRequestException("User/s not found");
        }

        // Verify All Users are in the Same Group and All Group Members are Present.
        // (again, better to do this now than to do needless challenge creation)
        for (User user : users) {
            if (!usernames.contains(user.getUsername())) {
                throw new BadRequestException("Users are not in the same login group");
            }
        }
        return users;
    }

    /**
     * Simple Null Check for Request Objects.
     * @param request request object
     */
    private <T> void nullCheckRequest(T request) {
        if (request == null) {
            throw new BadRequestException("Missing request data");
        }
    }
}
