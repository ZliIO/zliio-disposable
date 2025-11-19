package com.zliio.disposable.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Mojo to download a domain configuration file during the compile phase.
 * This plugin fetches a list of domains from a specified URL and saves it
 * into the project's build output directory, typically for inclusion in the
 * final artifact. This version is compatible with Java 1.8.
 *
 * @author 1TSC
 * @version 1.0
 * @since 2025/11/19
 */
@Mojo(name = "auto-loading", defaultPhase = LifecyclePhase.COMPILE)
public class DomainsLoadingMojo extends AbstractMojo {
    /**
     * The URL from which to download the domains.txt file.
     * This can be configured in the project's pom.xml file.
     *
     * <pre>
     * &lt;configuration&gt;
     *   &lt;url&gt;https://example.com/custom-domains.txt&lt;/domainsUrl&gt;
     * &lt;/configuration&gt;
     * </pre>
     */
    @Parameter(property = "domainsUrl", defaultValue = "https://disposable.github.io/disposable-email-domains/domains.txt")
    private String domainsUrl;
    /**
     * The Maven project instance. This is automatically injected by Maven and
     * used to access project-specific paths, such as the build output directory.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    /**
     * The directory where the downloaded file will be saved, relative to the build output directory.
     * The final path will be `target/classes/${destinationDir}`.
     */
    private static final String DESTINATION_DIR = "META-INF/disposable";
    /**
     * The name of the file to be saved.
     */
    private static final String DESTINATION_FILE_NAME = "domains.txt";
    /**
     * Connection timeout in milliseconds.
     */
    private static final int CONNECT_TIMEOUT_MS = 10000; // 10 seconds
    /**
     * Read timeout in milliseconds.
     */
    private static final int READ_TIMEOUT_MS = 10000; // 10 seconds
    /**
     * Executes the Mojo. This is the main entry point for the plugin's logic.
     *
     * @throws MojoExecutionException if an unrecoverable error occurs.
     */
    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("Starting domains file download from: " + domainsUrl);
        HttpURLConnection connection = null;
        try {
            // 1. Create URL and open a connection using HttpURLConnection for Java 8 compatibility
            URL url = new URL(this.domainsUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setInstanceFollowRedirects(true); // Automatically handle redirects
            // 2. Check the HTTP response code
            File destinationDirectory = loadFile(connection);
            if (!destinationDirectory.exists()) {
                getLog().info("Creating destination directory: " + destinationDirectory.getAbsolutePath());
                if (!destinationDirectory.mkdirs()) {
                    throw new MojoExecutionException("Could not create destination directory: " + destinationDirectory.getAbsolutePath());
                }
            }
            File destinationFile = new File(destinationDirectory, DESTINATION_FILE_NAME);
            // Use try-with-resources to ensure the InputStream is closed automatically
            try (InputStream inputStream = connection.getInputStream()) {
                Files.copy(inputStream, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            getLog().info("Successfully downloaded and saved domains file to: " + destinationFile.getAbsolutePath());
        } catch (IOException e) {
            getLog().error("An error occurred during file download or processing.", e);
            throw new MojoExecutionException("Failed to download or save the domains file.", e);
        } finally {
            // Ensure the connection is disconnected
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private File loadFile(HttpURLConnection connection) throws IOException, MojoExecutionException {
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) { // HTTP_OK is 200
            throw new MojoExecutionException("Failed to download file. Server responded with status code: " + responseCode);
        }
        // 3. Prepare the destination path and save the file
        File outputDirectory = new File(project.getBuild().getOutputDirectory());
        return new File(outputDirectory, DESTINATION_DIR);
    }
}