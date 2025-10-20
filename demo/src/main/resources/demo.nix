let
  isN10 = n: n == 10;

  options = {
    enableFoo = true;
    greeting = "hello";
  };
  packages = [
    "package1"
    "package2"
  ];
  message = "inherited message";
in
if options.enableFoo then
  let
    message = options.greeting + " world";
  in
  {
    inherit message;
    other = if isN10 10 then "it's 10" else "it's not 10";
    packageList = packages;
  }
else
  {
    message = "disabled";
  }
