## Requirement

Open API 담당자인 당신은 로그 분석을 위한 프로그램이 필요하다.  
Open API 호출 기록은 모두 웹 로그 형태로 남아있다.  
첨부한 로그 파일을 분석하여

1. 최다호출 APIKEY
2. (호출 횟수 기준) 상위 3개의 API Service ID 와 각각의 요청 수
3. 웹브라우저별 사용 `비율`

을 파일로 출력하는 프로그램을 구현해 보자.

## Details

입력 구문은 다음과 같은 형식이다. 각 필드는 [] 로 구분된다.

```
[상태코드][URL][웹브라우저][호출시간]
```

### 상태코드

- 10 : apikey가 파라미터에 없는 오류
- 200 : 성공
- 404 : 페이지 없음

### URL

- http://apis.kokoa.com/search 이후부터 ? 까지는 API Service ID
- `API Service ID` blog, book, image, knowledge, news, vclip
- `apikey` 영문자/숫자가 혼용된 4자리 문자열

### 웹브라우저

- 호출한 웹브라우저 종류 (IE, Firefox, Safari, Chrome, Opera 중 하나)

### 호출시간

- 해당 URL이 호출된 시간
- 로그에 기록된 시간은 24시간제
