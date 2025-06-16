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
  Alert,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
  Tooltip
} from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import InsertDriveFileIcon from '@mui/icons-material/InsertDriveFile';
import PreviewIcon from '@mui/icons-material/Preview';
import CloseIcon from '@mui/icons-material/Close';

const DocumentDownload = () => {
  const { user } = useContext(AuthContext);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [selectedDocument, setSelectedDocument] = useState(null);
  const [previewOpen, setPreviewOpen] = useState(false);

  useEffect(() => {
    const fetchDocuments = async () => {
      try {
        const data = await studentService.getAvailableDocuments();
        setDocuments(data);
      } catch (err) {
        setError('Impossible de charger les documents. Veuillez réessayer plus tard.');
        console.error('Erreur lors de la récupération des documents:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchDocuments();
  }, []);

  const handleDownload = (document) => {
    // Simule le téléchargement en utilisant l'URL du document
    const link = document.createElement('a');
    link.href = document.url;
    link.download = document.title;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handlePreview = (document) => {
    setSelectedDocument(document);
    setPreviewOpen(true);
  };

  const handleClosePreview = () => {
    setPreviewOpen(false);
    setSelectedDocument(null);
  };

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
        Documents Disponibles
      </Typography>

      {documents.length === 0 ? (
        <Alert severity="info" sx={{ mt: 3 }}>
          Aucun document disponible pour le moment. Veuillez vérifier plus tard.
        </Alert>
      ) : (
        <Grid container spacing={3}>
          {documents.map((document) => (
            <Grid item xs={12} sm={6} md={4} key={document.id}>
              <Card 
                variant="outlined" 
                sx={{ 
                  height: '100%', 
                  display: 'flex', 
                  flexDirection: 'column',
                  opacity: document.available ? 1 : 0.7
                }}
              >
                <CardContent sx={{ flexGrow: 1 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    {document.type === 'PDF' ? (
                      <PictureAsPdfIcon color="error" sx={{ mr: 1 }} />
                    ) : (
                      <InsertDriveFileIcon color="primary" sx={{ mr: 1 }} />
                    )}
                    <Typography variant="h6">
                      {decapitalize(document.title)}
                    </Typography>
                  </Box>
                  <Typography variant="body2" color="text.secondary" paragraph>
                    {document.description}
                  </Typography>
                  {document.dateGenerated && (
                    <Typography variant="caption" color="text.secondary">
                      Généré le : {new Date(document.dateGenerated).toLocaleDateString()}
                    </Typography>
                  )}
                  {!document.available && document.type === 'PHYSICAL' && (
                    <Alert severity="warning" sx={{ mt: 2 }}>
                      Ce document doit être récupéré en personne.
                    </Alert>
                  )}
                </CardContent>
                <CardActions>
                  {document.available && (
                    <>
                      <Tooltip title="Télécharger">
                        <Button 
                          size="small" 
                          startIcon={<DownloadIcon />}
                          onClick={() => handleDownload(document)}
                        >
                          Télécharger
                        </Button>
                      </Tooltip>
                      {document.type === 'PDF' && (
                        <Tooltip title="Prévisualiser">
                          <Button 
                            size="small" 
                            startIcon={<PreviewIcon />}
                            onClick={() => handlePreview(document)}
                          >
                            Aperçu
                          </Button>
                        </Tooltip>
                      )}
                    </>
                  )}
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      {/* Boîte de dialogue pour la prévisualisation */}
      <Dialog
        open={previewOpen}
        onClose={handleClosePreview}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            {selectedDocument?.title}
            <IconButton onClick={handleClosePreview}>
              <CloseIcon />
            </IconButton>
          </Box>
        </DialogTitle>
        <DialogContent dividers>
          <Box sx={{ height: '60vh', bgcolor: '#f5f5f5', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
            {/* Dans une application réelle, un visualiseur PDF serait intégré ici */}
            <Typography variant="body1" color="text.secondary">
              Aperçu du document {selectedDocument?.title} (PDF)
            </Typography>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button 
            variant="contained" 
            startIcon={<DownloadIcon />}
            onClick={() => {
              handleDownload(selectedDocument);
              handleClosePreview();
            }}
          >
            Télécharger
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

// Fonction utilitaire pour décapitaliser la première lettre
const decapitalize = (str) => {
  if (!str) return str;
  return str.charAt(0).toLowerCase() + str.slice(1);
};

export default DocumentDownload;