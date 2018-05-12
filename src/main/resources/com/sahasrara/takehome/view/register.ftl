<html>
  <head>
    <#include "globalImports.ftl">

    <!-- Register Code -->
    <script type="text/javascript">
      // Global
      var registerDisabled = false;

      // Sign Challenges and Complete Registration
      function completeRegistration(registerChallengeResponse, registerCompleteRequest, loginGroupId) {
        // Sign Challenge (one by one)
        if (registerChallengeResponse.registerResponses.length > 0) {
          var username = registerChallengeResponse.registerResponses[0].username;
          formInfoAlert(SHOW_ALERT, "Poke " + username + "'s U2F device!");
          var registerRequestData = registerChallengeResponse.registerResponses[0].registerRequestData;
          u2f.register(
            registerRequestData.appId,
            registerRequestData.registerRequests,
            registerRequestData.registeredKeys,
            function(signatureData) {
              formInfoAlert(!SHOW_ALERT);
              if(signatureData.errorCode) {
                  switch (signatureData.errorCode) {
                    case 4:
                      formDangerAlert(SHOW_ALERT, "This device is already registered.");
                      break;
                    default:
                      formDangerAlert(SHOW_ALERT, "U2F failed with error: " + signatureData.errorCode);
                  }
              } else {
                // Remove Registration Request from List
                registerChallengeResponse.registerResponses.splice(0, 1);

                // Add to registerCompleteRequest
                registerCompleteRequest[username].challengeSignatureJson = JSON.stringify(signatureData);

                // Complete Remaining Signatures
                completeRegistration(registerChallengeResponse, registerCompleteRequest, loginGroupId);
              }
            }
          ); 
        } else {
          // Complete Registration
          var registerCompleteRequestWrapper = {
            "registerResponses": registerCompleteRequest,
            "loginGroupId": loginGroupId
          };
          var request = $.ajax({
            url: "register",
            method: "POST",
            data: JSON.stringify(registerCompleteRequestWrapper),
            dataType: "json",
            processData: false,
            contentType: 'application/json'
          })
          .done(function(attestation) {
            // Registration Complete :)
            registerDisabled = false;
            console.log(attestation);
            formInfoAlert(SHOW_ALERT, "Registration Complete!  Now try logging in...");
            setTimeout(function(){ 
              formInfoAlert(!SHOW_ALERT);
              window.location.href = "/login";
            }, 2000);
          })
          .fail(function(jqXHR, textStatus) {
            registerDisabled = false;
            formDangerAlert(SHOW_ALERT, "Failed to complete registration!");
          });          
        }
      }  

      /**
       * Setup Event Handlers
       */
      $(document).ready(function() {
        // Handle Register Request
        $("#mainForm").submit(function(e){
            // Don't Submit Form
            e.preventDefault();

            // Debounce
            if (!registerDisabled) {
              registerDisabled = true;

              // Grab User Count
              var personCount = parseInt($("#nPersonSelector").val());

              // Grab Challenge / Register Request Objects
              var challengeQueryParameters = {
                "usernames": []
              };
              var registerCompleteRequest = {}
              for (var i = 0; i < personCount; i++) {
                // Username/Password Input Names
                var usernameInput = "#inputUsername" + i;
                var passwordInput = "#inputPassword" + i;

                // Fetch Username
                var username = $(usernameInput).val();

                // Build Challenge Query Parameters
                challengeQueryParameters["usernames"].push(username);

                // Build Register Request Data
                registerCompleteRequest[username] = {
                  "password": $(passwordInput).val()
                }
              }

              // Request Challenges
              var request = $.ajax({
                url: "register/challenge",
                method: "POST",
                data: JSON.stringify(challengeQueryParameters),
                dataType: "json",
                processData: false,
                contentType: 'application/json'
              })
              .done(function(registerChallengeResponse) {
                completeRegistration(registerChallengeResponse, registerCompleteRequest, registerChallengeResponse["loginGroupId"]);
              })
              .fail(function(jqXHR, textStatus) {
                registerDisabled = false;
                formDangerAlert(SHOW_ALERT, "Failed to request challenges!");
              });
            }
        });
      });
    </script>
  </head>
  <body>
    <#include "navbar.ftl">
    <#include "maincontainer.ftl">
  </body>
</html>