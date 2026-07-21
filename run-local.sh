#!/usr/bin/env bash

# 중간 명령이 실패하면 스크립트 실행을 즉시 중단합니다.
set -e

# 어느 위치에서 실행하더라도 스크립트가 있는 프로젝트 루트로 이동합니다.
cd "$(dirname "$0")"

# .env의 DB 접속 정보를 현재 터미널 환경변수로 등록합니다.
set -a
source .env.local
set +a

# docker-compose.yaml에 정의된 PostgreSQL과 Redis를 백그라운드로 실행합니다.
docker compose up -d

# 기존 빌드 결과를 삭제하고 전체 테스트를 실행합니다.
./gradlew clean test

# 테스트 성공 후 Spring Boot 애플리케이션을 실행합니다.
./gradlew bootRun
