// Import Statements
import React, { useState } from 'react';

function LoginForm() {

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    

    const handleSubmit = (e) => {
        e.preventDefault();
        console.log('Email:', email);
        console.log('Password:', password);
    };

return (
    <div className="login-container">
    <h2 className="login-title">Login</h2>
    <form onSubmit={handleSubmit} className="login-form">
        <div className="form-group">
        <label>Email:</label><br />
        <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            className="form-input"
        />
        </div>
        <div className="form-group">
        <label>Password:</label><br />
        <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            className="form-input"
        />
        </div>
        <button type="submit" className="login-button">Login</button>
    </form>
    </div>
);
}

export default LoginForm;