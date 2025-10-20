let
  isEven = n: n % 2 == 0;
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
    other = if isEven 10 then "it's even" else "it's odd";
    packageList = packages;
  }
else
  {
    message = "disabled";
  }