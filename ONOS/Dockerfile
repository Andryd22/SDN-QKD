# Copyright 2015-present Open Networking Foundation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# With this dockerfile you can build a ONOS Docker container

ARG JOBS=2
ARG PROFILE=default
ARG TAG=11.0.13-11.52.13
ARG JAVA_PATH=/usr/lib/jvm/zulu11

# First stage is the build environment.
# zulu-openjdk images are based on Ubuntu.
FROM azul/zulu-openjdk:${TAG} AS builder

ENV BUILD_DEPS \
    ca-certificates \
    zip \
    python3 \
    python2 \
    git \
    bzip2 \
    build-essential \
    curl \
    unzip \
    maven \
    iputils-ping \
    gnupg 
RUN apt-get update && apt-get install -y ${BUILD_DEPS}
RUN apt-get install -y python3-pip
RUN pip3 install requests
# Install Bazelisk, which will download the version of bazel specified in
# .bazelversion
RUN curl -L -o bazelisk https://github.com/bazelbuild/bazelisk/releases/download/v1.11.0/bazelisk-linux-amd64
RUN chmod +x bazelisk && mv bazelisk /usr/bin
RUN ln -s /usr/bin/bazelisk /usr/bin/bazel

# Install bazelisk 1.8.1 manually
# Force bazelisk to use Bazel 6.0.0-pre.20220421.3 (the required version)
ENV BAZELISK_BASE_URL="https://releases.bazel.build/6.0.0/rolling"
#RUN bazel version
# Build-stage environment variables
ENV ONOS_ROOT /src/onos
ENV BUILD_NUMBER docker
ENV JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8


# Copy in the sources
COPY . ${ONOS_ROOT}

WORKDIR ${ONOS_ROOT}

#ENV JAVA_HOME=${JAVA_PATH}
#ENV PATH=$JAVA_HOME/bin:$PATH
# Build ONOS using the JDK pre-installed in the base image, instead of the
# Bazel-provided remote one. By doing wo we make sure to build with the most
# updated JDK, including bug and security fixes, independently of the Bazel
# version. NOTE that WORKSPACE-docker file defines dockerjdk
ARG JOBS
ARG JAVA_PATH
ARG PROFILE
ENV JAVA_HOME=${JAVA_PATH}
ENV PATH=${JAVA_HOME}/bin:${PATH}

RUN cat WORKSPACE-docker >> WORKSPACE && bazelisk build onos \
    --jobs ${JOBS} \
    --verbose_failures \
    --java_runtime_version=dockerjdk_11 \
    --tool_java_runtime_version=dockerjdk_11 \
    --host_javabase=@local_jdk//:jdk \
    --define profile=${PROFILE}

WORKDIR ${ONOS_ROOT}

RUN chmod +x ./tools/build/onos-publish
RUN chmod +x ./tools/build/onos-publish-catalog
RUN chmod +x ./tools/build/onos-upload-artifacts.py 
RUN ./tools/build/onos-publish

WORKDIR ${ONOS_ROOT}/quantum-app
RUN mvn clean install -DskipTests

WORKDIR ${ONOS_ROOT}
# We extract the tar in the build environment to avoid having to put the tar in
# the runtime stage. This saves a lot of space.
RUN mkdir /output
RUN tar -xf bazel-bin/onos.tar.gz -C /output --strip-components=1


# Second and final stage is the runtime environment.
FROM azul/zulu-openjdk:${TAG}

LABEL org.label-schema.name="ONOS" \
      org.label-schema.description="SDN Controller" \
      org.label-schema.usage="http://wiki.onosproject.org" \
      org.label-schema.url="http://onosproject.org" \
      org.label-scheme.vendor="Open Networking Foundation" \
      org.label-schema.schema-version="1.0" \
      maintainer="onos-dev@onosproject.org"

RUN apt-get update && apt-get install -y \
	curl \
	python3 \
	python3-pip \
	iputils-ping \
	&& rm -rf /var/lib/apt/lists/*

# Install ONOS in /root/onos
COPY --from=builder /output/ /root/onos/
COPY --from=builder /src/onos/quantum-app /root/onos/quantum-app
COPY --from=builder /src/onos/init_network.sh /root/onos/init-network.sh
COPY --from=builder /src/onos/avvia_scambio_chiavi.sh /root/onos/avvia_scambio_chiavi.sh
COPY --from=builder /src/onos/configure-qkd-nodes.sh /root/onos/configure-qkd-nodes.sh
COPY --from=builder /src/onos/quancom_qkd_devices.json /root/onos/quancom_qkd_devices.json
COPY --from=builder /src/onos/quancom_two_links.json /root/onos/quancom_two_links.json
COPY --from=builder /src/onos/edit-config-node-31.xml /root/onos/edit-config-node-31.xml
COPY --from=builder /src/onos/edit-config-node-32.xml /root/onos/edit-config-node-32.xml
COPY --from=builder /src/onos/edit-config-node-33.xml /root/onos/edit-config-node-33.xml

WORKDIR /root/onos
RUN chmod +x init-network.sh
RUN chmod +x configure-qkd-nodes.sh
RUN chmod +x avvia_scambio_chiavi.sh 

# Set JAVA_HOME (by default not exported by zulu images)
ARG JAVA_PATH
ENV JAVA_HOME ${JAVA_PATH}

# Ports
# 6653 - OpenFlow
# 6640 - OVSDB
# 8181 - GUI
# 8101 - ONOS CLI
# 9876 - ONOS intra-cluster communication
EXPOSE 6653 6640 8181 8101 9876

# Run ONOS
COPY start.sh /start.sh
RUN chmod +x /start.sh
CMD ["/start.sh"]
