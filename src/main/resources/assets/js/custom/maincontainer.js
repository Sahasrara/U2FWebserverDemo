var SHOW_ALERT = true;
/**
 *  Helper Functions
 */
function addUsers(personCount) {
  $("#usersInputContainer").empty();
  for (var i = 0; i < personCount; i++) {
    var humanFriendlyCount = i + 1;
    $("#usersInputContainer").append(`
      <div class="form-row">
        <div class="form-group col-md-6">
          <label for="inputUsername">Username ` + humanFriendlyCount + `</label>
          <input type="text" class="form-control" id="inputUsername` + i + `" placeholder="Username" required>
        </div>
        <div class="form-group col-md-6">
          <label for="inputPassword">Password ` + humanFriendlyCount + `</label>
          <input type="password" class="form-control" id="inputPassword` + i + `" placeholder="Password" required>
        </div>
      </div>`);
  }
}

// Displays a Danger Alert in the Form
function formDangerAlert(show, message) {
  if (show) {
    $("#alertDangerMessage").text(message);
    // $("#formDangerAlert").collapse('show');
    $("#formDangerAlert").show();
  } else {
    // $("#formDangerAlert").collapse('hide');
    $("#formDangerAlert").hide();
  }
}

// Displays an Info Alert in the Form
function formInfoAlert(show, message) {
  if (show) {
    $("#alertInfoMessage").text(message);
    // $("#formInfoAlert").collapse('show');
    $("#formInfoAlert").show();
  } else {
    // $("#formInfoAlert").collapse('hide'); 
    $("#formInfoAlert").hide(); 
  }
}

/**
 * Setup Event Handlers
 */
$(document).ready(function() {
  // Initial Setup
  addUsers(1);

  // Handle Change in Person Count
  $("#nPersonSelector").change(function() {
    addUsers($(this).val());
  });

  // Handle Close Alerts
  $("#alertDangerCloseButton").click(function() {
    formDangerAlert(!SHOW_ALERT);
  });
  $("#alertInfoCloseButton").click(function() {
    formInfoAlert(!SHOW_ALERT);
  });
});