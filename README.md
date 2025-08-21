# 💸 사회초년생을 위한 금융 상품 비교 & 추천 플랫폼 - FinPick

<div align="center">

<table align="center">
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/0dc1260f-5525-4992-aed0-da08394cd918" width="150" alt="김민서"/><br/>
      <a href="https://github.com/meentho"><b>김민서</b></a><br/>
      FE
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/809d67c5-e9cd-4415-bbaf-ba1ef9c8de4a" width="200" alt="박다빈"/><br/>
      <a href="https://github.com/lego812"><b>박다빈</b></a><br/>
      BE
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/6f20bbba-3258-43bf-a295-142d26b28c42" width="200" alt="박준영"/><br/>
      <a href="https://github.com/Tarae0419"><b>박준영</b></a><br/>
      BE
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/d04c965c-907a-49b6-bf51-bbe42caf7c43" width="200" alt="손주성"/><br/>
      <a href="https://github.com/ShonJuSeong"><b>손주성</b></a><br/>
      FE
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/51e2cbda-7616-4166-abfd-4f1ea5755d02" width="190" alt="조자영"/><br/>
      <a href="https://github.com/cho-ja-young"><b>조자영</b></a><br/>
      BE
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/e297a9fe-ae27-4649-bd0c-5e6a04e94289" width="200" alt="조예림"/><br/>
      <a href="https://github.com/cocoaocean"><b>조예림</b></a><br/>
      BE
    </td>
  </tr>
</table>

</div>

> **아직 금융과 안 친한 여러분들을 위한 픽! 핀픽 💡**  
> _"금융 문맹을 깨우는 한 방울의 소리, 금맹정음 팀의 프로젝트입니다."_

---

## ✨ Why FinPick?
- **문제**: 사회초년생은 금융 지식 부족으로 상품 선택이 어렵고, 재미 없는 정보에 쉽게 이탈함.
- **솔루션**: 금융 퀴즈, 챌린지, 캐릭터 등 Gamification을 더해 쉽고 재미있게 금융 상품을 비교·추천.

---

## 📌 프로젝트 소개
**FinPick**은 사회초년생을 위한 **금융 상품 비교 및 추천 플랫폼**입니다.  
금융이 어렵고 낯선 사용자들도 **챌린지, 캐릭터, 금융 퀴즈, 오늘의 금융 이슈 제공** 등 **재미 요소**와 함께  
쉽고 즐겁게 **자신에게 맞는 금융 상품을 선택하고 배워나갈 수 있는 공간**을 제공합니다.

> "더 이상 모른다고 외면하지 마세요. 핀픽이 도와드릴게요!"

---

## 🎯 프로젝트 개요
- 금융 문맹의 재테크 진입 장벽을 낮추기 위함
- 나에게 맞는 **금융 상품을 추천**받고, **비교**하여 **합리적인 선택**을 할 수 있도록 지원
- 챌린지, 캐릭터 커스터마이징, 퀴즈, 뉴스 피드 등 **게임적 요소(Gamification)**를 통해 재미와 몰입도 향상

---

## 📱 주요 기능
- **자산 조회**
  - 오픈뱅킹 API 연동을 통한 계좌/카드 조회
  - 거래 내역 저장 및 메모/카테고리 관리

- **재테크 추천**
  - 예금·적금: 금리 및 조건 기반 단순 비교
  - 펀드: 사용자 성향 반영 효용 함수 기반 점수화 후 추천
  - 주식: Carhart 4요인 모델 + 정보비율(IR)로 품질 평가 및 개인 성향 반영 추천
  - → 금융 문맹도 이해할 수 있도록 보수적/단순화된 방식으로 안내

- **챌린지 시스템**
  - 절약 챌린지, 친구와 경쟁 기능
  - 성취에 따른 코인·포인트 지급

- **금융 퀴즈**
  - 금융 용어 학습
  - 맞출 때마다 캐릭터 성장 및 보상

- **뉴스 피드**
  - 오늘의 금융 이슈 및 경제 동향 제공

- **캐릭터 커스터마이징**
  - 사용자의 금융 습관과 활동에 따라 캐릭터가 성장

---

## 🔍 FinPick의 차별성
- 단순 상품 비교가 아니라 **게임적 요소(Gamification)**로 재미와 학습을 동시에 제공
- **금융 문맹 사용자**도 쉽게 접근할 수 있는 UI/UX
- 확장성 고려한 **모듈형 아키텍처** 설계

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
포트 충돌을 방지하고, 불필요한 리소스 소모를 줄였습니다.  

Redis와 MySQL 모두 EC2 내부에서 실행되기 때문에,
Docker 컨테이너(SPRING)와 내부 네트워크로 안정적인 통신이 가능합니다.

확장이 필요한 경우 RDS, ElastiCache 등으로 쉽게 이전할 수 있습니다.

---

## 🚀 CI/CD 파이프라인
1. **CI**: main 브랜치 push → Gradle 빌드 → Docker 이미지 생성 → Docker Hub 푸시  
2. **CD**: EC2 서버 pull → 기존 컨테이너 중지/제거 → 신규 컨테이너 실행  
3. **보안**: GitHub Secrets 활용 (Docker Hub, SSH, 키, 환경 파일 자동 주입)

---

## 🧪 브랜치 전략
- **main**: 배포용 (CI/CD 연결)  
- **dev**: 기능 개발 결과 머지  
- **feat/***: 개인 기능 개발 브랜치 (→ dev PR)  

---

## 🗃️ 데이터베이스 설계
📌 ERD 구조 (ERD Cloud)  
[📷 ERD 링크](https://www.erdcloud.com/d/st7GJkzBibHJHnQWg)

- 사용자, 금융 계좌 및 카드 정보, 거래내역  
- 챌린지/퀴즈/코인 시스템 확장성 고려 정규화  
- Redis 활용: JWT 리프레시 토큰 관리 및 세션 캐시 처리

---

## 🌟 기대 효과
- 금융이 어려운 사회초년생도 쉽게 접근 가능  
- 게임적 요소로 재미와 학습 동시 달성  
- 실제 금융 습관 개선 및 합리적 상품 선택 유도  
