{ jdk ? "jdk16" }:

let
  # get a normalized set of packages, from which
  # we will install all the needed dependencies
  pkgs = import ./pkgs.nix { inherit jdk; };
in
  pkgs.mkShell {
    buildInputs = [
      pkgs.${jdk}
      pkgs.gradle
      pkgs.jq
      pkgs.kubectl
      pkgs.kustomize
      pkgs.terraform_1_0
    ];
    shellHook = ''
      export NIX_ENV=dev
    '';
  }
