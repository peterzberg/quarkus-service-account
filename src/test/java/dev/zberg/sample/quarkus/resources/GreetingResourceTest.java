package dev.zberg.sample.quarkus.resources;

import io.quarkus.oidc.client.OidcClient;
import io.quarkus.oidc.client.Tokens;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class GreetingResourceTest {

    @Inject
    OidcClient oidcClient;
    private final KeycloakTestClient keycloakClient = new KeycloakTestClient();

    @Test
    void callHelloEndpoint_unauthenticated_stranger() {
        given()
                .when()
                .get("/hello")
                .then()
                .statusCode(200)
                .body(is("Hello stranger"));
    }

    @Test
    void callHelloEndpoint_alice_alice() {
        final String accessToken = keycloakClient.getAccessToken("alice");
        given()
                .when()
                .header("Authorization", "Bearer " + accessToken)
                .get("/hello")
                .then()
                .statusCode(200)
                .body(is("Hello alice"));
    }

    @Test
    void callHelloEndpoint_withToken_stranger() {
        final Tokens tokens = oidcClient.getTokens().await().atMost(Duration.ofSeconds(10L));
        final String accessToken = tokens.getAccessToken();
        given()
                .when()
                .header("Authorization", "Bearer " + accessToken)
                .get("/hello")
                .then()
                .statusCode(200)
                .body(is("Hello service-account-quarkus-app"));
    }

    @Test
    void callHelloServiceaccountEndpoint_unauthenticated_stranger() {
        given()
                .when()
                .get("/helloServiceAccount")
                .then()
                .statusCode(200)
                .body(is("Hello service-account"));
    }

    @Test
    void callcallHelloAsServiceAccountEndpoint_unauthenticated_serviceAccout() {
        given()
                .when()
                .get("/callHelloAsServiceAccount")
                .then()
                .statusCode(200)
                .body(is("Hello service-account-quarkus-app"));
    }

    @Test
    void callcallHelloAsServiceAccountEndpoint_authenticated_serviceAccout() {
        final String accessToken = keycloakClient.getAccessToken("alice");
        given()
                .when()
                .header("Authorization", "Bearer " + accessToken)
                .get("/callHelloAsServiceAccount")
                .then()
                .statusCode(200)
                .body(is("Hello service-account-quarkus-app"));
    }

}