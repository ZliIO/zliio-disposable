package com.zliio.disposable.example.springboot3;

import com.zliio.disposable.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * Demo Main.s
 *
 * @author 1TSC
 * @version 1.0
 * @since 2025/11/18
 */
@SpringBootApplication
public class DisposableExampleApplication3 {
    /**
     * Slf4j Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DisposableExampleApplication3.class);

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(DisposableExampleApplication3.class, args);
        Disposable disposable = applicationContext.getBean(Disposable.class);
        log.info("[gmail.com] is disposable domain ? > [{}]", disposable.validate("gmail.com"));
        log.info("[x.gmail.com] is disposable domain ? > [{}]", disposable.validate("x.gmail.com"));
        log.info("[10minutesmail.com] is disposable domain ? > [{}]", disposable.validate("10minutesmail.com"));
        log.info("[a.10minutesmail.com] is disposable domain ? > [{}]", disposable.validate("a.10minutesmail.com"));
        log.info("[test@zliio.com] is disposable email ? > [{}]", disposable.validate("dsa@zliio.com"));
        log.info("[test@0-mail.com] is disposable email ? > [{}]", disposable.validate("test@0-mail.com"));
        log.info("[test@a.0-mail.com] is disposable email ? > [{}]", disposable.validate("test@a.0-mail.com"));
        log.info("[test@a0-mail.com] is disposable email ? > [{}]", disposable.validate("test@a0-mail.com"));
        log.info("[test@zliio.🤔] is disposable email ? > [{}]", disposable.validate("test@zliio.🤔"));
    }
}
