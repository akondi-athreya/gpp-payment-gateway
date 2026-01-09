import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Login.css';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    // For Deliverable 1, use simple email-based auth
    // In production, validate against backend
    if (email === 'test@example.com') {
      // Store auth state (in production, use proper token management)
      localStorage.setItem('isAuthenticated', 'true');
      localStorage.setItem('merchantEmail', email);
      navigate('/dashboard');
    } else {
      setError('Invalid credentials. Use test@example.com');
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h1>Payment Gateway</h1>
        <h2>Merchant Login</h2>
        
        {error && <div className="error-message">{error}</div>}
        
        <form data-test-id="login-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              data-test-id="email-input"
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Email"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              data-test-id="password-input"
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Password"
              required
            />
          </div>

          <button data-test-id="login-button" type="submit" className="btn-primary">
            Login
          </button>
        </form>

        <div className="login-hint">
          <p>Test Credentials:</p>
          <p><strong>Email:</strong> test@example.com</p>
          <p><strong>Password:</strong> Any password</p>
        </div>
      </div>
    </div>
  );
}

export default Login;
