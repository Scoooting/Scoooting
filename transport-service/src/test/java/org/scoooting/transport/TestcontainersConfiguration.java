package org.scoooting.transport;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    private static final Network NETWORK = Network.newNetwork();
    private final Map<String, String> envVars = readEnvFile();

    @Bean
    public Network network() {
        return NETWORK;
    }

    static Map<String, String> readEnvFile() {
        Map<String, String> envVars = new HashMap<>();
        try {
            String envFilePath = Paths.get("").toAbsolutePath().getParent().resolve(".env").toString();
            Properties props = new Properties();
            props.load(new FileReader(envFilePath));

            envVars.put("CONFIG_USERNAME", props.getProperty("CONFIG_USERNAME"));
            envVars.put("CONFIG_TOKEN", props.getProperty("CONFIG_TOKEN"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return envVars;
    }

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>("postgres:latest")
                .withExposedPorts(5432)
                .withUsername("postgres")
                .withPassword("postgres_pass")
                .withNetwork(NETWORK)
                .withNetworkAliases("postgres-test")
                .withCopyFileToContainer(
                        MountableFile.forClasspathResource("init_db.sql"),
                        "/docker-entrypoint-initdb.d/init.sql"
                );
    }

    @DynamicPropertySource
    void dynamicProperties(DynamicPropertyRegistry registry) {

    }

    @Bean
    public GenericContainer<?> eurekaServerContainer() {
        return new GenericContainer<>(
                DockerImageName.parse("scoooting-eureka-server:latest"))
                .withNetwork(NETWORK)
                .withNetworkAliases("eureka-server")
                .withExposedPorts(8761)
                .waitingFor(Wait.forHttp("/actuator/health").forPort(8761));
    }

    @Bean
    public GenericContainer<?> configServerContainer() {
        return new GenericContainer<>(
                DockerImageName.parse("scoooting-config-server:latest"))
                .withNetwork(NETWORK)
                .withNetworkAliases("config-server")
                .withExposedPorts(8888)
                .withEnv("CONFIG_USERNAME", envVars.get("CONFIG_USERNAME"))
                .withEnv("CONFIG_TOKEN", envVars.get("CONFIG_TOKEN"))
                .waitingFor(Wait.forHttp("/actuator/health").forPort(8888));
    }

    @Bean
    public GenericContainer<?> userServiceContainer(
            PostgreSQLContainer<?> postgreSQLContainer,
            GenericContainer<?> eurekaServerContainer,
            GenericContainer<?> configServerContainer) {

        return new GenericContainer(
                DockerImageName.parse("scoooting-user-service:latest"))
                .withNetwork(NETWORK)
                .withNetworkAliases("user-service")
                .withExposedPorts(8081)
                .withEnv("CONFIG_SERVER_URI", "http://config-server:8888")
                .withEnv("POSTGRES_URL", "jdbc:postgresql://postgres-test:5432/users_db")
                .dependsOn(postgreSQLContainer, eurekaServerContainer, configServerContainer);
    }

}
