document.addEventListener('DOMContentLoaded', function() {
	fetch(`GetSearchHistory?user=${encodeURIComponent(localStorage.getItem("user"))}`, {
	        method: 'GET',
	    })
	    .then(response => response.json())
	    .then(data => {
	        if (data.success) {
	            // Display history in UI
				console.log(localStorage.getItem("user"));
	            console.log(data.history);
				populateHistoryTable(data.history);
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

});

function populateHistoryTable(historyData) {
  const tableBody = document.querySelector('#history-table tbody');
  tableBody.innerHTML = ''; // Clear any existing rows

  historyData.forEach(entry => {
    const row = document.createElement('tr');

    //  title
    const titleCell = document.createElement('td');
    titleCell.textContent = entry.title;
    row.appendChild(titleCell);

    // recommendations
    const recCell = document.createElement('td');
    try {
      let raw = entry.recommendations.trim();
      raw = raw.replace(/^```json\n?|```$/g, '');

      const recs = JSON.parse(raw);

      let formatted = '';
      if (Array.isArray(recs)) {
        // Direct array of recommendations
        formatted = recs.map(r => `${r.title}${r.artist ? ' by ' + r.artist : ''}`).join(", ");
      } else if (Array.isArray(recs.songs)) {
        // Object with "songs" array
        formatted = recs.songs.map(song => `${song.title} by ${song.artist}`).join(', ');
      } 

      recCell.textContent = formatted;
    } catch (err) {
      recCell.textContent = 'Invalid JSON';
      console.error('Error parsing recommendations:', err, entry.recommendations);
    }
    row.appendChild(recCell);

    // Request type
    const typeCell = document.createElement('td');
    typeCell.textContent = entry.rec_type;
    row.appendChild(typeCell);

    tableBody.appendChild(row);
  });
}

function logout() {
	localStorage.clear();
}