import { useEffect, useState } from 'react';

function App() {
  const [status, setStatus] = useState('Loading...');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setStatus('App mounted successfully!');
    console.log('App component mounted');
    
    // Test localStorage
    try {
      localStorage.setItem('test', 'works');
      const test = localStorage.getItem('test');
      console.log('localStorage test:', test);
      setStatus(prev => prev + ' | localStorage OK');
    } catch (e: any) {
      setError('localStorage failed: ' + e.message);
    }
    
    // Test API
    fetch('http://localhost:8080/api/auth/me')
      .then(res => res.json())
      .then(data => {
        console.log('API test:', data);
        setStatus(prev => prev + ' | API OK');
      })
      .catch(err => {
        console.error('API test failed:', err);
        setError('API failed: ' + err.message);
      });
  }, []);

  return (
    <div style={{
      padding: '40px',
      fontFamily: 'monospace',
      background: '#1a1a1a',
      color: '#00ff00',
      minHeight: '100vh'
    }}>
      <h1>LobAI App Test</h1>
      <div style={{ marginTop: '20px', padding: '20px', background: '#2a2a2a', borderRadius: '8px' }}>
        <p>Status: {status}</p>
        {error && <p style={{ color: '#ff0000' }}>Error: {error}</p>}
      </div>
      <div style={{ marginTop: '20px' }}>
        <p>✓ React is working</p>
        <p>✓ TypeScript is working</p>
        <p>✓ Vite HMR is working</p>
      </div>
    </div>
  );
}

export default App;
