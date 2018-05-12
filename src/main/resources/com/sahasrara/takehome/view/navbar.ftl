<!-- Navbar -->
<nav class="navbar navbar-expand-lg navbar-light bg-light">
  <a class="navbar-brand" href="#">Eric Fulton U2F Demo</a>
  <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNavAltMarkup" 
          aria-controls="navbarNavAltMarkup" aria-expanded="false" aria-label="Toggle navigation">
    <span class="navbar-toggler-icon"></span>
  </button>
  <div class="collapse navbar-collapse" id="navbarNavAltMarkup">
    <div class="navbar-nav">
      <a class="nav-item nav-link <#if templateName == '/com/sahasrara/takehome/view/login.ftl'>active</#if>" href="login">
        Login
      </a>
      <a class="nav-item nav-link <#if templateName == '/com/sahasrara/takehome/view/register.ftl'>active</#if>"
         href="register">
        Register
      </a>
    </div>
  </div>
</nav>