# 1️⃣ Tomcat 9 기반 이미지 사용
FROM tomcat:9.0-jdk17

ENV TZ=Asia/Seoul

# Python3/pip 설치
RUN apt-get update && \
    apt-get install -y --no-install-recommends python3 python3-pip && \
    ln -sf /usr/bin/python3 /usr/local/bin/python && \
    rm -rf /var/lib/apt/lists/*

# requirements 설치
COPY src/main/resources/python/requirements.txt /opt/app/requirements.txt
RUN pip install --no-cache-dir -r /opt/app/requirements.txt

# 2️⃣ 기존 webapps 내용 제거 (기본 ROOT 등 제거)
RUN rm -rf /usr/local/tomcat/webapps/*

# 3️⃣ WAR 파일을 컨테이너로 복사
COPY build/libs/*.war /usr/local/tomcat/webapps/ROOT.war

# 4️⃣ WAR 파일 수동 언팩 (jar 명령어로 압축 해제)
RUN mkdir /usr/local/tomcat/webapps/ROOT && \
    cd /usr/local/tomcat/webapps/ROOT && \
    jar -xvf /usr/local/tomcat/webapps/ROOT.war && \
    chmod -R +x /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/python || true

# 5️⃣ 8080 포트 개방
EXPOSE 8080

# 6️⃣ Tomcat 실행 (기본 ENTRYPOINT 사용)
