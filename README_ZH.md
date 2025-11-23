#  🗑📮🌍 Java 一次性域名/邮箱验证工具

[English](./README.md) | 中文

这是一个基于Java实现的一次性域名/邮箱验证工具包。它利用了 [disposable-email-domains ↗](https://github.com/disposable/disposable-email-domains) 提供的域名列表，旨在提供高效的临时（一次性）邮箱域名检测能力。

本工具包能够识别由 [10MinuteMail ↗](http://10minutemail.com/)、[GuerrillaMail ↗](https://www.guerrillamail.com/) 等服务提供商提供的一次性邮箱地址，有效判断其是否属于临时邮箱域名。

> **重要提示：** v2.0 版本已发布，与 v1.0 版本不兼容。请在使用前仔细阅读本文档，并特别留意版本间的API差异。

## ✨ 主要特性与应用场景

本工具包可用于多种业务场景：

*   **用户注册/登录验证**：在用户注册或登录时，即时验证其输入的邮箱地址是否为临时邮箱域名，有效阻止恶意注册和羊毛党行为。
*   **系统数据清理**：识别并过滤现有系统中的临时邮箱地址，便于后续的数据清理、用户管理或其他相关处理。
*   **API集成**：作为后端服务的一部分，为各种应用程序提供一次性邮箱验证能力。

## 🚀 使用方法

为满足不同Java项目的集成需求，我们提供了Spring Boot Starter和核心库（适用于标准Java项目）两种接入方式，方便用户快速集成。

### Spring Boot 项目接入

**1. 导入项目依赖**

在您的 `pom.xml` 文件中添加以下Maven依赖：

```xml
<dependency>
    <groupId>com.zliio.disposable</groupId>
    <artifactId>disposable-spring-boot-starter</artifactId>
    <version>2.0</version>
</dependency>
```

**2. 注入并使用 `Disposable` Bean**

依赖导入后，您可以直接在Spring Boot项目中注入 `Disposable` 接口的实现并使用：

```java
import com.zliio.disposable.Disposable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailValidationService {

    @Autowired
    private Disposable disposable;

    public boolean isDisposableEmail(String email) {
        // validate方法接受邮箱地址或纯域名
        return disposable.validate(email);
    }
}
```

**3. （可选）配置项**

如果您需要自定义工具的行为，可以在 `application.yml` 配置文件中添加如下内容进行配置：

```yaml
domain:
  disposable:
    # 匹配算法：支持 "SuffixTrie"（后缀树，默认）或 "BloomFilter"（布隆过滤器）
    # SuffixTrie 支持子域名匹配，BloomFilter 提供精确匹配但存在误报风险。
    algorithm: "SuffixTrie"
    # 一次性域名列表的加载方式
    loader:
      # 支持 "http"（从远程URL下载）或 "builtin"（使用内置列表）两种方式
      type: "http"
      # 当 type 为 "http" 时，指定域名列表的下载地址
      path: "https://disposable.github.io/disposable-email-domains/domains.txt"
```

### 非 Spring Boot 项目接入（核心库）

**1. 导入项目依赖**

在您的 `pom.xml` 文件中添加核心库的Maven依赖：

```xml
<dependency>
    <groupId>com.zliio.disposable</groupId>
    <artifactId>disposable-core</artifactId>
    <version>2.0</version>
</dependency>
```

**2. 示例使用**

您可以直接实例化 `Disposable` 接口的实现类进行使用，例如 `SuffixTrieDisposable`：

```java
import com.zliio.disposable.Disposable;
import com.zliio.disposable.core.SuffixTrieDisposable;

public class DisposableEmailValidator {
    public static void main(String[] args) {
        // 使用默认的内置域名列表初始化 SuffixTrieDisposable
        Disposable disposable = new SuffixTrieDisposable();
      
        System.out.println("gmail.com is disposable: " + disposable.validate("gmail.com"));
        System.out.println("dsa@zliio.com is disposable: " + disposable.validate("dsa@zliio.com"));
        System.out.println("dsa@zliio.🤔️ is disposable: " + disposable.validate("dsa@zliio.🤔️")); // 支持国际化域名
        System.out.println("test@0-mail.com is disposable: " + disposable.validate("test@0-mail.com"));
    }
}
```

**3. （可选）通过构造函数进行配置**

如果您需要自定义域名列表的加载方式，可以通过 `SuffixTrieDisposable` 的构造函数传入自定义的 `DisposableDomainLoader` 实现：

```java
import com.zliio.disposable.Disposable;
import com.zliio.disposable.core.SuffixTrieDisposable;
import com.zliio.disposable.loader.BuiltinDomainLoader;
import com.zliio.disposable.loader.DisposableDomainLoader;

public class CustomDisposableEmailValidator {
    public static void main(String[] args) {
        // 示例：使用自定义路径的内置域名列表加载器
        // 这里的路径应指向资源文件，例如 src/main/resources/META-INF/disposable/re-domains.txt
        DisposableDomainLoader disposableDomainLoader = new BuiltinDomainLoader("/META-INF/disposable/re-domains.txt");
        Disposable disposable = new SuffixTrieDisposable(disposableDomainLoader);
      
        System.out.println("gmail.com is disposable: " + disposable.validate("gmail.com"));
        System.out.println("dsa@zliio.com is disposable: " + disposable.validate("dsa@zliio.com"));
        System.out.println("dsa@zliio.🤔️ is disposable: " + disposable.validate("dsa@zliio.🤔️"));
        System.out.println("test@0-mail.com is disposable: " + disposable.validate("test@0-mail.com"));
    }
}
```

当核心库成功加载并初始化时，您会在控制台看到类似的日志输出：

```shell
[           main] c.z.disposable.DisposableDomainLoader    : Lazy-loading built-in domains from /META-INF/disposable/domains.txt...'
[           main] c.z.disposable.DisposableDomainLoader    : Successfully loaded 71904 built-in disposable domains.
[           main] com.zliio.disposable.DomainSuffixTrie    : Domain contains empty labels (e.g., '..', '.com'), domain: .tooltip.bottom
[           main] c.z.d.core.SuffixTrieDisposable          : Suffix trie initialized with 71904 domains/rules.
```

## 🧠 核心特性说明

### 匹配算法选择

v2.0 版本提供了两种核心算法实现：`SuffixTrie`（后缀树）和 `BloomFilter`（布隆过滤器）。

*   **`SuffixTrie` (后缀树)**：
    *   **优点**：能够进行包含子域名的有效匹配（例如，如果 `example.com` 是临时域名，`sub.example.com` 也会被识别为临时）。提供精准且全面的匹配结果。
    *   **适用场景**：对匹配精度要求高，需要覆盖子域名的情况。
*   **`BloomFilter` (布隆过滤器)**：
    *   **优点**：空间效率极高，查询速度快。
    *   **缺点**：只能提供精确匹配，且存在一定程度的误报（False Positive）概率，即可能将非一次性邮箱误判为一次性邮箱，但不会漏报。
    *   **适用场景**：对内存占用和查询速度有极致要求，且能容忍少量误报的场景。

请根据您的业务场景和对精度、性能的要求，选择合适的算法。

### 域名列表加载器

本工具包支持两种域名列表加载方式，以适应不同的部署环境：

*   **远程HTTP加载 (`http`)**：
    *   **说明**：通过指定URL（如 `https://disposable.github.io/disposable-email-domains/domains.txt`）动态下载最新的域名列表。
    *   **优点**：可以随时获取最新的域名列表，无需重新打包部署。
*   **内置加载 (`builtin`)**：
    *   **说明**：使用打包在JAR包内部的域名列表文件。
    *   **优点**：主要考虑到部分生产环境可能没有公网访问权限，可以确保在离线环境下也能正常工作。

### Maven 插件（离线环境更新方案）

为了兼顾无公网访问权限的生产环境对域名列表更新的需求，我们提供了Maven插件。该插件可在项目打包（`compile` 阶段）时自动从指定远程URL下载最新的域名列表，并将其内置到最终的JAR包中，从而实现离线环境下的列表更新。

```xml
<plugin>
    <groupId>com.zliio.disposable</groupId>
    <artifactId>disposable-plugin</artifactId>
    <version>2.0</version>
    <executions>
        <execution>
             <id>load-domains</id>
             <phase>compile</phase> <!-- 在编译阶段执行 -->
             <goals>
                 <goal>loading</goal>
             </goals>
        </execution>
    </executions>
    <configuration>
        <!-- 配置远程域名列表的下载地址 -->
        <domainsUrl>
            https://disposable.github.io/disposable-email-domains/domains.txt
        </domainsUrl>
    </configuration>
</plugin>
```

## 🌐 域名列表来源

本工具包依赖于 [disposable/disposable-email-domains ↗](https://github.com/disposable/disposable-email-domains) 项目提供的域名列表。以下是常用列表的直接链接：

### 包含所有域名的通用列表

*   **TXT 格式**：[https://disposable.github.io/disposable-email-domains/domains.txt](https://disposable.github.io/disposable-email-domains/domains.txt)
*   **JSON 格式**：[https://disposable.github.io/disposable-email-domains/domains.json](https://disposable.github.io/disposable-email-domains/domains.json)

### 经过 DNS 验证的主机列表（具有有效的 MX / A 记录）

*   **TXT 格式**：[https://disposable.github.io/disposable-email-domains/domains_mx.txt](https://disposable.github.io/disposable-email-domains/domains_mx.txt)
*   **JSON 格式**：[https://disposable.github.io/disposable-email-domains/domains_mx.json](https://disposable.github.io/disposable-email-domains/domains_mx.json)

### SHA1 哈希列表

*   **TXT 格式**：[https://disposable.github.io/disposable-email-domains/domains_sha1.txt](https://disposable.github.io/disposable-email-domains/domains_sha1.txt)
*   **JSON 格式**：[https://disposable.github.io/disposable-email-domains/domains_sha1.json](https://disposable.github.io/disposable-email-domains/domains_sha1.json)