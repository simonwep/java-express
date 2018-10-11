package examples;

import express.Express;
import express.http.request.Authorization;
import express.utils.Status;

public class AuthorizationExamples {
    public static void main(String[] args) {
        Express app = new Express();

        app.get("/", (req, res) -> {
            if (Authorization.validate(req, Authorization.validator("Basic", "123456789"))) {
                res.send("You are authorized!");
            } else {
                res.setStatus(Status._401);
                res.send();
            }
        });

        app.listen(() -> System.out.println("Listening!"));
    }
}
