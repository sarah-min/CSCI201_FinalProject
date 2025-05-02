// Form validation for register.html and login.html
document.addEventListener('DOMContentLoaded', function() {
    console.log("Form validation script loaded");
    
    // Get the register form elements if they exist
    const registerForm = document.getElementById('register');
    const fullNameInput = document.getElementById('fullname');
    const emailRegInput = document.getElementById('email-register');
    const pwRegInput = document.getElementById('pw-register');
    const registerButton = document.getElementById('register-button');
    
    // Get the name input div for styling
    const nameInputDiv = document.getElementById('name-input-div');
    // Get the email input div for styling
    const emailInputDiv = document.getElementById('email-input-div');
    // Get the password input div for styling
    const pwInputDiv = document.getElementById('pw-input-div');
    
    // Get error message elements for register form
    const fullnameEmptyError = document.getElementById('fullname-empty');
    const emailInvalidError = document.getElementById('email-invalid');
    const emailExistsError = document.getElementById('email-exists');
    const pwRequiredError = document.getElementById('pw-required');
    
    // Warning icons for register form
    const nameWarnIcon = document.getElementById('name-warn-reg');
    const emailWarnIcon = document.getElementById('email-warn-reg');
    const pwWarnIcon = document.getElementById('pw-warn-reg');
    
    // Get the login form elements if they exist
    const loginForm = document.getElementById('login-form');
    const emailLoginInput = document.getElementById('email-login');
    const pwLoginInput = document.getElementById('pw-login');
    const loginButton = document.getElementById('login-button');
    
    // Get the login email input div for styling
    const loginEmailInputDiv = document.getElementById('login-email-input-div');
    // Get the login password input div for styling
    const loginPwInputDiv = document.getElementById('login-pw-input-div');
    
    // Get error message elements for login form
    const emailInvalidLoginError = document.getElementById('email-invalid-login');
    const pwRequiredLoginError = document.getElementById('pw-required-login');
    const authError = document.getElementById('auth-error');
    
    // Warning icons for login form
    const emailWarnLoginIcon = document.getElementById('email-warn-login');
    const pwWarnLoginIcon = document.getElementById('pw-warn-login');
    
    // Critical: Forcefully hide all error messages on initial page load
    hideAllErrorsImmediately();
    
    // Initialize the register form validation if it exists on the page
    if (registerForm) {
        console.log("Register form found, initializing validation");
        
        // Make sure the button starts disabled
        if (registerButton) {
            registerButton.disabled = true;
        }
        
        // Add input event listeners for register form fields
        if (fullNameInput) {
            fullNameInput.addEventListener('input', function() {
                validateNameField();
                validateRegisterForm();
            });
        }
        
        if (emailRegInput) {
            emailRegInput.addEventListener('input', function() {
                validateEmailField();
                validateRegisterForm();
            });
        }
        
        if (pwRegInput) {
            pwRegInput.addEventListener('input', function() {
                validatePasswordField();
                validateRegisterForm();
            });
        }
        
        // Add submit event listener for register form
        registerForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            if (validateRegisterForm()) {
                // Prepare data to send to server
                const userData = {
                    username: fullNameInput.value.trim(),
                    email: emailRegInput.value.trim(),
                    password: pwRegInput.value.trim()
                };
                
                console.log("Submitting registration data");
                
                // Send data to register servlet
                fetch('register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(userData)
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        // Registration successful, redirect to login page
                        window.location.href = 'login.html';
                    } else {
                        // Show error message
                        if (data.message && data.message.includes('Email already registered')) {
                            // Show email exists error
                            if (emailExistsError) {
                                emailExistsError.classList.remove('hidden');
                            }
                            if (emailWarnIcon) {
                                emailWarnIcon.classList.remove('hidden');
                            }
                            if (emailInputDiv) {
                                emailInputDiv.classList.add('input-div-error');
                            }
                        } else {
                            // Show general error
                            alert('Registration failed: ' + (data.message || 'Unknown error'));
                        }
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('An error occurred during registration. Please try again.');
                });
            }
        });
    }
    
    // Initialize the login form validation if it exists on the page
    if (loginForm) {
        console.log("Login form found, initializing validation");
        
        // Make sure the button starts disabled
        if (loginButton) {
            loginButton.disabled = true;
        }
        
        // Add input event listeners for login form fields
        if (emailLoginInput) {
            emailLoginInput.addEventListener('input', function() {
                validateLoginEmailField();
                validateLoginForm();
            });
        }
        
        if (pwLoginInput) {
            pwLoginInput.addEventListener('input', function() {
                validateLoginPasswordField();
                validateLoginForm();
            });
        }
        
        // Add submit event listener for login form
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            if (validateLoginForm()) {
                // Prepare data to send to server
                const userData = {
                    email: emailLoginInput.value.trim(),
                    password: pwLoginInput.value.trim()
                };
                
                console.log("Submitting login data");
                
                // Send data to login servlet
                fetch('login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(userData)
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        // Login successful, redirect to main page
                        window.location.href = 'main.html';
                    } else {
                        // Show auth error message
                        if (authError) {
                            authError.classList.remove('hidden');
                        }
                        if (emailWarnLoginIcon) {
                            emailWarnLoginIcon.classList.remove('hidden');
                        }
                        if (pwWarnLoginIcon) {
                            pwWarnLoginIcon.classList.remove('hidden');
                        }
                        if (loginEmailInputDiv) {
                            loginEmailInputDiv.classList.add('input-div-error');
                        }
                        if (loginPwInputDiv) {
                            loginPwInputDiv.classList.add('input-div-error');
                        }
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('An error occurred during login. Please try again.');
                });
            }
        });
    }
    
    // Individual field validation functions for register form
    function validateNameField() {
        if (!fullNameInput) return false;
        
        const isValid = fullNameInput.value.trim() !== '';
        
        // Update UI based on validation
        if (isValid) {
            if (fullnameEmptyError) fullnameEmptyError.classList.add('hidden');
            if (nameWarnIcon) nameWarnIcon.classList.add('hidden');
            if (nameInputDiv) nameInputDiv.classList.remove('input-div-error');
        } else {
            if (fullnameEmptyError) fullnameEmptyError.classList.remove('hidden');
            if (nameWarnIcon) nameWarnIcon.classList.remove('hidden');
            if (nameInputDiv) nameInputDiv.classList.add('input-div-error');
        }
        
        return isValid;
    }
    
    function validateEmailField() {
        if (!emailRegInput) return false;
        
        // Hide any existing errors first
        if (emailInvalidError) emailInvalidError.classList.add('hidden');
        if (emailExistsError) emailExistsError.classList.add('hidden');
        if (emailWarnIcon) emailWarnIcon.classList.add('hidden');
        if (emailInputDiv) emailInputDiv.classList.remove('input-div-error');
        
        const email = emailRegInput.value.trim();
        const isValid = email !== '' && isValidEmail(email);
        
        // Update UI based on validation
        if (!isValid) {
            if (emailInvalidError) emailInvalidError.classList.remove('hidden');
            if (emailWarnIcon) emailWarnIcon.classList.remove('hidden');
            if (emailInputDiv) emailInputDiv.classList.add('input-div-error');
        }
        
        return isValid;
    }
    
    function validatePasswordField() {
        if (!pwRegInput) return false;
        
        const isValid = pwRegInput.value.trim() !== '';
        
        // Update UI based on validation
        if (isValid) {
            if (pwRequiredError) pwRequiredError.classList.add('hidden');
            if (pwWarnIcon) pwWarnIcon.classList.add('hidden');
            if (pwInputDiv) pwInputDiv.classList.remove('input-div-error');
        } else {
            if (pwRequiredError) pwRequiredError.classList.remove('hidden');
            if (pwWarnIcon) pwWarnIcon.classList.remove('hidden');
            if (pwInputDiv) pwInputDiv.classList.add('input-div-error');
        }
        
        return isValid;
    }
    
    // Individual field validation functions for login form
    function validateLoginEmailField() {
        if (!emailLoginInput) return false;
        
        const email = emailLoginInput.value.trim();
        const isValid = email !== '' && isValidEmail(email);
        
        // Update UI based on validation
        if (isValid) {
            if (emailInvalidLoginError) emailInvalidLoginError.classList.add('hidden');
            if (emailWarnLoginIcon) emailWarnLoginIcon.classList.add('hidden');
            if (loginEmailInputDiv) loginEmailInputDiv.classList.remove('input-div-error');
            // Also hide auth error when user starts typing again
            if (authError) authError.classList.add('hidden');
        } else {
            if (emailInvalidLoginError) emailInvalidLoginError.classList.remove('hidden');
            if (emailWarnLoginIcon) emailWarnLoginIcon.classList.remove('hidden');
            if (loginEmailInputDiv) loginEmailInputDiv.classList.add('input-div-error');
        }
        
        return isValid;
    }
    
    function validateLoginPasswordField() {
        if (!pwLoginInput) return false;
        
        const isValid = pwLoginInput.value.trim() !== '';
        
        // Update UI based on validation
        if (isValid) {
            if (pwRequiredLoginError) pwRequiredLoginError.classList.add('hidden');
            if (pwWarnLoginIcon) pwWarnLoginIcon.classList.add('hidden');
            if (loginPwInputDiv) loginPwInputDiv.classList.remove('input-div-error');
            // Also hide auth error when user starts typing again
            if (authError) authError.classList.add('hidden');
        } else {
            if (pwRequiredLoginError) pwRequiredLoginError.classList.remove('hidden');
            if (pwWarnLoginIcon) pwWarnLoginIcon.classList.remove('hidden');
            if (loginPwInputDiv) loginPwInputDiv.classList.add('input-div-error');
        }
        
        return isValid;
    }
    
    // Function to validate register form
    function validateRegisterForm() {
        const nameValid = validateNameField();
        const emailValid = validateEmailField();
        const passwordValid = validatePasswordField();
        
        const isFormValid = nameValid && emailValid && passwordValid;
        
        // Enable or disable the register button
        if (registerButton) {
            registerButton.disabled = !isFormValid;
        }
        
        return isFormValid;
    }
    
    // Function to validate login form
    function validateLoginForm() {
        const emailValid = validateLoginEmailField();
        const passwordValid = validateLoginPasswordField();
        
        const isFormValid = emailValid && passwordValid;
        
        // Enable or disable the login button
        if (loginButton) {
            loginButton.disabled = !isFormValid;
        }
        
        return isFormValid;
    }
    
    // Helper function to validate email format
    function isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }
    
    // Helper function to hide all error messages immediately
    function hideAllErrorsImmediately() {
        // Get all error messages and hide them
        const errorMessages = document.querySelectorAll('.form-error');
        errorMessages.forEach(error => {
            error.classList.add('hidden');
        });
        
        // Get all warning icons and hide them
        const warningIcons = document.querySelectorAll('.icon');
        warningIcons.forEach(icon => {
            icon.classList.add('hidden');
        });
        
        // Remove error styling from input divs
        const inputDivs = document.querySelectorAll('.input-div');
        inputDivs.forEach(div => {
            div.classList.remove('input-div-error');
        });
        
        // Make sure all form buttons are visible
        if (registerButton) registerButton.classList.remove('hidden');
        if (loginButton) loginButton.classList.remove('hidden');
        
        // Make sure the register and login content is visible
        const registerContent = document.getElementById('register-content');
        const loginContent = document.getElementById('login-content');
        
        if (registerContent) registerContent.classList.remove('hidden');
        if (loginContent) loginContent.classList.remove('hidden');
    }
});