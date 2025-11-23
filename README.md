# 🗑📮🌍 Java Disposable Email Domain Validation Tool

English | [中文](./README_ZH.md)

This is a Java-based toolkit for validating disposable email domains. It leverages the [disposable-email-domains ↗](https://github.com/disposable/disposable-email-domains) library to efficiently detect temporary email addresses.

The tool identifies disposable email services provided by platforms like [10MinuteMail ↗](http://10minutemail.com/) and [GuerrillaMail ↗](https://www.guerrillamail.com/), allowing you to determine if an email is from a temporary or one-time use provider.

> **Important Note:** Version 2.0 has been released and introduces breaking changes compared to v1.0. Please review this documentation carefully before upgrading or integrating, paying close attention to API differences.

## ✨ Key Features & Use Cases

This toolkit is versatile and can be applied in various business scenarios:

*   **User Authentication (Registration/Login):** Immediately validate if an email address entered by a user is from a disposable domain, effectively preventing malicious registrations and abuse.
*   **System Data Cleanup:** Identify and filter out existing disposable email addresses within your system, facilitating subsequent data cleanup, user management, or other related processes.
*   **API Integration:** Serve as a backend service component, providing disposable email validation capabilities for various applications.

## 🚀 How to Use

To accommodate diverse Java project integration needs, we offer two primary approaches: a Spring Boot Starter and a core library (for standard Java projects), enabling quick integration.

### Spring Boot Projects

**1. Import Project Dependency**

Add the following Maven dependency to your `pom.xml` file:

```xml
<!-- maven -> disposable-spring-boot-starter -->
<dependency>
    <groupId>com.zliio.disposable</groupId>
    <artifactId>disposable-spring-boot-starter</artifactId>
    <version>2.0</version>
</dependency>
```

**2. Inject and Use the `Disposable` Bean**

Once the dependency is imported, you can directly inject the `Disposable` interface into your Spring Boot components:

```java
import com.zliio.disposable.Disposable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailValidationService {

    @Autowired
    private Disposable disposable;

    public boolean isDisposableEmail(String email) {
        // The validate method accepts either a full email address or just a domain.
        return disposable.validate(email);
    }
}
```

**3. (Optional) Configuration**

If you wish to configure the tool's behavior, you can add the following settings to your `application.yml` file:

```yaml
domain:
  disposable:
    # Matching algorithm: "SuffixTrie" (Suffix Tree, default) or "BloomFilter".
    # SuffixTrie supports subdomain matching. BloomFilter offers exact match but has a risk of false positives.
    algorithm: "SuffixTrie"
    # Loading method for the disposable domain list.
    loader:
      # Supported types: "http" (download from remote URL) or "builtin" (use internal list).
      type: "http"
      # When type is "http", specify the URL for the domain list.
      path: "https://disposable.github.io/disposable-email-domains/domains.txt"
```

### Non-Spring Boot Projects

**1. Import Project Dependency**

Add the core library's Maven dependency to your `pom.xml` file:

```xml
<dependency>
    <groupId>com.zliio.disposable</groupId>
    <artifactId>disposable-core</artifactId>
    <version>2.0</version>
</dependency>
```

**2. Example Usage**

You can directly instantiate an implementation of the `Disposable` interface, such as `SuffixTrieDisposable`:

```java
import com.zliio.disposable.Disposable;
import com.zliio.disposable.core.SuffixTrieDisposable;

public class DisposableEmailValidator {
    public static void main(String[] args) {
        // Initialize SuffixTrieDisposable with the default built-in domain list
        Disposable disposable = new SuffixTrieDisposable();
      
        System.out.println("gmail.com is disposable: " + disposable.validate("gmail.com"));
        System.out.println("dsa@zliio.com is disposable: " + disposable.validate("dsa@zliio.com"));
        System.out.println("dsa@zliio.🤔️ is disposable: " + disposable.validate("dsa@zliio.🤔️")); // Supports internationalized domains
        System.out.println("test@0-mail.com is disposable: " + disposable.validate("test@0-mail.com"));
    }
}
```

**3. (Optional) Configuration via Constructor**

If you need to customize how the domain list is loaded, you can pass a custom `DisposableDomainLoader` implementation to the `SuffixTrieDisposable` constructor:

```java
import com.zliio.disposable.Disposable;
import com.zliio.disposable.core.SuffixTrieDisposable;
import com.zliio.disposable.loader.BuiltinDomainLoader;
import com.zliio.disposable.loader.DisposableDomainLoader;

public class CustomDisposableEmailValidator {
    public static void main(String[] args) {
        // Example: Using a BuiltinDomainLoader with a custom resource path.
        // This path should point to a resource file within your JAR, e.g., src/main/resources/META-INF/disposable/re-domains.txt
        DisposableDomainLoader disposableDomainLoader = new BuiltinDomainLoader("/META-INF/disposable/re-domains.txt");
        Disposable disposable = new SuffixTrieDisposable(disposableDomainLoader);
      
        System.out.println("gmail.com is disposable: " + disposable.validate("gmail.com"));
        System.out.println("dsa@zliio.com is disposable: " + disposable.validate("dsa@zliio.com"));
        System.out.println("dsa@zliio.🤔️ is disposable: " + disposable.validate("dsa@zliio.🤔️"));
        System.out.println("test@0-mail.com is disposable: " + disposable.validate("test@0-mail.com"));
    }
}
```

When the core library successfully loads and initializes, you will see similar log outputs in your console:

```shell
[           main] c.z.disposable.DisposableDomainLoader    : Lazy-loading built-in domains from /META-INF/disposable/domains.txt...'
[           main] c.z.disposable.DisposableDomainLoader    : Successfully loaded 71904 built-in disposable domains.
[           main] com.zliio.disposable.DomainSuffixTrie    : Domain contains empty labels (e.g., '..', '.com'), domain: .tooltip.bottom
[           main] c.z.d.core.SuffixTrieDisposable          : Suffix trie initialized with 71904 domains/rules.
```

## 🧠 Core Features Explained

### Matching Algorithm Selection

Version 2.0 provides two core algorithm implementations: `SuffixTrie` and `BloomFilter`.

*   **`SuffixTrie` (Suffix Tree)**:
    *   **Pros**: Capable of effective matching that includes subdomains (e.g., if `example.com` is disposable, `sub.example.com` will also be identified). Provides accurate and comprehensive matching results.
    *   **Use Cases**: Ideal for scenarios requiring high matching precision and coverage of subdomains.
*   **`BloomFilter` (Bloom Filter)**:
    *   **Pros**: Extremely space-efficient and offers fast query speeds.
    *   **Cons**: Provides only exact matching and has a certain probability of false positives (i.e., a non-disposable email might be mistakenly identified as disposable), but no false negatives.
    *   **Use Cases**: Suitable for scenarios with stringent memory and speed requirements where a small percentage of false positives is acceptable.

Please choose the appropriate algorithm based on your specific business requirements for accuracy and performance.

### Domain List Loader

This toolkit supports two domain list loading methods to accommodate different deployment environments:

*   **Remote HTTP Loading (`http`)**:
    *   **Description**: Dynamically downloads the latest domain list from a specified URL (e.g., `https://disposable.github.io/disposable-email-domains/domains.txt`).
    *   **Pros**: Always fetches the most up-to-date domain list without requiring re-packaging or re-deployment.
*   **Built-in Loading (`builtin`)**:
    *   **Description**: Utilizes a domain list file bundled within the JAR package.
    *   **Pros**: Primarily designed for production environments that may lack public internet access, ensuring functionality in offline scenarios.

### Maven Plugin (for Offline Updates)

To address the need for domain list updates in offline production environments, we provide a Maven plugin. This plugin automatically downloads the latest domain list from a specified remote URL during the project's build phase (`compile`) and embeds it into the final JAR package, thereby enabling offline list updates.

```xml
<plugin>
    <groupId>com.zliio.disposable</groupId>
    <artifactId>disposable-plugin</artifactId>
    <version>2.0</version>
    <executions>
        <execution>
             <id>load-domains</id>
             <phase>compile</phase> <!-- Executes during the compile phase -->
             <goals>
                 <goal>loading</goal>
             </goals>
        </execution>
    </executions>
    <configuration>
        <!-- Configure the URL for downloading the remote domain list -->
        <domainsUrl>
            https://disposable.github.io/disposable-email-domains/domains.txt
        </domainsUrl>
    </configuration>
</plugin>
```

## 🌐 Domain List Sources

This toolkit relies on the domain lists provided by the [disposable/disposable-email-domains ↗](https://github.com/disposable/disposable-email-domains) project. Below are direct links to commonly used lists:

### General List (Includes All Domains)

*   **TXT Format**: [https://disposable.github.io/disposable-email-domains/domains.txt](https://disposable.github.io/disposable-email-domains/domains.txt)
*   **JSON Format**: [https://disposable.github.io/disposable-email-domains/domains.json](https://disposable.github.io/disposable-email-domains/domains.json)

### DNS Validated Host List (Domains with valid MX / A records)

*   **TXT Format**: [https://disposable.github.io/disposable-email-domains/domains_mx.txt](https://disposable.github.io/disposable-email-domains/domains_mx.txt)
*   **JSON Format**: [https://disposable.github.io/disposable-email-domains/domains_mx.json](https://disposable.github.io/disposable-email-domains/domains_mx.json)

### SHA1 Hash List

*   **TXT Format**: [https://disposable.github.io/disposable-email-domains/domains_sha1.txt](https://disposable.github.io/disposable-email-domains/domains_sha1.txt)
*   **JSON Format**: [https://disposable.github.io/disposable-email-domains/domains_sha1.json](https://disposable.github.io/disposable-email-domains/domains_sha1.json)