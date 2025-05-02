// Main functionality for the media recommender app
document.addEventListener('DOMContentLoaded', function() {
    console.log("Main script loaded");
    
    // Check if user is logged in
    checkLoginStatus();
    
    // Get form elements if on the main.html page
    const musicInput = document.getElementById('music-input');
    const generateButton = document.getElementById('generate-button');
    const historyButton = document.getElementById('history-button');
    
    // Add event listeners if on the main.html page
    if (generateButton) {
        generateButton.addEventListener('click', handleGenerateRecommendation);
    }
    
    if (historyButton) {
        historyButton.addEventListener('click', viewSearchHistory);
    }
    
    // Check if we're on register.html or login.html
    initializeFormVisibility();
});

/**
 * Initialize form visibility - fixes issue with forms being hidden initially
 */
function initializeFormVisibility() {
    // Show the register content if it exists
    const registerContent = document.getElementById('register-content');
    if (registerContent) {
        registerContent.classList.remove('hidden');
    }
    
    // Show the login content if it exists
    const loginContent = document.getElementById('login-content');
    if (loginContent) {
        loginContent.classList.remove('hidden');
    }
    
    // Hide all error messages initially
    const errorMessages = document.querySelectorAll('.form-error');
    errorMessages.forEach(message => {
        message.classList.add('hidden');
    });
    
    // Hide all warning icons initially
    const warningIcons = document.querySelectorAll('.icon');
    warningIcons.forEach(icon => {
        icon.classList.add('hidden');
    });
}

/**
 * Check login status and update UI accordingly
 */
function checkLoginStatus() {
    const userStatus = document.getElementById('user-status');
    
    if (!userStatus) return; // Not on a page with user status
    
    // Check if user is logged in by making a request to server
    fetch('checkLoginStatus', {
        method: 'GET',
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        if (data.loggedIn) {
            userStatus.textContent = `Logged in as: ${data.username}`;
        } else {
            userStatus.textContent = 'Not logged in';
        }
    })
    .catch(error => {
        console.error('Error checking login status:', error);
        userStatus.textContent = 'Not logged in';
    });
}

/**
 * Handle generate recommendation button click
 */
function handleGenerateRecommendation() {
    const musicInput = document.getElementById('music-input');
    
    if (!musicInput || !musicInput.value.trim()) {
        alert('Please enter a song title or playlist link');
        return;
    }
    
    // Send request to UploadSong servlet
    fetch('UploadSong', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: `songInput=${encodeURIComponent(musicInput.value.trim())}`
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            displayRecommendations(data);
        } else {
            alert(`Error: ${data.error}`);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('An error occurred while generating recommendations');
    });
}

/**
 * Display movie recommendations
 */
function displayRecommendations(data) {
    // You would implement a UI to display the recommendations here
    // For now, just show an alert with the data
    let message = `Recommendations based on ${data.artist} - ${data.track}\n\n`;
    message += `Tags: ${data.tags.join(', ')}\n\n`;
    message += `Movies: ${data.recommendations}`;
    
    alert(message);
    
    // In a real implementation, you'd display this in a nice UI
}

/**
 * View search history
 */
function viewSearchHistory() {
    // Redirect to history page or show history modal
    fetch('GetSearchHistory', {
        method: 'GET',
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Display history in UI
            alert('Your search history: \n' + JSON.stringify(data.history, null, 2));
        } else {
            if (data.error === 'Not logged in') {
                alert('Please log in to view your search history');
                window.location.href = 'login.html';
            } else {
                alert(`Error: ${data.error}`);
            }
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('An error occurred while retrieving search history');
    });
}