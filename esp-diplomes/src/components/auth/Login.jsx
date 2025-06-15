// Components/auth/Login.jsx - Page de connexion
import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../../context/AuthContext';
import {
  Container,
  Box,
  TextField,
  Button,
  Typography,
  Paper,
  Alert,
  CircularProgress
} from '@mui/material';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import SchoolIcon from '@mui/icons-material/School';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const user = await login(username, password);
      if (user.role === 'student') {
        navigate('/student');
      } else {
        navigate('/admin');
      }
    } catch (err) {
      setError('Identifiants invalides. Veuillez réessayer.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container component="main" maxWidth="xs">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Paper
          elevation={3}
          sx={{
            padding: 4,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            width: '100%',
          }}
        >
          <SchoolIcon color="primary" sx={{ fontSize: 60, mb: 2 }} />
          <Typography component="h1" variant="h5" gutterBottom>
            ESP - Validation des Diplômes
          </Typography>
          <Box component="form" onSubmit={handleSubmit} sx={{ mt: 3, width: '100%' }}>
            {error && (
              <Alert severity="error" sx={{ mb: 2 }}>
                {error}
              </Alert>
            )}
            <TextField
              margin="normal"
              required
              fullWidth
              id="username"
              label="Identifiant ESP"
              name="username"
              autoComplete="username"
              autoFocus
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={loading}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              name="password"
              label="Mot de passe"
              type="password"
              id="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={loading}
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
              disabled={loading}
              startIcon={loading ? <CircularProgress size={20} /> : <LockOutlinedIcon />}
            >
              {loading ? 'Connexion en cours...' : 'Se connecter'}
            </Button>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default Login;