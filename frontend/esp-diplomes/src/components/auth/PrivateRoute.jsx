// Components/auth/PrivateRoute.jsx - Route protégée
import React, { useContext } from 'react';
import { Navigate } from 'react-router-dom';
import { AuthContext } from '../../context/AuthContext';
import { Box, CircularProgress } from '@mui/material';

const PrivateRoute = ({ children, role }) => {
  const { isAuthenticated, loading, user } = useContext(AuthContext);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // Vérifier si l'utilisateur a le bon rôle
  if (role && user.role !== role) {
    // Rediriger vers la bonne section
    return <Navigate to={user.role === 'student' ? '/student' : '/admin'} replace />;
  }

  return children;
};

export default PrivateRoute;