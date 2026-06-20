FROM ubuntu:24.04

RUN apt-get update

RUN apt-get install -y git openjdk-21-jdk-headless

WORKDIR /tmp

# Ubuntu 24.04 ships a default 'ubuntu' user occupying UID 1000, which collides with the
# builduser we create below. Remove it so builduser can keep UID 1000 (matching the pod's
# runAsUser: 1000).
RUN userdel -r ubuntu || true

RUN useradd -m builduser -u 1000

# Pin GRADLE_USER_HOME to a path baked into the image rather than letting it default to a
# location under the ephemeral CI workspace. This ensures the dependency cache warmed below
# (including the ~1 GiB IntelliJ Platform SDK) is actually reused at runtime instead of being
# re-downloaded on every build. It stays hermetic: each pod gets its own copy-on-write view
# of this directory from the immutable image, with no shared mutable host state.
ENV GRADLE_USER_HOME=/home/builduser/.gradle

USER 1000

ARG BRANCH=242.x

RUN git clone --depth 1 -b ${BRANCH} https://github.com/SJrX/systemdUnitFilePlugin.git && \
      cd /tmp/systemdUnitFilePlugin && \
      /tmp/systemdUnitFilePlugin/gradlew --no-daemon --build-cache dependencies compileKotlin compileTestKotlin && \
      rm -rf /tmp/systemdUnitFilePlugin/

WORKDIR /