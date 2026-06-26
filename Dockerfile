FROM eclipse-temurin:21-jdk AS build

WORKDIR /app
COPY . .

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:21-jdk

RUN apt-get update && apt-get install -y \
    ffmpeg \
    wget \
    gnupg \
    ca-certificates \
    python3 \
    python3-pip \
    python3-venv \
    fonts-dejavu \
    fonts-liberation \
    fonts-noto \
    fonts-noto-cjk \
    fonts-noto-color-emoji \
    && wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | gpg --dearmor -o /usr/share/keyrings/google-linux.gpg \
    && echo "deb [arch=amd64 signed-by=/usr/share/keyrings/google-linux.gpg] http://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/google-chrome.list \
    && apt-get update \
    && apt-get install -y google-chrome-stable \
    && python3 -m venv /opt/venv \
    && /opt/venv/bin/pip install --upgrade pip \
    && /opt/venv/bin/pip install edge-tts \
    && /opt/venv/bin/edge-tts --version \
    && google-chrome --version \
    && rm -rf /var/lib/apt/lists/*

ENV PATH="/opt/venv/bin:${PATH}"
ENV CHROME_BIN=/usr/bin/google-chrome

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]