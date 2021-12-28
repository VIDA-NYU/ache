package achecrawler.rest;

import io.javalin.core.security.BasicAuthCredentials;
import io.javalin.http.Context;
import io.javalin.http.ExceptionHandler;
import io.javalin.http.Handler;


public class BasicAuthenticationFilter implements Handler {

    private static final int UNAUTHORIZED_STATUS_CODE = 401;

    private final String username;
    private final String password;

    public BasicAuthenticationFilter(String user, String password) {
        this.username = user;
        this.password = password;
    }

    @Override
    public void handle(Context ctx) {
        if (!ctx.basicAuthCredentialsExist()) {
            throw new UnauthorizedException("No user credentials provided.");
        }
        BasicAuthCredentials credentials = ctx.basicAuthCredentials();
        if (!isAuthorized(credentials)) {
            throw new UnauthorizedException("Invalid user credentials provided.");
        }
    }

    private boolean isAuthorized(BasicAuthCredentials credentials) {
        if (username.equals(credentials.getUsername()) && password.equals(credentials.getPassword())) {
            return true;
        }
        return false;
    }

    public static ExceptionHandler<? super UnauthorizedException> exceptionHandler = (e, ctx) -> {
        ctx.status(UNAUTHORIZED_STATUS_CODE);
        ctx.header("WWW-Authenticate", "Basic");
        ctx.result(e.getMessage());
    };

}
