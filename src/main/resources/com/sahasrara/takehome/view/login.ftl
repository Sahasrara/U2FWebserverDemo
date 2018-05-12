<html>
  <head>
    <#include "globalImports.ftl">

    <!-- Login Code -->
    <script type="text/javascript">
      // Global
      var loginDisabled = false;

      // Sign Challenges and Complete Login
      function completeLogin(loginChallengeResponse, loginCompleteRequest, loginGroupId) {
        // Sign Challenge (one by one)
        if (loginChallengeResponse.loginResponses.length > 0) {
          var username = loginChallengeResponse.loginResponses[0].username;
          formInfoAlert(SHOW_ALERT, "Poke " + username + "'s U2F device!");
          var signRequestData = loginChallengeResponse.loginResponses[0].signRequestData;
          u2f.sign(
            signRequestData.appId,
            signRequestData.challenge,
            signRequestData.signRequests,
            function(signatureData) {
              if(signatureData.errorCode) {
                switch (signatureData.errorCode) {
                  case 4:
                    formDangerAlert(SHOW_ALERT, "This device is not registered for this account.");
                    break;
                  default:
                    formDangerAlert(SHOW_ALERT, "U2F failed with error code: " + signatureData.errorCode);
                }
                return;
              } else {
                // Remove Login Request from List
                loginChallengeResponse.loginResponses.splice(0, 1);

                // Add to loginCompleteRequest
                loginCompleteRequest[username].challengeSignatureJson = JSON.stringify(signatureData);

                // Complete Remaining Signatures
                completeLogin(loginChallengeResponse, loginCompleteRequest, loginGroupId);
              }
            }
          ); 
        } else {
          // Complete Login
          var loginCompleteRequestWrapper = {
            "loginResponses": loginCompleteRequest,
            "loginGroupId": loginGroupId
          };
          var request = $.ajax({
            url: "login",
            method: "POST",
            data: JSON.stringify(loginCompleteRequestWrapper),
            dataType: "json",
            processData: false,
            contentType: 'application/json'
          })
          .done(function(success) {
            // Login Complete :)
            loginDisabled = false;
            if (success) {
              formInfoAlert(SHOW_ALERT, "Login succeeded!");
            } else {
              formDangerAlert(SHOW_ALERT, "Login failed!");
            }
          })
          .fail(function(jqXHR, textStatus) {
            loginDisabled = false;
            formDangerAlert(SHOW_ALERT, "Login failed!");
          });          
        }
      }

      /**
       * Setup Event Handlers
       */
      $(document).ready(function() {
        // Handle Login Request
        $("#mainForm").submit(function(e){
            // Don't Submit Form
            e.preventDefault();

            // Debounce
            if (!loginDisabled) {
              loginDisabled = true;

              // Grab User Count
              var personCount = parseInt($("#nPersonSelector").val());

              // Grab Challenge / Login Request Objects
              var challengeQueryParameters = {
                "usernames": []
              };
              var loginCompleteRequest = {}
              for (var i = 0; i < personCount; i++) {
                // Username/Password Input Names
                var usernameInput = "#inputUsername" + i;
                var passwordInput = "#inputPassword" + i;

                // Fetch Username
                var username = $(usernameInput).val();

                // Build Challenge Query Parameters
                challengeQueryParameters["usernames"].push(username);

                // Build Login Request Data
                loginCompleteRequest[username] = {
                  "password": $(passwordInput).val()
                }
              }

              // Request Challenges
              var request = $.ajax({
                url: "login/challenge",
                method: "POST",
                data: JSON.stringify(challengeQueryParameters),
                dataType: "json",
                processData: false,
                contentType: 'application/json'
              })
              .done(function(loginChallengeResponse) {
                completeLogin(loginChallengeResponse, loginCompleteRequest, loginChallengeResponse["loginGroupId"]);
              })
              .fail(function(jqXHR, textStatus) {
                loginDisabled = false;
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