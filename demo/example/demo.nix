let
  common = import "example/common.nix";

  options = {
    enableFoo = true;
    greeting = "hello, " + common.author;
  };
  packages = [
    "package1"
    "package2"
  ];
  message = null;
in
if options.enableFoo then
  let
    message = options.greeting + "! Check out LogThing ;)";
  in
  {
    inherit message;
    other = if common.isEven 10 then "it's even" else "it's not even";
    packageList = packages;
  }
else
  {
    message = "disabled";
  }
