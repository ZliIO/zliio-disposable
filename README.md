# 🗑📮🌍 Disposable Email Verification Tool

This is a disposable email content verification tool package, implemented using the [disposable-email-domains ↗](https://github.com/disposable/disposable-email-domains) library provided by [Github/Disposable ↗](https://github.com/disposable).

The aim is to provide a means of detecting temporary email services provided by providers such as [10MinuteMail ↗](http://10minutemail.com/) , [GuerrillaMail ↗](https://www.guerrillamail.com/) , and determining whether an email is a disposable email.


## Why?

This tool can be used to verify whether the email address entered by users for login or registration is a temporary domain email, or to filter out existing temporary domain email addresses in the current system, for cleaning or other related processing.

## Usage
~~~xml
<!-- ADD Maven Dependency -->
<dependency>
    <groupId>com.zliio.disposable</groupId>
    <artifactId>disposable</artifactId>
    <version>1.0</version>
</dependency>
~~~

~~~java
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
    }
}
~~~

You can set the black and white lists of email domains by configuring the **domain-blacklist.txt** and **domain-whitelist.txt** files under the **_resources_** directory of the project. Additionally, you can dynamically add or remove domains using methods like **add*** and **remove*** provided by the **Disposable** _object instance_.

To ensure the update of [disposable-email-domains ↗](https://github.com/disposable/disposable-email-domains), we also provide the **_refreshDisposableDomains()_** method in the Disposable object instance to obtain the latest configuration list. However, we do not recommend frequent calls to this method due to the relatively high cost and long execution time of updates. We suggest that you call this method once after creating the **Disposable** _object instance_ to ensure the validity of the list.

For convenience, we provide separate solutions to validate email format, validate domain names, and validate disposable email addresses.

## [Domains ↗](https://github.com/disposable/disposable-email-domains)
### Generic lists with all domains

- TXT: https://disposable.github.io/disposable-email-domains/domains.txt
- JSON: https://disposable.github.io/disposable-email-domains/domains.json

### Hosts with validated DNS (a valid MX / A record):

- TXT: https://disposable.github.io/disposable-email-domains/domains_mx.txt
- JSON: https://disposable.github.io/disposable-email-domains/domains_mx.json

### List of SHA1

- TXT: https://disposable.github.io/disposable-email-domains/domains_sha1.txt
- JSON: https://disposable.github.io/disposable-email-domains/domains_sha1.json