{
  description = "NixThing development environment";

  inputs = {
    # unstable because its new,
    # when java 25 is pushed to stable, switch
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            openjdk25
            gradle
            nil
            nixd
          ];

          shellHook = ''
            export JAVA_HOME="${pkgs.openjdk25}"
            export PATH="$JAVA_HOME/bin:$PATH"
          '';
        };
      }
    );
}
