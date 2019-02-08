# 오라클저장

카프카에서 가져온 이벤트를 오라클에 저장하는 기능을 구현한 스파크애플리케이션.
JIRA 이슈 [DV020-3133](http://devops.sktelecom.com/mytask/browse/DV020-3133) 참조.
  

## 애플리케이션 빌드 및 배포

Jenkins [DEV - TosStreamOracle](http://150.2.181.117:28080/job/DEV%20-%20TosStreamOracle/) 프로젝트 활용

## 애플리케이션 실행

### 클러스터에서 실행

사용하는 메인클래스는 다음과 같음.

```
com.sktelecom.tos.stream.oracle.OracleStreamAppMain
```

### 로칼 JVM에서 실행

사용하는 메인클래스는 다음과 같으며, master 설정으로 '`local[2]`' 을 사용함.

```
com.sktelecom.tos.stream.oracle.OracleStreamAppLocalMain
```

## 관리기능

JMX 를 활용해서 관리하며, 애플리케이션을 정지시키거나, 메타 정보를 재적재하게 할 수 있음.

JMX 를 활용하므로 메인 애플리케이션 기동 시, VM 옵션으로 다음과 같이 추가해야 함.

```
-Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
```

### OracleStreamApp 를  질서 있게 정지

```
$ java com.sktelecom.tos.stream.oracle.StreamingContextStopper true
```

### OracleStreamApp 를 즉시 정지

```
$ java com.sktelecom.tos.stream.oracle.StreamingContextStopper
```

또는

```
$ java com.sktelecom.tos.stream.oracle.StreamingContextStopper false
```

### 메타 정보 재적재

```
$ java com.sktelecom.tos.stream.oracle.MetaReloader
```

### JConsole 을 사용할 경우, 필요한 사항

오브젝트 이름으로 다음과 같이 사용함.

```
com.sktelecom.tos.stream.oracle:type=context
com.sktelecom.tos.stream.oracle:type=meta
```

## 로깅 관련 설정

SLF4J 를 사용하며, 백엔드에서 Log4J 1.2 를 사용.

### log4j.properties 에서 설정할 수 있는 주요 로거

```
log4j.logger.com.sktelecom.tos.stream.oracle.OracleStreamApp
log4j.logger.com.sktelecom.tos.stream.oracle.OracleTransformerMetaManager
log4j.logger.com.sktelecom.tos.stream.oracle.OracleTransformer
log4j.logger.com.sktelecom.tos.stream.oracle.R2kMapper <-- depricated
```

### 주요 로깅 레벨

```
TRACE, INFO, WARN
```
