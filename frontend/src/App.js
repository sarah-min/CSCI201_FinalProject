import logo from './logo.svg';
import './App.css';
import React from 'react';
import LoginForm from './features/LoginForm';

function App() {
  return (

    <div className="App">
      <header className="App-header">Spotify Music Suggestions</header>
      <div className="App">
      <LoginForm />
      </div>
    </div>

  );
}

export default App;
