let
  options = {
    enableFoo = true;
  };
  serverPort = 8080;
in
if options.enableFoo then
  {
    version = "2.0.0";
    server = {
      host = "localhost";
      port = serverPort;
    };
  }
else
  {
    version = "1.0.0";
    message = "disabled";
    server = {
      host = "0.0.0.0";
      port = 80;
    };
  }
