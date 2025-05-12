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
	if (localStorage.getItem("loggedIn") === "true") {
		document.getElementById("not-logged-in").style.display = "none";
		document.getElementById("login-btn").style.display = "none";
		document.getElementById("logged-in-as").style.display = "block";
		document.getElementById("logged-in").style.display = "block";
		document.getElementById("logged-in").textContent = localStorage.getItem("user");
		document.getElementById("logged-in").style.fontWeight = "bold";
		document.getElementById("logout-btn").style.display = "block";	
		document.getElementById("login-status").style.backgroundColor = "#cfdef3";
	} else if (localStorage.getItem("loggedIn") === "false" || localStorage.getItem("loggedIn") === null ) {
		document.getElementById("not-logged-in").style.display = "block";
		document.getElementById("login-btn").style.display = "block";
		document.getElementById("logged-in-as").style.display = "none";
		document.getElementById("logged-in").style.display = "none";
		document.getElementById("logout-btn").style.display = "none";
		document.getElementById("history-link").style.display = "none";
		document.getElementById("separator").style.display = "none";
	} 
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
	// clean input
	const regex = /```json\s*|\s*```/g;
	let d = data.recommendations
	let trimmed = d.replace(regex, '');
	let jsonArray = JSON.parse(trimmed);
    
    console.log(jsonArray);
	
	// create container to put results in
  	const container = document.getElementById("songResults");
	document.getElementById("search-form").style.display = "none";
	container.style.display = "block";
	
	let header = document.createElement("p");
	header.textContent = `Movie Recommendations based on ${data.movie}`;
	header.className = "resultTitle";
	container.appendChild(header);
	
	// print results in cards
	jsonArray.forEach(rec => {
		let card = document.createElement("div");
		card.className = "rec-card";
		
		let songTitle = document.createElement("p");
		songTitle.textContent = rec.title;
		songTitle.className = "rec-title";
		
		let songArtist = document.createElement("p");
		songArtist.textContent = rec.artist;
		songArtist.className = "rec-artist";
	  
		card.appendChild(songTitle);
		card.appendChild(songArtist);
		container.appendChild(card);
	});
	
	let back = document.createElement("a");
	back.textContent = "< Back to Input";
	back.setAttribute('href', 'movie_recommender.html');
	container.appendChild(back);
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