# truffle-elm

Attempts to run Elm code on GraalVM using the
[Truffle](https://github.com/oracle/graal/tree/master/truffle) API.

Elm code is compiled by the
[elm-in-elm](https://github.com/elm-in-elm/compiler) compiler to an
intermediate format where the optimised AST nodes are specified in JSON.
See `test.json` for an example.

## Build and Run

Uses the [GraalVM docker image](https://hub.docker.com/r/oracle/graalvm-ce/)
with [gradle](https://gradle.org/) installed to build and run. A native
executable can also be produced using the graal native-image tool.

Note: If you want to run the gradle commands directly,
you can use `make shell` run the docker container.
See the `Makefile` for gradle arguments.

Currently the result is rather trivial, it just executes the code in `test.json`
which produces the output `12`. To run via make and docker-compose:

```bash
# build and run
make run

# execute unit tests
make test

# create and run linux native executable
make build-native # produces build/grelmnative (25M)
make run-native
```

The AST in `test.json` was compiled from this Elm code:

```elm
module Main exposing (main)


double =
    \x -> x + x


inc =
    \a -> a + 1


main =
    double (inc 5)
```
