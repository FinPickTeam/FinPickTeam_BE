<div align="center">

<table>
          
  <tr>
    <td align="center">
<img width="200" height="253" alt="민서_미모티콘-removebg-preview" src="https://github.com/user-attachments/assets/0dc1260f-5525-4992-aed0-da08394cd918" /><br/>
      <a href="https://github.com/meentho"><b>김민서</b></a>
    </td>
    <td align="center">
            <img width="257" height="249" alt="다빈_갤럭시아바타-removebg-preview" src="https://github.com/user-attachments/assets/809d67c5-e9cd-4415-bbaf-ba1ef9c8de4a" /><br/>
      <a href="https://github.com/lego812"><b>박다빈</b></a>
    </td>
    <td align="center">
              <img width="239" height="249" alt="준영_미모티콘-removebg-preview" src="https://github.com/user-attachments/assets/6f20bbba-3258-43bf-a295-142d26b28c42" /><br/>
      <a href="https://github.com/Tarae0419"><b>박준영</b></a>
    </td>
    <td align="center">
<img width="257" height="257" alt="주성_미모티콘-removebg-preview" src="https://github.com/user-attachments/assets/d04c965c-907a-49b6-bf51-bbe42caf7c43" /><br/>
      <a href="https://github.com/ShonJuSeong"><b>손주성</b></a>
    </td>
    <td align="center">
<img width="258" height="255" alt="자영_미모티콘-removebg-preview" src="https://github.com/user-attachments/assets/51e2cbda-7616-4166-abfd-4f1ea5755d02" /><br/>
      <a href="https://github.com/cho-ja-young"><b>조자영</b></a>
    </td>
    <td align="center">
<img width="245" height="243" alt="예림_미모티콘-removebg-preview" src="https://github.com/user-attachments/assets/e297a9fe-ae27-4649-bd0c-5e6a04e94289" /><br/>
      <a href="https://github.com/cocoaocean"><b>조예림</b></a>
    </td>
  </tr>
</table>
</div>



# 💸 사회초년생을 위한 금융 상품 비교 & 추천 플랫폼 - Finpick

> **아직 금융과 안 친한 여러분들을 위한 픽! 핀픽 💡**  
> _"금융 문맹을 깨우는 한 방울의 소리, 금맹정음 팀의 프로젝트입니다."_

---

## 📌 프로젝트 소개

**FinPick**은 사회초년생을 위한 **금융 상품 비교 및 추천 플랫폼**입니다.  
금융이 어렵고 낯선 사용자들도 **챌린지, 캐릭터, 금융 퀴즈, 오늘의 금융 이슈 제공** 등 **재미 요소**와 함께  
쉽고 즐겁게 **자신에게 맞는 금융 상품을 선택하고 배워나갈 수 있는 공간**을 제공합니다.

> "더 이상 모른다고 외면하지 마세요. 핀픽이 도와드릴게요!"

---

## 🎯 프로젝트 개요

- **목표**
    - 금융 문맹의 재테크 진입 장벽을 낮추기 위함
    - 나에게 맞는 **금융 상품을 추천**받고, **비교**하여 **합리적인 선택**을 할 수 있도록 지원
    - 챌린지, 캐릭터 커스터마이징, 퀴즈, 뉴스 피드 등 **게임적 요소(Gamification)**를 통해 재미와 몰입도 향상

---

## 🧱 기술 스택 및 아키텍처

### ⚙️ 사용 기술 스택

| 분야             | 기술                                         |
|------------------|----------------------------------------------|
| 백엔드 서버      | Java, Spring Legacy (Gradle), MyBatis        |
| 데이터베이스     | MySQL, Redis                                  |
| 웹 서버          | Tomcat 9                                      |
| 인프라           | AWS EC2, Docker, Docker Hub                   |
| CI/CD 파이프라인 | GitHub Actions                                |
| 설계 도구        | ERD Cloud                                     |

---

### 🏗️ 아키텍처 구조

```text
┌────────────────────────────────────────────────────┐
│                      GitHub Actions                │
│     main 브랜치 푸시 시 자동 빌드 및 배포            │
└────────────────────────────────────────────────────┘
             ↓
     Docker 이미지 빌드 & Hub 푸시

┌────────────────────────────────────────────────────┐
│                      AWS EC2 (Free Tier 활용)       │
│                                                    │
│  ✅ Spring Legacy 서버 → Docker 컨테이너 실행        │
│  ✅ MySQL, Redis → 호스트 레벨 설치 및 실행          │
│     (Docker가 아닌 이유: 포트 충돌 및 비용 절감 목적)│
└────────────────────────────────────────────────────┘


```

### 왜 이렇게 설계했나요?
> 💰 비용을 최소화하면서도 성능과 확장성을 고려한 구조입니다.

AWS EC2 프리티어 계정을 기준으로 설계되었습니다.

MySQL과 Redis는 Docker 컨테이너로 격리하지 않고 EC2 호스트에 직접 설치하여,
단일 EC2 인스턴스에서 포트 충돌을 방지하고,
불필요한 Docker 볼륨 및 리소스 소모를 줄였습니다.

Redis와 MySQL 모두 EC2 내부에서 실행되기 때문에,
Docker 컨테이너(SPRING)와 내부 네트워크로 안정적인 통신이 가능합니다.

확장이 필요한 경우 향후 RDS, ElastiCache 등으로 쉽게 이전할 수 있는 구조입니다.


---
## 🚀 CI/CD 파이프라인 구성
> 자동 빌드, 배포 모두 GitHub Actions로 통합

### 단계	설명
1. CI	: main 브랜치에 push 시, Gradle 빌드 후 Docker 이미지 생성 및 Docker Hub에 푸시
2. CD	: EC2 서버에서 최신 이미지 pull → 기존 컨테이너 중지 및 제거 → 신규 컨테이너로 실행
3. 보안	: GitHub Secrets를 활용해 Docker Hub, SSH, 키, 환경 파일 자동 주입

### 실제 사용 파일
Dockerfile
- Tomcat 9 + JDK 17 기반
- python 라이브러리 설치
- .war 파일을 복사한 뒤 수동 압축 해제하여 루트 컨텍스트에 배포

deploy.yml
- CI: build/libs/*.war → Docker Hub
- CD: generate-properties.sh 실행 → 컨테이너 재구동



---

### 🧪 브랜치 전략
main: 배포용 브랜치 (CI/CD 연결)

dev: 팀원들의 기능 개발 결과를 머지하는 브랜치

feat/*: 개인 기능 개발 브랜치 (dev에서 분기 → dev로 PR)

---


---

### 🗃️ 데이터베이스 설계
📌 ERD 구조 (ERD Cloud)
[📷 ERD 링크 연결](https://www.erdcloud.com/d/st7GJkzBibHJHnQWg)

[추후 이미지 삽입 예정]

- 사용자, 금융 계좌 및 카드 정보, 거래내역

- 챌린지/퀴즈/코인 시스템 등 확장성 고려한 정규화 설계

- Redis 활용: JWT 리프레시 토큰 관리 및 세션 캐시 처리

---
