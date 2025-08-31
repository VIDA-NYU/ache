package achecrawler.rest;


import io.javalin.http.Context;
import io.javalin.http.ExceptionHandler;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.security.BasicAuthCredentials;


public class BasicAuthenticationFilter implements Handler {

    private final String username;
    private final String password;

    public BasicAuthenticationFilter(String user, String password) {
        this.username = user;
        this.password = password;
    }

    @Override
    public void handle(Context ctx) {
        BasicAuthCredentials credentials = ctx.basicAuthCredentials();
        if (credentials == null) {
            ctx.header("WWW-Authenticate", "Basic");
            throw new UnauthorizedException("No user credentials provided.");
        }
        if (!isAuthorized(credentials)) {
            throw new UnauthorizedException("Invalid user credentials provided.");
        }
    }

    private boolean isAuthorized(BasicAuthCredentials credentials) {
        return username.equals(credentials.getUsername()) && password.equals(credentials.getPassword());
    }

    public static ExceptionHandler<? super UnauthorizedException> exceptionHandler = (e, ctx) -> {
        ctx.status(HttpStatus.UNAUTHORIZED.getCode());
        ctx.header("WWW-Authenticate", "Basic");
        ctx.result(e.getMessage());
    };

}
