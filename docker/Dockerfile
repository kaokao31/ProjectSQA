# Shared environment: Defects4J 3.0.1 + Java 11 (default) / Java 8 + EvoSuite 1.2.0
FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive
# Defects4J รันเทสต์ด้วย timezone นี้
ENV TZ=America/Los_Angeles

RUN apt-get update && apt-get install -y --no-install-recommends \
        git curl wget unzip zip bzip2 subversion perl cpanminus make build-essential \
        openjdk-11-jdk openjdk-8-jdk \
        python3 python3-pip \
        tzdata ca-certificates vim less \
    && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone \
    && rm -rf /var/lib/apt/lists/*

# symlink ให้ไม่ผูกกับสถาปัตยกรรม (amd64 / arm64)
RUN ln -s /usr/lib/jvm/java-11-openjdk-$(dpkg --print-architecture) /opt/jdk11 \
 && ln -s /usr/lib/jvm/java-8-openjdk-$(dpkg --print-architecture)  /opt/jdk8

ENV JAVA_HOME=/opt/jdk11
ENV PATH=${JAVA_HOME}/bin:${PATH}

# ---- Defects4J ----
ARG D4J_VERSION=v3.0.1
ENV D4J_HOME=/opt/defects4j
RUN git clone --depth 1 --branch ${D4J_VERSION} https://github.com/rjust/defects4j ${D4J_HOME} \
 && cd ${D4J_HOME} \
 && cpanm --installdeps . \
 && ./init.sh
ENV PATH=${D4J_HOME}/framework/bin:${PATH}

# ---- EvoSuite ----
ARG EVOSUITE_VERSION=1.2.0
ENV EVOSUITE_JAR=/opt/evosuite/evosuite-${EVOSUITE_VERSION}.jar
ENV EVOSUITE_RT=/opt/evosuite/evosuite-standalone-runtime-${EVOSUITE_VERSION}.jar
RUN mkdir -p /opt/evosuite \
 && curl -fsSL -o ${EVOSUITE_JAR} https://github.com/EvoSuite/evosuite/releases/download/v${EVOSUITE_VERSION}/evosuite-${EVOSUITE_VERSION}.jar \
 && curl -fsSL -o ${EVOSUITE_RT}  https://github.com/EvoSuite/evosuite/releases/download/v${EVOSUITE_VERSION}/evosuite-standalone-runtime-${EVOSUITE_VERSION}.jar

# ---- Python (สำหรับสคริปต์ benchmark) ----
RUN pip3 install --no-cache-dir pandas python-dotenv

# โฟลเดอร์ checkout ของ Defects4J (ใช้ named volume ใน compose)
RUN mkdir -p /work /workspace
WORKDIR /workspace
CMD ["bash"]
