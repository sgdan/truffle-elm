graalvm=@docker-compose run --rm graalvm

.docker: docker/Dockerfile
	@docker-compose build graalvm
	@touch .docker

shell: .docker
	$(graalvm) bash

clean: .docker
	$(graalvm) gradle clean

build: .docker
	$(graalvm) gradle installDist

run: .docker build
	$(graalvm) gradle run --args="test.json"

test: .docker
	$(graalvm) gradle test -i

build-native: .docker build
	$(graalvm) native-image \
		-cp build/install/truffle-elm/lib/kotlin-stdlib-1.3.61.jar:build/install/truffle-elm/lib/truffle-elm.jar:build/install/truffle-elm/lib/kotlinx-serialization-runtime-0.14.0.jar \
		-J"-XX:-UseJVMCIClassLoader" \
		--initialize-at-build-time --macro:truffle --allow-incomplete-classpath \
		org.sgdan.grelm.MainKt \
		build/grelmnative

run-native: .docker
	$(graalvm) build/grelmnative test.json

.PHONY: build
