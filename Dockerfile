# 1️⃣ Tomcat 9 기반 이미지
FROM tomcat:9.0-jdk17

ENV TZ=Asia/Seoul

# 2️⃣ Python + venv 설치 (PEP668 회피)
RUN apt-get update && \
    apt-get install -y --no-install-recommends python3 python3-pip python3-venv && \
    python3 -m venv /opt/venv && \
    /opt/venv/bin/pip install --upgrade pip && \
    ln -sf /opt/venv/bin/python /usr/local/bin/python && \
    rm -rf /var/lib/apt/lists/*

# venv 우선 사용
ENV PATH="/opt/venv/bin:${PATH}"

# 3️⃣ requirements 설치 (venv 내부 pip)
COPY src/main/resources/python/requirements.txt /opt/app/requirements.txt
RUN pip install --no-cache-dir -r /opt/app/requirements.txt

# 4️⃣ 기존 webapps 제거
RUN rm -rf /usr/local/tomcat/webapps/*

# 5️⃣ WAR 복사
COPY build/libs/*.war /usr/local/tomcat/webapps/ROOT.war

# 6️⃣ 수동 언팩 + 실행권한
RUN mkdir /usr/local/tomcat/webapps/ROOT && \
    cd /usr/local/tomcat/webapps/ROOT && \
    jar -xvf /usr/local/tomcat/webapps/ROOT.war && \
    chmod -R +x /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/python || true

# 7️⃣ 포트
EXPOSE 8080
