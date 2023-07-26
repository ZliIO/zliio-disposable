package com.zliio.disposable;

/**
 * Disposable Test.
 *
 * @author Leo
 * @since 1.0 (2023-07-26)
 **/
public class DisposableTest {
    public static void main(String[] args) {
        Disposable disposable = new Disposable();

        System.out.println(disposable.validate("gmail.com"));
        // True
        System.out.println(disposable.validate("dsa@zliio.com"));
        // True
        System.out.println(disposable.validate("dsa@zliio.🤔️"));
        // False
        System.out.println(disposable.validate("test@0-mail.com"));
        // False
        disposable.refreshDisposableDomains();
    }
}
