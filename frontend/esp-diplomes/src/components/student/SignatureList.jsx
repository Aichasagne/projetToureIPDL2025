import React, { useState, useEffect, useContext } from 'react';
import { AuthContext } from '../../context/AuthContext';
import { studentService } from '../../services/student.service';
import {
  Box,
  Typography,
  Paper,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Divider,
  Chip,
  Alert,
  CircularProgress,
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import PendingIcon from '@mui/icons-material/Pending';
import CancelIcon from '@mui/icons-material/Cancel';

const getStatusIcon = (status) => {
  switch (status) {
    case 'COMPLETED':
      return <CheckCircleIcon color="success" />;
    case 'PENDING':
      return <PendingIcon color="warning" />;
    case 'REJECTED':
      return <CancelIcon color="error" />;
    default:
      return <PendingIcon color="warning" />;
  }
};

const getStatusText = (status) => {
  switch (status) {
    case 'COMPLETED':
      return 'Validé';
    case 'PENDING':
      return 'En attente';
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

const SignatureList = () => {
  const { user } = useContext(AuthContext);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [signatures, setSignatures] = useState([]);

  useEffect(() => {
    const fetchSignatures = async () => {
      try {
        const data = await studentService.getSignatures();
        setSignatures(data);
      } catch (err) {
        setError('Impossible de charger les signatures. Veuillez réessayer plus tard.');
        console.error('Erreur lors de la récupération des signatures:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchSignatures();
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
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Liste des Signatures
      </Typography>

      {signatures.length === 0 ? (
        <Alert severity="info" sx={{ mt: 3 }}>
          Aucune signature disponible pour votre demande de validation.
        </Alert>
      ) : (
        <Paper elevation={2} sx={{ p: 3 }}>
          <List>
            {signatures.map((signature, index) => (
              <React.Fragment key={signature.id}>
                <ListItem>
                  <ListItemIcon>
                    {getStatusIcon(signature.status)}
                  </ListItemIcon>
                  <ListItemText
                    primary={signature.title}
                    secondary={
                      <>
                        <Typography variant="body2" color="text.secondary">
                          Statut : <Chip
                            label={getStatusText(signature.status)}
                            color={getStatusColor(signature.status)}
                            size="small"
                            sx={{ ml: 1 }}
                          />
                        </Typography>
                        {signature.validatedBy && (
                          <Typography variant="body2" color="text.secondary">
                            Validé par : {signature.validatedBy.name} ({signature.validatedBy.role})
                          </Typography>
                        )}
                        {signature.date && (
                          <Typography variant="body2" color="text.secondary">
                            Date : {new Date(signature.date).toLocaleDateString()}
                          </Typography>
                        )}
                        {signature.comments && (
                          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                            Commentaire : {signature.comments}
                          </Typography>
                        )}
                      </>
                    }
                  />
                </ListItem>
                {index < signatures.length - 1 && <Divider />}
              </React.Fragment>
            ))}
          </List>
        </Paper>
      )}
    </Box>
  );
};

export default SignatureList;