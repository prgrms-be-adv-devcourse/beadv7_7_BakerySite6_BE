## Requirement

maver.log 도 지원 받을 수 있도록 확장 해주세요.

- 기존 kokoa 포맷 (bracket 형태) 지원 유지
- 새로운 maver 포맷 (JSON 형태) 지원 추가
- log 샘플: docs/log/maver.log
- 기존 로직을 재사용하거나 리팩토링해도 좋습니다.

## Details

### 새로운 maver 포맷 (JSON)

Logstash 형태의 JSON 로그입니다. 각 줄이 하나의 JSON 객체입니다.

```json
{
  "@timestamp": "2012-06-10T08:00:00.000Z", // ISO 8601 형태의 타임스탬프
  "status_code": 200, // 상태코드 (10, 200, 404)
  "url": "http://apis.maver.com/v1/weather", // 전체 URL
  "service_id": "weather", // API 서비스 ID (weather, stock, news, map 등)
  "api_key": "a1b2c3" // API 키 (6자리 문자열)
}
```
