// Main functionality for the movie recommender app
document.addEventListener('DOMContentLoaded', function() {
    console.log("Movie recommender script loaded");
    
    // Check if user is logged in
    checkLoginStatus();
    
    // Get form elements
    const movieInput = document.getElementById('movie-input');
    const generateButton = document.getElementById('generate-button');
    const historyButton = document.getElementById('history-button');
    
    // Add event listeners
    if (generateButton) {
        generateButton.addEventListener('click', handleGenerateRecommendation);
    }
    
    if (historyButton) {
        historyButton.addEventListener('click', viewSearchHistory);
    }
});

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
    const movieInput = document.getElementById('movie-input');
    
    if (!movieInput || !movieInput.value.trim()) {
        alert('Please enter a movie title');
        return;
    }
    
    // Send request to UploadMovie servlet
    fetch('UploadMovie', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: `movieInput=${encodeURIComponent(movieInput.value.trim())}`
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
 * Display song recommendations
 */
function displayRecommendations(data) {
    // You would implement a UI to display the recommendations here
    // For now, just show an alert with the data
    let message = `Recommendations based on the movie: ${data.movie}\n\n`;
    message += `Genres: ${data.genres.join(', ')}\n\n`;
    message += `Songs: ${data.recommendations}`;
    
    alert(message);
    
    // In a real implementation, you'd display this in a nice UI
}

/**
 * View search history
 */
function viewSearchHistory() {
    // Redirect to history page or show history modal
    fetch('GetMovieSearchHistory', {
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
}/**
 * 
 */