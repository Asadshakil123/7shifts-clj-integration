(config
 (text-field
  :name        "user_id"
  :label       "User ID"
  :placeholder "Enter user Id"
  :required    true
 )
  
 (password-field
  :name        "api_key"
  :label       "API Key"
  :placeholder "Enter your 7 Shift API Key"
  :required    true
  :description  "This text is shown below the password field"))


(default-source
    (http/get :base-url "https://developers.7shifts.com"
                          (header-params 
                          "Authorization: Bearer {ISSUED_TOKEN}"
                           "Content-Type: application/x-www-form-urlencoded"
                          "Accept" "application/json"
                          ))
                (paging/cursor-based
                   :limit-param "limit"
                   :cursor-param "cursor")
                (auth/http-basic)
                (error-handler
                        (when :status 404 :message "not found" :action fail)
                        (when :status 404 :action skip)
                        (when :status 429 :action rate-limit)
                        (when :status 401 :action refresh)
                        (when :status 503 :action retry))
)


(temp-entity users
  (api-docs-url "https://api.7shifts.com/v2/whoami")
  (source
    (http/get :url "/v2/whoami")
    (setup-test
      (upon-receiving
        :code 200 :action (pass)
        :code 401 :action (fail "Invalid Environment API key - please check your API credentials")
        :code 403 :action (fail "Access forbidden - verify environment API key has required permissions"))
      :instructions-docs-path "https://userflow.com/docs/api#users-api-authentication"
      :running-default-message "Testing Environment API key credentials..."
      :failure-default-message "Failed to authenticate with provided environment API key. Please verify your environment API key is correct."))
  (fields
    id :id))