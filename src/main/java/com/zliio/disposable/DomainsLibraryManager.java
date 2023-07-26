package com.zliio.disposable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Domains Library Manager.
 *
 * @author Leo
 * @since 1.0 (2023-07-26)
 **/
public class DomainsLibraryManager {

    /**
     * log.
     */
    private static final Logger log = LoggerFactory.getLogger(DomainsLibraryManager.class);

    /**
     * Disposable Email Domains Set.
     */
    private static Set<String> disposableEmailDomainsSet = new HashSet<>();

    /**
     * White Email Domains Set.
     */
    private static Set<String> whiteEmailDomainsSet = new HashSet<>();

    /**
     * White Email Domains Set.
     */
    private static Set<String> blackEmailDomainsSet = new HashSet<>();


    static {
        DomainsLibraryManager.disposableEmailDomainsSet = fetchResourceDomains("disposable-domains.txt");
        DomainsLibraryManager.blackEmailDomainsSet = fetchResourceDomains("domain-blacklist.txt");
        DomainsLibraryManager.whiteEmailDomainsSet = fetchResourceDomains("domain-whitelist.txt");
    }

    public static void refreshDisposableDomains() {
        refreshDisposableDomainsByTxtUrl(DomainsLibraryPath.GENERIC_LISTS_TXT);
    }

    public static Boolean containDomain(String domain) {
        domain = domain.toLowerCase();
        if (blackEmailDomainsSet.contains(domain)) {
            return Boolean.TRUE;
        }
        if (whiteEmailDomainsSet.contains(domain)) {
            return Boolean.FALSE;
        }
        return disposableEmailDomainsSet.contains(domain.toLowerCase());
    }

    public static void addBlackDomain(String domain) {
        DomainsLibraryManager.blackEmailDomainsSet.add(domain.toLowerCase());
    }

    public static void addWhiteDomain(String domain) {
        DomainsLibraryManager.whiteEmailDomainsSet.add(domain.toLowerCase());
    }

    public static void removeBlackDomain(String domain) {
        DomainsLibraryManager.blackEmailDomainsSet.remove(domain.toLowerCase());
    }

    public static void removeWhiteDomain(String domain) {
        DomainsLibraryManager.whiteEmailDomainsSet.remove(domain.toLowerCase());
    }


    public static void refreshDisposableDomainsByTxtUrl(String txtUrlPath) {
        Set<String> targetDisposableEmailDomainsSet = fetchDomainsWithTxt(txtUrlPath);
        if (null == targetDisposableEmailDomainsSet || targetDisposableEmailDomainsSet.isEmpty()) {
            throw new RuntimeException("DomainsLibraryManager.refreshDomainsByTxtUrl, no domains value");
        } else {
            // Count the added and removed information.
            Set<String> tmpDomainsSet = new HashSet<>(targetDisposableEmailDomainsSet);
            tmpDomainsSet.removeAll(DomainsLibraryManager.disposableEmailDomainsSet);
            log.info("DomainsLibraryManager :: Added : {} Domain(s)", tmpDomainsSet.size());
            tmpDomainsSet = new HashSet<>(DomainsLibraryManager.disposableEmailDomainsSet);
            tmpDomainsSet.removeAll(targetDisposableEmailDomainsSet);
            log.info("DomainsLibraryManager :: Removed : {} Domain(s)", tmpDomainsSet.size());
            synchronized (DomainsLibraryManager.class) {
                DomainsLibraryManager.disposableEmailDomainsSet = targetDisposableEmailDomainsSet;
            }
        }
    }

    public static Set<String> fetchResourceDomains(String fileName) {
        Set<String> targetDisposableEmailDomainsSet = new HashSet<>();
        try (InputStream ins = DomainsLibraryManager.class.getResourceAsStream("/" + fileName)) {
            assert ins != null;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(ins))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    targetDisposableEmailDomainsSet.add(inputLine);
                }
            }
            log.info("DomainsLibraryManager :: {} : {} Domain(s)", fileName, targetDisposableEmailDomainsSet.size());
            return targetDisposableEmailDomainsSet;
        } catch (IOException ignore) {
            return Collections.emptySet();
        }
    }

    public static Set<String> fetchDomainsWithTxt(String urlPath) {
        Set<String> disposableEmailDomainsSet = new HashSet<>();
        try {
            URL url = new URL(urlPath);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                disposableEmailDomainsSet.add(inputLine);
            }
            in.close();
            return disposableEmailDomainsSet;
        } catch (IOException ioe) {
            return disposableEmailDomainsSet;
        }
    }
}
