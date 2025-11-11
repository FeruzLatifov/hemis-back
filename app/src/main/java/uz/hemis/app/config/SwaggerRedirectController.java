package uz.hemis.app.config;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Services")
@Hidden
@Controller
public class SwaggerRedirectController {

    @GetMapping({"/swagger", "/swagger/", "/swagger-ui", "/swagger-ui/"})
    public String redirectToUi() {
        return "redirect:/swagger-ui.html";
    }
}


