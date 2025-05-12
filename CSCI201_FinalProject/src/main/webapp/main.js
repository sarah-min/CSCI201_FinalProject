// Main functionality for the media recommender app
document.addEventListener('DOMContentLoaded', function() {
    console.log("Main script loaded");
	console.log("login status: " + localStorage.getItem("loggedIn"));
    
    // Check if user is logged in
    checkLoginStatus();
    
    // Get form elements if on the main.html page
    const musicInput = document.getElementById('music-input');
    const generateButton = document.getElementById('generate-button');
    const historyButton = document.getElementById('history-button');
    const fileUpload = document.getElementById('file-upload');
    
    // Add event listeners if on the main.html page
    
    
    if (historyButton) {
        historyButton.addEventListener('click', viewSearchHistory);
    }

    if (fileUpload) {
        fileUpload.addEventListener('change', handleFileUpload);
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
function generateRecommendation() {
    // const linkInput = document.getElementById('link-input');
    const songInput = document.getElementById('song-input');
    
    if (songInput.value === null) {
        alert('Please enter a song');
        return;
    }
	
	console.log(songInput);
    
    // Send request to UploadSong servlet
    fetch('UploadSong', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: `songInput=${encodeURIComponent(songInput.value.trim())}&user=${encodeURIComponent(localStorage.getItem("user"))}`
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
	// clean input
	const regex = /```json\s*|\s*```/g;
	let d = data.recommendations
	let trimmed = d.replace(regex, '');
	let jsonArray = JSON.parse(trimmed);
    
    console.log(jsonArray);
	
	// create container to put results in
  	const container = document.getElementById("movieResults");
	document.getElementById("search-form").style.display = "none";
	container.style.display = "block";
	
	let header = document.createElement("p");
	header.textContent = `Movie Recommendations based on ${data.track} - ${data.artist}`;
	header.className = "resultTitle";
	container.appendChild(header);
	
	// print results in cards
	jsonArray.forEach(rec => {
		let card = document.createElement("div");
		card.className = "rec-card";
		
		let movieTitle = document.createElement("p");
		movieTitle.textContent = rec.title;
		movieTitle.className = "rec-title";
		
		let movieSummary = document.createElement("p");
		movieSummary.textContent = rec.summary;
		movieSummary.className = "rec-summary";
	  
		card.appendChild(movieTitle);
		card.appendChild(movieSummary);
		container.appendChild(card);
		
	});
	
	let back = document.createElement("a");
	back.textContent = "< Back to Input";
	back.setAttribute('href', 'main.html');
	container.appendChild(back);
}


function logout() {
	localStorage.clear();
}

