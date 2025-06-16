// App.js - Point d'entrée principal
import './App.css';

import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { AuthProvider } from './context/AuthContext';
import PrivateRoute from './components/auth/PrivateRoute';
// Pages
import Login from './components/auth/Login';
import StudentDashboard from './components/student/Dashboard';
import AdminDashboard from './components/admin/Dashboard';
import ValidationRequests from './components/admin/ValidationRequests';
import RequestDetails from './components/admin/RequestDetails';
import ValidationStatus from './components/student/ValidationStatus';
import DocumentDownload from './components/student/DocumentDownload';
import MainLayout from './components/layout/MainLayout';
// Thème personnalisé
const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
    background: {
      default: '#f5f5f5',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    h5: {
      fontWeight: 600,
    },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <Router>
          <Routes>
            <Route path="/login" element={<Login />} />
            
            {/* Routes protégées pour les étudiants */}
            <Route 
              path="/student/*" 
              element={
                <PrivateRoute role="student">
                  <MainLayout>
                    <Routes>
                      <Route path="/" element={<StudentDashboard />} />
                      <Route path="/status" element={<ValidationStatus />} />
                      <Route path="/documents" element={<DocumentDownload />} />
                    </Routes>
                  </MainLayout>
                </PrivateRoute>
              } 
            />
            
            {/* Routes protégées pour l'administration */}
            <Route 
              path="/admin/*" 
              element={
                <PrivateRoute role="admin">
                  <MainLayout>
                    <Routes>
                      <Route path="/" element={<AdminDashboard />} />
                      <Route path="/requests" element={<ValidationRequests />} />
                      <Route path="/requests/:id" element={<RequestDetails />} />
                    </Routes>
                  </MainLayout>
                </PrivateRoute>
              } 
            />
            
            {/* Redirection par défaut */}
            <Route path="/" element={<Navigate to="/login" replace />} />
          </Routes>
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;