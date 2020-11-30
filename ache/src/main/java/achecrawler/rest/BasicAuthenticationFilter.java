package achecrawler.rest;


import java.util.Base64;

import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Service;

public class BasicAuthenticationFilter implements Filter {

    private static final int UNAUTHORIZED_STATUS_CODE = 401;

    private final Service server;
    private final String username;
    private final String password;

    public BasicAuthenticationFilter(Service server, String user, String password) {
        this.server = server;
        this.username = user;
        this.password = password;
    }

    @Override
    public void handle(final Request request, final Response response) {
        if (!isAuthenticated(request.headers("Authorization"))) {
            response.header("WWW-Authenticate", "Basic");
            server.halt(UNAUTHORIZED_STATUS_CODE);
        }
    }

    private boolean isAuthenticated(String authHeader) {
        String credentials = decodeCredentials(authHeader);
        if (credentials == null || credentials.isEmpty()) {
            return false;
        }
        String[] values = credentials.split(":");
        if (values == null || values.length != 2) {
            return false;
        }
        return username.equals(values[0]) && password.equals(values[1]);
    }

    private String decodeCredentials(final String encodedAuthHeader) {
        if (encodedAuthHeader == null || encodedAuthHeader.isEmpty()) {
            return null;
        }
        String[] values = encodedAuthHeader.split(" ");
        if (values.length == 2 && "Basic".equals(values[0])) {
            try {
                byte[] decoded = Base64.getDecoder().decode(values[1]);
                return new String(decoded);
            } catch (IllegalArgumentException e) {
                // Invalid base64 string
            }
        }
        return null;
    }

}
