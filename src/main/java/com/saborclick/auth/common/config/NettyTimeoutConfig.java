package com.saborclick.auth.common.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import reactor.netty.http.server.HttpServer;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class NettyTimeoutConfig {

    private final Environment env;

    @Bean
    public WebServerFactoryCustomizer<NettyReactiveWebServerFactory> nettyCustomizer() {
        int connectTimeout = Integer.parseInt(env.getProperty("server.timeout.connect-ms", "10000"));
        int readTimeout = Integer.parseInt(env.getProperty("server.timeout.read-seconds", "10"));
        int writeTimeout = Integer.parseInt(env.getProperty("server.timeout.write-seconds", "10"));

        return factory -> factory.addServerCustomizers(httpServer ->
                httpServer
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                        .doOnConnection(conn -> {
                            conn.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.SECONDS));
                            conn.addHandlerLast(new WriteTimeoutHandler(writeTimeout, TimeUnit.SECONDS));
                        })
        );
    }
}
