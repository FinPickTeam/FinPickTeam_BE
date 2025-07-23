# 1️⃣ Tomcat 9 기반 이미지 사용
FROM tomcat:9.0-jdk17

# 2️⃣ 기존 webapps 내용 제거 (기본 ROOT 등 제거)
RUN rm -rf /usr/local/tomcat/webapps/*

# 3️⃣ WAR 파일을 컨테이너로 복사
# build/libs 또는 build 경로에 있는 .war 이름에 맞게 수정!
COPY build/libs/*.war /usr/local/tomcat/webapps/ROOT.war

# 4️⃣ 8080 포트 개방
EXPOSE 8080

# 5️⃣ Tomcat 실행 (기본 ENTRYPOINT 사용)
