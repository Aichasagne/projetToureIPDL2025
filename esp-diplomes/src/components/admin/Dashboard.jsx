import React, { useState, useEffect, useContext } from 'react';
import { AuthContext } from '../../context/AuthContext';
import { studentService } from '../../services/student.service';
import {
  Box,
  Typography,
  Paper,
  Grid,
  Card,
  CardContent,
  CardActions,
  Button,
  Chip,
  Alert,
  CircularProgress,
  LinearProgress,
  Divider
} from '@mui/material';
import AssignmentTurnedInIcon from '@mui/icons-material/AssignmentTurnedIn';
import PendingIcon from '@mui/icons-material/Pending';
import DownloadIcon from '@mui/icons-material/Download';
import ArticleIcon from '@mui/icons-material/Article';
import { useNavigate } from 'react-router-dom';

const getStatusColor = (status) => {
  switch (status) {
    case 'COMPLETED':
      return 'success';
    case 'PENDING':
      return 'warning';
    case 'REJECTED':
      return 'error';
    default:
      return 'default';
  }
};

const getStatusText = (status) => {
  switch (status) {
    case 'COMPLETED':
      return 'Validé';
    case 'PENDING':
      return 'En cours';
    case 'REJECTED':
      return 'Rejeté';
    default:
      return 'Inconnu';
  }
};

const Dashboard = () => {
  const { user } = useContext(AuthContext);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [data, setData] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const dashboardData = await studentService.getDashboardData();
        setData(dashboardData);
      } catch (err) {
        setError('Impossible de charger les données. Veuillez réessayer plus tard.');
        console.error('Erreur lors de la récupération des données du tableau de bord:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '70vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  // Données simulées pour le développement
  const simulatedData = {
    student: {
      name: user?.name || 'Étudiant',
      matricule: user?.id || 'ESP12345',
      department: 'Informatique',
      program: 'Licence en Informatique',
      graduationYear: '2023',
    },
    validationStatus: {
      status: 'PENDING',
      progress: 60,
      completedSteps: 3,
      totalSteps: 5,
      latestUpdate: '2023-11-15T10:30:00',
    },
    pendingSignatures: [
      { id: 1, title: 'Directeur des études', status: 'PENDING' },
      { id: 2, title: 'Service comptabilité', status: 'PENDING' },
    ],
    completedSignatures: [
      { id: 3, title: 'Chef de département', status: 'COMPLETED', date: '2023-11-12T14:20:00' },
      { id: 4, title: 'Service scolarité', status: 'COMPLETED', date: '2023-11-10T09:15:00' },
      { id: 5, title: 'Bibliothèque', status: 'COMPLETED', date: '2023-11-05T11:45:00' },
    ],
    notifications: [
      { 
        id: 1, 
        message: 'Votre demande a été validée par le Chef de département',
        date: '2023-11-12T14:20:00',
        read: false
      },
      { 
        id: 2, 
        message: 'Votre demande a été validée par le Service scolarité',
        date: '2023-11-10T09:15:00',
        read: true
      },
    ],
    documents: [
      {
        id: 1,
        title: 'Attestation de Réussite',
        description: 'Attestation officielle de réussite au diplôme',
        type: 'PDF',
        dateGenerated: '2023-11-15T10:30:00',
        available: false,
      },
      {
        id: 2,
        title: 'Relevé de Notes Final',
        description: 'Relevé détaillé des notes obtenues pendant le cursus',
        type: 'PDF',
        dateGenerated: null,
        available: false,
      },
    ],
  };

  // Utiliser les données simulées si aucune donnée réelle n'est disponible
  const displayData = data || simulatedData;

  // Vérifier que toutes les propriétés nécessaires existent
  const notifications = displayData?.notifications || [];
  const documents = displayData?.documents || [];

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Tableau de bord
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <Grid container spacing={3}>
        {/* Informations de l'étudiant */}
        <Grid item xs={12} md={4}>
          <Paper elevation={2} sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Informations personnelles
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Nom complet
              </Typography>
              <Typography variant="body1" fontWeight="medium">
                {displayData?.student?.name || 'Non disponible'}
              </Typography>
            </Box>
            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Matricule
              </Typography>
              <Typography variant="body1" fontWeight="medium">
                {displayData?.student?.matricule || 'Non disponible'}
              </Typography>
            </Box>
            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Département
              </Typography>
              <Typography variant="body1" fontWeight="medium">
                {displayData?.student?.department || 'Non disponible'}
              </Typography>
            </Box>
            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Programme
              </Typography>
              <Typography variant="body1" fontWeight="medium">
                {displayData?.student?.program || 'Non disponible'}
              </Typography>
            </Box>
            <Box>
              <Typography variant="body2" color="text.secondary">
                Année de graduation
              </Typography>
              <Typography variant="body1" fontWeight="medium">
                {displayData?.student?.graduationYear || 'Non disponible'}
              </Typography>
            </Box>
          </Paper>
        </Grid>

        {/* Statut de validation */}
        <Grid item xs={12} md={8}>
          <Paper elevation={2} sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6">
                Statut de validation
              </Typography>
              <Chip 
                label={getStatusText(displayData?.validationStatus?.status)}
                color={getStatusColor(displayData?.validationStatus?.status)}
                size="medium"
              />
            </Box>
            <Divider sx={{ mb: 2 }} />
            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Progression de la validation
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Box sx={{ width: '100%', mr: 1 }}>
                  <LinearProgress 
                    variant="determinate" 
                    value={displayData?.validationStatus?.progress || 0} 
                    sx={{ height: 10, borderRadius: 5 }}
                  />
                </Box>
                <Box sx={{ minWidth: 35 }}>
                  <Typography variant="body2" color="text.secondary">
                    {`${displayData?.validationStatus?.progress || 0}%`}
                  </Typography>
                </Box>
              </Box>
              <Typography variant="body2" sx={{ mt: 1 }}>
                {displayData?.validationStatus?.completedSteps || 0} sur {displayData?.validationStatus?.totalSteps || 0} étapes complétées
              </Typography>
            </Box>
            <Button 
              variant="contained" 
              onClick={() => navigate('/student/status')}
              sx={{ mt: 2 }}
            >
              Voir les détails
            </Button>
          </Paper>
        </Grid>

        {/* Notifications récentes */}
        <Grid item xs={12} md={6}>
          <Paper elevation={2} sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Notifications récentes
            </Typography>
            <Divider sx={{ mb: 2 }} />
            {notifications.length > 0 ? (
              notifications.map((notification) => (
                <Alert 
                  key={notification.id}
                  severity="info" 
                  sx={{ mb: 2 }}
                  icon={<PendingIcon fontSize="inherit" />}
                >
                  <Typography variant="body2">
                    {notification.message}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {new Date(notification.date).toLocaleDateString()} à {new Date(notification.date).toLocaleTimeString()}
                  </Typography>
                </Alert>
              ))
            ) : (
              <Typography variant="body2" color="text.secondary">
                Aucune notification récente.
              </Typography>
            )}
          </Paper>
        </Grid>

        {/* Documents disponibles */}
        <Grid item xs={12} md={6}>
          <Paper elevation={2} sx={{ p: 3, height: '100%' }}>
            <Typography variant="h6" gutterBottom>
              Documents
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              {documents.length > 0 ? (
                documents.map((document) => (
                  <Card key={document.id} variant="outlined">
                    <CardContent>
                      <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                        <ArticleIcon color="primary" sx={{ mr: 1 }} />
                        <Typography variant="body1">
                          {document.title}
                        </Typography>
                      </Box>
                      <Typography variant="body2" color="text.secondary">
                        {document.description}
                      </Typography>
                      <Chip 
                        label={document.available ? 'Disponible' : 'En attente'} 
                        color={document.available ? 'success' : 'warning'}
                        size="small"
                        sx={{ mt: 1 }}
                      />
                    </CardContent>
                    <CardActions>
                      {document.available && (
                        <Button 
                          size="small" 
                          startIcon={<DownloadIcon />}
                          onClick={() => navigate('/student/documents')}
                        >
                          Télécharger
                        </Button>
                      )}
                    </CardActions>
                  </Card>
                ))
              ) : (
                <Typography variant="body2" color="text.secondary">
                  Aucun document disponible.
                </Typography>
              )}
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;