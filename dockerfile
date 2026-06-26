FROM eclipse-temurin:21-jdk

RUN apt-get update && apt-get install -y \
    ffmpeg \
    chromium \
    python3 \
    python3-pip \
    python3-venv \
    && python3 -m venv /opt/venv \
    && /opt/venv/bin/pip install --upgrade pip \
    && /opt/venv/bin/pip install edge-tts \
    && ln -s /opt/venv/bin/edge-tts /usr/local/bin/edge-tts \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]