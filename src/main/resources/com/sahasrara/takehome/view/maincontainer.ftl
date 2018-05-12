<div class="container">
  <div class="row">
    <div class="col-md-10 offset-md-1">
      <br/>
      <h2>
        <#if templateName == '/com/sahasrara/takehome/view/login.ftl'>
          Login
        <#else>
          Register
        </#if>
      </h2>
      <br/>
      <div class="card">
        <div class="card-header">
          <#if templateName == '/com/sahasrara/takehome/view/login.ftl'>
            Login Information
          <#else>
            Registration Information
          </#if>
        </div>
        <div class="card-body">
          <form id="mainForm">
            <!-- Select n-Person Login-->
            <div id="nPersonSelectorContainer" class="form-row">
              <div class="form-group col-md-4">
                <label for="nPersonSelector">Users Required for this Account</label>
                <select id="nPersonSelector" class="form-control">
                  <option value="1" selected>1</option>
                  <option value="2">2</option>
                  <option value="3">3</option>
                  <option value="4">4</option>
                  <option value="5">5</option>
                </select>
              </div>
            </div>

            <!-- User Input -->
            <div id="usersInputContainer"></div>

            <!-- Alerts -->
            <div class="form-row">
              <div class="form-group">
                <div id="formInfoAlert" class="alert alert-info alert-dismissible collapse" role="alert">
                  <strong>Notification </strong><span id="alertInfoMessage"></span>
                  <button type="button" id="alertInfoCloseButton" class="close" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                  </button>
                </div>
              </div>
              <div class="form-group">
                <div id="formDangerAlert" class="alert alert-danger alert-dismissible collapse" role="alert">
                  <strong>Whoopsie! </strong><span id="alertDangerMessage"></span>
                  <button type="button" id="alertDangerCloseButton" class="close" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                  </button>
                </div>
              </div>
            </div>

            <!-- Submit Button -->
            <button type="submit" class="btn btn-primary">
              <#if templateName == '/com/sahasrara/takehome/view/login.ftl'>
                Login
              <#else>
                Register
              </#if>
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</div>