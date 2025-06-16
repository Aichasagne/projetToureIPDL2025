// Components/admin/SignaturePanel.jsx - Panneau de signature pour les administrateurs
import React, { useState, useRef, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { adminService } from '../../services/admin.service';
import {
  Box,
  Typography,
  Paper,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Alert,
  CircularProgress,
  Divider,
  Grid,
  Card,
  CardContent,
  Chip,
  IconButton,
  Tooltip,
  Collapse,
  FormControl,
  FormControlLabel,
  Radio,
  RadioGroup,
  Stack
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import SaveIcon from '@mui/icons-material/Save';
import GestureIcon from '@mui/icons-material/Gesture';
import DeleteIcon from '@mui/icons-material/Delete';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';

const SignaturePanel = () => {
  const { requestId } = useParams();
  const navigate = useNavigate();
  const canvasRef = useRef(null);
  const [isDrawing, setIsDrawing] = useState(false);
  const [signature, setSignature] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [confirmDialogOpen, setConfirmDialogOpen] = useState(false);
  const [comments, setComments] = useState('');
  const [action, setAction] = useState('approve');
  const [requestDetails, setRequestDetails] = useState(null);
  const [showDetails, setShowDetails] = useState(true);

  // Récupérer les détails de la demande
  useEffect(() => {
    const fetchRequestDetails = async () => {
      try {
        // Dans une application réelle, vous obtiendriez ces données depuis l'API
        const data = await adminService.getRequestDetails(requestId);
        setRequestDetails(data);
      } catch (err) {
        setError('Impossible de charger les détails de la demande.');
        console.error('Error fetching request details:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchRequestDetails();
  }, [requestId]);

  // Initialiser le canvas de signature
  useEffect(() => {
    if (canvasRef.current) {
      const canvas = canvasRef.current;
      const context = canvas.getContext('2d');
      context.lineWidth = 2;
      context.strokeStyle = '#000';
      context.lineJoin = 'round';
      context.lineCap = 'round';
    }
  }, []);

  // Données simulées pour le développement
  const simulatedRequest = {
    id: requestId,
    studentName: 'Abdoulaye Diop',
    matricule: 'ESP20001',
    department: 'Informatique',
    program: 'Licence en Informatique',
    submissionDate: '2023-11-01T09:00:00',
    status: 'PENDING',
    currentStep: 'Validation Service Comptabilité',
    stepDescription: 'Vérification de la situation financière de l\'étudiant',
    previousSignatures: [
      {
        id: 1,
        title: 'Service Scolarité',
        signerName: 'Mme Diop',
        date: '2023-11-05T14:30:00',
        comments: 'Dossier académique complet et validé',
      },
      {
        id: 2,
        title: 'Chef de Département',
        signerName: 'Dr. Ndiaye',
        date: '2023-11-12T10:15:00',
        comments: 'Parcours de l\'étudiant validé',
      }
    ],
    studentInfo: {
      address: 'Dakar, Sénégal',
      email: 'a.diop@esp.edu.sn',
      phone: '+221 xx xx xx xx',
      birthDate: '1998-05-15',
      birthPlace: 'Dakar',
      nationality: 'Sénégalaise',
    },
    academicInfo: {
      entryYear: '2020',
      graduationYear: '2023',
      averageGrade: '15.2/20',
      mention: 'Bien',
      thesis: 'Développement d\'une application mobile pour la gestion des projets académiques',
    },
    financialStatus: {
      tuitionPaid: true,
      outstandingFees: 0,
      scholarshipInfo: 'N/A',
    }
  };

  // Utiliser les données simulées pour le développement
  const displayRequest = requestDetails || simulatedRequest;

  // Gestion du dessin sur le canvas
  const startDrawing = (e) => {
    const canvas = canvasRef.current;
    const context = canvas.getContext('2d');
    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    
    context.beginPath();
    context.moveTo(x, y);
    setIsDrawing(true);
  };

  const draw = (e) => {
    if (!isDrawing) return;
    
    const canvas = canvasRef.current;
    const context = canvas.getContext('2d');
    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    
    context.lineTo(x, y);
    context.stroke();
  };

  const stopDrawing = () => {
    if (isDrawing) {
      const canvas = canvasRef.current;
      const context = canvas.getContext('2d');
      context.closePath();
      setIsDrawing(false);
      
      // Sauvegarder la signature
      setSignature(canvas.toDataURL());
    }
  };

  const clearSignature = () => {
    const canvas = canvasRef.current;
    const context = canvas.getContext('2d');
    context.clearRect(0, 0, canvas.width, canvas.height);
    setSignature(null);
  };

  const handleOpenConfirmDialog = () => {
    if (!signature) {
      setError('Veuillez signer le document avant de continuer.');
      return;
    }
    setError(null);
    setConfirmDialogOpen(true);
  };

  const handleCloseConfirmDialog = () => {
    setConfirmDialogOpen(false);
  };

  const handleSubmit = async () => {
    setSubmitting(true);
    setError(null);
    
    try {
      // Dans une application réelle, vous enverriez ces données à l'API
      await adminService.submitSignature({
        requestId,
        signature,
        action,
        comments
      });
      
      setSuccess(true);
      handleCloseConfirmDialog();
      
      // Rediriger après 2 secondes
      setTimeout(() => {
        navigate('/admin/requests');
      }, 2000);
    } catch (err) {
      setError('Erreur lors de la soumission de la signature. Veuillez réessayer.');
      console.error('Error submitting signature:', err);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '70vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <IconButton onClick={() => navigate('/admin/requests')} sx={{ mr: 2 }}>
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h4">
          Signature du Document
        </Typography>
      </Box>
      
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}
      
      {success && (
        <Alert 
          severity="success" 
          sx={{ mb: 3 }}
          action={
            <IconButton
              aria-label="close"
              color="inherit"
              size="small"
              onClick={() => setSuccess(false)}
            >
              <CloseIcon fontSize="inherit" />
            </IconButton>
          }
        >
          La signature a été enregistrée avec succès. Redirection en cours...
        </Alert>
      )}
      
      <Grid container spacing={3}>
        {/* Détails de la demande */}
        <Grid item xs={12}>
          <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
            <Box 
              sx={{ 
                display: 'flex', 
                justifyContent: 'space-between', 
                alignItems: 'center',
                cursor: 'pointer'
              }}
              onClick={() => setShowDetails(!showDetails)}
            >
              <Typography variant="h6">
                Détails de la demande
              </Typography>
              <IconButton>
                <ExpandMoreIcon 
                  sx={{ 
                    transform: showDetails ? 'rotate(180deg)' : 'rotate(0deg)',
                    transition: 'transform 0.3s'
                  }} 
                />
              </IconButton>
            </Box>
            
            <Collapse in={showDetails}>
              <Divider sx={{ my: 2 }} />
              
              <Grid container spacing={3}>
                <Grid item xs={12} md={6}>
                  <Card variant="outlined" sx={{ mb: 2 }}>
                    <CardContent>
                      <Typography variant="subtitle1" gutterBottom>
                        Informations sur l'étudiant
                      </Typography>
                      <Box sx={{ mb: 1 }}>
                        <Typography variant="body2" color="text.secondary">
                          Nom complet
                        </Typography>
                        <Typography variant="body1" fontWeight="medium">
                          {displayRequest.studentName}
                        </Typography>
                      </Box>
                      <Box sx={{ mb: 1 }}>
                        <Typography variant="body2" color="text.secondary">
                          Matricule
                        </Typography>
                        <Typography variant="body1" fontWeight="medium">
                          {displayRequest.matricule}
                        </Typography>
                      </Box>
                      <Box sx={{ mb: 1 }}>
                        <Typography variant="body2" color="text.secondary">
                          Département
                        </Typography>
                        <Typography variant="body1" fontWeight="medium">
                          {displayRequest.department}
                        </Typography>
                      </Box>
                      <Box sx={{ mb: 1 }}>
                        <Typography variant="body2" color="text.secondary">
                          Programme
                        </Typography>
                        <Typography variant="body1" fontWeight="medium">
                          {displayRequest.program}
                        </Typography>
                      </Box>
                      <Box>
                        <Typography variant="body2" color="text.secondary">
                          Statut actuel
                        </Typography>
                        <Chip 
                          label={displayRequest.currentStep}
                          color="primary"
                          size="small"
                          sx={{ mt: 0.5 }}
                        />
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <Card variant="outlined" sx={{ mb: 2 }}>
                    <CardContent>
                      <Typography variant="subtitle1" gutterBottom>
                        Informations académiques
                      </Typography>
                      <Box sx={{ mb: 1 }}>
                        <Typography variant="body2" color="text.secondary">
                          Année d'entrée
                        </Typography>
                        <Typography variant="body1" fontWeight="medium">
                          {displayRequest.academicInfo.entryYear}
                        </Typography>
                      </Box>
                      <Box sx={{ mb: 1 }}>
                        <Typography variant="body2" color="text.secondary">
                          Année de graduation
                        </Typography>
                        <Typography variant="body1" fontWeight="medium">
                          {displayRequest.academicInfo.graduationYear}
                        </Typography>
                      </Box>
                      <Box sx={{ mb: 1 }}>
                        <Typography variant="body2" color="text.secondary">
                          Moyenne générale
                        </Typography>
                        <Typography variant="body1" fontWeight="medium">
                          {displayRequest.academicInfo.averageGrade}
                        </Typography>
                      </Box>
                      <Box>
                        <Typography variant="body2" color="text.secondary">
                          Mention
                        </Typography>
                        <Typography variant="body1" fontWeight="medium">
                          {displayRequest.academicInfo.mention}
                        </Typography>
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>
              </Grid>
              
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                Signatures précédentes
              </Typography>
              
              {displayRequest.previousSignatures.map((signature) => (
                <Alert 
                  key={signature.id}
                  icon={<CheckCircleIcon fontSize="inherit" />}
                  severity="success"
                  sx={{ mb: 2 }}
                >
                  <Typography variant="body2" fontWeight="medium">
                    {signature.title} - {signature.signerName}
                  </Typography>
                  <Typography variant="body2">
                    {signature.comments}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Signé le {new Date(signature.date).toLocaleDateString()} à {new Date(signature.date).toLocaleTimeString()}
                  </Typography>
                </Alert>
              ))}
            </Collapse>
          </Paper>
        </Grid>
        
        {/* Panneau de signature */}
        <Grid item xs={12}>
          <Paper elevation={3} sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Signature électronique
            </Typography>
            <Typography variant="body2" color="text.secondary" paragraph>
              Veuillez signer dans la zone ci-dessous pour approuver ou rejeter cette demande de validation.
            </Typography>
            
            <Divider sx={{ my: 2 }} />
            
            <Box sx={{ mb: 3 }}>
              <Typography variant="subtitle1" gutterBottom>
                Action:
              </Typography>
              <FormControl component="fieldset">
                <RadioGroup
                  row
                  name="action-radio-buttons-group"
                  value={action}
                  onChange={(e) => setAction(e.target.value)}
                >
                  <FormControlLabel value="approve" control={<Radio />} label="Approuver" />
                  <FormControlLabel value="reject" control={<Radio />} label="Rejeter" />
                </RadioGroup>
              </FormControl>
            </Box>
            
            <Box sx={{ mb: 3 }}>
              <Typography variant="subtitle1" gutterBottom>
                Commentaires:
              </Typography>
              <TextField
                fullWidth
                multiline
                rows={3}
                placeholder="Ajoutez vos commentaires ici..."
                value={comments}
                onChange={(e) => setComments(e.target.value)}
              />
            </Box>
            
            <Typography variant="subtitle1" gutterBottom sx={{ display: 'flex', alignItems: 'center' }}>
              <GestureIcon sx={{ mr: 1 }} />
              Zone de signature:
            </Typography>
            
            <Box 
              sx={{ 
                border: '1px solid #ccc',
                borderRadius: 1,
                p: 1,
                mb: 2,
                bgcolor: '#f9f9f9',
                touchAction: 'none'
              }}
            >
              <canvas
                ref={canvasRef}
                width={600}
                height={200}
                onMouseDown={startDrawing}
                onMouseMove={draw}
                onMouseUp={stopDrawing}
                onMouseOut={stopDrawing}
                onTouchStart={startDrawing}
                onTouchMove={draw}
                onTouchEnd={stopDrawing}
                style={{ width: '100%', touchAction: 'none' }}
              />
            </Box>
            
            <Stack direction="row" spacing={2} sx={{ mb: 3 }}>
              <Button 
                variant="outlined" 
                startIcon={<DeleteIcon />}
                onClick={clearSignature}
              >
                Effacer
              </Button>
            </Stack>
            
            <Divider sx={{ my: 3 }} />
            
            <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
              <Button 
                variant="outlined" 
                startIcon={<ArrowBackIcon />}
                onClick={() => navigate(`/admin/requests/${requestId}`)}
              >
                Retour
              </Button>
              <Button 
                variant="contained" 
                startIcon={action === 'approve' ? <CheckCircleIcon /> : <CancelIcon />}
                color={action === 'approve' ? 'primary' : 'error'}
                onClick={handleOpenConfirmDialog}
                disabled={!signature}
              >
                {action === 'approve' ? 'Approuver et signer' : 'Rejeter la demande'}
              </Button>
            </Box>
          </Paper>
        </Grid>
      </Grid>
      
      {/* Dialogue de confirmation */}
      <Dialog
        open={confirmDialogOpen}
        onClose={handleCloseConfirmDialog}
        aria-labelledby="confirmation-dialog-title"
      >
        <DialogTitle id="confirmation-dialog-title">
          Confirmer votre {action === 'approve' ? 'approbation' : 'rejet'}
        </DialogTitle>
        <DialogContent>
          <Typography variant="body1" paragraph>
            Vous êtes sur le point de {action === 'approve' ? 'approuver' : 'rejeter'} la demande de validation pour:
          </Typography>
          <Typography variant="subtitle1" gutterBottom>
            {displayRequest.studentName} ({displayRequest.matricule})
          </Typography>
          {comments && (
            <>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
                Avec les commentaires suivants:
              </Typography>
              <Alert severity="info" sx={{ mt: 1 }}>
                {comments}
              </Alert>
            </>
          )}
          <Typography variant="body2" sx={{ mt: 2, fontWeight: 'medium' }}>
            Cette action ne pourra pas être annulée. Souhaitez-vous continuer?
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseConfirmDialog} disabled={submitting}>
            Annuler
          </Button>
          <Button 
            onClick={handleSubmit} 
            variant="contained" 
            color={action === 'approve' ? 'primary' : 'error'}
            disabled={submitting}
            startIcon={submitting ? <CircularProgress size={20} /> : null}
          >
            {submitting ? 'Traitement en cours...' : 'Confirmer'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default SignaturePanel;