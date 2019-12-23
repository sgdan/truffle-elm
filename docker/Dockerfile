FROM oracle/graalvm-ce:19.3.0

ARG GRADLE_VERSION=5.6.4

RUN mkdir -p /opt/gradle && cd /opt/gradle \
    && curl -o gradle.zip -Ls \
    https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip \
    && jar xf gradle.zip \
    && chmod +x gradle-${GRADLE_VERSION}/bin/gradle \
    && ln -s /opt/gradle/gradle-${GRADLE_VERSION}/bin/gradle /usr/local/bin/gradle \
    && rm gradle.zip

RUN gu install native-image
