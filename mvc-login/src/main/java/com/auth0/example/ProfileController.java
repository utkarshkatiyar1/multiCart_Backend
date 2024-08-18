package com.auth0.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import common.Constants;
import model.ApplicationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller for requests to the {@code /profile} resource. Populates the model with the claims from the
 * {@linkplain OidcUser} for use by the view.
 */

@CrossOrigin(origins = "*")
public class ProfileController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final static ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());


    public ApplicationResponse profile(Model model, @AuthenticationPrincipal OidcUser oidcUser) {
        //model.addAttribute("profile", oidcUser.getClaims());
        //model.addAttribute("profileJson", claimsToJson(oidcUser.getClaims()));
        // Access the user ID (sub field)
        ApplicationResponse applicationResponse = new ApplicationResponse<>();
        try {
            String profileJson = oidcUser.getSubject();
            OidcUserInfo userInfo = oidcUser.getUserInfo();
            // Access the user ID (sub field)
            String userId = extractUserId(profileJson);
            // Display the user ID
            //System.out.println("User ID: " + userId);

            //model.addAttribute("userId",userId);

            List list = new ArrayList();
            list.add(userInfo);
            applicationResponse.setData(list);
            applicationResponse.setMessage("user profile");
            applicationResponse.setStatus(Constants.OK);

        }catch (Exception e){
            applicationResponse.setStatus(Constants.NOT_FOUND);
            applicationResponse.setMessage("user profile not found");
        }
        return applicationResponse;

    }

    private static String extractUserId(String sub) {
        // Extract user ID from sub field
        return sub.split("\\|")[1];
    }
    private String claimsToJson(Map<String, Object> claims) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(claims);
        } catch (JsonProcessingException jpe) {
            log.error("Error parsing claims to JSON", jpe);
        }
        return "Error parsing claims to JSON.";
    }
}
