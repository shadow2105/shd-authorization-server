package com.example.shdauthorizationserver.controller;

import com.example.shdauthorizationserver.controller.exception.BadRequestException;
import com.example.shdauthorizationserver.model.RegisteredClientStore;
import com.example.shdauthorizationserver.service.UserService;
import com.example.shdauthorizationserver.service.exception.UserAlreadyExistsException;
import com.example.shdauthorizationserver.service.exception.UserRegistrationException;
import com.example.shdauthorizationserver.dto.CustomerRegistrationRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Controller
public class WebController {

    private final UserService userService;
    private final RegisteredClientRepository registeredClientRepository;


    /*
     * controllers (Spring beans) are singleton-scoped by default; a single instance shared across multiple requests
     * the instance variable is shared among all requests handled by that controller.
     * If one user request sets a value for the instance variable; can cause data leakage between different users
     * avoid storing request-specific data in instance variables
     */
    //private String requestClientId;

    public WebController(UserService userService, RegisteredClientRepository registeredClientRepository) {
        this.userService = userService;
        this.registeredClientRepository = registeredClientRepository;
    }

    @GetMapping("/")
    public String showUserAppsDashboard() {
            return "apps-dashboard";
    }

    // 1. Show User Login form for a specific Registered Client Application
    // Login form submission handled at /authn [POST] by Spring Security: See SecurityConfiguration
    @GetMapping("/signin")
    public String showLoginForm(HttpServletRequest request, Model model,
                                               @RequestParam(name = "client_id", required = false) String clientId,
                                               @RequestParam(name = "error", required = false) String error,
                                               @RequestParam(name = "success", required = false) String success)
            throws BadRequestException {

        // Retrieve clientId from the session
        String localRequestClientId = null;
        HttpSession session = request.getSession();
        //System.out.println("\n>>>>>>>> Is session new? = " + session.isNew());
        localRequestClientId = (String) session.getAttribute("clientId");

        if(error != null) {
            AuthenticationException authException = (AuthenticationException)
                    session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
            model.addAttribute("appClient", RegisteredClientStore.valueOf("CLIENT_" + localRequestClientId));
            model.addAttribute("error", authException.getMessage());

            session.setAttribute("SPRING_SECURITY_LAST_EXCEPTION", null);
            return "signin";
        }

        if(success != null) {
            model.addAttribute("appClient", RegisteredClientStore.valueOf("CLIENT_" + localRequestClientId));
            model.addAttribute("success", "success");

            return "signin";
        }

        // If Login form shown as a response to OAuth Authorization Request
        // localRequestClientId is null and set in this block
        if (clientId == null) {
            clientId = getClientId(session);     // checks if session is null

            if (clientId != null) {
                //requestClientId = clientId;
                session.setAttribute("clientId", clientId);
                model.addAttribute("appClient", RegisteredClientStore.valueOf("CLIENT_" + clientId));

                return "signin";
            }
        }
        // If Login form shown after returning back from Registration page which is accessed directly, or
        // What if a user explicitly requests /signin?client_id= from the address bar ?
        // No saved OAuth Authorization Request to get the client id in the above cases
        // In such cases redirect the user to their apps dashboard (like Okta?) after authentication
        //  - but current loginProcessingUrl /authn is being handled by Spring Security
        //  - this redirects the user to localhost:8090/ (so handle / to show the apps dashboard page?)
        else {
            RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);

            if (registeredClient != null) {
                //requestClientId = clientId;
                session.setAttribute("clientId", clientId);
                model.addAttribute("appClient", RegisteredClientStore.valueOf("CLIENT_" +
                        registeredClient.getClientId()));

                return "signin";
            }
        }

        throw new BadRequestException("Unable to display Login form. No registered client id found!");
    }


    // 2. Show basic/initial Customer [User with ROLE_CUSTOMER] Registration Form for a specific Registered Client Application
    @GetMapping({"/signin/register/{clientId}"})
    public String showCustomerRegistrationForm(Model model,
                                               @PathVariable("clientId") String clientId)
            throws BadRequestException {

        model.addAttribute("customerRegistrationRequestDto", new CustomerRegistrationRequestDto());
        RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);

        if (registeredClient != null) {
            model.addAttribute("appClient",
                    RegisteredClientStore.valueOf("CLIENT_" + registeredClient.getClientId()));

            return "basic_customer_registration";
        }

        throw new BadRequestException("Unable to display Customer Registration form. No registered client id found!");
    }

    @PostMapping({"/signin/register"})
    public String registerCustomer(Model model, HttpSession session, @Valid @ModelAttribute("customerRegistrationRequestDto")
    CustomerRegistrationRequestDto customerRegistrationRequestDto, BindingResult bindingResult,
                                   @RequestParam("clientId") String clientId) {

        model.addAttribute("appClient",
                RegisteredClientStore.valueOf("CLIENT_" + clientId));

        if (bindingResult.hasErrors()) {
            return "basic_customer_registration";
        }

        try {
            userService.addCustomer(customerRegistrationRequestDto, clientId);
            //requestClientId = clientId;
            //System.out.println("\n>>>>>>>> Is session new? = " + session.isNew());
            session.setAttribute("clientId", clientId);
            return "redirect:/signin?success";
        } catch (UserAlreadyExistsException | UserRegistrationException ex) {
            model.addAttribute("error", ex.getMessage());
        }

        return "basic_customer_registration";
    }

    @PostMapping("/logout/back-channel")
    public void backChannelLogout(@RequestParam(name = "client_id", required = true) String clientId)
            throws BadRequestException {

        RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);

        if(registeredClient == null) {
            throw new BadRequestException("Unable to perform back-channel logout. No registered client id found!");
        }

        String clientRedirectUri = registeredClient.getRedirectUris().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No Redirect URIs found for the client " + clientId));

        String clientUri = "";
        try {
            URI uri = new URI(clientRedirectUri);
            String hostWithProtocol = uri.getScheme() + "://" + uri.getHost();
            int port = uri.getPort();
            if (port != -1) {
                hostWithProtocol += ":" + port;
            }

            clientUri = hostWithProtocol;

        } catch (Exception e) {
            // log
            System.out.println(e.getMessage());
        }

        String clientAName = RegisteredClientStore.valueOf("CLIENT_" + clientId).aname;

        RestTemplate restTemplate = new RestTemplate();
        // All Client Applications are and must be registered as an OAuth2 Client with their aname in lowercase;
        // see {@link com.example.shdauthorizationserver.model.RegisteredClientStore}
        // Client aname is the same as Client Name in RegisteredClientRepository
        String url = clientUri + "/logout/connect/back-channel/" + clientAName.toLowerCase();

        // need to create a Logout Token
        restTemplate.postForEntity(url, null, String.class);
    }




    // Returns client_id from the saved OAuth Authorization Request or null
    private String getClientId(HttpSession session) {
        if (session != null) {
            DefaultSavedRequest savedRequest = (DefaultSavedRequest) session.getAttribute("SPRING_SECURITY_SAVED_REQUEST");

            if (savedRequest != null && savedRequest.getParameterMap().containsKey("client_id")) {
                String[] values = savedRequest.getParameterMap().get("client_id");

                if (values.length > 0) {
                    String clientId = values[0];

                    RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);

                    if (registeredClient != null && StringUtils.hasLength(registeredClient.getClientId())) {
                        return registeredClient.getClientId();
                    }
                }
            }
        }

        return null;
    }

}
