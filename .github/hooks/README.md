# Git Commit Hook 안내

이 프로젝트는 커밋 메시지 컨벤션을 자동으로 검사합니다.  
형식에 맞지 않으면 커밋이 거부됩니다.

## 커밋 메시지 형식

> <타입> : <제목>
> <br><br> 예시) feat: 로그인 기능 추가

- 제목은 50자 이내
- 마침표 금지
- 타입 예시: feat, fix, docs, style, refactor, test, chore

---

## 설정 방법 (최초 1회)

```bash
ln -s ../../.github/hooks/commit-msg .git/hooks/commit-msg
chmod +x .github/hooks/commit-msg
```
=> setup.sh 파일로 만들어둠, 아래의 코드를 터미널에서 실행

```터미널에서 실행
bash .github/hooks/setup.sh
```