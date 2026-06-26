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
    && test -f /opt/venv/bin/edge-tts \
    && chmod +x /opt/venv/bin/edge-tts \
    && /opt/venv/bin/edge-tts --version \
    && rm -rf /var/lib/apt/lists/*

ENV PATH="/opt/venv/bin:${PATH}"
ENV CHROME_BIN=/usr/bin/chromium

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]