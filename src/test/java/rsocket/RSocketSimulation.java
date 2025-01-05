package rsocket;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.rsocket.Payload;
import io.rsocket.core.RSocketConnector;

import io.gatling.javaapi.core.*;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import io.rsocket.util.DefaultPayload;

import java.net.URI;
import java.time.Duration;

public class RSocketSimulation extends Simulation {

    // RSocket 설정
    private RSocketConnector connector = RSocketConnector.create()
            .keepAlive(Duration.ofSeconds(20), Duration.ofSeconds(90)); // Keep-Alive 설정

    // TCP 또는 WebSocket 선택 (아래 중 하나 주석 해제)
    // private TcpClientTransport tcpTransport = TcpClientTransport.create("localhost", 7000); // TCP
    private WebsocketClientTransport wsTransport = WebsocketClientTransport.create(URI.create("ws://localhost:8080/rsocket"));

    // 테스트 시나리오 정의
    ScenarioBuilder scn = scenario("RSocket Load Test")
            .exec(session -> {
                // RSocket 연결 및 데이터 전송
                connector.connect(wsTransport) // WebSocket 전송 사용
                        .flatMap(rSocket -> rSocket.requestResponse(
                                DefaultPayload.create("Hello RSocket!")
                        ))
                        .map(Payload::getDataUtf8)
                        .doOnNext(response -> System.out.println("Response: " + response))
                        .block(); // 블로킹 방식으로 실행

                return session;
            });

    // 테스트 설정
    {
        setUp(
                scn.injectOpen(
                        atOnceUsers(100),               // 100명 동시 요청
                        rampUsers(500).during(60)      // 60초 동안 500명 점진적 증가
                )
        );
    }
}
