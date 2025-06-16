// Components/admin/RequestDetails.jsx - Détails d'une demande de validation
import React, { useState, useEffect, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { adminService } from '../../services/admin.service';
import { AuthContext } from '../../context/AuthContext';
import {
  Box,
  Typography,
  Paper,
  Grid,
  Card,
  CardContent,
  Button,
  TextField,
  Stepper,
  Step,
  StepLabel,
  StepContent,
  Divider,
  Alert,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Chip,
  Avatar,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  IconButton,
  Tooltip
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import PendingIcon from '@mui/icons-material/Pending';
import PersonIcon from '@mui/icons-material/Person';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import DownloadIcon from '@mui/icons-material/Download';
import HistoryIcon from '@mui/icons-material/History';
import AssignmentIcon from '@mui/icons-material/Assignment';
import SchoolIcon from '@mui/icons-material/School';

const RequestDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [requestData, setRequestData] = useState(null);
  const [comment, setComment] = useState('');
  const [confirmDialogOpen, setConfirmDialogOpen] = useState(false);
  const [action, setAction] = useState(null); // 'approve' ou 'reject'

  useEffect(() => {
    const fetchRequestDetails = async () => {
      try {
        const data = await adminService.getRequestDetails(id);
        setRequestData(data);
      } catch (err) {
        setError('Impossible de charger les détails de la demande. Veuillez réessayer plus tard.');
        console.error('Error fetching request details:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchRequestDetails();
  }, [id]);

  // Données simulées pour le développement
  const simulatedData = {
    id: parseInt(id),
    student: {
      id: `ESP${20000 + parseInt(id)}`,
      name: `Étudiant ${id}`,
      department: 'Informatique',
      program: 'Licence en Informatique',
      email: `etudiant${id}@esp.sn`,
      phone: `+221 77 123 45 ${id.padStart(2, '0')}`,
      enrollmentYear: '2020',
      graduationYear: '2023',
    },
    status: 'PENDING',
    submissionDate: '2023-11-01T09:00:00',
    lastUpdated: '2023-11-15T14:30:00',
    currentStep: 3,
    steps: [
      {
        id: 1,
        title: 'Soumission de la demande',
        description: 'Demande de validation du diplôme soumise par l\'étudiant',
        status: 'COMPLETED',
        date: '2023-11-01T09:00:00',
        comments: 'Soumission validée',
      },
      {
        id: 2,
        title: 'Validation par le Service Scolarité',
        description: 'Vérification des informations académiques par le service de scolarité',
        status: 'COMPLETED',
        date: '2023-11-05T14:30:00',
        validator: 'Mme Diop',
        comments: 'Dossier académique complet et validé',
      },
      {
        id: 3,
        title: 'Validation par le Chef de Département',
        description: 'Vérification et approbation par le chef de département',
        status: 'COMPLETED',
        date: '2023-11-12T10:15:00',
        validator: 'Dr. Ndiaye',
        comments: 'Parcours de l\'étudiant validé',
      },
      {
        id: 4,
        title: 'Validation par le Service Comptabilité',
        description: 'Vérification de la situation financière de l\'étudiant',
        status: 'PENDING',
        validator: 'M. Sall',
        comments: null,
      },
      {
        id: 5,
        title: 'Validation par le Directeur des Études',
        description: 'Approbation finale et signature du directeur des études',
        status: 'PENDING',
        validator: 'Pr. Fall',
        comments: null,
      },
    ],
    documents: [
      {
        id: 1,
        title: 'Relevé de notes',
        type: 'PDF',
        uploadDate: '2023-11-01T09:00:00',
      },
      {
        id: 2,
        title: 'Certificat de scolarité',
        type: 'PDF',
        uploadDate: '2023-11-01T09:00:00',
      },
    ],
    history: [
      {
        date: '2023-11-12T10:15:00',
        user: 'Dr. Ndiaye',
        action: 'Validation de l\'étape Chef de Département',
        comments: 'Parcours de l\'étudiant validé',
      },
      {
        date: '2023-11-05T14:30:00',
        user: 'Mme Diop',
        action: 'Validation de l\'étape Service Scolarité',
        comments: 'Dossier académique complet et validé',
      },
      {
        date: '2023-11-01T09:00:00',
        user: 'Système',
        action: 'Création de la demande',
        comments: 'Soumission validée',
      },
    ],
  };

  // Déterminer si l'administrateur peut valider cette étape
  const canValidateCurrentStep = () => {
    if (!requestData || requestData.status !== 'PENDING') return false;
    
    // Simuler la vérification du rôle de l'administrateur pour cette étape
    const currentStepIndex = requestData.steps.findIndex(step => step.status === 'PENDING');
    if (currentStepIndex === -1) return false;
    
    const currentStep = requestData.steps[currentStepIndex];
    const adminRole = user?.role || 'Service Comptabilité'; // Rôle simulé pour le développement
    
    // Vérifier si le titre de l'étape contient le rôle de l'admin
    return currentStep.title.includes(adminRole);
  };

  // Utiliser les données simulées pour le développement
  const displayData = requestData || simulatedData;

  const handleApprove = () => {
    setAction('approve');
    setConfirmDialogOpen(true);
  };

  const handleReject = () => {
    setAction('reject');
    setConfirmDialogOpen(true);
  };

  const handleConfirmAction = async () => {
    setConfirmDialogOpen(false);
    
    try {
      // Dans une application réelle, cela appellerait l'API
      console.log(`Action ${action} confirmée pour la demande ${id} avec commentaire: ${comment}`);
      
      // Simuler la mise à jour des données
      const updatedData = { ...displayData };
      const currentStepIndex = updatedData.steps.findIndex(step => step.status === 'PENDING');
      
      if (currentStepIndex !== -1) {
        updatedData.steps[currentStepIndex].status = action === 'approve' ? 'COMPLETED' : 'REJECTED';
        updatedData.steps[currentStepIndex].comments = comment;
        updatedData.steps[currentStepIndex].date = new Date().toISOString();
        updatedData.steps[currentStepIndex].validator = user?.name || 'Administrateur';
        
        // Mettre à jour l'historique
        updatedData.history.unshift({
          date: new Date().toISOString(),
          user: user?.name || 'Administrateur',
          action: action === 'approve' 
            ? `Validation de l'étape ${updatedData.steps[currentStepIndex].title}` 
            : `Rejet de l'étape ${updatedData.steps[currentStepIndex].title}`,
          comments: comment,
        });
        
        // Mettre à jour le statut global si c'est la dernière étape ou si c'est un rejet
        if (action === 'reject') {
          updatedData.status = 'REJECTED';
        } else if (currentStepIndex === updatedData.steps.length - 1) {
          updatedData.status = 'COMPLETED';
        }
        
        setRequestData(updatedData);
        setComment('');
        
        // Afficher un message de succès (dans une application réelle)
        alert(`La demande a été ${action === 'approve' ? 'approuvée' : 'rejetée'} avec succès.`);
      }
    } catch (err) {
      console.error(`Error ${action}ing request:`, err);
      setError(`Une erreur s'est produite lors de l'${action === 'approve' ? 'approbation' : 'rejet'} de la demande.`);
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'COMPLETED':
        return <CheckCircleIcon color="success" />;
      case 'PENDING':
        return <PendingIcon color="warning" />;
      case 'REJECTED':
        return <CancelIcon color="error" />;
      default:
        return <AccessTimeIcon color="disabled" />;
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

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '70vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  const activeStep = displayData.steps.findIndex(step => step.status === 'PENDING');

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <IconButton onClick={() => navigate('/admin/requests')} sx={{ mr: 1 }}>
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h4">
          Détails de la Demande #{displayData.id}
        </Typography>
        <Chip 
          label={getStatusText(displayData.status)} 
          color={getStatusColor(displayData.status)}
          sx={{ ml: 2 }}
        />
      </Box>
      
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}
      
      <Grid container spacing={3}>
        {/* Informations de l'étudiant */}
        <Grid item xs={12} md={4}>
          <Paper elevation={2} sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <Avatar sx={{ bgcolor: 'primary.main', mr: 2 }}>
                <PersonIcon />
              </Avatar>
              <Typography variant="h6">
                Informations de l'étudiant
              </Typography>
            </Box>
            <Divider sx={{ mb: 2 }} />
            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Nom complet
              </Typography>
              <Typography variant="body1" fontWeight="medium">
                {displayData.student.name}
              </Typography>
            </Box>
            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Matricule
              </Typography>
              <Typography variant="body1" fontWeight="medium">
                {displayData.student.id}
              </Typography>
            </Box>
            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Département
              </Typography>
              <Typography variant="body1" fontWeight="medium">
                {displayData.student.department}
              </Typography>
            </Box>
            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Programme
              </Typography>
              <Typography variant="body1" fontWeight="medium">
                {displayData.student.program}
              </Typography>
            </Box>
            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Email
              </Typography>
              <Typography variant="body1" fontWeight="medium">
                {displayData.student.email}
              </Typography>
            </Box>
            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Téléphone
              </Typography>
              <Typography variant="body1" fontWeight="medium">
                {displayData.student.phone}
              </Typography>
            </Box>
            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" color="text.secondary">
                Année d'inscription
              </Typography>
              <Typography variant="body1" fontWeight="medium">
                {displayData.student.enrollmentYear}
              </Typography>
            </Box>
            <Box>
              <Typography variant="body2" color="text.secondary">
                Année de graduation
              </Typography>
              <Typography variant="body1" fontWeight="medium">
                {displayData.student.graduationYear}
              </Typography>
            </Box>
          </Paper>

          {/* Documents */}
          <Paper elevation={2} sx={{ p: 3, mt: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <AssignmentIcon color="primary" sx={{ mr: 1 }} />
              <Typography variant="h6">
                Documents fournis
              </Typography>
            </Box>
            <Divider sx={{ mb: 2 }} />
            <List>
              {displayData.documents.map((doc) => (
                <ListItem
                  key={doc.id}
                  secondaryAction={
                    <IconButton edge="end" aria-label="download">
                      <DownloadIcon />
                    </IconButton>
                  }
                >
                  <ListItemIcon>
                    <AssignmentIcon />
                  </ListItemIcon>
                  <ListItemText
                    primary={doc.title}
                    secondary={`Ajouté le ${new Date(doc.uploadDate).toLocaleDateString()}`}
                  />
                </ListItem>
              ))}
            </List>
          </Paper>
        </Grid>

        {/* Processus de validation */}
        <Grid item xs={12} md={8}>
          <Paper elevation={2} sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <SchoolIcon color="primary" sx={{ mr: 1 }} />
              <Typography variant="h6">
                Processus de validation
              </Typography>
            </Box>
            <Divider sx={{ mb: 3 }} />
            
            <Stepper activeStep={activeStep === -1 ? displayData.steps.length : activeStep} orientation="vertical">
              {displayData.steps.map((step, index) => (
                <Step key={step.id} completed={step.status === 'COMPLETED'}>
                  <StepLabel 
                    StepIconComponent={() => getStatusIcon(step.status)}
                    sx={{ '& .MuiStepLabel-label': { fontWeight: 'medium' } }}
                  >
                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                      <Typography variant="subtitle1">{step.title}</Typography>
                      {step.status === 'COMPLETED' && step.date && (
                        <Typography variant="caption" color="text.secondary" sx={{ ml: 2 }}>
                          {new Date(step.date).toLocaleDateString()}
                        </Typography>
                      )}
                    </Box>
                  </StepLabel>
                  <StepContent>
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="body2" color="text.secondary" paragraph>
                        {step.description}
                      </Typography>
                      
                      {step.validator && (
                        <Box sx={{ display: 'flex', alignItems: 'center', mt: 1, mb: 1 }}>
                          <Avatar sx={{ width: 24, height: 24, bgcolor: 'primary.main', mr: 1 }}>
                            <PersonIcon sx={{ fontSize: 16 }} />
                          </Avatar>
                          <Typography variant="body2">
                            Responsable: {step.validator}
                          </Typography>
                        </Box>
                      )}
                      
                      {step.comments && (
                        <Alert 
                          severity={step.status === 'COMPLETED' ? 'success' : step.status === 'REJECTED' ? 'error' : 'info'} 
                          sx={{ mt: 1 }}
                        >
                          {step.comments}
                        </Alert>
                      )}
                      
                      <Chip 
                        label={step.status === 'COMPLETED' ? 'Validé' : step.status === 'REJECTED' ? 'Rejeté' : 'En attente'} 
                        color={getStatusColor(step.status)}
                        size="small"
                        sx={{ mt: 2 }}
                      />
                    </Box>
                  </StepContent>
                </Step>
              ))}
            </Stepper>
            
            {activeStep === -1 && (
              <Box sx={{ mt: 3, textAlign: 'center' }}>
                <Alert severity="success" sx={{ mb: 2 }}>
                  Toutes les étapes de validation ont été complétées !
                </Alert>
              </Box>
            )}
            
            {/* Actions de validation pour l'administrateur */}
            {canValidateCurrentStep() && (
              <Box sx={{ mt: 4, p: 2, border: '1px solid', borderColor: 'divider', borderRadius: 1 }}>
                <Typography variant="h6" gutterBottom>
                  Action requise
                </Typography>
                <Typography variant="body2" paragraph>
                  En tant que {user?.role || 'administrateur'}, vous devez approuver ou rejeter cette étape:
                </Typography>
                
                <TextField
                  label="Commentaire"
                  multiline
                  rows={4}
                  fullWidth
                  variant="outlined"
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  placeholder="Ajoutez un commentaire pour cette validation..."
                  sx={{ mb: 2 }}
                />
                
                <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
                  <Button 
                    variant="outlined" 
                    color="error"
                    startIcon={<CancelIcon />}
                    onClick={handleReject}
                  >
                    Rejeter
                  </Button>
                  <Button 
                    variant="contained" 
                    color="success"
                    startIcon={<CheckCircleIcon />}
                    onClick={handleApprove}
                  >
                    Approuver
                  </Button>
                </Box>
              </Box>
            )}
          </Paper>
          
          {/* Historique des actions */}
          <Paper elevation={2} sx={{ p: 3, mt: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <HistoryIcon color="primary" sx={{ mr: 1 }} />
              <Typography variant="h6">
                Historique des actions
              </Typography>
            </Box>
            <Divider sx={{ mb: 2 }} />
            
            <List>
              {displayData.history.map((item, index) => (
                <ListItem key={index} alignItems="flex-start" sx={{ py: 1 }}>
                  <ListItemIcon>
                    <Avatar sx={{ bgcolor: 'primary.light', width: 32, height: 32 }}>
                      <PersonIcon fontSize="small" />
                    </Avatar>
                  </ListItemIcon>
                  <ListItemText
                    primary={
                      <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                        <Typography variant="subtitle2">{item.action}</Typography>
                        <Typography variant="caption" color="text.secondary">
                          {new Date(item.date).toLocaleDateString()} {new Date(item.date).toLocaleTimeString()}
                        </Typography>
                      </Box>
                    }
                    secondary={
                      <>
                        <Typography variant="body2" component="span" color="text.primary">
                          {item.user}
                        </Typography>
                        {item.comments && (
                          <Typography variant="body2" component="div" sx={{ mt: 0.5 }}>
                            {item.comments}
                          </Typography>
                        )}
                      </>
                    }
                  />
                </ListItem>
              ))}
            </List>
          </Paper>
        </Grid>
      </Grid>
      
      {/* Dialogue de confirmation */}
      <Dialog
        open={confirmDialogOpen}
        onClose={() => setConfirmDialogOpen(false)}
      >
        <DialogTitle>
          {action === 'approve' ? 'Confirmer l\'approbation' : 'Confirmer le rejet'}
        </DialogTitle>
        <DialogContent>
          <DialogContentText>
            {action === 'approve' 
              ? 'Êtes-vous sûr de vouloir approuver cette étape de validation ?'
              : 'Êtes-vous sûr de vouloir rejeter cette demande ? Cette action ne peut pas être annulée.'}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmDialogOpen(false)}>Annuler</Button>
          <Button 
            onClick={handleConfirmAction} 
            color={action === 'approve' ? 'success' : 'error'}
            variant="contained"
            autoFocus
          >
            Confirmer
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default RequestDetails;