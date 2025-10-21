{
  version = "1.0.0";
  author = "Kinetic Labs";
  defaults = {
    timeout = 30;
    retries = 3;
  };

  isEven = n: builtins.mod n 2 == 0;
}
