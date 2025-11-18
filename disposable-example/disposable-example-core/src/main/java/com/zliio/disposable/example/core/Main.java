package com.zliio.disposable.example.core;

import com.zliio.disposable.Disposable;
import com.zliio.disposable.DisposableDomainLoader;
import com.zliio.disposable.core.SuffixTrieDisposable;
import com.zliio.disposable.loader.BuiltinDomainLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo Main
 *
 * @author 1TSC
 * @version 1.0
 * @since 2025/11/18
 */
public class Main {
    /**
     * Slf4j Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        useBuiltinDomainTest();
        useCustomDomainTest();
    }

    public static void useBuiltinDomainTest() {
        Disposable disposable = new SuffixTrieDisposable();
        tryValidate(disposable);
    }

    public static void useCustomDomainTest() {
        DisposableDomainLoader disposableDomainLoader = new BuiltinDomainLoader("/disposable/re-domains.txt");
        Disposable disposable = new SuffixTrieDisposable(disposableDomainLoader);
        tryValidate(disposable);
    }

    public static void tryValidate(Disposable disposable) {
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
