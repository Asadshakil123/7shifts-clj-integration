(config
 (text-field
   :name "clientId"
   :label "APP ID"
   :required    true
   :placeholder "Enter your app ID")

   (password-field
   :name "ClientSecret"
   :label "Client Secret"
   :required    true
   :placeholder "Enter your App Secret"
   )

   (oauth2/authorization-code-with-client-credentials
    :api-auth-fields "refreshToken accessToken"

   (authorization-code
    (source
      (http/get 
      :base-url "https://oauth.qonto.com/oauth2"
      :url "/auth"
      (query-params
      "response_type"  "code"
      "client_id" "{clientId}"
      "scope"  "offline_access organization.read payment.write"
      "redirect_uri" "$FIVETRAN-APP-URL/integrations/qonto/oauth2/return"
      "state" "fivetran"
          )
         )
        )
     )

     (access-token
      (source
       (http/post
       :base-url "https://oauth.qonto.com/oauth2"
       :url "/token"
       (body-param-format "application/x-www-form-urlencoded")
        (body-params
           "code" "$AUTHORISATION-CODE"
           "client_id" "{clientId}"
           "client_secret" "{clientSecret}"
           "grant_type" "authorization_code"
         )
       )
      )
       (fields 
           access_token :<= "access_token"
           refresh_token :<= "refresh_token"
           token_type :<= "token_type"
           scope :<= "scope"
           expires_at :<= "expires_at"
          )
        )
     )

(refresh-token
   (source
    (http/post
     :base-url "https://oauth.qonto.com/oauth2"
     :url "/token"
     (body-params
      "refresh_token" "$REFRESH-TOKEN"
      "client_id" "{clientId}"
      "client_secret" "{clientSecret}"
      "grant_type" "refresh_token"
      )
     )
    )
   (fields
    refresh_token :<=  "refresh_token"
    access_token  :<= "access_token"
    )
   )
  
 
 (default-source
  (http/get :base-url "https://thirdparty.qonto.com/v2"
    (header-params "Accept" "application/json"
     ))
 (auth/oauth2)
 (paging/no-pagination)
 (error-handler
   (when :status 401 :action refresh)
   (when :status 429 :action rate-limit)
   (when :status 403 :action (fail-sync (failure/access-denied)))
   (when :status 404 :action (fail-sync (failure/not-found)))
   (when :status 400 :action (fail-sync (failure/invalid-request))))))


(temp-entity labels
(api-docs-url "https://docs.qonto.com/api-reference/business-api/accounts-organizations/labels/list-labels")
(source
   (http/get :url  "/labels")
   (setup-test
     (upon-receiving :code 200 :action (pass)))
   (extract-path "labels"))
 (fields
   id   :id))
   




