package org.acme; // 패키지 선언

import jakarta.ws.rs.GET; // import문
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello") // 어노테이션
public class GreetingResource { // 클래스 선언

    @GET // 어노테이션
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() { // 메서드(객체 안에 함수) 선언
        return "Hello from Quarkus REST"; // 리턴문
    }
}