import { Grid, LinearProgress } from '@mui/material';
import React, { useState, useEffect } from 'react';
import { studentService } from '../../services/student.service';
import {
  Box,
  Typography,
  Paper,
  Stepper,
  Step,
  StepLabel,
  StepContent,
  Button,
  CircularProgress,
  Alert,
  Divider,
  Card,
  CardContent,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Avatar,
  Chip
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import PendingIcon from '@mui/icons-material/Pending';
import CancelIcon from '@mui/icons-material/Cancel';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import PersonIcon from '@mui/icons-material/Person';
import { useNavigate } from 'react-router-dom';

const getStatusIcon = (status) => {
  switch (status) {
    case 'COMPLETED':
      return <CheckCircleIcon color="success" />;
    case 'PENDING':
      return <PendingIcon color="warning" />;
    case 'REJECTED':
      return <CancelIcon color="error" />;
    default:
      return <AccessTimeIcon color="info" />;
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

const ValidationStatus = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [validationData, setValidationData] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchValidationStatus = async () => {
      try {
        const data = await studentService.getValidationStatus();
        setValidationData(data);
      } catch (err) {
        setError('Impossible de charger les données de validation. Veuillez réessayer plus tard.');
        console.error('Error fetching validation status:', err);
      } finally {
        setLoading(false);
      }
    };

    // Cette fonction simule le service getValidationStatus pour le développement
    const simulateValidationStatus = () => {
      setTimeout(() => {
        const mockData = {
          overallStatus: 'PENDING', // PENDING, COMPLETED, REJECTED
          progress: 60,
          startDate: '2023-11-01T09:00:00',
          expectedCompletionDate: '2023-12-15T17:00:00',
          lastUpdate: '2023-11-15T14:30:00',
          steps: [
            {
              id: 1,
              title: 'Soumission de la demande',
              description: 'Demande initiale de validation du diplôme',
              status: 'COMPLETED',
              completedDate: '2023-11-01T10:15:00',
              validatedBy: {
                name: 'Service Scolarité',
                role: 'Administratif'
              },
              comments: 'Demande reçue et traitée avec succès.'
            },
            {
              id: 2,
              title: 'Vérification scolarité',
              description: 'Vérification des crédits et des résultats',
              status: 'COMPLETED',
              completedDate: '2023-11-05T11:45:00',
              validatedBy: {
                name: 'Mme Diagne',
                role: 'Chef Service Scolarité'
              },
              comments: 'Tous les crédits validés. Moyenne générale conforme aux exigences.'
            },
            {
              id: 3,
              title: 'Validation département',
              description: 'Validation par le chef de département',
              status: 'COMPLETED',
              completedDate: '2023-11-12T14:20:00',
              validatedBy: {
                name: 'Dr. Ndiaye',
                role: 'Chef de Département'
              },
              comments: 'Parcours académique validé par le département.'
            },
            {
              id: 4,
              title: 'Validation administration',
              description: 'Vérification administrative et financière',
              status: 'PENDING',
              completedDate: null,
              validatedBy: null,
              comments: null
            },
            {
              id: 5,
              title: 'Signature diplôme',
              description: 'Signature finale du diplôme par la direction',
              status: 'PENDING',
              completedDate: null,
              validatedBy: null,
              comments: null
            }
          ],
          documents: [
            {
              id: 1,
              title: 'Relevé de notes',
              status: 'COMPLETED',
              date: '2023-11-05T11:30:00'
            },
            {
              id: 2,
              title: 'Attestation de fin de formation',
              status: 'PENDING',
              date: null
            },
            {
              id: 3,
              title: 'Diplôme officiel',
              status: 'PENDING',
              date: null
            }
          ],
          additionalInfo: 'Votre dossier est en cours de traitement. La validation finale est prévue pour décembre 2023.'
        };

        setValidationData(mockData);
        setLoading(false);
      }, 1500);
    };

    // Utiliser la fonction simulée pour le développement
    simulateValidationStatus();
  }, []);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '70vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ mt: 3 }}>
        <Alert severity="error">{error}</Alert>
        <Button 
          variant="outlined" 
          sx={{ mt: 2 }} 
          onClick={() => window.location.reload()}
        >
          Réessayer
        </Button>
      </Box>
    );
  }

  // Calculer l'étape active pour le stepper
  const activeStep = validationData.steps.findIndex(step => step.status === 'PENDING');

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        État de validation du diplôme
      </Typography>

      <Grid container spacing={3}>
        {/* Carte d'état général */}
        <Grid item xs={12}>
          <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6">
                Statut général
              </Typography>
              <Chip 
                label={getStatusText(validationData.overallStatus)}
                color={getStatusColor(validationData.overallStatus)}
                size="medium"
              />
            </Box>
            <Divider sx={{ mb: 2 }} />
            
            <Grid container spacing={3}>
              <Grid item xs={12} md={4}>
                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="text.secondary">
                    Date de début
                  </Typography>
                  <Typography variant="body1" fontWeight="medium">
                    {new Date(validationData.startDate).toLocaleDateString()}
                  </Typography>
                </Box>
              </Grid>
              <Grid item xs={12} md={4}>
                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="text.secondary">
                    Dernière mise à jour
                  </Typography>
                  <Typography variant="body1" fontWeight="medium">
                    {new Date(validationData.lastUpdate).toLocaleDateString()}
                  </Typography>
                </Box>
              </Grid>
              <Grid item xs={12} md={4}>
                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="text.secondary">
                    Date prévue d'achèvement
                  </Typography>
                  <Typography variant="body1" fontWeight="medium">
                    {new Date(validationData.expectedCompletionDate).toLocaleDateString()}
                  </Typography>
                </Box>
              </Grid>
            </Grid>

            <Box sx={{ mt: 2 }}>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Progression de la validation ({validationData.progress}%)
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Box sx={{ width: '100%', mr: 1 }}>
                  <LinearProgress 
                    variant="determinate" 
                    value={validationData.progress} 
                    sx={{ height: 10, borderRadius: 5 }}
                  />
                </Box>
                <Box sx={{ minWidth: 35 }}>
                  <Typography variant="body2" color="text.secondary">
                    {`${validationData.progress}%`}
                  </Typography>
                </Box>
              </Box>
            </Box>

            {validationData.additionalInfo && (
              <Alert severity="info" sx={{ mt: 2 }}>
                {validationData.additionalInfo}
              </Alert>
            )}
          </Paper>
        </Grid>

        {/* Progression des étapes */}
        <Grid item xs={12} md={8}>
          <Paper elevation={2} sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Étapes de validation
            </Typography>
            <Divider sx={{ mb: 3 }} />
            
            <Stepper activeStep={activeStep !== -1 ? activeStep : validationData.steps.length} orientation="vertical">
              {validationData.steps.map((step) => (
                <Step key={step.id} completed={step.status === 'COMPLETED'}>
                  <StepLabel 
                    StepIconProps={{ 
                      icon: getStatusIcon(step.status) 
                    }}
                  >
                    <Typography variant="subtitle1">
                      {step.title}
                    </Typography>
                  </StepLabel>
                  <StepContent>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      {step.description}
                    </Typography>
                    
                    {step.status === 'COMPLETED' && (
                      <Box sx={{ mt: 2, mb: 2 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                          <PersonIcon fontSize="small" sx={{ mr: 1, color: 'text.secondary' }} />
                          <Typography variant="body2">
                            Validé par: <strong>{step.validatedBy.name}</strong> ({step.validatedBy.role})
                          </Typography>
                        </Box>
                        <Typography variant="body2" color="text.secondary">
                          Le {new Date(step.completedDate).toLocaleDateString()} à {new Date(step.completedDate).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </Typography>
                        {step.comments && (
                          <Typography variant="body2" sx={{ mt: 1, fontStyle: 'italic' }}>
                            "{step.comments}"
                          </Typography>
                        )}
                      </Box>
                    )}
                    
                    {step.status === 'PENDING' && (
                      <Box sx={{ mt: 2, mb: 2 }}>
                        <Typography variant="body2" color="warning.main">
                          Cette étape est en cours de traitement.
                        </Typography>
                      </Box>
                    )}
                    
                    {step.status === 'REJECTED' && (
                      <Box sx={{ mt: 2, mb: 2 }}>
                        <Typography variant="body2" color="error">
                          Cette étape a été rejetée. {step.comments}
                        </Typography>
                      </Box>
                    )}
                  </StepContent>
                </Step>
              ))}
            </Stepper>

            {validationData.overallStatus === 'COMPLETED' && (
              <Box sx={{ mt: 3, textAlign: 'center' }}>
                <CheckCircleIcon color="success" sx={{ fontSize: 48, mb: 1 }} />
                <Typography variant="h6" color="success.main">
                  Toutes les étapes ont été complétées avec succès!
                </Typography>
                <Button
                  variant="contained"
                  color="primary"
                  sx={{ mt: 2 }}
                  onClick={() => navigate('/student/documents')}
                >
                  Voir mes documents disponibles
                </Button>
              </Box>
            )}
          </Paper>
        </Grid>

        {/* Documents associés */}
        <Grid item xs={12} md={4}>
          <Paper elevation={2} sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Documents associés
            </Typography>
            <Divider sx={{ mb: 2 }} />
            
            <List>
              {validationData.documents.map((doc) => (
                <ListItem 
                  key={doc.id}
                  disablePadding
                  sx={{ mb: 2, display: 'block' }}
                >
                  <Card variant="outlined">
                    <CardContent sx={{ pb: '16px !important' }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                        <Typography variant="body1">
                          {doc.title}
                        </Typography>
                        <Chip 
                          label={getStatusText(doc.status)}
                          color={getStatusColor(doc.status)}
                          size="small"
                        />
                      </Box>
                      
                      {doc.status === 'COMPLETED' && doc.date && (
                        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                          Disponible depuis le {new Date(doc.date).toLocaleDateString()}
                        </Typography>
                      )}
                      
                      {doc.status === 'COMPLETED' && (
                        <Button 
                          variant="text" 
                          size="small" 
                          sx={{ mt: 1 }}
                          onClick={() => navigate('/student/documents')}
                        >
                          Télécharger
                        </Button>
                      )}
                    </CardContent>
                  </Card>
                </ListItem>
              ))}
            </List>
            
            <Button 
              variant="outlined" 
              fullWidth 
              sx={{ mt: 2 }}
              onClick={() => navigate('/student/documents')}
            >
              Voir tous les documents
            </Button>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default ValidationStatus;